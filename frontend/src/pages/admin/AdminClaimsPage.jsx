import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import adminService from '../../services/adminService';
import Loading from '../../components/Loading';
import ErrorMessage from '../../components/ErrorMessage';

/**
 * Admin Claims Management Page.
 * Central adjudication queue displaying all customer warranty claims across the platform,
 * status filtering, and navigation to granular decision processing.
 * Phase 11 — Admin Management Module
 */
export default function AdminClaimsPage() {
  const [claims, setClaims] = useState([]);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchClaims = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await adminService.getClaims();
      setClaims(data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load claims queue.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClaims();
  }, []);

  const formatDate = (isoString) => {
    if (!isoString) return '—';
    try {
      return new Date(isoString).toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    } catch {
      return isoString;
    }
  };

  const filteredClaims = claims.filter((c) => {
    const matchesStatus = statusFilter === 'ALL' || c.status === statusFilter;
    if (!matchesStatus) return false;

    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      (c.claimReason && c.claimReason.toLowerCase().includes(q)) ||
      (c.productName && c.productName.toLowerCase().includes(q)) ||
      (c.customerName && c.customerName.toLowerCase().includes(q)) ||
      (c.customerEmail && c.customerEmail.toLowerCase().includes(q)) ||
      (c.id && c.id.toLowerCase().includes(q))
    );
  });

  const pendingCount = claims.filter((c) => c.status === 'PENDING').length;
  const approvedCount = claims.filter((c) => c.status === 'APPROVED').length;
  const inProgressCount = claims.filter((c) => c.status === 'IN_PROGRESS').length;

  return (
    <div className="container" style={{ padding: '3rem 1.5rem', flex: 1, maxWidth: '1180px' }}>
      {/* Navigation Breadcrumb */}
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to="/admin" className="btn btn-ghost btn-sm" style={{ paddingLeft: 0 }}>
          ← Back to Operations Dashboard
        </Link>
      </div>

      {/* Page Title & Counters */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '2rem' }}>
        <div>
          <h1 className="page-title" style={{ marginBottom: '0.25rem' }}>Warranty Claims Adjudication Queue</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)', margin: 0 }}>
            Audit incoming defect reports, triage claims, approve service orders, and monitor warranty fulfillment.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--primary-light)', color: 'var(--primary)' }}>
            {claims.length} Total Claims
          </span>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--warning-bg)', color: 'var(--warning)' }}>
            {pendingCount} Pending Triage
          </span>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--success-bg)', color: 'var(--success)' }}>
            {approvedCount} Approved
          </span>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--primary-light)', color: 'var(--primary)' }}>
            {inProgressCount} In Progress
          </span>
        </div>
      </div>

      {error && (
        <div style={{ marginBottom: '1.5rem' }}>
          <ErrorMessage message={error} />
        </div>
      )}

      {/* Filter Tabs & Search Bar */}
      <div className="detail-card" style={{ marginBottom: '1.5rem', padding: '1rem 1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
          {['ALL', 'PENDING', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'CANCELLED'].map((st) => (
            <button
              key={st}
              type="button"
              className={`btn btn-sm ${statusFilter === st ? 'btn-primary' : 'btn-ghost'}`}
              style={{ fontSize: '0.75rem' }}
              onClick={() => setStatusFilter(st)}
            >
              {st.replace('_', ' ')}
            </button>
          ))}
        </div>

        <input
          type="text"
          className="form-input"
          style={{ width: '280px' }}
          placeholder="Filter by customer, product, reason, ID..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>

      {/* Claims Table */}
      <div className="detail-card" style={{ padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '4rem 1.5rem', textAlign: 'center' }}>
            <Loading size="md" text="Loading claims..." />
          </div>
        ) : filteredClaims.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '4rem 1.5rem' }}>
            <div className="empty-state-icon" style={{ margin: '0 auto 1rem auto' }}>
              ⚖️
            </div>
            <h3 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', marginBottom: '0.5rem' }}>
              No claims found.
            </h3>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)', margin: 0 }}>
              {searchQuery || statusFilter !== 'ALL'
                ? 'No claims match your status filter and search query.'
                : 'No customer warranty claims submitted yet.'}
            </p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: 'var(--font-size-sm)' }}>
              <thead>
                <tr style={{ backgroundColor: 'var(--surface-hover)', borderBottom: '1px solid var(--border)' }}>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Claim Issue / Reason</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Customer</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Product</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Lifecycle Status</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Submitted</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)', textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredClaims.map((c) => (
                  <tr key={c.id} style={{ borderBottom: '1px solid var(--border)', transition: 'background-color 0.15s' }}>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ fontWeight: '700', color: 'var(--text-primary)' }}>{c.claimReason}</div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
                        ID: {c.id}
                      </div>
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ fontWeight: '600', color: 'var(--text-primary)' }}>{c.customerName || 'N/A'}</div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>{c.customerEmail}</div>
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ fontWeight: '600', color: 'var(--text-primary)' }}>{c.productName}</div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                        {c.brand} • {c.modelNumber}
                      </div>
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <span
                        className={`status-badge ${
                          c.status === 'PENDING'
                            ? 'badge-pending'
                            : c.status === 'APPROVED' || c.status === 'COMPLETED'
                            ? 'badge-active'
                            : c.status === 'IN_PROGRESS'
                            ? 'badge-expiring'
                            : 'badge-expired'
                        }`}
                      >
                        {c.status.replace('_', ' ')}
                      </span>
                    </td>
                    <td style={{ padding: '1rem 1.25rem', color: 'var(--text-muted)', fontSize: 'var(--font-size-xs)', whiteSpace: 'nowrap' }}>
                      {formatDate(c.createdAt)}
                    </td>
                    <td style={{ padding: '1rem 1.25rem', textAlign: 'right', whiteSpace: 'nowrap' }}>
                      <Link to={`/admin/claims/${c.id}`} className="btn btn-primary btn-sm">
                        View Details →
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
