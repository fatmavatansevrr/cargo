import React, { useEffect, useState } from 'react';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

interface ToastProps {
  message: string;
  type: ToastType;
  duration?: number;
  onClose: () => void;
}

const Toast: React.FC<ToastProps> = ({ message, type, duration = 3000, onClose }) => {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    // Component mount olduğunda animasyonu başlat
    setIsVisible(true);

    // Belirlenen süre sonra toast'ı kapat
    const timer = setTimeout(() => {
      setIsVisible(false);
      setTimeout(onClose, 300); // Animasyon tamamlanması için bekle
    }, duration);

    return () => clearTimeout(timer);
  }, [duration, onClose]);

  const getToastStyles = () => {
    const baseStyles = 'fixed top-4 right-4 z-50 max-w-sm w-full bg-white border-l-4 shadow-lg rounded-lg p-4 transform transition-all duration-300 ease-in-out';
    
    if (!isVisible) {
      return `${baseStyles} translate-x-full opacity-0`;
    }

    const typeStyles = {
      success: 'border-green-500',
      error: 'border-red-500', 
      warning: 'border-yellow-500',
      info: 'border-blue-500'
    };

    return `${baseStyles} translate-x-0 opacity-100 ${typeStyles[type]}`;
  };

  const getIcon = () => {
    const icons = {
      success: '✅',
      error: '❌',
      warning: '⚠️',
      info: 'ℹ️'
    };
    return icons[type];
  };

  const getTextColor = () => {
    const colors = {
      success: 'text-green-800',
      error: 'text-red-800',
      warning: 'text-yellow-800', 
      info: 'text-blue-800'
    };
    return colors[type];
  };

  const getBgColor = () => {
    const colors = {
      success: 'bg-green-50',
      error: 'bg-red-50',
      warning: 'bg-yellow-50',
      info: 'bg-blue-50'
    };
    return colors[type];
  };

  return (
    <div className={getToastStyles()}>
      <div className={`${getBgColor()} rounded p-3`}>
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <span className="text-xl">{getIcon()}</span>
          </div>
          <div className="ml-3 flex-1">
            <p className={`text-sm font-medium ${getTextColor()}`}>
              {message}
            </p>
          </div>
          <div className="ml-4 flex-shrink-0">
            <button
              onClick={() => {
                setIsVisible(false);
                setTimeout(onClose, 300);
              }}
              className={`inline-flex rounded-md p-1.5 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 ${getTextColor()}`}
            >
              <span className="sr-only">Kapat</span>
              <svg className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Toast; 