# =========================
# BUILD STAGE
# =========================
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests


# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:17-jdk

WORKDIR /app

# تثبيت Tesseract + dependencies (🔥 مهم جدًا)
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-ara \
    tesseract-ocr-eng \
    libtesseract-dev \
    libleptonica-dev \
    && rm -rf /var/lib/apt/lists/*

# نسخ التطبيق
COPY --from=build /app/target/Tears-0.0.1-SNAPSHOT.jar app.jar

# نسخ tessdata (اختياري لكن مفيد)
COPY src/main/resources/tessdata /app/tessdata

# تشغيل
ENTRYPOINT ["java", "-jar", "app.jar"]