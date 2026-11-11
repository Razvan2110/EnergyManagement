import ApiClient from './apiClient';

class UsersService {
  constructor() {
    this.client = new ApiClient(import.meta.env.VITE_USERS_URL || 'http://localhost:8082/api/v1');
  }

  setToken(token) { this.client.setToken(token); }
  clearToken() { this.client.clearToken(); }

  async createUser(data) {
    return this.client.post('/users', data);
  }

  async getUser(id) {
    return this.client.get(`/users/${id}`);
  }

  async getAllUsers() {
    return this.client.get('/users');
  }

  async updateUser(id, data) {
    return this.client.put(`/users/${id}`, data);
  }

  async deleteUser(id) {
    return this.client.delete(`/users/${id}`);
  }

  async ensureUserExists(id) {
    try {
      const user = await this.getUser(id);
      return user;
    } catch (err) {
      throw {status: 404, message: 'User not found'};
    }
  }
}

const usersService = new UsersService();
export default usersService;
