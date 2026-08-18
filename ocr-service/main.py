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

app = FastAPI(title="Tersapp Istimara OCR", version="3.0.0")

MAX_FILE_SIZE = 10 * 1024 * 1024
MIN_WIDTH = 700
MIN_HEIGHT = 450
MIN_SHARPNESS = 45.0

# Add aliases here only when the same spelling exists in the Java car-brand/model database.
VEHICLES = {
    "Toyota": ["Camry", "Corolla", "Yaris", "Land Cruiser", "Hilux", "RAV4", "Prado", "Fortuner"],
    "Hyundai": ["Elantra", "Sonata", "Accent", "Tucson", "Santa Fe"],
    "Kia": ["K5", "Cerato", "Sportage", "Rio", "Seltos"],
    "Nissan": ["Altima", "Sunny", "Patrol", "X-Trail"],
    "Honda": ["Accord", "Civic", "City"],
    "Mazda": ["Mazda 3", "Mazda 6", "CX-5"],
    "Chevrolet": ["Tahoe", "Malibu", "Captiva"],
    "Ford": ["Taurus", "Explorer", "Edge"],
    "GMC": ["Yukon", "Terrain"],
    "MG": ["MG 5", "MG 6", "ZS", "RX5"],
    "Changan": ["CS35", "CS55", "CS75"],
    "Geely": ["Emgrand", "Coolray", "Monjaro"],
}
BRAND_ALIASES = {
    "Toyota": ["toyota", "تويوتا"], "Hyundai": ["hyundai", "هيونداي"],
    "Kia": ["kia", "كيا"], "Nissan": ["nissan", "نيسان"],
    "Honda": ["honda", "هوندا"], "Mazda": ["mazda", "مازدا"],
    "Chevrolet": ["chevrolet", "شيفروليه"], "Ford": ["ford", "فورد"],
    "GMC": ["gmc", "جي ام سي"], "MG": ["mg", "ام جي"],
    "Changan": ["changan", "شانجان"], "Geely": ["geely", "جيلي"],
}
ARABIC_DIGITS = str.maketrans("٠١٢٣٤٥٦٧٨٩", "0123456789")

ocr: Optional[PaddleOCR] = None

def engine() -> PaddleOCR:
    global ocr
    if ocr is None:
        # PaddleOCR v3 document preprocessing corrects rotation and mild perspective
        # distortion before reading. The Arabic recognizer also reads Arabic digits.
        ocr = PaddleOCR(
            lang="ar",
            use_doc_orientation_classify=True,
            use_doc_unwarping=True,
            use_textline_orientation=True,
            text_recognition_model_name="arabic_PP-OCRv3_mobile_rec",
            engine="paddle",
        )
    return ocr

def normalize(value: str) -> str:
    return value.translate(ARABIC_DIGITS).replace("ـ", "").lower().strip()

@dataclass
class TextItem:
    text: str
    score: float
    box: list

def quality_for(image: np.ndarray) -> tuple[float, list[str]]:
    height, width = image.shape[:2]
    issues: list[str] = []
    if width < MIN_WIDTH or height < MIN_HEIGHT:
        issues.append("IMAGE_RESOLUTION_TOO_LOW")
    sharpness = float(cv2.Laplacian(cv2.cvtColor(image, cv2.COLOR_BGR2GRAY), cv2.CV_64F).var())
    if sharpness < MIN_SHARPNESS:
        issues.append("IMAGE_BLURRY")
    return sharpness, issues

def read_items(image_path: str) -> tuple[list[TextItem], int]:
    result = next(engine().predict(image_path))
    payload = result.json
    payload = payload.get("res", payload)
    texts = payload.get("rec_texts", [])
    scores = payload.get("rec_scores", [])
    boxes = payload.get("rec_boxes", [])
    items = [
        TextItem(text=str(text).strip(), score=float(score), box=box.tolist() if hasattr(box, "tolist") else box)
        for text, score, box in zip(texts, scores, boxes)
        if str(text).strip()
    ]
    angle = int(payload.get("doc_preprocessor_res", {}).get("angle", 0) or 0)
    return items, angle

