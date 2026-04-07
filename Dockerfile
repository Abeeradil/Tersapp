# اختيار صورة جافا
FROM openjdk:21-jdk-slim

# تعيين مجلد العمل
WORKDIR /app

# نسخ ملف JAR من المجلد target إلى الصورة
COPY target/Tears-0.0.1-SNAPSHOT.jar app.jar

# تشغيل التطبيق
ENTRYPOINT ["java", "-jar", "app.jar"]
