import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authService } from '../services/authService';
import { User, UserRole } from '../types';

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<boolean>;
  logout: () => void;
  isAuthenticated: boolean;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

// Role normalizasyon fonksiyonu
const normalizeUserRole = (userData: any): UserRole | null => {
  if (!userData) return null;
  
  // Eğer role field'ı varsa
  if (userData.role && typeof userData.role === 'string') {
    const role = userData.role.toLowerCase();
    if (Object.values(UserRole).includes(role as UserRole)) {
      return role as UserRole;
    }
  }
  
  // Eğer roles array'i varsa
  if (userData.roles && Array.isArray(userData.roles) && userData.roles.length > 0) {
    const role = userData.roles[0].toLowerCase();
    if (Object.values(UserRole).includes(role as UserRole)) {
      return role as UserRole;
    }
  }
  
  // Eğer authorities field'ı varsa (Spring Security format)
  if (userData.authorities && Array.isArray(userData.authorities) && userData.authorities.length > 0) {
    const authority = userData.authorities[0];
    let roleString = '';
    
    if (typeof authority === 'string') {
      roleString = authority.replace('ROLE_', '');
    } else if (authority.authority) {
      roleString = authority.authority.replace('ROLE_', '');
    }
    
    const role = roleString.toLowerCase();
    if (Object.values(UserRole).includes(role as UserRole)) {
      return role as UserRole;
    }
  }
  
  return null;
};

// User object normalizasyon fonksiyonu
const normalizeUser = (userData: any): User | null => {
  if (!userData) return null;
  
  const normalizedRole = normalizeUserRole(userData);
  if (!normalizedRole) {
    console.warn('Kullanıcının geçerli bir rolü yok. Role:', userData.roles || userData.role);
    return null;
  }
  
  return {
    id: userData.id || userData.userId || '',
    email: userData.email || userData.username || '',
    firstName: userData.firstName || userData.name || userData.fullName || 'Kullanıcı',
    lastName: userData.lastName || '',
    phone: userData.phone || userData.phoneNumber || '',
    role: normalizedRole,
    roles: userData.roles || (normalizedRole ? [normalizedRole] : []),
    createdAt: userData.createdAt || userData.createDate || new Date().toISOString(),
    updatedAt: userData.updatedAt || userData.updateDate || new Date().toISOString()
  };
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setIsLoading(false);
        return;
      }

      // Token'ı validate etmek için getCurrentUser kullan
      const response = await authService.getCurrentUser();
      
      if (response.success && response.data) {
        const normalizedUser = normalizeUser(response.data);
        setUser(normalizedUser);
      } else {
        // Token geçersiz, temizle
        localStorage.removeItem('token');
        setUser(null);
      }
    } catch (error) {
      console.error('Auth status check failed:', error);
      localStorage.removeItem('token');
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (email: string, password: string): Promise<boolean> => {
    try {
      setIsLoading(true);
      const response = await authService.login({ email, password });
      
      if (response.success && response.data) {
        // Token'ı kaydet
        if (response.data.token) {
          localStorage.setItem('token', response.data.token);
        }
        
        // User bilgilerini normalize et
        const normalizedUser = normalizeUser(response.data.user || response.data);
        setUser(normalizedUser);
        
        return true;
      } else {
        console.error('Login failed:', response.error);
        return false;
      }
    } catch (error) {
      console.error('Login error:', error);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
  };

  const isAuthenticated = !!user && !!localStorage.getItem('token');

  return (
    <AuthContext.Provider value={{
      user,
      login,
      logout,
      isAuthenticated,
      isLoading
    }}>
      {children}
    </AuthContext.Provider>
  );
}; 