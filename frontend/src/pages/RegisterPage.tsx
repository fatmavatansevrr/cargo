import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { companyService, Company } from '../services/companyService';
import { RegisterData } from '../types';

// Role seçenekleri - Backend ile aynı format (ROLE_ prefix olmadan)
const roleOptions = [
    {
        value: 'CUSTOMER',
        label: 'Müşteri',
        description: 'Kargo gönderme, takip ve bildirim alma',
        icon: (
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
        ),
        color: 'from-blue-400 to-cyan-500'
    },
    {
        value: 'CARRIER',
        label: 'Taşıyıcı',
        description: 'Kargo durumunu güncellemek için',
        icon: (
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
        ),
        color: 'from-orange-400 to-red-500'
    },
    {
        value: 'SHIPMENT_COMPANY',
        label: 'Kargo Şirketi',
        description: 'Sistem yönetimi ve analiz paneli erişimi',
        icon: (
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
            </svg>
        ),
        color: 'from-purple-400 to-pink-500'
    }
];

const RegisterPage: React.FC = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string>('');
    const [success, setSuccess] = useState<string>('');
    const [selectedRole, setSelectedRole] = useState<string>('CUSTOMER');
    const [companies, setCompanies] = useState<Company[]>([]);
    const [selectedCompanyId, setSelectedCompanyId] = useState<number | null>(null);
    const [isLoadingCompanies, setIsLoadingCompanies] = useState(false);

    const navigate = useNavigate();

    const {
        register,
        handleSubmit,
        formState: { errors },
        watch,
    } = useForm<RegisterData & { confirmPassword: string }>();

    const watchPassword = watch('password');

    // Carrier role seçildiğinde şirket listesini getir
    useEffect(() => {
        if (selectedRole === 'CARRIER') {
            fetchCompanies();
        } else {
            setSelectedCompanyId(null);
        }
    }, [selectedRole]);

    const fetchCompanies = async () => {
        setIsLoadingCompanies(true);
        console.log('🔍 CARRIER DEBUG - Şirket listesi getiriliyor...');
        try {
            const response = await companyService.getShipmentCompanies();
            console.log('🔍 CARRIER DEBUG - API Response:', response);
            if (response.success) {
                console.log('🔍 CARRIER DEBUG - Şirketler başarıyla alındı:', response.data);
                setCompanies(response.data);
                if (response.data.length === 0) {
                    console.warn('⚠️ CARRIER DEBUG - Şirket listesi boş!');
                }
            } else {
                console.error('❌ CARRIER DEBUG - API Error:', response.message);
                setError(response.message);
            }
        } catch (err) {
            console.error('❌ CARRIER DEBUG - Network Error:', err);
            setError('Şirket listesi alınamadı: ' + (err as Error).message);
        } finally {
            setIsLoadingCompanies(false);
        }
    };

    const onSubmit = async (data: RegisterData & { confirmPassword: string }) => {
        console.log('🚀 Register form submitted with data:', { ...data, password: '***', confirmPassword: '***' });
        setIsLoading(true);
        setError('');
        setSuccess('');

        try {
            // Carrier role için şirket seçimi zorunlu kontrolü
            if (selectedRole === 'CARRIER' && !selectedCompanyId) {
                setError('Taşıyıcı olarak kayıt olurken bir şirket seçmelisiniz.');
                return;
            }

            // confirmPassword'u çıkarıp RegisterData formatına dönüştür
            const registerData: RegisterData = {
                username: data.username,
                email: data.email,
                password: data.password,
                firstName: data.firstName,
                lastName: data.lastName,
                phone: data.phone,
                address: data.address || '',
                roles: [selectedRole], // Role prefix'i kaldırıldı - backend enum'ı ROLE_ olmadan tanımlı
                companyId: selectedRole === 'CARRIER' ? selectedCompanyId || undefined : undefined
            };

            console.log('🔄 Calling register service...');
            const response = await authService.register(registerData);

            if (response.success) {
                console.log('✅ Registration successful:', response.data);
                setSuccess('Kayıt başarılı! Giriş sayfasına yönlendiriliyorsunuz...');

                // 2 saniye bekle ve login sayfasına yönlendir
                setTimeout(() => {
                    navigate('/login');
                }, 2000);
            } else {
                throw new Error(response.error || 'Kayıt işlemi başarısız');
            }
        } catch (err: any) {
            console.error('❌ Registration error:', err);
            setError(err.message || 'Kayıt olurken bir hata oluştu.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 relative overflow-hidden">
            {/* Background Decorative Elements */}
            <div className="absolute inset-0 overflow-hidden">
                <div className="absolute -top-40 -left-40 w-80 h-80 bg-gradient-to-r from-purple-100 to-pink-100 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-pulse"></div>
                <div className="absolute -bottom-40 -right-40 w-80 h-80 bg-gradient-to-r from-cyan-100 to-blue-100 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-pulse animation-delay-2000"></div>
                <div className="absolute top-40 right-40 w-60 h-60 bg-gradient-to-r from-orange-100 to-yellow-100 rounded-full mix-blend-multiply filter blur-xl opacity-50 animate-pulse animation-delay-4000"></div>
            </div>

            <div className="relative z-10 flex min-h-screen">
                {/* Left Side - Welcome Content */}
                <div className="hidden lg:flex lg:flex-1 lg:flex-col lg:justify-center lg:px-20 xl:px-24">
                    <div className="mx-auto max-w-sm">
                        <div className="mb-8">
                            <div className="w-20 h-20 bg-gradient-to-br from-purple-400 via-pink-500 to-orange-400 rounded-3xl shadow-2xl flex items-center justify-center mb-6 transform hover:scale-105 transition-transform duration-300">
                                <svg className="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
                                </svg>
                            </div>
                            <h1 className="text-4xl xl:text-5xl font-bold text-gray-900 leading-tight">
                                CargoTrack
                                <br />
                                <span className="bg-gradient-to-r from-purple-500 to-pink-500 bg-clip-text text-transparent">
                  Ailesine Katılın
                </span>
                            </h1>
                            <p className="mt-6 text-lg text-gray-600 leading-relaxed">
                                Ücretsiz hesap oluşturun ve modern kargo takip deneyiminizi bugün başlatın.
                            </p>
                        </div>

                        <div className="grid grid-cols-1 gap-4">
                            <div className="flex items-center space-x-4 p-4 bg-white/60 backdrop-blur-sm rounded-2xl border border-white/20 shadow-sm">
                                <div className="w-12 h-12 bg-gradient-to-br from-emerald-400 to-teal-500 rounded-xl flex items-center justify-center">
                                    <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                                    </svg>
                                </div>
                                <div>
                                    <h3 className="font-semibold text-gray-900">Hızlı Başlangıç</h3>
                                    <p className="text-sm text-gray-600">Saniyeler içinde hesap oluşturun</p>
                                </div>
                            </div>

                            <div className="flex items-center space-x-4 p-4 bg-white/60 backdrop-blur-sm rounded-2xl border border-white/20 shadow-sm">
                                <div className="w-12 h-12 bg-gradient-to-br from-blue-400 to-indigo-500 rounded-xl flex items-center justify-center">
                                    <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                                    </svg>
                                </div>
                                <div>
                                    <h3 className="font-semibold text-gray-900">Güvenli Platform</h3>
                                    <p className="text-sm text-gray-600">Verileriniz şifrelenir ve korunur</p>
                                </div>
                            </div>

                            <div className="flex items-center space-x-4 p-4 bg-white/60 backdrop-blur-sm rounded-2xl border border-white/20 shadow-sm">
                                <div className="w-12 h-12 bg-gradient-to-br from-rose-400 to-pink-500 rounded-xl flex items-center justify-center">
                                    <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                                    </svg>
                                </div>
                                <div>
                                    <h3 className="font-semibold text-gray-900">Ücretsiz</h3>
                                    <p className="text-sm text-gray-600">Temel özellikler tamamen ücretsiz</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Right Side - Register Form */}
                <div className="flex-1 flex flex-col justify-center px-6 py-12 lg:px-20 xl:px-24">
                    <div className="mx-auto w-full max-w-md">
                        {/* Mobile Logo */}
                        <div className="lg:hidden text-center mb-8">
                            <div className="w-16 h-16 bg-gradient-to-br from-purple-400 via-pink-500 to-orange-400 rounded-2xl shadow-xl flex items-center justify-center mx-auto mb-4">
                                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
                                </svg>
                            </div>
                        </div>

                        <div className="text-center lg:text-left mb-8">
                            <h2 className="text-3xl font-bold text-gray-900">
                                Hesap Oluşturun
                            </h2>
                            <p className="mt-2 text-gray-600">
                                Birkaç basit adımda ücretsiz hesabınızı oluşturun
                            </p>
                        </div>

                        {/* Success Message */}
                        {success && (
                            <div className="mb-6 p-4 rounded-2xl bg-green-50 border border-green-100 animate-slide-down">
                                <div className="flex items-center">
                                    <div className="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center mr-3">
                                        <svg className="w-4 h-4 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                                            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                                        </svg>
                                    </div>
                                    <p className="text-sm font-medium text-green-800">{success}</p>
                                </div>
                            </div>
                        )}

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

                        {/* Role Selection */}
                        <div className="mb-8">
                            <label className="block text-sm font-medium text-gray-700 mb-4">
                                Hangi amaçla kullanacaksınız? *
                            </label>
                            <div className="grid grid-cols-1 gap-3">
                                {roleOptions.map((role) => (
                                    <label
                                        key={role.value}
                                        className={`relative flex items-center p-4 cursor-pointer rounded-2xl border-2 transition-all duration-200 hover:shadow-md ${
                                            selectedRole === role.value
                                                ? 'border-purple-500 bg-purple-50 shadow-lg'
                                                : 'border-gray-200 bg-white hover:border-gray-300'
                                        }`}
                                    >
                                        <input
                                            type="radio"
                                            name="role"
                                            value={role.value}
                                            checked={selectedRole === role.value}
                                            onChange={(e) => setSelectedRole(e.target.value)}
                                            className="sr-only"
                                        />
                                        <div className={`w-10 h-10 rounded-xl flex items-center justify-center text-white mr-4 bg-gradient-to-br ${role.color}`}>
                                            {role.icon}
                                        </div>
                                        <div className="flex-1">
                                            <div className="text-sm font-semibold text-gray-900">{role.label}</div>
                                            <div className="text-xs text-gray-600">{role.description}</div>
                                        </div>
                                        {selectedRole === role.value && (
                                            <div className="w-5 h-5 text-purple-500">
                                                <svg fill="currentColor" viewBox="0 0 20 20">
                                                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                                                </svg>
                                            </div>
                                        )}
                                    </label>
                                ))}
                            </div>
                        </div>

                        {/* Company Selection - Sadece Carrier role seçildiğinde görünür */}
                        {selectedRole === 'CARRIER' && (
                            <div className="mb-8">
                                <label className="block text-sm font-medium text-gray-700 mb-4">
                                    Hangi kargo şirketinde çalışıyorsunuz? *
                                </label>
                                {isLoadingCompanies ? (
                                    <div className="flex items-center justify-center p-4 bg-gray-50 rounded-2xl">
                                        <div className="flex items-center space-x-2">
                                            <svg className="animate-spin h-5 w-5 text-purple-500" fill="none" viewBox="0 0 24 24">
                                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                            </svg>
                                            <span className="text-sm text-gray-600">Şirketler yükleniyor...</span>
                                        </div>
                                    </div>
                                ) : companies.length > 0 ? (
                                    <div className="grid grid-cols-1 gap-3 max-h-48 overflow-y-auto">
                                        {companies.map((company) => (
                                            <label
                                                key={company.id}
                                                className={`relative flex items-center p-4 cursor-pointer rounded-2xl border-2 transition-all duration-200 hover:shadow-md ${
                                                    selectedCompanyId === company.id
                                                        ? 'border-purple-500 bg-purple-50 shadow-lg'
                                                        : 'border-gray-200 bg-white hover:border-gray-300'
                                                }`}
                                            >
                                                <input
                                                    type="radio"
                                                    name="company"
                                                    value={company.id}
                                                    checked={selectedCompanyId === company.id}
                                                    onChange={(e) => setSelectedCompanyId(Number(e.target.value))}
                                                    className="sr-only"
                                                />
                                                <div className="w-10 h-10 rounded-xl flex items-center justify-center text-white mr-4 bg-gradient-to-br from-indigo-400 to-purple-500">
                                                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                                                    </svg>
                                                </div>
                                                <div className="flex-1">
                                                    <div className="text-sm font-semibold text-gray-900">
                                                        {company.firstName} {company.lastName}
                                                    </div>
                                                    <div className="text-xs text-gray-600">
                                                        {company.email} {company.phone && `• ${company.phone}`}
                                                    </div>
                                                </div>
                                                {selectedCompanyId === company.id && (
                                                    <div className="w-5 h-5 text-purple-500">
                                                        <svg fill="currentColor" viewBox="0 0 20 20">
                                                            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                                                        </svg>
                                                    </div>
                                                )}
                                            </label>
                                        ))}
                                    </div>
                                ) : (
                                    <div className="text-center p-6 bg-yellow-50 rounded-2xl border border-yellow-200">
                                        <svg className="w-12 h-12 text-yellow-500 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                                        </svg>
                                        <p className="text-sm text-yellow-800 font-medium">Henüz kayıtlı kargo şirketi yok</p>
                                        <p className="text-xs text-yellow-600 mt-1">Lütfen daha sonra tekrar deneyin veya müşteri olarak kayıt olun.</p>
                                    </div>
                                )}
                            </div>
                        )}

                        {/* Register Form */}
                        <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
                            {/* İsim ve Soyisim */}
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-2">
                                        Ad *
                                    </label>
                                    <input
                                        {...register('firstName', {
                                            required: 'Ad gereklidir',
                                            minLength: {
                                                value: 2,
                                                message: 'Ad en az 2 karakter olmalıdır',
                                            },
                                        })}
                                        type="text"
                                        placeholder="Adınız"
                                        className={`w-full px-4 py-3 bg-white border-2 rounded-xl text-gray-900 placeholder-gray-400 transition-all duration-200 focus:outline-none focus:ring-4 ${
                                            errors.firstName
                                                ? 'border-red-300 focus:border-red-500 focus:ring-red-100'
                                                : 'border-gray-200 focus:border-purple-500 focus:ring-purple-100 hover:border-gray-300'
                                        }`}
                                    />
                                    {errors.firstName && (
                                        <p className="mt-1 text-xs text-red-600">{errors.firstName.message}</p>
                                    )}
                                </div>

                                <div>
                                    <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-2">
                                        Soyad *
                                    </label>
                                    <input
                                        {...register('lastName', {
                                            required: 'Soyad gereklidir',
                                            minLength: {
                                                value: 2,
                                                message: 'Soyad en az 2 karakter olmalıdır',
                                            },
                                        })}
                                        type="text"
                                        placeholder="Soyadınız"
                                        className={`w-full px-4 py-3 bg-white border-2 rounded-xl text-gray-900 placeholder-gray-400 transition-all duration-200 focus:outline-none focus:ring-4 ${
                                            errors.lastName
                                                ? 'border-red-300 focus:border-red-500 focus:ring-red-100'
                                                : 'border-gray-200 focus:border-purple-500 focus:ring-purple-100 hover:border-gray-300'
                                        }`}
                                    />
                                    {errors.lastName && (
                                        <p className="mt-1 text-xs text-red-600">{errors.lastName.message}</p>
                                    )}
                                </div>
                            </div>

                            {/* Kullanıcı Adı */}
                            <div>
                                <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-2">
                                    Kullanıcı Adı *
                                </label>
                                <div className="relative">
                                    <input
                                        {...register('username', {
                                            required: 'Kullanıcı adı gereklidir',
                                            minLength: {
                                                value: 3,
                                                message: 'Kullanıcı adı en az 3 karakter olmalıdır',
                                            },
                                            pattern: {
                                                value: /^[a-zA-Z0-9_]+$/,
                                                message: 'Kullanıcı adı sadece harf, rakam ve alt çizgi içerebilir',
                                            },
                                        })}
                                        type="text"
                                        placeholder="kullaniciadi"
                                        className={`w-full px-4 py-3 bg-white border-2 rounded-xl text-gray-900 placeholder-gray-400 transition-all duration-200 focus:outline-none focus:ring-4 ${
                                            errors.username
                                                ? 'border-red-300 focus:border-red-500 focus:ring-red-100'
                                                : 'border-gray-200 focus:border-purple-500 focus:ring-purple-100 hover:border-gray-300'
                                        }`}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                                        <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                                        </svg>
                                    </div>
                                </div>
                                {errors.username && (
                                    <p className="mt-2 text-sm text-red-600 flex items-center">
                                        <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                                        </svg>
                                        {errors.username.message}
                                    </p>
                                )}
                            </div>

                            {/* E-posta */}
                            <div>
                                <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                                    E-posta Adresi *
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
                                        className={`w-full px-4 py-3 bg-white border-2 rounded-xl text-gray-900 placeholder-gray-400 transition-all duration-200 focus:outline-none focus:ring-4 ${
                                            errors.email
                                                ? 'border-red-300 focus:border-red-500 focus:ring-red-100'
                                                : 'border-gray-200 focus:border-purple-500 focus:ring-purple-100 hover:border-gray-300'
                                        }`}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
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

                            {/* Telefon */}
                            <div>
                                <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-2">
                                    Telefon Numarası *
                                </label>
                                <div className="relative">
                                    <input
                                        {...register('phone', {
                                            required: 'Telefon numarası gereklidir',
                                            pattern: {
                                                value: /^[0-9]{10,11}$/,
                                                message: 'Geçerli bir telefon numarası girin (10-11 haneli)',
                                            },
                                        })}
                                        type="tel"
                                        placeholder="05XXXXXXXXX"
                                        className={`w-full px-4 py-3 bg-white border-2 rounded-xl text-gray-900 placeholder-gray-400 transition-all duration-200 focus:outline-none focus:ring-4 ${
                                            errors.phone
                                                ? 'border-red-300 focus:border-red-500 focus:ring-red-100'
                                                : 'border-gray-200 focus:border-purple-500 focus:ring-purple-100 hover:border-gray-300'
                                        }`}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                                        <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                                        </svg>
                                    </div>
                                </div>
                                {errors.phone && (
                                    <p className="mt-2 text-sm text-red-600 flex items-center">
                                        <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                                        </svg>
                                        {errors.phone.message}
                                    </p>
                                )}
                            </div>

                            {/* Şifre */}
                            <div>
                                <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                                    Şifre *
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
                                        placeholder="••••••••"
                                        className={`w-full px-4 py-3 bg-white border-2 rounded-xl text-gray-900 placeholder-gray-400 transition-all duration-200 focus:outline-none focus:ring-4 ${
                                            errors.password
                                                ? 'border-red-300 focus:border-red-500 focus:ring-red-100'
                                                : 'border-gray-200 focus:border-purple-500 focus:ring-purple-100 hover:border-gray-300'
                                        }`}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
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

                            {/* Şifre Tekrar */}
                            <div>
                                <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-2">
                                    Şifre Tekrar *
                                </label>
                                <div className="relative">
                                    <input
                                        {...register('confirmPassword', {
                                            required: 'Şifre tekrarı gereklidir',
                                            validate: (value) =>
                                                value === watchPassword || 'Şifreler eşleşmiyor',
                                        })}
                                        type="password"
                                        placeholder="••••••••"
                                        className={`w-full px-4 py-3 bg-white border-2 rounded-xl text-gray-900 placeholder-gray-400 transition-all duration-200 focus:outline-none focus:ring-4 ${
                                            errors.confirmPassword
                                                ? 'border-red-300 focus:border-red-500 focus:ring-red-100'
                                                : 'border-gray-200 focus:border-purple-500 focus:ring-purple-100 hover:border-gray-300'
                                        }`}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                                        <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                                        </svg>
                                    </div>
                                </div>
                                {errors.confirmPassword && (
                                    <p className="mt-2 text-sm text-red-600 flex items-center">
                                        <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                                        </svg>
                                        {errors.confirmPassword.message}
                                    </p>
                                )}
                            </div>

                            <button
                                type="submit"
                                disabled={isLoading}
                                className="group relative w-full flex justify-center py-4 px-6 border border-transparent text-base font-semibold rounded-2xl text-white bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-600 hover:to-pink-600 focus:outline-none focus:ring-4 focus:ring-purple-200 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 transform hover:scale-[1.02] hover:shadow-xl"
                            >
                                {isLoading ? (
                                    <div className="flex items-center">
                                        <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                        </svg>
                                        Hesap oluşturuluyor...
                                    </div>
                                ) : (
                                    <>
                    <span className="absolute left-0 inset-y-0 flex items-center pl-4">
                      <svg className="h-5 w-5 text-white/70 group-hover:text-white/90 transition-colors duration-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
                      </svg>
                    </span>
                                        Ücretsiz Hesap Oluştur
                                    </>
                                )}
                            </button>
                        </form>

                        {/* Login Link */}
                        <div className="mt-8 text-center">
                            <p className="text-gray-600">
                                Zaten hesabınız var mı?{' '}
                                <Link
                                    to="/login"
                                    className="font-semibold text-purple-600 hover:text-purple-500 transition-colors duration-200 hover:underline"
                                >
                                    Giriş Yapın
                                </Link>
                            </p>
                        </div>

                        {/* Terms */}
                        <div className="mt-6 text-center">
                            <p className="text-xs text-gray-500">
                                Kayıt olarak{' '}
                                <a href="#" className="text-purple-600 hover:text-purple-500 font-medium">
                                    Kullanım Şartları
                                </a>{' '}
                                ve{' '}
                                <a href="#" className="text-purple-600 hover:text-purple-500 font-medium">
                                    Gizlilik Politikası
                                </a>
                                'nı kabul etmiş olursunuz.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RegisterPage;