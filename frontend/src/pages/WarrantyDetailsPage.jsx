import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import warrantyService from '../services/warrantyService';

/**
 * Warranty Specifications & Timeline Detail Page.
 * Displays granular coverage attributes, countdown metrics, and accessible progress indicators.
 * Phase 8 — Warranty Management
 */
export default function WarrantyDetailsPage() {
  const { id } = useParams();

  const [warranty, setWarranty] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchWarranty = async () => {
      try {
        setLoading(true);
        setError('');
        const data = await warrantyService.getWarranty(id);
        setWarranty(data);
      } catch (err) {
        if (err.response?.status === 404) {
          setError('Warranty not found or you do not have permission to view it.');
        } else {
          setError(err.response?.data?.message || 'Failed to load warranty details.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchWarranty();
  }, [id]);

  if (loading) {
    return (
      <div className="container" style={{ padding: '4rem 1.5rem', flex: 1 }}>
        <div className="loading-container">
          <div className="spinner spinner-lg"></div>
          <span>Loading warranty information...</span>
        </div>
      </div>
    );
  }

  if (error || !warranty) {
    return (
      <div className="container" style={{ padding: '4rem 1.5rem', flex: 1, maxWidth: '640px' }}>
        <div className="empty-state">
          <div className="empty-state-icon" style={{ backgroundColor: 'var(--danger-bg)', color: 'var(--danger)' }}>
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
          </div>
          <h2 className="empty-state-title">Warranty Not Found</h2>
          <p className="empty-state-desc">{error || 'Unable to retrieve this warranty record.'}</p>
          <Link to="/warranties" className="btn btn-secondary btn-md">
            Return to Warranties
          </Link>
        </div>
      </div>
    );
  }

  const progress = warranty.progressPercentage || 0;

  return (
    <div className="container" style={{ padding: '3rem 1.5rem', flex: 1, maxWidth: '1000px' }}>
      {/* Navigation */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <Link to="/warranties" className="btn btn-ghost btn-sm" style={{ paddingLeft: 0 }}>
          ← Back to Warranties
        </Link>
        {warranty.productId && (
          <Link to={`/products/${warranty.productId}`} className="btn btn-secondary btn-sm">
            View Product Details
          </Link>
        )}
      </div>

      {/* Header */}
      <div style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--primary)', letterSpacing: '0.05em' }}>
            {warranty.brand || 'Product'} • Model {warranty.modelNumber || 'N/A'}
          </span>
          <span
            className={`status-badge ${
              warranty.status === 'ACTIVE'
                ? 'badge-active'
                : warranty.status === 'EXPIRING_SOON'
                ? 'badge-expiring'
                : 'badge-expired'
            }`}
          >
            <span className="status-dot"></span> {warranty.status?.replace('_', ' ')}
          </span>
        </div>
        <h1 className="page-title">{warranty.productName || 'Warranty Protection Record'}</h1>
      </div>

      {/* Status Advisory Banner */}
      {warranty.status === 'ACTIVE' && (
        <div
          style={{
            marginBottom: '2rem',
            padding: '1.25rem',
            borderRadius: 'var(--radius-lg)',
            backgroundColor: 'var(--success-bg)',
            border: '1px solid var(--success-border)',
            color: 'var(--success)'
          }}
        >
          <h3 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', marginBottom: '0.25rem' }}>
            ✓ Warranty Active
          </h3>
          <p style={{ fontSize: 'var(--font-size-sm)', margin: 0 }}>
            Your product is currently protected under active terms. You have <strong>{warranty.daysRemaining} days</strong> of coverage remaining.
          </p>
        </div>
      )}

      {warranty.status === 'EXPIRING_SOON' && (
        <div
          style={{
            marginBottom: '2rem',
            padding: '1.25rem',
            borderRadius: 'var(--radius-lg)',
            backgroundColor: 'var(--warning-bg)',
            border: '1px solid var(--warning-border)',
            color: 'var(--warning)'
          }}
        >
          <h3 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', marginBottom: '0.25rem' }}>
            ⚠️ Your warranty expires soon
          </h3>
          <p style={{ fontSize: 'var(--font-size-sm)', margin: 0 }}>
            This warranty will expire on <strong>{warranty.expiryDate}</strong> ({warranty.daysRemaining} days remaining). Please evaluate any potential defect claims before coverage ends.
          </p>
        </div>
      )}

      {warranty.status === 'EXPIRED' && (
        <div
          style={{
            marginBottom: '2rem',
            padding: '1.25rem',
            borderRadius: 'var(--radius-lg)',
            backgroundColor: 'var(--danger-bg)',
            border: '1px solid var(--danger-border)',
            color: 'var(--danger)'
          }}
        >
          <h3 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', marginBottom: '0.25rem' }}>
            ✕ Warranty Expired
          </h3>
          <p style={{ fontSize: 'var(--font-size-sm)', margin: 0 }}>
            This warranty concluded on <strong>{warranty.expiryDate}</strong>. Coverage terms have ended.
          </p>
        </div>
      )}

      {/* Detail Layout */}
      <div className="detail-grid">
        {/* Left Column: Specifications */}
        <div className="detail-card">
          <h2 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '1.25rem' }}>
            Warranty Specifications
          </h2>

          <div className="spec-list">
            <div className="spec-item">
              <span className="meta-label">Warranty Record ID</span>
              <span className="meta-value" style={{ fontFamily: 'monospace', fontSize: 'var(--font-size-xs)' }}>
                {warranty.id}
              </span>
            </div>

            <div className="spec-item">
              <span className="meta-label">Associated Product</span>
              <span className="meta-value" style={{ fontWeight: '600' }}>
                {warranty.productName || 'N/A'}
              </span>
            </div>

            <div className="spec-item">
              <span className="meta-label">Brand & Model</span>
              <span className="meta-value">
                {warranty.brand || 'N/A'} • {warranty.modelNumber || 'N/A'}
              </span>
            </div>

            <div className="spec-item">
              <span className="meta-label">Effective Start Date</span>
              <span className="meta-value">{warranty.startDate}</span>
            </div>

            <div className="spec-item">
              <span className="meta-label">Official Expiry Date</span>
              <span className="meta-value" style={{ fontWeight: '700' }}>{warranty.expiryDate}</span>
            </div>

            <div className="spec-item">
              <span className="meta-label">Total Duration</span>
              <span className="meta-value">{warranty.warrantyDurationMonths ? `${warranty.warrantyDurationMonths} Months` : 'N/A'}</span>
            </div>

            <div className="spec-item">
              <span className="meta-label">Initial Provisioned Timestamp</span>
              <span className="meta-value" style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                {warranty.createdAt ? new Date(warranty.createdAt).toLocaleString() : 'N/A'}
              </span>
            </div>
          </div>
        </div>

        {/* Right Column: Dynamic Coverage Progress */}
        <div>
          <div className="warranty-highlight-card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="var(--primary-border)" strokeWidth="2.5">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                <path d="m9 12 2 2 4-4" />
              </svg>
              <h2 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '700', color: '#ffffff' }}>
                Coverage Timeline
              </h2>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div>
                <div style={{ fontSize: 'var(--font-size-xs)', color: '#94a3b8', marginBottom: '0.25rem' }}>
                  Days Remaining
                </div>
                <div style={{ fontSize: 'var(--font-size-3xl)', fontWeight: '800', color: '#ffffff' }}>
                  {warranty.status === 'EXPIRED' ? '0' : warranty.daysRemaining}
                </div>
                <div style={{ fontSize: 'var(--font-size-xs)', color: '#94a3b8', marginTop: '0.25rem' }}>
                  {warranty.status === 'EXPIRED' ? 'Coverage period concluded' : 'Calendar days of protection left'}
                </div>
              </div>

              {/* Progress Bar */}
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 'var(--font-size-xs)', color: '#94a3b8', marginBottom: '0.5rem' }}>
                  <span>{warranty.startDate}</span>
                  <span style={{ fontWeight: '700', color: '#ffffff' }}>{progress}% elapsed</span>
                  <span>{warranty.expiryDate}</span>
                </div>
                <div
                  role="progressbar"
                  aria-valuenow={progress}
                  aria-valuemin="0"
                  aria-valuemax="100"
                  aria-label={`Warranty period elapsed: ${progress}%`}
                  style={{
                    height: '10px',
                    width: '100%',
                    background: '#334155',
                    borderRadius: 'var(--radius-full)',
                    overflow: 'hidden'
                  }}
                >
                  <div
                    style={{
                      height: '100%',
                      width: `${progress}%`,
                      background:
                        warranty.status === 'ACTIVE'
                          ? 'var(--primary)'
                          : warranty.status === 'EXPIRING_SOON'
                          ? 'var(--warning)'
                          : 'var(--danger)',
                      borderRadius: 'var(--radius-full)',
                      transition: 'width 0.4s ease'
                    }}
                  />
                </div>
              </div>

              <div style={{ borderTop: '1px solid #334155', paddingTop: '1rem', display: 'flex', justifyContent: 'space-between', fontSize: 'var(--font-size-xs)', color: '#94a3b8' }}>
                <span>Authoritative Backend Status:</span>
                <span style={{ fontWeight: '700', color: '#ffffff' }}>{warranty.status}</span>
              </div>
            </div>
          </div>

          <div
            style={{
              marginTop: '1.5rem',
              padding: '1.25rem',
              borderRadius: 'var(--radius-lg)',
              backgroundColor: 'var(--surface)',
              border: '1px solid var(--border)'
            }}
          >
            <h3 style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
              Warranty Claim Eligibility
            </h3>
            <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
              Claims and proof-of-purchase invoice management will be integrated in subsequent phases.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
