import api from './api';

/**
 * Invoice API Client Service.
 * Coordinates invoice upload, retrieval, download, preview, and deletion.
 * All operations interact securely with Spring Boot backend endpoints.
 * Phase 9 — Invoice Management
 */
const invoiceService = {
  /**
   * Uploads a proof-of-purchase invoice document for a product.
   * @param {string} productId - UUID of the product
   * @param {File} file - PDF, JPG, JPEG, or PNG file (up to 10 MB)
   * @returns {Promise<Object>} InvoiceResponse
   */
  uploadInvoice: async (productId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('productId', productId);

    const response = await api.post('/api/invoices/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  /**
   * Retrieves all invoices belonging to the authenticated customer.
   * @returns {Promise<Array>} Array of InvoiceResponse
   */
  getInvoices: async () => {
    const response = await api.get('/api/invoices');
    return response.data;
  },

  /**
   * Retrieves a single invoice metadata record by ID.
   * @param {string} id - Invoice UUID
   * @returns {Promise<Object>} InvoiceResponse
   */
  getInvoiceById: async (id) => {
    const response = await api.get(`/api/invoices/${id}`);
    return response.data;
  },

  /**
   * Retrieves the latest invoice metadata associated with a specific product.
   * @param {string} productId - Product UUID
   * @returns {Promise<Object>} InvoiceResponse
   */
  getProductInvoice: async (productId) => {
    const response = await api.get(`/api/products/${productId}/invoice`);
    return response.data;
  },

  /**
   * Downloads the original invoice binary file and triggers browser download.
   * @param {string} id - Invoice UUID
   * @param {string} fileName - File name to save as
   */
  downloadInvoice: async (id, fileName) => {
    const response = await api.get(`/api/invoices/${id}/download`, {
      responseType: 'blob',
    });
    const contentType = response.headers['content-type'] || 'application/octet-stream';
    const blob = new Blob([response.data], { type: contentType });
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = fileName || 'invoice.pdf';
    document.body.appendChild(link);
    link.click();
    link.remove();
    setTimeout(() => window.URL.revokeObjectURL(downloadUrl), 1000);
  },

  /**
   * Fetches the invoice binary and opens it in a new browser tab for inline preview.
   * @param {string} id - Invoice UUID
   */
  viewInvoice: async (id) => {
    const response = await api.get(`/api/invoices/${id}/view`, {
      responseType: 'blob',
    });
    const contentType = response.headers['content-type'] || 'application/pdf';
    const blob = new Blob([response.data], { type: contentType });
    const fileUrl = window.URL.createObjectURL(blob);
    window.open(fileUrl, '_blank', 'noopener,noreferrer');
    setTimeout(() => window.URL.revokeObjectURL(fileUrl), 60000);
  },

  /**
   * Deletes an invoice from private Supabase Storage and PostgreSQL metadata.
   * @param {string} id - Invoice UUID
   */
  deleteInvoice: async (id) => {
    await api.delete(`/api/invoices/${id}`);
  },
};

export default invoiceService;
