import api from './api';

/**
 * Health service to verify connectivity with Spring Boot backend.
 * Calls GET /api/health
 */
export const checkBackendHealth = async () => {
  try {
    const response = await api.get('/api/health');
    return {
      success: true,
      data: response.data
    };
  } catch (error) {
    return {
      success: false,
      error: error.message || 'Unable to connect to backend server'
    };
  }
};
