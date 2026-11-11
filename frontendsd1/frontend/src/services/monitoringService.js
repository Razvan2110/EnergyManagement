import ApiClient from './apiClient';

class MonitoringService {
    constructor() {
        this.client = new ApiClient(import.meta.env.VITE_MONITORING_URL || 'http://localhost:8084');
    }

    setToken(token) { this.client.setToken(token); }
    clearToken() { this.client.clearToken(); }

    /**
     * Fetch hourly consumption for a specific device on a specific date.
     * @param {string} deviceId 
     * @param {string} date YYYY-MM-DD format
     */
    async getHourlyConsumption(deviceId, date) {
        return this.client.get(`/hourlyConsumption/${deviceId}?date=${date}`);
    }
}

const monitoringService = new MonitoringService();
export default monitoringService;
