import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

// Backend DTO'larına uygun interface'ler
interface TrackingHistoryResponse {
  shipmentId: string;
  status: TrackingState;
  location: string;
  updatedBy: string;
  updatedAt: string;
}

interface ShipmentResponse {
  id: number;
  trackingNumber: string;
  senderUserId: number;
  senderAddress: AddressDto;
  recipientAddress: AddressDto;
  packageInfo: PackageDto;
  serviceType: ServiceType;
  status: ShipmentStatus;
  estimatedDeliveryDate: string | null;
  actualDeliveryDate: string | null;
  shippingCost: number;
  specialInstructions: string | null;
  notes: string | null;
  assignedCarrierId: number | null;
  deliveryPreferences: DeliveryPreferencesDto | null;
  createdAt: string;
  updatedAt: string;
}

interface AddressDto {
  fullName: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  phone?: string;
  email?: string;
}

interface PackageDto {
  weight: number;
  length: number;
  width: number;
  height: number;
  contentType: ContentType;
  contentDescription?: string;
  declaredValue: number;
  isFragile: boolean;
  requiresSignature: boolean;
}

interface DeliveryPreferencesDto {
  preferredDeliveryTime?: string;
  specialInstructions?: string;
}

// Enums
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

enum ServiceType {
  STANDARD = 'STANDARD',
  EXPRESS = 'EXPRESS',
  OVERNIGHT = 'OVERNIGHT',
  INTERNATIONAL = 'INTERNATIONAL'
}

enum ShipmentStatus {
  ACTIVE = 'ACTIVE',
  FINISHED = 'FINISHED',
  CANCELLED = 'CANCELLED'
}

enum ContentType {
  DOCUMENTS = 'DOCUMENTS',
  ELECTRONICS = 'ELECTRONICS',
  CLOTHING = 'CLOTHING',
  FOOD = 'FOOD',
  FRAGILE = 'FRAGILE',
  OTHER = 'OTHER'
}