def detect_vehicle(text: str) -> tuple[Optional[str], Optional[str]]:
    source = normalize(text)
    brand = next(
        (name for name, aliases in BRAND_ALIASES.items()
         if any(normalize(alias) in source for alias in aliases)),
        None,
    )
    if not brand:
        return None, None
    model = next((model for model in VEHICLES[brand] if normalize(model) in source), None)
    return brand, model

def extract_plate(text: str) -> Optional[str]:
    source = text.translate(ARABIC_DIGITS).upper()
    # Saudi plate lines normally contain three letters and up to four digits.
    match = re.search(r"\b(?:[A-Z]\s*){1,3}(?:\d\s*){1,4}\b", source)
    if match:
        return re.sub(r"\s+", " ", match.group(0)).strip()
    match = re.search(r"\b(?:\d\s*){1,4}(?:[A-Z]\s*){1,3}\b", source)
    return re.sub(r"\s+", " ", match.group(0)).strip() if match else None

def extract_data(items: list[TextItem]) -> dict:
    text = "\n".join(item.text for item in items)
    brand, model = detect_vehicle(text)
    vin = re.search(r"(?<![A-Z0-9])[A-HJ-NPR-Z0-9]{17}(?![A-Z0-9])", text.upper())
    years = re.findall(r"\b(?:19|20)\d{2}\b", text.translate(ARABIC_DIGITS))
    return {
        "plate_number": extract_plate(text),
        "plate_text_ar": None,
        "plate_text_en": None,
        "vehicle_make": brand,
        "vehicle_model": model,
        "model_year": years[0] if years else None,
        "color": None,
        "vin": vin.group(0) if vin else None,
        "owner_name": None,
    }

@app.get("/health")
def health() -> dict:
    return {"status": "ok", "engine": "paddleocr-v3", "languages": ["ar", "en"]}

@app.post("/extract-istimara")
async def extract_istimara(file: UploadFile = File(...)) -> dict:
    if file.content_type not in {"image/jpeg", "image/png", "image/webp"}:
        raise HTTPException(415, "Upload a JPEG, PNG, or WEBP image.")

    contents = await file.read()
    if not contents:
        raise HTTPException(400, "The uploaded image is empty.")
    if len(contents) > MAX_FILE_SIZE:
        raise HTTPException(413, "The image must not exceed 10 MB.")

    image = cv2.imdecode(np.frombuffer(contents, np.uint8), cv2.IMREAD_COLOR)
    if image is None:
        raise HTTPException(400, "The uploaded file is not a valid image.")

    sharpness, issues = quality_for(image)
    height, width = image.shape[:2]
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
                "sharpness": round(sharpness, 2),
            },
        }

    suffix = Path(file.filename or "istimara.jpg").suffix or ".jpg"
    try:
        with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as temporary:
            temporary.write(contents)
            temporary_path = temporary.name
        items, angle = read_items(temporary_path)
    except Exception as exc:print(f"PaddleOCR error: {type(exc).__name__}: {exc}")
            raise HTTPException(
                502,
                f"PaddleOCR could not process this image: {type(exc).__name__}: {exc}"
            ) from exc
    finally:
        if "temporary_path" in locals():
            os.unlink(temporary_path)

    data = extract_data(items)
    average_confidence = sum(item.score for item in items) / len(items) if items else 0
    required = ["plate_number", "vehicle_make", "vehicle_model"]
    missing = [field for field in required if not data[field]]
    if average_confidence < 0.70:
        issues.append("LOW_TEXT_CONFIDENCE")
    if missing:
        issues.append("REQUIRED_FIELDS_MISSING")

    accepted = not issues
    return {
        "success": accepted,
        "data": data,
        "quality": {
            "accepted": accepted,
            "score": round(average_confidence, 3),
            "issues": issues,
            "missing_fields": missing,
            "rotation_corrected_degrees": angle,
            "sharpness": round(sharpness, 2),
        },
    }
