# Spring Boot Eureka Server için Dockerfile
FROM openjdk:17-jdk-slim

# wget ekleyin (Debian/Ubuntu için apt-get kullanın)
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

# Uygulama dizini oluştur
WORKDIR /app

# Maven wrapper ve pom.xml dosyalarını kopyala
COPY mvnw ./
COPY .mvn .mvn/
COPY pom.xml ./

# Maven wrapper'a execute permission ver
RUN chmod +x ./mvnw

# Bağımlılıkları indir (Docker layer caching için)
RUN ./mvnw dependency:go-offline -B

# Kaynak kodunu kopyala
COPY src ./src

# Uygulamayı derle
RUN ./mvnw clean package -DskipTests

# Port expose et
EXPOSE 8761

# JVM optimizasyonları ile uygulamayı çalıştır
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-jar", "target/eureka-server-0.0.1-SNAPSHOT.jar"]