const TrackingPage: React.FC = () => {
  const { trackingNumber: urlTrackingNumber } = useParams<{ trackingNumber: string }>();
  const navigate = useNavigate();
  
  const [trackingNumber, setTrackingNumber] = useState(urlTrackingNumber || '');
  const [trackingHistory, setTrackingHistory] = useState<TrackingHistoryResponse[]>([]);
  const [shipmentDetails, setShipmentDetails] = useState<ShipmentResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searched, setSearched] = useState(false);

  // Sayfa yüklendiğinde URL'deki takip numarasını arama
  useEffect(() => {
    if (urlTrackingNumber) {
      handleTrackingSearch();
    }
  }, [urlTrackingNumber]);

  // Tracking durumu Türkçe çevirisi
  const getTrackingStateText = (state: TrackingState): string => {
    const stateTexts = {
      [TrackingState.CREATED]: 'Oluşturuldu',
      [TrackingState.PICKED_UP]: 'Kargoya Verildi',
      [TrackingState.IN_TRANSIT]: 'Yolda',
      [TrackingState.AT_SORTING_FACILITY]: 'Transfer Merkezinde',
      [TrackingState.OUT_FOR_DELIVERY]: 'Dağıtımda',
      [TrackingState.DELIVERED]: 'Teslim Edildi',
      [TrackingState.DELIVERY_FAILED]: 'Teslim Edilemedi',
      [TrackingState.RETURNED_TO_SENDER]: 'Gönderene İade',
      [TrackingState.CANCELLED]: 'İptal Edildi'
    };
    return stateTexts[state] || state;
  };

  // Durum renk sınıfı
  const getStatusColorClass = (state: TrackingState): string => {
    switch (state) {
      case TrackingState.DELIVERED:
        return 'bg-green-100 text-green-800 border-green-200';
      case TrackingState.CANCELLED:
      case TrackingState.DELIVERY_FAILED:
      case TrackingState.RETURNED_TO_SENDER:
        return 'bg-red-100 text-red-800 border-red-200';
      case TrackingState.OUT_FOR_DELIVERY:
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case TrackingState.IN_TRANSIT:
      case TrackingState.AT_SORTING_FACILITY:
        return 'bg-blue-100 text-blue-800 border-blue-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  // Hizmet tipi Türkçe çevirisi
  const getServiceTypeText = (type: ServiceType): string => {
    const typeTexts = {
      [ServiceType.STANDARD]: 'Standart Kargo',
      [ServiceType.EXPRESS]: 'Hızlı Kargo',
      [ServiceType.OVERNIGHT]: 'Ertesi Gün Teslimat',
      [ServiceType.INTERNATIONAL]: 'Uluslararası'
    };
    return typeTexts[type] || type;
  };

  // İçerik tipi Türkçe çevirisi
  const getContentTypeText = (type: ContentType): string => {
    const typeTexts = {
      [ContentType.DOCUMENTS]: 'Belgeler',
      [ContentType.ELECTRONICS]: 'Elektronik',
      [ContentType.CLOTHING]: 'Giyim',
      [ContentType.FOOD]: 'Gıda',
      [ContentType.FRAGILE]: 'Kırılabilir',
      [ContentType.OTHER]: 'Diğer'
    };
    return typeTexts[type] || type;
  };

  // Tarih formatı
  const formatDateTime = (dateString: string): string => {
    return new Date(dateString).toLocaleString('tr-TR');
  };

  // Takip arama işlemi
  const handleTrackingSearch = async () => {
    if (!trackingNumber.trim()) {
      setError('Lütfen takip numarası giriniz');
      return;
    }

    setLoading(true);
    setError(null);
    setSearched(true);

    try {
      // Paralel olarak hem tracking hem de shipment bilgilerini al
      const [trackingResponse, shipmentResponse] = await Promise.all([
        fetch(`/api/tracking/${trackingNumber}`),
        fetch(`/api/shipments/tracking/${trackingNumber}`)
      ]);

      // Tracking bilgileri
      if (trackingResponse.ok) {
        const trackingData = await trackingResponse.json();
        setTrackingHistory([trackingData]); // Backend tek kayıt döndürüyor, array yapıyoruz
      } else {
        setTrackingHistory([]);
      }

      // Shipment detayları
      if (shipmentResponse.ok) {
        const shipmentData = await shipmentResponse.json();
        setShipmentDetails(shipmentData);
      } else {
        setShipmentDetails(null);
      }

      // Her ikisi de başarısızsa hata
      if (!trackingResponse.ok && !shipmentResponse.ok) {
        throw new Error('Takip numarası bulunamadı');
      }

    } catch (err) {
      setError(err instanceof Error ? err.message : 'Bir hata oluştu');
      setTrackingHistory([]);
      setShipmentDetails(null);
    } finally {
      setLoading(false);
    }
  };

  // Form submit
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleTrackingSearch();
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Başlık ve Arama */}
        <div className="bg-white shadow-lg rounded-lg overflow-hidden mb-6">
          <div className="bg-blue-600 px-6 py-4">
            <h1 className="text-2xl font-bold text-white">Kargo Takip</h1>
            <p className="text-blue-100 mt-1">Takip numaranızı girerek kargonuzun durumunu öğrenin</p>
          </div>

          <div className="p-6">
            <form onSubmit={handleSubmit} className="flex gap-4">
              <div className="flex-1">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Takip Numarası
                </label>
                <input
                  type="text"
                  value={trackingNumber}
                  onChange={(e) => setTrackingNumber(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Örn: SHIP123456789"
                />
              </div>
              <div className="flex items-end">
                <button
                  type="submit"
                  disabled={loading}
                  className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  {loading ? 'Aranıyor...' : 'Takip Et'}
                </button>
              </div>
            </form>

            {error && (
              <div className="mt-4 bg-red-50 border border-red-200 rounded-lg p-4">
                <p className="text-red-800">{error}</p>
              </div>
            )}
          </div>
        </div>

        {/* Sonuçlar */}
        {searched && !loading && (
          <>
            {/* Gönderi Detayları */}
            {shipmentDetails && (
              <div className="bg-white shadow-lg rounded-lg overflow-hidden mb-6">
                <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
                  <h2 className="text-lg font-semibold text-gray-900">Gönderi Detayları</h2>
                </div>
                <div className="p-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* Temel Bilgiler */}
                    <div>
                      <h3 className="text-sm font-medium text-gray-900 mb-3">Temel Bilgiler</h3>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-gray-600">Takip No:</span>
                          <span className="font-medium">{shipmentDetails.trackingNumber}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">Hizmet Tipi:</span>
                          <span className="font-medium">{getServiceTypeText(shipmentDetails.serviceType)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">Oluşturma:</span>
                          <span className="font-medium">{formatDateTime(shipmentDetails.createdAt)}</span>
                        </div>
                        {shipmentDetails.shippingCost && (
                          <div className="flex justify-between">
                            <span className="text-gray-600">Kargo Ücreti:</span>
                            <span className="font-medium">{shipmentDetails.shippingCost} TL</span>
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Paket Bilgileri */}
                    <div>
                      <h3 className="text-sm font-medium text-gray-900 mb-3">Paket Bilgileri</h3>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-gray-600">Ağırlık:</span>
                          <span className="font-medium">{shipmentDetails.packageInfo.weight} kg</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">Boyutlar:</span>
                          <span className="font-medium">
                            {shipmentDetails.packageInfo.length}x{shipmentDetails.packageInfo.width}x{shipmentDetails.packageInfo.height} cm
                          </span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">İçerik:</span>
                          <span className="font-medium">{getContentTypeText(shipmentDetails.packageInfo.contentType)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">Değer:</span>
                          <span className="font-medium">{shipmentDetails.packageInfo.declaredValue} TL</span>
                        </div>
                        {shipmentDetails.packageInfo.isFragile && (
                          <div className="flex justify-between">
                            <span className="text-gray-600">Özellik:</span>
                            <span className="font-medium text-yellow-600">Kırılabilir</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Adres Bilgileri */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6 pt-6 border-t border-gray-200">
                    <div>
                      <h3 className="text-sm font-medium text-gray-900 mb-3">Gönderici</h3>
                      <div className="text-sm text-gray-600">
                        <p className="font-medium text-gray-900">{shipmentDetails.senderAddress.fullName}</p>
                        <p>{shipmentDetails.senderAddress.addressLine1}</p>
                        {shipmentDetails.senderAddress.addressLine2 && (
                          <p>{shipmentDetails.senderAddress.addressLine2}</p>
                        )}
                        <p>{shipmentDetails.senderAddress.city}, {shipmentDetails.senderAddress.state} {shipmentDetails.senderAddress.postalCode}</p>
                        <p>{shipmentDetails.senderAddress.country}</p>
                        {shipmentDetails.senderAddress.phone && (
                          <p className="mt-1">Tel: {shipmentDetails.senderAddress.phone}</p>
                        )}
                      </div>
                    </div>
                    <div>
                      <h3 className="text-sm font-medium text-gray-900 mb-3">Alıcı</h3>
                      <div className="text-sm text-gray-600">
                        <p className="font-medium text-gray-900">{shipmentDetails.recipientAddress.fullName}</p>
                        <p>{shipmentDetails.recipientAddress.addressLine1}</p>
                        {shipmentDetails.recipientAddress.addressLine2 && (
                          <p>{shipmentDetails.recipientAddress.addressLine2}</p>
                        )}
                        <p>{shipmentDetails.recipientAddress.city}, {shipmentDetails.recipientAddress.state} {shipmentDetails.recipientAddress.postalCode}</p>
                        <p>{shipmentDetails.recipientAddress.country}</p>
                        {shipmentDetails.recipientAddress.phone && (
                          <p className="mt-1">Tel: {shipmentDetails.recipientAddress.phone}</p>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Özel Talimatlar */}
                  {shipmentDetails.specialInstructions && (
                    <div className="mt-6 pt-6 border-t border-gray-200">
                      <h3 className="text-sm font-medium text-gray-900 mb-2">Özel Talimatlar</h3>
                      <p className="text-sm text-gray-600">{shipmentDetails.specialInstructions}</p>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Takip Geçmişi */}
            {trackingHistory.length > 0 ? (
              <div className="bg-white shadow-lg rounded-lg overflow-hidden">
                <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
                  <h2 className="text-lg font-semibold text-gray-900">Takip Geçmişi</h2>
                </div>
                <div className="p-6">
                  <div className="space-y-4">
                    {trackingHistory.map((history, index) => (
                      <div key={index} className="flex items-start space-x-4">
                        <div className="flex-shrink-0">
                          <div className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium border ${getStatusColorClass(history.status)}`}>
                            {getTrackingStateText(history.status)}
                          </div>
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between">
                            <p className="text-sm font-medium text-gray-900">
                              {history.location}
                            </p>
                            <p className="text-sm text-gray-500">
                              {formatDateTime(history.updatedAt)}
                            </p>
                          </div>
                          <p className="text-sm text-gray-600">
                            Güncelleme: {history.updatedBy}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            ) : searched && !loading && (
              <div className="bg-white shadow-lg rounded-lg overflow-hidden">
                <div className="p-6 text-center">
                  <div className="text-gray-400 mb-4">
                    <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                  </div>
                  <h3 className="text-lg font-medium text-gray-900 mb-2">Takip bilgisi bulunamadı</h3>
                  <p className="text-gray-600">Girdiğiniz takip numarası sistemde bulunamadı. Lütfen takip numaranızı kontrol edin.</p>
                </div>
              </div>
            )}
          </>
        )}

        {/* Geri Dön Butonu */}
        <div className="mt-8 text-center">
          <button
            onClick={() => navigate('/dashboard')}
            className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <svg className="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Dashboard'a Dön
          </button>
        </div>
      </div>
    </div>
  );
};

export default TrackingPage; 