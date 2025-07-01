import axios, { AxiosInstance } from 'axios';
import { LoginData, RegisterData, User, ApiResponse } from '../types';

// API base URL - API Gateway portu
const API_BASE_URL = 'http://localhost:8080';

// Axios instance oluştur
const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - token ekleme
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - token süresi dolduğunda logout
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

class AuthService {
  // Giriş yap - API Gateway üzerinden
  async login(data: LoginData): Promise<ApiResponse<{ token: string; user: User }>> {
    console.log('🌐 authService.login called with:', { 
      email: data.email, 
      password: '***',
      apiUrl: API_BASE_URL 
    });
    
    try {
      console.log(`📤 Making request to: ${API_BASE_URL}/api/auth/login`);
      const response = await api.post('/api/auth/login', {
        usernameOrEmail: data.email,
        password: data.password
      });
      console.log('📥 Success! HTTP Response received:', {
        status: response.status,
        statusText: response.statusText,
        data: response.data
      });
      
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('🚨 authService.login error:', error);
      console.error('🚨 Error details:', {
        message: error.message,
        code: error.code,
        response: error.response ? {
          status: error.response.status,
          statusText: error.response.statusText,
          data: error.response.data
        } : 'No response'
      });
      
      return {
        success: false,
        error: error.response?.data?.error || error.message || 'Network error'
      };
    }
  }

  // Kayıt ol
  async register(data: RegisterData): Promise<ApiResponse<{ message: string; user: any }>> {
    console.log('🌐 authService.register called with:', { 
      username: data.username,
      email: data.email, 
      firstName: data.firstName,
      lastName: data.lastName,
      phone: data.phone,
      roles: data.roles,
      apiUrl: API_BASE_URL 
    });
    
    try {
      console.log(`📤 Making request to: ${API_BASE_URL}/api/auth/register`);
      const response = await api.post('/api/auth/register', data);
      console.log('📥 Registration success! HTTP Response received:', {
        status: response.status,
        statusText: response.statusText,
        data: response.data
      });
      
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('🚨 authService.register error:', error);
      console.error('🚨 Error details:', {
        message: error.message,
        code: error.code,
        response: error.response ? {
          status: error.response.status,
          statusText: error.response.statusText,
          data: error.response.data
        } : 'No response'
      });
      
      return {
        success: false,
        error: error.response?.data?.error || error.message || 'Network error'
      };
    }
  }

  // Mevcut kullanıcıyı al
  async getCurrentUser(): Promise<ApiResponse<User>> {
    try {
      const response = await api.get('/api/auth/profile');
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.response?.data?.message || error.message || 'Network error'
      };
    }
  }

  // Şifre sıfırlama
  async resetPassword(email: string): Promise<ApiResponse<string>> {
    try {
      const response = await api.post('/api/auth/reset-password', { email });
      return {
        success: true,
        data: response.data.message
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.response?.data?.message || error.message || 'Network error'
      };
    }
  }

  // Profil güncelleme
  async updateProfile(userData: Partial<User>): Promise<ApiResponse<User>> {
    try {
      const response = await api.put('/api/auth/profile', userData);
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.response?.data?.message || error.message || 'Network error'
      };
    }
  }

  // Token al
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  // Çıkış yap
  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  }

  // Token'ı kaydet
  setToken(token: string): void {
    localStorage.setItem('token', token);
  }

  // Kullanıcı bilgilerini kaydet
  setUser(user: User): void {
    localStorage.setItem('user', JSON.stringify(user));
  }

  // Kullanıcı bilgilerini al
  getStoredUser(): User | null {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }
}

export const authService = new AuthService(); 