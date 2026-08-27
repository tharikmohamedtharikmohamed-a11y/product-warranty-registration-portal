import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import warrantyService from '../services/warrantyService';

/**
 * Warranty Portfolio & Monitoring Page.
 * Displays customer's warranties with calculated status, countdown days, and progress bars.
 * Phase 8 — Warranty Management
 */
export default function WarrantiesPage() {
  const [warranties, setWarranties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  const fetchWarranties = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await warrantyService.getWarranties();
      setWarranties(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load your warranties.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWarranties();
  }, []);

  // Compute metrics
  const metrics = useMemo(() => {
    const total = warranties.length;
    let active = 0;
    let expiring = 0;
    let expired = 0;

    warranties.forEach((w) => {
      if (w.status === 'ACTIVE') active += 1;
      else if (w.status === 'EXPIRING_SOON') expiring += 1;
      else if (w.status === 'EXPIRED') expired += 1;
    });

    return { total, active, expiring, expired };
  }, [warranties]);

  // Filtered warranties list
  const filteredWarranties = useMemo(() => {
    return warranties.filter((w) => {
      const matchesSearch =
        searchTerm.trim() === '' ||
        w.productName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        w.brand?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        w.modelNumber?.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesStatus =
        statusFilter === 'ALL' || w.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [warranties, searchTerm, statusFilter]);

  const renderStatusBadge = (status) => {
    switch (status) {
      case 'ACTIVE':
        return (
          <span className="status-badge badge-active" aria-label="Status: Active">
            <span className="status-dot"></span> Active
          </span>
        );
      case 'EXPIRING_SOON':
        return (
          <span className="status-badge badge-expiring" aria-label="Status: Expiring Soon">
            <span className="status-dot"></span> Expiring Soon
          </span>
        );
      case 'EXPIRED':
        return (
          <span className="status-badge badge-expired" aria-label="Status: Expired">
            <span className="status-dot"></span> Expired
          </span>
        );
      default:
        return <span className="status-badge">{status || 'UNKNOWN'}</span>;
    }
  };

  return (
    <div className="container" style={{ padding: '3rem 1.5rem', flex: 1 }}>
      {/* Header */}
      <div className="page-header-row">
        <div>
          <h1 className="page-title">My Warranties</h1>
          <p className="page-subtitle">
            Track real-time warranty coverage, protection timelines, and proactive expiration alerts.
          </p>
        </div>
        <Link to="/products/register" className="btn btn-primary btn-md">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          Register New Product
        </Link>
      </div>

      {/* Metrics Strip */}
      <div className="metrics-strip">
        <div className="metric-card">
          <span className="metric-label">Total Warranties</span>
          <span className="metric-value">{metrics.total}</span>
        </div>
        <div className="metric-card" style={{ borderLeft: '4px solid var(--success)' }}>
          <span className="metric-label">Active Coverage</span>
          <span className="metric-value" style={{ color: 'var(--success)' }}>{metrics.active}</span>
        </div>
        <div className="metric-card" style={{ borderLeft: '4px solid var(--warning)' }}>
          <span className="metric-label">Expiring Soon (30d)</span>
          <span className="metric-value" style={{ color: 'var(--warning)' }}>{metrics.expiring}</span>
        </div>
        <div className="metric-card" style={{ borderLeft: '4px solid var(--danger)' }}>
          <span className="metric-label">Expired</span>
          <span className="metric-value" style={{ color: 'var(--danger)' }}>{metrics.expired}</span>
        </div>
      </div>

      {/* Search & Filters */}
      <div className="filter-bar">
        <div className="search-box">
          <span className="search-icon-slot">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
          </span>
          <input
            type="text"
            className="form-input"
            placeholder="Search warranties by product, brand, or model..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="filter-dropdowns">
          <select
            className="form-input"
            style={{ width: 'auto', minWidth: '180px' }}
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="ALL">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="EXPIRING_SOON">Expiring Soon</option>
            <option value="EXPIRED">Expired</option>
          </select>
        </div>
      </div>

      {/* Error State */}
      {error && (
        <div className="error-alert" style={{ marginBottom: '1.5rem' }}>
          <span>{error}</span>
          <button
            onClick={fetchWarranties}
            className="btn btn-secondary btn-sm"
            style={{ marginLeft: 'auto' }}
          >
            Retry
          </button>
        </div>
      )}

      {/* Loading State */}
      {loading ? (
        <div className="loading-container">
          <div className="spinner spinner-lg"></div>
          <span>Loading warranties...</span>
        </div>
      ) : warranties.length === 0 ? (
        /* Empty State */
        <div className="empty-state">
          <div className="empty-state-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              <path d="m9 12 2 2 4-4" />
            </svg>
          </div>
          <h2 className="empty-state-title">No warranties available yet</h2>
          <p className="empty-state-desc">
            Register your purchased equipment to automatically generate active warranty protection records.
          </p>
          <Link to="/products/register" className="btn btn-primary btn-md">
            Register a Product
          </Link>
        </div>
      ) : filteredWarranties.length === 0 ? (
        /* Empty Filter Match State */
        <div className="empty-state">
          <h2 className="empty-state-title">No matching warranties found</h2>
          <p className="empty-state-desc">
            Try adjusting your search query or reset the filter dropdown.
          </p>
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => {
              setSearchTerm('');
              setStatusFilter('ALL');
            }}
          >
            Reset Filters
          </button>
        </div>
      ) : (
        /* Warranties Cards Grid */
        <div className="products-grid">
          {filteredWarranties.map((w) => {
            const progress = w.progressPercentage || 0;
            return (
              <div key={w.id} className="product-card">
                <div>
                  <div className="product-card-header">
                    <div>
                      <div className="product-card-brand">{w.brand || 'Product'} • Model {w.modelNumber || 'N/A'}</div>
                      <h3 className="product-card-title">{w.productName || 'Registered Equipment'}</h3>
                    </div>
                    {renderStatusBadge(w.status)}
                  </div>

                  {/* Status Banner */}
                  {w.status === 'EXPIRING_SOON' && (
                    <div
                      style={{
                        marginTop: '1rem',
                        padding: '0.6rem 0.8rem',
                        borderRadius: 'var(--radius-md)',
                        backgroundColor: 'var(--warning-bg)',
                        color: 'var(--warning)',
                        fontSize: 'var(--font-size-xs)',
                        fontWeight: '600',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.5rem'
                      }}
                    >
                      <span>⚠️ Your warranty expires soon.</span>
                    </div>
                  )}

                  {w.status === 'EXPIRED' && (
                    <div
                      style={{
                        marginTop: '1rem',
                        padding: '0.6rem 0.8rem',
                        borderRadius: 'var(--radius-md)',
                        backgroundColor: 'var(--danger-bg)',
                        color: 'var(--danger)',
                        fontSize: 'var(--font-size-xs)',
                        fontWeight: '600',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.5rem'
                      }}
                    >
                      <span>✕ Warranty expired on {w.expiryDate}</span>
                    </div>
                  )}

                  <div className="product-card-meta" style={{ marginTop: '1rem' }}>
                    <div className="meta-item">
                      <span className="meta-label">Start Date</span>
                      <span className="meta-value">{w.startDate}</span>
                    </div>
                    <div className="meta-item">
                      <span className="meta-label">Expiry Date</span>
                      <span className="meta-value" style={{ fontWeight: '600' }}>{w.expiryDate}</span>
                    </div>
                    <div className="meta-item">
                      <span className="meta-label">Duration</span>
                      <span className="meta-value">{w.warrantyDurationMonths ? `${w.warrantyDurationMonths} Months` : 'N/A'}</span>
                    </div>
                    <div className="meta-item">
                      <span className="meta-label">Remaining Coverage</span>
                      <span
                        className="meta-value"
                        style={{
                          color:
                            w.status === 'ACTIVE'
                              ? 'var(--success)'
                              : w.status === 'EXPIRING_SOON'
                              ? 'var(--warning)'
                              : 'var(--danger)',
                          fontWeight: '700'
                        }}
                      >
                        {w.status === 'EXPIRED' ? 'Expired (0 days)' : `${w.daysRemaining} days remaining`}
                      </span>
                    </div>
                  </div>

                  {/* Accessible Progress Bar */}
                  <div style={{ marginTop: '1.25rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                      <span>Warranty Period Elapsed</span>
                      <span style={{ fontWeight: '700', color: 'var(--text-primary)' }}>{progress}%</span>
                    </div>
                    <div
                      role="progressbar"
                      aria-valuenow={progress}
                      aria-valuemin="0"
                      aria-valuemax="100"
                      aria-label={`Warranty progress: ${progress}%`}
                      style={{
                        height: '8px',
                        width: '100%',
                        backgroundColor: 'var(--border)',
                        borderRadius: 'var(--radius-full)',
                        overflow: 'hidden'
                      }}
                    >
                      <div
                        style={{
                          height: '100%',
                          width: `${progress}%`,
                          backgroundColor:
                            w.status === 'ACTIVE'
                              ? 'var(--primary)'
                              : w.status === 'EXPIRING_SOON'
                              ? 'var(--warning)'
                              : 'var(--danger)',
                          borderRadius: 'var(--radius-full)',
                          transition: 'width 0.3s ease'
                        }}
                      />
                    </div>
                  </div>
                </div>

                <div className="product-card-footer" style={{ marginTop: '1.5rem' }}>
                  <Link to={`/warranties/${w.id}`} className="btn btn-ghost btn-sm">
                    View Details →
                  </Link>

                  {w.productId && (
                    <Link to={`/products/${w.productId}`} className="btn btn-secondary btn-sm">
                      View Product
                    </Link>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
