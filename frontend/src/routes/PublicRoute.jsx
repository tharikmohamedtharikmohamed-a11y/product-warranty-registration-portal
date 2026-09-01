import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import Loading from '../components/Loading';

/**
 * PublicRoute Component.
 * Guards public authentication pages (/login, /register).
 * If user is already authenticated, redirects them to /dashboard.
 * Phase 6 — Frontend Authentication & Route Guards
 */
export default function PublicRoute() {
  const { user, isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ minHeight: '60vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Loading size="lg" text="Checking authentication..." />
      </div>
    );
  }

  // If already authenticated, redirect based on role
  if (isAuthenticated) {
    return user?.role === 'ADMIN' ? <Navigate to="/admin" replace /> : <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}
