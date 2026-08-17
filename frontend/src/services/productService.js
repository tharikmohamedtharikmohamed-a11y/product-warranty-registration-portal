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

  /**
   * Register a new product
   * Endpoint: POST /api/products
   */
  registerProduct: async (productData) => {
    try {
      const response = await api.post('/api/products', productData);
      return response.data;
    } catch (error) {
      if (error.response?.status === 409) {
        throw new Error(error.response?.data?.message || 'Product with this serial number already exists.');
      } else if (error.response?.status === 400) {
        throw new Error(error.response?.data?.message || 'Please check the product information.');
      } else if (error.response?.status === 401) {
        throw new Error('Your session has expired. Please login again.');
      } else if (error.response?.status === 403) {
        throw new Error('You are not authorized to register this product.');
      }
      const message = error.response?.data?.message || 'Unable to register product. Please try again.';
      throw new Error(message);
    }
  },

  /**
   * Update an existing product
   * Endpoint: PUT /api/products/{id}
   */
  updateProduct: async (id, productData) => {
    try {
      const response = await api.put(`/api/products/${id}`, productData);
      return response.data;
    } catch (error) {
      if (error.response?.status === 409) {
        throw new Error(error.response?.data?.message || 'Product with this serial number already exists.');
      } else if (error.response?.status === 400) {
        throw new Error(error.response?.data?.message || 'Please check the product information.');
      } else if (error.response?.status === 401) {
        throw new Error('Your session has expired. Please login again.');
      } else if (error.response?.status === 403) {
        throw new Error('You are not authorized to update this product.');
      }
      const message = error.response?.data?.message || 'Unable to update product. Please try again.';
      throw new Error(message);
    }
  },

  /**
   * Delete a product
   * Endpoint: DELETE /api/products/{id}
   */
  deleteProduct: async (id) => {
    try {
      const response = await api.delete(`/api/products/${id}`);
      return response.data;
    } catch (error) {
      if (error.response?.status === 401) {
        throw new Error('Your session has expired. Please login again.');
      } else if (error.response?.status === 403) {
        throw new Error('You are not authorized to delete this product.');
      }
      const message = error.response?.data?.message || 'Unable to delete product. Please try again.';
      throw new Error(message);
    }
  },
};
