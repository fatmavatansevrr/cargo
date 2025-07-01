import { UserRole } from '../types';

// İzin türleri
export enum Permission {
  // Shipment permissions
  CREATE_SHIPMENT = 'CREATE_SHIPMENT',
  VIEW_SHIPMENTS = 'VIEW_SHIPMENTS',
  UPDATE_SHIPMENT = 'UPDATE_SHIPMENT',
  DELETE_SHIPMENT = 'DELETE_SHIPMENT',
  TRACK_SHIPMENTS = 'TRACK_SHIPMENTS',
  
  // User management permissions
  MANAGE_USERS = 'MANAGE_USERS',
  VIEW_USERS = 'VIEW_USERS',
  
  // Reporting permissions
  VIEW_REPORTS = 'VIEW_REPORTS',
  EXPORT_REPORTS = 'EXPORT_REPORTS',
  
  // Notification permissions
  VIEW_NOTIFICATIONS = 'VIEW_NOTIFICATIONS',
  MANAGE_NOTIFICATIONS = 'MANAGE_NOTIFICATIONS',
  
  // Admin permissions
  SYSTEM_SETTINGS = 'SYSTEM_SETTINGS',
  VIEW_ANALYTICS = 'VIEW_ANALYTICS'
}

// Rol - İzin mapping'i (role string değerleri ile)
const rolePermissions: Record<UserRole, Permission[]> = {
  [UserRole.ADMIN]: [
    Permission.CREATE_SHIPMENT,
    Permission.VIEW_SHIPMENTS,
    Permission.UPDATE_SHIPMENT,
    Permission.DELETE_SHIPMENT,
    Permission.TRACK_SHIPMENTS,
    Permission.MANAGE_USERS,
    Permission.VIEW_USERS,
    Permission.VIEW_REPORTS,
    Permission.EXPORT_REPORTS,
    Permission.VIEW_NOTIFICATIONS,
    Permission.MANAGE_NOTIFICATIONS,
    Permission.SYSTEM_SETTINGS,
    Permission.VIEW_ANALYTICS
  ],
  [UserRole.SHIPPER]: [
    Permission.CREATE_SHIPMENT,
    Permission.VIEW_SHIPMENTS,
    Permission.UPDATE_SHIPMENT,
    Permission.TRACK_SHIPMENTS,
    Permission.VIEW_NOTIFICATIONS,
    Permission.VIEW_REPORTS
  ],
  [UserRole.CARRIER]: [
    Permission.VIEW_SHIPMENTS,
    Permission.UPDATE_SHIPMENT,
    Permission.TRACK_SHIPMENTS,
    Permission.VIEW_NOTIFICATIONS
  ],
  [UserRole.CUSTOMER]: [
    Permission.VIEW_SHIPMENTS,
    Permission.TRACK_SHIPMENTS,
    Permission.VIEW_NOTIFICATIONS
  ]
};

/**
 * Role değerini string'e çevirir - backend'den object olarak gelebilir
 */
const extractRoleString = (userRole: any): string => {
  if (typeof userRole === 'string') {
    return userRole.toLowerCase();
  }
  
  // Role object ise (örn: {name: "ADMIN"} veya {roleName: "admin"})
  if (typeof userRole === 'object' && userRole !== null) {
    // Possible field names for role
    const possibleFields = ['name', 'roleName', 'role', 'authority'];
    
    for (const field of possibleFields) {
      if (userRole[field] && typeof userRole[field] === 'string') {
        return String(userRole[field]).toLowerCase();
      }
    }
    
    // Eğer array ise ilk elemanı al
    if (Array.isArray(userRole) && userRole.length > 0) {
      return extractRoleString(userRole[0]);
    }
  }
  
  console.error('❌ Could not extract role string from:', userRole);
  return '';
};

/**
 * Kullanıcının belirli bir izni olup olmadığını kontrol eder
 */
export const hasPermission = (userRole: string | UserRole | undefined | null, permission: Permission): boolean => {
  const normalizedRole = normalizeRole(userRole);
  
  if (!normalizedRole) {
    console.warn('hasPermission: Geçersiz role:', userRole);
    return false;
  }
  
  const permissions = rolePermissions[normalizedRole];
  return permissions ? permissions.includes(permission) : false;
};

