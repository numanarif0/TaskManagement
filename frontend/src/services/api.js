import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // CORS için gerekli
});

// Her istekte authentication bilgisini ekle
api.interceptors.request.use((config) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  if (user.mail && user.password) {
    // HTTP Basic Authentication
    const auth = btoa(`${user.mail}:${user.password}`);
    config.headers.Authorization = `Basic ${auth}`;
  }
  return config;
});

export const authService = {
  register: async (userData) => {
    try {
      const response = await api.post('/api/auth/register', userData);
      return { success: true, data: response.data };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Registration failed',
      };
    }
  },

  login: async (credentials) => {
    try {
      const response = await api.post('/api/auth/login', credentials, {
        auth: {
          username: credentials.mail,
          password: credentials.password
        }
      });
      
      // Şifreyi de kaydet (Basic Auth için gerekli)
      const userData = {
        ...response.data,
        password: credentials.password // DIKKAT: Güvenlik için sadece development'ta
      };
      
      return { success: true, data: userData };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Login failed. Invalid credentials.',
      };
    }
  },
};

export const taskService = {
  getAllTasks: async () => {
    try {
      const response = await api.get('/api/tasks');
      return { success: true, data: response.data };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to fetch tasks',
      };
    }
  },

  createTask: async (taskData) => {
    try {
      const response = await api.post('/api/tasks', taskData);
      return { success: true, data: response.data };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to create task',
      };
    }
  },
};

export default api;