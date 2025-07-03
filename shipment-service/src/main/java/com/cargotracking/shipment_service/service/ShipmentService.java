package com.cargotracking.shipment_service.service;

import com.cargotracking.shipment_service.client.UserServiceClient;
import com.cargotracking.shipment_service.dto.*;
import com.cargotracking.shipment_service.event.ShipmentEvent;
import com.cargotracking.shipment_service.model.*;
import com.cargotracking.shipment_service.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.HashMap;

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
    private final UserServiceClient userServiceClient;
    private final RestTemplate restTemplate;
    
    @Value("${app.analytics-service.url}")
    private String analyticsServiceUrl;
    
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
        shipment.setSenderCustomerId(senderUserId); // Güncellenmiş alan adı
        shipment.setSenderAddress(convertToAddressEntity(request.getSenderAddress()));
        
        // Recipient address'e iletişim bilgilerini ekle
        Address recipientAddress = convertToAddressEntity(request.getRecipientAddress());
        recipientAddress.setEmail(request.getRecipientEmail());
        recipientAddress.setPhone(request.getRecipientPhone());
        shipment.setRecipientAddress(recipientAddress);
        shipment.setPackageInfo(convertToPackageEntity(request.getPackageInfo()));
        shipment.setServiceType(request.getServiceType());
        shipment.setStatus(Shipment.ShipmentStatus.ACTIVE);
        shipment.setShipmentCompanyId(request.getShipmentCompanyId());
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
        
        // Otomatik carrier atama
        log.info("Carrier atama işlemi başlatılıyor. Şirket ID: {}", request.getShipmentCompanyId());
        Long assignedCarrierId = assignCarrierAutomatically(request.getShipmentCompanyId());
        if (assignedCarrierId != null) {
            shipment.setAssignedCarrierId(assignedCarrierId);
            log.info("✅ Gönderi başarıyla carrier'a atandı. Tracking: {}, Carrier ID: {}", 
                shipment.getTrackingNumber(), assignedCarrierId);
        } else {
            log.warn("⚠️ Otomatik carrier ataması başarısız. Şirket ID: {}", request.getShipmentCompanyId());
        }
        
        // Veritabanına kaydet
        Shipment savedShipment = shipmentRepository.save(shipment);
        
        // Kayıt sonrası doğrulama logu
        log.info("💾 Gönderi veritabanına kaydedildi. ID: {}, Tracking: {}, Atanan Carrier: {}", 
            savedShipment.getId(), savedShipment.getTrackingNumber(), savedShipment.getAssignedCarrierId());
        
        // Kafka olayı yayınla - recipient iletişim bilgileri ile
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("shipment", convertToResponse(savedShipment));
        eventData.put("recipientEmail", request.getRecipientEmail());
        eventData.put("recipientPhone", request.getRecipientPhone());
        
        publishShipmentEvent(ShipmentEvent.created(
            savedShipment.getId(),
            savedShipment.getTrackingNumber(),
            savedShipment.getSenderCustomerId(),
            eventData
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
        return shipmentRepository.findBySenderCustomerId(userId, pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Carrier'a atanmış gönderileri listeleme
     * Requirements: Carrier rolündeki kullanıcıların atandığı gönderileri görüntülemesi
     */
    @Transactional(readOnly = true)
    public List<ShipmentResponse> findCarrierShipments(Long carrierId) {
        log.info("🔍 Carrier gönderileri listeleniyor. Carrier ID: {}", carrierId);
        
        // Sadece bu carrier'a atanmış ve aktif olan gönderileri getir
        List<Shipment> shipments = shipmentRepository.findByAssignedCarrierIdAndStatus(
            carrierId, 
                Shipment.ShipmentStatus.ACTIVE
        );
        
        log.info("📦 Bulunan atanmış kargo sayısı: {} (Carrier ID: {})", shipments.size(), carrierId);
        
        return shipments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
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
        
        // Teslim edildi ise gerçek teslimat tarihini set et ve analiz verisi gönder
        if (newStatus == Shipment.ShipmentStatus.FINISHED) {
            shipment.setActualDeliveryDate(LocalDateTime.now());
            sendAnalyticsData(shipment);
        }
        
        Shipment updatedShipment = shipmentRepository.save(shipment);
        
        // Kafka olayı yayınla
        publishShipmentEvent(ShipmentEvent.updated(
            updatedShipment.getId(),
            updatedShipment.getTrackingNumber(),
            updatedShipment.getSenderCustomerId(),
            newStatus,
            previousStatus,
            convertToResponse(updatedShipment)
        ));
        
        log.info("Gönderi durumu güncellendi. Takip numarası: {}, Durum: {} -> {}", 
                updatedShipment.getTrackingNumber(), previousStatus, newStatus);
        
        return convertToResponse(updatedShipment);
    }
    
    /**
     * Kafka event'i ile gelen 'teslim edildi' bilgisine göre gönderiyi sonlandırır.
     * Bu metot, tracking-service'den gelen DELIVERED durumu üzerine tetiklenir.
     */
    @Transactional
    public void finalizeShipment(String trackingNumber) {
        log.info("Gönderi sonlandırma işlemi başlatıldı. Takip Numarası: {}", trackingNumber);

        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Takip numarasına sahip gönderi bulunamadı: " + trackingNumber));

        Shipment.ShipmentStatus previousStatus = shipment.getStatus();
        Shipment.ShipmentStatus newStatus = Shipment.ShipmentStatus.FINISHED;

        if (previousStatus == newStatus) {
            log.warn("Gönderi zaten {} durumunda. İşlem yapılmayacak. Takip Numarası: {}", newStatus, trackingNumber);
            return;
        }

        if (!previousStatus.canTransitionTo(newStatus)) {
            log.error("Geçersiz durum geçişi denemesi: {} -> {}. Takip Numarası: {}", previousStatus, newStatus, trackingNumber);
            throw new IllegalStateException("Geçersiz durum geçişi: " + previousStatus + " -> " + newStatus);
        }

        shipment.setStatus(newStatus);
        shipment.setActualDeliveryDate(LocalDateTime.now());
        shipment.setUpdatedBy(0L); // 0L -> SİSTEM kullanıcısı

        sendAnalyticsData(shipment);
        Shipment finalizedShipment = shipmentRepository.save(shipment);

        publishShipmentEvent(ShipmentEvent.updated(
            finalizedShipment.getId(),
            finalizedShipment.getTrackingNumber(),
            finalizedShipment.getSenderCustomerId(),
            newStatus,
            previousStatus,
            convertToResponse(finalizedShipment)
        ));

        log.info("Gönderi başarıyla sonlandırıldı (FINISHED). Takip Numarası: {}", trackingNumber);
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
            updatedShipment.getSenderCustomerId(),
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
            canceledShipment.getSenderCustomerId(),
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
        response.setSenderCustomerId(shipment.getSenderCustomerId()); // Güncellenmiş alan adı
        response.setSenderAddress(convertToAddressDto(shipment.getSenderAddress()));
        response.setRecipientAddress(convertToAddressDto(shipment.getRecipientAddress()));
        
        // Recipient iletişim bilgileri - address'ten al
        if (shipment.getRecipientAddress() != null) {
            response.setRecipientEmail(shipment.getRecipientAddress().getEmail());
            response.setRecipientPhone(shipment.getRecipientAddress().getPhone());
        }
        
        response.setPackageInfo(convertToPackageDto(shipment.getPackageInfo()));
        response.setServiceType(shipment.getServiceType());
        response.setStatus(shipment.getStatus());
        response.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
        response.setActualDeliveryDate(shipment.getActualDeliveryDate());
        response.setShippingCost(shipment.getShippingCost());
        response.setSpecialInstructions(shipment.getSpecialInstructions());
        response.setNotes(shipment.getNotes());
        response.setAssignedCarrierId(shipment.getAssignedCarrierId());
        response.setShipmentCompanyId(shipment.getShipmentCompanyId());
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
    
    /**
     * Dashboard istatistikleri hesaplama
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        log.info("Dashboard istatistikleri hesaplanıyor");
        
        try {
            // Tüm gönderileri al
            List<Shipment> allShipments = shipmentRepository.findAll();
            
            // İstatistikleri hesapla
            long totalShipments = allShipments.size();
            long activeShipments = allShipments.stream()
                .filter(s -> s.getStatus() == Shipment.ShipmentStatus.ACTIVE)
                .count();
            
            long deliveredShipments = allShipments.stream()
                .filter(s -> s.getStatus() == Shipment.ShipmentStatus.FINISHED)
                .count();

            
            // Aylık büyüme oranını hesapla (basit bir simülasyon)
            double monthlyGrowth = calculateMonthlyGrowth(allShipments);
            
            // Son 10 gönderiyi al
            List<ShipmentResponse> recentShipments = allShipments.stream()
                .sorted((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()))
                .limit(10)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalShipments", totalShipments);
            stats.put("activeShipments", activeShipments);
            stats.put("deliveredShipments", deliveredShipments);
            stats.put("monthlyGrowth", monthlyGrowth);
            stats.put("recentShipments", recentShipments);
            
            log.info("Dashboard istatistikleri hesaplandı. Toplam gönderi: {}", totalShipments);
            return stats;
            
        } catch (Exception e) {
            log.error("Dashboard istatistikleri hesaplanırken hata: {}", e.getMessage(), e);
            throw new RuntimeException("Dashboard istatistikleri hesaplanamadı: " + e.getMessage());
        }
    }
    
    /**
     * Aylık büyüme oranını hesapla
     */
    private double calculateMonthlyGrowth(List<Shipment> allShipments) {
        if (allShipments.isEmpty()) {
            return 0.0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        LocalDateTime twoMonthsAgo = now.minusMonths(2);
        
        long currentMonthShipments = allShipments.stream()
            .filter(s -> s.getCreatedAt().isAfter(oneMonthAgo))
            .count();
        
        long previousMonthShipments = allShipments.stream()
            .filter(s -> s.getCreatedAt().isAfter(twoMonthsAgo) && s.getCreatedAt().isBefore(oneMonthAgo))
            .count();
        
        if (previousMonthShipments == 0) {
            return currentMonthShipments > 0 ? 100.0 : 0.0;
        }
        
        return ((double) (currentMonthShipments - previousMonthShipments) / previousMonthShipments) * 100.0;
    }
    
    /**
     * Test amaçlı örnek gönderi verisi oluşturma
     */
    public List<ShipmentResponse> createTestShipments() {
        log.info("Test gönderileri oluşturuluyor");
        
        List<Shipment> testShipments = List.of(
            createTestShipment(
                "CT" + System.currentTimeMillis() + "001",
                "Ahmet Yılmaz",
                "+90532123456",
                "ahmet@example.com",
                "Atatürk Cad. No:123, Beşiktaş",
                "İstanbul",
                "34000",
                "Cumhuriyet Mah. Barış Sok. No:45",
                "Ankara",
                "06000",
                2.5,
                "Elektronik",
                true,
                false,
                Shipment.ServiceType.EXPRESS,
                Shipment.ShipmentStatus.ACTIVE
            ),
            createTestShipment(
                "CT" + System.currentTimeMillis() + "002", 
                "Fatma Demir",
                "+90533987654",
                "fatma@example.com",
                "İnönü Bulvarı No:67, Çankaya",
                "Ankara", 
                "06100",
                "Kemal Paşa Cad. No:234, Konak",
                "İzmir",
                "35000",
                1.2,
                "Kitap",
                false,
                false,
                Shipment.ServiceType.STANDARD,
                Shipment.ShipmentStatus.ACTIVE
            ),
            createTestShipment(
                "CT" + System.currentTimeMillis() + "003",
                "Mehmet Kaya",
                "+90544555666",
                "mehmet@example.com", 
                "Bağdat Cad. No:456, Kadıköy",
                "İstanbul",
                "34710",
                "Atatürk Bulvarı No:789, Alsancak",
                "İzmir",
                "35220",
                0.8,
                "Giyim",
                false,
                false,
                Shipment.ServiceType.STANDARD,
                Shipment.ShipmentStatus.ACTIVE
            ),
            createTestShipment(
                "CT" + System.currentTimeMillis() + "004",
                "Ayşe Özdemir",
                "+90555777888",
                "ayse@example.com",
                "Cumhuriyet Cad. No:321, Şişli",
                "İstanbul", 
                "34380",
                "Gazi Mustafa Kemal Bulvarı No:654, Çankaya",
                "Ankara",
                "06420",
                3.1,
                "Elektronik Aksesuar",
                true,
                false,
                Shipment.ServiceType.EXPRESS,
                Shipment.ShipmentStatus.ACTIVE
            )
        );
        
        List<Shipment> savedShipments = shipmentRepository.saveAll(testShipments);
        log.info("{} test gönderisi oluşturuldu", savedShipments.size());
        
        return savedShipments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private Shipment createTestShipment(String trackingNumber, String recipientName, String recipientPhone, 
                                       String recipientEmail, String senderStreet, String senderCity, 
                                       String senderPostalCode, String recipientStreet, String recipientCity, 
                                       String recipientPostalCode, double weight, String contentType, 
                                       boolean isFragile, boolean isLiquid, Shipment.ServiceType serviceType,
                                       Shipment.ShipmentStatus status) {
        
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(trackingNumber);
        shipment.setSenderCustomerId(1L); // Test customer ID
        
        // Sender address
        Address senderAddress = new Address();
        senderAddress.setFullName("Test Gönderici");
        senderAddress.setAddressLine1(senderStreet);
        senderAddress.setCity(senderCity);
        senderAddress.setState(senderCity);
        senderAddress.setPostalCode(senderPostalCode);
        senderAddress.setCountry("Türkiye");
        shipment.setSenderAddress(senderAddress);
        
        // Recipient address
        Address recipientAddress = new Address();
        recipientAddress.setFullName(recipientName);
        recipientAddress.setAddressLine1(recipientStreet);
        recipientAddress.setCity(recipientCity);
        recipientAddress.setState(recipientCity);
        recipientAddress.setPostalCode(recipientPostalCode);
        recipientAddress.setCountry("Türkiye");
        recipientAddress.setEmail(recipientEmail);
        recipientAddress.setPhone(recipientPhone);
        shipment.setRecipientAddress(recipientAddress);
        
        // Package info
        com.cargotracking.shipment_service.model.Package packageInfo = new com.cargotracking.shipment_service.model.Package();
        packageInfo.setWeight(BigDecimal.valueOf(weight));
        packageInfo.setLength(BigDecimal.valueOf(30));
        packageInfo.setWidth(BigDecimal.valueOf(20));
        packageInfo.setHeight(BigDecimal.valueOf(15));
        packageInfo.setContentType(com.cargotracking.shipment_service.model.Package.ContentType.valueOf(contentType.replace(" ", "_").toUpperCase()));
        packageInfo.setIsFragile(isFragile);
        packageInfo.setDeclaredValue(BigDecimal.valueOf(100.0));
        shipment.setPackageInfo(packageInfo);
        
        shipment.setServiceType(serviceType);
        shipment.setStatus(status);
        shipment.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(serviceType == Shipment.ServiceType.EXPRESS ? 1 : 3));
        shipment.setShippingCost(BigDecimal.valueOf(serviceType == Shipment.ServiceType.EXPRESS ? 50.0 : 25.0));
        shipment.setCreatedBy(1L);
        shipment.setUpdatedBy(1L);
        
        return shipment;
    }

    /**
     * Otomatik carrier atama - En az kargo sayısına sahip carrier'ı seçer
     * Aynı sayıda kargo olan carrier'lar varsa random seçer
     * Synchronized - Race condition'ı önlemek için
     */
    private synchronized Long assignCarrierAutomatically(Long shipmentCompanyUserId) {
        if (shipmentCompanyUserId == null) {
            log.error("❌ Carrier ataması için şirket kullanıcısının ID'si null olamaz.");
            return null;
        }

        try {
            // 1. Adım: Gelen ID ile şirket kullanıcısının bilgilerini al.
            log.info("📞 User-management-service çağrılıyor. Şirket Kullanıcı ID: {}", shipmentCompanyUserId);
            UserServiceClient.ApiResponseWrapper<UserServiceClient.UserDto> userResponseWrapper = userServiceClient.getUserById(shipmentCompanyUserId);

            if (userResponseWrapper == null || !userResponseWrapper.isSuccess() || userResponseWrapper.getData() == null) {
                log.warn("⚠️ Şirket kullanıcısı bilgileri alınamadı. Kullanıcı ID: {}", shipmentCompanyUserId);
                return null;
            }

            UserServiceClient.UserDto companyUser = userResponseWrapper.getData();
            Long actualCompanyId = companyUser.getCompanyId();

            if (actualCompanyId == null) {
                log.warn("⚠️ Kullanıcının bir şirketi bulunmuyor. Kullanıcı ID: {}", shipmentCompanyUserId);
                return null;
            }

            // 2. Adım: Alınan gerçek şirket ID'si ile o şirketin kuryelerini getir.
            log.info("📞 User-management-service çağrılıyor. Gerçek Şirket ID: {}", actualCompanyId);
            UserServiceClient.ApiResponseWrapper<List<UserServiceClient.UserDto>> carriersResponseWrapper = userServiceClient.getCompanyCarriers(actualCompanyId);

            if (carriersResponseWrapper != null && carriersResponseWrapper.isSuccess() && carriersResponseWrapper.getData() != null && !carriersResponseWrapper.getData().isEmpty()) {
                List<UserServiceClient.UserDto> carriers = carriersResponseWrapper.getData();
                log.info("✅ {} şirketine ait {} adet carrier bulundu.", actualCompanyId, carriers.size());

                // Rastgele bir carrier seç
                UserServiceClient.UserDto chosenCarrier = carriers.get(random.nextInt(carriers.size()));
                log.info("Seçilen carrier: ID {}", chosenCarrier.getId());

                return chosenCarrier.getId();
            } else {
                String reason = (carriersResponseWrapper == null) ? "null response" :
                                !carriersResponseWrapper.isSuccess() ? "başarısız yanıt" :
                                "boş data";
                log.warn("⚠️ {} şirketi için uygun carrier bulunamadı. Sebep: {}", actualCompanyId, reason);
                if (carriersResponseWrapper != null && !carriersResponseWrapper.isSuccess()) {
                    log.warn("Hata mesajı: {}", carriersResponseWrapper.getMessage());
                }
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Carrier'ları getirirken hata oluştu. Şirket Kullanıcı ID: {}. Hata: {}", shipmentCompanyUserId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Analiz servisine veri gönderme
     * Bu metot, gönderi tamamlandığında tetiklenir.
     */
    private void sendAnalyticsData(Shipment shipment) {
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate is not available. Skipping sending analytics data.");
            return;
        }

        log.info("Sending analytics data for tracking number: {}", shipment.getTrackingNumber());
        try {
            AnalyticsDataEvent analyticsDataEvent = AnalyticsDataEvent.builder()
                .shipmentId(shipment.getId().toString())
                .trackingNumber(shipment.getTrackingNumber())
                .companyId(String.valueOf(shipment.getShipmentCompanyId()))
                .carrierId(shipment.getAssignedCarrierId() != null ? shipment.getAssignedCarrierId().toString() : "N/A")
                .shipperId(shipment.getSenderCustomerId().toString())
                .status(shipment.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();

            if (shipment.getEstimatedDeliveryDate() != null && shipment.getActualDeliveryDate() != null) {
                Duration deliveryDuration = Duration.between(shipment.getEstimatedDeliveryDate(), shipment.getActualDeliveryDate());
                analyticsDataEvent.setDeliveryDelayHours((int) deliveryDuration.toHours());
            } else {
                analyticsDataEvent.setDeliveryDelayHours(0);
            }

            kafkaTemplate.send("analytics-topic", analyticsDataEvent);
            log.info("Successfully sent analytics data for tracking number: {}", shipment.getTrackingNumber());

        } catch (Exception e) {
            log.error("Error sending analytics data for tracking number: {}", shipment.getTrackingNumber(), e);
        }
    }
}
