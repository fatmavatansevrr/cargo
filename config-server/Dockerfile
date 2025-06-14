# Spring Boot Config Server için Dockerfile
FROM eclipse-temurin:17-jdk-alpine

# Uygulama dizini oluştur
WORKDIR /app

# Maven wrapper ve pom.xml dosyalarını kopyala
COPY mvnw ./
COPY .mvn .mvn/
COPY pom.xml ./

# Bağımlılıkları indir (Docker layer caching için)
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Kaynak kodunu kopyala
COPY src ./src

# Uygulamayı derle
RUN ./mvnw clean package -DskipTests

# Port expose et
EXPOSE 8888

# Uygulamayı çalıştır
CMD ["java", "-jar", "target/config-server-0.0.1-SNAPSHOT.jar"] 