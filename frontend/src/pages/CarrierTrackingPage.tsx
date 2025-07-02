import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Shipment } from '../types';
import { useToast } from '../context/ToastContext';
import Layout from '../components/Layout';

// Backend TrackingState enum'ı ile uyumlu - Kurye bunu günceller
enum TrackingState {
  CREATED = 'CREATED',
  PICKED_UP = 'PICKED_UP', 
  IN_TRANSIT = 'IN_TRANSIT',
  AT_SORTING_FACILITY = 'AT_SORTING_FACILITY',
  OUT_FOR_DELIVERY = 'OUT_FOR_DELIVERY',
  DELIVERED = 'DELIVERED',
  DELIVERY_FAILED = 'DELIVERY_FAILED',
  RETURNED_TO_SENDER = 'RETURNED_TO_SENDER',
  CANCELLED = 'CANCELLED'
}

// TrackingState display mapping - backend'teki description'larla uyumlu
const trackingStatusDisplayMap: Record<TrackingState, { label: string; color: string; bgColor: string }> = {
  [TrackingState.CREATED]: { label: 'Oluşturuldu', color: 'text-gray-700', bgColor: 'bg-gray-100' },
  [TrackingState.PICKED_UP]: { label: 'Kargoya Verildi', color: 'text-blue-700', bgColor: 'bg-blue-100' },
  [TrackingState.IN_TRANSIT]: { label: 'Yolda', color: 'text-blue-700', bgColor: 'bg-blue-100' },
  [TrackingState.AT_SORTING_FACILITY]: { label: 'Transfer Merkezinde', color: 'text-purple-700', bgColor: 'bg-purple-100' },
  [TrackingState.OUT_FOR_DELIVERY]: { label: 'Dağıtımda', color: 'text-orange-700', bgColor: 'bg-orange-100' },
  [TrackingState.DELIVERED]: { label: 'Teslim Edildi', color: 'text-green-700', bgColor: 'bg-green-100' },
  [TrackingState.DELIVERY_FAILED]: { label: 'Teslim Edilemedi', color: 'text-red-700', bgColor: 'bg-red-100' },
  [TrackingState.RETURNED_TO_SENDER]: { label: 'Gönderene İade', color: 'text-amber-700', bgColor: 'bg-amber-100' },
  [TrackingState.CANCELLED]: { label: 'İptal Edildi', color: 'text-gray-700', bgColor: 'bg-gray-100' }
};

interface CarrierShipment extends Shipment {
  currentLocation?: string;
  lastUpdate?: string;
  carrierId: string;
  trackingStatus: TrackingState; // Ayrı tracking durumu
}

