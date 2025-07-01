# Spring Boot Config Server için Dockerfile
FROM eclipse-temurin:17-jdk-alpine
# wget ekleyin (healthcheck için gerekli)
RUN apk add --no-cache wget

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

# Health check ekle (opsiyonel - docker-compose'da da var ama Docker image seviyesinde de olabilir)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8888/actuator/health || exit 1


# Uygulamayı çalıştır
CMD ["java", "-jar", "target/config-server-0.0.1-SNAPSHOT.jar"] 