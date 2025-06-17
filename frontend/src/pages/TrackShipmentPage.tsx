import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const TrackShipmentPage: React.FC = () => {
  const navigate = useNavigate();
  const [trackingNumber, setTrackingNumber] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [trackingData, setTrackingData] = useState<any>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!trackingNumber.trim()) {
      setError('Lütfen takip numarası giriniz');
      return;
    }

    setLoading(true);
    setError(null);

    console.log('🔍 Takip isteği gönderiliyor:', {
      url: `http://localhost:8080/api/tracking/${trackingNumber}`,
      trackingNumber
    });

    try {
      const response = await fetch(`http://localhost:8080/api/tracking/${trackingNumber}`);
      
      console.log('📥 Takip response alındı:', {
        status: response.status,
        statusText: response.statusText,
        url: response.url
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('❌ Takip error response:', errorText);
        throw new Error('Takip numarası bulunamadı');
      }
      const data = await response.json();
      console.log('✅ Takip verisi alındı:', data);
      setTrackingData(data);
    } catch (err) {
      console.error('❌ Takip hatası:', err);
      setError(err instanceof Error ? err.message : 'Bir hata oluştu');
      setTrackingData(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto px-4">
        <div className="bg-white shadow-lg rounded-lg overflow-hidden">
          <div className="bg-blue-600 px-6 py-4">
            <h1 className="text-2xl font-bold text-white">Kargo Takip</h1>
            <p className="text-blue-100 mt-1">Takip numaranızı girerek kargonuzun durumunu öğrenin</p>
          </div>

          <div className="p-6">
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
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
              
              <button
                type="submit"
                disabled={loading}
                className="w-full px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                {loading ? 'Aranıyor...' : 'Takip Et'}
              </button>
            </form>

            {error && (
              <div className="mt-4 bg-red-50 border border-red-200 rounded-lg p-4">
                <p className="text-red-800">{error}</p>
              </div>
            )}

            {trackingData && (
              <div className="mt-6 bg-green-50 border border-green-200 rounded-lg p-4">
                <h3 className="text-lg font-semibold text-green-800 mb-2">Takip Sonucu</h3>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Takip No:</span>
                    <span className="font-medium">{trackingData.shipmentId}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Durum:</span>
                    <span className="font-medium">{trackingData.status}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Konum:</span>
                    <span className="font-medium">{trackingData.location}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Güncelleme:</span>
                    <span className="font-medium">{new Date(trackingData.updatedAt).toLocaleString('tr-TR')}</span>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="mt-8 text-center">
          <button
            onClick={() => navigate('/dashboard')}
            className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Dashboard'a Dön
          </button>
        </div>
      </div>
    </div>
  );
};

export default TrackShipmentPage; 