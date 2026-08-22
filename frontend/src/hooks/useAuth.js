import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

/**
 * Custom hook to access authentication context state and actions.
 *
 * @returns {{
 *   user: Object|null,
 *   loading: boolean,
 *   isAuthenticated: boolean,
 *   login: Function,
 *   register: Function,
 *   logout: Function,
 *   refreshUser: Function
 * }}
 */
export default function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
