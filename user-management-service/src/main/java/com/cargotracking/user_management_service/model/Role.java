package com.cargotracking.user_management_service.model;

/**
 * Role enum for RBAC - Requirements FR-UM-004
 * CUSTOMER: Kargo gönderen veya alan son kullanıcı. Bu rol, sistemdeki her iki tarafı da temsil eder.
 * CARRIER: Kargo durumunu güncelleyen taşıyıcı personel.
 * SHIPMENT_COMPANY: Analiz ve yönetim paneline erişimi olan, taşıyıcıları yöneten aracı kargo şirketi.
 */
public enum Role {
    CUSTOMER("Müşteri - Gönderici/Alıcı"),
    CARRIER("Taşıyıcı Personel"),
    SHIPMENT_COMPANY("Kargo Şirketi");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 