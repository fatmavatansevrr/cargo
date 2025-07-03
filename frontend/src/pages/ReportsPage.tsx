import React, { useState, useEffect } from 'react';
import Header from '../components/Header';
import reportService, { ShipmentAnalytics, CarrierPerformance, StatusDistribution } from '../services/reportService';
import { companyService } from '../services/companyService';
import { useAuth } from '../context/AuthContext';
import './ReportsPage.css';

interface ReportsPageProps {}

const ReportsPage: React.FC<ReportsPageProps> = () => {
    const [analytics, setAnalytics] = useState<ShipmentAnalytics[]>([]);
    const [statusDistribution, setStatusDistribution] = useState<StatusDistribution | null>(null);
    const [carrierPerformances, setCarrierPerformances] = useState<Record<string, CarrierPerformance>>({});
    const [carrierNames, setCarrierNames] = useState<Record<string, string>>({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [selectedCarrier, setSelectedCarrier] = useState<string>('');
    const [dateRange, setDateRange] = useState({
        startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        endDate: new Date().toISOString().split('T')[0]
    });
    const { user } = useAuth();

    useEffect(() => {
        if (user?.id) {
            loadReportData(user.id);
        }
    }, [user]);

    const loadReportData = async (companyId: string) => {
        if (!companyId) {
            setError("Şirket kimliği bulunamadı. Raporlar yüklenemiyor.");
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            setError(null);

            // Load all analytics data for the specific company
            const analyticsData = await reportService.getAllAnalytics(companyId);
            setAnalytics(analyticsData);

            // Load status distribution
            const statusData = await reportService.getStatusDistribution();
            setStatusDistribution(statusData);

            // Load carrier performances for unique carriers
            const uniqueCarriers = Array.from(new Set(analyticsData.map(a => a.carrierId)));
            const performances: Record<string, CarrierPerformance> = {};
            const names: Record<string, string> = {};

            for (const carrierId of uniqueCarriers) {
                try {
                    const performance = await reportService.getCarrierPerformance(carrierId);
                    performances[carrierId] = performance;

                    const userResponse = await companyService.getUserById(carrierId);
                    if (userResponse.success) {
                        names[carrierId] = userResponse.data.firstName + ' ' + userResponse.data.lastName;
                    } else {
                        names[carrierId] = `Taşıyıcı ${carrierId}`;
                    }
                } catch (err) {
                    console.warn(`Could not load performance or name for carrier ${carrierId}`);
                    names[carrierId] = `Taşıyıcı ${carrierId}`;
                }
            }
            setCarrierPerformances(performances);
            setCarrierNames(names);

        } catch (err) {
            setError('Rapor verileri yüklenirken hata oluştu');
            console.error('Error loading report data:', err);
        } finally {
            setLoading(false);
        }
    };

    const generateSampleData = async () => {
        try {
            setLoading(true);
            setError(null);
            console.log('🔄 Örnek veri oluşturuluyor...');
            await reportService.generateSampleData();
            console.log('✅ Örnek veri başarıyla oluşturuldu, veriler yeniden yükleniyor...');
            if (user?.id) {
                await loadReportData(user.id);
            }
        } catch (err: any) {
            console.error('❌ Sample data generation error:', err);
            const errorMessage = err.message || 'Bilinmeyen hata oluştu';
            setError(`Örnek veri oluşturulurken hata oluştu: ${errorMessage}`);
        } finally {
            setLoading(false);
        }
    };

    const handleDateRangeChange = async () => {
        try {
            setLoading(true);
            const dateRangeData = await reportService.getAnalyticsByDateRange(
                dateRange.startDate + 'T00:00:00',
                dateRange.endDate + 'T23:59:59'
            );
            setAnalytics(dateRangeData);
        } catch (err) {
            setError('Tarih aralığı verileri yüklenirken hata oluştu');
        } finally {
            setLoading(false);
        }
    };

    const handleCarrierFilter = async () => {
        if (!selectedCarrier) {
            if (user?.id) {
                loadReportData(user.id);
            }
            return;
        }

        try {
            setLoading(true);
            const carrierData = await reportService.getAnalyticsByCarrier(selectedCarrier);
            setAnalytics(carrierData);
        } catch (err) {
            setError('Taşıyıcı verileri yüklenirken hata oluştu');
        } finally {
            setLoading(false);
        }
    };

    // Calculate summary statistics
    const summaryStats = {
        totalShipments: analytics.length,
        averageDeliveryTime: analytics.length > 0
            ? analytics.reduce((sum, a) => sum + a.averageDeliveryTime, 0) / analytics.length
            : 0
    };

    // Get unique carriers for dropdown
    const uniqueCarriers = Array.from(new Set(analytics.map(a => a.carrierId)));

    // Status colors for better visualization
    const statusColors: Record<string, string> = {
        'DELIVERED': '#28a745',
        'IN_TRANSIT': '#007bff',
        'OUT_FOR_DELIVERY': '#ffc107',
        'PROCESSING': '#6c757d',
        'EXCEPTION': '#dc3545',
        'RETURNED': '#6f42c1'
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50">
                <Header />
                <div className="reports-page">
                    <div className="loading-spinner">
                        <div className="spinner"></div>
                        <p>Raporlar yükleniyor...</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <Header />
            <div className="reports-page">
                <div className="reports-header">
                    <h1>📊 Raporlar ve Analizler</h1>
                    <div className="reports-actions">
                        <button
                            className="btn btn-primary"
                            onClick={generateSampleData}
                            disabled={loading}
                        >
                            🎲 Örnek Veri Oluştur
                        </button>
                        <button
                            className="btn btn-secondary"
                            onClick={() => user?.id && loadReportData(user.id)}
                            disabled={loading}
                        >
                            🔄 Verileri Yenile
                        </button>
                    </div>
                </div>

                {error && (
                    <div className="error-message">
                        <strong>Hata:</strong> {error}
                    </div>
                )}

                {/* Filter Section */}
                <div className="filters-section">
                    <h2>📅 Filtreler</h2>
                    <div className="filters-row">
                        <div className="filter-group">
                            <label>Tarih Aralığı:</label>
                            <input
                                type="date"
                                value={dateRange.startDate}
                                onChange={(e) => setDateRange({...dateRange, startDate: e.target.value})}
                            />
                            <span>-</span>
                            <input
                                type="date"
                                value={dateRange.endDate}
                                onChange={(e) => setDateRange({...dateRange, endDate: e.target.value})}
                            />
                            <button className="btn btn-sm" onClick={handleDateRangeChange}>
                                Filtrele
                            </button>
                        </div>
                        <div className="filter-group">
                            <label>Taşıyıcı:</label>
                            <select
                                value={selectedCarrier}
                                onChange={(e) => setSelectedCarrier(e.target.value)}
                            >
                                <option value="">Tüm Taşıyıcılar</option>
                                {uniqueCarriers.map(carrier => (
                                    <option key={carrier} value={carrier}>
                                        {carrierNames[carrier] || carrier}
                                    </option>
                                ))}
                            </select>
                            <button className="btn btn-sm" onClick={handleCarrierFilter}>
                                Filtrele
                            </button>
                        </div>
                    </div>
                </div>

                {/* Summary Statistics */}
                <div className="summary-section">
                    <h2>📈 Genel Özet</h2>
                    <div className="summary-cards">
                        <div className="summary-card">
                            <div className="card-icon">📦</div>
                            <div className="card-content">
                                <h3>Toplam Gönderi</h3>
                                <p className="card-value">{summaryStats.totalShipments}</p>
                            </div>
                        </div>
                        <div className="summary-card">
                            <div className="card-icon">⏱️</div>
                            <div className="card-content">
                                <h3>Ortalama Teslimat Süresi</h3>
                                <p className="card-value">{summaryStats.averageDeliveryTime.toFixed(1)} saat</p>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Status Distribution */}
                {statusDistribution && (
                    <div className="status-section">
                        <h2>📊 Durum Dağılımı</h2>
                        <div className="status-chart">
                            {Object.entries(statusDistribution.statusDistribution).map(([status, count]) => (
                                <div key={status} className="status-bar">
                                    <div className="status-label">
                                        <span
                                            className="status-color"
                                            style={{ backgroundColor: statusColors[status] || '#6c757d' }}
                                        ></span>
                                        {status}
                                    </div>
                                    <div className="status-count">{count}</div>
                                    <div
                                        className="status-progress"
                                        style={{
                                            width: `${(count / summaryStats.totalShipments) * 100}%`,
                                            backgroundColor: statusColors[status] || '#6c757d'
                                        }}
                                    ></div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* Carrier Performance */}
                {Object.keys(carrierPerformances).length > 0 && (
                    <div className="carrier-section">
                        <h2>🚛 Taşıyıcı Performansları</h2>
                        <div className="carrier-grid">
                            {Object.entries(carrierPerformances).map(([carrierId, performance]) => (
                                <div key={carrierId} className="carrier-card">
                                    <h3>{carrierNames[carrierId] || carrierId}</h3>
                                    <div className="carrier-metrics">
                                        <div className="metric">
                                            <span className="metric-label">Toplam Gönderi:</span>
                                            <span className="metric-value">{performance.totalShipments}</span>
                                        </div>
                                        <div className="metric">
                                            <span className="metric-label">Zamanında Teslimat:</span>
                                            <span className="metric-value">{(performance.onTimeDeliveryRate * 100).toFixed(1)}%</span>
                                        </div>
                                        <div className="metric">
                                            <span className="metric-label">Ortalama Teslimat:</span>
                                            <span className="metric-value">{performance.averageDeliveryTime.toFixed(1)} saat</span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* Recent Analytics Table */}
                <div className="analytics-section">
                    <h2>📋 Son Analitik Veriler</h2>
                    <div className="analytics-table-container">
                        <table className="analytics-table">
                            <thead>
                            <tr>
                                <th>Gönderi ID</th>
                                <th>Taşıyıcı</th>
                                <th>Durum</th>
                                <th>Teslimat Süresi</th>
                                <th>Gecikme</th>
                                <th>Tarih</th>
                            </tr>
                            </thead>
                            <tbody>
                            {analytics.slice(0, 10).map((item) => (
                                <tr key={item.shipmentId}>
                                    <td>{item.shipmentId}</td>
                                    <td>{carrierNames[item.carrierId] || item.carrierId}</td>
                                    <td>
                                            <span
                                                className="status-badge"
                                                style={{
                                                    backgroundColor: statusColors[item.status] || '#6c757d',
                                                    color: 'white',
                                                    padding: '4px 8px',
                                                    borderRadius: '4px',
                                                    fontSize: '12px'
                                                }}
                                            >
                                                {item.status}
                                            </span>
                                    </td>
                                    <td>{item.averageDeliveryTime.toFixed(1)} saat</td>
                                    <td>
                                            <span className={item.deliveryDelayHours > 0 ? 'delay-positive' : 'delay-negative'}>
                                                {item.deliveryDelayHours > 0 ? '+' : ''}{item.deliveryDelayHours} saat
                                            </span>
                                    </td>
                                    <td>{new Date(item.timestamp).toLocaleDateString('tr-TR')}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ReportsPage;