# CargoTrack - Frontend

Modern, responsive ve kullanıcı dostu kargo takip sistemi frontend uygulaması. Airbnb tarzı temiz tasarım ile geliştirilmiştir.

## 🚀 Özellikler

- **Modern UI/UX**: Airbnb tarzı temiz ve modern tasarım
- **Responsive Design**: Tüm cihazlarda mükemmel görünüm
- **Role-Based Access**: Kullanıcı rollerine göre erişim kontrolü
- **Dark/Light Theme**: Karanlık ve aydınlık tema desteği
- **Real-time Updates**: Anlık durum güncellemeleri
- **Progressive Web App**: PWA desteği ile mobil deneyim

## 🛠 Teknolojiler

- **React 19** - UI Framework
- **TypeScript** - Tip güvenliği
- **Material-UI (MUI)** - Component library
- **React Router** - Sayfa yönlendirme
- **React Hook Form** - Form yönetimi
- **React Query** - Veri yönetimi ve cache
- **Axios** - HTTP client

## 👥 Kullanıcı Rolleri

### 🔴 Admin (Sistem Yöneticisi)
- Tüm sistem yönetimi
- Kullanıcı yönetimi
- Analitik ve raporlar
- Tüm gönderileri görme/düzenleme

### 🔵 Shipper (Gönderici)
- Yeni gönderi oluşturma
- Kendi gönderilerini yönetme
- Gönderi takibi
- Kendi analitikleri

### 🟡 Carrier (Taşıyıcı)
- Atanmış gönderileri görme
- Durum güncellemeleri yapma
- Teslimat işlemleri

### 🟢 Customer (Müşteri)
- Gönderi takibi
- Bildirim tercihleri
- Geçmiş görüntüleme

## 🚀 Kurulum

1. **Repository'yi klonlayın:**
   ```bash
   git clone <repository-url>
   cd cargo-tracking/frontend
   ```

2. **Bağımlılıkları yükleyin:**
   ```bash
   npm install
   ```

3. **Environment variables ayarlayın:**
   `.env` dosyası oluşturun:
   ```env
   REACT_APP_API_URL=http://localhost:8080/api
   REACT_APP_NAME=CargoTrack
   REACT_APP_VERSION=1.0.0
   REACT_APP_DEBUG=true
   ```

4. **Uygulamayı başlatın:**
   ```bash
   npm start
   ```

## 📱 Demo Hesapları

Uygulamayı test etmek için aşağıdaki demo hesapları kullanabilirsiniz:

| Rol | E-posta | Şifre |
|-----|---------|-------|
| Admin | admin@cargo.com | 123456 |
| Gönderici | shipper@cargo.com | 123456 |
| Taşıyıcı | carrier@cargo.com | 123456 |
| Müşteri | customer@cargo.com | 123456 |

## 🎨 Tema Özellikleri

### Renk Paleti
- **Primary**: #FF5A60 (Airbnb Red)
- **Secondary**: #00A699 (Teal)
- **Success**: #2E7D32 (Green)
- **Warning**: #ED6C02 (Orange)
- **Error**: #D32F2F (Red)

### Tipografi
- **Font Family**: "Circular", "Helvetica Neue", Arial, sans-serif
- **Modern spacing**: Tutarlı boşluklar
- **Clear hierarchy**: Net başlık hiyerarşisi

## 📱 Sayfa Yapısı

```
├── /login              # Giriş sayfası
├── /dashboard          # Ana dashboard
├── /tracking           # Gönderi takibi
├── /shipments          # Gönderi listesi
├── /shipments/create   # Yeni gönderi
├── /profile            # Kullanıcı profili
├── /settings           # Ayarlar
├── /analytics          # Analitik (Admin/Shipper)
├── /users              # Kullanıcı yönetimi (Admin)
└── /notifications      # Bildirimler
```

## 🔧 Geliştirme

### Proje Yapısı
```
src/
├── components/         # Yeniden kullanılabilir bileşenler
│   ├── Header.tsx     # Header bileşeni
│   ├── Sidebar.tsx    # Sidebar navigation
│   └── Layout.tsx     # Ana layout
├── pages/             # Sayfa bileşenleri
│   ├── LoginPage.tsx  # Giriş sayfası
│   └── DashboardPage.tsx # Dashboard
├── context/           # React Context'ler
│   ├── AuthContext.tsx    # Kimlik doğrulama
│   └── ThemeContext.tsx   # Tema yönetimi
├── services/          # API servisleri
│   ├── authService.ts     # Auth API
│   └── shipmentService.ts # Shipment API
├── types/             # TypeScript tipleri
│   └── index.ts       # Ana tip tanımları
└── utils/             # Yardımcı fonksiyonlar
```

### Kod Standartları
- **TypeScript**: Tüm kodlar tip güvenli
- **ESLint**: Kod kalitesi kontrolü
- **Prettier**: Kod formatı
- **Component Pattern**: Fonksiyonel bileşenler ve hooks

## 📦 Build

Production build oluşturmak için:

```bash
npm run build
```

Build dosyaları `build/` klasöründe oluşacaktır.

## 🧪 Test

Testleri çalıştırmak için:

```bash
npm test
```

## 🌐 Browser Desteği

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 🤝 Katkıda Bulunma

1. Fork edin
2. Feature branch oluşturun (`git checkout -b feature/AmazingFeature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add some AmazingFeature'`)
4. Branch'inizi push edin (`git push origin feature/AmazingFeature`)
5. Pull Request oluşturun

---

**CargoTrack** - Modern Kargo Takip Sistemi 📦
