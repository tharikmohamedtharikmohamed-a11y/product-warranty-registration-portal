import api, { TOKEN_STORAGE_KEY } from './api';

/**
 * Authentication Service
 * Interacts with Spring Boot Phase 4 authentication REST endpoints.
 * Phase 6 — Frontend Authentication & Route Guards
 */

/**
 * Register a new customer.
 * POST /api/auth/register
 *
 * @param {Object} data
 * @param {string} data.name
 * @param {string} data.email
 * @param {string} data.password
 * @returns {Promise<Object>} Safe user response payload
 */
export const registerUser = async ({ name, email, password }) => {
  const response = await api.post('/api/auth/register', {
    name: name.trim(),
    email: email.trim().toLowerCase(),
    password
  });
  return response.data;
};

/**
 * Log in an existing user with email and password.
 * POST /api/auth/login
 *
 * @param {Object} credentials
 * @param {string} credentials.email
 * @param {string} credentials.password
 * @returns {Promise<Object>} Response containing token and user profile
 */
export const loginUser = async ({ email, password }) => {
  const response = await api.post('/api/auth/login', {
    email: email.trim().toLowerCase(),
    password
  });
  return response.data;
};

/**
 * Retrieve authenticated user profile.
 * GET /api/auth/me
 *
 * @returns {Promise<Object>} Safe user profile (id, name, email, role)
 */
export const getCurrentUser = async () => {
  const response = await api.get('/api/auth/me');
  return response.data;
};

/**
 * Store JWT access token in localStorage.
 *
 * @param {string} token
 */
export const setStoredToken = (token) => {
  try {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  } catch (e) {
    // Storage access error handling
  }
};

/**
 * Retrieve stored JWT token.
 *
 * @returns {string|null}
 */
export const getStoredToken = () => {
  try {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  } catch (e) {
    return null;
  }
};

/**
 * Remove stored JWT token.
 */
export const removeStoredToken = () => {
  try {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  } catch (e) {
    // Storage access error handling
  }
};
