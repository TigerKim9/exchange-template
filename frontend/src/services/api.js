import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Auth APIs
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
};

// Wallet APIs
export const walletAPI = {
  getWallets: () => api.get('/wallets'),
  getWallet: (currency) => api.get(`/wallets/${currency}`),
  createWallet: (currency) => api.post(`/wallets/create/${currency}`),
  deposit: (data) => api.post('/wallets/deposit', data),
  withdraw: (data) => api.post('/wallets/withdraw', data),
};

// Trading APIs
export const tradingAPI = {
  getTradingPairs: () => api.get('/trading/pairs'),
  getTradingPair: (id) => api.get(`/trading/pairs/${id}`),
  createOrder: (orderData) => api.post('/trading/orders', orderData),
  getUserOrders: () => api.get('/trading/orders'),
  getActiveOrders: (tradingPairId) => api.get(`/trading/orders/active/${tradingPairId}`),
  cancelOrder: (orderId) => api.delete(`/trading/orders/${orderId}`),
};

// Transaction APIs
export const transactionAPI = {
  getTransactions: () => api.get('/transactions'),
  getTransactionsPaged: (page, size) => api.get(`/transactions/paged?page=${page}&size=${size}`),
  getTransactionsByCurrency: (currency) => api.get(`/transactions/currency/${currency}`),
};

// Admin APIs
export const adminAPI = {
  getUsers: () => api.get('/admin/users'),
  getStats: () => api.get('/admin/stats'),
  enableUser: (userId) => api.put(`/admin/users/${userId}/enable`),
  disableUser: (userId) => api.put(`/admin/users/${userId}/disable`),
  createTradingPair: (data) => api.post('/admin/trading-pairs', data),
  updateTradingPair: (id, data) => api.put(`/admin/trading-pairs/${id}`, data),
};

export default api;
