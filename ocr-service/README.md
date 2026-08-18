# Istimara OCR service

A free, local PaddleOCR v3 service for Saudi vehicle-registration images.

## What it returns

`POST /extract-istimara` returns the Java-compatible `data` object plus a
`quality` object. When the image is blurry, too small, or does not produce the
plate/brand/model, `success` is `false`; the Java backend must not save a car.

The service uses document-orientation classification, document unwarping, and
Arabic OCR. It accepts JPEG, PNG, or WEBP images up to 10 MB.

## Railway

Deploy `ocr-service` as its own service with Root Directory `/ocr-service`.
Set the Railway health check to `/health`. No API key is required.

Then set the Java service variable:

```
OCR_API_URL=https://your-ocr-service.up.railway.app/extract-istimara
```

## Accuracy

Take a landscape photo with the full card visible, the camera parallel to the
card, no glare, and at least 1000×600 pixels. The first image can download
PaddleOCR models, so the initial request may take longer.
