import api from './api';

/**
 * Claim API Client Service.
 * Coordinates warranty claim submission, customer claim queries,
 * product claim listing, and cancellation transitions.
 * Phase 10 — Warranty Claims
 */
const claimService = {
  /**
   * Submits a new warranty claim for an authenticated customer's product.
   * @param {Object} claimData - { productId, claimReason, description }
   * @returns {Promise<Object>} ClaimResponse
   */
  createClaim: async (claimData) => {
    const response = await api.post('/api/claims', claimData);
    return response.data;
  },

  /**
   * Retrieves all warranty claims filed by the authenticated customer.
   * @returns {Promise<Array>} Array of ClaimResponse
   */
  getClaims: async () => {
    const response = await api.get('/api/claims');
    return response.data;
  },

  /**
   * Retrieves full details for a single warranty claim.
   * @param {string} id - Claim UUID
   * @returns {Promise<Object>} ClaimResponse
   */
  getClaimById: async (id) => {
    const response = await api.get(`/api/claims/${id}`);
    return response.data;
  },

  /**
   * Cancels an eligible pending claim.
   * @param {string} id - Claim UUID
   * @returns {Promise<Object>} Updated ClaimResponse
   */
  cancelClaim: async (id) => {
    const response = await api.patch(`/api/claims/${id}/cancel`);
    return response.data;
  },

  /**
   * Retrieves all warranty claims submitted for a specific product.
   * @param {string} productId - Product UUID
   * @returns {Promise<Array>} Array of ClaimResponse
   */
  getProductClaims: async (productId) => {
    const response = await api.get(`/api/products/${productId}/claims`);
    return response.data;
  },
};

export default claimService;
