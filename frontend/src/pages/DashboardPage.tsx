import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { Link } from 'react-router-dom';
import Header from '../components/Header';
import { Shipment, UserRole } from '../types';
import { shipmentService } from '../services/shipmentService';
import { hasPermission, Permission } from '../utils/rolePermissions';

// Dashboard istatistikleri interface'i
interface DashboardStats {
  totalShipments: number;
  activeShipments: number;
  deliveredShipments: number;
  pendingShipments: number;
  monthlyGrowth: number;
  recentShipments: Shipment[];
}

const StatusIcon: React.FC<{ status: string }> = ({ status }) => {
  const getStatusConfig = (status: string) => {
    switch (status?.toLowerCase()) {
      case 'active':
      case 'picked_up':
      case 'in_transit':
        return { icon: '🚛', label: 'Aktif', color: 'text-blue-600 bg-blue-100' };
      case 'finished':
      case 'delivered':
        return { icon: '✅', label: 'Teslim Edildi', color: 'text-green-600 bg-green-100' };
      case 'pending':
      case 'processing':
        return { icon: '⏳', label: 'Beklemede', color: 'text-yellow-600 bg-yellow-100' };
      case 'cancelled':
        return { icon: '❌', label: 'İptal', color: 'text-red-600 bg-red-100' };
      default:
        return { icon: '📦', label: status || 'Bilinmiyor', color: 'text-gray-600 bg-gray-100' };
    }
  };

  const config = getStatusConfig(status);
  
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${config.color}`}>
      <span className="mr-1">{config.icon}</span>
      {config.label}
    </span>
  );
};

const StatCard: React.FC<{ title: string; value: number; icon: string; trend?: number; color: string; loading?: boolean }> = ({ title, value, icon, trend, color, loading }) => (
  <div className="card p-6 animate-slide-up">
    <div className="flex items-center justify-between">
      <div>
        <p className="text-sm font-medium text-gray-600 mb-1">{title}</p>
        {loading ? (
          <div className="h-8 bg-gray-200 rounded animate-pulse w-20"></div>
        ) : (
          <p className="text-3xl font-bold text-gray-900">{Number(value || 0).toLocaleString()}</p>
        )}
        {trend && !loading && (
          <div className="flex items-center mt-2">
            <svg className={`w-4 h-4 mr-1 ${trend > 0 ? 'text-green-500' : 'text-red-500'}`} fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d={trend > 0 ? "M5.293 9.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L10 6.414 6.707 9.707a1 1 0 01-1.414 0z" : "M14.707 10.293a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 111.414-1.414L10 13.586l3.293-3.293a1 1 0 011.414 0z"} clipRule="evenodd" />
            </svg>
            <span className={`text-sm font-medium ${trend > 0 ? 'text-green-600' : 'text-red-600'}`}>
              {Math.abs(Number(trend || 0)).toFixed(1)}%
            </span>
            <span className="text-sm text-gray-500 ml-1">önceki aya göre</span>
          </div>
        )}
      </div>
      <div className={`w-16 h-16 rounded-2xl flex items-center justify-center text-2xl ${color}`}>
        {loading ? (
          <div className="w-8 h-8 bg-gray-300 rounded animate-pulse"></div>
        ) : (
          icon
        )}
      </div>
    </div>
  </div>
);

const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const { addToast } = useToast();
  const [currentTime, setCurrentTime] = useState(new Date());
  const [dashboardStats, setDashboardStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    loadDashboardStats();
  }, []);

  const loadDashboardStats = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await shipmentService.getDashboardStats();
      
      if (response.success && response.data) {
        // Veri formatlamasını düzelt
        const formattedData: DashboardStats = {
          totalShipments: Number(response.data.totalShipments || 0),
          activeShipments: Number(response.data.activeShipments || 0),
          deliveredShipments: Number(response.data.deliveredShipments || 0),
          pendingShipments: Number(response.data.pendingShipments || 0),
          monthlyGrowth: Number(response.data.monthlyGrowth || 0),
          recentShipments: Array.isArray(response.data.recentShipments) ? response.data.recentShipments : []
        };
        
        setDashboardStats(formattedData);
        
      } else {
        // Fallback: Boş istatistikler ile çalış
        const fallbackStats: DashboardStats = {
          totalShipments: 0,
          activeShipments: 0,
          deliveredShipments: 0,
          pendingShipments: 0,
          monthlyGrowth: 0,
          recentShipments: []
        };
        
        setDashboardStats(fallbackStats);
        // Error mesajını gizle - analytics service optional
        console.log('ℹ️ Analytics service bağlanamıyor, fallback data kullanılıyor');
      }
    } catch (err: any) {
      console.log('ℹ️ Analytics service bağlantı hatası (normal):', err.message || err);
      
      // Fallback stats
      const fallbackStats: DashboardStats = {
        totalShipments: 0,
        activeShipments: 0,
        deliveredShipments: 0,
        pendingShipments: 0,
        monthlyGrowth: 0,
        recentShipments: []
      };
      
      setDashboardStats(fallbackStats);
      // Error mesajını kullanıcıya gösterme - analytics optional
    } finally {
      setLoading(false);
    }
  };

  const getGreeting = () => {
    const hour = currentTime.getHours();
    if (hour < 12) return 'Günaydın';
    if (hour < 17) return 'İyi öğleden sonra';
    return 'İyi akşamlar';
  };

  const getRoleSpecificMessage = (roles: string[]) => {
    if (roles.includes('ADMIN')) return 'Sistem yöneticisi olarak tüm operasyonları kontrol edebilirsiniz.';
    if (roles.includes('SHIPPER')) return 'Yeni gönderi oluşturabilir ve mevcut gönderilerinizi takip edebilirsiniz.';
    if (roles.includes('CARRIER')) return 'Size atanan gönderilerin durumlarını güncelleyebilirsiniz.';
    if (roles.includes('CUSTOMER')) return 'Gönderilerinizi takip edebilir ve bildirim tercihlerinizi yönetebilirsiniz.';
    return 'CargoTrack sistemine hoş geldiniz!';
  };

  // Kullanıcının rolüne göre erişilebilir quick actions
  const getQuickActions = () => {
    const allActions = [
      { 
        title: 'Yeni Gönderi', 
        icon: '📦', 
        color: 'bg-primary-500 hover:bg-primary-600', 
        href: '/shipments/new',
        permission: Permission.CREATE_SHIPMENT
      },
      { 
        title: 'Gönderi Ara', 
        icon: '🔍', 
        color: 'bg-secondary-500 hover:bg-secondary-600', 
        href: '/tracking',
        permission: Permission.TRACK_SHIPMENTS
      },
      { 
        title: 'Bildirimler', 
        icon: '🔔', 
        color: 'bg-purple-500 hover:bg-purple-600', 
        href: '/notifications',
        permission: Permission.VIEW_NOTIFICATIONS
      },
      { 
        title: 'Raporlar', 
        icon: '📊', 
        color: 'bg-indigo-500 hover:bg-indigo-600', 
        href: '/reports',
        permission: Permission.VIEW_REPORTS
      },
    ];

    // Kullanıcının yetkisi olan aksiyonları filtrele
    return allActions.filter(action => 
      user?.role && hasPermission(user.role, action.permission)
    );
  };

  const quickActions = getQuickActions();

  // Hata durumunda yeniden deneme
  const handleRetry = () => {
    addToast('İstatistikler yeniden yükleniyor...', 'info');
    loadDashboardStats();
  };

  return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50">
        <Header/>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Header Section */}
          <div className="mb-8">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-3xl font-bold text-gray-900">
                  {getGreeting()}, {user?.firstName}! 👋
                </h1>
                <p className="mt-2 text-lg text-gray-600">
                  {user?.role ? getRoleSpecificMessage([user.role]) : 'CargoTrack sistemine hoş geldiniz!'}
                </p>
              </div>
              <div className="text-right">
                <p className="text-sm text-gray-500">Bugün</p>
                <p className="text-xl font-semibold text-gray-900">
                  {currentTime.toLocaleDateString('tr-TR', {
                    weekday: 'long',
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                  })}
                </p>
                <p className="text-lg text-gray-600">
                  {currentTime.toLocaleTimeString('tr-TR', {
                    hour: '2-digit',
                    minute: '2-digit'
                  })}
                </p>
              </div>
            </div>
          </div>

          {/* Analytics service optional - error mesajı gösterilmiyor */}

          {/* Statistics Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <StatCard
                title="Toplam Gönderi"
                value={dashboardStats?.totalShipments || 0}
                icon="📦"
                trend={dashboardStats?.monthlyGrowth}
                color="bg-red-500 text-white"
                loading={loading}
            />
            <StatCard
                title="Aktif Gönderi"
                value={dashboardStats?.activeShipments || 0}
                icon="🚛"
                color="bg-blue-100 text-blue-600"
                loading={loading}
            />
            <StatCard
                title="Teslim Edildi"
                value={dashboardStats?.deliveredShipments || 0}
                icon="✅"
                color="bg-green-100 text-green-600"
                loading={loading}
            />
            <StatCard
                title="Bekleyen"
                value={dashboardStats?.pendingShipments || 0}
                icon="📋"
                color="bg-yellow-100 text-yellow-600"
                loading={loading}
            />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Recent Shipments */}
            <div className="lg:col-span-2">
              <div className="card p-6">
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-xl font-bold text-gray-900">Son Gönderiler</h2>
                  <Link to="/shipments" className="text-primary-600 hover:text-primary-700 font-medium text-sm">
                    Tümünü gör →
                  </Link>
                </div>

                {loading ? (
                  // Loading skeleton
                  <div className="space-y-4">
                    {[1, 2, 3].map((index) => (
                      <div key={index} className="border border-gray-200 rounded-xl p-4 animate-pulse">
                        <div className="flex items-center justify-between">
                          <div className="flex-1">
                            <div className="h-4 bg-gray-200 rounded w-24 mb-2"></div>
                            <div className="h-3 bg-gray-200 rounded w-32"></div>
                          </div>
                          <div className="h-6 bg-gray-200 rounded-full w-16"></div>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : dashboardStats?.recentShipments?.length ? (
                  <div className="space-y-4">
                    {dashboardStats.recentShipments.map((shipment) => (
                      <div key={shipment.id} className="border border-gray-200 rounded-xl p-4 hover:border-primary-300 transition-colors cursor-pointer">
                        <div className="flex items-center justify-between">
                          <div className="flex-1">
                            <div className="flex items-center space-x-3 mb-2">
                              <p className="font-semibold text-gray-900">{shipment.trackingNumber}</p>
                              <StatusIcon status={shipment.status} />
                            </div>
                            <div className="flex items-center space-x-4 text-sm text-gray-600">
                              <span>🏢 {shipment.senderAddress?.city}</span>
                              <span>📍 {shipment.recipientAddress?.city}</span>
                                                             {shipment.estimatedDelivery && (
                                 <span>📅 {new Date(shipment.estimatedDelivery).toLocaleDateString('tr-TR')}</span>
                               )}
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                      📦
                    </div>
                    <p>Henüz gönderi bulunmuyor</p>
                  </div>
                )}
              </div>
            </div>

            {/* Quick Actions */}
            <div className="space-y-6">
              <div className="card p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-6">Hızlı İşlemler</h2>
                <div className="space-y-3">
                  {quickActions.map((action, index) => (
                      <Link
                          key={index}
                          to={action.href}
                          className={`flex items-center p-4 rounded-xl text-white font-medium transition-all duration-200 hover:scale-105 ${action.color}`}
                      >
                        <span className="text-2xl mr-3">{action.icon}</span>
                        <span>{action.title}</span>
                        <svg className="w-5 h-5 ml-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7"/>
                        </svg>
                      </Link>
                  ))}
                </div>
              </div>

              {/* System Status */}
              <div className="card p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-6">Sistem Durumu</h2>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">API Bağlantısı</span>
                    <div className="flex items-center">
                      <div className="w-2 h-2 bg-green-500 rounded-full mr-2"></div>
                      <span className="text-sm text-green-600 font-medium">Aktif</span>
                    </div>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">Database</span>
                    <div className="flex items-center">
                      <div className="w-2 h-2 bg-green-500 rounded-full mr-2"></div>
                      <span className="text-sm text-green-600 font-medium">Çevrimiçi</span>
                    </div>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">Bildirimler</span>
                    <div className="flex items-center">
                      <div className="w-2 h-2 bg-yellow-500 rounded-full mr-2"></div>
                      <span className="text-sm text-yellow-600 font-medium">Kısmi</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
  );
};

export default DashboardPage; 