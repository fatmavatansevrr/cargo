import React from 'react';
import { toast } from 'react-hot-toast';

/**
 * Custom Toast Hook
 * Daha güzel ve tutarlı toast mesajları için
 */
export const useToast = () => {

    const success = (message: string, options?: { duration?: number }) => {
        return toast.success(message, {
            duration: options?.duration || 3000,
            style: {
                background: '#f0fdf4',
                color: '#166534',
                border: '1px solid #bbf7d0',
                borderRadius: '12px',
                padding: '16px 20px',
                fontSize: '14px',
                fontWeight: '500',
                boxShadow: '0 10px 15px -3px rgba(34, 197, 94, 0.1), 0 4px 6px -2px rgba(34, 197, 94, 0.05)',
            },
            iconTheme: {
                primary: '#22c55e',
                secondary: '#f0fdf4',
            },
        });
    };

    const error = (message: string, options?: { duration?: number }) => {
        return toast.error(message, {
            duration: options?.duration || 5000,
            style: {
                background: '#fef2f2',
                color: '#991b1b',
                border: '1px solid #fecaca',
                borderRadius: '12px',
                padding: '16px 20px',
                fontSize: '14px',
                fontWeight: '500',
                boxShadow: '0 10px 15px -3px rgba(239, 68, 68, 0.1), 0 4px 6px -2px rgba(239, 68, 68, 0.05)',
            },
            iconTheme: {
                primary: '#ef4444',
                secondary: '#fef2f2',
            },
        });
    };

    const info = (message: string, options?: { duration?: number }) => {
        return toast(message, {
            duration: options?.duration || 4000,
            icon: '📢',
            style: {
                background: '#eff6ff',
                color: '#1e40af',
                border: '1px solid #dbeafe',
                borderRadius: '12px',
                padding: '16px 20px',
                fontSize: '14px',
                fontWeight: '500',
                boxShadow: '0 10px 15px -3px rgba(59, 130, 246, 0.1), 0 4px 6px -2px rgba(59, 130, 246, 0.05)',
            },
        });
    };

    const warning = (message: string, options?: { duration?: number }) => {
        return toast(message, {
            duration: options?.duration || 4000,
            icon: '⚠️',
            style: {
                background: '#fffbeb',
                color: '#92400e',
                border: '1px solid #fed7aa',
                borderRadius: '12px',
                padding: '16px 20px',
                fontSize: '14px',
                fontWeight: '500',
                boxShadow: '0 10px 15px -3px rgba(245, 158, 11, 0.1), 0 4px 6px -2px rgba(245, 158, 11, 0.05)',
            },
        });
    };

    const loading = (message: string) => {
        return toast.loading(message, {
            style: {
                background: '#f8fafc',
                color: '#475569',
                border: '1px solid #e2e8f0',
                borderRadius: '12px',
                padding: '16px 20px',
                fontSize: '14px',
                fontWeight: '500',
                boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
            },
        });
    };

    const promise = <T,>(
        promise: Promise<T>,
        msgs: {
            loading: string;
            success: string | ((data: T) => string);
            error: string | ((error: any) => string);
        }
    ) => {
        return toast.promise(promise, msgs, {
            style: {
                borderRadius: '12px',
                padding: '16px 20px',
                fontSize: '14px',
                fontWeight: '500',
            },
            success: {
                duration: 3000,
                style: {
                    background: '#f0fdf4',
                    color: '#166534',
                    border: '1px solid #bbf7d0',
                },
            },
            error: {
                duration: 5000,
                style: {
                    background: '#fef2f2',
                    color: '#991b1b',
                    border: '1px solid #fecaca',
                },
            },
            loading: {
                style: {
                    background: '#f8fafc',
                    color: '#475569',
                    border: '1px solid #e2e8f0',
                },
            },
        });
    };

    // Simplified notification function without custom JSX
    const notification = (
        title: string,
        message: string,
        type: 'success' | 'error' | 'info' | 'warning' = 'info'
    ) => {
        const icons = {
            success: '✅',
            error: '❌',
            info: '📢',
            warning: '⚠️'
        };

        const styles = {
            success: {
                background: '#f0fdf4',
                color: '#166534',
                border: '1px solid #bbf7d0',
            },
            error: {
                background: '#fef2f2',
                color: '#991b1b',
                border: '1px solid #fecaca',
            },
            info: {
                background: '#eff6ff',
                color: '#1e40af',
                border: '1px solid #dbeafe',
            },
            warning: {
                background: '#fffbeb',
                color: '#92400e',
                border: '1px solid #fed7aa',
            }
        };

        const fullMessage = `${title}\n${message}`;

        return toast(fullMessage, {
            icon: icons[type],
            duration: 5000,
            style: {
                ...styles[type],
                borderRadius: '12px',
                padding: '16px 20px',
                fontSize: '14px',
                fontWeight: '500',
                whiteSpace: 'pre-line',
                maxWidth: '400px',
                boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
            },
        });
    };

    // Bulk operations toast
    const bulk = (
        operation: string,
        count: number,
        type: 'success' | 'error' = 'success'
    ) => {
        const message = `${count} öğe ${operation}`;
        if (type === 'success') {
            return success(message);
        } else {
            return error(message);
        }
    };

    // Keyboard shortcut toast
    const shortcut = (action: string, shortcut: string) => {
        return info(`${action} (${shortcut})`, { duration: 2000 });
    };

    // Dismiss specific toast
    const dismiss = (toastId?: string) => {
        if (toastId) {
            toast.dismiss(toastId);
        } else {
            toast.dismiss();
        }
    };

    // Remove all toasts
    const removeAll = () => {
        toast.remove();
    };

    // Advanced notification with custom JSX (for action button etc.)
    const advancedNotification = (
        title: string,
        message: string,
        type: 'success' | 'error' | 'info' | 'warning' = 'info',
        options?: { duration?: number; action?: () => void; actionLabel?: string }
    ) => {
        const icons = {
            success: '✅',
            error: '❌',
            info: '📢',
            warning: '⚠️'
        };

        const styles = {
            success: {
                background: '#f0fdf4',
                color: '#166534',
                border: '1px solid #bbf7d0',
            },
            error: {
                background: '#fef2f2',
                color: '#991b1b',
                border: '1px solid #fecaca',
            },
            info: {
                background: '#eff6ff',
                color: '#1e40af',
                border: '1px solid #dbeafe',
            },
            warning: {
                background: '#fffbeb',
                color: '#92400e',
                border: '1px solid #fed7aa',
            }
        };

        // If action and label are provided, show button using custom JSX
        if (options?.action && options?.actionLabel) {
            return toast(
                <div style={{ whiteSpace: 'pre-line' }}>
            <strong style={{ fontSize: '16px', display: 'block', marginBottom: 4 }}>{title}</strong>
            <span style={{ fontSize: '14px' }}>{message}</span>
            <div>
            <button
                style={{
                marginTop: 12,
                    padding: '4px 12px',
                    borderRadius: 6,
                    border: 'none',
                    background: '#1e40af',
                    color: '#fff',
                    fontWeight: 500,
                    cursor: 'pointer'
            }}
            onClick={() => {
                options.action?.();
                toast.dismiss();
            }}
        >
            {options.actionLabel}
            </button>
            </div>
            </div>,
            {
                icon: icons[type],
                    duration: options.duration || 8000,
                style: {
            ...styles[type],
                    borderRadius: '12px',
                    padding: '16px 20px',
                    fontSize: '14px',
                    fontWeight: '500',
                    whiteSpace: 'pre-line',
                    maxWidth: '400px',
                    boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
            },
            }
        );
        }

        // No action, just info
        return toast(
            `🔹 ${title}\n${message}`,
            {
                icon: icons[type],
                duration: options?.duration || 5000,
                style: {
                    ...styles[type],
                    borderRadius: '12px',
                    padding: '16px 20px',
                    fontSize: '14px',
                    fontWeight: '500',
                    whiteSpace: 'pre-line',
                    maxWidth: '400px',
                    boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
                },
            }
        );
    };

    return {
        success,
        error,
        info,
        warning,
        loading,
        promise,
        notification,
        advancedNotification,
        bulk,
        shortcut,
        dismiss,
        removeAll,
        toast,
    };
};

export default useToast;
