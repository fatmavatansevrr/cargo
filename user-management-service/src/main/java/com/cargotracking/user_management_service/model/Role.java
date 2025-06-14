package com.cargotracking.user_management_service.model;

/**
 * Role enum for RBAC - Requirements FR-UM-004
 * Gönderici (Shipper): Kendi gönderilerini oluşturabilir/yönetebilir, takip edebilir, profilini yönetebilir.
 * Taşıyıcı (Carrier): Atanmış gönderilerin durumunu güncelleyebilir, ilgili gönderi detaylarını görebilir.
 * Alıcı/Müşteri (Customer): Gönderileri takip edebilir, profilini ve bildirim tercihlerini yönetebilir.
 * Sistem Yöneticisi (Admin): Tüm kullanıcıları ve gönderileri yönetebilir, sistem ayarlarını yapılandırabilir.
 */
public enum Role {
    ADMIN("Sistem Yöneticisi"),
    SHIPPER("Gönderici"),
    CARRIER("Taşıyıcı"),
    CUSTOMER("Alıcı/Müşteri");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 