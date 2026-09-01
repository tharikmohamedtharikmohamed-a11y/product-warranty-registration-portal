import api from './api';

/**
 * Service for administrative operations.
 * Connects to /api/admin/** endpoints with Bearer authentication.
 * Phase 11 — Admin Management Module
 */
const adminService = {
  /**
   * Fetches global platform KPI statistics.
   */
  getDashboardStats: async () => {
    const response = await api.get('/api/admin/dashboard/stats');
    return response.data;
  },

  /**
   * Fetches all registered users, with optional search term.
   */
  getUsers: async (search) => {
    const params = search ? { search } : {};
    const response = await api.get('/api/admin/users', { params });
    return response.data;
  },

  /**
   * Fetches all registered products across all customers.
   */
  getProducts: async () => {
    const response = await api.get('/api/admin/products');
    return response.data;
  },

  /**
   * Fetches all warranties across all customers with live status and countdowns.
   */
  getWarranties: async () => {
    const response = await api.get('/api/admin/warranties');
    return response.data;
  },

  /**
   * Fetches all purchase invoices across all customers.
   */
  getInvoices: async () => {
    const response = await api.get('/api/admin/invoices');
    return response.data;
  },

  /**
   * Downloads an invoice file as an administrator.
   */
  downloadInvoice: async (invoiceId, fileName) => {
    const response = await api.get(`/api/admin/invoices/${invoiceId}/download`, {
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName || 'invoice-document');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  /**
   * Opens an invoice in a new browser tab for inline preview.
   */
  viewInvoice: async (invoiceId) => {
    const response = await api.get(`/api/admin/invoices/${invoiceId}/view`, {
      responseType: 'blob'
    });
    const contentType = response.headers['content-type'] || 'application/pdf';
    const blob = new Blob([response.data], { type: contentType });
    const url = window.URL.createObjectURL(blob);
    window.open(url, '_blank', 'noopener,noreferrer');
    setTimeout(() => window.URL.revokeObjectURL(url), 60000);
  },

  /**
   * Fetches all warranty claims submitted across the platform.
   */
  getClaims: async () => {
    const response = await api.get('/api/admin/claims');
    return response.data;
  },

  /**
   * Fetches single claim details by ID.
   */
  getClaimById: async (id) => {
    const response = await api.get(`/api/admin/claims/${id}`);
    return response.data;
  },

  /**
   * Approves a PENDING warranty claim.
   */
  approveClaim: async (id, adminNotes) => {
    const payload = adminNotes ? { adminNotes } : {};
    const response = await api.patch(`/api/admin/claims/${id}/approve`, payload);
    return response.data;
  },

  /**
   * Rejects a PENDING warranty claim.
   */
  rejectClaim: async (id, adminNotes) => {
    const payload = adminNotes ? { adminNotes } : {};
    const response = await api.patch(`/api/admin/claims/${id}/reject`, payload);
    return response.data;
  },

  /**
   * Moves an APPROVED claim to IN_PROGRESS.
   */
  startClaim: async (id, adminNotes) => {
    const payload = adminNotes ? { adminNotes } : {};
    const response = await api.patch(`/api/admin/claims/${id}/start`, payload);
    return response.data;
  },

  /**
   * Marks an IN_PROGRESS claim as COMPLETED.
   */
  completeClaim: async (id, adminNotes) => {
    const payload = adminNotes ? { adminNotes } : {};
    const response = await api.patch(`/api/admin/claims/${id}/complete`, payload);
    return response.data;
  }
};

export default adminService;