/**
 * Kullanıcının birden fazla izne sahip olup olmadığını kontrol eder (hepsi gerekli)
 */
export const hasAllPermissions = (userRole: string | UserRole | undefined | null, permissions: Permission[]): boolean => {
  return permissions.every(permission => hasPermission(userRole, permission));
};

/**
 * Kullanıcının birden fazla izinden en az birine sahip olup olmadığını kontrol eder
 */
export const hasAnyPermission = (userRole: string | UserRole | undefined | null, permissions: Permission[]): boolean => {
  return permissions.some(permission => hasPermission(userRole, permission));
};

/**
 * Rol için tüm izinleri getirir
 */
export const getUserPermissions = (userRole: string | UserRole | undefined | null): Permission[] => {
  const normalizedRole = normalizeRole(userRole);
  
  if (!normalizedRole) {
    console.warn('getUserPermissions: Geçersiz role:', userRole);
    return [];
  }
  
  return rolePermissions[normalizedRole] || [];
};

/**
 * Sayfa erişim kontrolü için yardımcı fonksiyonlar
 */
export const canAccessPage = (userRole: string | UserRole | undefined | null, requiredPermissions: Permission[]): boolean => {
  if (!requiredPermissions.length) return true;
  
  return requiredPermissions.some(permission => hasPermission(userRole, permission));
};

/**
 * Kullanıcı dostu hata mesajları
 */
export const getAccessDeniedMessage = (permission: Permission): string => {
  const messages = {
    [Permission.CREATE_SHIPMENT]: 'Gönderi oluşturmak için Gönderici veya Admin yetkisine sahip olmalısınız.',
    [Permission.VIEW_SHIPMENTS]: 'Gönderileri görüntülemek için yetkiniz bulunmamaktadır.',
    [Permission.UPDATE_SHIPMENT]: 'Gönderi durumunu güncellemek için Taşıyıcı veya Admin yetkisine sahip olmalısınız.',
    [Permission.DELETE_SHIPMENT]: 'Gönderi silmek için Admin yetkisine sahip olmalısınız.',
    [Permission.TRACK_SHIPMENTS]: 'Gönderi takibi yapma yetkiniz bulunmamaktadır.',
    [Permission.MANAGE_USERS]: 'Kullanıcı yönetimi için Admin yetkisine sahip olmalısınız.',
    [Permission.VIEW_USERS]: 'Kullanıcıları görüntülemek için Admin yetkisine sahip olmalısınız.',
    [Permission.VIEW_REPORTS]: 'Raporları görüntülemek için Gönderici veya Admin yetkisine sahip olmalısınız.',
    [Permission.EXPORT_REPORTS]: 'Rapor dışa aktarmak için Admin yetkisine sahip olmalısınız.',
    [Permission.VIEW_NOTIFICATIONS]: 'Bu sayfaya erişim yetkiniz bulunmamaktadır.',
    [Permission.MANAGE_NOTIFICATIONS]: 'Bildirimleri yönetmek için Admin yetkisine sahip olmalısınız.',
    [Permission.SYSTEM_SETTINGS]: 'Sistem ayarlarını yönetmek için Admin yetkisine sahip olmalısınız.',
    [Permission.VIEW_ANALYTICS]: 'Analizleri görüntülemek için Admin yetkisine sahip olmalısınız.'
  };
  
  return messages[permission] || 'Bu işlemi gerçekleştirmek için yeterli yetkiniz bulunmamaktadır.';
};

// Role normalizasyon fonksiyonu
const normalizeRole = (role: string | UserRole | undefined | null): UserRole | null => {
  if (!role) return null;
  
  const roleString = role.toString().toUpperCase();
  
  // UserRole enum değerlerini kontrol et
  for (const [key, value] of Object.entries(UserRole)) {
    if (value.toUpperCase() === roleString || key.toUpperCase() === roleString) {
      return value as UserRole;
    }
  }
  
  return null;
}; 