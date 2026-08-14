import api from './api';

export const claimService = {
  /**
   * Fetch all claims for the logged-in user
   * Endpoint: GET /api/claims
   */
  getUserClaims: async () => {
    try {
      const response = await api.get('/api/claims');
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load warranty claims';
      throw new Error(message);
    }
  },

  /**
   * Fetch a specific warranty claim by ID
   * Endpoint: GET /api/claims/{id}
   */
  getClaimById: async (id) => {
    try {
      const response = await api.get(`/api/claims/${id}`);
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load claim details';
      throw new Error(message);
    }
  },

  /**
   * Submit a new warranty claim
   * Endpoint: POST /api/claims
   * Payload: { productId, warrantyId, invoiceId (optional), issueDescription }
   */
  submitClaim: async (claimData) => {
    try {
      const response = await api.post('/api/claims', claimData);
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to submit warranty claim';
      throw new Error(message);
    }
  },

  /**
   * Cancel an active warranty claim
   * Endpoint: PUT /api/claims/{id}/cancel
   */
  cancelClaim: async (id) => {
    try {
      const response = await api.put(`/api/claims/${id}/cancel`);
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to cancel warranty claim';
      throw new Error(message);
    }
  },
};
