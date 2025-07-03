import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_ANALYTICS_API_URL || 'http://localhost:8085';

// Types for analytics data
export interface ShipmentAnalytics {
    shipmentId: string;
    carrierId: string;
    status: string;
    totalShipments: number;
    totalRevenue: number;
    averageDeliveryTime: number;
    deliveryDelayHours: number;
    customerSatisfaction: number;
    shipmentsByStatus: Record<string, number>;
    timestamp: string;
}

export interface CarrierPerformance {
    carrierId: string;
    totalShipments: number;
    onTimeDeliveryRate: number;
    averageDeliveryTime: number;
    delayedDeliveries: number;
    averageDelayTime: number;
    satisfactionRating: number;
    deliveriesByStatus: Record<string, number>;
    totalRevenue: number;
    periodStart: string;
    periodEnd: string;
}

export interface StatusDistribution {
    statusDistribution: Record<string, number>;
    statusChangesOverTime: Record<string, Record<string, number>>;
    averageTimeInStatus: Record<string, number>;
    statusTransitionPaths: Record<string, number>;
    periodStart: string;
    periodEnd: string;
}

// Report service class
class ReportService {
    private getAuthHeaders() {
        const token = localStorage.getItem('token');
        const headers: any = {
            'Content-Type': 'application/json'
        };

        // Only add Authorization header if token exists
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        return { headers };
    }

    // Generate sample data for testing
    async generateSampleData(): Promise<void> {
        try {
            console.log('🔍 Sample data API isteği gönderiliyor:', `${API_BASE_URL}/api/v1/analytics/generate-sample-data`);
            const response = await axios.post(`${API_BASE_URL}/api/v1/analytics/generate-sample-data`, {}, this.getAuthHeaders());
            console.log('✅ Sample data oluşturuldu:', response.data);
        } catch (error: any) {
            console.error('❌ Sample data error:', {
                message: error.message,
                status: error.response?.status,
                statusText: error.response?.statusText,
                data: error.response?.data,
                url: `${API_BASE_URL}/api/v1/analytics/generate-sample-data`
            });
            throw new Error(`Sample data API hatası: ${error.response?.status} - ${error.message}`);
        }
    }

    // Get all analytics data for a specific company
    async getAllAnalytics(companyId: string): Promise<ShipmentAnalytics[]> {
        if (!companyId) {
            console.error('❌ Company ID is required for getAllAnalytics');
            return []; // or throw new Error('Company ID is required');
        }
        try {
            const url = `${API_BASE_URL}/api/v1/analytics?companyId=${companyId}`;
            console.log('🔍 Analytics API isteği gönderiliyor:', url);
            const response = await axios.get(url, this.getAuthHeaders());
            console.log('✅ Analytics verisi alındı:', response.data);
            return response.data;
        } catch (error: any) {
            const url = `${API_BASE_URL}/api/v1/analytics?companyId=${companyId}`;
            console.error('❌ Analytics error:', {
                message: error.message,
                status: error.response?.status,
                statusText: error.response?.statusText,
                data: error.response?.data,
                url: url
            });
            throw new Error(`Analytics API hatası: ${error.response?.data?.message || error.message}`);
        }
    }

    // Get analytics by carrier
    async getAnalyticsByCarrier(carrierId: string): Promise<ShipmentAnalytics[]> {
        try {
            const response = await axios.get(`${API_BASE_URL}/api/v1/analytics/carrier/${carrierId}`, this.getAuthHeaders());
            return response.data;
        } catch (error) {
            console.error('Error fetching carrier analytics:', error);
            throw error;
        }
    }

    // Get carrier performance metrics
    async getCarrierPerformance(carrierId: string): Promise<CarrierPerformance> {
        try {
            const response = await axios.get(`${API_BASE_URL}/api/v1/analytics/carrier/${carrierId}/performance`, this.getAuthHeaders());
            return response.data;
        } catch (error) {
            console.error('Error fetching carrier performance:', error);
            throw error;
        }
    }

    // Get status distribution
    async getStatusDistribution(): Promise<StatusDistribution> {
        try {
            const response = await axios.get(`${API_BASE_URL}/api/v1/analytics/status-distribution`, this.getAuthHeaders());
            return response.data;
        } catch (error) {
            console.error('Error fetching status distribution:', error);
            throw error;
        }
    }

    // Get analytics by date range
    async getAnalyticsByDateRange(startDate: string, endDate: string): Promise<ShipmentAnalytics[]> {
        try {
            const response = await axios.get(`${API_BASE_URL}/api/v1/analytics/date-range`, {
                ...this.getAuthHeaders(),
                params: {
                    startDate,
                    endDate
                }
            });
            return response.data;
        } catch (error) {
            console.error('Error fetching analytics by date range:', error);
            throw error;
        }
    }

    // Get analytics by customer
    async getAnalyticsByCustomer(customerId: string): Promise<ShipmentAnalytics[]> {
        try {
            const response = await axios.get(`${API_BASE_URL}/api/v1/analytics/customer/${customerId}`, this.getAuthHeaders());
            return response.data;
        } catch (error) {
            console.error('Error fetching customer analytics:', error);
            throw error;
        }
    }
}

export default new ReportService();