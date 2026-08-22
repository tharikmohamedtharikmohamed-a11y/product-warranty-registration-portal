import React, { createContext, useState, useEffect, useCallback } from 'react';
import {
  registerUser as apiRegister,
  loginUser as apiLogin,
  getCurrentUser as apiGetCurrentUser,
  setStoredToken,
  getStoredToken,
  removeStoredToken
} from '../services/authService';

export const AuthContext = createContext(null);

/**
 * Authentication Context Provider.
 * Provides global authentication state and methods across the application.
 * Phase 6 — Frontend Authentication & Route Guards
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Restore authenticated session on application mount
  const restoreAuth = useCallback(async () => {
    const token = getStoredToken();
    if (!token) {
      setUser(null);
      setLoading(false);
      return;
    }

    try {
      const currentUser = await apiGetCurrentUser();
      setUser(currentUser);
    } catch (error) {
      // If token is expired or invalid, purge from localStorage
      removeStoredToken();
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    restoreAuth();
  }, [restoreAuth]);

  /**
   * Log in user with credentials and store token.
   *
   * @param {string} email
   * @param {string} password
   * @returns {Promise<Object>} The authenticated user object
   */
  const login = async (email, password) => {
    const data = await apiLogin({ email, password });
    if (data && data.token) {
      setStoredToken(data.token);
      setUser(data.user);
      return data.user;
    }
    throw new Error('Authentication succeeded but no access token was returned.');
  };

  /**
   * Register a new customer account.
   *
   * @param {string} name
   * @param {string} email
   * @param {string} password
   * @returns {Promise<Object>} Safe registration response
   */
  const register = async (name, email, password) => {
    return await apiRegister({ name, email, password });
  };

  /**
   * Log out the current user and clear local session state.
   */
  const logout = () => {
    removeStoredToken();
    setUser(null);
  };

  /**
   * Refresh current user profile from the backend.
   */
  const refreshUser = async () => {
    try {
      const currentUser = await apiGetCurrentUser();
      setUser(currentUser);
      return currentUser;
    } catch (error) {
      logout();
      throw error;
    }
  };

  const value = {
    user,
    loading,
    isAuthenticated: Boolean(user),
    login,
    register,
    logout,
    refreshUser
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
