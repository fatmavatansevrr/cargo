package com.cargotracking.tracking_service.model;

public enum TrackingState {
    CREATED("Oluşturuldu"),
    PICKED_UP("Kargoya Verildi"),
    IN_TRANSIT("Yolda"),
    AT_SORTING_FACILITY("Transfer Merkezinde"),
    OUT_FOR_DELIVERY("Dağıtımda"),
    DELIVERED("Teslim Edildi"),
    DELIVERY_FAILED("Teslim Edilemedi"),
    RETURNED_TO_SENDER("Gönderene İade"),
    CANCELLED("İptal Edildi");

    private final String description;

    TrackingState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
