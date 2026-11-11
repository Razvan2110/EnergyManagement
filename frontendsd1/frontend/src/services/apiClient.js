class ApiClient {
  constructor(baseURL, opts = {}) {
    this.baseURL = baseURL.replace(/\/+$/, '');
    this.timeout = opts.timeout || 15000;
    this.defaultHeaders = opts.defaultHeaders || {'Content-Type': 'application/json'};
    this.token = null;
  }

  setToken(token) {
    this.token = token;
  }

  clearToken() {
    this.token = null;
  }

  async request(path, {method = 'GET', body = null, headers = {}} = {}) {
    const controller = new AbortController();
    const signal = controller.signal;
    setTimeout(() => controller.abort(), this.timeout);

    const options = {method, headers: {...this.defaultHeaders, ...headers}, signal};
    if (body) options.body = JSON.stringify(body);
    if (this.token) options.headers.Authorization = `Bearer ${this.token}`;

    const response = await fetch(`${this.baseURL}${path}`, options);
    const text = await response.text();
    const data = text ? JSON.parse(text) : null;
    if (!response.ok) throw {status: response.status, message: data?.message || response.statusText};
    return data;
  }

  get(path) { return this.request(path, {method: 'GET'}); }
  post(path, body) { return this.request(path, {method: 'POST', body}); }
  put(path, body) { return this.request(path, {method: 'PUT', body}); }
  delete(path) { return this.request(path, {method: 'DELETE'}); }
}

export default ApiClient;
