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
 * Shipment service ile tamamen uyumlu event modeli
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class ShipmentEvent {
    
    // Event metadata
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
    
    // Customer Information - eventData'dan parse edilecek
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Address Information
    private String senderFullName;
    private String senderAddress;
    private String senderCity;
    private String senderCountry;
    
    private String recipientFullName;
    private String recipientAddress;
    private String recipientCity;
    private String recipientCountry;
    private String recipientEmail;
    private String recipientPhone;
    
    // Package Information
    private String packageDescription;
    private Double packageWeight;
    private BigDecimal packageValue;
    private String contentType;
    
    // Shipment Details
    private String serviceType; // STANDARD, EXPRESS, etc.
    private String carrierName;
    private LocalDateTime estimatedDeliveryDate;
    private BigDecimal shippingCost;
    private String specialInstructions;
    
    /**
     * eventData'dan notification için gerekli bilgileri çıkar
     * Shipment service'den gelen JSON data'yı parse eder
     */
    public void extractDataFromEventData() {
        if (eventData == null) {
            log.warn("Event data is null for shipment event: {}", trackingNumber);
            return;
        }
        
        try {
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
            
            // Sender Address
            Map<String, Object> senderAddr = (Map<String, Object>) data.get("senderAddress");
            if (senderAddr != null) {
                this.senderFullName = (String) senderAddr.get("fullName");
                this.senderAddress = buildAddressString(senderAddr);
                this.senderCity = (String) senderAddr.get("city");
                this.senderCountry = (String) senderAddr.get("country");
                
                // Customer info sender'dan alınabilir
                if (this.customerName == null) {
                    this.customerName = this.senderFullName;
                }
                if (this.customerEmail == null) {
                    this.customerEmail = (String) senderAddr.get("email");
                }
                if (this.customerPhone == null) {
                    this.customerPhone = (String) senderAddr.get("phone");
                }
            }
            
            // Recipient Address
            Map<String, Object> recipientAddr = (Map<String, Object>) data.get("recipientAddress");
            if (recipientAddr != null) {
                this.recipientFullName = (String) recipientAddr.get("fullName");
                this.recipientAddress = buildAddressString(recipientAddr);
                this.recipientCity = (String) recipientAddr.get("city");
                this.recipientCountry = (String) recipientAddr.get("country");
                this.recipientEmail = (String) recipientAddr.get("email");
                this.recipientPhone = (String) recipientAddr.get("phone");
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
            this.carrierName = determineCarrierName((String) data.get("serviceType"));
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
            log.error("Error extracting data from event data for tracking: {}", trackingNumber, e);
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
    
    /**
     * Service type'a göre carrier name belirle
     */
    private String determineCarrierName(String serviceType) {
        if (serviceType == null) return "Cargo Tracking Express";
        
        return switch (serviceType.toUpperCase()) {
            case "EXPRESS" -> "Cargo Express";
            case "OVERNIGHT" -> "Overnight Cargo";
            case "GROUND" -> "Ground Cargo";
            case "INTERNATIONAL" -> "International Cargo";
            default -> "Cargo Tracking Express";
        };
    }
    
    /**
     * Event'in priority level'ini belirle
     */
    public int getPriorityLevel() {
        if (eventType == null) return 1;
        
        return switch (eventType.toLowerCase()) {
            case "shipment.canceled" -> 3; // High priority
            case "shipment.created" -> 2; // Medium priority
            case "shipment.updated" -> 1; // Low priority
            default -> 1;
        };
    }
    
    /**
     * Event'in customer'a gönderilmesi gerekip gerekmediğini kontrol et
     */
    public boolean shouldNotifyCustomer() {
        return eventType != null && (
            eventType.equals("shipment.created") ||
            eventType.equals("shipment.canceled") ||
            (eventType.equals("shipment.updated") && isSignificantUpdate())
        );
    }
    
    /**
     * Günceleme önemli mi kontrol et
     */
    private boolean isSignificantUpdate() {
        return status != null && previousStatus != null && !status.equals(previousStatus);
    }
} 