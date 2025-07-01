import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { hasPermission, Permission, getAccessDeniedMessage } from '../utils/rolePermissions';

// Loading Spinner Component
const LoadingSpinner: React.FC = () => (
  <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-purple-50">
    <div className="flex flex-col items-center space-y-4">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      <p className="text-gray-600 font-medium">Yükleniyor...</p>
    </div>
  </div>
);

// Access Denied Component
const AccessDenied: React.FC<{ message: string; onRetry?: () => void }> = ({ message, onRetry }) => (
  <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-red-50 via-white to-orange-50">
    <div className="max-w-md mx-auto text-center p-8">
      <div className="text-6xl mb-4">🚫</div>
      <h2 className="text-2xl font-bold text-gray-900 mb-4">Erişim Engellendi</h2>
      <p className="text-gray-600 mb-6">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="bg-indigo-600 text-white px-6 py-2 rounded-lg hover:bg-indigo-700 transition-colors"
        >
          Yeniden Dene
        </button>
      )}
    </div>
  </div>
);

// Simple Protected Route component (sadece giriş kontrolü için)
const SimpleProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

// Advanced Protected Route component (role ve permission kontrolleri ile)
interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredPermissions?: Permission[];
  fallbackPath?: string;
  simpleAuth?: boolean; // Sadece giriş kontrolü için
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredPermissions = [],
  fallbackPath = "/dashboard",
  simpleAuth = false
}) => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const { addToast } = useToast();
  const [accessDeniedCount, setAccessDeniedCount] = useState(0);
  const [showAccessDenied, setShowAccessDenied] = useState(false);

  // Permission kontrolü - useEffect'i her zaman çağır
  useEffect(() => {
    // Sadece gerekli durumda permission kontrolü yap
    if (requiredPermissions.length > 0 && user && isAuthenticated && !isLoading && !simpleAuth) {
      const hasAccess = requiredPermissions.some(permission => 
        hasPermission(user.role, permission)
      );

      if (!hasAccess) {
        setAccessDeniedCount(prev => prev + 1);
        
        // İlk 3 deneme için toast göster
        if (accessDeniedCount < 3) {
          const firstPermission = requiredPermissions[0];
          const message = getAccessDeniedMessage(firstPermission);
          addToast(message, 'error', 5000);
          setShowAccessDenied(true);
        } else {
          // 3 denemeden sonra otomatik logout
          addToast('Yetkisiz erişim denemesi nedeniyle oturumunuz sonlandırılıyor.', 'warning');
          setTimeout(() => {
            localStorage.removeItem('token');
            window.location.href = '/login';
          }, 2000);
        }
      } else {
        setShowAccessDenied(false);
        setAccessDeniedCount(0);
      }
    }
  }, [user, requiredPermissions, accessDeniedCount, addToast, isAuthenticated, isLoading, simpleAuth]);

  // Basit auth kontrolü için
  if (simpleAuth) {
    return <SimpleProtectedRoute>{children}</SimpleProtectedRoute>;
  }

  // Loading state
  if (isLoading) {
    return <LoadingSpinner />;
  }

  // Auth kontrolü
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  // Access denied durumunda
  if (showAccessDenied) {
    const firstPermission = requiredPermissions[0];
    const message = getAccessDeniedMessage(firstPermission);
    
    return (
      <AccessDenied 
        message={message}
        onRetry={() => {
          setShowAccessDenied(false);
          setAccessDeniedCount(0);
        }}
      />
    );
  }

  // Permission kontrolü geçilirse
  if (requiredPermissions.length > 0 && user) {
    const hasAccess = requiredPermissions.some(permission => 
      hasPermission(user.role, permission)
    );

    if (!hasAccess) {
      return <Navigate to={fallbackPath} replace />;
    }
  }

  return <>{children}</>;
};

export default ProtectedRoute; 