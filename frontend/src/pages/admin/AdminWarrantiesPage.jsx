import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import adminService from '../../services/adminService';
import Loading from '../../components/Loading';
import ErrorMessage from '../../components/ErrorMessage';

/**
 * Admin Warranty Portfolio Management Page.
 * Displays authoritative real-time warranty coverage across all customer products,
 * including dynamic status thresholds, days remaining countdowns, and progress bars.
 * Phase 11 — Admin Management Module
 */
export default function AdminWarrantiesPage() {
  const [warranties, setWarranties] = useState([]);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchWarranties = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await adminService.getWarranties();
      setWarranties(data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load warranties portfolio.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWarranties();
  }, []);

  const filteredWarranties = warranties.filter((w) => {
    const matchesStatus = statusFilter === 'ALL' || w.status === statusFilter;
    if (!matchesStatus) return false;

    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      (w.productName && w.productName.toLowerCase().includes(q)) ||
      (w.brand && w.brand.toLowerCase().includes(q)) ||
      (w.modelNumber && w.modelNumber.toLowerCase().includes(q)) ||
      (w.customerName && w.customerName.toLowerCase().includes(q)) ||
      (w.customerEmail && w.customerEmail.toLowerCase().includes(q))
    );
  });

  const activeCount = warranties.filter((w) => w.status === 'ACTIVE').length;
  const expiringCount = warranties.filter((w) => w.status === 'EXPIRING_SOON').length;
  const expiredCount = warranties.filter((w) => w.status === 'EXPIRED').length;

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
          <h1 className="page-title" style={{ marginBottom: '0.25rem' }}>Global Warranties Portfolio</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)', margin: 0 }}>
            Authoritative dynamic lifecycle tracking, countdown timers, and protection coverage across all customer accounts.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--primary-light)', color: 'var(--primary)' }}>
            {warranties.length} Total
          </span>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--success-bg)', color: 'var(--success)' }}>
            {activeCount} Active
          </span>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--warning-bg)', color: 'var(--warning)' }}>
            {expiringCount} Expiring Soon
          </span>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--danger-bg)', color: 'var(--danger)' }}>
            {expiredCount} Expired
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
        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          {['ALL', 'ACTIVE', 'EXPIRING_SOON', 'EXPIRED'].map((st) => (
            <button
              key={st}
              type="button"
              className={`btn btn-sm ${statusFilter === st ? 'btn-primary' : 'btn-ghost'}`}
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
          placeholder="Filter by product, brand, customer..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>

      {/* Warranties Table */}
      <div className="detail-card" style={{ padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '4rem 1.5rem', textAlign: 'center' }}>
            <Loading size="md" text="Loading warranties..." />
          </div>
        ) : filteredWarranties.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '4rem 1.5rem' }}>
            <div className="empty-state-icon" style={{ margin: '0 auto 1rem auto' }}>
              🛡️
            </div>
            <h3 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', marginBottom: '0.5rem' }}>
              No warranties found.
            </h3>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)', margin: 0 }}>
              No warranty records match your selected status filter and search query.
            </p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: 'var(--font-size-sm)' }}>
              <thead>
                <tr style={{ backgroundColor: 'var(--surface-hover)', borderBottom: '1px solid var(--border)' }}>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Product</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Customer</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Status</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Validity Window</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Days Left</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)', minWidth: '160px' }}>Period Progress</th>
                </tr>
              </thead>
              <tbody>
                {filteredWarranties.map((w) => (
                  <tr key={w.id} style={{ borderBottom: '1px solid var(--border)', transition: 'background-color 0.15s' }}>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ fontWeight: '700', color: 'var(--text-primary)' }}>{w.productName}</div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                        {w.brand} • {w.modelNumber}
                      </div>
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ fontWeight: '600', color: 'var(--text-primary)' }}>{w.customerName || 'N/A'}</div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>{w.customerEmail}</div>
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <span
                        className={`status-badge ${
                          w.status === 'ACTIVE'
                            ? 'badge-active'
                            : w.status === 'EXPIRING_SOON'
                            ? 'badge-expiring'
                            : 'badge-expired'
                        }`}
                      >
                        {w.status.replace('_', ' ')}
                      </span>
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div>{w.startDate} → {w.expiryDate}</div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                        {w.warrantyDurationMonths} Months duration
                      </div>
                    </td>
                    <td style={{ padding: '1rem 1.25rem', fontWeight: '700' }}>
                      {w.status === 'EXPIRED' ? (
                        <span style={{ color: 'var(--danger)' }}>0 days (Expired)</span>
                      ) : (
                        <span style={{ color: w.status === 'EXPIRING_SOON' ? 'var(--warning)' : 'var(--success)' }}>
                          {w.daysRemaining} days
                        </span>
                      )}
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <div
                          role="progressbar"
                          aria-valuenow={w.progressPercentage}
                          aria-valuemin="0"
                          aria-valuemax="100"
                          style={{ flex: 1, height: '6px', backgroundColor: 'var(--border)', borderRadius: '3px', overflow: 'hidden' }}
                        >
                          <div
                            style={{
                              height: '100%',
                              width: `${w.progressPercentage}%`,
                              backgroundColor:
                                w.status === 'ACTIVE'
                                  ? 'var(--primary)'
                                  : w.status === 'EXPIRING_SOON'
                                  ? 'var(--warning)'
                                  : 'var(--danger)'
                            }}
                          />
                        </div>
                        <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '600', width: '36px', textAlign: 'right' }}>
                          {w.progressPercentage}%
                        </span>
                      </div>
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
