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
      console.error('❌ Error details:', {
        message: err.message,
        response: err.response,
        stack: err.stack
      });
      setError(err.message || 'Giriş yapılırken bir hata oluştu.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-gray-100 flex flex-col justify-center relative overflow-hidden">
      {/* Background Pattern */}
      <div className="absolute inset-0 z-0">
        <div className="absolute top-0 -left-4 w-72 h-72 bg-gradient-to-br from-primary-400 to-primary-600 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob"></div>
        <div className="absolute top-0 -right-4 w-72 h-72 bg-gradient-to-br from-secondary-400 to-secondary-600 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-2000"></div>
        <div className="absolute -bottom-8 left-20 w-72 h-72 bg-gradient-to-br from-primary-300 to-secondary-400 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-4000"></div>
      </div>

      <div className="sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="mx-auto w-auto flex justify-center">
          <div className="w-16 h-16 bg-gradient-primary rounded-2xl flex items-center justify-center shadow-airbnb">
            <svg className="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
          </div>
        </div>
        <h2 className="mt-6 text-center text-3xl font-bold text-gray-900">
          CargoTrack'e Hoş Geldiniz
        </h2>
        <p className="mt-2 text-center text-sm text-gray-600">
          Kargo takip sisteminize giriş yapın
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="card-elevated px-8 py-8 shadow-hard">
          {error && (
            <div className="mb-6 p-4 rounded-xl bg-red-50 border border-red-200 animate-slide-up">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="ml-3">
                  <p className="text-sm font-medium text-red-800">{error}</p>
                </div>
              </div>
            </div>
          )}

          <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
            <div>
              <label htmlFor="email" className="block text-sm font-semibold text-gray-700 mb-2">
                E-posta Adresi
              </label>
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
                className={`input-primary ${errors.email ? 'border-red-300 focus:ring-red-500 focus:border-red-500' : ''}`}
              />
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
              <label htmlFor="password" className="block text-sm font-semibold text-gray-700 mb-2">
                Şifre
              </label>
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
                className={`input-primary ${errors.password ? 'border-red-300 focus:ring-red-500 focus:border-red-500' : ''}`}
              />
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
                  className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                />
                <label htmlFor="remember-me" className="ml-2 block text-sm text-gray-700">
                  Beni hatırla
                </label>
              </div>

              <div className="text-sm">
                <a href="#" className="font-medium text-primary-600 hover:text-primary-500 transition-colors duration-200">
                  Şifremi unuttum
                </a>
              </div>
            </div>

            <div>
              <button
                type="submit"
                disabled={isLoading}
                className="w-full btn-primary py-4 text-base font-semibold disabled:opacity-50 disabled:cursor-not-allowed relative overflow-hidden group"
              >
                {isLoading ? (
                  <div className="flex items-center justify-center">
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Giriş yapılıyor...
                  </div>
                ) : (
                  <span className="relative z-10">Giriş Yap</span>
                )}
                <div className="absolute inset-0 bg-gradient-to-r from-primary-600 to-primary-700 transform scale-x-0 group-hover:scale-x-100 transition-transform origin-left duration-300"></div>
              </button>
            </div>
          </form>

          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500 font-medium">Demo Hesaplar</span>
              </div>
            </div>

            <div className="mt-6 grid grid-cols-1 gap-3">
              <div className="bg-gray-50 rounded-xl p-4 border border-gray-200">
                <h4 className="text-sm font-semibold text-gray-900 mb-3">Test hesapları:</h4>
                <div className="space-y-2 text-xs">
                  <div className="flex justify-between items-center p-2 bg-white rounded-lg">
                    <span className="font-medium text-gray-700">Admin:</span>
                    <span className="text-gray-600">admin@test.com / 123456</span>
                  </div>
                  <div className="flex justify-between items-center p-2 bg-white rounded-lg">
                    <span className="font-medium text-gray-700">Gönderici:</span>
                    <span className="text-gray-600">shipper@test.com / 123456</span>
                  </div>
                  <div className="flex justify-between items-center p-2 bg-white rounded-lg">
                    <span className="font-medium text-gray-700">Taşıyıcı:</span>
                    <span className="text-gray-600">carrier@test.com / 123456</span>
                  </div>
                  <div className="flex justify-between items-center p-2 bg-white rounded-lg">
                    <span className="font-medium text-gray-700">Test User:</span>
                    <span className="text-gray-600">ahmet@test.com / 123456</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="mt-8 text-center">
            <p className="text-sm text-gray-600">
              Hesabınız yok mu?{' '}
              <Link to="/register" className="font-medium text-primary-600 hover:text-primary-500 transition-colors duration-200">
                Kayıt olun
              </Link>
            </p>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="mt-8 pb-6 text-center relative z-10">
        <p className="text-xs text-gray-500">
          © 2024 CargoTrack. Tüm hakları saklıdır.
        </p>
      </div>


    </div>
  );
};

export default LoginPage; 