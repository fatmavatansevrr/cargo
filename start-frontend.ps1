# Frontend başlatma script'i - PowerShell için
# Kullanım: .\start-frontend.ps1

Write-Host "🚀 Frontend başlatılıyor..." -ForegroundColor Green

# Frontend dizinine geç
Set-Location "frontend"

# Node modüllerini kontrol et
if (-Not (Test-Path "node_modules")) {
    Write-Host "📦 Node modülleri yükleniyor..." -ForegroundColor Yellow
    npm install
}

# Development server'ı başlat
Write-Host "🌐 Development server başlatılıyor..." -ForegroundColor Cyan
npm start 