import os
import re
import tempfile
from dataclasses import dataclass
from pathlib import Path
from typing import Optional

import cv2
import numpy as np
from fastapi import FastAPI, File, HTTPException, UploadFile
from paddleocr import PaddleOCR


app = FastAPI(
    title="Tersapp Istimara OCR",
    version="4.0.0"
)


# =========================================================
# Configuration
# =========================================================

MAX_FILE_SIZE = 10 * 1024 * 1024

# Do NOT reject a clear but relatively small image.
MIN_SHARPNESS = 45.0

SUPPORTED_TYPES = {
    "image/jpeg",
    "image/png",
    "image/webp",
}

ARABIC_DIGITS = str.maketrans(
    "٠١٢٣٤٥٦٧٨٩",
    "0123456789"
)


# =========================================================
# OCR Engine
# =========================================================

ocr: Optional[PaddleOCR] = None


def engine() -> PaddleOCR:
    global ocr

    if ocr is None:
        print("Initializing PaddleOCR...")

        ocr = PaddleOCR(
            lang="ar",
            use_doc_orientation_classify=True,
            use_doc_unwarping=True,
            use_textline_orientation=True,
            text_recognition_model_name="arabic_PP-OCRv3_mobile_rec",
        )

        print("PaddleOCR initialized successfully.")

    return ocr


# =========================================================
# Models
# =========================================================

@dataclass
class TextItem:
    text: str
    score: float
    box: list


# =========================================================
# Utilities
# =========================================================

def normalize(value: str) -> str:
    if not value:
        return ""

    return (
        value
        .translate(ARABIC_DIGITS)
        .replace("ـ", "")
        .strip()
        .lower()
    )


def clean_text(value: str) -> str:
    if not value:
        return ""

    return re.sub(
        r"\s+",
        " ",
        value
        .replace("\n", " ")
        .replace("\r", " ")
    ).strip()


def box_center(box: list) -> tuple[float, float]:
    """
    PaddleOCR rec_boxes format:
    [x_min, y_min, x_max, y_max]
    """

    if len(box) < 4:
        return 0, 0

    x1, y1, x2, y2 = map(float, box[:4])

    return (
        (x1 + x2) / 2,
        (y1 + y2) / 2
    )


def box_size(box: list) -> tuple[float, float]:
    if len(box) < 4:
        return 0, 0

    x1, y1, x2, y2 = map(float, box[:4])

    return (
        abs(x2 - x1),
        abs(y2 - y1)
    )


# =========================================================
# Image quality
# =========================================================

def quality_for(
    image: np.ndarray
) -> tuple[float, list[str]]:

    issues = []

    gray = cv2.cvtColor(
        image,
        cv2.COLOR_BGR2GRAY
    )

    sharpness = float(
        cv2.Laplacian(
            gray,
            cv2.CV_64F
        ).var()
    )

    if sharpness < MIN_SHARPNESS:
        issues.append("IMAGE_BLURRY")

    return sharpness, issues


# =========================================================
# PaddleOCR
# =========================================================

def read_items(
    image_path: str
) -> tuple[list[TextItem], int]:

    results = engine().predict(image_path)

    # PaddleOCR versions may return either
    # a list or an iterator.
    if isinstance(results, list):

        if not results:
            return [], 0

        result = results[0]

    else:

        result = next(results)

    payload = result.json

    if not isinstance(payload, dict):
        raise ValueError(
            "Unexpected PaddleOCR result format."
        )

    payload = payload.get(
        "res",
        payload
    )

    if not isinstance(payload, dict):
        raise ValueError(
            "Unexpected PaddleOCR payload format."
        )

    texts = payload.get(
        "rec_texts",
        []
    )

    scores = payload.get(
        "rec_scores",
        []
    )

    boxes = payload.get(
        "rec_boxes",
        []
    )

    items: list[TextItem] = []

    for text, score, box in zip(
        texts,
        scores,
        boxes
    ):

        text = clean_text(
            str(text)
        )

        if not text:
            continue

        if hasattr(box, "tolist"):
            box = box.tolist()

        items.append(
            TextItem(
                text=text,
                score=float(score),
                box=box
            )
        )

    doc_preprocessor = payload.get(
        "doc_preprocessor_res",
        {}
    )

    if not isinstance(
        doc_preprocessor,
        dict
    ):
        doc_preprocessor = {}

    angle = int(
        doc_preprocessor.get(
            "angle",
            0
        ) or 0
    )

    return items, angle


