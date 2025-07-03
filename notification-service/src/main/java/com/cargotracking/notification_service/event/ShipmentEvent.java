package com.cargotracking.notification_service.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ShipmentEvent - Kafka'dan gelen shipment olayları
 * Sadece gönderilen ve kullanılan alanlar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class ShipmentEvent {

    // Event metadata - JSON'dan gelen
    @JsonProperty("eventType")
    private String eventType; // shipment.created, shipment.updated, shipment.canceled

    @JsonProperty("shipmentId")
    private Long shipmentId;

    @JsonProperty("trackingNumber")
    private String trackingNumber;

    @JsonProperty("senderUserId")
    private Long senderUserId;

    @JsonProperty("status")
    private String status; // ShipmentStatus enum değeri

    @JsonProperty("previousStatus")
    private String previousStatus;

    @JsonProperty("eventTimestamp")
    private LocalDateTime eventTimestamp;

    @JsonProperty("eventSource")
    private String eventSource = "shipment-service";

    @JsonProperty("eventData")
    private Object eventData; // Shipment detayları

    // ✅ Kullanılan alanlar - eventData'dan parse edilecek
    private String recipientFullName;
    private String recipientAddress;
    private String recipientEmail;
    private String recipientPhone;

    private String senderFullName;
    private String senderAddress;

    // Package Information
    private String packageDescription;
    private Double packageWeight;
    private BigDecimal packageValue;
    private String contentType;

    // Shipment Details
    private String serviceType; // STANDARD, EXPRESS, etc.
    private LocalDateTime estimatedDeliveryDate;
    private BigDecimal shippingCost;
    private String specialInstructions;

    // ✅ Backward compatibility için
    private String customerName;
    private String customerEmail;

    /**
     * eventData'dan notification için gerekli bilgileri çıkar
     */
    public void extractDataFromEventData() {
        if (eventData == null) {
            log.warn("Event data is null for shipment event: {}", trackingNumber);
            return;
        }

        try {
            log.debug("🔍 Event data içeriği: {}", eventData);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data;

            // eventData Map veya JSON string olabilir
            if (eventData instanceof String) {
                data = mapper.readValue((String) eventData, Map.class);
            } else if (eventData instanceof Map) {
                data = (Map<String, Object>) eventData;
            } else {
                // Object'i Map'e dönüştür
                String jsonString = mapper.writeValueAsString(eventData);
                data = mapper.readValue(jsonString, Map.class);
            }

            log.debug("🔍 Parse edilen data: {}", data);

            // ✅ Recipient iletişim bilgileri direkt event data'dan al
            this.recipientEmail = (String) data.get("recipientEmail");
            this.recipientPhone = (String) data.get("recipientPhone");

            log.debug("🔍 Extract edilen recipient email: {}", this.recipientEmail);

            // Sender Address
            Map<String, Object> senderAddr = (Map<String, Object>) data.get("senderAddress");
            if (senderAddr != null) {
                this.senderFullName = (String) senderAddr.get("fullName");
                this.senderAddress = buildAddressString(senderAddr);

                // Customer info backward compatibility için
                if (this.customerName == null) {
                    this.customerName = this.senderFullName;
                }
                if (this.customerEmail == null) {
                    this.customerEmail = (String) senderAddr.get("email");
                }
            }

            // Recipient Address
            Map<String, Object> recipientAddr = (Map<String, Object>) data.get("recipientAddress");
            if (recipientAddr != null) {
                this.recipientFullName = (String) recipientAddr.get("fullName");
                this.recipientAddress = buildAddressString(recipientAddr);
            }

            // Package Information
            Map<String, Object> packageInfo = (Map<String, Object>) data.get("packageInfo");
            if (packageInfo != null) {
                this.packageDescription = (String) packageInfo.get("contentDescription");
                this.contentType = (String) packageInfo.get("contentType");

                Object weight = packageInfo.get("weight");
                if (weight != null) {
                    this.packageWeight = weight instanceof Number ? ((Number) weight).doubleValue() : Double.valueOf(weight.toString());
                }

                Object declaredValue = packageInfo.get("declaredValue");
                if (declaredValue != null) {
                    this.packageValue = declaredValue instanceof BigDecimal ? (BigDecimal) declaredValue :
                            new BigDecimal(declaredValue.toString());
                }
            }

            // Shipment Details
            this.serviceType = (String) data.get("serviceType");
            this.specialInstructions = (String) data.get("specialInstructions");

            Object shippingCostObj = data.get("shippingCost");
            if (shippingCostObj != null) {
                this.shippingCost = shippingCostObj instanceof BigDecimal ? (BigDecimal) shippingCostObj :
                        new BigDecimal(shippingCostObj.toString());
            }

            // Estimated delivery date
            Object estimatedDelivery = data.get("estimatedDeliveryDate");
            if (estimatedDelivery != null && estimatedDelivery instanceof String) {
                this.estimatedDeliveryDate = LocalDateTime.parse((String) estimatedDelivery);
            }

            log.debug("Successfully extracted data from event data for tracking: {}", trackingNumber);

        } catch (Exception e) {
            log.error("❌ Event data extract edilirken hata: {}", e.getMessage(), e);
        }
    }

    /**
     * Address map'inden tam adres string'i oluştur
     */
    private String buildAddressString(Map<String, Object> addressMap) {
        StringBuilder address = new StringBuilder();

        String line1 = (String) addressMap.get("addressLine1");
        String line2 = (String) addressMap.get("addressLine2");
        String city = (String) addressMap.get("city");
        String state = (String) addressMap.get("state");
        String postalCode = (String) addressMap.get("postalCode");

        if (line1 != null) address.append(line1);
        if (line2 != null && !line2.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(line2);
        }
        if (city != null) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }
        if (state != null) {
            if (address.length() > 0) address.append(", ");
            address.append(state);
        }
        if (postalCode != null) {
            if (address.length() > 0) address.append(" ");
            address.append(postalCode);
        }

        return address.toString();
    }
}