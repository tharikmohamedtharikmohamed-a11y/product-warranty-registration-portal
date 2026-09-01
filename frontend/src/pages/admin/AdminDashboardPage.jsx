import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import adminService from '../../services/adminService';
import Loading from '../../components/Loading';
import ErrorMessage from '../../components/ErrorMessage';

/**
 * Admin Dashboard Page.
 * Centralized administration hub displaying real-time platform KPIs,
 * breakdown metrics, and quick links to admin modules.
 * Phase 11 — Admin Management Module
 */
export default function AdminDashboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchStats = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await adminService.getDashboardStats();
      setStats(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load administrative telemetry.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  if (loading) {
    return (
      <div className="container" style={{ padding: '4rem 1.5rem', flex: 1 }}>
        <Loading size="lg" text="Loading platform telemetry..." />
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '3rem 1.5rem', flex: 1, maxWidth: '1120px' }}>
      {/* Header Banner */}
      <div
        style={{
          background: 'linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%)',
          color: '#ffffff',
          borderRadius: 'var(--radius-xl)',
          padding: '2.5rem',
          marginBottom: '2rem',
          boxShadow: 'var(--shadow-lg)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1.5rem'
        }}
      >
        <div>
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.5rem',
              background: 'rgba(239, 68, 68, 0.2)',
              color: '#fca5a5',
              padding: '0.25rem 0.75rem',
              borderRadius: 'var(--radius-full)',
              fontSize: 'var(--font-size-xs)',
              fontWeight: '700',
              textTransform: 'uppercase',
              marginBottom: '0.75rem'
            }}
          >
            🛡️ Administrative Control Center • Phase 11
          </div>
          <h1 style={{ fontSize: 'var(--font-size-3xl)', fontWeight: '800', marginBottom: '0.5rem', letterSpacing: '-0.02em' }}>
            System Operations Dashboard
          </h1>
          <p style={{ color: '#cbd5e1', fontSize: 'var(--font-size-sm)', maxWidth: '580px', lineHeight: '1.5' }}>
            Authoritative platform-wide telemetry, user management, equipment registries, invoice assets, and warranty claim adjudication queue.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <button
            type="button"
            className="btn btn-secondary btn-md"
            onClick={fetchStats}
            style={{ color: '#ffffff', borderColor: '#475569', backgroundColor: '#334155' }}
          >
            ↻ Refresh Telemetry
          </button>
          <Link to="/admin/claims" className="btn btn-primary btn-md">
            Adjudicate Claims →
          </Link>
        </div>
      </div>

      {error && (
        <div style={{ marginBottom: '1.5rem' }}>
          <ErrorMessage message={error} />
        </div>
      )}

      {stats && (
        <>
          {/* Main KPI Strip */}
          <div className="metrics-strip" style={{ marginBottom: '2rem' }}>
            <div className="metric-card">
              <span className="metric-label">Total Users</span>
              <span className="metric-value">{stats.users}</span>
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                {stats.customers} Customers • {stats.admins} Admins
              </span>
            </div>
            <div className="metric-card" style={{ borderLeft: '4px solid var(--primary)' }}>
              <span className="metric-label">Registered Products</span>
              <span className="metric-value" style={{ color: 'var(--primary)' }}>{stats.products}</span>
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                Across all accounts
              </span>
            </div>
            <div className="metric-card" style={{ borderLeft: '4px solid var(--success)' }}>
              <span className="metric-label">Active Warranties</span>
              <span className="metric-value" style={{ color: 'var(--success)' }}>{stats.activeWarranties}</span>
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                {stats.expiringSoonWarranties} expiring soon
              </span>
            </div>
            <div className="metric-card" style={{ borderLeft: '4px solid var(--warning)' }}>
              <span className="metric-label">Pending Claims</span>
              <span className="metric-value" style={{ color: 'var(--warning)' }}>{stats.pendingClaims}</span>
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                Awaiting triage
              </span>
            </div>
          </div>

          {/* Granular Management Category Cards */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
            {/* Warranty Claims Queue Card */}
            <div className="detail-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
                <h2 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '700', margin: 0 }}>
                  Claims Adjudication Queue
                </h2>
                <Link to="/admin/claims" className="btn btn-ghost btn-sm">
                  View All ({stats.claims}) →
                </Link>
              </div>
              <div className="spec-list">
                <div className="spec-item">
                  <span className="meta-label">Pending Triage</span>
                  <span className="status-badge badge-pending">{stats.pendingClaims} PENDING</span>
                </div>
                <div className="spec-item">
                  <span className="meta-label">Approved & Queued</span>
                  <span className="status-badge badge-active">{stats.approvedClaims} APPROVED</span>
                </div>
                <div className="spec-item">
                  <span className="meta-label">In Service / Progress</span>
                  <span className="status-badge badge-expiring">{stats.inProgressClaims} IN PROGRESS</span>
                </div>
                <div className="spec-item">
                  <span className="meta-label">Successfully Resolved</span>
                  <span className="status-badge badge-active">{stats.completedClaims} COMPLETED</span>
                </div>
                <div className="spec-item">
                  <span className="meta-label">Rejected</span>
                  <span className="status-badge badge-expired">{stats.rejectedClaims} REJECTED</span>
                </div>
                <div className="spec-item">
                  <span className="meta-label">Customer Cancelled</span>
                  <span className="status-badge badge-expired">{stats.cancelledClaims} CANCELLED</span>
                </div>
              </div>
              <div style={{ marginTop: '1.25rem', paddingTop: '1rem', borderTop: '1px solid var(--border)' }}>
                <Link to="/admin/claims" className="btn btn-primary btn-sm" style={{ width: '100%', textAlign: 'center' }}>
                  Open Claims Management Queue
                </Link>
              </div>
            </div>

            {/* Warranty Portfolio Overview Card */}
            <div className="detail-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
                <h2 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '700', margin: 0 }}>
                  Warranties Portfolio
                </h2>
                <Link to="/admin/warranties" className="btn btn-ghost btn-sm">
                  View All ({stats.warranties}) →
                </Link>
              </div>
              <div className="spec-list">
                <div className="spec-item">
                  <span className="meta-label">Fully Active Coverage</span>
                  <span style={{ fontWeight: '700', color: 'var(--success)' }}>{stats.activeWarranties}</span>
                </div>
                <div className="spec-item">
                  <span className="meta-label">Expiring Within 30 Days</span>
                  <span style={{ fontWeight: '700', color: 'var(--warning)' }}>{stats.expiringSoonWarranties}</span>
                </div>
                <div className="spec-item">
                  <span className="meta-label">Expired Coverage</span>
                  <span style={{ fontWeight: '700', color: 'var(--danger)' }}>{stats.expiredWarranties}</span>
                </div>
                <div className="spec-item">
                  <span className="meta-label">Total Provisioned Records</span>
                  <span className="meta-value">{stats.warranties}</span>
                </div>
              </div>
              <div style={{ marginTop: '1.25rem', paddingTop: '1rem', borderTop: '1px solid var(--border)' }}>
                <Link to="/admin/warranties" className="btn btn-secondary btn-sm" style={{ width: '100%', textAlign: 'center' }}>
                  Inspect Warranties Portfolio
                </Link>
              </div>
            </div>

            {/* User & Asset Directory Card */}
            <div className="detail-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
                <h2 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '700', margin: 0 }}>
                  User & Storage Directory
                </h2>
                <Link to="/admin/users" className="btn btn-ghost btn-sm">
                  View Users ({stats.users}) →
                </Link>
              </div>
              <div className="spec-list">
                <div className="spec-item">
                  <span className="meta-label">Customer Accounts</span>
                  <span className="meta-value" style={{ fontWeight: '700' }}>{stats.customers}</span>
                </div>
                <div className="spec-item">
                  <span className="meta-label">System Administrators</span>
                  <span className="meta-value" style={{ fontWeight: '700', color: 'var(--primary)' }}>{stats.admins}</span>
                </div>
                <div className="spec-item">
                  <span className="meta-label">Registered Products</span>
                  <span className="meta-value" style={{ fontWeight: '700' }}>{stats.products}</span>
                </div>
                <div className="spec-item">
                  <span className="meta-label">Uploaded Invoices</span>
                  <span className="meta-value" style={{ fontWeight: '700' }}>{stats.invoices}</span>
                </div>
              </div>
              <div style={{ marginTop: '1.25rem', paddingTop: '1rem', borderTop: '1px solid var(--border)', display: 'flex', gap: '0.5rem' }}>
                <Link to="/admin/products" className="btn btn-secondary btn-sm" style={{ flex: 1, textAlign: 'center' }}>
                  Products
                </Link>
                <Link to="/admin/invoices" className="btn btn-secondary btn-sm" style={{ flex: 1, textAlign: 'center' }}>
                  Invoices
                </Link>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
