import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    BellIcon,
    CheckIcon,
    ExclamationTriangleIcon,
    Cog8ToothIcon,
    InboxIcon,
    EnvelopeIcon,
    DevicePhoneMobileIcon,
    NoSymbolIcon
} from '@heroicons/react/24/outline';
import {
    CheckCircleIcon,
    XCircleIcon,
    ClockIcon,
    BellIcon as BellAlertIcon
} from '@heroicons/react/24/solid';
import { useAuth } from '../context/AuthContext';
import Header from '../components/Header';
import {
    notificationService,
    Notification,
    NotificationPreference,
    NotificationType,
    NotificationStatus,
    NotificationChannel
} from '../services/notificationService';

// Notification Card Component
const NotificationCard: React.FC<{ notification: Notification }> = ({ notification }) => {
    const icon = notificationService.getNotificationIcon(notification.type);
    const bgColor = notificationService.getNotificationColor(notification.type);

    return (
        <div className={`bg-white/80 backdrop-blur-lg rounded-xl p-6 border border-gray-200/50 shadow-sm hover:shadow-md transition-all duration-300 ${
            notification.status === NotificationStatus.READ ? 'opacity-75' : ''
        }`}>
            <div className="flex items-start space-x-4">
                <div className={`flex-shrink-0 w-10 h-10 ${bgColor} rounded-full flex items-center justify-center text-white`}>
                    {icon}
                </div>
                <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                        <h3 className="text-sm font-semibold text-gray-900 truncate">
                            {notification.title}
                        </h3>
                        <div className="flex items-center space-x-2">
                            {notification.status === NotificationStatus.READ ? (
                                <CheckCircleIcon className="w-4 h-4 text-green-500" />
                            ) : (
                                <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                            )}
                            <span className="text-xs text-gray-500">
                {notificationService.formatDate(notification.createdAt)}
              </span>
                        </div>
                    </div>
                    <p className="mt-1 text-sm text-gray-600 line-clamp-2">
                        {notification.message}
                    </p>
                    <div className="mt-2 flex items-center space-x-2">
            <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                notification.channel === NotificationChannel.EMAIL ? 'bg-blue-100 text-blue-800' :
                    notification.channel === NotificationChannel.SMS ? 'bg-green-100 text-green-800' :
                        notification.channel === NotificationChannel.PUSH_NOTIFICATION ? 'bg-purple-100 text-purple-800' :
                            'bg-gray-100 text-gray-800'
            }`}>
              {notification.channel}
            </span>
                        {notification.trackingNumber && (
                            <span className="text-xs text-gray-500">
                #{notification.trackingNumber}
              </span>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

// Notification Preferences Panel
const NotificationPreferencesPanel: React.FC<{
    preferences: NotificationPreference | undefined;
    onUpdateEmail: (enabled: boolean) => void;
    onClose: () => void;
}> = ({ preferences, onUpdateEmail, onClose }) => {
    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center">
            <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 overflow-hidden">
                <div className="bg-gradient-to-r from-indigo-600 to-purple-600 px-6 py-4">
                    <div className="flex items-center justify-between">
                        <h3 className="text-lg font-semibold text-white">Bildirim Ayarları</h3>
                        <button
                            onClick={onClose}
                            className="text-white/70 hover:text-white transition-colors"
                        >
                            <XCircleIcon className="w-6 h-6" />
                        </button>
                    </div>
                </div>

                <div className="p-6 space-y-4">
                    {/* Email Notifications */}
                    <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl">
                        <div className="flex items-center space-x-3">
                            <EnvelopeIcon className="w-5 h-5 text-blue-600" />
                            <div>
                                <p className="font-medium text-gray-900">Email Bildirimleri</p>
                                <p className="text-sm text-gray-500">Email ile bildirim al</p>
                            </div>
                        </div>
                        <label className="relative inline-flex items-center cursor-pointer">
                            <input
                                type="checkbox"
                                checked={preferences?.emailEnabled || false}
                                onChange={(e) => onUpdateEmail(e.target.checked)}
                                className="sr-only peer"
                            />
                            <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                        </label>
                    </div>

                    {/* SMS Notifications */}
                    <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl opacity-50">
                        <div className="flex items-center space-x-3">
                            <DevicePhoneMobileIcon className="w-5 h-5 text-green-600" />
                            <div>
                                <p className="font-medium text-gray-900">SMS Bildirimleri</p>
                                <p className="text-sm text-gray-500">Yakında geliyor</p>
                            </div>
                        </div>
                        <div className="w-11 h-6 bg-gray-200 rounded-full"></div>
                    </div>

                    {/* Push Notifications */}
                    <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl opacity-50">
                        <div className="flex items-center space-x-3">
                            <BellAlertIcon className="w-5 h-5 text-purple-600" />
                            <div>
                                <p className="font-medium text-gray-900">Push Bildirimleri</p>
                                <p className="text-sm text-gray-500">Yakında geliyor</p>
                            </div>
                        </div>
                        <div className="w-11 h-6 bg-gray-200 rounded-full"></div>
                    </div>
                </div>
            </div>
        </div>
    );
};

// Loading Skeleton
const NotificationSkeleton: React.FC = () => (
    <div className="bg-white/80 backdrop-blur-lg rounded-xl p-6 border border-gray-200/50">
        <div className="flex items-start space-x-4">
            <div className="w-10 h-10 bg-gray-200 rounded-full animate-pulse"></div>
            <div className="flex-1 space-y-2">
                <div className="h-4 bg-gray-200 rounded animate-pulse w-3/4"></div>
                <div className="h-3 bg-gray-200 rounded animate-pulse w-1/2"></div>
                <div className="h-3 bg-gray-200 rounded animate-pulse w-1/4"></div>
            </div>
        </div>
    </div>
);

// Empty State
const EmptyState: React.FC<{ filter: string }> = ({ filter }) => (
    <div className="text-center py-12">
        <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
            {filter === 'unread' ? (
                <BellIcon className="w-8 h-8 text-gray-400" />
            ) : (
                <InboxIcon className="w-8 h-8 text-gray-400" />
            )}
        </div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">
            {filter === 'unread' ? 'Okunmamış bildirim yok' : 'Henüz bildirim yok'}
        </h3>
        <p className="text-gray-500 max-w-sm mx-auto">
            {filter === 'unread'
                ? 'Tüm bildirimlerinizi okumuşsunuz!'
                : 'Gönderi durumu değişikliklerinde size bildirim göndereceğiz.'
            }
        </p>
    </div>
);

export default function NotificationsPage() {
    const { user } = useAuth();
    const queryClient = useQueryClient();
    const [currentPage, setCurrentPage] = useState(0);
    const [filter, setFilter] = useState<'all' | 'unread' | 'read'>('all');
    const [showPreferences, setShowPreferences] = useState(false);
    const pageSize = 10;

    // Convert user.id to number
    const userId = user?.id ? parseInt(user.id) : 0;

    // User notifications query
    const {
        data: notificationsData,
        isLoading: notificationsLoading,
        error: notificationsError,
    } = useQuery({
        queryKey: ['notifications', userId, currentPage],
        queryFn: () => notificationService.getUserNotifications(userId, currentPage, pageSize),
        enabled: !!userId,
        staleTime: 30000, // 30 seconds
    });

    // User preferences query
    const {
        data: preferences,
        isLoading: preferencesLoading,
    } = useQuery({
        queryKey: ['notification-preferences', userId],
        queryFn: () => notificationService.getUserPreferences(userId),
        enabled: !!userId && showPreferences,
    });

    // Update email preference mutation
    const updateEmailMutation = useMutation({
        mutationFn: ({ enabled }: { enabled: boolean }) =>
            notificationService.updateEmailPreference(userId, enabled),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['notification-preferences', userId] });
        },
    });

    // Filter notifications
    const filteredNotifications = notificationsData?.content?.filter(notification => {
        if (filter === 'unread') return notification.status === NotificationStatus.UNREAD;
        if (filter === 'read') return notification.status === NotificationStatus.READ;
        return true;
    }) || [];

    const handleUpdateEmailPreference = (enabled: boolean) => {
        updateEmailMutation.mutate({ enabled });
    };

    if (!user) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50 flex items-center justify-center">
                <div className="text-center">
                    <ExclamationTriangleIcon className="w-12 h-12 text-yellow-500 mx-auto mb-4" />
                    <h2 className="text-xl font-semibold text-gray-900">Giriş Yapmanız Gerekiyor</h2>
                    <p className="text-gray-600">Bildirimlerinizi görmek için lütfen giriş yapın.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50">
            <Header />
            <div className="max-w-4xl mx-auto px-4 py-8">
                {/* Header */}
                <div className="bg-white/80 backdrop-blur-lg rounded-2xl shadow-lg border border-white/20 p-6 mb-8">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-4">
                            <div className="w-12 h-12 bg-gradient-to-r from-indigo-600 to-purple-600 rounded-xl flex items-center justify-center">
                                <BellIcon className="w-6 h-6 text-white" />
                            </div>
                            <div>
                                <h1 className="text-2xl font-bold text-gray-900">Bildirimler</h1>
                                <p className="text-gray-600">Gönderi güncellemelerinizi takip edin</p>
                            </div>
                        </div>
                        <button
                            onClick={() => setShowPreferences(true)}
                            className="flex items-center space-x-2 px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                        >
                            <Cog8ToothIcon className="w-5 h-5 text-gray-600" />
                            <span className="text-gray-700">Ayarlar</span>
                        </button>
                    </div>

                    {/* Filter Tabs */}
                    <div className="flex space-x-1 mt-6 bg-gray-100 rounded-lg p-1">
                        {[
                            { key: 'all', label: 'Tümü', count: notificationsData?.totalElements || 0 },
                            { key: 'unread', label: 'Okunmamış', count: filteredNotifications.filter(n => n.status === NotificationStatus.UNREAD).length },
                            { key: 'read', label: 'Okunmuş', count: filteredNotifications.filter(n => n.status === NotificationStatus.READ).length },
                        ].map((tab) => (
                            <button
                                key={tab.key}
                                onClick={() => setFilter(tab.key as any)}
                                className={`flex-1 px-4 py-2 rounded-md text-sm font-medium transition-all ${
                                    filter === tab.key
                                        ? 'bg-white text-indigo-600 shadow-sm'
                                        : 'text-gray-600 hover:text-gray-900'
                                }`}
                            >
                                {tab.label}
                                {tab.count > 0 && (
                                    <span className={`ml-2 px-2 py-0.5 rounded-full text-xs ${
                                        filter === tab.key
                                            ? 'bg-indigo-100 text-indigo-600'
                                            : 'bg-gray-200 text-gray-600'
                                    }`}>
                    {tab.count}
                  </span>
                                )}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Notifications List */}
                <div className="space-y-4">
                    {notificationsLoading ? (
                        // Loading state
                        Array.from({ length: 5 }).map((_, index) => (
                            <NotificationSkeleton key={index} />
                        ))
                    ) : notificationsError ? (
                        // Error state
                        <div className="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
                            <ExclamationTriangleIcon className="w-8 h-8 text-red-500 mx-auto mb-2" />
                            <h3 className="text-red-800 font-medium">Bildirimler yüklenemedi</h3>
                            <p className="text-red-600 text-sm">Lütfen daha sonra tekrar deneyin.</p>
                        </div>
                    ) : filteredNotifications.length === 0 ? (
                        // Empty state
                        <div className="bg-white/80 backdrop-blur-lg rounded-2xl shadow-lg border border-white/20 p-8">
                            <EmptyState filter={filter} />
                        </div>
                    ) : (
                        // Notifications
                        filteredNotifications.map((notification) => (
                            <NotificationCard key={notification.id} notification={notification} />
                        ))
                    )}
                </div>

                {/* Pagination */}
                {notificationsData && notificationsData.totalPages > 1 && (
                    <div className="mt-8 flex justify-center">
                        <nav className="flex space-x-2">
                            {Array.from({ length: notificationsData.totalPages }, (_, i) => (
                                <button
                                    key={i}
                                    onClick={() => setCurrentPage(i)}
                                    className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                                        currentPage === i
                                            ? 'bg-indigo-600 text-white'
                                            : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-300'
                                    }`}
                                >
                                    {i + 1}
                                </button>
                            ))}
                        </nav>
                    </div>
                )}
            </div>

            {/* Preferences Panel */}
            {showPreferences && (
                <NotificationPreferencesPanel
                    preferences={preferences}
                    onUpdateEmail={handleUpdateEmailPreference}
                    onClose={() => setShowPreferences(false)}
                />
            )}
        </div>
    );
}