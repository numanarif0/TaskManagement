import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true, // CORS gerekiyorsa
});

// Her istekte Basic Auth ekle (localStorage'daki user'dan)
api.interceptors.request.use((config) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  if (user.mail && user.password) {
    const auth = btoa(`${user.mail}:${user.password}`);
    config.headers.Authorization = `Basic ${auth}`;
  }
  return config;
});

// ---- Auth Service ----
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
      // Bazı Spring Security konfiglerinde 401 almamak için axios'un "auth" alanını kullanmak iyi olur
      const response = await api.post('/api/auth/login', credentials, {
        auth: {
          username: credentials.mail,
          password: credentials.password,
        },
      });

      // Basic için şifreyi geçici olarak saklıyoruz (dev ortamı!)
      const userData = {
        ...response.data,
        mail: credentials.mail,
        password: credentials.password,
      };
      localStorage.setItem('user', JSON.stringify(userData));

      return { success: true, data: userData };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Login failed. Invalid credentials.',
      };
    }
  },

  // İstersen: çıkış yardımcıları
  logout: () => {
    localStorage.removeItem('user');
  },
};

// ---- Task Service ----
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

  // ✅ EDIT (PUT /api/tasks/{id})
  updateTask: async (id, taskData) => {
    try {
      const response = await api.put(`/api/tasks/${id}`, taskData);
      return { success: true, data: response.data };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to update task',
      };
    }
  },

  // ✅ DELETE (DELETE /api/tasks/{id})
  deleteTask: async (id) => {
    try {
      await api.delete(`/api/tasks/${id}`);
      return { success: true, data: null };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to delete task',
      };
    }
  },
};

export default api;
