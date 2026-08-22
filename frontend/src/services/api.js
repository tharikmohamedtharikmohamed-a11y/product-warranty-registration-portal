import axios from 'axios';

/**
 * Reusable Axios HTTP client.
 * Configured with backend base URL from VITE_API_BASE_URL.
 * Includes JWT request interceptor and 401 response handling.
 * Phase 6 — Frontend Authentication & Route Guards
 */
export const TOKEN_STORAGE_KEY = 'warrantyhub_token';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  timeout: 10000
});

// Request Interceptor: Attach Bearer JWT token if present in localStorage
api.interceptors.request.use(
  (config) => {
    try {
      const token = localStorage.getItem(TOKEN_STORAGE_KEY);
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    } catch (e) {
      // Ignore localStorage access errors if any
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Catch 401 Unauthorized on protected routes
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const originalRequest = error.config;
    const isAuthEndpoint =
      originalRequest?.url?.includes('/api/auth/login') ||
      originalRequest?.url?.includes('/api/auth/register');

    // If 401 is returned on a protected endpoint (not login or register), clear token
    if (error.response && error.response.status === 401 && !isAuthEndpoint) {
      try {
        localStorage.removeItem(TOKEN_STORAGE_KEY);
      } catch (e) {
        // Ignore storage removal errors
      }

      // Avoid infinite redirects if already on /login or /register
      const currentPath = window.location.pathname;
      if (currentPath !== '/login' && currentPath !== '/register') {
        window.location.href = '/login';
      }
    }

    return Promise.reject(error);
  }
);

export default api;
