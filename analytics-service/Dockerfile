# Spring Boot Analytics Service için Dockerfile
FROM openjdk:17-jdk-slim

# Uygulama dizini oluştur
WORKDIR /app

# Maven wrapper ve pom.xml dosyalarını kopyala
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Bağımlılıkları indir
RUN ./mvnw dependency:go-offline -B
RUN apt-get update && apt-get install -y curl

# Kaynak kodunu kopyala
COPY src src

# Uygulamayı derle
RUN ./mvnw clean package -DskipTests

# JAR dosyasını çalıştır
EXPOSE 8085
CMD ["java", "-jar", "target/analytics-service-0.0.1-SNAPSHOT.jar"] 