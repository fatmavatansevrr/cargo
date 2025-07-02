import React, { useState } from 'react';
import Header from './Header';
import Sidebar from './Sidebar';

interface LayoutProps {
  children: React.ReactNode;
}

/**
 * Ana layout component'i - tüm sayfalarda kullanılan temel yapı
 * Responsive tasarım ve accessibility özellikleri içerir
 */
const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleSidebarToggle = () => {
    setSidebarOpen(!sidebarOpen);
  };

  const handleSidebarClose = () => {
    setSidebarOpen(false);
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Sidebar component'i */}
      <Sidebar open={sidebarOpen} onClose={handleSidebarClose} />
      
      {/* Header component'i - sidebar toggle fonksiyonu ile */}
      <Header onSidebarToggle={handleSidebarToggle} />
      
      {/* Ana içerik alanı - responsive ve accessible */}
      <main 
        className="flex-1 w-full overflow-x-hidden"
        role="main"
        aria-label="Ana içerik"
      >
        <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-6">
          {children}
        </div>
      </main>
      
      {/* Footer alanı (gelecekte eklenebilir) */}
      <footer 
        className="bg-white border-t border-gray-200 py-4"
        role="contentinfo"
        aria-label="Site footer"
      >
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <p className="text-center text-sm text-gray-500">
            © 2024 Kargo Takip Sistemi. Tüm hakları saklıdır.
          </p>
        </div>
      </footer>
    </div>
  );
};

export default Layout; 