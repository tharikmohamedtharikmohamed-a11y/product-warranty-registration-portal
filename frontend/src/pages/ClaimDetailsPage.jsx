import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import claimService from '../services/claimService';

/**
 * Claim Details Page (/claims/:id).
 * Granular inspection of filed claim, product information, defect description,
 * visual lifecycle progression timeline, and pending claim cancellation.
 * Phase 10 — Warranty Claims
 */
export default function ClaimDetailsPage() {
  const { id } = useParams();

  const [claim, setClaim] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Cancellation modal state
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [cancelError, setCancelError] = useState('');

  const fetchClaim = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await claimService.getClaimById(id);
      setClaim(data);
    } catch (err) {
      if (err.response?.status === 404) {
        setError('Claim not found or you do not have permission to view it.');
      } else {
        setError(err.response?.data?.message || 'Failed to load claim details.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClaim();
  }, [id]);

  const formatDate = (isoString) => {
    if (!isoString) return '—';
    try {
      return new Date(isoString).toLocaleString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return isoString;
    }
  };

  const handleConfirmCancel = async () => {
    try {
      setCancelling(true);
      setCancelError('');
      const updated = await claimService.cancelClaim(id);
      setClaim(updated);
      setShowCancelModal(false);
      setSuccessMessage('Your warranty claim has been cancelled successfully.');
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setCancelError(err.response?.data?.message || 'Failed to cancel claim. Please try again.');
    } finally {
      setCancelling(false);
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
          fontSize: 'var(--font-size-sm)',
          fontWeight: '700',
          padding: '0.35rem 0.85rem',
          borderRadius: 'var(--radius-full)',
          display: 'inline-block'
        }}
      >
        {c.label}
      </span>
    );
  };

  const renderLifecycleTimeline = (status) => {
    const isStandardLifecycle = !['REJECTED', 'CANCELLED'].includes(status);
    const standardSteps = [
      { id: 'PENDING', label: 'Submitted & Pending Review' },
      { id: 'APPROVED', label: 'Claim Approved' },
      { id: 'IN_PROGRESS', label: 'Repair / Processing In Progress' },
      { id: 'COMPLETED', label: 'Resolution Completed' }
    ];

    const getStepIndex = (st) => {
      switch (st) {
        case 'PENDING':
          return 0;
        case 'APPROVED':
          return 1;
        case 'IN_PROGRESS':
          return 2;
        case 'COMPLETED':
          return 3;
        default:
          return 0;
      }
    };

    const currentIndex = getStepIndex(status);

    return (
      <div
        style={{
          padding: '1.5rem',
          backgroundColor: 'var(--surface-hover)',
          borderRadius: 'var(--radius-lg)',
          border: '1px solid var(--border)',
          marginBottom: '2rem'
        }}
      >
        <h3
          style={{
            fontSize: 'var(--font-size-xs)',
            fontWeight: '700',
            textTransform: 'uppercase',
            color: 'var(--text-muted)',
            marginBottom: '1.25rem'
          }}
        >
          Claim Lifecycle Timeline
        </h3>

        {isStandardLifecycle ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {standardSteps.map((step, idx) => {
              const isPassed = idx < currentIndex;
              const isCurrent = idx === currentIndex;

              return (
                <div key={step.id} style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem' }}>
                  <div
                    style={{
                      width: '28px',
                      height: '28px',
                      borderRadius: '50%',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: 'var(--font-size-xs)',
                      fontWeight: '700',
                      flexShrink: 0,
                      backgroundColor: isPassed
                        ? 'var(--success)'
                        : isCurrent
                        ? 'var(--primary)'
                        : 'var(--border)',
                      color: isPassed || isCurrent ? '#ffffff' : 'var(--text-muted)'
                    }}
                  >
                    {isPassed ? '✓' : idx + 1}
                  </div>
                  <div>
                    <div
                      style={{
                        fontWeight: isCurrent ? '700' : '500',
                        color: isCurrent ? 'var(--primary)' : isPassed ? 'var(--text-primary)' : 'var(--text-muted)',
                        fontSize: 'var(--font-size-sm)'
                      }}
                    >
                      {step.label}
                    </div>
                    {isCurrent && (
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--primary)', marginTop: '0.15rem' }}>
                        Current status of your claim
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        ) : status === 'REJECTED' ? (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '1rem',
              padding: '1rem',
              backgroundColor: 'var(--danger-bg)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid #fecaca'
            }}
          >
            <div
              style={{
                width: '32px',
                height: '32px',
                borderRadius: '50%',
                backgroundColor: 'var(--danger)',
                color: '#ffffff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: '700'
              }}
            >
              ✕
            </div>
            <div>
              <div style={{ fontWeight: '700', color: 'var(--danger-text)', fontSize: 'var(--font-size-sm)' }}>
                Claim Formally Rejected
              </div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--danger-text)', opacity: 0.9 }}>
                The warranty adjudicator has reviewed and rejected this claim request.
              </div>
            </div>
          </div>
        ) : (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '1rem',
              padding: '1rem',
              backgroundColor: '#f1f5f9',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border)'
            }}
          >
            <div
              style={{
                width: '32px',
                height: '32px',
                borderRadius: '50%',
                backgroundColor: '#64748b',
                color: '#ffffff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: '700'
              }}
            >
              —
            </div>
            <div>
              <div style={{ fontWeight: '700', color: 'var(--text-primary)', fontSize: 'var(--font-size-sm)' }}>
                Claim Cancelled by Customer
              </div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-secondary)' }}>
                This claim was voluntarily cancelled prior to administrator review.
              </div>
            </div>
          </div>
        )}
      </div>
    );
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '4rem 1.5rem', flex: 1 }}>
        <div className="loading-container">
          <div className="spinner spinner-lg"></div>
          <span>Loading claim review details...</span>
        </div>
      </div>
    );
  }

  if (error || !claim) {
    return (
      <div className="container" style={{ padding: '4rem 1.5rem', flex: 1, maxWidth: '640px' }}>
        <div className="empty-state">
          <h2 className="empty-state-title">Access Error</h2>
          <p className="empty-state-desc">{error || 'Unable to retrieve this warranty claim.'}</p>
          <Link to="/claims" className="btn btn-secondary btn-md">
            ← Back to Claims
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '2.5rem 1.5rem 4rem', flex: 1, maxWidth: '900px' }}>
      {/* Navigation Breadcrumb */}
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to="/claims" style={{ color: 'var(--primary)', fontSize: 'var(--font-size-sm)', textDecoration: 'none', fontWeight: '500' }}>
          ← Back to All Claims
        </Link>
      </div>

      {/* Success Alert */}
      {successMessage && (
        <div
          style={{
            padding: '1rem 1.25rem',
            borderRadius: 'var(--radius-md)',
            backgroundColor: 'var(--success-bg)',
            color: 'var(--success-text)',
            marginBottom: '1.5rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.75rem',
            border: '1px solid #a7f3d0'
          }}
          role="status"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <path d="M20 6L9 17l-5-5" />
          </svg>
          <span style={{ fontWeight: '500' }}>{successMessage}</span>
        </div>
      )}

      {/* Main Claim Header */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '2rem',
          paddingBottom: '1.5rem',
          borderBottom: '1px solid var(--border)'
        }}
      >
        <div>
          <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', marginBottom: '0.25rem', fontFamily: 'monospace' }}>
            CLAIM #{claim.id}
          </div>
          <h1 style={{ fontSize: 'var(--font-size-2xl)', fontWeight: '800', color: 'var(--text-primary)', margin: '0 0 0.5rem 0' }}>
            {claim.claimReason}
          </h1>
          <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: 'var(--font-size-sm)' }}>
            Submitted for <strong>{claim.productName}</strong>
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          {renderStatusBadge(claim.status)}

          {claim.status === 'PENDING' && (
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              style={{ color: 'var(--danger)', borderColor: 'var(--danger)' }}
              onClick={() => setShowCancelModal(true)}
            >
              Cancel Claim
            </button>
          )}
        </div>
      </div>

      {/* Lifecycle Timeline */}
      {renderLifecycleTimeline(claim.status)}

      {/* Claim & Product Details Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
        {/* Left: Defect Description */}
        <div
          style={{
            backgroundColor: 'var(--surface)',
            padding: '1.5rem',
            borderRadius: 'var(--radius-lg)',
            border: '1px solid var(--border)',
            boxShadow: 'var(--shadow-sm)'
          }}
        >
          <h3 style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '1rem' }}>
            Defect Details & Description
          </h3>

          <div style={{ marginBottom: '1.25rem' }}>
            <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', display: 'block', marginBottom: '0.25rem' }}>
              Primary Reason
            </span>
            <div style={{ fontWeight: '600', color: 'var(--text-primary)' }}>
              {claim.claimReason}
            </div>
          </div>

          <div style={{ marginBottom: '1.25rem' }}>
            <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', display: 'block', marginBottom: '0.25rem' }}>
              Full Issue Description
            </span>
            <div
              style={{
                color: 'var(--text-secondary)',
                fontSize: 'var(--font-size-sm)',
                lineHeight: '1.6',
                whiteSpace: 'pre-wrap',
                backgroundColor: 'var(--background)',
                padding: '1rem',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border)'
              }}
            >
              {claim.description || 'No additional details provided.'}
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid var(--border)', paddingTop: '1rem', fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
            <span>Submitted: {formatDate(claim.createdAt)}</span>
            <span>Updated: {formatDate(claim.updatedAt)}</span>
          </div>
        </div>

        {/* Right: Associated Product Card */}
        <div
          style={{
            backgroundColor: 'var(--surface)',
            padding: '1.5rem',
            borderRadius: 'var(--radius-lg)',
            border: '1px solid var(--border)',
            boxShadow: 'var(--shadow-sm)'
          }}
        >
          <h3 style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '1rem' }}>
            Associated Equipment
          </h3>

          <div style={{ marginBottom: '1rem' }}>
            <div style={{ fontWeight: '700', fontSize: 'var(--font-size-base)', color: 'var(--text-primary)' }}>
              {claim.productName}
            </div>
            <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
              Manufacturer: {claim.brand || 'N/A'} • Model: {claim.modelNumber || 'N/A'}
            </div>
          </div>

          <div style={{ marginTop: '1.5rem', paddingTop: '1.25rem', borderTop: '1px solid var(--border)' }}>
            <Link
              to={`/products/${claim.productId}`}
              className="btn btn-secondary btn-sm"
              style={{ width: '100%', textAlign: 'center', display: 'block' }}
            >
              View Full Product Specifications →
            </Link>
          </div>
        </div>
      </div>

      {/* Cancellation Confirmation Modal */}
      {showCancelModal && (
        <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="cancel-modal-title">
          <div className="modal-dialog">
            <h3 id="cancel-modal-title" className="modal-title">Cancel Warranty Claim?</h3>
            <p className="modal-desc">
              Are you sure you want to cancel your warranty claim for <strong>{claim.productName}</strong>?
              This will transition the claim to <strong>CANCELLED</strong>.
            </p>

            {cancelError && (
              <div className="error-alert" style={{ marginBottom: '1rem' }} role="alert">
                {cancelError}
              </div>
            )}

            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => {
                  setShowCancelModal(false);
                  setCancelError('');
                }}
                disabled={cancelling}
              >
                Keep Claim
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                style={{ backgroundColor: 'var(--danger)', borderColor: 'var(--danger)' }}
                onClick={handleConfirmCancel}
                disabled={cancelling}
              >
                {cancelling ? 'Cancelling...' : 'Confirm Cancellation'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
