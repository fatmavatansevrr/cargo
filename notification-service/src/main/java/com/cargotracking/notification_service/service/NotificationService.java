package com.cargotracking.notification_service.service;

import com.cargotracking.notification_service.client.UserServiceClient;
import com.cargotracking.notification_service.client.ShipmentServiceClient;
import com.cargotracking.notification_service.event.ShipmentEvent;
import com.cargotracking.notification_service.event.TrackingEvent;
import com.cargotracking.notification_service.model.Notification;
import com.cargotracking.notification_service.model.NotificationPreference;
import com.cargotracking.notification_service.repository.NotificationPreferenceRepository;
import com.cargotracking.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final UserServiceClient userServiceClient;
    private final ShipmentServiceClient shipmentServiceClient;
    private final Optional<JavaMailSender> mailSender;

    @Value("${spring.mail.username:noreply@cargotracking.com}")
    private String fromEmail;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // ✅ SHIPMENT EVENT PROCESSING - Ana metod
    public void processShipmentEvent(ShipmentEvent event) {
        log.info("📦 Shipment event işleniyor: {} - {}", event.getEventType(), event.getTrackingNumber());

        try {
            // ✅ Event data'yı extract et
            if (event.getEventData() != null) {
                event.extractDataFromEventData();
                log.debug("📋 Event data extracted for: {}", event.getTrackingNumber());
            }

            String recipientEmail = extractRecipientEmail(event);
            if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
                log.warn("⚠️ Recipient email bulunamadı: {}", event.getTrackingNumber());
                return;
            }

            // Event tipine göre email içeriği oluştur ve gönder
            switch (event.getEventType().toLowerCase()) {
                case "shipment.created":
                    sendShipmentCreatedEmail(event, recipientEmail);
                    break;
                case "shipment.updated":
                    sendShipmentUpdatedEmail(event, recipientEmail);
                    break;
                case "shipment.cancelled":
                case "shipment.canceled":
                    sendShipmentCancelledEmail(event, recipientEmail);
                    break;
                case "shipment.finished":
                    sendShipmentFinishedEmail(event, recipientEmail);
                    break;
                default:
                    log.warn("⚠️ Bilinmeyen shipment event tipi: {}", event.getEventType());
                    return;
            }

            log.info("✅ Shipment notification gönderildi: {} -> {}", event.getTrackingNumber(), recipientEmail);

        } catch (Exception e) {
            log.error("❌ Shipment event işlenirken hata: {} - {}", event.getTrackingNumber(), e.getMessage(), e);
        }
    }


    // ✅ EMAIL SENDING METHODS

    private void sendShipmentCreatedEmail(ShipmentEvent event, String recipientEmail) {
        String subject = "Gönderiniz Oluşturuldu - " + event.getTrackingNumber();
        String content = createShipmentCreatedEmailContent(event);
        sendEmailNotification(recipientEmail, subject, content);
    }

    private void sendShipmentUpdatedEmail(ShipmentEvent event, String recipientEmail) {
        String subject = "Gönderi Bilgileri Güncellendi - " + event.getTrackingNumber();
        String content = createShipmentUpdatedEmailContent(event);
        sendEmailNotification(recipientEmail, subject, content);
    }

    private void sendShipmentCancelledEmail(ShipmentEvent event, String recipientEmail) {
        String subject = "Gönderiniz İptal Edildi - " + event.getTrackingNumber();
        String content = createShipmentCancelledEmailContent(event);
        sendEmailNotification(recipientEmail, subject, content);
    }

    private void sendShipmentFinishedEmail(ShipmentEvent event, String recipientEmail) {
        String subject = "Gönderiniz Teslim Edildi - " + event.getTrackingNumber();
        String content = createShipmentFinishedEmailContent(event);
        sendEmailNotification(recipientEmail, subject, content);
    }

    private void sendTrackingStatusEmail(TrackingEvent event, String recipientEmail) {
        String subject = "Gönderinizin Durumu Güncellendi: "
                + getStatusDisplayName(event.getCurrentStatus())
                + " - " + event.getTrackingNumber();
        String content = createTrackingStatusEmailContent(event);

        sendEmailNotification(recipientEmail, subject, content);
    }

    // ✅ EMAIL CONTENT CREATION
    private String createShipmentCreatedEmailContent(ShipmentEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("Sayın ").append(event.getRecipientFullName() != null ? event.getRecipientFullName() : "Müşteri").append(",\n\n");
        content.append("Size gönderilmek üzere yeni bir kargo oluşturulmuştur.\n\n");
        content.append("📦 GÖNDERI BİLGİLERİ:\n");
        content.append("Takip Numarası: ").append(event.getTrackingNumber()).append("\n");
        content.append("Gönderici: ").append(event.getSenderFullName() != null ? event.getSenderFullName() : "Bilinmiyor").append("\n");
        content.append("Alıcı: ").append(event.getRecipientFullName() != null ? event.getRecipientFullName() : "Bilinmiyor").append("\n");
        content.append("Teslimat Adresi: ").append(event.getRecipientAddress() != null ? event.getRecipientAddress() : "Bilinmiyor").append("\n");

        if (event.getPackageDescription() != null) {
            content.append("Paket İçeriği: ").append(event.getPackageDescription()).append("\n");
        }
        if (event.getServiceType() != null) {
            content.append("Servis Tipi: ").append(getServiceTypeDisplayName(event.getServiceType())).append("\n");
        }
        if (event.getEstimatedDeliveryDate() != null) {
            content.append("Tahmini Teslimat: ").append(event.getEstimatedDeliveryDate().format(FORMATTER)).append("\n");
        }

        content.append("\n🔍 Gönderinizi takip etmek için takip numaranızı kullanabilirsiniz.\n");
        content.append("Bu bir otomatik mesajdır, lütfen yanıtlamayınız.\n\n");
        content.append("İyi günler dileriz.\n");
        content.append("Kargo Takip Sistemi");

        return content.toString();
    }

    private String createShipmentUpdatedEmailContent(ShipmentEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("Sayın ").append(event.getRecipientFullName() != null ? event.getRecipientFullName() : "Müşteri").append(",\n\n");
        content.append("Gönderinizin bilgileri güncellenmiştir.\n\n");
        content.append("📦 GÜNCELLENMIŞ BİLGİLER:\n");
        content.append("Takip Numarası: ").append(event.getTrackingNumber()).append("\n");

        if (event.getStatus() != null) {
            content.append("Durum: ").append(getStatusDisplayName(event.getStatus())).append("\n");
        }
        if (event.getEstimatedDeliveryDate() != null) {
            content.append("Tahmini Teslimat: ").append(event.getEstimatedDeliveryDate().format(FORMATTER)).append("\n");
        }
        if (event.getSpecialInstructions() != null) {
            content.append("Özel Talimatlar: ").append(event.getSpecialInstructions()).append("\n");
        }

        content.append("\n🔍 Gönderinizi takip etmek için takip numaranızı kullanabilirsiniz.\n");
        content.append("Bu bir otomatik mesajdır, lütfen yanıtlamayınız.\n\n");
        content.append("İyi günler dileriz.\n");
        content.append("Kargo Takip Sistemi");

        return content.toString();
    }

    private String createShipmentCancelledEmailContent(ShipmentEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("Sayın ").append(event.getRecipientFullName() != null ? event.getRecipientFullName() : "Müşteri").append(",\n\n");
        content.append("Maalesef gönderiniz iptal edilmiştir.\n\n");
        content.append("📦 İPTAL EDİLEN GÖNDERI:\n");
        content.append("Takip Numarası: ").append(event.getTrackingNumber()).append("\n");
        content.append("İptal Tarihi: ").append(LocalDateTime.now().format(FORMATTER)).append("\n");

        content.append("\nHerhangi bir sorunuz varsa lütfen müşteri hizmetlerimizle iletişime geçiniz.\n");
        content.append("Bu bir otomatik mesajdır, lütfen yanıtlamayınız.\n\n");
        content.append("İyi günler dileriz.\n");
        content.append("Kargo Takip Sistemi");

        return content.toString();
    }

    private String createShipmentFinishedEmailContent(ShipmentEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("Sayın ").append(event.getRecipientFullName() != null ? event.getRecipientFullName() : "Müşteri").append(",\n\n");
        content.append("🎉 Gönderiniz başarıyla teslim edilmiştir!\n\n");
        content.append("📦 TESLİM EDİLEN GÖNDERI:\n");
        content.append("Takip Numarası: ").append(event.getTrackingNumber()).append("\n");
        content.append("Teslimat Tarihi: ").append(LocalDateTime.now().format(FORMATTER)).append("\n");

        content.append("\nGönderinizi seçtiğiniz için teşekkür ederiz.\n");
        content.append("Bu bir otomatik mesajdır, lütfen yanıtlamayınız.\n\n");
        content.append("İyi günler dileriz.\n");
        content.append("Kargo Takip Sistemi");

        return content.toString();
    }

    private String createTrackingStatusEmailContent(TrackingEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("Sayın Müşteri,\n\n");
        content.append("Gönderinizin durumu güncellenmiştir.\n\n");
        content.append("📦 DURUM BİLGİSİ:\n");
        content.append("Takip Numarası: ").append(event.getTrackingNumber()).append("\n");
        content.append("Güncel Durum: ").append(getStatusDisplayName(event.getCurrentStatus())).append("\n");

        if (event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
            content.append("Konum: ").append(event.getLocation()).append("\n");
        }

        content.append("Güncelleme Tarihi: ").append(event.getTimestamp() != null
                ? event.getTimestamp().format(FORMATTER) : LocalDateTime.now().format(FORMATTER)).append("\n");

        if (event.getUpdatedBy() != null && !"system".equals(event.getUpdatedBy())) {
            content.append("Güncelleyen: ").append(event.getUpdatedBy()).append("\n");
        }

        content.append("\n📋 DURUM AÇIKLAMASI:\n");
        content.append(getStatusDescription(event.getCurrentStatus())).append("\n");

        content.append("\n🔍 Gönderinizi takip etmek için takip numaranızı kullanabilirsiniz.\n");
        content.append("Bu bir otomatik mesajdır, lütfen yanıtlamayınız.\n\n");
        content.append("İyi günler dileriz.\n");
        content.append("Kargo Takip Sistemi");

        return content.toString();
    }

    // ✅ HELPER METHODS

    private String extractRecipientEmail(ShipmentEvent event) {
        // Önce event'te direkt recipient email var mı kontrol et
        if (event.getRecipientEmail() != null && !event.getRecipientEmail().trim().isEmpty()) {
            return event.getRecipientEmail().trim();
        }

        // Customer email kontrol et
        if (event.getCustomerEmail() != null && !event.getCustomerEmail().trim().isEmpty()) {
            return event.getCustomerEmail().trim();
        }

        log.warn("⚠️ Recipient email bulunamadı: {}", event.getTrackingNumber());
        return null;
    }

    private String getRecipientEmailFromTracking(String trackingNumber) {
        try {
            log.debug("🔍 Tracking number için recipient email aranıyor: {}", trackingNumber);
            return shipmentServiceClient.getRecipientEmailByTrackingNumber(trackingNumber);
        } catch (Exception e) {
            log.error("❌ Recipient email alınırken hata: {} - {}", trackingNumber, e.getMessage());
            return null;
        }
    }

    private void sendEmailNotification(String to, String subject, String message) {
        try {
            if (!emailEnabled) {
                log.info("📧 Email kapalı - Mock gönderim: {} -> {}", subject, to);
                return;
            }

            emailService.sendEmail(to, subject, message);
            saveNotificationRecord(to, subject, message, "EMAIL");
            log.info("✅ Email notification sent to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email notification to: {}", to, e);
        }
    }

    private String getServiceTypeDisplayName(String serviceType) {
        if (serviceType == null) return "Standart";
        return switch (serviceType.toUpperCase()) {
            case "EXPRESS" -> "Ekspres";
            case "STANDARD" -> "Standart";
            case "ECONOMY" -> "Ekonomik";
            default -> serviceType;
        };
    }

    private String getStatusDisplayName(String status) {
        if (status == null) return "Bilinmiyor";
        return switch (status.toUpperCase()) {
            case "ACTIVE" -> "Aktif";
            case "FINISHED" -> "Teslim Edildi";
            case "CANCELLED", "CANCELED" -> "İptal Edildi";
            case "CREATED" -> "Oluşturuldu";
            case "IN_TRANSIT" -> "Yolda";
            case "DELIVERED" -> "Teslim Edildi";
            case "OUT_FOR_DELIVERY" -> "Teslimat İçin Yola Çıktı";
            case "EXCEPTION" -> "Teslimat Sorunu";
            default -> status;
        };
    }

    private String getStatusDescription(String status) {
        if (status == null) return "Durum bilgisi mevcut değil.";
        return switch (status.toUpperCase()) {
            case "CREATED" -> "Gönderiniz kargo sistemine kaydedildi ve işleme alındı.";
            case "PICKED_UP" -> "Gönderiniz tarafımızca teslim alındı ve işleme başlandı.";
            case "IN_TRANSIT" -> "Gönderiniz teslimat adresine doğru yolda.";
            case "OUT_FOR_DELIVERY" -> "Gönderiniz teslimat için kurye araçına yüklendi ve size doğru yola çıktı.";
            case "DELIVERED" -> "Gönderiniz başarıyla teslim edildi.";
            case "CANCELLED" -> "Gönderiniz iptal edildi.";
            case "EXCEPTION" -> "Gönderinizde bir sorun oluştu. Lütfen müşteri hizmetleri ile iletişime geçiniz.";
            default -> "Gönderinizin durumu güncellendi: " + status;
        };
    }

    // ✅ EXISTING METHODS (User preferences, manual notifications, etc.)

    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public NotificationPreference updateEmailPreference(Long userId, boolean emailEnabled) {
        if (!userServiceClient.userExists(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        NotificationPreference preference = getUserPreferences(userId);
        preference.setEmailEnabled(emailEnabled);
        preference.setUpdatedAt(LocalDateTime.now());

        NotificationPreference saved = preferenceRepository.save(preference);
        log.info("📧 Updated email preference for user {}: {}", userId, emailEnabled);
        return saved;
    }

    public NotificationPreference getUserPreferences(Long userId) {
        if (!userServiceClient.userExists(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        return preferenceRepository.findByUserId(userId)
                .orElse(createDefaultPreferences(userId));
    }

    private NotificationPreference createDefaultPreferences(Long userId) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId(userId);
        preference.setEmailEnabled(true);
        preference.setSmsEnabled(false);
        preference.setPushEnabled(false);
        preference.setCreatedAt(LocalDateTime.now());
        preference.setUpdatedAt(LocalDateTime.now());
        return preference;
    }

    public Notification sendManualNotification(String userId, String type, String subject, String content) {
        log.info("📤 Sending manual notification: userId={}, type={}, subject={}", userId, type, subject);

        try {
            Long userIdLong = Long.valueOf(userId);

            Notification.NotificationChannel channel;
            try {
                channel = Notification.NotificationChannel.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid notification type: " + type +
                        ". Valid types: EMAIL, SMS, PUSH_NOTIFICATION, IN_APP");
            }

            String userEmail = userServiceClient.getUserEmail(userIdLong);
            String recipient = userEmail != null ? userEmail : userId + "@test.com";

            Notification notification = new Notification();
            notification.setUserId(userIdLong);
            notification.setRecipient(recipient);
            notification.setTitle(subject);
            notification.setMessage(content);
            notification.setChannel(channel);
            notification.setType(Notification.NotificationType.SYSTEM_ALERT);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setCreatedAt(LocalDateTime.now());

            Notification saved = notificationRepository.save(notification);

            switch (channel) {
                case EMAIL:
                    emailService.sendEmail(notification.getRecipient(), subject, content);
                    break;
                case SMS:
                    smsService.sendSms(notification.getRecipient(), content);
                    break;
                case PUSH_NOTIFICATION:
                    log.info("📱 Push notification: {}", subject);
                    break;
                case IN_APP:
                    log.info("📱 In-app notification: {}", subject);
                    break;
            }

            saved.markAsSent();
            saved = notificationRepository.save(saved);

            log.info("✅ Manual notification sent successfully: id={}", saved.getId());
            return saved;

        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Failed to send manual notification", e);
            throw new RuntimeException("Manual notification gönderilirken hata oluştu", e);
        }
    }

    private void saveNotificationRecord(String recipient, String subject, String message, String channel) {
        try {
            Notification notification = new Notification();
            notification.setRecipient(recipient);
            notification.setTitle(subject);
            notification.setMessage(message);
            notification.setChannel(Notification.NotificationChannel.valueOf(channel));
            notification.setType(Notification.NotificationType.STATUS_CHANGED);
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setSentAt(LocalDateTime.now());

            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("❌ Failed to save notification record", e);
        }
    }

    // Status changed event işleme
    public void processStatusChangedEvent(TrackingEvent event) {
        String recipientEmail = event.getReceiverEmail();
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            log.warn("⚠️ Tracking için recipient email bulunamadı: {}", event.getTrackingNumber());
            return;
        }
        sendTrackingStatusEmail(event, recipientEmail);
    }

    // Delivery completed event işleme
    public void processDeliveryCompletedEvent(TrackingEvent event) {
        String recipientEmail = getRecipientEmailFromTracking(event.getTrackingNumber());

        String subject = "🎉 Gönderiniz Başarıyla Teslim Edildi! - " + event.getTrackingNumber();
        String message = String.format("""
        Sayın %s,

        Gönderiniz başarıyla teslim edilmiştir.

        📦 TESLİMAT DETAYLARI:
        - Takip Numarası: %s
        - Teslimat Zamanı: %s
        - Teslimat Konumu: %s

        Bizi tercih ettiğiniz için teşekkür ederiz.

        İyi günler dileriz,
        Kargo Takip Sistemi
        """,
                recipientEmail,
                event.getTrackingNumber(),
                event.getTimestamp().format(FORMATTER),
                event.getLocation() != null ? event.getLocation() : "Bilinmiyor");

        sendEmailNotification(recipientEmail, subject, message);
    }

    // Delivery failed event işleme
    public void processDeliveryFailedEvent(TrackingEvent event) {
        String recipientEmail = getRecipientEmailFromTracking(event.getTrackingNumber());

        String subject = "⚠️ Gönderiniz Teslim Edilemedi - " + event.getTrackingNumber();
        String message = String.format("""
        Sayın %s,

        Gönderiniz teslim edilemedi.

        📦 TESLİMAT BİLGİLERİ:
        - Takip Numarası: %s
        - Deneme Zamanı: %s
        - Son Deneme Konumu: %s

        Lütfen teslimat adresinizi ve iletişim bilgilerinizi kontrol ediniz veya müşteri hizmetleri ile iletişime geçiniz.

        İyi günler dileriz,
        Kargo Takip Sistemi
        """,
                recipientEmail,
                event.getTrackingNumber(),
                event.getTimestamp().format(FORMATTER),
                event.getLocation() != null ? event.getLocation() : "Bilinmiyor");

        sendEmailNotification(recipientEmail, subject, message);
    }


}