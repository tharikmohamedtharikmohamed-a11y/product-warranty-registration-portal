import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import adminService from '../../services/adminService';
import Loading from '../../components/Loading';
import ErrorMessage from '../../components/ErrorMessage';

/**
 * Admin Claim Adjudication & Lifecycle Management Page.
 * Evaluates warranty claims, processes state machine transitions (Approve, Reject, Start, Complete),
 * and maintains internal administrative notes.
 * Phase 11 — Admin Management Module
 */
export default function AdminClaimDetailsPage() {
  const { id } = useParams();

  const [claim, setClaim] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionSuccess, setActionSuccess] = useState('');
  const [actionError, setActionError] = useState('');

  // Admin notes input
  const [adminNotes, setAdminNotes] = useState('');
  const [processing, setProcessing] = useState(false);

  // Confirmation modal state
  const [confirmModal, setConfirmModal] = useState({
    isOpen: false,
    actionType: null, // 'APPROVE' | 'REJECT' | 'START' | 'COMPLETE'
    title: '',
    message: '',
    confirmText: '',
    buttonColor: ''
  });

  const fetchClaimDetails = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await adminService.getClaimById(id);
      setClaim(data);
      setAdminNotes(data.adminNotes || '');
    } catch (err) {
      if (err.response?.status === 404) {
        setError('Warranty claim record not found.');
      } else {
        setError(err.response?.data?.message || 'Failed to retrieve claim details.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClaimDetails();
  }, [id]);

  const formatDate = (isoString) => {
    if (!isoString) return '—';
    try {
      return new Date(isoString).toLocaleDateString(undefined, {
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

  const openConfirmation = (actionType) => {
    setActionError('');
    if (actionType === 'REJECT') {
      if (!adminNotes.trim()) {
        setActionError('Please provide a rejection reason or admin note before rejecting the claim.');
        return;
      }
      setConfirmModal({
        isOpen: true,
        actionType: 'REJECT',
        title: 'Are you sure you want to reject this claim?',
        message: 'This is a terminal state. The rejection reason recorded in the admin notes will be saved with the claim record.',
        confirmText: 'Confirm Rejection',
        buttonColor: 'var(--danger)'
      });
    } else if (actionType === 'APPROVE') {
      setConfirmModal({
        isOpen: true,
        actionType: 'APPROVE',
        title: 'Approve Warranty Claim?',
        message: 'Are you sure you want to approve this defect claim? This validates coverage terms and moves the claim to APPROVED.',
        confirmText: 'Confirm Approval',
        buttonColor: 'var(--success)'
      });
    } else if (actionType === 'START') {
      setConfirmModal({
        isOpen: true,
        actionType: 'START',
        title: 'Commence Defect Processing?',
        message: 'Move this claim to IN_PROGRESS. This signifies that replacement units or repair technicians are actively mobilized.',
        confirmText: 'Start Processing',
        buttonColor: 'var(--primary)'
      });
    } else if (actionType === 'COMPLETE') {
      setConfirmModal({
        isOpen: true,
        actionType: 'COMPLETE',
        title: 'Mark this claim as completed?',
        message: 'Are you sure this defect claim is fully satisfied and resolved? This concludes the claim lifecycle.',
        confirmText: 'Finalize & Complete',
        buttonColor: 'var(--success)'
      });
    }
  };

  const handleExecuteDecision = async () => {
    if (!confirmModal.actionType) return;

    try {
      setProcessing(true);
      setActionError('');
      let updated;

      if (confirmModal.actionType === 'APPROVE') {
        updated = await adminService.approveClaim(id, adminNotes);
        setActionSuccess('Claim successfully approved.');
      } else if (confirmModal.actionType === 'REJECT') {
        updated = await adminService.rejectClaim(id, adminNotes);
        setActionSuccess('Claim successfully rejected.');
      } else if (confirmModal.actionType === 'START') {
        updated = await adminService.startClaim(id, adminNotes);
        setActionSuccess('Claim moved to IN_PROGRESS.');
      } else if (confirmModal.actionType === 'COMPLETE') {
        updated = await adminService.completeClaim(id, adminNotes);
        setActionSuccess('Claim successfully marked as COMPLETED.');
      }

      setClaim(updated);
      setAdminNotes(updated.adminNotes || '');
      setConfirmModal({ isOpen: false, actionType: null });
      setTimeout(() => setActionSuccess(''), 5000);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to update claim lifecycle state.');
    } finally {
      setProcessing(false);
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '4rem 1.5rem', flex: 1 }}>
        <Loading size="lg" text="Loading claims..." />
      </div>
    );
  }

  if (error || !claim) {
    return (
      <div className="container" style={{ padding: '4rem 1.5rem', flex: 1, maxWidth: '640px' }}>
        <div className="empty-state">
          <div className="empty-state-icon" style={{ backgroundColor: 'var(--danger-bg)', color: 'var(--danger)' }}>
            ✕
          </div>
          <h2 className="empty-state-title">Claim Not Found</h2>
          <p className="empty-state-desc">{error || 'Unable to retrieve this warranty claim record.'}</p>
          <Link to="/admin/claims" className="btn btn-secondary btn-md">
            Return to Claims Queue
          </Link>
        </div>
      </div>
    );
  }

  const isPending = claim.status === 'PENDING';
  const isApproved = claim.status === 'APPROVED';
  const isInProgress = claim.status === 'IN_PROGRESS';
  const isTerminal = claim.status === 'COMPLETED' || claim.status === 'REJECTED' || claim.status === 'CANCELLED';

  const getStepStatus = (stepIndex) => {
    // Steps: 0: Submitted, 1: Approved, 2: In Progress, 3: Completed
    if (claim.status === 'REJECTED') {
      return stepIndex === 0 ? 'completed' : stepIndex === 1 ? 'rejected' : 'upcoming';
    }
    if (claim.status === 'CANCELLED') {
      return stepIndex === 0 ? 'cancelled' : 'upcoming';
    }
    if (claim.status === 'PENDING') {
      return stepIndex === 0 ? 'active' : 'upcoming';
    }
    if (claim.status === 'APPROVED') {
      return stepIndex <= 1 ? 'completed' : stepIndex === 2 ? 'active' : 'upcoming';
    }
    if (claim.status === 'IN_PROGRESS') {
      return stepIndex <= 2 ? 'completed' : 'active';
    }
    if (claim.status === 'COMPLETED') {
      return 'completed';
    }
    return 'upcoming';
  };

  return (
    <div className="container" style={{ padding: '3rem 1.5rem', flex: 1, maxWidth: '1120px' }}>
      {/* Navigation Breadcrumbs */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <Link to="/admin/claims" className="btn btn-ghost btn-sm" style={{ paddingLeft: 0 }}>
          ← Back to Claims Queue
        </Link>
        <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
          Record ID: {claim.id}
        </span>
      </div>

      {/* Header Info */}
      <div style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem', flexWrap: 'wrap' }}>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--primary)', letterSpacing: '0.05em' }}>
            Customer Claim Adjudication
          </span>
          <span
            className={`status-badge ${
              claim.status === 'PENDING'
                ? 'badge-pending'
                : claim.status === 'APPROVED' || claim.status === 'COMPLETED'
                ? 'badge-active'
                : claim.status === 'IN_PROGRESS'
                ? 'badge-expiring'
                : 'badge-expired'
            }`}
          >
            {claim.status}
          </span>
        </div>
        <h1 className="page-title" style={{ marginBottom: '0.25rem' }}>{claim.claimReason}</h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)', margin: 0 }}>
          Submitted on {formatDate(claim.createdAt)} • Last modified {formatDate(claim.updatedAt)}
        </p>
      </div>

      {actionSuccess && (
        <div
          style={{
            padding: '0.875rem 1rem',
            borderRadius: 'var(--radius-md)',
            backgroundColor: 'var(--success-bg)',
            color: 'var(--success-text)',
            fontSize: 'var(--font-size-sm)',
            fontWeight: '600',
            marginBottom: '1.5rem'
          }}
          role="alert"
        >
          ✓ {actionSuccess}
        </div>
      )}

      {actionError && (
        <div style={{ marginBottom: '1.5rem' }}>
          <ErrorMessage message={actionError} />
        </div>
      )}

      {/* 2-Column Detail & Action Layout */}
      <div className="detail-grid">
        {/* Left Column: Metadata & Issue Description */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Customer Information Card */}
          <div className="detail-card">
            <h2 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '1rem' }}>
              Claimant Profile
            </h2>
            <div className="spec-list">
              <div className="spec-item">
                <span className="meta-label">Customer Name</span>
                <span className="meta-value" style={{ fontWeight: '600' }}>{claim.customerName || 'N/A'}</span>
              </div>
              <div className="spec-item">
                <span className="meta-label">Email Address</span>
                <span className="meta-value">{claim.customerEmail}</span>
              </div>
              <div className="spec-item">
                <span className="meta-label">Customer User ID</span>
                <span className="meta-value" style={{ fontFamily: 'monospace', fontSize: 'var(--font-size-xs)' }}>
                  {claim.userId}
                </span>
              </div>
            </div>
          </div>

          {/* Product Specifications Card */}
          <div className="detail-card">
            <h2 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '1rem' }}>
              Associated Equipment
            </h2>
            <div className="spec-list">
              <div className="spec-item">
                <span className="meta-label">Product Name</span>
                <span className="meta-value" style={{ fontWeight: '700' }}>{claim.productName}</span>
              </div>
              <div className="spec-item">
                <span className="meta-label">Brand & Model</span>
                <span className="meta-value">{claim.brand} • {claim.modelNumber}</span>
              </div>
              <div className="spec-item">
                <span className="meta-label">Product ID</span>
                <span className="meta-value" style={{ fontFamily: 'monospace', fontSize: 'var(--font-size-xs)' }}>
                  {claim.productId}
                </span>
              </div>
            </div>
          </div>

          {/* Defect Description Card */}
          <div className="detail-card">
            <h2 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '1rem' }}>
              Reported Issue & Defect Statement
            </h2>
            <div style={{ marginBottom: '1rem' }}>
              <span className="meta-label" style={{ display: 'block', marginBottom: '0.35rem' }}>Claim Reason / Title</span>
              <p style={{ fontSize: 'var(--font-size-sm)', fontWeight: '600', color: 'var(--text-primary)', margin: 0 }}>
                {claim.claimReason}
              </p>
            </div>
            <div>
              <span className="meta-label" style={{ display: 'block', marginBottom: '0.35rem' }}>Customer Elaboration</span>
              <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-secondary)', lineHeight: '1.6', margin: 0, whiteSpace: 'pre-wrap' }}>
                {claim.description || 'No additional details provided by claimant.'}
              </p>
            </div>
          </div>
        </div>

        {/* Right Column: Status Lifecycle & Adjudication Controls */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Lifecycle Progress Card */}
          <div className="detail-card">
            <h2 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '1.25rem' }}>
              Lifecycle Status Progression
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {[
                { title: '1. Submitted', desc: 'Claim received in PENDING queue', idx: 0 },
                { title: '2. Triage & Review', desc: 'Coverage confirmed; claim APPROVED', idx: 1 },
                { title: '3. Service Dispatch', desc: 'Repair/replacement IN PROGRESS', idx: 2 },
                { title: '4. Resolution', desc: 'Fulfillment concluded (COMPLETED)', idx: 3 }
              ].map((step) => {
                const status = getStepStatus(step.idx);
                return (
                  <div key={step.idx} style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
                    <div
                      style={{
                        width: '24px',
                        height: '24px',
                        borderRadius: '50%',
                        backgroundColor:
                          status === 'completed'
                            ? 'var(--success)'
                            : status === 'active'
                            ? 'var(--primary)'
                            : status === 'rejected' || status === 'cancelled'
                            ? 'var(--danger)'
                            : 'var(--border)',
                        color: '#ffffff',
                        fontSize: '0.75rem',
                        fontWeight: '700',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        flexShrink: 0
                      }}
                    >
                      {status === 'completed' ? '✓' : status === 'rejected' || status === 'cancelled' ? '✕' : step.idx + 1}
                    </div>
                    <div>
                      <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: '700', color: status === 'active' ? 'var(--primary)' : 'var(--text-primary)' }}>
                        {step.title}
                      </div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                        {step.desc}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Adjudication Decision Panel */}
          <div className="detail-card" style={{ border: '2px solid var(--primary-light)' }}>
            <h2 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '1rem' }}>
              Administrative Decision Panel
            </h2>

            {/* Admin Notes Box */}
            <div className="form-group" style={{ marginBottom: '1.25rem' }}>
              <label htmlFor="admin-notes" className="form-label">
                Internal Administrative Notes / Resolution Rationale
              </label>
              <textarea
                id="admin-notes"
                className="form-input"
                rows="4"
                placeholder="Enter internal inspection notes, technician dispatch numbers, or rejection explanation..."
                value={adminNotes}
                onChange={(e) => setAdminNotes(e.target.value)}
                disabled={isTerminal || processing}
              />
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', marginTop: '0.35rem', display: 'block' }}>
                Notes are persisted internally and visible across administrator sessions.
              </span>
            </div>

            {/* Action Buttons Based on Lifecycle State */}
            {isPending && (
              <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                <button
                  type="button"
                  className="btn btn-primary btn-md"
                  style={{ flex: 1, backgroundColor: 'var(--success)', borderColor: 'var(--success)' }}
                  onClick={() => openConfirmation('APPROVE')}
                  disabled={processing}
                >
                  ✓ Approve Claim
                </button>
                <button
                  type="button"
                  className="btn btn-primary btn-md"
                  style={{ flex: 1, backgroundColor: 'var(--danger)', borderColor: 'var(--danger)' }}
                  onClick={() => openConfirmation('REJECT')}
                  disabled={processing}
                >
                  ✕ Reject Claim
                </button>
              </div>
            )}

            {isApproved && (
              <button
                type="button"
                className="btn btn-primary btn-md"
                style={{ width: '100%' }}
                onClick={() => openConfirmation('START')}
                disabled={processing}
              >
                ⚙️ Start Processing (IN PROGRESS)
              </button>
            )}

            {isInProgress && (
              <button
                type="button"
                className="btn btn-primary btn-md"
                style={{ width: '100%', backgroundColor: 'var(--success)', borderColor: 'var(--success)' }}
                onClick={() => openConfirmation('COMPLETE')}
                disabled={processing}
              >
                ✓ Mark Claim as COMPLETED
              </button>
            )}

            {isTerminal && (
              <div
                style={{
                  padding: '0.875rem',
                  borderRadius: 'var(--radius-md)',
                  backgroundColor: 'var(--background)',
                  border: '1px solid var(--border)',
                  color: 'var(--text-secondary)',
                  fontSize: 'var(--font-size-xs)',
                  lineHeight: '1.5'
                }}
              >
                <strong>Terminal Status:</strong> This warranty claim has reached a final disposition ({claim.status}).
                In accordance with state machine safety rules, no further transitions are permitted.
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Confirmation Modal */}
      {confirmModal.isOpen && (
        <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="confirm-modal-title">
          <div className="modal-dialog">
            <h3 id="confirm-modal-title" className="modal-title">{confirmModal.title}</h3>
            <p className="modal-desc">{confirmModal.message}</p>

            {adminNotes && (
              <div style={{ backgroundColor: 'var(--background)', padding: '0.75rem', borderRadius: 'var(--radius-sm)', marginBottom: '1.25rem', border: '1px solid var(--border)', fontSize: 'var(--font-size-xs)' }}>
                <strong>Attaching Admin Note:</strong> "{adminNotes}"
              </div>
            )}

            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => setConfirmModal({ isOpen: false, actionType: null })}
                disabled={processing}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                style={{ backgroundColor: confirmModal.buttonColor, borderColor: confirmModal.buttonColor }}
                onClick={handleExecuteDecision}
                disabled={processing}
              >
                {processing ? 'Updating...' : confirmModal.confirmText}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
