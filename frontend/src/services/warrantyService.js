import api from './api';

/**
 * Warranty Lifecycle & Tracking API Service.
 * Communicates with backend /api/warranties and /api/products/{id}/warranty endpoints with JWT authentication.
 * Phase 8 — Warranty Management
 */
export const warrantyService = {
  /**
   * Fetch all warranties belonging to the authenticated customer.
   * @returns {Promise<Array>} List of WarrantyResponse objects with daysRemaining and progressPercentage
   */
  async getWarranties() {
    const response = await api.get('/api/warranties');
    return response.data;
  },

  /**
   * Fetch a single warranty by its UUID.
   * @param {string} id Warranty UUID
   * @returns {Promise<Object>} WarrantyResponse object
   */
  async getWarranty(id) {
    const response = await api.get(`/api/warranties/${id}`);
    return response.data;
  },

  /**
   * Fetch the warranty associated with a specific product ID.
   * @param {string} productId Product UUID
   * @returns {Promise<Object>} WarrantyResponse object
   */
  async getProductWarranty(productId) {
    const response = await api.get(`/api/products/${productId}/warranty`);
    return response.data;
  }
};

export default warrantyService;
