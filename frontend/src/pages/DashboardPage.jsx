import React from 'react';
import { useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

/**
 * Dashboard Page Placeholder.
 * Used exclusively for authentication and session testing.
 * Phase 6 — Frontend Authentication & Route Guards
 */
export default function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="container" style={{ padding: '3.5rem 1.5rem', flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <div
        className="auth-card"
        style={{
          maxWidth: '560px',
          textAlign: 'left',
          backgroundColor: 'var(--surface)',
          padding: '2.5rem',
          borderRadius: 'var(--radius-xl)',
          border: '1px solid var(--border)',
          boxShadow: 'var(--shadow-md)'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', borderBottom: '1px solid var(--border)', paddingBottom: '1rem' }}>
          <div>
            <span style={{ fontSize: 'var(--font-size-xs)', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--primary)', fontWeight: '700' }}>
              WarrantyHub Portal
            </span>
            <h1 style={{ fontSize: 'var(--font-size-2xl)', fontWeight: '800', color: 'var(--text-primary)', marginTop: '0.25rem' }}>
              Welcome, {user?.name || 'User'}!
            </h1>
          </div>
          <span
            style={{
              padding: '0.35rem 0.75rem',
              borderRadius: 'var(--radius-full)',
              fontSize: 'var(--font-size-xs)',
              fontWeight: '700',
              backgroundColor: user?.role === 'ADMIN' ? '#fef3c7' : 'var(--primary-light)',
              color: user?.role === 'ADMIN' ? '#92400e' : 'var(--primary)',
              border: `1px solid ${user?.role === 'ADMIN' ? '#fde68a' : 'var(--primary-border)'}`
            }}
          >
            {user?.role || 'CUSTOMER'}
          </span>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '2rem' }}>
          <div style={{ background: 'var(--background)', padding: '1rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border)' }}>
            <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>
              Email Address
            </div>
            <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: '600', color: 'var(--text-primary)' }}>
              {user?.email || 'N/A'}
            </div>
          </div>

          <div style={{ background: 'var(--background)', padding: '1rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border)' }}>
            <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>
              Account Identifier (UUID)
            </div>
            <div style={{ fontSize: 'var(--font-size-xs)', fontFamily: 'monospace', color: 'var(--text-secondary)' }}>
              {user?.id || 'N/A'}
            </div>
          </div>
        </div>

        <div
          style={{
            background: 'var(--primary-light)',
            border: '1px solid var(--primary-border)',
            borderRadius: 'var(--radius-md)',
            padding: '1rem',
            fontSize: 'var(--font-size-xs)',
            color: 'var(--primary-active)',
            lineHeight: '1.5',
            marginBottom: '2rem'
          }}
        >
          <strong>Authentication Verification Active:</strong>
          <p style={{ marginTop: '0.25rem' }}>
            You are successfully authenticated via Spring Boot JWT. Product registration, warranty tracking,
            and cloud invoice storage will be enabled in subsequent phases.
          </p>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
          <button
            type="button"
            className="btn btn-secondary btn-md"
            onClick={handleLogout}
          >
            Log Out
          </button>
        </div>
      </div>
    </div>
  );
}
