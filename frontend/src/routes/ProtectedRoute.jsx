import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import Loading from '../components/Loading';

/**
 * ProtectedRoute Component.
 * Guards private routes, requiring active authentication.
 * Phase 6 — Frontend Authentication & Route Guards
 */
export default function ProtectedRoute() {
  const { isAuthenticated, loading } = useAuth();

  // Show loading spinner while checking local token against backend
  if (loading) {
    return (
      <div style={{ minHeight: '60vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Loading size="lg" text="Checking authentication..." />
      </div>
    );
  }

  // Redirect unauthenticated visitors to /login
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
