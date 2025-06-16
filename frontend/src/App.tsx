import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, useAuth } from './context/AuthContext';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import CreateShipmentPage from './pages/CreateShipmentPage';
import TrackShipmentPage from './pages/TrackShipmentPage';

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

// Protected Route component
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Layout>{children}</Layout>;
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
            
            {/* Protected Routes */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <DashboardPage />
                </ProtectedRoute>
              }
            />

            {/* Gönderi sayfaları */}
            <Route
              path="/tracking"
              element={
                <ProtectedRoute>
                  <TrackShipmentPage />
                </ProtectedRoute>
              }
            />

            <Route
              path="/tracking/:trackingNumber"
              element={
                <ProtectedRoute>
                  <TrackShipmentPage />
                </ProtectedRoute>
              }
            />

            <Route
              path="/shipments"
              element={
                <ProtectedRoute>
                  <PlaceholderPage title="Gönderilerim" />
                </ProtectedRoute>
              }
            />

            <Route
              path="/shipments/new"
              element={
                <ProtectedRoute>
                  <CreateShipmentPage />
                </ProtectedRoute>
              }
            />

            <Route
              path="/notifications"
              element={
                <ProtectedRoute>
                  <PlaceholderPage title="Bildirimler" />
                </ProtectedRoute>
              }
            />

            <Route
              path="/reports"
              element={
                <ProtectedRoute>
                  <PlaceholderPage title="Raporlar" />
                </ProtectedRoute>
              }
            />

            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <PlaceholderPage title="Profil Ayarları" />
                </ProtectedRoute>
              }
            />

            {/* Default redirect */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            
            {/* 404 Route */}  
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </Router>
      </AuthProvider>
    </QueryClientProvider>
  );
};

export default App;
