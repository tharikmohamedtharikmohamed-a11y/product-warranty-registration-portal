import api from './api';

export const adminService = {
  /**
   * Fetch system dashboard statistics
   * Endpoint: GET /api/admin/dashboard/stats
   */
  getDashboardStats: async () => {
    try {
      const response = await api.get('/api/admin/dashboard/stats');
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load admin dashboard statistics';
      throw new Error(message);
    }
  },

  /**
   * Fetch all warranty claims in the system
   * Endpoint: GET /api/admin/claims
   */
  getAllClaims: async () => {
    try {
      const response = await api.get('/api/admin/claims');
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load warranty claims';
      throw new Error(message);
    }
  },

  /**
   * Fetch specific claim by ID
   * Endpoint: GET /api/admin/claims/{id}
   */
  getClaimById: async (id) => {
    try {
      const response = await api.get(`/api/admin/claims/${id}`);
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load claim details';
      throw new Error(message);
    }
  },

  /**
   * Update claim status and resolution notes
   * Endpoint: PUT /api/admin/claims/{id}/status
   * Payload: { status, resolutionNotes }
   */
  updateClaimStatus: async (id, statusData) => {
    try {
      const response = await api.put(`/api/admin/claims/${id}/status`, statusData);
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to update claim status';
      throw new Error(message);
    }
  },

  /**
   * Fetch all registered users
   * Endpoint: GET /api/admin/users
   */
  getAllUsers: async () => {
    try {
      const response = await api.get('/api/admin/users');
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load user list';
      throw new Error(message);
    }
  },

  /**
   * Fetch all products across all users
   * Endpoint: GET /api/admin/products
   */
  getAllProducts: async () => {
    try {
      const response = await api.get('/api/admin/products');
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load product list';
      throw new Error(message);
    }
  },

  /**
   * Fetch all warranties across all users
   * Endpoint: GET /api/admin/warranties
   */
  getAllWarranties: async () => {
    try {
      const response = await api.get('/api/admin/warranties');
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load warranty list';
      throw new Error(message);
    }
  },

  /**
   * Fetch all invoices uploaded in system
   * Endpoint: GET /api/admin/invoices
   */
  getAllInvoices: async () => {
    try {
      const response = await api.get('/api/admin/invoices');
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load invoice list';
      throw new Error(message);
    }
  },

  /**
   * Get signed URL to download/view an invoice
   * Endpoint: GET /api/admin/invoices/{id}/download
   */
  getInvoiceDownload: async (id) => {
    try {
      const response = await api.get(`/api/admin/invoices/${id}/download`);
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to generate download link';
      throw new Error(message);
    }
  },
};
