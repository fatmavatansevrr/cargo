import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LoginData } from '../types';

const LoginPage: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string>('');
  
  const { login } = useAuth();
  const navigate = useNavigate();
  
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginData>();

  const onSubmit = async (data: LoginData) => {
    console.log('🚀 Login form submitted with data:', data);
    setIsLoading(true);
    setError('');

    try {
      console.log('🔄 Calling login function...');
      await login(data.email, data.password);
      console.log('✅ Login successful, navigating to dashboard...');
      navigate('/dashboard');
    } catch (err: any) {
      console.error('❌ Login error:', err);
      setError(err.message || 'Giriş yapılırken bir hata oluştu.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 relative overflow-hidden">
      {/* Background Decorative Elements */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-gradient-to-r from-rose-100 to-teal-100 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-pulse"></div>
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-gradient-to-r from-orange-100 to-rose-100 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-pulse animation-delay-2000"></div>
        <div className="absolute top-40 left-40 w-60 h-60 bg-gradient-to-r from-cyan-100 to-blue-100 rounded-full mix-blend-multiply filter blur-xl opacity-50 animate-pulse animation-delay-4000"></div>
      </div>

      <div className="relative z-10 flex min-h-screen">
        {/* Left Side - Welcome Content */}
        <div className="hidden lg:flex lg:flex-1 lg:flex-col lg:justify-center lg:px-20 xl:px-24">
          <div className="mx-auto max-w-sm">
            <div className="mb-8">
              <div className="w-20 h-20 bg-gradient-to-br from-orange-400 via-pink-500 to-purple-600 rounded-3xl shadow-2xl flex items-center justify-center mb-6 transform hover:scale-105 transition-transform duration-300">
                <svg className="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                </svg>
              </div>
              <h1 className="text-4xl xl:text-5xl font-bold text-gray-900 leading-tight">
                CargoTrack'e
                <br />
                <span className="bg-gradient-to-r from-orange-500 to-pink-500 bg-clip-text text-transparent">
                  Hoş Geldiniz
                </span>
              </h1>
              <p className="mt-6 text-lg text-gray-600 leading-relaxed">
                Modern kargo takip deneyimi ile gönderilerinizi güvenle takip edin, yönetin ve kontrol edin.
              </p>
            </div>
            
            <div className="grid grid-cols-1 gap-4">
              <div className="flex items-center space-x-4 p-4 bg-white/60 backdrop-blur-sm rounded-2xl border border-white/20 shadow-sm">
                <div className="w-12 h-12 bg-gradient-to-br from-green-400 to-emerald-500 rounded-xl flex items-center justify-center">
                  <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">Gerçek Zamanlı Takip</h3>
                  <p className="text-sm text-gray-600">Kargonuzun konumunu canlı olarak izleyin</p>
                </div>
              </div>
              
              <div className="flex items-center space-x-4 p-4 bg-white/60 backdrop-blur-sm rounded-2xl border border-white/20 shadow-sm">
                <div className="w-12 h-12 bg-gradient-to-br from-blue-400 to-cyan-500 rounded-xl flex items-center justify-center">
                  <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-5 5v-5zM12 17H7l5 5v-5zM12 7h5l-5-5v5zM12 7H7l5-5v5z" />
                  </svg>
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">Hızlı Gönderi</h3>
                  <p className="text-sm text-gray-600">Kolayca yeni gönderi oluşturun</p>
                </div>
              </div>

              <div className="flex items-center space-x-4 p-4 bg-white/60 backdrop-blur-sm rounded-2xl border border-white/20 shadow-sm">
                <div className="w-12 h-12 bg-gradient-to-br from-purple-400 to-pink-500 rounded-xl flex items-center justify-center">
                  <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-5 5v-5z" />
                  </svg>
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">Akıllı Bildirimler</h3>
                  <p className="text-sm text-gray-600">SMS ve email ile güncel kalın</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Side - Login Form */}
        <div className="flex-1 flex flex-col justify-center px-6 py-12 lg:px-20 xl:px-24">
          <div className="mx-auto w-full max-w-md">
            {/* Mobile Logo */}
            <div className="lg:hidden text-center mb-8">
              <div className="w-16 h-16 bg-gradient-to-br from-orange-400 via-pink-500 to-purple-600 rounded-2xl shadow-xl flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                </svg>
              </div>
            </div>

            <div className="text-center lg:text-left mb-8">
              <h2 className="text-3xl font-bold text-gray-900">
                Tekrar Hoş Geldiniz
              </h2>
              <p className="mt-2 text-gray-600">
                Hesabınıza giriş yaparak devam edin
              </p>
            </div>

            {/* Error Message */}
            {error && (
              <div className="mb-6 p-4 rounded-2xl bg-red-50 border border-red-100 animate-slide-down">
                <div className="flex items-center">
                  <div className="w-6 h-6 bg-red-100 rounded-full flex items-center justify-center mr-3">
                    <svg className="w-4 h-4 text-red-500" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <p className="text-sm font-medium text-red-800">{error}</p>
                </div>
              </div>
            )}

            {/* Login Form */}
            <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                  E-posta Adresi
                </label>
                <div className="relative">
                  <input
                    {...register('email', {
                      required: 'E-posta gereklidir',
                      pattern: {
                        value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                        message: 'Geçerli bir e-posta adresi girin',
                      },
                    })}
                    type="email"
                    autoComplete="email"
                    placeholder="ornek@email.com"
                    className={`w-full px-4 py-4 bg-white border-2 rounded-2xl text-gray-900 placeholder-gray-400 transition-all duration-200 focus:outline-none focus:ring-4 ${
                      errors.email 
                        ? 'border-red-300 focus:border-red-500 focus:ring-red-100' 
                        : 'border-gray-200 focus:border-orange-500 focus:ring-orange-100 hover:border-gray-300'
                    }`}
                  />
                  <div className="absolute inset-y-0 right-0 pr-4 flex items-center">
                    <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                    </svg>
                  </div>
                </div>
                {errors.email && (
                  <p className="mt-2 text-sm text-red-600 flex items-center">
                    <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                    {errors.email.message}
                  </p>
                )}
              </div>

              <div>
                <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                  Şifre
                </label>
                <div className="relative">
                  <input
                    {...register('password', {
                      required: 'Şifre gereklidir',
                      minLength: {
                        value: 6,
                        message: 'Şifre en az 6 karakter olmalıdır',
                      },
                    })}
                    type="password"
                    autoComplete="current-password"
                    placeholder="••••••••"
                    className={`w-full px-4 py-4 bg-white border-2 rounded-2xl text-gray-900 placeholder-gray-400 transition-all duration-200 focus:outline-none focus:ring-4 ${
                      errors.password 
                        ? 'border-red-300 focus:border-red-500 focus:ring-red-100' 
                        : 'border-gray-200 focus:border-orange-500 focus:ring-orange-100 hover:border-gray-300'
                    }`}
                  />
                  <div className="absolute inset-y-0 right-0 pr-4 flex items-center">
                    <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </div>
                </div>
                {errors.password && (
                  <p className="mt-2 text-sm text-red-600 flex items-center">
                    <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                    {errors.password.message}
                  </p>
                )}
              </div>

              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <input
                    id="remember-me"
                    name="remember-me"
                    type="checkbox"
                    className="h-4 w-4 text-orange-600 focus:ring-orange-500 border-gray-300 rounded"
                  />
                  <label htmlFor="remember-me" className="ml-3 block text-sm text-gray-700">
                    Beni hatırla
                  </label>
                </div>

                <div className="text-sm">
                  <a href="#" className="font-medium text-orange-600 hover:text-orange-500 transition-colors duration-200">
                    Şifremi unuttum
                  </a>
                </div>
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="group relative w-full flex justify-center py-4 px-6 border border-transparent text-base font-semibold rounded-2xl text-white bg-gradient-to-r from-orange-500 to-pink-500 hover:from-orange-600 hover:to-pink-600 focus:outline-none focus:ring-4 focus:ring-orange-200 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 transform hover:scale-[1.02] hover:shadow-xl"
              >
                {isLoading ? (
                  <div className="flex items-center">
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Giriş yapılıyor...
                  </div>
                ) : (
                  <>
                    <span className="absolute left-0 inset-y-0 flex items-center pl-4">
                      <svg className="h-5 w-5 text-white/70 group-hover:text-white/90 transition-colors duration-200" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clipRule="evenodd" />
                      </svg>
                    </span>
                    Giriş Yap
                  </>
                )}
              </button>
            </form>

            {/* Register Link */}
            <div className="mt-8 text-center">
              <p className="text-gray-600">
                Henüz hesabınız yok mu?{' '}
                <Link 
                  to="/register" 
                  className="font-semibold text-orange-600 hover:text-orange-500 transition-colors duration-200 hover:underline"
                >
                  Ücretsiz Kayıt Olun
                </Link>
              </p>
            </div>

            {/* Demo Accounts */}
            <div className="mt-8 p-6 bg-white/80 backdrop-blur-sm rounded-3xl border border-gray-100 shadow-sm">
              <h4 className="text-sm font-semibold text-gray-900 mb-4 text-center">
                Demo Hesapları
              </h4>
              <div className="space-y-3">
                <div className="flex justify-between items-center p-3 bg-gradient-to-r from-gray-50 to-white rounded-xl border border-gray-100">
                  <span className="font-medium text-gray-700">Admin:</span>
                  <span className="text-gray-600 font-mono text-sm">admin@test.com / 123456</span>
                </div>
                <div className="flex justify-between items-center p-3 bg-gradient-to-r from-gray-50 to-white rounded-xl border border-gray-100">
                  <span className="font-medium text-gray-700">Kullanıcı:</span>
                  <span className="text-gray-600 font-mono text-sm">user@test.com / 123456</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage; 