# =========================================================
# Text helpers
# =========================================================

def all_text(items: list[TextItem]) -> str:

    return "\n".join(
        item.text
        for item in items
    )


def find_year(
    items: list[TextItem]
) -> Optional[str]:

    for item in items:

        text = item.text.translate(
            ARABIC_DIGITS
        )

        match = re.search(
            r"\b(?:19|20)\d{2}\b",
            text
        )

        if match:
            return match.group(0)

    return None


def find_vin(
    items: list[TextItem]
) -> Optional[str]:

    # Search every OCR item separately first.
    for item in items:

        text = (
            item.text
            .upper()
            .replace(" ", "")
            .replace("-", "")
        )

        match = re.search(
            r"(?<![A-Z0-9])"
            r"[A-HJ-NPR-Z0-9]{17}"
            r"(?![A-Z0-9])",
            text
        )

        if match:
            return match.group(0)

    # Fallback: search combined OCR text.
    text = (
        all_text(items)
        .upper()
        .replace(" ", "")
        .replace("-", "")
    )

    match = re.search(
        r"(?<![A-Z0-9])"
        r"[A-HJ-NPR-Z0-9]{17}"
        r"(?![A-Z0-9])",
        text
    )

    return (
        match.group(0)
        if match
        else None
    )


# =========================================================
# Saudi plate
# =========================================================

# Saudi plates use a restricted set of Arabic letters.
# This mapping is useful when OCR recognizes Arabic plate
# characters separately from the numbers.

SAUDI_ARABIC_TO_ENGLISH = {
    "ا": "A",
    "ب": "B",
    "ح": "J",
    "د": "D",
    "ر": "R",
    "س": "S",
    "ص": "X",
    "ط": "T",
    "ع": "E",
    "ق": "G",
    "ك": "K",
    "ل": "L",
    "م": "Z",
    "ن": "N",
    "ه": "H",
    "و": "U",
    "ي": "V",
}


def normalize_plate_text(
    text: str
) -> str:

    text = text.translate(
        ARABIC_DIGITS
    )

    result = []

    for char in text:

        if char in SAUDI_ARABIC_TO_ENGLISH:
            result.append(
                SAUDI_ARABIC_TO_ENGLISH[char]
            )

        elif char.isascii() and char.isalnum():
            result.append(
                char.upper()
            )

        elif char.isdigit():
            result.append(char)

    return "".join(result)


def plate_candidate_score(
    value: str
) -> int:

    value = value.upper()

    letters = sum(
        c.isalpha()
        for c in value
    )

    digits = sum(
        c.isdigit()
        for c in value
    )

    score = 0

    if 1 <= letters <= 3:
        score += 3

    if 1 <= digits <= 4:
        score += 3

    if 4 <= len(value) <= 7:
        score += 2

    return score


def extract_plate(
    items: list[TextItem]
) -> Optional[str]:

    candidates = []

    for item in items:

        normalized = normalize_plate_text(
            item.text
        )

        if not normalized:
            continue

        # Whole OCR item
        score = plate_candidate_score(
            normalized
        )

        if score >= 5:
            candidates.append(
                (
                    score,
                    item.score,
                    normalized
                )
            )

        # Look for smaller chunks.
        chunks = re.findall(
            r"[A-Z]{1,3}\d{1,4}"
            r"|\d{1,4}[A-Z]{1,3}",
            normalized
        )

        for chunk in chunks:

            score = plate_candidate_score(
                chunk
            )

            if score >= 5:
                candidates.append(
                    (
                        score,
                        item.score,
                        chunk
                    )
                )

    if not candidates:
        return None

    candidates.sort(
        key=lambda x: (
            x[0],
            x[1]
        ),
        reverse=True
    )

    return candidates[0][2]


# =========================================================
# Label detection
# =========================================================

