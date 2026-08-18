# Istimara OCR service

A standalone FastAPI service for extracting fields from Saudi vehicle-registration images.

## Run locally

```bash
cd ocr-service
python -m venv .venv
source .venv/bin/activate # Windows: .venv\\Scripts\\activate
pip install -r requirements.txt
export OPENAI_API_KEY="..."
uvicorn main:app --reload --port 8000
```

Open `http://127.0.0.1:8000/docs` and use `POST /extract-istimara` with a `file` field.

## Deploy

Deploy the `ocr-service` directory as a separate Railway service. Set `OPENAI_API_KEY` and optionally `OPENAI_MODEL` in Railway variables. Copy the resulting public URL into the Spring application's `OCR_API_URL` variable, for example:

```
OCR_API_URL=https://your-ocr-service.up.railway.app/extract-istimara
```

Never expose `OPENAI_API_KEY` in the mobile/web client.
