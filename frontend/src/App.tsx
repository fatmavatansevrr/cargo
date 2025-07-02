import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import Layout from './components/Layout';
import Header from './components/Header';
import ProtectedRoute from './components/ProtectedRoute';
import { Permission } from './utils/rolePermissions';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import CreateShipmentPage from './pages/CreateShipmentPage';
import TrackShipmentPage from './pages/TrackShipmentPage';
import NotificationsPage from './pages/NotificationsPage';
import ReportsPage from './pages/ReportsPage';
import CarrierTrackingPage from './pages/CarrierTrackingPage';

// React Query client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 dakika
    },
  },
});

// Loading spinner component
const LoadingSpinner: React.FC = () => (
  <div className="flex justify-center items-center min-h-screen bg-gray-50">
    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500"></div>
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

// Public Route component (sadece giriş yapmamış kullanıcılar için)
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

// Placeholder page component
const PlaceholderPage: React.FC<{ title: string; description?: string }> = ({ title, description = "Yakında gelecek..." }) => (
  <div className="min-h-screen bg-gray-50">
    <Header />
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="text-center">
        <h1 className="text-3xl font-bold text-gray-900 mb-4">{title}</h1>
        <p className="text-lg text-gray-600">{description}</p>
        <div className="mt-8">
          <div className="w-24 h-24 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
            <span className="text-4xl">🚧</span>
          </div>
          <p className="text-gray-500">Bu sayfa geliştirme aşamasında</p>
        </div>
      </div>
    </div>
  </div>
);

// 404 Page component
const NotFoundPage: React.FC = () => (
  <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
    <div className="text-center">
      <div className="w-32 h-32 mx-auto mb-6 bg-gray-100 rounded-full flex items-center justify-center">
        <span className="text-6xl">😕</span>
      </div>
      <h1 className="text-4xl font-bold text-gray-900 mb-4">404 - Sayfa Bulunamadı</h1>
      <p className="text-lg text-gray-600 mb-8">Aradığınız sayfa mevcut değil.</p>
      <Navigate to="/dashboard" />
    </div>
  </div>
);

// Ana App component
const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <Router>
            <Routes>
              {/* Public Routes */}
              <Route
                path="/login"
                element={
                  <PublicRoute>
                    <LoginPage />
                  </PublicRoute>
                }
              />
              
              <Route
                path="/register"
                element={
                  <PublicRoute>
                    <RegisterPage />
                  </PublicRoute>
                }
              />
              
              {/* Protected Routes with Role-based Access Control */}
              
              {/* Dashboard - Herkes erişebilir */}
              <Route
                path="/dashboard"
                element={
                  <SimpleProtectedRoute>
                    <DashboardPage />
                  </SimpleProtectedRoute>
                }
              />

              {/* Gönderi Takibi - Herkes erişebilir */}
              <Route
                path="/tracking"
                element={
                  <ProtectedRoute requiredPermissions={[Permission.TRACK_SHIPMENTS]}>
                    <TrackShipmentPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/tracking/:trackingNumber"
                element={
                  <ProtectedRoute requiredPermissions={[Permission.TRACK_SHIPMENTS]}>
                    <TrackShipmentPage />
                  </ProtectedRoute>
                }
              />

              {/* Gönderiler Listesi - Herkes erişebilir */}
              <Route
                path="/shipments"
                element={
                  <SimpleProtectedRoute>
                    <PlaceholderPage title="Gönderilerim" />
                  </SimpleProtectedRoute>
                }
              />

              {/* Yeni Gönderi Oluşturma - Sadece ADMIN ve SHIPPER */}
              <Route
                path="/shipments/new"
                element={
                  <ProtectedRoute requiredPermissions={[Permission.CREATE_SHIPMENT]}>
                    <CreateShipmentPage />
                  </ProtectedRoute>
                }
              />

              {/* Bildirimler - Herkes kendi bildirimlerini görür */}
              <Route
                path="/notifications"
                element={
                  <ProtectedRoute requiredPermissions={[Permission.VIEW_NOTIFICATIONS]}>
                    <NotificationsPage />
                  </ProtectedRoute>
                }
              />

              {/* Carrier Tracking Page - Sadece CARRIER rolü */}
              <Route
                path="/carrier/tracking"
                element={
                  <ProtectedRoute requiredPermissions={[Permission.CARRIER_TRACKING_MANAGEMENT]}>
                    <CarrierTrackingPage />
                  </ProtectedRoute>
                }
              />

              {/* Raporlar - Sadece ADMIN ve SHIPPER */}
              <Route
                path="/reports"
                element={
                  <ProtectedRoute requiredPermissions={[Permission.VIEW_REPORTS]}>
                    <ReportsPage />
                  </ProtectedRoute>
                }
              />

              {/* Kullanıcı Yönetimi - Sadece ADMIN (henüz sayfa yok) */}
              <Route
                path="/admin/users"
                element={
                  <ProtectedRoute requiredPermissions={[Permission.MANAGE_USERS]}>
                    <PlaceholderPage title="Kullanıcı Yönetimi" description="Admin paneli - kullanıcı yönetimi" />
                  </ProtectedRoute>
                }
              />

              {/* Profil - Herkes kendi profilini görür */}
              <Route
                path="/profile"
                element={
                  <SimpleProtectedRoute>
                    <PlaceholderPage title="Profil Ayarları" />
                  </SimpleProtectedRoute>
                }
              />

              {/* Default redirect */}
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              
              {/* 404 Route */}  
              <Route path="*" element={<NotFoundPage />} />
            </Routes>
          </Router>
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
};

export default App;