LABELS = {
    "plate": [
        "رقم اللوحة",
        "رقم لوحه",
        "plate number",
        "plate no",
        "plate"
    ],

    "make": [
        "ماركة المركبة",
        "ماركة المركبه",
        "ماركة",
        "car name",
        "vehicle make",
        "make"
    ],

    "model": [
        "طراز المركبة",
        "طراز المركبه",
        "طراز",
        "car model",
        "vehicle model",
        "model"
    ],

    "year": [
        "سنة الصنع",
        "سنه الصنع",
        "سنة",
        "car year",
        "vehicle year",
        "year"
    ],

    "vin": [
        "رقم الهيكل",
        "رقم هیکل",
        "رقم الشاسيه",
        "الشاسيه",
        "chassis number",
        "chassis",
        "vin",
        "vehicle identification"
    ],

    "color": [
        "لون المركبة",
        "لون المركبه",
        "لون",
        "car color",
        "vehicle colour",
        "vehicle color",
        "color",
        "colour"
    ],

    "owner": [
        "اسم المالك",
        "المالك",
        "owner name",
        "owner"
    ]
}


def label_kind(
    text: str
) -> Optional[str]:

    source = normalize(text)

    if not source:
        return None

    for kind, aliases in LABELS.items():

        for alias in aliases:

            if normalize(alias) in source:
                return kind

    return None


# =========================================================
# Spatial extraction
# =========================================================

def distance(
    a: tuple[float, float],
    b: tuple[float, float]
) -> float:

    return (
        (a[0] - b[0]) ** 2
        +
        (a[1] - b[1]) ** 2
    ) ** 0.5


def value_near_label(
    label: TextItem,
    items: list[TextItem]
) -> Optional[TextItem]:

    lx, ly = box_center(
        label.box
    )

    label_width, label_height = box_size(
        label.box
    )

    candidates = []

    for item in items:

        if item is label:
            continue

        if label_kind(item.text):
            continue

        text = clean_text(
            item.text
        )

        if not text:
            continue

        x, y = box_center(
            item.box
        )

        dx = x - lx
        dy = y - ly

        # Candidate on the same horizontal row.
        same_row = (
            abs(dy)
            <= max(
                label_height * 2.5,
                40
            )
        )

        # Candidate below the label.
        below = (
            dy > 0
            and dy
            <= max(
                label_height * 5,
                100
            )
        )

        if not same_row and not below:
            continue

        # Prefer values to the left/right
        # of the label before values far below it.
        score = abs(dy) * 2 + abs(dx)

        if same_row:
            score *= 0.5

        candidates.append(
            (
                score,
                item.score,
                item
            )
        )

    if not candidates:
        return None

    candidates.sort(
        key=lambda x: (
            x[0],
            -x[1]
        )
    )

    return candidates[0][2]


def extract_labeled_fields(
    items: list[TextItem]
) -> dict:

    result = {
        "vehicle_make": None,
        "vehicle_model": None,
        "model_year": None,
        "color": None,
        "owner_name": None,
    }

    labels = []

    for item in items:

        kind = label_kind(
            item.text
        )

        if kind:
            labels.append(
                (
                    kind,
                    item
                )
            )

    for kind, label in labels:

        value = value_near_label(
            label,
            items
        )

        if value is None:
            continue

        text = clean_text(
            value.text
        )

        if not text:
            continue

        if kind == "make":
            result["vehicle_make"] = text

        elif kind == "model":
            result["vehicle_model"] = text

        elif kind == "year":

            match = re.search(
                r"(?:19|20)\d{2}",
                text.translate(
                    ARABIC_DIGITS
                )
            )

            result["model_year"] = (
                match.group(0)
                if match
                else text
            )

        elif kind == "color":
            result["color"] = text

        elif kind == "owner":
            result["owner_name"] = text

    return result


# =========================================================
# Final extraction
# =========================================================

def extract_data(
    items: list[TextItem]
) -> dict:

    labeled = extract_labeled_fields(
        items
    )

    plate = extract_plate(
        items
    )

    vin = find_vin(
        items
    )

    year = (
        labeled["model_year"]
        or find_year(items)
    )

    return {
        "plate_number": plate,
        "plate_text_ar": None,
        "plate_text_en": plate,
        "vehicle_make": labeled[
            "vehicle_make"
        ],
        "vehicle_model": labeled[
            "vehicle_model"
        ],
        "model_year": year,
        "color": labeled[
            "color"
        ],
        "vin": vin,
        "owner_name": labeled[
            "owner_name"
        ],
    }


