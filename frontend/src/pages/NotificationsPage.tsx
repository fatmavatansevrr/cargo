import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';
import { useNavigate } from 'react-router-dom'; // react-router-dom kullanıyorsanız
// import { useRouter } from 'next/router'; // Next.js kullanıyorsanız
import { useToast } from '../hooks/useToast';

// Icons
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

// Components and Services
import { useAuth } from '../context/AuthContext';
import Header from '../components/Header';
import NotificationCard from '../components/NotificationCardProps';
import NotificationPreferencesPanel from '../components/NotificationPreferencesPanelProps';
import {
    notificationService,
    Notification,
    NotificationPreference,
    NotificationType,
    NotificationStatus,
    NotificationChannel
} from '../services/notificationService';

// Loading Skeleton Component
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

// Empty State Component
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

// Main Component
export default function NotificationsPage() {
    const { user } = useAuth();
    const queryClient = useQueryClient();
    const navigate = useNavigate(); // react-router-dom için
    // const router = useRouter(); // Next.js için

    const [currentPage, setCurrentPage] = useState(0);
    const [filter, setFilter] = useState<'all' | 'unread' | 'read'>('all');
    const [showPreferences, setShowPreferences] = useState(false);
    const [selectedNotifications, setSelectedNotifications] = useState<string[]>([]);
    const pageSize = 10;
    const customToast = useToast();
    useEffect(() => {
        customToast.info('Bildirimler yenilendi');
    }, []); // [] -> sadece ilk renderda
    const userId = user?.id ? parseInt(user.id.toString()) : 0;

    // User notifications query
    const {
        data: notificationsData,
        isLoading: notificationsLoading,
        error: notificationsError,
        refetch: refetchNotifications,
    } = useQuery({
        queryKey: ['notifications', userId, currentPage, filter],
        queryFn: () => {
            const status = filter === 'all' ? undefined : filter.toUpperCase();
            return notificationService.getNotificationsWithFilter(userId, currentPage, pageSize, status);
        },
        enabled: !!userId,
        staleTime: 30000,
        refetchInterval: 60000, // 1 dakikada bir güncelle
    });

    // Unread count query
    const {
        data: unreadCount,
        isLoading: unreadCountLoading,
    } = useQuery({
        queryKey: ['unread-count', userId],
        queryFn: () => notificationService.getUnreadCount(userId),
        enabled: !!userId,
        refetchInterval: 30000,
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

    // Notification stats query
    const {
        data: notificationStats,
        isLoading: statsLoading,
    } = useQuery({
        queryKey: ['notification-stats', userId],
        queryFn: () => notificationService.getNotificationStats(userId),
        enabled: !!userId && showPreferences,
    });

    // Mutations
    const markAsReadMutation = useMutation({
        mutationFn: ({ notificationId, userId }: { notificationId: string; userId: number }) =>
            notificationService.markAsRead(notificationId, userId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['notifications', userId] });
            queryClient.invalidateQueries({ queryKey: ['unread-count', userId] });
            toast.success('Bildirim okundu olarak işaretlendi');
        },
        onError: () => {
            toast.error('Bildirim güncellenirken hata oluştu');
        },
    });

    const markAllAsReadMutation = useMutation({
        mutationFn: ({ userId, inAppOnly }: { userId: number; inAppOnly: boolean }) =>
            notificationService.markAllAsRead(userId, inAppOnly),
        onSuccess: (data) => {
            queryClient.invalidateQueries({ queryKey: ['notifications', userId] });
            queryClient.invalidateQueries({ queryKey: ['unread-count', userId] });
            toast.success(`${data.updatedCount} bildirim okundu olarak işaretlendi`);
        },
        onError: () => {
            toast.error('Bildirimler güncellenirken hata oluştu');
        },
    });

    const deleteNotificationMutation = useMutation({
        mutationFn: ({ notificationId, userId }: { notificationId: string; userId: number }) =>
            notificationService.deleteNotification(notificationId, userId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['notifications', userId] });
            queryClient.invalidateQueries({ queryKey: ['unread-count', userId] });
            toast.success('Bildirim silindi');
        },
        onError: () => {
            toast.error('Bildirim silinirken hata oluştu');
        },
    });

    const clearAllNotificationsMutation = useMutation({
        mutationFn: ({ userId, readOnly }: { userId: number; readOnly: boolean }) =>
            notificationService.clearAllNotifications(userId, readOnly),
        onSuccess: (data) => {
            queryClient.invalidateQueries({ queryKey: ['notifications', userId] });
            queryClient.invalidateQueries({ queryKey: ['unread-count', userId] });
            toast.success(`${data.deletedCount} bildirim temizlendi`);
        },
        onError: () => {
            toast.error('Bildirimler temizlenirken hata oluştu');
        },
    });

    // Preference mutations
    const updateEmailMutation = useMutation({
        mutationFn: ({ enabled }: { enabled: boolean }) =>
            notificationService.updateEmailPreference(userId, enabled),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['notification-preferences', userId] });
            toast.success('Email tercihleri güncellendi');
        },
    });

    const updatePushMutation = useMutation({
        mutationFn: ({ enabled }: { enabled: boolean }) =>
            notificationService.updatePushPreference(userId, enabled),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['notification-preferences', userId] });
            toast.success('Push bildirim tercihleri güncellendi');
        },
    });

    const updateSmsMutation = useMutation({
        mutationFn: ({ enabled }: { enabled: boolean }) =>
            notificationService.updateSmsPreference(userId, enabled),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['notification-preferences', userId] });
            toast.success('SMS tercihleri güncellendi');
        },
    });

    // Handler functions
    const handleMarkAsRead = (notificationId: string) => {
        markAsReadMutation.mutate({ notificationId, userId });
    };

    const handleMarkAllAsRead = () => {
        if (window.confirm('Tüm bildirimleri okundu olarak işaretlemek istediğinizden emin misiniz?')) {
            markAllAsReadMutation.mutate({ userId, inAppOnly: false });
        }
    };

    const handleDeleteNotification = (notificationId: string) => {
        if (window.confirm('Bu bildirimi silmek istediğinizden emin misiniz?')) {
            deleteNotificationMutation.mutate({ notificationId, userId });
        }
    };

    const handleClearAllNotifications = () => {
        if (window.confirm('Tüm bildirimleri temizlemek istediğinizden emin misiniz? Bu işlem geri alınamaz.')) {
            clearAllNotificationsMutation.mutate({ userId, readOnly: false });
        }
    };

    const handleClearReadNotifications = () => {
        if (window.confirm('Okunmuş bildirimleri temizlemek istediğinizden emin misiniz?')) {
            clearAllNotificationsMutation.mutate({ userId, readOnly: true });
        }
    };

    const handleNotificationClick = (notification: Notification) => {
        // Eğer okunmamışsa okundu olarak işaretle
        if (notification.status === NotificationStatus.UNREAD) {
            handleMarkAsRead(notification.id);
        }

        // Eğer tracking number varsa tracking sayfasına yönlendir
        if (notification.trackingNumber) {
            navigate(`/track/${notification.trackingNumber}`); // react-router-dom için
            // router.push(`/track/${notification.trackingNumber}`); // Next.js için
        }
    };

    const handleUpdateEmailPreference = (enabled: boolean) => {
        updateEmailMutation.mutate({ enabled });
    };

    const handleUpdatePushPreference = (enabled: boolean) => {
        updatePushMutation.mutate({ enabled });
    };

    const handleUpdateSmsPreference = (enabled: boolean) => {
        updateSmsMutation.mutate({ enabled });
    };

    // Bulk operations
    const handleBulkMarkAsRead = () => {
        if (selectedNotifications.length === 0) {
            toast.error('Lütfen işlem yapmak için bildirim seçin');
            return;
        }

        selectedNotifications.forEach(notificationId => {
            markAsReadMutation.mutate({ notificationId, userId });
        });
        setSelectedNotifications([]);
    };

    const handleBulkDelete = () => {
        if (selectedNotifications.length === 0) {
            toast.error('Lütfen silmek için bildirim seçin');
            return;
        }

        if (window.confirm(`${selectedNotifications.length} bildirimi silmek istediğinizden emin misiniz?`)) {
            selectedNotifications.forEach(notificationId => {
                deleteNotificationMutation.mutate({ notificationId, userId });
            });
            setSelectedNotifications([]);
        }
    };

    // Keyboard shortcuts
    useEffect(() => {
        const handleKeyPress = (event: KeyboardEvent) => {
            if (event.ctrlKey || event.metaKey) {
                switch (event.key) {
                    case 'a':
                        event.preventDefault();
                        handleMarkAllAsRead();
                        break;
                    case 'r':
                        event.preventDefault();
                        refetchNotifications();
                        customToast.info('Bildirimler yenilendi');
                        break;
                    case 'd':
                        event.preventDefault();
                        if (selectedNotifications.length > 0) {
                            handleBulkDelete();
                        }
                        break;
                }
            }
        };

        window.addEventListener('keydown', handleKeyPress);
        return () => window.removeEventListener('keydown', handleKeyPress);
    }, [selectedNotifications, refetchNotifications]);

    // Auto-refresh
    useEffect(() => {
        const interval = setInterval(() => {
            if (userId && !showPreferences) {
                queryClient.invalidateQueries({ queryKey: ['notifications', userId] });
                queryClient.invalidateQueries({ queryKey: ['unread-count', userId] });
            }
        }, 120000); // 2 dakikada bir

        return () => clearInterval(interval);
    }, [userId, showPreferences, queryClient]);

    // Filter notifications
    const filteredNotifications = notificationsData?.content || [];
    const totalUnread = unreadCount?.totalUnread || 0;
    const totalRead = (notificationsData?.totalElements || 0) - totalUnread;

    // Loading states
    const isAnyMutationLoading = markAsReadMutation.isPending ||
        deleteNotificationMutation.isPending ||
        markAllAsReadMutation.isPending ||
        clearAllNotificationsMutation.isPending;

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
            <div className="max-w-5xl mx-auto px-4 py-8">
                {/* Header with actions */}
                <div className="bg-white/80 backdrop-blur-lg rounded-2xl shadow-lg border border-white/20 p-6 mb-8">
                    <div className="flex items-center justify-between mb-6">
                        <div className="flex items-center space-x-4">
                            <div className="relative">
                                <div className="w-12 h-12 bg-gradient-to-r from-indigo-600 to-purple-600 rounded-xl flex items-center justify-center">
                                    <BellIcon className="w-6 h-6 text-white" />
                                </div>
                                {totalUnread > 0 && (
                                    <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-6 h-6 flex items-center justify-center font-bold">
                                        {totalUnread > 99 ? '99+' : totalUnread}
                                    </span>
                                )}
                            </div>
                            <div>
                                <h1 className="text-2xl font-bold text-gray-900">Bildirimler</h1>
                                <p className="text-gray-600">Gönderi güncellemelerinizi takip edin</p>
                            </div>
                        </div>

                        <div className="flex items-center space-x-3">
                            {/* Bulk actions */}
                            {selectedNotifications.length > 0 && (
                                <div className="flex items-center space-x-2 bg-blue-50 px-3 py-2 rounded-lg">
                                    <span className="text-sm text-blue-600 font-medium">
                                        {selectedNotifications.length} seçili
                                    </span>
                                    <button
                                        onClick={handleBulkMarkAsRead}
                                        className="text-sm text-blue-600 hover:text-blue-800"
                                    >
                                        Okundu İşaretle
                                    </button>
                                    <button
                                        onClick={handleBulkDelete}
                                        className="text-sm text-red-600 hover:text-red-800"
                                    >
                                        Sil
                                    </button>
                                </div>
                            )}

                            {/* Quick actions */}
                            <div className="flex items-center space-x-2">
                                <button
                                    onClick={() => refetchNotifications()}
                                    disabled={notificationsLoading}
                                    className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
                                    title="Yenile (Ctrl+R)"
                                >
                                    <svg className={`w-5 h-5 ${notificationsLoading ? 'animate-spin' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                                    </svg>
                                </button>

                                <button
                                    onClick={handleMarkAllAsRead}
                                    disabled={totalUnread === 0 || isAnyMutationLoading}
                                    className="flex items-center space-x-2 px-3 py-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors disabled:opacity-50"
                                    title="Tümünü Okundu İşaretle (Ctrl+A)"
                                >
                                    <CheckIcon className="w-4 h-4" />
                                    <span className="text-sm">Tümünü Okundu İşaretle</span>
                                </button>

                                <button
                                    onClick={() => setShowPreferences(true)}
                                    className="flex items-center space-x-2 px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                                >
                                    <Cog8ToothIcon className="w-5 h-5 text-gray-600" />
                                    <span className="text-gray-700">Ayarlar</span>
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* Filter Tabs with counts */}
                    <div className="flex space-x-1 bg-gray-100 rounded-lg p-1">
                        {[
                            { key: 'all', label: 'Tümü', count: notificationsData?.totalElements || 0 },
                            { key: 'unread', label: 'Okunmamış', count: totalUnread },
                            { key: 'read', label: 'Okunmuş', count: totalRead },
                        ].map((tab) => (
                            <button
                                key={tab.key}
                                onClick={() => {
                                    setFilter(tab.key as any);
                                    setCurrentPage(0);
                                    setSelectedNotifications([]);
                                }}
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

                    {/* Bulk selection */}
                    {filteredNotifications.length > 0 && (
                        <div className="mt-4 flex items-center justify-between">
                            <label className="flex items-center space-x-2">
                                <input
                                    type="checkbox"
                                    checked={selectedNotifications.length === filteredNotifications.length}
                                    onChange={(e) => {
                                        if (e.target.checked) {
                                            setSelectedNotifications(filteredNotifications.map(n => n.id));
                                        } else {
                                            setSelectedNotifications([]);
                                        }
                                    }}
                                    className="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500"
                                />
                                <span className="text-sm text-gray-600">Tümünü Seç</span>
                            </label>

                            {filteredNotifications.length > 0 && (
                                <div className="flex items-center space-x-4">
                                    <button
                                        onClick={handleClearReadNotifications}
                                        className="text-sm text-gray-600 hover:text-red-600 transition-colors"
                                    >
                                        Okunmuşları Temizle
                                    </button>
                                    <button
                                        onClick={handleClearAllNotifications}
                                        className="text-sm text-gray-600 hover:text-red-600 transition-colors"
                                    >
                                        Tümünü Temizle
                                    </button>
                                </div>
                            )}
                        </div>
                    )}
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
                            <p className="text-red-600 text-sm mb-4">Lütfen daha sonra tekrar deneyin.</p>
                            <button
                                onClick={() => refetchNotifications()}
                                className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                            >
                                Tekrar Dene
                            </button>
                        </div>
                    ) : filteredNotifications.length === 0 ? (
                        // Empty state
                        <div className="bg-white/80 backdrop-blur-lg rounded-2xl shadow-lg border border-white/20 p-8">
                            <EmptyState filter={filter} />
                        </div>
                    ) : (
                        // Notifications with selection
                        filteredNotifications.map((notification) => (
                            <div key={notification.id} className="relative">
                                {/* Selection checkbox */}
                                <label className="absolute top-4 left-4 z-10">
                                    <input
                                        type="checkbox"
                                        checked={selectedNotifications.includes(notification.id)}
                                        onChange={(e) => {
                                            if (e.target.checked) {
                                                setSelectedNotifications(prev => [...prev, notification.id]);
                                            } else {
                                                setSelectedNotifications(prev => prev.filter(id => id !== notification.id));
                                            }
                                        }}
                                        className="w-4 h-4 text-blue-600 bg-white border-gray-300 rounded focus:ring-blue-500"
                                    />
                                </label>

                                {/* Notification card with enhanced props */}
                                <div className="pl-12">
                                    <NotificationCard
                                        notification={notification}
                                        onMarkAsRead={handleMarkAsRead}
                                        onDelete={handleDeleteNotification}
                                        onClick={handleNotificationClick}
                                        isLoading={
                                            markAsReadMutation.isPending ||
                                            deleteNotificationMutation.isPending
                                        }
                                    />
                                </div>
                            </div>
                        ))
                    )}

                    {/* Load more trigger for infinite scroll */}
                    {filteredNotifications.length > 0 && !notificationsData?.last && (
                        <div id="load-more-trigger" className="py-4 text-center">
                            <button
                                onClick={() => setCurrentPage(prev => prev + 1)}
                                disabled={notificationsLoading}
                                className="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors disabled:opacity-50"
                            >
                                {notificationsLoading ? 'Yükleniyor...' : 'Daha Fazla Göster'}
                            </button>
                        </div>
                    )}
                </div>

                {/* Pagination */}
                {notificationsData && notificationsData.totalPages > 1 && (
                    <div className="mt-8 flex justify-center">
                        <nav className="flex space-x-2">
                            <button
                                onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                                disabled={currentPage === 0}
                                className="px-3 py-2 rounded-lg text-sm font-medium bg-white text-gray-700 border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Önceki
                            </button>

                            {Array.from({ length: Math.min(5, notificationsData.totalPages) }, (_, i) => {
                                const page = i + Math.max(0, currentPage - 2);
                                if (page >= notificationsData.totalPages) return null;

                                return (
                                    <button
                                        key={page}
                                        onClick={() => setCurrentPage(page)}
                                        className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                                            currentPage === page
                                                ? 'bg-indigo-600 text-white'
                                                : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-300'
                                        }`}
                                    >
                                        {page + 1}
                                    </button>
                                );
                            })}

                            <button
                                onClick={() => setCurrentPage(prev => Math.min(notificationsData.totalPages - 1, prev + 1))}
                                disabled={currentPage === notificationsData.totalPages - 1}
                                className="px-3 py-2 rounded-lg text-sm font-medium bg-white text-gray-700 border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Sonraki
                            </button>
                        </nav>
                    </div>
                )}

                {/* Keyboard shortcuts info */}
                <div className="mt-8 bg-white/60 backdrop-blur-lg rounded-xl p-4 border border-white/20">
                    <details className="group">
                        <summary className="cursor-pointer text-sm font-medium text-gray-700 group-open:text-indigo-600">
                            Klavye Kısayolları
                        </summary>
                        <div className="mt-2 grid grid-cols-2 gap-2 text-xs text-gray-600">
                            <div><kbd className="bg-gray-100 px-1 rounded">Ctrl+A</kbd> Tümünü okundu işaretle</div>
                            <div><kbd className="bg-gray-100 px-1 rounded">Ctrl+R</kbd> Yenile</div>
                            <div><kbd className="bg-gray-100 px-1 rounded">Ctrl+D</kbd> Seçilenleri sil</div>
                            <div><kbd className="bg-gray-100 px-1 rounded">Esc</kbd> Seçimi temizle</div>
                        </div>
                    </details>
                </div>
            </div>

            {/* Enhanced Preferences Panel */}
            {showPreferences && (
                <NotificationPreferencesPanel
                    preferences={preferences}
                    notificationStats={notificationStats}
                    onUpdateEmail={handleUpdateEmailPreference}
                    onUpdateSms={handleUpdateSmsPreference}
                    onUpdatePush={handleUpdatePushPreference}
                    onClose={() => setShowPreferences(false)}
                    isLoading={preferencesLoading || statsLoading}
                />
            )}
        </div>
    );
}