import api from './api';

/**
 * Product & Warranty API Service.
 * Communicates with backend /api/products endpoints with JWT authentication.
 * Phase 7 — Product Management
 */
export const productService = {
  /**
   * Fetch all products registered by the current authenticated customer.
   * @returns {Promise<Array>} List of ProductResponse objects with nested warranties
   */
  async getProducts() {
    const response = await api.get('/api/products');
    return response.data;
  },

  /**
   * Fetch a single product by its UUID.
   * @param {string} id Product UUID
   * @returns {Promise<Object>} ProductResponse object
   */
  async getProductById(id) {
    const response = await api.get(`/api/products/${id}`);
    return response.data;
  },

  /**
   * Alias for getProductById.
   * @param {string} id Product UUID
   * @returns {Promise<Object>} ProductResponse object
   */
  async getProduct(id) {
    return this.getProductById(id);
  },

  /**
   * Register a new product and trigger automatic warranty creation.
   * @param {Object} productData Product registration payload
   * @returns {Promise<Object>} Created ProductResponse object
   */
  async createProduct(productData) {
    const response = await api.post('/api/products', productData);
    return response.data;
  },

  /**
   * Update an existing product and recalculate warranty terms if applicable.
   * @param {string} id Product UUID
   * @param {Object} productData Updated product fields
   * @returns {Promise<Object>} Updated ProductResponse object
   */
  async updateProduct(id, productData) {
    const response = await api.put(`/api/products/${id}`, productData);
    return response.data;
  },

  /**
   * Delete a registered product and its associated warranty.
   * @param {string} id Product UUID
   * @returns {Promise<void>}
   */
  async deleteProduct(id) {
    await api.delete(`/api/products/${id}`);
  }
};

export default productService;
