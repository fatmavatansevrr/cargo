import React, { createContext, useContext, useState, useCallback } from 'react';
import Toast, { ToastType } from '../components/Toast';

interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration?: number;
}

interface ToastContextType {
  toasts: Toast[];
  addToast: (message: string, type: Toast['type'], duration?: number) => void;
  removeToast: (id: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

let toastIdCounter = 0;
const recentMessages = new Set<string>();

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const generateToastId = () => {
    toastIdCounter += 1;
    return `toast-${toastIdCounter}-${Date.now()}`;
  };

  const cleanOldMessages = () => {
    // Her 5 saniyede bir eski mesajları temizle
    setTimeout(() => {
      recentMessages.clear();
    }, 5000);
  };

  const addToast = useCallback((message: string, type: Toast['type'], duration = 5000) => {
    // Dublike mesajları engelle
    const messageKey = `${type}-${message}`;
    if (recentMessages.has(messageKey)) {
      return;
    }
    
    recentMessages.add(messageKey);
    cleanOldMessages();

    const id = generateToastId();
    const newToast: Toast = { id, message, type, duration };

    setToasts(prev => {
      // Maksimum 3 toast göster
      const filtered = prev.slice(-2);
      return [...filtered, newToast];
    });

    // Otomatik kaldırma
    if (duration > 0) {
      setTimeout(() => {
        removeToast(id);
      }, duration);
    }
  }, []);

  const removeToast = useCallback((id: string) => {
    setToasts(prev => prev.filter(toast => toast.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ toasts, addToast, removeToast }}>
      {children}
      {/* Toast'ları render et */}
      <div className="toast-container">
        {toasts.map((toast, index) => (
          <div 
            key={toast.id} 
            style={{ 
              top: `${1 + index * 5}rem`, // Çoklu toast'lar için konum ayarı
              zIndex: 1000 + index 
            }}
          >
            <Toast
              message={toast.message}
              type={toast.type as ToastType}
              duration={toast.duration}
              onClose={() => removeToast(toast.id)}
            />
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = (): ToastContextType => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}; 