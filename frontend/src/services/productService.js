import api from './api';

export const productService = {
  /**
   * Fetch all products registered for the logged-in user
   * Endpoint: GET /api/products
   */
  getUserProducts: async () => {
    try {
      const response = await api.get('/api/products');
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load products';
      throw new Error(message);
    }
  },

  /**
   * Fetch a specific product by ID
   * Endpoint: GET /api/products/{id}
   */
  getProductById: async (id) => {
    try {
      const response = await api.get(`/api/products/${id}`);
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load product details';
      throw new Error(message);
    }
  },
};
