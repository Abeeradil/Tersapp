# مرحلة البناء
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# مرحلة التشغيل
FROM eclipse-temurin:17-jdk
WORKDIR /app

# نسخ JAR من مرحلة البناء
COPY --from=build /app/target/Tears-0.0.1-SNAPSHOT.jar app.jar

# نسخ ملفات tessdata
COPY src/main/resources/tessdata /app/tessdata

# تحديد entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]