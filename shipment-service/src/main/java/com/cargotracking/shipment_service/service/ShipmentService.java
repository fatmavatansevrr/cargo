package com.cargotracking.shipment_service.service;

import com.cargotracking.shipment_service.dto.*;
import com.cargotracking.shipment_service.event.ShipmentEvent;
import com.cargotracking.shipment_service.model.*;
import com.cargotracking.shipment_service.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

/**
 * Shipment Service - Ana gönderi yönetim servisi
 * Requirements: FR-SM-001, FR-SM-002, FR-SM-003, FR-SM-004, FR-SM-005, FR-SM-006, FR-SM-008
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShipmentService {
    
    private final ShipmentRepository shipmentRepository;
    
    @Autowired(required = false) // Kafka yoksa hata vermesin
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private final Random random = new Random();
    
    private static final String SHIPMENT_TOPIC = "shipment-events";
    
    /**
     * Yeni gönderi oluşturma
     * Requirements: FR-SM-001, FR-SM-002, FR-SM-008
     */
    public ShipmentResponse createShipment(CreateShipmentRequest request, Long senderUserId) {
        log.info("Yeni gönderi oluşturuluyor. Gönderici ID: {}", senderUserId);
        
        // Shipment entity oluştur
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setSenderUserId(senderUserId);
        shipment.setSenderAddress(convertToAddressEntity(request.getSenderAddress()));
        shipment.setRecipientAddress(convertToAddressEntity(request.getRecipientAddress()));
        shipment.setPackageInfo(convertToPackageEntity(request.getPackageInfo()));
        shipment.setServiceType(request.getServiceType());
        shipment.setStatus(Shipment.ShipmentStatus.ACTIVE);
        shipment.setSpecialInstructions(request.getSpecialInstructions());
        shipment.setNotes(request.getNotes());
        
        // FR-SM-009: Teslimat tercihleri
        if (request.getDeliveryPreferences() != null) {
            shipment.setDeliveryPreferences(convertToDeliveryPreferencesEntity(request.getDeliveryPreferences()));
        }
        
        shipment.setCreatedBy(senderUserId);
        shipment.setUpdatedBy(senderUserId);
        
        // Tahmini teslimat tarihi hesapla
        shipment.setEstimatedDeliveryDate(calculateEstimatedDeliveryDate(request.getServiceType()));
        
        // Kargo ücreti hesapla
        shipment.setShippingCost(calculateShippingCost(request.getPackageInfo(), request.getServiceType()));
        
        // Veritabanına kaydet
        Shipment savedShipment = shipmentRepository.save(shipment);
        
        // Kafka olayı yayınla
        publishShipmentEvent(ShipmentEvent.created(
            savedShipment.getId(),
            savedShipment.getTrackingNumber(),
            savedShipment.getSenderUserId(),
            convertToResponse(savedShipment)
        ));
        
        log.info("Gönderi oluşturuldu. Takip numarası: {}", savedShipment.getTrackingNumber());
        return convertToResponse(savedShipment);
    }
    
    /**
     * Takip numarasına göre gönderi bulma
     * Requirements: FR-SM-003
     */
    @Transactional(readOnly = true)
    public Optional<ShipmentResponse> findByTrackingNumber(String trackingNumber) {
        log.info("Gönderi aranıyor. Takip numarası: {}", trackingNumber);
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .map(this::convertToResponse);
    }
    
    /**
     * Gönderi ID'sine göre gönderi bulma
     * Requirements: FR-SM-003
     */
    @Transactional(readOnly = true)
    public Optional<ShipmentResponse> findById(Long id) {
        return shipmentRepository.findById(id)
                .map(this::convertToResponse);
    }
    
    /**
     * Kullanıcının gönderilerini listeleme
     * Requirements: FR-SM-003
     */
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> findUserShipments(Long userId, Pageable pageable) {
        return shipmentRepository.findBySenderUserId(userId, pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Gönderi durumu güncelleme
     * Requirements: FR-SM-006, FR-SM-008
     */
    public ShipmentResponse updateShipmentStatus(Long shipmentId, Shipment.ShipmentStatus newStatus, Long updatedBy) {
        log.info("Gönderi durumu güncelleniyor. ID: {}, Yeni durum: {}", shipmentId, newStatus);
        
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Gönderi bulunamadı: " + shipmentId));
        
        Shipment.ShipmentStatus previousStatus = shipment.getStatus();
        
        // Durum geçişi kontrolü
        if (!previousStatus.canTransitionTo(newStatus)) {
            throw new RuntimeException(String.format("Geçersiz durum geçişi: %s -> %s", previousStatus, newStatus));
        }
        
        shipment.setStatus(newStatus);
        shipment.setUpdatedBy(updatedBy);
        
        // Teslim edildi ise gerçek teslimat tarihini set et
        if (newStatus == Shipment.ShipmentStatus.FINISHED) {
            shipment.setActualDeliveryDate(LocalDateTime.now());
        }
        
        Shipment updatedShipment = shipmentRepository.save(shipment);
        
        // Kafka olayı yayınla
        publishShipmentEvent(ShipmentEvent.updated(
            updatedShipment.getId(),
            updatedShipment.getTrackingNumber(),
            updatedShipment.getSenderUserId(),
            newStatus,
            previousStatus,
            convertToResponse(updatedShipment)
        ));
        
        log.info("Gönderi durumu güncellendi. Takip numarası: {}, Durum: {} -> {}", 
                updatedShipment.getTrackingNumber(), previousStatus, newStatus);
        
        return convertToResponse(updatedShipment);
    }
    
    /**
     * Gönderi bilgilerini güncelleme
     * Requirements: FR-SM-004, FR-SM-009
     */
    public ShipmentResponse updateShipment(Long shipmentId, UpdateShipmentRequest request, Long updatedBy) {
        log.info("Gönderi güncelleniyor. ID: {}", shipmentId);
        
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Gönderi bulunamadı: " + shipmentId));
        
        // Sadece belirli durumlarda güncelleme yapılabilir
        if (!canBeUpdated(shipment.getStatus())) {
            throw new RuntimeException("Bu durumda gönderi güncellenemez: " + shipment.getStatus());
        }
        
        // Güncelleme yapılacak alanları kontrol et ve güncelle
        if (request.getSenderAddress() != null) {
            shipment.setSenderAddress(convertToAddressEntity(request.getSenderAddress()));
        }
        
        if (request.getRecipientAddress() != null) {
            shipment.setRecipientAddress(convertToAddressEntity(request.getRecipientAddress()));
        }
        
        if (request.getPackageInfo() != null) {
            shipment.setPackageInfo(convertToPackageEntity(request.getPackageInfo()));
            // Paket bilgisi değişirse kargo ücretini yeniden hesapla
            shipment.setShippingCost(calculateShippingCost(request.getPackageInfo(), shipment.getServiceType()));
        }
        
        if (request.getServiceType() != null) {
            shipment.setServiceType(request.getServiceType());
            // Hizmet tipi değişirse tahmini teslimat tarihini yeniden hesapla
            shipment.setEstimatedDeliveryDate(calculateEstimatedDeliveryDate(request.getServiceType()));
            // Kargo ücretini yeniden hesapla
            if (shipment.getPackageInfo() != null) {
                shipment.setShippingCost(calculateShippingCost(
                    convertToPackageDto(shipment.getPackageInfo()), 
                    request.getServiceType()
                ));
            }
        }
        
        if (request.getSpecialInstructions() != null) {
            shipment.setSpecialInstructions(request.getSpecialInstructions());
        }
        
        if (request.getNotes() != null) {
            shipment.setNotes(request.getNotes());
        }
        
        // FR-SM-009: Teslimat tercihleri
        if (request.getDeliveryPreferences() != null) {
            shipment.setDeliveryPreferences(convertToDeliveryPreferencesEntity(request.getDeliveryPreferences()));
        }
        
        shipment.setUpdatedBy(updatedBy);
        
        Shipment updatedShipment = shipmentRepository.save(shipment);
        
        log.info("Gönderi veritabanında güncellendi, şimdi Kafka olayı yayınlanacak...");
        
        // Kafka olayı yayınla
        publishShipmentEvent(ShipmentEvent.updated(
            updatedShipment.getId(),
            updatedShipment.getTrackingNumber(),
            updatedShipment.getSenderUserId(),
            updatedShipment.getStatus(),
            updatedShipment.getStatus(), // Durum değişmedi, sadece bilgiler güncellendi
            convertToResponse(updatedShipment)
        ));
        
        log.info("Gönderi güncellendi. Takip numarası: {}", updatedShipment.getTrackingNumber());
        return convertToResponse(updatedShipment);
    }
    
    /**
     * Gönderi iptal etme
     * Requirements: FR-SM-005, FR-SM-008
     */
    public ShipmentResponse cancelShipment(Long shipmentId, Long canceledBy) {
        log.info("Gönderi iptal ediliyor. ID: {}", shipmentId);
        
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Gönderi bulunamadı: " + shipmentId));
        
        // İptal edilebilir durumda mı kontrol et
        if (!canBeCanceled(shipment.getStatus())) {
            throw new RuntimeException("Bu durumda gönderi iptal edilemez: " + shipment.getStatus());
        }
        
        shipment.setStatus(Shipment.ShipmentStatus.CANCELLED);
        shipment.setUpdatedBy(canceledBy);
        
        Shipment canceledShipment = shipmentRepository.save(shipment);
        
        log.info("Gönderi veritabanında iptal edildi, şimdi Kafka olayı yayınlanacak...");
        
        // Kafka olayı yayınla
        publishShipmentEvent(ShipmentEvent.canceled(
            canceledShipment.getId(),
            canceledShipment.getTrackingNumber(),
            canceledShipment.getSenderUserId(),
            convertToResponse(canceledShipment)
        ));
        
        log.info("Gönderi iptal edildi. Takip numarası: {}", canceledShipment.getTrackingNumber());
        return convertToResponse(canceledShipment);
    }
    
    /**
     * Benzersiz takip numarası oluşturma
     * Requirements: FR-SM-002
     */
    private String generateTrackingNumber() {
        String trackingNumber;
        do {
            trackingNumber = "CT" + System.currentTimeMillis() + String.format("%04d", random.nextInt(10000));
        } while (shipmentRepository.existsByTrackingNumber(trackingNumber));
        
        return trackingNumber;
    }
    
    /**
     * Tahmini teslimat tarihi hesaplama
     */
    private LocalDateTime calculateEstimatedDeliveryDate(Shipment.ServiceType serviceType) {
        return LocalDateTime.now().plusDays(serviceType.getMaxDeliveryDays());
    }
    
    /**
     * Kargo ücreti hesaplama
     */
    private BigDecimal calculateShippingCost(PackageDto packageDto, Shipment.ServiceType serviceType) {
        // Basit hesaplama: ağırlık * hizmet tipi çarpanı
        BigDecimal baseCost = packageDto.getWeight().multiply(BigDecimal.valueOf(10)); // 10 TL per kg
        
        BigDecimal multiplier = switch (serviceType) {
            case ECONOMY -> BigDecimal.valueOf(0.8);
            case STANDARD -> BigDecimal.valueOf(1.0);
            case EXPRESS -> BigDecimal.valueOf(1.5);
            case OVERNIGHT -> BigDecimal.valueOf(2.0);
            case INTERNATIONAL -> BigDecimal.valueOf(3.0);
        };
        
        return baseCost.multiply(multiplier);
    }
    
    /**
     * İptal edilebilir durumda mı kontrol
     */
    private boolean canBeCanceled(Shipment.ShipmentStatus status) {
        return status == Shipment.ShipmentStatus.ACTIVE;
    }
    
    /**
     * Güncellenebilir durumda mı kontrol
     * Requirements: FR-SM-004
     */
    private boolean canBeUpdated(Shipment.ShipmentStatus status) {
        return status == Shipment.ShipmentStatus.ACTIVE;
    }
    
    /**
     * Kafka olayı yayınlama
     * Kafka yoksa sessizce geçer
     */
    private void publishShipmentEvent(ShipmentEvent event) {
        log.info("Kafka olayı yayınlanmaya çalışılıyor: {}, Tracking: {}", event.getEventType(), event.getTrackingNumber());
        
        if (kafkaTemplate != null) {
            try {
                log.info("Kafka template bulundu, olay gönderiliyor...");
                
                // Senkron gönderim için get() kullan
                kafkaTemplate.send(SHIPMENT_TOPIC, event.getTrackingNumber(), event).get();
                
                log.info("✅ Kafka olayı başarıyla yayınlandı: {} - Topic: {}", event.getEventType(), SHIPMENT_TOPIC);
            } catch (Exception e) {
                log.error("❌ Kafka olayı yayınlanırken hata oluştu: {}", e.getMessage(), e);
            }
        } else {
            log.warn("⚠️ Kafka template bulunamadı, olay yayınlanmadı: {}", event.getEventType());
        }
    }
    
    /**
     * DTO dönüşüm metodları
     */
    private Address convertToAddressEntity(AddressDto dto) {
        Address address = new Address();
        address.setFullName(dto.getFullName());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        address.setPhone(dto.getPhone());
        address.setEmail(dto.getEmail());
        return address;
    }
    
    private com.cargotracking.shipment_service.model.Package convertToPackageEntity(PackageDto dto) {
        com.cargotracking.shipment_service.model.Package pkg = new com.cargotracking.shipment_service.model.Package();
        pkg.setWeight(dto.getWeight());
        pkg.setLength(dto.getLength());
        pkg.setWidth(dto.getWidth());
        pkg.setHeight(dto.getHeight());
        pkg.setContentType(dto.getContentType());
        pkg.setContentDescription(dto.getContentDescription());
        pkg.setDeclaredValue(dto.getDeclaredValue());
        pkg.setIsFragile(dto.getIsFragile());
        pkg.setRequiresSignature(dto.getRequiresSignature());
        return pkg;
    }
    
    private ShipmentResponse convertToResponse(Shipment shipment) {
        ShipmentResponse response = new ShipmentResponse();
        response.setId(shipment.getId());
        response.setTrackingNumber(shipment.getTrackingNumber());
        response.setSenderUserId(shipment.getSenderUserId());
        response.setSenderAddress(convertToAddressDto(shipment.getSenderAddress()));
        response.setRecipientAddress(convertToAddressDto(shipment.getRecipientAddress()));
        response.setPackageInfo(convertToPackageDto(shipment.getPackageInfo()));
        response.setServiceType(shipment.getServiceType());
        response.setStatus(shipment.getStatus());
        response.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
        response.setActualDeliveryDate(shipment.getActualDeliveryDate());
        response.setShippingCost(shipment.getShippingCost());
        response.setSpecialInstructions(shipment.getSpecialInstructions());
        response.setNotes(shipment.getNotes());
        response.setAssignedCarrierId(shipment.getAssignedCarrierId());
        response.setDeliveryPreferences(convertToDeliveryPreferencesDto(shipment.getDeliveryPreferences()));
        response.setCreatedAt(shipment.getCreatedAt());
        response.setUpdatedAt(shipment.getUpdatedAt());
        response.setCreatedBy(shipment.getCreatedBy());
        response.setUpdatedBy(shipment.getUpdatedBy());
        return response;
    }
    
    private AddressDto convertToAddressDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setFullName(address.getFullName());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setPhone(address.getPhone());
        dto.setEmail(address.getEmail());
        return dto;
    }
    
    private PackageDto convertToPackageDto(com.cargotracking.shipment_service.model.Package pkg) {
        PackageDto dto = new PackageDto();
        dto.setWeight(pkg.getWeight());
        dto.setLength(pkg.getLength());
        dto.setWidth(pkg.getWidth());
        dto.setHeight(pkg.getHeight());
        dto.setContentType(pkg.getContentType());
        dto.setContentDescription(pkg.getContentDescription());
        dto.setDeclaredValue(pkg.getDeclaredValue());
        dto.setIsFragile(pkg.getIsFragile());
        dto.setRequiresSignature(pkg.getRequiresSignature());
        return dto;
    }
    
    /**
     * Teslimat tercihleri dönüşüm metodları
     * Requirements: FR-SM-009
     */
    private DeliveryPreferences convertToDeliveryPreferencesEntity(DeliveryPreferencesDto dto) {
        DeliveryPreferences entity = new DeliveryPreferences();
        entity.setLeaveAtDoor(dto.getLeaveAtDoor());
        entity.setLeaveWithNeighbor(dto.getLeaveWithNeighbor());
        entity.setLeaveWithSecurity(dto.getLeaveWithSecurity());
        entity.setRequireSignature(dto.getRequireSignature());
        entity.setRequireIdCheck(dto.getRequireIdCheck());
        entity.setPreferredTimeSlot(dto.getPreferredTimeSlot());
        entity.setPreferredDay(dto.getPreferredDay());
        entity.setDeliveryInstructions(dto.getDeliveryInstructions());
        entity.setSmsNotification(dto.getSmsNotification());
        entity.setEmailNotification(dto.getEmailNotification());
        entity.setCallBeforeDelivery(dto.getCallBeforeDelivery());
        entity.setAlternativePhone(dto.getAlternativePhone());
        
        if (dto.getAlternativeAddress() != null) {
            entity.setAlternativeAddress(convertToEmbeddableAddress(dto.getAlternativeAddress()));
        }
        
        return entity;
    }
    
    private DeliveryPreferencesDto convertToDeliveryPreferencesDto(DeliveryPreferences entity) {
        if (entity == null) return null;
        
        DeliveryPreferencesDto dto = new DeliveryPreferencesDto();
        dto.setLeaveAtDoor(entity.getLeaveAtDoor());
        dto.setLeaveWithNeighbor(entity.getLeaveWithNeighbor());
        dto.setLeaveWithSecurity(entity.getLeaveWithSecurity());
        dto.setRequireSignature(entity.getRequireSignature());
        dto.setRequireIdCheck(entity.getRequireIdCheck());
        dto.setPreferredTimeSlot(entity.getPreferredTimeSlot());
        dto.setPreferredDay(entity.getPreferredDay());
        dto.setDeliveryInstructions(entity.getDeliveryInstructions());
        dto.setSmsNotification(entity.getSmsNotification());
        dto.setEmailNotification(entity.getEmailNotification());
        dto.setCallBeforeDelivery(entity.getCallBeforeDelivery());
        dto.setAlternativePhone(entity.getAlternativePhone());
        
        if (entity.getAlternativeAddress() != null) {
            dto.setAlternativeAddress(convertFromEmbeddableAddress(entity.getAlternativeAddress()));
        }
        
        return dto;
    }
    
    /**
     * EmbeddableAddress dönüşüm metodları
     */
    private EmbeddableAddress convertToEmbeddableAddress(AddressDto dto) {
        EmbeddableAddress address = new EmbeddableAddress();
        address.setFullName(dto.getFullName());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        address.setPhone(dto.getPhone());
        address.setEmail(dto.getEmail());
        return address;
    }
    
    private AddressDto convertFromEmbeddableAddress(EmbeddableAddress address) {
        AddressDto dto = new AddressDto();
        dto.setFullName(address.getFullName());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setPhone(address.getPhone());
        dto.setEmail(address.getEmail());
        return dto;
    }
}
