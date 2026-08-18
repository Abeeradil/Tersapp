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


# =========================================================
# APP
# =========================================================

app = FastAPI(
    title="Tersapp Istimara OCR",
    version="5.0.0"
)


# =========================================================
# CONFIG
# =========================================================

MAX_FILE_SIZE = 10 * 1024 * 1024
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
# VEHICLES
# =========================================================

VEHICLES = {
    "Toyota": [
        "Camry",
        "Corolla",
        "Yaris",
        "Land Cruiser",
        "Hilux",
        "RAV4",
        "Prado",
        "Fortuner",
    ],
    "Hyundai": [
        "Elantra",
        "Sonata",
        "Accent",
        "Tucson",
        "Santa Fe",
    ],
    "Kia": [
        "K5",
        "Cerato",
        "Sportage",
        "Rio",
        "Seltos",
    ],
    "Nissan": [
        "Altima",
        "Sunny",
        "Patrol",
        "X-Trail",
    ],
    "Honda": [
        "Accord",
        "Civic",
        "City",
    ],
    "Mazda": [
        "Mazda 3",
        "Mazda 6",
        "CX-5",
    ],
    "Chevrolet": [
        "Tahoe",
        "Malibu",
        "Captiva",
    ],
    "Ford": [
        "Taurus",
        "Explorer",
        "Edge",
    ],
    "GMC": [
        "Yukon",
        "Terrain",
    ],
    "MG": [
        "MG 5",
        "MG 6",
        "ZS",
        "RX5",
    ],
    "Changan": [
        "CS35",
        "CS55",
        "CS75",
    ],
    "Geely": [
        "Emgrand",
        "Coolray",
        "Monjaro",
    ],
}


BRAND_ALIASES = {
    "Toyota": [
        "toyota",
        "تويوتا",
        "تويوتا",
    ],
    "Hyundai": [
        "hyundai",
        "هيونداي",
        "هيونداى",
    ],
    "Kia": [
        "kia",
        "كيا",
    ],
    "Nissan": [
        "nissan",
        "نيسان",
    ],
    "Honda": [
        "honda",
        "هوندا",
    ],
    "Mazda": [
        "mazda",
        "مازدا",
    ],
    "Chevrolet": [
        "chevrolet",
        "شيفروليه",
    ],
    "Ford": [
        "ford",
        "فورد",
    ],
    "GMC": [
        "gmc",
        "جي ام سي",
        "جي إم سي",
    ],
    "MG": [
        "mg",
        "ام جي",
        "إم جي",
    ],
    "Changan": [
        "changan",
        "شانجان",
    ],
    "Geely": [
        "geely",
        "جيلي",
    ],
}


# =========================================================
# OCR
# =========================================================

ocr: Optional[PaddleOCR] = None


def engine() -> PaddleOCR:

    global ocr

    if ocr is None:

        print("====================================")
        print("Initializing PaddleOCR...")
        print("====================================")

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
# MODEL
# =========================================================

@dataclass
class TextItem:

    text: str
    score: float
    box: list


# =========================================================
# TEXT
# =========================================================

