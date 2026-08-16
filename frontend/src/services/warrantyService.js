import api from './api';

export const warrantyService = {
  /**
   * Fetch all warranties for the logged-in user
   * Endpoint: GET /api/warranties
   */
  getUserWarranties: async () => {
    try {
      const response = await api.get('/api/warranties');
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load warranties';
      throw new Error(message);
    }
  },

  /**
   * Fetch a specific warranty by ID
   * Endpoint: GET /api/warranties/{id}
   */
  getWarrantyById: async (id) => {
    try {
      const response = await api.get(`/api/warranties/${id}`);
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load warranty details';
      throw new Error(message);
    }
  },

  /**
   * Fetch warranty attached to a specific product
   * Endpoint: GET /api/products/{productId}/warranty
   */
  getWarrantyByProductId: async (productId) => {
    try {
      const response = await api.get(`/api/products/${productId}/warranty`);
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load product warranty';
      throw new Error(message);
    }
  },
};
