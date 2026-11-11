import ApiClient from './apiClient';

class AuthService {
  constructor() {
    this.client = new ApiClient(import.meta.env.VITE_AUTH_URL || 'http://localhost:8081');
  }

  setToken(token) { this.client.setToken(token); }
  clearToken() { this.client.clearToken(); }

  async register(payload) {
    return this.client.post('/auth/register', payload);
  }

  async login({ username, password }) {
  const response = await fetch(`${import.meta.env.VITE_AUTH_URL || import.meta.env.REACT_APP_AUTH_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) throw new Error("Invalid credentials");

  const token = await response.text();
  return token;
}


  async validate(token) {
  const response = await fetch(`${import.meta.env.VITE_AUTH_URL || 'http://localhost:8081'}/auth/validate`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ token }),
  });

  if (!response.ok) throw new Error("Token validation failed");
  return response.json();
}


  async listUsers() {
    return this.client.get('/users');
  }

  async getUser(id) {
    return this.client.get(`/users/${id}`);
  }

  async createUser(user) {
    return this.client.post('/users', user);
  }

  async updateUser(id, data) {
    return this.client.put(`/users/${id}`, data);
  }

  async deleteUser(id) {
    return this.client.delete(`/users/${id}`);
  }
}

const authService = new AuthService();
export default authService;
