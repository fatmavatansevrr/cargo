import axios from 'axios';
import { Shipment, StatusUpdate, ApiResponse } from '../types';

// Base API URL
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - token eklemek için
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const shipmentService = {
  // Yeni gönderi oluştur
  async createShipment(shipmentData: Omit<Shipment, 'id' | 'trackingNumber' | 'createdAt' | 'updatedAt'>): Promise<ApiResponse<Shipment>> {
    try {
      const response = await api.post('/shipments', shipmentData);
      return {
        success: true,
        data: response.data,
        message: 'Gönderi başarıyla oluşturuldu',
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.response?.data?.message || 'Gönderi oluşturulurken hata oluştu',
      };
    }
  },

  // Gönderi listesi al
  async getShipments(params?: {
    page?: number;
    limit?: number;
    status?: string;
    senderId?: string;
  }): Promise<ApiResponse<{ shipments: Shipment[]; total: number; pages: number }>> {
    try {
      const response = await api.get('/shipments', { params });
      return {
        success: true,
        data: response.data,
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.response?.data?.message || 'Gönderiler alınırken hata oluştu',
      };
    }
  },

  // Tekil gönderi al
  async getShipment(shipmentId: string): Promise<ApiResponse<Shipment>> {
    try {
      const response = await api.get(`/shipments/${shipmentId}`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.response?.data?.message || 'Gönderi bilgileri alınırken hata oluştu',
      };
    }
  },

  // Takip numarası ile gönderi al
  async trackShipment(trackingNumber: string): Promise<ApiResponse<Shipment>> {
    try {
      const response = await api.get(`/shipments/track/${trackingNumber}`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.response?.data?.message || 'Gönderi takip edilirken hata oluştu',
      };
    }
  },

  // Gönderi güncelle
  async updateShipment(shipmentId: string, updateData: Partial<Shipment>): Promise<ApiResponse<Shipment>> {
    try {
      const response = await api.put(`/shipments/${shipmentId}`, updateData);
      return {
        success: true,
        data: response.data,
        message: 'Gönderi başarıyla güncellendi',
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.response?.data?.message || 'Gönderi güncellenirken hata oluştu',
      };
    }
  },

  // Gönderi iptal et
  async cancelShipment(shipmentId: string, reason?: string): Promise<ApiResponse<Shipment>> {
    try {
      const response = await api.post(`/shipments/${shipmentId}/cancel`, { reason });
      return {
        success: true,
        data: response.data,
        message: 'Gönderi başarıyla iptal edildi',
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.response?.data?.message || 'Gönderi iptal edilirken hata oluştu',
      };
    }
  },

  // Gönderi durumu güncelle
  async updateShipmentStatus(
    shipmentId: string,
    status: string,
    location: string,
    description: string
  ): Promise<ApiResponse<StatusUpdate>> {
    try {
      const response = await api.post(`/shipments/${shipmentId}/status`, {
        status,
        location,
        description,
      });
      return {
        success: true,
        data: response.data,
        message: 'Durum başarıyla güncellendi',
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.response?.data?.message || 'Durum güncellenirken hata oluştu',
      };
    }
  },

  // Gönderi durum geçmişini al
  async getShipmentHistory(shipmentId: string): Promise<ApiResponse<StatusUpdate[]>> {
    try {
      const response = await api.get(`/shipments/${shipmentId}/history`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.response?.data?.message || 'Durum geçmişi alınırken hata oluştu',
      };
    }
  },

  // Dashboard istatistikleri al
  async getDashboardStats(): Promise<ApiResponse<{
    totalShipments: number;
    activeShipments: number;
    deliveredShipments: number;
    pendingShipments: number;
    monthlyGrowth: number;
    recentShipments: Shipment[];
  }>> {
    try {
      const response = await api.get('/shipments/dashboard/stats');
      
      return {
        success: true,
        data: response.data,
      };
    } catch (error: any) {
      console.error('Dashboard stats API error:', {
        message: error.message,
        status: error.response?.status,
        url: error.config?.url
      });
      
      return {
        success: false,
        error: error.response?.data?.message || error.message || 'İstatistikler alınırken hata oluştu',
      };
    }
  },
}; 