# =========================================================
# Health
# =========================================================

@app.get("/health")
def health() -> dict:

    return {
        "status": "ok",
        "engine": "paddleocr-v3",
        "languages": [
            "ar",
            "en"
        ]
    }


# =========================================================
# OCR endpoint
# =========================================================

@app.post("/extract-istimara")
async def extract_istimara(
    file: UploadFile = File(...)
) -> dict:

    if file.content_type not in SUPPORTED_TYPES:
        raise HTTPException(
            status_code=415,
            detail=(
                "Upload a JPEG, PNG, or WEBP image."
            )
        )

    contents = await file.read()

    if not contents:
        raise HTTPException(
            status_code=400,
            detail="The uploaded image is empty."
        )

    if len(contents) > MAX_FILE_SIZE:
        raise HTTPException(
            status_code=413,
            detail=(
                "The image must not exceed 10 MB."
            )
        )

    image = cv2.imdecode(
        np.frombuffer(
            contents,
            np.uint8
        ),
        cv2.IMREAD_COLOR
    )

    if image is None:
        raise HTTPException(
            status_code=400,
            detail=(
                "The uploaded file is not a valid image."
            )
        )

    height, width = image.shape[:2]

    sharpness, issues = quality_for(
        image
    )

    # Only reject genuinely blurry images.
    # Do not reject small but sharp images.
    if "IMAGE_BLURRY" in issues:

        return {
            "success": False,
            "data": {},
            "quality": {
                "accepted": False,
                "score": 0,
                "width": width,
                "height": height,
                "issues": issues,
                "sharpness": round(
                    sharpness,
                    2
                ),
            }
        }

    temporary_path = None

    suffix = (
        Path(
            file.filename
            or "istimara.jpg"
        ).suffix
        or ".jpg"
    )

    try:

        with tempfile.NamedTemporaryFile(
            suffix=suffix,
            delete=False
        ) as temporary:

            temporary.write(
                contents
            )

            temporary_path = (
                temporary.name
            )

        items, angle = read_items(
            temporary_path
        )

    except Exception as exc:

        print(
            "PaddleOCR error:",
            type(exc).__name__,
            str(exc)
        )

        raise HTTPException(
            status_code=502,
            detail=(
                "PaddleOCR could not process "
                f"this image: "
                f"{type(exc).__name__}: {exc}"
            )
        ) from exc

    finally:

        if (
            temporary_path
            and os.path.exists(
                temporary_path
            )
        ):
            os.unlink(
                temporary_path
            )

    data = extract_data(
        items
    )

    average_confidence = (
        sum(
            item.score
            for item in items
        )
        /
        len(items)
        if items
        else 0
    )

    missing = []

    # We no longer require make/model
    # to mark the whole OCR as failed.
    #
    # VIN is highly reliable when available.
    # Plate is also independently extracted.

    if not data["plate_number"]:
        missing.append(
            "plate_number"
        )

    if not data["vehicle_make"]:
        missing.append(
            "vehicle_make"
        )

    if not data["vehicle_model"]:
        missing.append(
            "vehicle_model"
        )

    if not data["model_year"]:
        missing.append(
            "model_year"
        )

    quality_issues = list(
        issues
    )

    if average_confidence < 0.70:
        quality_issues.append(
            "LOW_TEXT_CONFIDENCE"
        )

    if missing:
        quality_issues.append(
            "FIELDS_NOT_DETECTED"
        )

    # OCR itself succeeded even if some fields
    # were not detected.
    accepted = (
        data["plate_number"] is not None
        or data["vin"] is not None
    )

    return {
        "success": accepted,
        "data": data,
        "quality": {
            "accepted": accepted,
            "score": round(
                average_confidence,
                3
            ),
            "issues": quality_issues,
            "missing_fields": missing,
            "rotation_corrected_degrees": angle,
            "width": width,
            "height": height,
            "sharpness": round(
                sharpness,
                2
            ),
            "detected_text_count": len(items),
        }
    }