import re
from typing import Optional

import cv2
import numpy as np
import pytesseract
from fastapi import FastAPI, File, HTTPException, UploadFile
from pytesseract import TesseractNotFoundError

app = FastAPI(title="Tersapp Istimara OCR", version="2.0.0")

MAX_FILE_SIZE = 10 * 1024 * 1024
ALLOWED_MEDIA_TYPES = {"image/jpeg", "image/png", "image/webp"}
ARABIC_DIGITS = str.maketrans("٠١٢٣٤٥٦٧٨٩", "0123456789")

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

def normalize(text: str) -> str:
    return text.translate(ARABIC_DIGITS).replace("ـ", "").lower()

def preprocess(contents: bytes) -> np.ndarray:
    image = cv2.imdecode(np.frombuffer(contents, np.uint8), cv2.IMREAD_COLOR)
    if image is None:
        raise HTTPException(400, "The uploaded file is not a valid image.")
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    gray = cv2.resize(gray, None, fx=2, fy=2, interpolation=cv2.INTER_CUBIC)
    gray = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8)).apply(gray)
    return cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)[1]

def value_after_label(text: str, labels: list[str]) -> Optional[str]:
    for label in labels:
        match = re.search(rf"{label}\s*[:\-]?\s*([^\n]{{2,80}})", text, re.IGNORECASE)
        if match:
            return match.group(1).strip()
    return None

def detect_vehicle(text: str) -> tuple[Optional[str], Optional[str]]:
    normalized = normalize(text)
    brand = next(
        (name for name, aliases in BRAND_ALIASES.items()
         if any(normalize(alias) in normalized for alias in aliases)),
        None,
    )
    if not brand:
        return None, None
    model = next((model for model in VEHICLES[brand] if normalize(model) in normalized), None)
    return brand, model

def extract_plate(text: str) -> Optional[str]:
    normalized = text.translate(ARABIC_DIGITS).upper()
    match = re.search(r"\b[A-Z]{1,3}\s*\d{1,4}\b", normalized)
    if match:
        return re.sub(r"\s+", " ", match.group(0))
    arabic = re.search(r"[أبحدرسصطعقكلمنهوي]{1,3}\s*\d{1,4}", normalized)
    return re.sub(r"\s+", " ", arabic.group(0)) if arabic else None

def extract_data(text: str) -> dict:
    brand, model = detect_vehicle(text)
    vin_match = re.search(r"(?<![A-Z0-9])[A-HJ-NPR-Z0-9]{17}(?![A-Z0-9])", text.upper())
    years = re.findall(r"\b(?:19|20)\d{2}\b", text.translate(ARABIC_DIGITS))
    return {
        "plate_number": extract_plate(text),
        "plate_text_ar": None,
        "plate_text_en": None,
        "vehicle_make": brand,
        "vehicle_model": model,
        "model_year": years[0] if years else None,
        "color": value_after_label(text, ["اللون", "color"]),
        "vin": vin_match.group(0) if vin_match else None,
        "owner_name": value_after_label(text, ["اسم المالك", "مالك المركبة", "owner name"]),
    }

@app.get("/health")
def health() -> dict:
    return {"status": "ok", "engine": "tesseract", "languages": "ara+eng"}

@app.post("/extract-istimara")
async def extract_istimara(file: UploadFile = File(...)) -> dict:
    if file.content_type not in ALLOWED_MEDIA_TYPES:
        raise HTTPException(415, "Upload a JPEG, PNG, or WEBP image.")

    contents = await file.read()
    if not contents:
        raise HTTPException(400, "The uploaded image is empty.")
    if len(contents) > MAX_FILE_SIZE:
        raise HTTPException(413, "The image must not exceed 10 MB.")

    try:
        raw_text = pytesseract.image_to_string(
            preprocess(contents), lang="ara+eng", config="--oem 1 --psm 6"
        )
    except TesseractNotFoundError as exc:
        raise HTTPException(500, "Tesseract is not installed in this deployment.") from exc

    data = extract_data(raw_text)
    required = ["plate_number", "vehicle_make", "vehicle_model"]
    missing = [field for field in required if not data[field]]
    return {
        "success": not missing,
        "data": data,
        "missing_fields": missing,
        "raw_text": raw_text,
    }
