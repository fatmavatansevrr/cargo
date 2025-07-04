package com.cargotracking.notification_service.factory;

import com.cargotracking.notification_service.model.Notification;
import com.cargotracking.notification_service.model.Notification.NotificationChannel;
import com.cargotracking.notification_service.model.Notification.NotificationStatus;
import com.cargotracking.notification_service.model.Notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationFactory {

    /**
     * Yeni gönderi oluşturulduğunda şirket için notification oluşturur
     */
    public Notification createShipmentCreatedCompanyNotification(
            Long companyUserId, String shipmentId, String trackingNumber
    ) {
        String title = "Yeni Gönderi Oluşturuldu";
        String message = String.format("Yeni bir gönderi oluşturuldu. Takip No: %s", trackingNumber);

        return buildNotification(companyUserId, shipmentId, trackingNumber,
                NotificationType.SHIPMENT_CREATED,
                NotificationChannel.IN_APP, title, message);
    }

    /**
     * Yeni gönderi oluşturulduğunda gönderici için notification oluşturur
     */
    public Notification createShipmentCreatedSenderNotification(
            Long senderUserId, String shipmentId, String trackingNumber
    ) {
        String title = "Gönderiniz Oluşturuldu";
        String message = String.format("Gönderiniz başarıyla oluşturuldu. Takip No: %s", trackingNumber);

        return buildNotification(senderUserId, shipmentId, trackingNumber,
                NotificationType.SHIPMENT_CREATED,
                NotificationChannel.IN_APP, title, message);
    }

    /**
     * Gönderi carrier'a atandığında notification oluşturur
     */
    public Notification createShipmentAssignedCarrierNotification(
            Long carrierUserId, String shipmentId, String trackingNumber
    ) {
        String title = "Yeni Gönderi Atandı";
        String message = String.format("%s, size yeni bir gönderi atandı. Takip No: %s",
                 trackingNumber);

        return buildNotification(carrierUserId, shipmentId, trackingNumber,
                NotificationType.SHIPMENT_UPDATED,
                NotificationChannel.IN_APP, title, message);
    }

    /**
     * Gönderi durumu değiştiğinde şirket için notification oluşturur
     */
    public Notification createShipmentStatusChangedCompanyNotification(
            Long companyUserId, String shipmentId, String trackingNumber, String newStatus
    ) {
        String title = "Gönderi Durumu Güncellendi";
        String message = String.format("Takip No: %s durum değişikliği: %s → %s",
                trackingNumber, newStatus);

        return buildNotification(companyUserId, shipmentId, trackingNumber,
                NotificationType.STATUS_CHANGED,
                NotificationChannel.IN_APP, title, message);
    }

    /**
     * Gönderi durumu değiştiğinde gönderici için notification oluşturur
     */
    public Notification createShipmentStatusChangedSenderNotification(
            Long senderUserId, String shipmentId, String trackingNumber, String newStatus
    ) {
        String title = "Gönderinizin Durumu Değişti";
        String message = String.format("Gönderiniz (%s) durumu: %s → %s",
                trackingNumber, newStatus);

        return buildNotification(senderUserId, shipmentId, trackingNumber,
                NotificationType.STATUS_CHANGED,
                NotificationChannel.IN_APP, title, message);
    }

    /**
     * Gönderi durumu değiştiğinde carrier için notification oluşturur (opsiyonel)
     */
    public Notification createShipmentStatusChangedCarrierNotification(
            Long carrierUserId, String shipmentId, String trackingNumber, String newStatus
    ) {
        String title = "Atanan Gönderinin Durumu Değişti";
        String message = String.format("Atandığınız gönderi (%s) durumu: %s → %s",
                trackingNumber, newStatus);

        return buildNotification(carrierUserId, shipmentId, trackingNumber,
                NotificationType.STATUS_CHANGED,
                NotificationChannel.IN_APP, title, message);
    }

    /**
     * Gönderi iptal edildiğinde gönderici için notification oluşturur
     */
    public Notification createShipmentCancelledSenderNotification(
            Long senderUserId, String shipmentId, String trackingNumber
    ) {
        String title = "Gönderiniz İptal Edildi";
        String message = String.format("Gönderiniz (%s) iptal edildi.", trackingNumber);

        return buildNotification(senderUserId, shipmentId, trackingNumber,
                NotificationType.SHIPMENT_CANCELLED,
                NotificationChannel.IN_APP, title, message);
    }

    /**
     * Teslimat tamamlandığında gönderici için notification oluşturur
     */
    public Notification createDeliveryCompletedNotification(
            Long senderUserId, String shipmentId, String trackingNumber
    ) {
        String title = "Gönderiniz Teslim Edildi";
        String message = String.format("Gönderiniz (%s) başarıyla teslim edildi.", trackingNumber);

        return buildNotification(senderUserId, shipmentId, trackingNumber,
                NotificationType.DELIVERY_COMPLETED,
                NotificationChannel.IN_APP, title, message);
    }

    /**
     * Genel amaçlı notification builder metodu
     */
    private Notification buildNotification(
            Long userId,
            String shipmentId,
            String trackingNumber,
            NotificationType type,
            NotificationChannel channel,
            String title,
            String message
    ) {
        Notification notification = new Notification();

        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(userId);
        notification.setShipmentId(shipmentId);
        notification.setTrackingNumber(trackingNumber);
        notification.setType(type);
        notification.setChannel(channel);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRecipient(String.valueOf(userId)); // Burayı email/sms user'a göre değiştirebilirsin.
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        return notification;
    }
}