def normalize(value: str) -> str:

    if not value:
        return ""

    return (
        value
        .translate(ARABIC_DIGITS)
        .replace("ـ", "")
        .replace("\n", " ")
        .replace("\r", " ")
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


# =========================================================
# BOX
# =========================================================

def box_center(box: list):

    if not box or len(box) < 4:
        return 0.0, 0.0

    try:

        # [x1, y1, x2, y2]

        x1, y1, x2, y2 = map(
            float,
            box[:4]
        )

        return (
            (x1 + x2) / 2,
            (y1 + y2) / 2
        )

    except Exception:

        return 0.0, 0.0


def box_size(box: list):

    if not box or len(box) < 4:
        return 0.0, 0.0

    try:

        x1, y1, x2, y2 = map(
            float,
            box[:4]
        )

        return (
            abs(x2 - x1),
            abs(y2 - y1)
        )

    except Exception:

        return 0.0, 0.0


# =========================================================
# IMAGE QUALITY
# =========================================================

def quality_for(image: np.ndarray):

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

    issues = []

    if sharpness < MIN_SHARPNESS:
        issues.append("IMAGE_BLURRY")

    return sharpness, issues


# =========================================================
# OCR RESULT
# =========================================================

def read_items(
    image_path: str
):

    results = engine().predict(
        image_path
    )

    # PaddleOCR 3.x predict is normally
    # a generator. Some versions/configurations
    # may return a list.

    if isinstance(results, list):

        if not results:
            return [], 0

        result = results[0]

    else:

        result = next(results)

    payload = result.json

    if not isinstance(payload, dict):

        raise ValueError(
            "Unexpected PaddleOCR result."
        )

    payload = payload.get(
        "res",
        payload
    )

    if not isinstance(payload, dict):

        raise ValueError(
            "Unexpected PaddleOCR payload."
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

    items = []

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

        if hasattr(
            box,
            "tolist"
        ):
            box = box.tolist()

        items.append(
            TextItem(
                text=text,
                score=float(score),
                box=box
            )
        )

    # PaddleOCR gives document angle
    # as part of doc_preprocessor_res.

    preprocessor = payload.get(
        "doc_preprocessor_res",
        {}
    )

    if not isinstance(
        preprocessor,
        dict
    ):
        preprocessor = {}

    angle = preprocessor.get(
        "angle",
        0
    )

    try:
        angle = int(angle)
    except Exception:
        angle = 0

    print(
        f"OCR detected {len(items)} text items."
    )

    for item in items:

        print(
            f"[OCR {item.score:.2f}] "
            f"{item.text}"
        )

    return items, angle


# =========================================================
# ALL TEXT
# =========================================================

def all_text(
    items: list[TextItem]
):

    return "\n".join(
        item.text
        for item in items
    )


# =========================================================
# VIN
# =========================================================

def find_vin(
    items: list[TextItem]
):

    # First search each OCR item.

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

    # Then combined text.

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

    if match:

        return match.group(0)

    return None


# =========================================================
# YEAR
# =========================================================

def find_year(
    items: list[TextItem]
):

    for item in items:

        text = item.text.translate(
            ARABIC_DIGITS
        )

        match = re.search(
            r"(?:19|20)\d{2}",
            text
        )

        if match:

            year = int(
                match.group(0)
            )

            if 1950 <= year <= 2035:

                return str(year)

    return None


# =========================================================
# SAUDI PLATE
# =========================================================

SAUDI_ARABIC_TO_ENGLISH = {

    "ا": "A",
    "أ": "A",
    "إ": "A",
    "آ": "A",

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
):

    text = text.translate(
        ARABIC_DIGITS
    )

    result = []

    for char in text:

        if char in SAUDI_ARABIC_TO_ENGLISH:

            result.append(
                SAUDI_ARABIC_TO_ENGLISH[
                    char
                ]
            )

        elif (
            char.isascii()
            and char.isalnum()
        ):

            result.append(
                char.upper()
            )

        elif char.isdigit():

            result.append(char)

    return "".join(result)


def plate_score(
    value: str
):

    letters = sum(
        c.isalpha()
        for c in value
    )

    digits = sum(
        c.isdigit()
        for c in value
    )

    score = 0

    if letters == 3:
        score += 5

    elif letters == 2:
        score += 3

    elif letters == 1:
        score += 1

    if 1 <= digits <= 4:
        score += 4

    if 4 <= len(value) <= 7:
        score += 3

    return score


def extract_plate(
    items: list[TextItem]
):

    candidates = []

    for item in items:

        normalized = normalize_plate_text(
            item.text
        )

        if not normalized:
            continue

        # Whole item

        score = plate_score(
            normalized
        )

        if score >= 6:

            candidates.append(
                (
                    score,
                    item.score,
                    normalized
                )
            )

        # Split combinations

        parts = re.findall(
            r"[A-Z]+|\d+",
            normalized
        )

        if len(parts) >= 2:

            letters = ""
            digits = ""

            for part in parts:

                if part.isalpha():
                    letters += part

                elif part.isdigit():
                    digits += part

            if (
                1 <= len(letters) <= 3
                and
                1 <= len(digits) <= 4
            ):

                candidate = (
                    letters
                    + digits
                )

                score = plate_score(
                    candidate
                )

                if score >= 6:

                    candidates.append(
                        (
                            score,
                            item.score,
                            candidate
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
# BRAND / MODEL
# =========================================================

def detect_vehicle(
    items: list[TextItem]
):

    text = normalize(
        all_text(items)
    )

    brand = None
    model = None

    # Brand

    for name, aliases in BRAND_ALIASES.items():

        for alias in aliases:

            if normalize(alias) in text:

                brand = name
                break

        if brand:
            break

    # Model

    if brand:

        for vehicle_model in VEHICLES.get(
            brand,
            []
        ):

            if normalize(
                vehicle_model
            ) in text:

                model = vehicle_model
                break

    return brand, model


# =========================================================
# LABELS
# =========================================================

LABELS = {

    "make": [
        "ماركة المركبة",
        "ماركة المركبه",
        "ماركة المركبة",
        "ماركه المركبه",
        "ماركة",
        "vehicle make",
        "car make",
        "make",
    ],

    "model": [
        "طراز المركبة",
        "طراز المركبه",
        "طراز",
        "موديل المركبة",
        "موديل المركبه",
        "موديل",
        "vehicle model",
        "car model",
        "model",
    ],

    "year": [
        "سنة الصنع",
        "سنه الصنع",
        "سنة",
        "سنه",
        "year",
        "model year",
        "vehicle year",
    ],

    "color": [
        "لون المركبة",
        "لون المركبه",
        "لون",
        "color",
        "colour",
        "vehicle color",
        "vehicle colour",
    ],

    "owner": [
        "اسم المالك",
        "اسم مالك",
        "المالك",
        "owner name",
        "owner",
    ],
}


def label_kind(
    text: str
):

    source = normalize(
        text
    )

    for kind, aliases in LABELS.items():

        for alias in aliases:

            if normalize(alias) in source:

                return kind

    return None


# =========================================================
# VALUE EXTRACTION
# =========================================================

def value_for_label(
    label: TextItem,
    items: list[TextItem]
):

    lx, ly = box_center(
        label.box
    )

    lw, lh = box_size(
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

        # Same line

        same_row = (
            abs(dy)
            <= max(
                lh * 2.0,
                30
            )
        )

        # Directly below

        below = (
            dy > 0
            and
            dy
            <= max(
                lh * 4.0,
                80
            )
        )

        if not same_row and not below:
            continue

        distance_score = (
            abs(dx)
            +
            abs(dy) * 1.5
        )

        if same_row:

            distance_score *= 0.5

        candidates.append(
            (
                distance_score,
                -item.score,
                item
            )
        )

    if not candidates:
        return None

    candidates.sort(
        key=lambda x: (
            x[0],
            x[1]
        )
    )

    return candidates[0][2]


# =========================================================
# LABELED FIELDS
# =========================================================

def extract_labeled_fields(
    items: list[TextItem]
):

    result = {

        "vehicle_make": None,
        "vehicle_model": None,
        "model_year": None,
        "color": None,
        "owner_name": None,
    }

    for item in items:

        kind = label_kind(
            item.text
        )

        if not kind:
            continue

        value = value_for_label(
            item,
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

            result[
                "vehicle_make"
            ] = text

        elif kind == "model":

            result[
                "vehicle_model"
            ] = text

        elif kind == "year":

            match = re.search(
                r"(?:19|20)\d{2}",
                text.translate(
                    ARABIC_DIGITS
                )
            )

            if match:

                year = int(
                    match.group(0)
                )

                if 1950 <= year <= 2035:

                    result[
                        "model_year"
                    ] = str(year)

        elif kind == "color":

            result[
                "color"
            ] = text

        elif kind == "owner":

            result[
                "owner_name"
            ] = text

    return result


# =========================================================
# FINAL EXTRACTION
# =========================================================

def extract_data(
    items: list[TextItem]
):

    labeled = extract_labeled_fields(
        items
    )

    detected_brand, detected_model = (
        detect_vehicle(items)
    )

    brand = (
        labeled["vehicle_make"]
        or detected_brand
    )

    model = (
        labeled["vehicle_model"]
        or detected_model
    )

    year = (
        labeled["model_year"]
        or find_year(items)
    )

    plate = extract_plate(
        items
    )

    vin = find_vin(
        items
    )

    return {

        "plate_number": plate,

        "plate_text_ar": None,

        "plate_text_en": plate,

        "vehicle_make": brand,

        "vehicle_model": model,

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
# HEALTH
# =========================================================

@app.get("/health")
def health():

    return {

        "status": "ok",

        "engine": "paddleocr-v3",

        "languages": [
            "ar",
            "en"
        ]
    }


# =========================================================
# OCR ENDPOINT
# =========================================================

@app.post("/extract-istimara")
async def extract_istimara(
    file: UploadFile = File(...)
):

    # -----------------------------------------------------
    # File validation
    # -----------------------------------------------------

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

    # -----------------------------------------------------
    # Decode image
    # -----------------------------------------------------

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

    # -----------------------------------------------------
    # Blur check only
    # -----------------------------------------------------

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

    # -----------------------------------------------------
    # Temporary image
    # -----------------------------------------------------

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

        # -------------------------------------------------
        # OCR
        # -------------------------------------------------

        items, angle = read_items(
            temporary_path
        )

    except Exception as exc:

        print(
            "===================================="
        )

        print(
            "PaddleOCR ERROR"
        )

        print(
            "TYPE:",
            type(exc).__name__
        )

        print(
            "MESSAGE:",
            str(exc)
        )

        print(
            "===================================="
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
            and
            os.path.exists(
                temporary_path
            )
        ):

            os.unlink(
                temporary_path
            )

    # -----------------------------------------------------
    # Extract
    # -----------------------------------------------------

    data = extract_data(
        items
    )

    # -----------------------------------------------------
    # Confidence
    # -----------------------------------------------------

    if items:

        average_confidence = (
            sum(
                item.score
                for item in items
            )
            /
            len(items)
        )

    else:

        average_confidence = 0.0

    # -----------------------------------------------------
    # Missing fields
    # -----------------------------------------------------

    missing = []

    if not data[
        "plate_number"
    ]:

        missing.append(
            "plate_number"
        )

    if not data[
        "vehicle_make"
    ]:

        missing.append(
            "vehicle_make"
        )

    if not data[
        "vehicle_model"
    ]:

        missing.append(
            "vehicle_model"
        )

    if not data[
        "model_year"
    ]:

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

    # -----------------------------------------------------
    # Success
    # -----------------------------------------------------

    accepted = (
        data["plate_number"] is not None
        or
        data["vin"] is not None
    )

    # -----------------------------------------------------
    # Response
    # -----------------------------------------------------

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

            "sharpness": round(
                sharpness,
                2
            ),

            "width": width,

            "height": height,

            "detected_text_count": len(
                items
            ),
        },

        "ocr_text": [

            {
                "text": item.text,

                "score": round(
                    item.score,
                    3
                ),

                "box": item.box
            }

            for item in items
        ],
    }