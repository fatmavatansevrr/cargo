// Kullanıcı rolleri için enum
export enum UserRole {
  SHIPPER = 'shipper',
  CARRIER = 'carrier',
  CUSTOMER = 'customer',
  ADMIN = 'admin'
}

// Gönderi durumları için enum
export enum ShipmentStatus {
  CREATED = 'created',
  PICKED_UP = 'picked_up',
  IN_TRANSIT = 'in_transit',
  AT_TRANSIT_HUB = 'at_transit_hub',
  OUT_FOR_DELIVERY = 'out_for_delivery',
  DELIVERED = 'delivered',
  DELIVERY_FAILED = 'delivery_failed',
  CANCELLED = 'cancelled'
}

// Kullanıcı interface'i
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  role: UserRole;
  roles?: string[]; // Backend'den gelen roles array'i
  createdAt: string;
  updatedAt: string;
}

// Adres interface'i
export interface Address {
  street: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
}

// Gönderi interface'i
export interface Shipment {
  id: string;
  trackingNumber: string;
  senderId: string;
  recipientName: string;
  recipientPhone: string;
  senderAddress: Address;
  recipientAddress: Address;
  packageDetails: {
    weight: number;
    dimensions: {
      length: number;
      width: number;
      height: number;
    };
    contentType: string;
    isFragile: boolean;
    isLiquid: boolean;
  };
  serviceType: 'standard' | 'express' | 'overnight';
  status: ShipmentStatus;
  estimatedDelivery: string;
  actualDelivery?: string;
  deliveryInstructions?: string;
  createdAt: string;
  updatedAt: string;
}

// Durum güncellemesi interface'i
export interface StatusUpdate {
  id: string;
  shipmentId: string;
  status: ShipmentStatus;
  location: string;
  description: string;
  updatedBy: string;
  timestamp: string;
}

// API Response interface'i
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

// Giriş form verileri
export interface LoginData {
  email: string;
  password: string;
}

// Kayıt form verileri
export interface RegisterData {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone: string;
  address?: string;
  roles: string[]; // Backend'de Set<Role> bekliyor
} 