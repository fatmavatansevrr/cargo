import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import { Shipment, UserRole } from '../types';

// Mock data for demonstration
const mockStats = {
  totalShipments: 1247,
  activeShipments: 89,
  deliveredToday: 34,
  pendingPickups: 12
};

const mockRecentShipments = [
  {
    id: 'TRK001234',
    sender: 'Ahmet Yılmaz',
    receiver: 'Fatma Kaya',
    status: 'IN_TRANSIT',
    destination: 'İstanbul',
    estimatedDelivery: '2024-12-17',
    progress: 65
  },
  {
    id: 'TRK001235',
    sender: 'Mehmet Demir',
    receiver: 'Ayşe Özkan',
    status: 'OUT_FOR_DELIVERY',
    destination: 'Ankara',
    estimatedDelivery: '2024-12-16',
    progress: 90
  },
  {
    id: 'TRK001236',
    sender: 'Elif Çelik',
    receiver: 'Ali Yurt',
    status: 'PICKED_UP',
    destination: 'İzmir',
    estimatedDelivery: '2024-12-18',
    progress: 25
  }
];

const StatusIcon: React.FC<{ status: string }> = ({ status }) => {
  const getStatusConfig = (status: string) => {
    switch (status) {
      case 'IN_TRANSIT':
        return { icon: '🚛', color: 'bg-blue-100 text-blue-800', label: 'Yolda' };
      case 'OUT_FOR_DELIVERY':
        return { icon: '📦', color: 'bg-green-100 text-green-800', label: 'Dağıtımda' };
      case 'PICKED_UP':
        return { icon: '📋', color: 'bg-yellow-100 text-yellow-800', label: 'Alındı' };
      case 'DELIVERED':
        return { icon: '✅', color: 'bg-emerald-100 text-emerald-800', label: 'Teslim Edildi' };
      default:
        return { icon: '📋', color: 'bg-gray-100 text-gray-800', label: 'Bilinmiyor' };
    }
  };

  const config = getStatusConfig(status);
  
  return (
    <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium ${config.color}`}>
      <span className="mr-1">{config.icon}</span>
      {config.label}
    </span>
  );
};

const StatCard: React.FC<{ title: string; value: number; icon: string; trend?: number; color: string }> = ({ title, value, icon, trend, color }) => (
  <div className="card p-6 animate-slide-up">
    <div className="flex items-center justify-between">
      <div>
        <p className="text-sm font-medium text-gray-600 mb-1">{title}</p>
        <p className="text-3xl font-bold text-gray-900">{value.toLocaleString()}</p>
        {trend && (
          <div className="flex items-center mt-2">
            <svg className={`w-4 h-4 mr-1 ${trend > 0 ? 'text-green-500' : 'text-red-500'}`} fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d={trend > 0 ? "M5.293 9.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L10 6.414 6.707 9.707a1 1 0 01-1.414 0z" : "M14.707 10.293a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 111.414-1.414L10 13.586l3.293-3.293a1 1 0 011.414 0z"} clipRule="evenodd" />
            </svg>
            <span className={`text-sm font-medium ${trend > 0 ? 'text-green-600' : 'text-red-600'}`}>
              {Math.abs(trend)}%
            </span>
            <span className="text-sm text-gray-500 ml-1">önceki aya göre</span>
          </div>
        )}
      </div>
      <div className={`w-16 h-16 rounded-2xl flex items-center justify-center text-2xl ${color}`}>
        {icon}
      </div>
    </div>
  </div>
);

const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [currentTime, setCurrentTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

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

  const quickActions = [
    { title: 'Yeni Gönderi', icon: '📦', color: 'bg-primary-500 hover:bg-primary-600', href: '/shipments/new' },
    { title: 'Gönderi Ara', icon: '🔍', color: 'bg-secondary-500 hover:bg-secondary-600', href: '/tracking' },
    { title: 'Bildirimler', icon: '🔔', color: 'bg-purple-500 hover:bg-purple-600', href: '/notifications' },
    { title: 'Raporlar', icon: '📊', color: 'bg-indigo-500 hover:bg-indigo-600', href: '/reports' },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
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

        {/* Statistics Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <StatCard
            title="Toplam Gönderi"
            value={mockStats.totalShipments}
            icon="📦"
            trend={12}
            color="bg-red-500 text-white"
          />
          <StatCard
            title="Aktif Gönderi"
            value={mockStats.activeShipments}
            icon="🚛"
            trend={8}
            color="bg-blue-100 text-blue-600"
          />
          <StatCard
            title="Bugün Teslim"
            value={mockStats.deliveredToday}
            icon="✅"
            trend={-3}
            color="bg-green-100 text-green-600"
          />
          <StatCard
            title="Bekleyen Alım"
            value={mockStats.pendingPickups}
            icon="📋"
            color="bg-yellow-100 text-yellow-600"
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
              
              {mockRecentShipments.length > 0 ? (
                <div className="space-y-4">
                  {mockRecentShipments.map((shipment) => (
                    <div key={shipment.id} className="border border-gray-200 rounded-xl p-4 hover:shadow-medium transition-all duration-200">
                      <div className="flex items-center justify-between mb-3">
                        <div>
                          <p className="font-semibold text-gray-900">{shipment.id}</p>
                          <p className="text-sm text-gray-600">{shipment.sender} → {shipment.receiver}</p>
                        </div>
                        <StatusIcon status={shipment.status} />
                      </div>
                      
                      <div className="flex items-center justify-between mb-3">
                        <div>
                          <p className="text-sm text-gray-600">Varış: {shipment.destination}</p>
                          <p className="text-sm text-gray-600">Tahmini: {shipment.estimatedDelivery}</p>
                        </div>
                        <div className="text-right">
                          <p className="text-sm font-medium text-gray-900">{shipment.progress}%</p>
                          <p className="text-xs text-gray-500">tamamlandı</p>
                        </div>
                      </div>
                      
                      <div className="w-full bg-gray-200 rounded-full h-2">
                        <div 
                          className="bg-gradient-primary h-2 rounded-full transition-all duration-300"
                          style={{ width: `${shipment.progress}%` }}
                        ></div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-12">
                  <div className="w-24 h-24 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
                    <span className="text-4xl">📦</span>
                  </div>
                  <h3 className="text-lg font-medium text-gray-900 mb-2">Henüz gönderi yok</h3>
                  <p className="text-gray-600 mb-4">İlk gönderinizi oluşturmaya hazır mısınız?</p>
                  <button className="btn-primary">
                    Yeni Gönderi Oluştur
                  </button>
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
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
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