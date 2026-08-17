import api from './api';

export const invoiceService = {
  /**
   * Fetch all invoices for the logged-in user
   * Endpoint: GET /api/invoices
   */
  getUserInvoices: async () => {
    try {
      const response = await api.get('/api/invoices');
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load invoices.';
      throw new Error(message);
    }
  },

  /**
   * Fetch invoices for a specific product by filtering user invoices
   */
  getInvoicesByProductId: async (productId) => {
    try {
      const response = await api.get('/api/invoices');
      const allInvoices = response.data || [];
      return allInvoices.filter((inv) => String(inv.productId) === String(productId));
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load product invoices.';
      throw new Error(message);
    }
  },

  /**
   * Fetch a single invoice by ID
   * Endpoint: GET /api/invoices/{id}
   */
  getInvoiceById: async (id) => {
    try {
      const response = await api.get(`/api/invoices/${id}`);
      return response.data;
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to load invoice details.';
      throw new Error(message);
    }
  },

  /**
   * Upload an invoice file for a product
   * Endpoint: POST /api/invoices/upload (multipart/form-data)
   * Parameters: productId (UUID), file (MultipartFile)
   */
  uploadInvoice: async (productId, file) => {
    try {
      const formData = new FormData();
      formData.append('productId', productId);
      formData.append('file', file);

      const response = await api.post('/api/invoices/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    } catch (error) {
      if (error.response?.status === 413) {
        throw new Error('The invoice file is too large. Maximum size is 10 MB.');
      } else if (error.response?.status === 415) {
        throw new Error('This file type is not supported. Allowed formats: PDF, JPG, PNG.');
      } else if (error.response?.status === 400) {
        throw new Error(error.response?.data?.message || 'Please select a valid invoice file.');
      } else if (error.response?.status === 401) {
        throw new Error('Your session has expired. Please login again.');
      } else if (error.response?.status === 403) {
        throw new Error('You are not authorized to upload an invoice for this product.');
      }
      const message = error.response?.data?.message || 'Unable to upload invoice. Please try again.';
      throw new Error(message);
    }
  },

  /**
   * Download / View invoice
   * Endpoint: GET /api/invoices/{id}/download
   * Response: { invoiceId, fileName, fileType, downloadUrl, expiresInSeconds }
   */
  getInvoiceDownload: async (id) => {
    try {
      const response = await api.get(`/api/invoices/${id}/download`);
      return response.data;
    } catch (error) {
      if (error.response?.status === 401) {
        throw new Error('Your session has expired. Please login again.');
      } else if (error.response?.status === 403) {
        throw new Error('You are not authorized to access this invoice.');
      }
      const message = error.response?.data?.message || 'Unable to download invoice. Please try again.';
      throw new Error(message);
    }
  },

  /**
   * Delete an invoice
   * Endpoint: DELETE /api/invoices/{id}
   */
  deleteInvoice: async (id) => {
    try {
      const response = await api.delete(`/api/invoices/${id}`);
      return response.data;
    } catch (error) {
      if (error.response?.status === 401) {
        throw new Error('Your session has expired. Please login again.');
      } else if (error.response?.status === 403) {
        throw new Error('You are not authorized to delete this invoice.');
      }
      const message = error.response?.data?.message || 'Unable to delete invoice. Please try again.';
      throw new Error(message);
    }
  },
};
