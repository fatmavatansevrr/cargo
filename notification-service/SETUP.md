# Notification Service Kurulum Talimatları

## Gmail SMTP Ayarları

Gmail SMTP hatası "535 5.7.8 BadCredentials" alıyorsanız:

### 1. Gmail App Password Oluşturun
1. Google hesabınızda **2-Factor Authentication** aktif olmalı
2. https://myaccount.google.com/apppasswords adresine gidin
3. Yeni app password oluşturun (16 karakterlik)
4. Bu password'ü `MAIL_PASSWORD` olarak kullanın

### 2. Environment Variables Ayarlayın
```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-16-character-app-password
```

### 3. Alternatif: application.yml'de ayarlayın
```yaml
spring:
  mail:
    username: your-email@gmail.com
    password: your-16-character-app-password
```

## Kafka Ayarları

Kafka bağlantı hatası alıyorsanız:

### 1. Docker ile Kafka başlatın
```bash
cd cargo-tracking
docker-compose up -d kafka zookeeper
```

### 2. Kafka olmadan çalıştırın
Kafka olmadan test için health check'i devre dışı bıraktık:
```yaml
management:
  health:
    kafka:
      enabled: false
```

## Test Komutları

### Email test
```bash
curl -X POST "http://localhost:8084/api/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "type": "EMAIL",
    "subject": "Test Email",
    "content": "Bu bir test emailidir"
  }'
```

### Health check
```bash
curl http://localhost:8084/actuator/health
```

## Sorun Giderme

### Gmail "Less secure apps" Hatası
- Gmail artık "less secure apps" ayarını desteklemiyor
- Mutlaka **App Password** kullanın

### Kafka Bağlantı Hatası
- Docker'da Kafka çalışıyor mu kontrol edin: `docker ps`
- Kafka port'u açık mı: `telnet localhost 9092`

### Mock Mode
Email ve Kafka ayarları olmadan da servis çalışır:
- Email'ler log'a yazdırılır
- Kafka events mock olarak işlenir 