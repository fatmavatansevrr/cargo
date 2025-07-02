import { UserRole } from '../types';

// İzin türleri
export enum Permission {
  // Shipment permissions
  CREATE_SHIPMENT = 'CREATE_SHIPMENT',
  VIEW_SHIPMENTS = 'VIEW_SHIPMENTS',
  UPDATE_SHIPMENT = 'UPDATE_SHIPMENT',
  DELETE_SHIPMENT = 'DELETE_SHIPMENT',
  TRACK_SHIPMENTS = 'TRACK_SHIPMENTS',
  
  // Carrier specific permissions
  CARRIER_TRACKING_MANAGEMENT = 'CARRIER_TRACKING_MANAGEMENT', // Sadece CARRIER rolü için
  
  // User management permissions
  MANAGE_USERS = 'MANAGE_USERS',
  VIEW_USERS = 'VIEW_USERS',
  
  // Reporting permissions
  VIEW_REPORTS = 'VIEW_REPORTS',
  EXPORT_REPORTS = 'EXPORT_REPORTS',
  
  // Notification permissions
  VIEW_NOTIFICATIONS = 'VIEW_NOTIFICATIONS',
  MANAGE_NOTIFICATIONS = 'MANAGE_NOTIFICATIONS',
  
  // Kargo şirketi permissions
  SYSTEM_SETTINGS = 'SYSTEM_SETTINGS',
  VIEW_ANALYTICS = 'VIEW_ANALYTICS'
}

// Rol - İzin mapping'i - Backend ile aynı format (ROLE_ prefix olmadan)
const rolePermissions: Record<UserRole, Permission[]> = {
  [UserRole.CUSTOMER]: [
    Permission.CREATE_SHIPMENT,
    Permission.VIEW_SHIPMENTS,
    Permission.TRACK_SHIPMENTS,
    Permission.VIEW_NOTIFICATIONS
  ],
  [UserRole.CARRIER]: [
    Permission.VIEW_SHIPMENTS,
    Permission.UPDATE_SHIPMENT,
    Permission.TRACK_SHIPMENTS,
    Permission.VIEW_NOTIFICATIONS,
    Permission.CARRIER_TRACKING_MANAGEMENT // Sadece CARRIER için özel permission
  ],
  [UserRole.SHIPMENT_COMPANY]: [
    Permission.VIEW_SHIPMENTS,
    Permission.UPDATE_SHIPMENT,
    Permission.DELETE_SHIPMENT,
    Permission.MANAGE_USERS,
    Permission.VIEW_USERS,
    Permission.VIEW_REPORTS,
    Permission.EXPORT_REPORTS,
    Permission.VIEW_NOTIFICATIONS,
    Permission.MANAGE_NOTIFICATIONS,
    Permission.SYSTEM_SETTINGS,
    Permission.VIEW_ANALYTICS
  ]
};

// Backend'den ROLE_ prefix'i ile gelen veriler frontend'de ROLE_ olmadan kullanılıyor

/**
 * Kullanıcının belirli bir izni olup olmadığını kontrol eder
 */
export const hasPermission = (userRole: string | UserRole | undefined | null, permission: Permission): boolean => {
  console.log('🔍 hasPermission - Input role:', userRole, 'Permission:', permission);
  
  const normalizedRole = normalizeRole(userRole);
  console.log('🔍 hasPermission - Normalized role:', normalizedRole);
  
  if (!normalizedRole) {
    console.warn('hasPermission: Geçersiz role:', userRole);
    return false;
  }
  
  const permissions = rolePermissions[normalizedRole];
  console.log('🔍 hasPermission - Available permissions for role:', permissions);
  
  const hasAccess = permissions ? permissions.includes(permission) : false;
  console.log('🔍 hasPermission - Has access:', hasAccess);
  
  return hasAccess;
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
    [Permission.CREATE_SHIPMENT]: 'Gönderi oluşturmak için Müşteri veya Kargo Şirketi yetkisine sahip olmalısınız.',
    [Permission.VIEW_SHIPMENTS]: 'Gönderileri görüntülemek için yetkiniz bulunmamaktadır.',
    [Permission.UPDATE_SHIPMENT]: 'Gönderi durumunu güncellemek için Taşıyıcı veya Kargo Şirketi yetkisine sahip olmalısınız.',
    [Permission.DELETE_SHIPMENT]: 'Gönderi silmek için Kargo Şirketi yetkisine sahip olmalısınız.',
    [Permission.TRACK_SHIPMENTS]: 'Gönderi takibi yapma yetkiniz bulunmamaktadır.',
    [Permission.CARRIER_TRACKING_MANAGEMENT]: 'Kargo takip yönetimi için Taşıyıcı yetkisine sahip olmalısınız.',
    [Permission.MANAGE_USERS]: 'Kullanıcı yönetimi için Kargo Şirketi yetkisine sahip olmalısınız.',
    [Permission.VIEW_USERS]: 'Kullanıcıları görüntülemek için Kargo Şirketi yetkisine sahip olmalısınız.',
    [Permission.VIEW_REPORTS]: 'Raporları görüntülemek için Kargo Şirketi yetkisine sahip olmalısınız.',
    [Permission.EXPORT_REPORTS]: 'Rapor dışa aktarmak için Kargo Şirketi yetkisine sahip olmalısınız.',
    [Permission.VIEW_NOTIFICATIONS]: 'Bu sayfaya erişim yetkiniz bulunmamaktadır.',
    [Permission.MANAGE_NOTIFICATIONS]: 'Bildirimleri yönetmek için Kargo Şirketi yetkisine sahip olmalısınız.',
    [Permission.SYSTEM_SETTINGS]: 'Sistem ayarlarını yönetmek için Kargo Şirketi yetkisine sahip olmalısınız.',
    [Permission.VIEW_ANALYTICS]: 'Analizleri görüntülemek için Kargo Şirketi yetkisine sahip olmalısınız.'
  };
  
  return messages[permission] || 'Bu işlemi gerçekleştirmek için yeterli yetkiniz bulunmamaktadır.';
};

// Role normalizasyon fonksiyonu - Backend'den ROLE_ prefix'i ile gelen veriyi frontend formatına çevirir
const normalizeRole = (role: string | UserRole | undefined | null): UserRole | null => {
  console.log('🔍 normalizeRole - Input:', role, 'Type:', typeof role);
  
  if (!role) return null;
  
  // String'e çevir ve büyük harfe dönüştür
  let roleString = role.toString().toUpperCase();
  
  // Backend'den ROLE_ prefix'i ile gelirse kaldır
  if (roleString.startsWith('ROLE_')) {
    roleString = roleString.replace('ROLE_', '');
  }
  
  console.log('🔍 normalizeRole - Clean Role String:', roleString);
  
  // UserRole enum değerlerini kontrol et
  for (const [key, value] of Object.entries(UserRole)) {
    if (key === roleString || value === roleString) {
      console.log('✅ normalizeRole - Eşleşme bulundu:', value);
      return value as UserRole;
    }
  }
  
  console.warn(`❌ normalizeRole: Eşleşen rol bulunamadı: "${roleString}"`);
  return null;
}; 