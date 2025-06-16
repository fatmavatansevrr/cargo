import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, LoginData, RegisterData, ApiResponse } from '../types';
import { authService } from '../services/authService';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterData) => Promise<{ success: boolean; error?: string }>;
  logout: () => void;
  updateUser: (user: User) => void;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // Sayfa yenilendiğinde token kontrolü
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      (async () => {
        try {
          console.log('Checking stored token...');
          const response = await authService.getCurrentUser();
          if (response.success && response.data) {
            setUser(response.data);
            setIsAuthenticated(true);
          } else {
            // Token geçersiz, temizle
            localStorage.removeItem('token');
          }
        } catch (error) {
          console.error('Token validation failed:', error);
          localStorage.removeItem('token');
        } finally {
          setIsLoading(false);
        }
      })();
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = async (email: string, password: string) => {
    console.log('🔧 AuthContext login called with:', { email, password: '***' });
    setIsLoading(true);
    setError(null);

    try {
      console.log('📡 Making API call to authService.login...');
      const response = await authService.login({ email, password });
      console.log('📡 API Response received:', response);
      
      if (response.success && response.data) {
        console.log('✅ Login API successful, setting user data:', response.data);
        setUser(response.data.user);
        setIsAuthenticated(true);
        
        // Store token in localStorage if available
        if (response.data.token) {
          console.log('💾 Storing token in localStorage');
          localStorage.setItem('token', response.data.token);
        }
      } else {
        console.log('❌ Login API failed:', response.error);
        throw new Error(response.error || 'Giriş yapılamadı');
      }
    } catch (err: any) {
      console.error('💥 AuthContext login error:', err);
      const errorMessage = err.response?.data?.message || err.message || 'Giriş yapılırken hata oluştu';
      console.error('💥 Error message:', errorMessage);
      setError(errorMessage);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: RegisterData): Promise<{ success: boolean; error?: string }> => {
    try {
      setIsLoading(true);
      setError(null);
      
      const response = await authService.register(data);
      
      if (response.success && response.data) {
        localStorage.setItem('token', response.data.token);
        setUser(response.data.user);
        return { success: true };
      } else {
        const errorMessage = response.error || 'Kayıt olurken bilinmeyen bir hata oluştu';
        setError(errorMessage);
        return { success: false, error: errorMessage };
      }
    } catch (error: any) {
      console.error('Register error in context:', error);
      const errorMessage = error.message || 'Kayıt olurken hata oluştu';
      setError(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    setError(null);
    setIsAuthenticated(false);
  };

  const updateUser = (updatedUser: User) => {
    setUser(updatedUser);
  };

  const clearError = () => {
    setError(null);
  };

  const value = {
    user,
    isLoading,
    isAuthenticated,
    error,
    login,
    register,
    logout,
    updateUser,
    clearError,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}; 