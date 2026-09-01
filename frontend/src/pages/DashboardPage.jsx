import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import dashboardService from '../services/dashboardService';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';

/**
 * Customer Dashboard Page.
 * Real database-backed KPIs, expiring warranties preview, recent claims preview,
 * and authoritative recent activity timeline.
 * Phase 12 — Dashboard & Notifications
 */
export default function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await dashboardService.getDashboard();
      setDashboard(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load dashboard telemetry. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const formatDate = (dateString) => {
    if (!dateString) return '—';
    try {
      return new Date(dateString).toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    } catch (e) {
      return dateString;
    }
  };

  const formatTimestamp = (dateString) => {
    if (!dateString) return '—';
    try {
      const date = new Date(dateString);
      const now = new Date();
      const diffMs = now - date;
      const diffMins = Math.floor(diffMs / 60000);
      const diffHours = Math.floor(diffMins / 60);
      const diffDays = Math.floor(diffHours / 24);

      if (diffMins < 1) return 'Just now';
      if (diffMins < 60) return `${diffMins}m ago`;
      if (diffHours < 24) return `${diffHours}h ago`;
      if (diffDays === 1) return 'Yesterday';
      if (diffDays < 7) return `${diffDays}d ago`;

      return date.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric'
      });
    } catch (e) {
      return dateString;
    }
  };

  const getClaimBadge = (status) => {
    switch (status) {
      case 'PENDING':
        return { label: 'Pending', color: 'var(--warning, #f59e0b)', bg: 'rgba(245, 158, 11, 0.12)' };
      case 'APPROVED':
        return { label: 'Approved', color: 'var(--primary, #2563eb)', bg: 'rgba(37, 99, 235, 0.12)' };
      case 'IN_PROGRESS':
        return { label: 'In Progress', color: 'var(--info, #0284c7)', bg: 'rgba(2, 132, 199, 0.12)' };
      case 'COMPLETED':
        return { label: 'Completed', color: 'var(--success, #10b981)', bg: 'rgba(16, 185, 129, 0.12)' };
      case 'REJECTED':
      case 'CANCELLED':
        return { label: status, color: 'var(--danger, #ef4444)', bg: 'rgba(239, 68, 68, 0.12)' };
      default:
        return { label: status, color: 'var(--text-secondary, #64748b)', bg: 'rgba(100, 116, 139, 0.12)' };
    }
  };

  const getActivityBadge = (type) => {
    switch (type) {
      case 'PRODUCT':
        return { label: 'Product', color: 'var(--primary, #2563eb)', bg: 'rgba(37, 99, 235, 0.12)' };
      case 'INVOICE':
        return { label: 'Invoice', color: '#8b5cf6', bg: 'rgba(139, 92, 246, 0.12)' };
      case 'CLAIM':
        return { label: 'Claim', color: 'var(--warning, #f59e0b)', bg: 'rgba(245, 158, 11, 0.12)' };
      default:
        return { label: type, color: 'var(--text-secondary, #64748b)', bg: 'rgba(100, 116, 139, 0.12)' };
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '4rem 1.5rem', flex: 1 }}>
        <Loading size="lg" text="Loading dashboard telemetry..." />
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '3rem 1.5rem', flex: 1, maxWidth: '1080px' }}>
      {/* Welcome Banner */}
      <div
        style={{
          background: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)',
          color: '#ffffff',
          borderRadius: 'var(--radius-xl)',
          padding: '2.5rem',
          marginBottom: '2rem',
          boxShadow: 'var(--shadow-md)',
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
              background: 'rgba(37, 99, 235, 0.2)',
              color: '#93c5fd',
              padding: '0.25rem 0.75rem',
              borderRadius: 'var(--radius-full)',
              fontSize: 'var(--font-size-xs)',
              fontWeight: '700',
              textTransform: 'uppercase',
              marginBottom: '0.75rem'
            }}
          >
            Customer Portal • Phase 12 Telemetry
          </div>
          <h1 style={{ fontSize: 'var(--font-size-3xl)', fontWeight: '800', marginBottom: '0.5rem', letterSpacing: '-0.02em' }}>
            Welcome back, {user?.name || 'Customer'}!
          </h1>
          <p style={{ color: '#94a3b8', fontSize: 'var(--font-size-sm)', maxWidth: '540px', lineHeight: '1.5' }}>
            Your centralized equipment protection hub. Monitor warranty lifecycles, file repair claims, view invoices, and stay on top of upcoming expirations.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <Link to="/products/register" className="btn btn-primary btn-md">
            + Register Product
          </Link>
          <Link to="/warranties" className="btn btn-secondary btn-md" style={{ color: '#ffffff', borderColor: '#475569', backgroundColor: '#334155' }}>
            Warranties
          </Link>
          <Link to="/invoices" className="btn btn-secondary btn-md" style={{ color: '#ffffff', borderColor: '#475569', backgroundColor: '#334155' }}>
            Invoices
          </Link>
          <Link to="/claims" className="btn btn-secondary btn-md" style={{ color: '#ffffff', borderColor: '#475569', backgroundColor: '#334155' }}>
            Claims
          </Link>
          <Link to="/notifications" className="btn btn-ghost btn-md" style={{ color: '#93c5fd' }}>
            Inbox
          </Link>
        </div>
      </div>

      {error && (
        <div style={{ marginBottom: '1.5rem' }}>
          <ErrorMessage message={error} />
          <div style={{ marginTop: '0.5rem', textAlign: 'right' }}>
            <button type="button" className="btn btn-secondary btn-sm" onClick={fetchDashboard}>
              Retry
            </button>
          </div>
        </div>
      )}

      {dashboard && (
        <>
          {/* Main KPI Strip */}
          <div className="metrics-strip" style={{ marginBottom: '2rem' }}>
            <div className="metric-card">
              <span className="metric-label">Registered Products</span>
              <span className="metric-value">{dashboard.products?.total || 0}</span>
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                {dashboard.invoices?.total || 0} Invoices Stored
              </span>
            </div>
            <div className="metric-card" style={{ borderLeft: '4px solid var(--success)' }}>
              <span className="metric-label">Active Warranties</span>
              <span className="metric-value" style={{ color: 'var(--success)' }}>
                {dashboard.warranties?.active || 0}
              </span>
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                {dashboard.warranties?.total || 0} Total Policies
              </span>
            </div>
            <div className="metric-card" style={{ borderLeft: '4px solid var(--warning)' }}>
              <span className="metric-label">Expiring Soon (30d)</span>
              <span className="metric-value" style={{ color: 'var(--warning)' }}>
                {dashboard.warranties?.expiringSoon || 0}
              </span>
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                {dashboard.warranties?.expired || 0} Expired
              </span>
            </div>
            <div className="metric-card" style={{ borderLeft: '4px solid var(--primary)' }}>
              <span className="metric-label">Claims Adjudication</span>
              <span className="metric-value" style={{ color: 'var(--primary)' }}>
                {dashboard.claims?.pending || 0}
              </span>
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                {dashboard.claims?.completed || 0} Completed • {dashboard.claims?.total || 0} Total
              </span>
            </div>
          </div>

          {/* 2-Column Section: Expiring Warranties & Recent Claims */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
            {/* Expiring Warranties Preview */}
            <div className="detail-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
                <div>
                  <h2 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '700', margin: 0, color: 'var(--text-primary)' }}>
                    Expiring Warranties
                  </h2>
                  <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                    Action required within next 30 days
                  </span>
                </div>
                <Link to="/warranties" className="btn btn-ghost btn-sm">
                  View All →
                </Link>
              </div>

              {!dashboard.expiringWarranties || dashboard.expiringWarranties.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '2.5rem 1rem', background: 'var(--background)', borderRadius: 'var(--radius-lg)', border: '1px dashed var(--border)' }}>
                  <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>🛡️</div>
                  <p style={{ margin: '0 0 0.25rem', fontSize: 'var(--font-size-sm)', fontWeight: '600', color: 'var(--text-primary)' }}>
                    All warranties up to date
                  </p>
                  <p style={{ margin: 0, fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                    No warranty coverage expiring in the next 30 days.
                  </p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {dashboard.expiringWarranties.slice(0, 5).map((w) => (
                    <div
                      key={w.warrantyId}
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        padding: '0.875rem 1rem',
                        background: 'var(--background)',
                        borderRadius: 'var(--radius-md)',
                        border: '1px solid var(--border)'
                      }}
                    >
                      <div>
                        <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                          {w.brand}
                        </div>
                        <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: '700', color: 'var(--text-primary)' }}>
                          {w.productName}
                        </div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.125rem' }}>
                          Expires: {formatDate(w.expiryDate)}
                        </div>
                      </div>

                      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.375rem' }}>
                        <span
                          style={{
                            fontSize: '0.6875rem',
                            fontWeight: '700',
                            padding: '0.2rem 0.5rem',
                            borderRadius: '9999px',
                            background: w.daysRemaining <= 7 ? 'rgba(239, 68, 68, 0.12)' : 'rgba(245, 158, 11, 0.12)',
                            color: w.daysRemaining <= 7 ? 'var(--danger, #ef4444)' : 'var(--warning, #f59e0b)'
                          }}
                        >
                          {w.daysRemaining <= 0 ? 'Expired' : `${w.daysRemaining}d left`}
                        </span>
                        <Link to={`/warranties/${w.warrantyId}`} className="btn btn-ghost btn-xs" style={{ fontSize: '11px', padding: '0.2rem 0.4rem' }}>
                          Inspect →
                        </Link>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Recent Claims Preview */}
            <div className="detail-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
                <div>
                  <h2 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '700', margin: 0, color: 'var(--text-primary)' }}>
                    Recent Claims
                  </h2>
                  <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                    Latest adjudications & service requests
                  </span>
                </div>
                <Link to="/claims" className="btn btn-ghost btn-sm">
                  View All →
                </Link>
              </div>

              {!dashboard.recentClaims || dashboard.recentClaims.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '2.5rem 1rem', background: 'var(--background)', borderRadius: 'var(--radius-lg)', border: '1px dashed var(--border)' }}>
                  <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>📋</div>
                  <p style={{ margin: '0 0 0.25rem', fontSize: 'var(--font-size-sm)', fontWeight: '600', color: 'var(--text-primary)' }}>
                    No claims submitted
                  </p>
                  <p style={{ margin: 0, fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                    File a claim whenever your equipment experiences an issue.
                  </p>
                  <div style={{ marginTop: '0.75rem' }}>
                    <Link to="/claims" className="btn btn-primary btn-xs">
                      Submit Claim
                    </Link>
                  </div>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {dashboard.recentClaims.slice(0, 5).map((claim) => {
                    const badge = getClaimBadge(claim.status);
                    return (
                      <div
                        key={claim.claimId}
                        style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          padding: '0.875rem 1rem',
                          background: 'var(--background)',
                          borderRadius: 'var(--radius-md)',
                          border: '1px solid var(--border)'
                        }}
                      >
                        <div>
                          <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: '700', color: 'var(--text-primary)' }}>
                            {claim.productName}
                          </div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.125rem' }}>
                            {claim.claimReason}
                          </div>
                          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '0.125rem' }}>
                            {formatTimestamp(claim.createdAt)}
                          </div>
                        </div>

                        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.375rem' }}>
                          <span
                            style={{
                              fontSize: '0.6875rem',
                              fontWeight: '700',
                              padding: '0.2rem 0.5rem',
                              borderRadius: '9999px',
                              background: badge.bg,
                              color: badge.color
                            }}
                          >
                            {badge.label}
                          </span>
                          <Link to={`/claims/${claim.claimId}`} className="btn btn-ghost btn-xs" style={{ fontSize: '11px', padding: '0.2rem 0.4rem' }}>
                            Details →
                          </Link>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

          {/* Recent Activity Timeline */}
          <div className="detail-card" style={{ marginBottom: '2rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <div>
                <h2 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '700', margin: 0, color: 'var(--text-primary)' }}>
                  Recent Account Activity
                </h2>
                <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                  Authoritative timestamped audit trail
                </span>
              </div>
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                {dashboard.recentActivity?.length || 0} events
              </span>
            </div>

            {!dashboard.recentActivity || dashboard.recentActivity.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '2.5rem 1rem', background: 'var(--background)', borderRadius: 'var(--radius-lg)', border: '1px dashed var(--border)' }}>
                <p style={{ margin: 0, fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)' }}>
                  No recent account activity recorded yet.
                </p>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.875rem' }}>
                {dashboard.recentActivity.map((activity, idx) => {
                  const badge = getActivityBadge(activity.activityType);
                  return (
                    <div
                      key={idx}
                      style={{
                        display: 'flex',
                        alignItems: 'flex-start',
                        gap: '1rem',
                        padding: '0.875rem 1rem',
                        background: 'var(--background)',
                        borderRadius: 'var(--radius-md)',
                        border: '1px solid var(--border)'
                      }}
                    >
                      <div
                        style={{
                          fontSize: '0.6875rem',
                          fontWeight: '700',
                          padding: '0.25rem 0.5rem',
                          borderRadius: '0.25rem',
                          background: badge.bg,
                          color: badge.color,
                          textTransform: 'uppercase',
                          flexShrink: 0,
                          marginTop: '0.125rem'
                        }}
                      >
                        {badge.label}
                      </div>

                      <div style={{ flex: 1 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', flexWrap: 'wrap', gap: '0.5rem' }}>
                          <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: '700', color: 'var(--text-primary)' }}>
                            {activity.title}
                          </span>
                          <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                            {formatTimestamp(activity.timestamp)}
                          </span>
                        </div>
                        <p style={{ margin: '0.25rem 0 0', fontSize: 'var(--font-size-xs)', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                          {activity.description}
                        </p>
                      </div>

                      {activity.referenceId && (
                        <div style={{ flexShrink: 0 }}>
                          {activity.activityType === 'PRODUCT' && (
                            <Link to={`/products/${activity.referenceId}`} className="btn btn-ghost btn-xs">
                              View
                            </Link>
                          )}
                          {activity.activityType === 'CLAIM' && (
                            <Link to={`/claims/${activity.referenceId}`} className="btn btn-ghost btn-xs">
                              View
                            </Link>
                          )}
                          {activity.activityType === 'INVOICE' && (
                            <Link to="/invoices" className="btn btn-ghost btn-xs">
                              View
                            </Link>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </>
      )}

      {/* Account Info Footer */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
          padding: '1.25rem',
          background: 'var(--surface)',
          borderRadius: 'var(--radius-lg)',
          border: '1px solid var(--border)'
        }}
      >
        <div>
          <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>Logged in as:</div>
          <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: '600', color: 'var(--text-primary)' }}>
            {user?.name} ({user?.email}) • {user?.role}
          </div>
        </div>
        <button
          type="button"
          className="btn btn-secondary btn-sm"
          onClick={handleLogout}
        >
          Sign Out
        </button>
      </div>
    </div>
  );
}
