import { API_BASE_URL } from './authService';
import { User } from '../types';

export interface Company {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  username: string;
  phone?: string;
  address?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserResponse {
    success: boolean;
    data: User;
    message: string;
}

export interface CompanyListResponse {
  success: boolean;
  data: Company[];
  message: string;
}

export const companyService = {
  // Tüm aktif kargo şirketlerini getir (public endpoint - kayıt için)
  async getShipmentCompanies(): Promise<CompanyListResponse> {
    console.log('🔄 CompanyService: API çağrısı başlatılıyor...');
    console.log('🔄 CompanyService: URL:', `${API_BASE_URL}/api/companies/public`);
    
    try {
      const response = await fetch(`${API_BASE_URL}/api/companies/public`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      console.log('🔄 CompanyService: HTTP Response Status:', response.status);
      console.log('🔄 CompanyService: HTTP Response OK:', response.ok);

      const data = await response.json();
      console.log('🔄 CompanyService: Raw Response Data:', data);
      
      if (response.ok) {
        const companies = data.data || [];
        console.log('✅ CompanyService: Parsed Companies:', companies);
        console.log('✅ CompanyService: Company Count:', companies.length);
        
        return {
          success: true,
          data: companies,
          message: data.message || 'Şirketler başarıyla getirildi'
        };
      } else {
        console.error('❌ CompanyService: HTTP Error Response:', data);
        return {
          success: false,
          data: [],
          message: data.message || 'Şirketler getirilemedi'
        };
      }
    } catch (error) {
      console.error('❌ CompanyService: Network/Parse Error:', error);
      return {
        success: false,
        data: [],
        message: 'Ağ hatası: Şirketler getirilemedi'
      };
    }
  },

  async getUserById(userId: string): Promise<UserResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/admin/internal/users/id/${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      const data = await response.json();

      if (response.ok) {
        return {
          success: true,
          data: data.data,
          message: data.message || 'Kullanıcı başarıyla getirildi'
        };
      } else {
        return {
          success: false,
          data: {} as User,
          message: data.message || `Kullanıcı getirilemedi (ID: ${userId})`
        };
      }
    } catch (error) {
        return {
            success: false,
            data: {} as User,
            message: `Ağ hatası: Kullanıcı getirilemedi (ID: ${userId})`
        };
    }
  }
}; 