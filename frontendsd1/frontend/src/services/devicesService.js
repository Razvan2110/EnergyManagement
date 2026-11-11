import ApiClient from './apiClient';

class DevicesService {
  constructor() {
    this.client = new ApiClient(import.meta.env.VITE_DEVICES_URL || 'http://localhost:8083/api/v1');
  }

  setToken(token) { this.client.setToken(token); }
  clearToken() { this.client.clearToken(); }

  async createDevice(data) {
    return this.client.post('/devices', data);
  }

  async getAllDevices() {
    return this.client.get('/devices');
  }

  async getDevice(id) {
    return this.client.get(`/devices/${id}`);
  }

  async updateDevice(id, data) {
    return this.client.put(`/devices/${id}`, data);
  }

  async deleteDevice(id) {
    return this.client.delete(`/devices/${id}`);
  }

  async getDevicesByUserId(userId) {
    return this.client.get(`/devices/user/${userId}`);
  }

  async deleteDevicesByUserId(userId) {
    return this.client.delete(`/devices/user/${userId}`);
  }
}

const devicesService = new DevicesService();
export default devicesService;
