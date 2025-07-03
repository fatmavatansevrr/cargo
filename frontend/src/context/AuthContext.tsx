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

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

// Backend'den ROLE_ prefix'i ile gelen veriler frontend'de ROLE_ olmadan kullanılıyor

// Role normalizasyon fonksiyonu - Backend'den ROLE_ prefix'i ile gelen veriyi frontend formatına çevirir
const normalizeUserRole = (userData: any): UserRole | null => {
  if (!userData) return null;

  console.log('🔍 normalizeUserRole - Gelen Veri:', userData);

  let roleString: string | undefined;

  // 1. Spring Security'den gelen 'authorities'
  if (Array.isArray(userData.authorities) && userData.authorities.length > 0) {
    const authority = userData.authorities[0];
    roleString = typeof authority === 'string' ? authority : authority?.authority;
  }
  // 2. 'roles' dizisi (genellikle JWT payload)
  else if (Array.isArray(userData.roles) && userData.roles.length > 0) {
    const role = userData.roles[0];
    roleString = typeof role === 'string' ? role : role?.name;
  }
  // 3. 'role' alanı (string veya nesne)
  else if (userData.role) {
    roleString = typeof userData.role === 'string' ? userData.role : userData.role?.name;
  }

  console.log('🔍 normalizeUserRole - Ham Rol Dizesi:', roleString);

  if (!roleString) {
    console.warn('❌ normalizeUserRole: Kullanıcı verisinden rol dizesi çıkarılamadı.');
    return null;
  }

  // String'e çevir ve büyük harfe dönüştür
  let cleanRoleString = roleString.toUpperCase();
  
  // Backend'den ROLE_ prefix'i ile gelirse kaldır
  if (cleanRoleString.startsWith('ROLE_')) {
    cleanRoleString = cleanRoleString.replace('ROLE_', '');
  }
  
  console.log('🔍 normalizeUserRole - Temizlenmiş Rol Dizesi:', cleanRoleString);

  // Enum ile eşleştirme - ROLE_ prefix'i olmadan
  for (const [key, value] of Object.entries(UserRole)) {
    if (key === cleanRoleString || value === cleanRoleString) {
      console.log('✅ normalizeUserRole - Eşleşme bulundu:', value);
      return value as UserRole;
    }
  }

  console.warn(`❌ normalizeUserRole: Eşleşen rol bulunamadı: "${cleanRoleString}"`);
  return null;
};

// User object normalizasyon fonksiyonu
const normalizeUser = (userData: any): User | null => {
  if (!userData) return null;
  
  const normalizedRole = normalizeUserRole(userData);
  console.log('🔍 normalizeUser - normalizedRole:', normalizedRole);
  
  if (!normalizedRole) {
    console.warn('Kullanıcının geçerli bir rolü yok. Role:', userData.roles || userData.role);
    return null;
  }
  
  const user = {
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
  
  console.log('🔍 normalizeUser - final user object:', user);
  console.log('🔍 normalizeUser - final user role:', user.role);
  console.log('🔍 normalizeUser - final user role type:', typeof user.role);
  
  // Force re-render trigger için timestamp ekle
  (user as any).lastUpdated = Date.now();
  
  return user;
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
        console.log('🐛 AUTH DEBUG - checkAuthStatus success, normalized user:', normalizedUser);
        console.log('🐛 AUTH DEBUG - Setting user in state from checkAuthStatus');
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
        console.log('🐛 AUTH DEBUG - Login success, normalized user:', normalizedUser);
        console.log('🐛 AUTH DEBUG - Setting user in state');
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