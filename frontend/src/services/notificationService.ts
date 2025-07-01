import { authService } from './authService';

// API Base URL
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// Notification Types
export interface Notification {
    id: string;
    userId: number;
    trackingNumber?: string;
    shipmentId?: number;
    type: NotificationType;
    channel: NotificationChannel;
    title: string;
    message: string;
    recipient: string;
    status: NotificationStatus;
    createdAt: string;
    sentAt?: string;
    updatedAt: string;
}

export enum NotificationType {
    SHIPMENT_CREATED = 'SHIPMENT_CREATED',
    SHIPMENT_UPDATED = 'SHIPMENT_UPDATED',
    SHIPMENT_CANCELLED = 'SHIPMENT_CANCELLED',
    STATUS_CHANGED = 'STATUS_CHANGED',
    DELIVERY_COMPLETED = 'DELIVERY_COMPLETED',
    DELIVERY_FAILED = 'DELIVERY_FAILED',
    OUT_FOR_DELIVERY = 'OUT_FOR_DELIVERY',
    PACKAGE_ARRIVED = 'PACKAGE_ARRIVED',
    DELIVERY_EXCEPTION = 'DELIVERY_EXCEPTION',
    SYSTEM_ALERT = 'SYSTEM_ALERT',
    REMINDER = 'REMINDER'
}

export enum NotificationChannel {
    EMAIL = 'EMAIL',
    SMS = 'SMS',
    PUSH_NOTIFICATION = 'PUSH_NOTIFICATION',
    IN_APP = 'IN_APP'
}

export enum NotificationStatus {
    PENDING = 'PENDING',
    SENT = 'SENT',
    DELIVERED = 'DELIVERED',
    FAILED = 'FAILED',
    READ = 'READ',
    UNREAD = 'UNREAD',
    EXPIRED = 'EXPIRED'
}

// Notification Preferences
export interface NotificationPreference {
    id: string;
    userId: number;
    emailEnabled: boolean;
    smsEnabled: boolean;
    pushEnabled: boolean;
    inAppEnabled: boolean;
    createdAt: string;
    updatedAt: string;
}

