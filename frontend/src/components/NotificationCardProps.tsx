import React, { useState } from 'react';
import {
    CheckCircleIcon,
    XCircleIcon,
    ClockIcon,
    TrashIcon,
    EyeIcon,
    EyeSlashIcon,
    ArrowTopRightOnSquareIcon
} from '@heroicons/react/24/outline';
import {
    notificationService,
    Notification,
    NotificationStatus,
    NotificationChannel,
    NotificationType
} from "../services/notificationService";

interface NotificationCardProps {
    notification: Notification;
    onMarkAsRead: (notificationId: string) => void;
    onDelete: (notificationId: string) => void;
    onClick: (notification: Notification) => void;
    isLoading?: boolean;
}

const NotificationCard: React.FC<NotificationCardProps> = ({
                                                               notification,
                                                               onMarkAsRead,
                                                               onDelete,
                                                               onClick,
                                                               isLoading = false
                                                           }) => {
    const [isHovered, setIsHovered] = useState(false);
    const [actionLoading, setActionLoading] = useState<string | null>(null);

    const icon = notificationService.getNotificationIcon(notification.type);
    const bgColor = notificationService.getNotificationColor(notification.type);
    const isUnread = notification.status === NotificationStatus.UNREAD;
    const typeLabel = notificationService.getNotificationTypeLabel(notification.type);
    const channelLabel = notificationService.getNotificationChannelLabel(notification.channel);

    const handleMarkAsRead = async (e: React.MouseEvent) => {
        e.stopPropagation();
        setActionLoading('read');
        try {
            await onMarkAsRead(notification.id);
        } finally {
            setActionLoading(null);
        }
    };

    const handleDelete = async (e: React.MouseEvent) => {
        e.stopPropagation();
        setActionLoading('delete');
        try {
            await onDelete(notification.id);
        } finally {
            setActionLoading(null);
        }
    };

    const handleClick = () => {
        onClick(notification);
    };

    return (
        <div
            className={`
                relative bg-white/80 backdrop-blur-lg rounded-xl p-6 border border-gray-200/50 
                shadow-sm hover:shadow-md transition-all duration-300 cursor-pointer
                ${isUnread ? 'ring-2 ring-blue-500/20 bg-blue-50/30' : ''}
                ${isLoading ? 'opacity-50' : ''}
            `}
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
            onClick={handleClick}
        >
            {/* Unread indicator */}
            {isUnread && (
                <div className="absolute top-4 right-4 w-3 h-3 bg-blue-500 rounded-full animate-pulse"></div>
            )}

            <div className="flex items-start space-x-4">
                {/* Icon */}
                <div className={`flex-shrink-0 w-12 h-12 ${bgColor} rounded-xl flex items-center justify-center text-white text-lg shadow-md`}>
                    {icon}
                </div>

                <div className="flex-1 min-w-0">
                    {/* Header */}
                    <div className="flex items-start justify-between">
                        <div className="flex-1">
                            <h3 className={`text-base font-semibold truncate ${isUnread ? 'text-gray-900' : 'text-gray-600'}`}>
                                {notification.title}
                            </h3>
                            <div className="flex items-center space-x-2 mt-1">
                                <span className="text-xs text-gray-500">{typeLabel}</span>
                                <span className="text-xs text-gray-300">•</span>
                                <span className="text-xs text-gray-500">
                                    {notificationService.formatDate(notification.createdAt)}
                                </span>
                            </div>
                        </div>

                        {/* Action buttons */}
                        <div className={`flex items-center space-x-2 transition-opacity ${isHovered ? 'opacity-100' : 'opacity-0'}`}>
                            {isUnread && (
                                <button
                                    onClick={handleMarkAsRead}
                                    disabled={actionLoading === 'read'}
                                    className="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
                                    title="Okundu olarak işaretle"
                                >
                                    {actionLoading === 'read' ? (
                                        <div className="w-4 h-4 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                                    ) : (
                                        <EyeIcon className="w-4 h-4" />
                                    )}
                                </button>
                            )}

                            <button
                                onClick={handleDelete}
                                disabled={actionLoading === 'delete'}
                                className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-md transition-colors"
                                title="Sil"
                            >
                                {actionLoading === 'delete' ? (
                                    <div className="w-4 h-4 border-2 border-red-600 border-t-transparent rounded-full animate-spin"></div>
                                ) : (
                                    <TrashIcon className="w-4 h-4" />
                                )}
                            </button>
                        </div>
                    </div>

                    {/* Message */}
                    <p className={`mt-3 text-sm leading-relaxed ${isUnread ? 'text-gray-700' : 'text-gray-500'}`}>
                        {notification.message}
                    </p>

                    {/* Footer */}
                    <div className="mt-4 flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                            {/* Channel badge */}
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                notification.channel === NotificationChannel.EMAIL ? 'bg-blue-100 text-blue-800' :
                                    notification.channel === NotificationChannel.SMS ? 'bg-green-100 text-green-800' :
                                        notification.channel === NotificationChannel.PUSH_NOTIFICATION ? 'bg-purple-100 text-purple-800' :
                                            'bg-indigo-100 text-indigo-800'
                            }`}>
                                {channelLabel}
                            </span>

                            {/* Tracking number */}
                            {notification.trackingNumber && (
                                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                                    #{notification.trackingNumber}
                                </span>
                            )}
                        </div>

                        {/* Status indicator */}
                        <div className="flex items-center space-x-2">
                            {notification.status === NotificationStatus.READ ? (
                                <div className="flex items-center space-x-1 text-green-600">
                                    <CheckCircleIcon className="w-4 h-4" />
                                    <span className="text-xs">Okundu</span>
                                </div>
                            ) : (
                                <div className="flex items-center space-x-1 text-blue-600">
                                    <ClockIcon className="w-4 h-4" />
                                    <span className="text-xs">Okunmadı</span>
                                </div>
                            )}

                            {/* External link indicator */}
                            {notification.trackingNumber && (
                                <ArrowTopRightOnSquareIcon className="w-4 h-4 text-gray-400" />
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Loading overlay */}
            {isLoading && (
                <div className="absolute inset-0 bg-white/50 backdrop-blur-sm rounded-xl flex items-center justify-center">
                    <div className="w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                </div>
            )}
        </div>
    );
};

export default NotificationCard;