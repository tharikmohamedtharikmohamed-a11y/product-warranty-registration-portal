import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import claimService from '../services/claimService';

/**
 * Claims Page (/claims).
 * Customer portal view of all filed warranty claims, review statuses, and resolution timelines.
 * Phase 10 — Warranty Claims
 */
export default function ClaimsPage() {
  const [claims, setClaims] = useState([]);
  const [filteredClaims, setFilteredClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  const fetchClaims = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await claimService.getClaims();
      setClaims(data || []);
      setFilteredClaims(data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load claims. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClaims();
  }, []);

  useEffect(() => {
    let result = claims;

    if (statusFilter !== 'ALL') {
      result = result.filter((c) => c.status === statusFilter);
    }

    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(
        (c) =>
          c.productName?.toLowerCase().includes(q) ||
          c.brand?.toLowerCase().includes(q) ||
          c.claimReason?.toLowerCase().includes(q) ||
          c.description?.toLowerCase().includes(q)
      );
    }

    setFilteredClaims(result);
  }, [statusFilter, searchQuery, claims]);

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

  const renderStatusBadge = (status) => {
    const config = {
      PENDING: { bg: 'var(--warning-bg)', text: 'var(--warning-text)', label: 'Pending Review' },
      APPROVED: { bg: '#eff6ff', text: '#1d4ed8', label: 'Approved' },
      IN_PROGRESS: { bg: '#f5f3ff', text: '#6d28d9', label: 'In Progress' },
      COMPLETED: { bg: 'var(--success-bg)', text: 'var(--success-text)', label: 'Completed' },
      REJECTED: { bg: 'var(--danger-bg)', text: 'var(--danger-text)', label: 'Rejected' },
      CANCELLED: { bg: '#f1f5f9', text: '#64748b', label: 'Cancelled' }
    };

    const c = config[status] || { bg: '#f1f5f9', text: '#64748b', label: status };

    return (
      <span
        style={{
          backgroundColor: c.bg,
          color: c.text,
          fontSize: 'var(--font-size-xs)',
          fontWeight: '700',
          padding: '0.25rem 0.65rem',
          borderRadius: 'var(--radius-sm)',
          display: 'inline-block',
          whiteSpace: 'nowrap'
        }}
      >
        {c.label}
      </span>
    );
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '4rem 1.5rem', flex: 1 }}>
        <div className="loading-container">
          <div className="spinner spinner-lg"></div>
          <span>Loading your warranty claims...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '2.5rem 1.5rem 4rem', flex: 1 }}>
      {/* Header */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '2rem'
        }}
      >
        <div>
          <h1 style={{ fontSize: 'var(--font-size-3xl)', fontWeight: '800', color: 'var(--text-primary)', margin: 0 }}>
            My Warranty Claims
          </h1>
          <p style={{ color: 'var(--text-secondary)', marginTop: '0.25rem', fontSize: 'var(--font-size-base)' }}>
            Track resolution status, repair evaluations, and service adjudications for your equipment.
          </p>
        </div>

        <Link to="/products" className="btn btn-secondary btn-md">
          Browse Products
        </Link>
      </div>

      {error && (
        <div className="error-alert" style={{ marginBottom: '1.5rem' }} role="alert">
          {error}
        </div>
      )}

      {/* Filter and Search Bar */}
      {claims.length > 0 && (
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '1rem',
            marginBottom: '1.5rem'
          }}
        >
          {/* Status Tabs */}
          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            {['ALL', 'PENDING', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'CANCELLED'].map((st) => (
              <button
                key={st}
                type="button"
                className={`btn btn-sm ${statusFilter === st ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => setStatusFilter(st)}
                style={{ fontSize: 'var(--font-size-xs)' }}
              >
                {st === 'ALL' ? 'All Claims' : st.replace('_', ' ')}
              </button>
            ))}
          </div>

          {/* Search */}
          <div style={{ minWidth: '240px' }}>
            <input
              type="text"
              className="form-control"
              placeholder="Search claims or product..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{ fontSize: 'var(--font-size-sm)', padding: '0.45rem 0.75rem' }}
            />
          </div>
        </div>
      )}

      {/* Claims Content */}
      {claims.length === 0 ? (
        <div className="empty-state" style={{ maxWidth: '540px', margin: '3rem auto' }}>
          <div
            className="empty-state-icon"
            style={{ backgroundColor: 'var(--primary-light)', color: 'var(--primary)' }}
          >
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              <path d="m9 12 2 2 4-4" />
            </svg>
          </div>
          <h2 className="empty-state-title">No warranty claims yet</h2>
          <p className="empty-state-desc">
            You have not submitted any warranty claims. If any of your registered products experience defects, you can submit a claim directly from the product page.
          </p>
          <Link to="/products" className="btn btn-primary btn-md">
            View My Products
          </Link>
        </div>
      ) : filteredClaims.length === 0 ? (
        <div className="empty-state" style={{ padding: '3rem 1.5rem' }}>
          <h3 style={{ fontSize: 'var(--font-size-lg)', color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
            No claims match your filter criteria
          </h3>
          <p style={{ color: 'var(--text-muted)', fontSize: 'var(--font-size-sm)', marginBottom: '1rem' }}>
            Try resetting your status filter or clearing your search term.
          </p>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => {
              setStatusFilter('ALL');
              setSearchQuery('');
            }}
          >
            Reset Filters
          </button>
        </div>
      ) : (
        <div
          style={{
            backgroundColor: 'var(--surface)',
            borderRadius: 'var(--radius-lg)',
            border: '1px solid var(--border)',
            boxShadow: 'var(--shadow-sm)',
            overflow: 'hidden'
          }}
        >
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ backgroundColor: 'var(--surface-hover)', borderBottom: '1px solid var(--border)' }}>
                  <th style={{ padding: '0.875rem 1.25rem', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                    Product
                  </th>
                  <th style={{ padding: '0.875rem 1.25rem', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                    Claim Reason
                  </th>
                  <th style={{ padding: '0.875rem 1.25rem', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                    Current Status
                  </th>
                  <th style={{ padding: '0.875rem 1.25rem', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                    Date Submitted
                  </th>
                  <th style={{ padding: '0.875rem 1.25rem', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                    Last Updated
                  </th>
                  <th style={{ padding: '0.875rem 1.25rem', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)', textAlign: 'right' }}>
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {filteredClaims.map((claim) => (
                  <tr
                    key={claim.id}
                    style={{
                      borderBottom: '1px solid var(--border)',
                      transition: 'background-color var(--transition-fast)'
                    }}
                  >
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ fontWeight: '600', color: 'var(--text-primary)' }}>
                        {claim.productName || 'Registered Equipment'}
                      </div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                        {claim.brand} • Model {claim.modelNumber || 'N/A'}
                      </div>
                    </td>

                    <td style={{ padding: '1rem 1.25rem' }}>
                      <span style={{ fontWeight: '500', color: 'var(--text-primary)' }}>
                        {claim.claimReason}
                      </span>
                    </td>

                    <td style={{ padding: '1rem 1.25rem' }}>
                      {renderStatusBadge(claim.status)}
                    </td>

                    <td style={{ padding: '1rem 1.25rem', color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)' }}>
                      {formatDate(claim.createdAt)}
                    </td>

                    <td style={{ padding: '1rem 1.25rem', color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)' }}>
                      {formatDate(claim.updatedAt)}
                    </td>

                    <td style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>
                      <Link
                        to={`/claims/${claim.id}`}
                        className="btn btn-secondary btn-sm"
                        style={{ fontSize: 'var(--font-size-xs)' }}
                      >
                        View Details →
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
