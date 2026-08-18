# Istimara OCR service (free)

A standalone FastAPI OCR service for Saudi vehicle-registration images. It runs
Tesseract locally with Arabic and English language packs, so it has no external
AI API charge and does not require an API key.

## Deploy on Railway

Deploy the `ocr-service` directory as its own Railway service. Use
`/ocr-service` as the Root Directory and set `/health` as the health-check
path. Generate a public domain, then set the Spring application's
`OCR_API_URL` variable to:

```
https://your-ocr-service.up.railway.app/extract-istimara
```

## Important accuracy note

Tesseract is free but cannot guarantee correct Arabic document understanding.
For reliable registration, use a clear, straight, full-page image. The service
returns `success: false` rather than guessing when it cannot identify the
plate, brand, or model. Review the `raw_text` and `missing_fields` fields
during testing to see what needs improving.
