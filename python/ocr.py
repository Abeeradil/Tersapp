import easyocr
import json
import sys
import re

reader = easyocr.Reader(['ar','en'])

image_path = sys.argv[1]

result = reader.readtext(image_path, detail=0)

plate = None

for item in result:

    text = item.replace(" ", "")

    # لوحة عربية مثل: رعك٣٢٦٦
    if re.search(r'[ء-ي]{1,4}\d{3,4}', text):

        plate = item
        break

    # لوحة انجليزية مثل: 3266KER
    if re.search(r'\d{3,4}[A-Z]{2,4}', text):

        plate = item
        break

print(json.dumps({
    "raw": result,
    "plate": plate
}, ensure_ascii=False))