// API Response Types
export interface NotificationPageResponse {
    content: Notification[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

// Manual Notification Request
export interface ManualNotificationRequest {
    userId: string;
    type: string;
    subject: string;
    content: string;
}

class NotificationService {
    private getAuthHeaders() {
        const token = authService.getToken();
        return {
            'Content-Type': 'application/json',
            ...(token && { Authorization: `Bearer ${token}` }),
        };
    }

    private async handleResponse<T>(response: Response): Promise<T> {
        if (!response.ok) {
            const error = await response.text();
            console.error('API Error:', {
                status: response.status,
                statusText: response.statusText,
                error
            });
            throw new Error(`API Error: ${response.status} - ${error}`);
        }
        return response.json();
    }

    /**
     * Kullanıcının bildirimlerini getirir
     */
    async getUserNotifications(userId: number, page = 0, size = 20): Promise<NotificationPageResponse> {
        try {
            console.log('📋 Fetching user notifications:', { userId, page, size });

            const url = `${API_BASE_URL}/api/notifications/user/${userId}?page=${page}&size=${size}`;
            const response = await fetch(url, {
                method: 'GET',
                headers: this.getAuthHeaders(),
            });

            const data = await this.handleResponse<NotificationPageResponse>(response);
            console.log('✅ User notifications retrieved:', data);
            return data;

        } catch (error) {
            console.error('❌ Error fetching user notifications:', error);
            throw error;
        }
    }

    /**
     * Kullanıcının bildirim tercihlerini getirir
     */
    async getUserPreferences(userId: number): Promise<NotificationPreference> {
        try {
            console.log('⚙️ Fetching user preferences:', { userId });

            const url = `${API_BASE_URL}/api/notifications/preferences/${userId}`;
            const response = await fetch(url, {
                method: 'GET',
                headers: this.getAuthHeaders(),
            });

            const data = await this.handleResponse<NotificationPreference>(response);
            console.log('✅ User preferences retrieved:', data);
            return data;

        } catch (error) {
            console.error('❌ Error fetching user preferences:', error);
            throw error;
        }
    }

    /**
     * Email tercihlerini günceller
     */
    async updateEmailPreference(userId: number, enabled: boolean): Promise<NotificationPreference> {
        try {
            console.log('📧 Updating email preference:', { userId, enabled });

            const url = `${API_BASE_URL}/api/notifications/preferences/${userId}/email?enabled=${enabled}`;
            const response = await fetch(url, {
                method: 'PUT',
                headers: this.getAuthHeaders(),
            });

            const data = await this.handleResponse<NotificationPreference>(response);
            console.log('✅ Email preference updated:', data);
            return data;

        } catch (error) {
            console.error('❌ Error updating email preference:', error);
            throw error;
        }
    }

    /**
     * Manuel bildirim gönderir (Test için)
     */
    async sendManualNotification(request: ManualNotificationRequest): Promise<Notification> {
        try {
            console.log('📤 Sending manual notification:', request);

            const url = `${API_BASE_URL}/api/notifications/send`;
            const response = await fetch(url, {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify(request),
            });

            const data = await this.handleResponse<Notification>(response);
            console.log('✅ Manual notification sent:', data);
            return data;

        } catch (error) {
            console.error('❌ Error sending manual notification:', error);
            throw error;
        }
    }

    /**
     * Notification type'ların Türkçe açıklamalarını döndürür
     */
    getNotificationTypeLabel(type: NotificationType): string {
        const labels: Record<NotificationType, string> = {
            [NotificationType.SHIPMENT_CREATED]: 'Gönderi Oluşturuldu',
            [NotificationType.SHIPMENT_UPDATED]: 'Gönderi Güncellendi',
            [NotificationType.SHIPMENT_CANCELLED]: 'Gönderi İptal Edildi',
            [NotificationType.STATUS_CHANGED]: 'Durum Değişti',
            [NotificationType.DELIVERY_COMPLETED]: 'Teslimat Tamamlandı',
            [NotificationType.DELIVERY_FAILED]: 'Teslimat Başarısız',
            [NotificationType.OUT_FOR_DELIVERY]: 'Teslimat İçin Yola Çıktı',
            [NotificationType.PACKAGE_ARRIVED]: 'Paket Geldi',
            [NotificationType.DELIVERY_EXCEPTION]: 'Teslimat İstisnası',
            [NotificationType.SYSTEM_ALERT]: 'Sistem Uyarısı',
            [NotificationType.REMINDER]: 'Hatırlatma'
        };
        return labels[type] || type;
    }

    /**
     * Notification status'ların Türkçe açıklamalarını döndürür
     */
    getNotificationStatusLabel(status: NotificationStatus): string {
        const labels: Record<NotificationStatus, string> = {
            [NotificationStatus.PENDING]: 'Beklemede',
            [NotificationStatus.SENT]: 'Gönderildi',
            [NotificationStatus.DELIVERED]: 'Teslim Edildi',
            [NotificationStatus.FAILED]: 'Başarısız',
            [NotificationStatus.READ]: 'Okundu',
            [NotificationStatus.UNREAD]: 'Okunmadı',
            [NotificationStatus.EXPIRED]: 'Süresi Doldu'
        };
        return labels[status] || status;
    }

    /**
     * Notification channel'ların Türkçe açıklamalarını döndürür
     */
    getNotificationChannelLabel(channel: NotificationChannel): string {
        const labels: Record<NotificationChannel, string> = {
            [NotificationChannel.EMAIL]: 'E-posta',
            [NotificationChannel.SMS]: 'SMS',
            [NotificationChannel.PUSH_NOTIFICATION]: 'Push Bildirimi',
            [NotificationChannel.IN_APP]: 'Uygulama İçi'
        };
        return labels[channel] || channel;
    }

    /**
     * Notification type'ına göre ikon döndürür
     */
    getNotificationIcon(type: NotificationType): string {
        const icons: Record<NotificationType, string> = {
            [NotificationType.SHIPMENT_CREATED]: '📦',
            [NotificationType.SHIPMENT_UPDATED]: '📝',
            [NotificationType.SHIPMENT_CANCELLED]: '❌',
            [NotificationType.STATUS_CHANGED]: '🔄',
            [NotificationType.DELIVERY_COMPLETED]: '✅',
            [NotificationType.DELIVERY_FAILED]: '❌',
            [NotificationType.OUT_FOR_DELIVERY]: '🚚',
            [NotificationType.PACKAGE_ARRIVED]: '📬',
            [NotificationType.DELIVERY_EXCEPTION]: '⚠️',
            [NotificationType.SYSTEM_ALERT]: '🔔',
            [NotificationType.REMINDER]: '⏰'
        };
        return icons[type] || '📢';
    }

    /**
     * Notification type'ına göre renk döndürür
     */
    getNotificationColor(type: NotificationType): string {
        const colors: Record<NotificationType, string> = {
            [NotificationType.DELIVERY_COMPLETED]: 'bg-green-500',
            [NotificationType.DELIVERY_FAILED]: 'bg-red-500',
            [NotificationType.SHIPMENT_CANCELLED]: 'bg-red-500',
            [NotificationType.STATUS_CHANGED]: 'bg-blue-500',
            [NotificationType.OUT_FOR_DELIVERY]: 'bg-blue-500',
            [NotificationType.SHIPMENT_CREATED]: 'bg-purple-500',
            [NotificationType.SHIPMENT_UPDATED]: 'bg-indigo-500',
            [NotificationType.PACKAGE_ARRIVED]: 'bg-green-500',
            [NotificationType.DELIVERY_EXCEPTION]: 'bg-orange-500',
            [NotificationType.SYSTEM_ALERT]: 'bg-orange-500',
            [NotificationType.REMINDER]: 'bg-yellow-500'
        };
        return colors[type] || 'bg-gray-500';
    }

    /**
     * Tarih formatını döndürür
     */
    formatDate(dateString: string): string {
        const date = new Date(dateString);
        const now = new Date();
        const diffInHours = (now.getTime() - date.getTime()) / (1000 * 60 * 60);

        if (diffInHours < 1) {
            return 'Az önce';
        } else if (diffInHours < 24) {
            return `${Math.floor(diffInHours)} saat önce`;
        } else if (diffInHours < 168) { // 7 days
            return `${Math.floor(diffInHours / 24)} gün önce`;
        } else {
            return date.toLocaleDateString('tr-TR', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        }
    }
}

export const notificationService = new NotificationService();