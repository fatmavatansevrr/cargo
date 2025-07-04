import React, { useState } from 'react';
import {
    XCircleIcon,
    EnvelopeIcon,
    DevicePhoneMobileIcon,
    BellAlertIcon,
    ChartBarIcon,
    Cog6ToothIcon
} from '@heroicons/react/24/outline';
import {
    NotificationPreference,
    NotificationType,
    notificationService
} from "../services/notificationService";

interface NotificationPreferencesPanelProps {
    preferences: NotificationPreference | undefined;
    notificationStats?: any;
    onUpdateEmail: (enabled: boolean) => void;
    onUpdateSms: (enabled: boolean) => void;
    onUpdatePush: (enabled: boolean) => void;
    onClose: () => void;
    isLoading?: boolean;
}

const NotificationPreferencesPanel: React.FC<NotificationPreferencesPanelProps> = ({
                                                                                       preferences,
                                                                                       notificationStats,
                                                                                       onUpdateEmail,
                                                                                       onUpdateSms,
                                                                                       onUpdatePush,
                                                                                       onClose,
                                                                                       isLoading = false
                                                                                   }) => {
    const [activeTab, setActiveTab] = useState<'preferences' | 'stats'>('preferences');
    const [updatingPreference, setUpdatingPreference] = useState<string | null>(null);

    const handleUpdateEmail = async (enabled: boolean) => {
        setUpdatingPreference('email');
        try {
            await onUpdateEmail(enabled);
        } finally {
            setUpdatingPreference(null);
        }
    };

    const handleUpdateSms = async (enabled: boolean) => {
        setUpdatingPreference('sms');
        try {
            await onUpdateSms(enabled);
        } finally {
            setUpdatingPreference(null);
        }
    };

    const handleUpdatePush = async (enabled: boolean) => {
        setUpdatingPreference('push');
        try {
            await onUpdatePush(enabled);
        } finally {
            setUpdatingPreference(null);
        }
    };

    const PreferenceToggle: React.FC<{
        icon: React.ReactNode;
        title: string;
        description: string;
        enabled: boolean;
        onChange: (enabled: boolean) => void;
        disabled?: boolean;
        loading?: boolean;
    }> = ({ icon, title, description, enabled, onChange, disabled = false, loading = false }) => (
        <div className={`flex items-center justify-between p-4 bg-gray-50 rounded-xl ${disabled ? 'opacity-50' : ''}`}>
            <div className="flex items-center space-x-3">
                <div className="text-gray-600">{icon}</div>
                <div>
                    <p className="font-medium text-gray-900">{title}</p>
                    <p className="text-sm text-gray-500">{description}</p>
                </div>
            </div>

            {disabled ? (
                <div className="w-11 h-6 bg-gray-200 rounded-full"></div>
            ) : (
                <label className="relative inline-flex items-center cursor-pointer">
                    <input
                        type="checkbox"
                        checked={enabled}
                        onChange={(e) => onChange(e.target.checked)}
                        disabled={loading}
                        className="sr-only peer"
                    />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600">
                        {loading && (
                            <div className="absolute inset-0 flex items-center justify-center">
                                <div className="w-3 h-3 border border-gray-400 border-t-transparent rounded-full animate-spin"></div>
                            </div>
                        )}
                    </div>
                </label>
            )}
        </div>
    );

    const StatCard: React.FC<{
        title: string;
        value: number | string;
        subtitle?: string;
        color?: string;
    }> = ({ title, value, subtitle, color = 'blue' }) => (
        <div className="bg-white rounded-lg p-4 border border-gray-200">
            <div className="flex items-center justify-between">
                <div>
                    <p className="text-sm font-medium text-gray-600">{title}</p>
                    <p className={`text-2xl font-bold text-${color}-600`}>{value}</p>
                    {subtitle && <p className="text-xs text-gray-500">{subtitle}</p>}
                </div>
            </div>
        </div>
    );

    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center">
            <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl mx-4 max-h-[90vh] overflow-hidden">
                {/* Header */}
                <div className="bg-gradient-to-r from-indigo-600 to-purple-600 px-6 py-4">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                            <Cog6ToothIcon className="w-6 h-6 text-white" />
                            <h3 className="text-lg font-semibold text-white">Bildirim Ayarları</h3>
                        </div>
                        <button
                            onClick={onClose}
                            className="text-white/70 hover:text-white transition-colors"
                        >
                            <XCircleIcon className="w-6 h-6" />
                        </button>
                    </div>

                    {/* Tabs */}
                    <div className="flex space-x-4 mt-4">
                        <button
                            onClick={() => setActiveTab('preferences')}
                            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                                activeTab === 'preferences'
                                    ? 'bg-white/20 text-white'
                                    : 'text-white/70 hover:text-white hover:bg-white/10'
                            }`}
                        >
                            Tercihler
                        </button>
                        <button
                            onClick={() => setActiveTab('stats')}
                            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                                activeTab === 'stats'
                                    ? 'bg-white/20 text-white'
                                    : 'text-white/70 hover:text-white hover:bg-white/10'
                            }`}
                        >
                            İstatistikler
                        </button>
                    </div>
                </div>

                {/* Content */}
                <div className="p-6 max-h-[60vh] overflow-y-auto">
                    {activeTab === 'preferences' && (
                        <div className="space-y-4">
                            <div className="mb-6">
                                <h4 className="text-lg font-medium text-gray-900 mb-2">Bildirim Kanalları</h4>
                                <p className="text-sm text-gray-600">Hangi kanallardan bildirim almak istediğinizi seçin.</p>
                            </div>

                            {/* Email Notifications */}
                            <PreferenceToggle
                                icon={<EnvelopeIcon className="w-5 h-5" />}
                                title="Email Bildirimleri"
                                description="Kargo güncellemelerini email ile al"
                                enabled={preferences?.emailEnabled || false}
                                onChange={handleUpdateEmail}
                                loading={updatingPreference === 'email'}
                            />

                            {/* SMS Notifications */}
                            <PreferenceToggle
                                icon={<DevicePhoneMobileIcon className="w-5 h-5" />}
                                title="SMS Bildirimleri"
                                description="Önemli güncellemeleri SMS ile al"
                                enabled={preferences?.smsEnabled || false}
                                onChange={handleUpdateSms}
                                loading={updatingPreference === 'sms'}
                            />

                            {/* Push Notifications */}
                            <PreferenceToggle
                                icon={<BellAlertIcon className="w-5 h-5" />}
                                title="Push Bildirimleri"
                                description="Tarayıcı bildirimleri al"
                                enabled={preferences?.pushEnabled || false}
                                onChange={handleUpdatePush}
                                loading={updatingPreference === 'push'}
                            />

                            {/* In-App Notifications (Always enabled) */}
                            <PreferenceToggle
                                icon={<BellAlertIcon className="w-5 h-5" />}
                                title="Uygulama İçi Bildirimler"
                                description="Her zaman aktif - kapatılamaz"
                                enabled={true}
                                onChange={() => {}}
                                disabled={true}
                            />

                            {/* Notification Types */}
                            <div className="mt-8">
                                <h4 className="text-lg font-medium text-gray-900 mb-4">Bildirim Türleri</h4>
                                <div className="grid grid-cols-2 gap-3">
                                    {[
                                        { key: 'shipment', label: 'Gönderi Güncellemeleri', enabled: true },
                                        { key: 'delivery', label: 'Teslimat Bildirimleri', enabled: true },
                                        { key: 'system', label: 'Sistem Bildirimleri', enabled: true },
                                        { key: 'marketing', label: 'Pazarlama Bildirimleri', enabled: false },
                                    ].map((item) => (
                                        <div key={item.key} className="flex items-center space-x-2 p-3 bg-gray-50 rounded-lg">
                                            <input
                                                type="checkbox"
                                                id={item.key}
                                                checked={item.enabled}
                                                disabled={item.key !== 'marketing'}
                                                className="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500"
                                                onChange={() => {}} // Placeholder - backend'de implement edilecek
                                            />
                                            <label htmlFor={item.key} className="text-sm text-gray-700">
                                                {item.label}
                                            </label>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    )}

                    {activeTab === 'stats' && (
                        <div className="space-y-6">
                            <div>
                                <h4 className="text-lg font-medium text-gray-900 mb-2">Bildirim İstatistikleri</h4>
                                <p className="text-sm text-gray-600">Bildirim geçmişiniz ve kullanım istatistikleriniz.</p>
                            </div>

                            {/* Overall Stats */}
                            <div className="grid grid-cols-2 gap-4">
                                <StatCard
                                    title="Toplam Bildirim"
                                    value={notificationStats?.total || 0}
                                    color="blue"
                                />
                                <StatCard
                                    title="Okunmamış"
                                    value={notificationStats?.unread || 0}
                                    color="red"
                                />
                                <StatCard
                                    title="Bu Ay"
                                    value={notificationStats?.thisMonth || 0}
                                    color="green"
                                />
                                <StatCard
                                    title="Uygulama İçi"
                                    value={notificationStats?.channels?.inApp || 0}
                                    color="purple"
                                />
                            </div>

                            {/* Channel Distribution */}
                            <div>
                                <h5 className="font-medium text-gray-900 mb-3">Kanal Dağılımı</h5>
                                <div className="space-y-3">
                                    {[
                                        { channel: 'Email', count: notificationStats?.channels?.email || 0, color: 'bg-blue-500' },
                                        { channel: 'SMS', count: notificationStats?.channels?.sms || 0, color: 'bg-green-500' },
                                        { channel: 'Push', count: notificationStats?.channels?.push || 0, color: 'bg-purple-500' },
                                        { channel: 'Uygulama İçi', count: notificationStats?.channels?.inApp || 0, color: 'bg-indigo-500' },
                                    ].map((item) => {
                                        const total = notificationStats?.total || 1;
                                        const percentage = Math.round((item.count / total) * 100);

                                        return (
                                            <div key={item.channel} className="flex items-center space-x-3">
                                                <div className="flex-1">
                                                    <div className="flex justify-between text-sm mb-1">
                                                        <span className="text-gray-700">{item.channel}</span>
                                                        <span className="text-gray-500">{item.count} ({percentage}%)</span>
                                                    </div>
                                                    <div className="w-full bg-gray-200 rounded-full h-2">
                                                        <div
                                                            className={`${item.color} h-2 rounded-full transition-all duration-500`}
                                                            style={{ width: `${percentage}%` }}
                                                        ></div>
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>

                            {/* Type Distribution */}
                            <div>
                                <h5 className="font-medium text-gray-900 mb-3">Tür Dağılımı</h5>
                                <div className="grid grid-cols-1 gap-2">
                                    {Object.entries(notificationStats?.types || {}).map(([type, count]) => (
                                        <div key={type} className="flex justify-between items-center p-2 bg-gray-50 rounded">
                                            <span className="text-sm text-gray-700">
                                                {notificationService.getNotificationTypeLabel(type as NotificationType)}
                                            </span>
                                            <span className="text-sm font-medium text-gray-900">{count as number}</span>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="border-t border-gray-200 px-6 py-4 bg-gray-50">
                    <div className="flex justify-between items-center">
                        <p className="text-xs text-gray-500">
                            Son güncelleme: {preferences?.updatedAt ?
                            notificationService.formatDate(preferences.updatedAt) :
                            'Bilinmiyor'
                        }
                        </p>
                        <button
                            onClick={onClose}
                            className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors text-sm"
                        >
                            Tamam
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default NotificationPreferencesPanel;