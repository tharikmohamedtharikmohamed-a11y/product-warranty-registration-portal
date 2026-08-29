import React, { useState } from 'react';
import claimService from '../services/claimService';

/**
 * Submit Warranty Claim Modal Component.
 * Supports selecting reason, entering comprehensive defect details,
 * frontend validation, loading states, and error handling.
 * Phase 10 — Warranty Claims
 */
export default function SubmitClaimModal({ product, warranty, onSuccess, onClose }) {
  const [claimReason, setClaimReason] = useState('');
  const [customReason, setCustomReason] = useState('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  const isExpired = warranty?.status === 'EXPIRED';

  const COMMON_REASONS = [
    'Hardware / Component Failure',
    'Display / Screen Malfunction',
    'Power / Battery Defect',
    'Audio / Speaker Issue',
    'Connectivity / Network Failure',
    'Mechanical / Physical Defect',
    'Other / Specific Problem'
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    setFieldErrors({});

    if (isExpired) {
      setFormError('Warranty has expired. A new claim cannot be submitted.');
      return;
    }

    const errors = {};
    const effectiveReason = claimReason === 'Other / Specific Problem' ? customReason.trim() : claimReason.trim();

    if (!effectiveReason) {
      errors.claimReason = 'Claim reason is required.';
    }

    if (!description.trim()) {
      errors.description = 'Please describe the defect or issue in detail.';
    } else if (description.trim().length < 10) {
      errors.description = 'Please provide a more detailed description (at least 10 characters).';
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    try {
      setSubmitting(true);
      const newClaim = await claimService.createClaim({
        productId: product.id,
        claimReason: effectiveReason,
        description: description.trim()
      });

      if (onSuccess) {
        onSuccess(newClaim);
      }
      if (onClose) {
        onClose();
      }
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to submit claim. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="claim-modal-title">
      <div className="modal-dialog" style={{ maxWidth: '580px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h3 id="claim-modal-title" className="modal-title" style={{ margin: 0 }}>
            File Warranty Claim
          </h3>
          <button
            type="button"
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '1.25rem',
              cursor: 'pointer',
              color: 'var(--text-muted)'
            }}
            aria-label="Close modal"
          >
            ✕
          </button>
        </div>

        {/* Product Summary Header */}
        <div
          style={{
            padding: '0.875rem 1rem',
            borderRadius: 'var(--radius-md)',
            backgroundColor: 'var(--surface-hover)',
            border: '1px solid var(--border)',
            marginBottom: '1.25rem',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
          }}
        >
          <div>
            <div style={{ fontWeight: '700', color: 'var(--text-primary)', fontSize: 'var(--font-size-sm)' }}>
              {product?.productName || 'Registered Equipment'}
            </div>
            <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
              {product?.brand} • Model {product?.modelNumber || 'N/A'} • SN: {product?.serialNumber}
            </div>
          </div>

          <div>
            <span
              style={{
                backgroundColor: isExpired ? 'var(--danger-bg)' : 'var(--success-bg)',
                color: isExpired ? 'var(--danger-text)' : 'var(--success-text)',
                fontSize: 'var(--font-size-xs)',
                fontWeight: '700',
                padding: '0.2rem 0.5rem',
                borderRadius: 'var(--radius-sm)'
              }}
            >
              {warranty?.status === 'ACTIVE' ? 'Active Coverage' : warranty?.status === 'EXPIRING_SOON' ? 'Expiring Soon' : 'Expired'}
            </span>
          </div>
        </div>

        {isExpired && (
          <div className="error-alert" style={{ marginBottom: '1.25rem' }} role="alert">
            Warranty expired on {warranty?.expiryDate || 'N/A'} — new claims cannot be submitted for this product.
          </div>
        )}

        {formError && (
          <div className="error-alert" style={{ marginBottom: '1.25rem' }} role="alert">
            {formError}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Claim Reason */}
          <div className="form-group" style={{ marginBottom: '1rem' }}>
            <label htmlFor="claimReason" className="form-label">
              Claim Reason / Primary Issue <span style={{ color: 'var(--danger)' }}>*</span>
            </label>
            <select
              id="claimReason"
              className={`form-control ${fieldErrors.claimReason ? 'is-invalid' : ''}`}
              value={claimReason}
              onChange={(e) => {
                setClaimReason(e.target.value);
                setFieldErrors((prev) => ({ ...prev, claimReason: '' }));
              }}
              disabled={submitting || isExpired}
            >
              <option value="">-- Select issue category --</option>
              {COMMON_REASONS.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
            {fieldErrors.claimReason && (
              <span className="form-error" style={{ color: 'var(--danger)', fontSize: 'var(--font-size-xs)' }}>
                {fieldErrors.claimReason}
              </span>
            )}
          </div>

          {/* Custom Reason if "Other" selected */}
          {claimReason === 'Other / Specific Problem' && (
            <div className="form-group" style={{ marginBottom: '1rem' }}>
              <label htmlFor="customReason" className="form-label">
                Specify Problem Title <span style={{ color: 'var(--danger)' }}>*</span>
              </label>
              <input
                id="customReason"
                type="text"
                className="form-control"
                placeholder="E.g., Device reboots randomly during operation"
                value={customReason}
                onChange={(e) => setCustomReason(e.target.value)}
                disabled={submitting || isExpired}
                maxLength={200}
              />
            </div>
          )}

          {/* Description */}
          <div className="form-group" style={{ marginBottom: '1.25rem' }}>
            <label htmlFor="claimDescription" className="form-label">
              Detailed Description of Problem <span style={{ color: 'var(--danger)' }}>*</span>
            </label>
            <textarea
              id="claimDescription"
              rows="4"
              className={`form-control ${fieldErrors.description ? 'is-invalid' : ''}`}
              placeholder="Describe symptoms, steps to reproduce, or troubleshooting steps already tried..."
              value={description}
              onChange={(e) => {
                setDescription(e.target.value);
                setFieldErrors((prev) => ({ ...prev, description: '' }));
              }}
              disabled={submitting || isExpired}
              maxLength={4000}
            />
            {fieldErrors.description && (
              <span className="form-error" style={{ color: 'var(--danger)', fontSize: 'var(--font-size-xs)' }}>
                {fieldErrors.description}
              </span>
            )}
          </div>

          {/* Actions */}
          <div className="modal-actions">
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={onClose}
              disabled={submitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary btn-sm"
              disabled={submitting || isExpired}
              style={{ minWidth: '130px' }}
            >
              {submitting ? (
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <div className="spinner spinner-sm"></div>
                  Submitting claim...
                </span>
              ) : (
                'Submit Claim'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
