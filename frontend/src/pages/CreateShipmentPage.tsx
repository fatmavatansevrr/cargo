import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';

// Backend DTO'larına uygun TypeScript interface'leri
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
  contentType: 'DOCUMENTS' | 'ELECTRONICS' | 'CLOTHING' | 'FOOD' | 'FRAGILE' | 'OTHER';
  contentDescription?: string;
  declaredValue: number;
    isFragile: boolean;
  requiresSignature: boolean;
}

interface DeliveryPreferencesDto {
  preferredDeliveryTime?: string;
  specialInstructions?: string;
}

interface CreateShipmentRequest {
  senderAddress: AddressDto;
  recipientAddress: AddressDto;
  packageInfo: PackageDto;
  serviceType: 'STANDARD' | 'EXPRESS' | 'OVERNIGHT' | 'INTERNATIONAL';
  specialInstructions?: string;
  notes?: string;
  deliveryPreferences?: DeliveryPreferencesDto;
}

const CreateShipmentPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Form state - Başlangıç değerleri ile
  const [formData, setFormData] = useState<CreateShipmentRequest>({
      senderAddress: {
      fullName: '',
      addressLine1: '',
      addressLine2: '',
        city: '',
        state: '',
        postalCode: '',
      country: 'Türkiye',
      phone: '',
      email: '',
      },
      recipientAddress: {
      fullName: '',
      addressLine1: '',
      addressLine2: '',
        city: '',
        state: '',
        postalCode: '',
      country: 'Türkiye',
      phone: '',
      email: '',
      },
    packageInfo: {
        weight: 0,
          length: 0,
          width: 0,
      height: 0,
      contentType: 'OTHER',
      contentDescription: '',
      declaredValue: 0,
      isFragile: false,
      requiresSignature: false,
    },
    serviceType: 'STANDARD',
    specialInstructions: '',
    notes: '',
    deliveryPreferences: {
      preferredDeliveryTime: '',
      specialInstructions: '',
    },
  });

  // Generic input handler - Path ile nested object güncelleme
  const handleInputChange = (path: string, value: any) => {
    setFormData(prev => {
      const keys = path.split('.');
      const newData = { ...prev };
      let current: any = newData;
      
      for (let i = 0; i < keys.length - 1; i++) {
        current[keys[i]] = { ...current[keys[i]] };
        current = current[keys[i]];
      }
      
      current[keys[keys.length - 1]] = value;
      return newData;
    });
  };

  // Form submission
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    
    console.log('📤 Gönderi oluşturma isteği gönderiliyor:', {
      url: 'http://localhost:8080/api/shipments',
      method: 'POST',
      data: formData
    });
    
    try {
      const response = await fetch('http://localhost:8080/api/shipments', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });
      
      console.log('📥 Response alındı:', {
        status: response.status,
        statusText: response.statusText,
        headers: Object.fromEntries(response.headers.entries()),
        url: response.url
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('❌ Error response:', errorText);
        
        // HTML response geliyorsa API'ye ulaşamıyoruz demektir
        if (errorText.includes('<!DOCTYPE')) {
          throw new Error(`API servisine ulaşılamıyor. Status: ${response.status}`);
        }
        
        let errorData;
        try {
          errorData = JSON.parse(errorText);
        } catch {
          throw new Error(`Server error: ${response.status} - ${errorText}`);
        }
        
        throw new Error(errorData.message || 'Gönderi oluşturulamadı');
      }
      
      const result = await response.json();
      console.log('✅ Gönderi başarıyla oluşturuldu:', result);
      alert(`Gönderi başarıyla oluşturuldu! Takip numarası: ${result.trackingNumber}`);
      navigate('/dashboard');
      
    } catch (err) {
      console.error('❌ Gönderi oluşturma hatası:', err);
      setError(err instanceof Error ? err.message : 'Bir hata oluştu');
    } finally {
      setLoading(false);
    }
  };

        return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white shadow-xl rounded-lg overflow-hidden">
          <div className="bg-blue-600 px-6 py-4">
            <h1 className="text-2xl font-bold text-white">Yeni Gönderi Oluştur</h1>
            <p className="text-blue-100 mt-1">Kargo gönderinizi detaylarıyla birlikte oluşturun</p>
            </div>

          <form onSubmit={handleSubmit} className="p-6 space-y-8">
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                <p className="text-red-800">{error}</p>
              </div>
            )}

            {/* Gönderici Bilgileri */}
            <div className="bg-gray-50 rounded-lg p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Gönderici Bilgileri</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Ad Soyad <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.senderAddress.fullName}
                    onChange={(e) => handleInputChange('senderAddress.fullName', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Telefon
                  </label>
                  <input
                    type="tel"
                    value={formData.senderAddress.phone}
                    onChange={(e) => handleInputChange('senderAddress.phone', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    E-posta
                  </label>
                  <input
                    type="email"
                    value={formData.senderAddress.email}
                    onChange={(e) => handleInputChange('senderAddress.email', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
            </div>
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Adres Satırı 1 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.senderAddress.addressLine1}
                    onChange={(e) => handleInputChange('senderAddress.addressLine1', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Adres Satırı 2
                  </label>
                  <input
                    type="text"
                    value={formData.senderAddress.addressLine2}
                    onChange={(e) => handleInputChange('senderAddress.addressLine2', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Şehir <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.senderAddress.city}
                    onChange={(e) => handleInputChange('senderAddress.city', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    İl <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.senderAddress.state}
                    onChange={(e) => handleInputChange('senderAddress.state', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Posta Kodu <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.senderAddress.postalCode}
                    onChange={(e) => handleInputChange('senderAddress.postalCode', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Ülke <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.senderAddress.country}
                    onChange={(e) => handleInputChange('senderAddress.country', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
              </div>
            </div>

            {/* Alıcı Bilgileri */}
            <div className="bg-gray-50 rounded-lg p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Alıcı Bilgileri</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Ad Soyad <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.recipientAddress.fullName}
                    onChange={(e) => handleInputChange('recipientAddress.fullName', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Telefon
                  </label>
                  <input
                    type="tel"
                    value={formData.recipientAddress.phone}
                    onChange={(e) => handleInputChange('recipientAddress.phone', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    E-posta
                  </label>
                  <input
                    type="email"
                    value={formData.recipientAddress.email}
                    onChange={(e) => handleInputChange('recipientAddress.email', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Adres Satırı 1 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.recipientAddress.addressLine1}
                    onChange={(e) => handleInputChange('recipientAddress.addressLine1', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Adres Satırı 2
                  </label>
                  <input
                    type="text"
                    value={formData.recipientAddress.addressLine2}
                    onChange={(e) => handleInputChange('recipientAddress.addressLine2', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Şehir <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.recipientAddress.city}
                    onChange={(e) => handleInputChange('recipientAddress.city', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    İl <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.recipientAddress.state}
                    onChange={(e) => handleInputChange('recipientAddress.state', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Posta Kodu <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.recipientAddress.postalCode}
                    onChange={(e) => handleInputChange('recipientAddress.postalCode', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Ülke <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.recipientAddress.country}
                    onChange={(e) => handleInputChange('recipientAddress.country', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                    />
                  </div>
              </div>
            </div>

            {/* Paket Bilgileri */}
            <div className="bg-gray-50 rounded-lg p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Paket Bilgileri</h2>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Ağırlık (kg) <span className="text-red-500">*</span>
                  </label>
                  <input
                      type="number"
                    min="0.1"
                    step="0.1"
                    value={formData.packageInfo.weight}
                    onChange={(e) => handleInputChange('packageInfo.weight', parseFloat(e.target.value))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Uzunluk (cm) <span className="text-red-500">*</span>
                  </label>
                  <input
                        type="number"
                    min="1"
                    value={formData.packageInfo.length}
                    onChange={(e) => handleInputChange('packageInfo.length', parseFloat(e.target.value))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Genişlik (cm) <span className="text-red-500">*</span>
                  </label>
                  <input
                        type="number"
                    min="1"
                    value={formData.packageInfo.width}
                    onChange={(e) => handleInputChange('packageInfo.width', parseFloat(e.target.value))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Yükseklik (cm) <span className="text-red-500">*</span>
                  </label>
                  <input
                        type="number"
                    min="1"
                    value={formData.packageInfo.height}
                    onChange={(e) => handleInputChange('packageInfo.height', parseFloat(e.target.value))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    İçerik Tipi <span className="text-red-500">*</span>
                  </label>
                  <select
                    value={formData.packageInfo.contentType}
                    onChange={(e) => handleInputChange('packageInfo.contentType', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  >
                    <option value="DOCUMENTS">Belgeler</option>
                    <option value="ELECTRONICS">Elektronik</option>
                    <option value="CLOTHING">Giyim</option>
                    <option value="FOOD">Gıda</option>
                    <option value="FRAGILE">Kırılabilir</option>
                    <option value="OTHER">Diğer</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Değer (TL) <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={formData.packageInfo.declaredValue}
                    onChange={(e) => handleInputChange('packageInfo.declaredValue', parseFloat(e.target.value))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
              </div>
              
              <div className="mt-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  İçerik Açıklaması
                </label>
                <textarea
                  value={formData.packageInfo.contentDescription}
                  onChange={(e) => handleInputChange('packageInfo.contentDescription', e.target.value)}
                  maxLength={500}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  rows={3}
                />
              </div>
              
              <div className="flex gap-6 mt-4">
                <label className="flex items-center">
                  <input
                    type="checkbox"
                    checked={formData.packageInfo.isFragile}
                    onChange={(e) => handleInputChange('packageInfo.isFragile', e.target.checked)}
                    className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="ml-2 text-sm text-gray-700">Kırılabilir</span>
                </label>
                <label className="flex items-center">
                  <input
                    type="checkbox"
                    checked={formData.packageInfo.requiresSignature}
                    onChange={(e) => handleInputChange('packageInfo.requiresSignature', e.target.checked)}
                    className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="ml-2 text-sm text-gray-700">İmza Gerekli</span>
                </label>
            </div>
            </div>

            {/* Hizmet Tipi ve Ek Bilgiler */}
            <div className="bg-gray-50 rounded-lg p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Hizmet ve Ek Bilgiler</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Hizmet Tipi <span className="text-red-500">*</span>
                  </label>
                  <select
                    value={formData.serviceType}
                    onChange={(e) => handleInputChange('serviceType', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  >
                    <option value="STANDARD">Standart Kargo</option>
                    <option value="EXPRESS">Hızlı Kargo</option>
                    <option value="OVERNIGHT">Ertesi Gün Teslimat</option>
                    <option value="INTERNATIONAL">Uluslararası</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Tercih Edilen Teslimat Zamanı
                  </label>
                  <input
                    type="text"
                    value={formData.deliveryPreferences?.preferredDeliveryTime || ''}
                    onChange={(e) => handleInputChange('deliveryPreferences.preferredDeliveryTime', e.target.value)}
                    placeholder="Örn: 09:00-17:00"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
              </div>
              
              <div className="mt-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Özel Talimatlar
                </label>
                <textarea
                  value={formData.specialInstructions}
                  onChange={(e) => handleInputChange('specialInstructions', e.target.value)}
                  maxLength={1000}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  rows={3}
                  placeholder="Teslimat için özel talimatlarınız..."
                />
              </div>
              
              <div className="mt-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Notlar
                </label>
                <textarea
                  value={formData.notes}
                  onChange={(e) => handleInputChange('notes', e.target.value)}
                  maxLength={500}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  rows={2}
                  placeholder="İç notlarınız..."
                />
              </div>
                </div>

            {/* Form Buttons */}
            <div className="flex justify-end space-x-4 pt-4 border-t border-gray-200">
              <button
                type="button"
                onClick={() => navigate('/dashboard')}
                className="px-6 py-2 text-gray-700 bg-gray-200 rounded-lg hover:bg-gray-300 transition-colors"
              >
                İptal
              </button>
              <button
                type="submit"
                disabled={loading}
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                {loading ? 'Oluşturuluyor...' : 'Gönderi Oluştur'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateShipmentPage;