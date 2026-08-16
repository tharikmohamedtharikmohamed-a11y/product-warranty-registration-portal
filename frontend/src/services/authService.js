import api from './api';

export const authService = {
  /**
   * Register a new user
   * DTO: { name, email, password, phone }
   */
  register: async (userData) => {
    try {
      const response = await api.post('/api/auth/register', userData);
      return response.data;
    } catch (error) {
      const message =
        error.response?.data?.message ||
        (error.response?.status === 409
          ? 'Email already registered'
          : 'Unable to connect to server. Please try again.');
      throw new Error(message);
    }
  },

  /**
   * Login user
   * DTO: { email, password }
   * Response: { token, tokenType: "Bearer", user: { id, name, email, phone, role } }
   */
  login: async (credentials) => {
    try {
      const response = await api.post('/api/auth/login', credentials);
      return response.data;
    } catch (error) {
      const message =
        error.response?.data?.message ||
        (error.response?.status === 401
          ? 'Invalid email or password'
          : 'Unable to connect to server. Please try again.');
      throw new Error(message);
    }
  },
};