const CarrierTrackingPage: React.FC = () => {
  const { user } = useAuth();
  const { addToast } = useToast();
  
  const [shipments, setShipments] = useState<CarrierShipment[]>([]);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState<string | null>(null);
  const [filter, setFilter] = useState<TrackingState | 'ALL'>('ALL');
  const [searchTerm, setSearchTerm] = useState('');

  // Mock data - gerçek API çağrısı ile değiştirilecek
  useEffect(() => {
    const fetchCarrierShipments = async () => {
      setLoading(true);
      try {
        // Gerçek API çağrısı
        const response = await fetch('http://localhost:8080/api/shipments/carrier/assigned', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        if (response.ok) {
          const shipmentData = await response.json();
          console.log('🔄 Backend\'den gelen gönderi verisi:', shipmentData);

          // Backend verilerini CarrierShipment formatına dönüştür ve tracking durumunu al
          const carrierShipments: CarrierShipment[] = await Promise.all(
            shipmentData.map(async (shipment: any) => {
              // Her shipment için tracking durumunu al
              let trackingStatus = TrackingState.IN_TRANSIT; // varsayılan
              try {
                const trackingResponse = await fetch(`http://localhost:8083/api/tracking/${shipment.trackingNumber}`);
                if (trackingResponse.ok) {
                  const trackingData = await trackingResponse.json();
                  trackingStatus = trackingData.status || TrackingState.IN_TRANSIT;
                  console.log(`📍 ${shipment.trackingNumber} tracking durumu:`, trackingStatus);
                }
              } catch (error) {
                console.warn(`⚠️ ${shipment.trackingNumber} tracking durumu alınamadı:`, error);
              }

              return {
                id: shipment.id,
                trackingNumber: shipment.trackingNumber,
                senderCustomerId: shipment.senderCustomerId?.toString() || 'unknown',
                recipientName: shipment.recipientAddress?.street || 'Bilinmeyen Alıcı',
                recipientPhone: shipment.recipientAddress?.phone || '+90532000000',
                recipientEmail: shipment.recipientAddress?.email || 'recipient@example.com',
                senderAddress: {
                  street: shipment.senderAddress?.street || '',
                  city: shipment.senderAddress?.city || '',
                  state: shipment.senderAddress?.state || '',
                  postalCode: shipment.senderAddress?.postalCode || '',
                  country: shipment.senderAddress?.country || 'Türkiye'
                },
                recipientAddress: {
                  street: shipment.recipientAddress?.street || '',
                  city: shipment.recipientAddress?.city || '',
                  state: shipment.recipientAddress?.state || '',
                  postalCode: shipment.recipientAddress?.postalCode || '',
                  country: shipment.recipientAddress?.country || 'Türkiye'
                },
                packageDetails: {
                  weight: shipment.packageInfo?.weight || 1.0,
                  dimensions: {
                    length: shipment.packageInfo?.length || 10,
                    width: shipment.packageInfo?.width || 10,
                    height: shipment.packageInfo?.height || 10
                  },
                  contentType: shipment.packageInfo?.contentType || 'Genel',
                  isFragile: shipment.packageInfo?.isFragile || false,
                  isLiquid: shipment.packageInfo?.isLiquid || false
                },
                serviceType: shipment.serviceType?.toLowerCase() || 'standard',
                status: shipment.status || 'active',
                trackingStatus: trackingStatus, // Gerçek tracking durumu
                estimatedDelivery: shipment.estimatedDeliveryDate || new Date().toISOString(),
                createdAt: shipment.createdAt || new Date().toISOString(),
                updatedAt: shipment.updatedAt || new Date().toISOString(),
                carrierId: user?.id || 'carrier-1',
                currentLocation: 'Bilinmeyen Konum',
                lastUpdate: shipment.updatedAt || new Date().toISOString()
              };
            })
          );

          console.log('✅ Dönüştürülmüş gönderi verisi:', carrierShipments);
          setShipments(carrierShipments);
        } else {
          throw new Error(`API hatası: ${response.status}`);
        }
      } catch (error) {
        console.error('❌ Gönderi yükleme hatası:', error);
        addToast('Kargolar yüklenirken hata oluştu', 'error');
        // Hata durumunda boş array set et
        setShipments([]);
      } finally {
        setLoading(false);
      }
    };

    fetchCarrierShipments();
  }, [user?.id, addToast]);

  // Status update fonksiyonu
  const handleStatusUpdate = async (trackingNumber: string, newStatus: TrackingState) => {
    setUpdating(trackingNumber);
    
    try {
      // Backend tracking service API çağrısı
      const response = await fetch(`http://localhost:8083/api/tracking/${trackingNumber}/status?newState=${newStatus}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        // Update local state
        setShipments(prev => prev.map(shipment => 
          shipment.trackingNumber === trackingNumber 
            ? { 
                ...shipment, 
                trackingStatus: newStatus,
                lastUpdate: new Date().toISOString(),
                updatedAt: new Date().toISOString()
              }
            : shipment
        ));
        
        addToast('Kargo takip durumu başarıyla güncellendi', 'success');
      } else {
        throw new Error('Status update failed');
      }
    } catch (error) {
      console.error('Tracking status güncelleme hatası:', error);
      addToast('Takip durumu güncellenirken hata oluştu', 'error');
    } finally {
      setUpdating(null);
    }
  };

  // Filtreleme fonksiyonu - trackingStatus'a göre
  const filteredShipments = shipments.filter(shipment => {
    const matchesFilter = filter === 'ALL' || shipment.trackingStatus === filter;
    const matchesSearch = searchTerm === '' || 
      shipment.trackingNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      shipment.recipientName.toLowerCase().includes(searchTerm.toLowerCase());
    
    return matchesFilter && matchesSearch;
  });

  // Status değiştirebilir mi kontrolü - trackingStatus'a göre
  const canUpdateStatus = (currentTrackingStatus: TrackingState): boolean => {
    const nonUpdatableStatuses = [TrackingState.DELIVERED, TrackingState.CANCELLED];
    return !nonUpdatableStatuses.includes(currentTrackingStatus);
  };

  // Format date
  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString('tr-TR', {
      day: '2-digit',
      month: '2-digit', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex items-center justify-center min-h-[50vh]">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500 mx-auto"></div>
            <p className="mt-4 text-gray-600">Kargolar yükleniyor...</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                🚛 Kargo Durum Güncelleme
              </h1>
              <p className="text-gray-600 mt-1">
                Size atanmış kargoların mevcut durumunu buradan güncelleyebilirsiniz.
              </p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="bg-orange-100 px-4 py-2 rounded-lg">
                <span className="text-orange-800 font-semibold">
                  {filteredShipments.length} Kargo
                </span>
              </div>
              <input
                type="text"
                placeholder="Takip no veya alıcı adı ara..."
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>
        </div>

        {/* Test Butonları - Sadece geliştirme için, sonra kaldırılacak */}
        {process.env.NODE_ENV === 'development' && (
          <div className="flex space-x-4 my-4">
            
          </div>
        )}

        {/* Filtreleme */}
        <div className="mb-6 flex items-center justify-between">
          <div className="flex space-x-2 bg-gray-100 p-1 rounded-lg">
            <button
              onClick={() => setFilter('ALL')}
              className={`px-3 py-1 rounded-lg text-sm ${
                filter === 'ALL' ? 'bg-primary-500 text-white' : 'text-gray-700'
              }`}
            >
              Tümü
            </button>
            {Object.entries(trackingStatusDisplayMap).map(([key, value]) => (
              <button
                key={key}
                onClick={() => setFilter(key as TrackingState)}
                className={`px-3 py-1 rounded-lg text-sm ${
                  filter === key ? 'bg-primary-500 text-white' : 'text-gray-700'
                }`}
              >
                {value.label}
              </button>
            ))}
          </div>
        </div>

        {/* Shipments List */}
        <div className="space-y-4">
          {filteredShipments.length === 0 ? (
            <div className="bg-white rounded-lg shadow-sm p-8 text-center">
              <div className="text-gray-400 text-6xl mb-4">📦</div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                Kargo bulunamadı
              </h3>
              <p className="text-gray-600">
                {searchTerm || filter !== 'ALL' 
                  ? 'Arama kriterlerinize uygun kargo bulunamadı.'
                  : 'Henüz size atanmış kargo bulunmuyor.'}
              </p>
            </div>
          ) : (
            filteredShipments.map((shipment) => (
              <div key={shipment.id} className="bg-white rounded-lg shadow-sm p-6">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                  {/* Shipment Info */}
                  <div className="lg:col-span-2">
                    <div className="flex items-center justify-between mb-4">
                      <div>
                        <h3 className="text-lg font-semibold text-gray-900">
                          {shipment.trackingNumber}
                        </h3>
                        <p className="text-gray-600">
                          {shipment.recipientName} • {shipment.recipientPhone}
                        </p>
                      </div>
                      <div className={`px-3 py-1 rounded-full text-sm font-medium ${
                        trackingStatusDisplayMap[shipment.trackingStatus]?.bgColor || 'bg-gray-100'
                      } ${trackingStatusDisplayMap[shipment.trackingStatus]?.color || 'text-gray-700'}`}>
                        {trackingStatusDisplayMap[shipment.trackingStatus]?.label || shipment.trackingStatus}
                      </div>
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                      <div>
                        <p className="text-gray-500">Gönderi Adresi:</p>
                        <p className="text-gray-900">
                          {shipment.senderAddress.city}, {shipment.senderAddress.state}
                        </p>
                      </div>
                      <div>
                        <p className="text-gray-500">Teslimat Adresi:</p>
                        <p className="text-gray-900">
                          {shipment.recipientAddress.city}, {shipment.recipientAddress.state}
                        </p>
                      </div>
                      <div>
                        <p className="text-gray-500">Paket Detayı:</p>
                        <p className="text-gray-900">
                          {shipment.packageDetails.weight}kg • {shipment.packageDetails.contentType}
                        </p>
                      </div>
                      <div>
                        <p className="text-gray-500">Son Güncelleme:</p>
                        <p className="text-gray-900">
                          {shipment.lastUpdate ? formatDate(shipment.lastUpdate) : 'Bilinmiyor'}
                        </p>
                      </div>
                    </div>

                    {shipment.currentLocation && (
                      <div className="mt-4 p-3 bg-blue-50 rounded-lg">
                        <p className="text-sm text-blue-700">
                          📍 Mevcut Konum: {shipment.currentLocation}
                        </p>
                      </div>
                    )}
                  </div>

                  {/* Status Update */}
                  <div className="border-l-0 lg:border-l border-gray-200 lg:pl-6">
                    <h4 className="font-medium text-gray-900 mb-3">Takip Durumu Güncelle</h4>
                    
                    {canUpdateStatus(shipment.trackingStatus) ? (
                      <div className="space-y-3">
                        {Object.entries(TrackingState).map(([key, value]) => (
                          <button
                            key={key}
                            onClick={() => handleStatusUpdate(shipment.trackingNumber, value)}
                            disabled={updating === shipment.trackingNumber || shipment.trackingStatus === value}
                            className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${
                              shipment.trackingStatus === value
                                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                : updating === shipment.trackingNumber
                                  ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                  : 'bg-gray-50 text-gray-700 hover:bg-orange-50 hover:text-orange-700'
                            }`}
                          >
                            {updating === shipment.trackingNumber ? (
                              <div className="flex items-center">
                                <div className="animate-spin h-4 w-4 border-2 border-orange-500 border-t-transparent rounded-full mr-2"></div>
                                Güncelleniyor...
                              </div>
                            ) : (
                              trackingStatusDisplayMap[value]?.label || value
                            )}
                          </button>
                        ))}
                      </div>
                    ) : (
                      <div className="text-center py-4">
                        <p className="text-gray-500 text-sm">
                          Bu kargo takip durumu güncellenemez
                        </p>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </Layout>
  );
};

export default CarrierTrackingPage; 