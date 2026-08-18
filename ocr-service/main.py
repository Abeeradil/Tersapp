import base64
import json
import os
from typing import Optional

from fastapi import FastAPI, File, HTTPException, UploadFile
from openai import OpenAI
from pydantic import BaseModel, ConfigDict

app = FastAPI(title="Tersapp Istimara OCR", version="1.0.0")

MAX_FILE_SIZE = 10 * 1024 * 1024
ALLOWED_MEDIA_TYPES = {"image/jpeg", "image/png", "image/webp"}

class IstimaraData(BaseModel):
    model_config = ConfigDict(extra="forbid")

    plate_number: Optional[str] = None
    plate_text_ar: Optional[str] = None
    plate_text_en: Optional[str] = None
    vehicle_make: Optional[str] = None
    vehicle_model: Optional[str] = None
    model_year: Optional[str] = None
    color: Optional[str] = None
    vin: Optional[str] = None
    owner_name: Optional[str] = None

INSTRUCTIONS = """
You extract data from Saudi vehicle registration (Istimara) images.
Return only fields visible in the image. Never invent a value. Use null when a
field is absent, cut off, blurred, or uncertain. Transcribe Arabic and Latin
text exactly as printed. Keep VIN uppercase with no spaces. Return model_year
as four digits when visible. vehicle_make and vehicle_model should be the
printed make/model, not a translated or guessed value.
"""

def image_data_url(contents: bytes, media_type: str) -> str:
    encoded = base64.b64encode(contents).decode("ascii")
    return f"data:{media_type};base64,{encoded}"

@app.get("/health")
def health() -> dict:
    return {"status": "ok"}

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
        response = OpenAI().responses.create(
            model=os.getenv("OPENAI_MODEL", "gpt-4.1-mini"),
            instructions=INSTRUCTIONS,
            input=[{
                "role": "user",
                "content": [
                    {
                        "type": "input_text",
                        "text": "Extract the vehicle registration fields from this image.",
                    },
                    {
                        "type": "input_image",
                        "image_url": image_data_url(contents, file.content_type),
                        "detail": "high",
                    },
                ],
            }],
            text={
                "format": {
                    "type": "json_schema",
                    "name": "istimara_data",
                    "schema": IstimaraData.model_json_schema(),
                    "strict": True,
                }
            },
        )
        data = IstimaraData.model_validate(json.loads(response.output_text))
        return {"success": True, "data": data.model_dump()}
    except json.JSONDecodeError as exc:
        raise HTTPException(502, "The OCR provider returned invalid JSON.") from exc
   except Exception as exc:
       print(f"OCR provider error: {type(exc).__name__}: {exc}")
       raise HTTPException(502, f"OCR provider request failed: {exc}") from exc