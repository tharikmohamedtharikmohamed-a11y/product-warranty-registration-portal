import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation, Link } from 'react-router-dom';
import { claimService } from '../services/claimService';

export const ClaimDetails = () => {
  const { id } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  const [claim, setClaim] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [cancelling, setCancelling] = useState(false);
  const [successMessage, setSuccessMessage] = useState(location.state?.message || null);

  useEffect(() => {
    fetchClaimDetails();
  }, [id]);

  const fetchClaimDetails = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await claimService.getClaimById(id);
      setClaim(data);
    } catch (err) {
      setError(err.message || 'Failed to load warranty claim details.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelClaim = async () => {
    if (!window.confirm('Are you sure you want to cancel this warranty claim?')) {
      return;
    }

    setCancelling(true);
    setError(null);
    try {
      const updated = await claimService.cancelClaim(id);
      setClaim(updated);
      setSuccessMessage('Warranty claim cancelled successfully.');
    } catch (err) {
      setError(err.message || 'Failed to cancel warranty claim.');
    } finally {
      setCancelling(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'PENDING':
        return <span className="status-badge badge-warning">Pending</span>;
      case 'IN_PROGRESS':
        return <span className="status-badge badge-info">In Progress</span>;
      case 'APPROVED':
        return <span className="status-badge badge-success">Approved</span>;
      case 'REJECTED':
        return <span className="status-badge badge-danger">Rejected</span>;
      case 'COMPLETED':
        return <span className="status-badge badge-success">Completed</span>;
      case 'CANCELLED':
        return <span className="status-badge badge-secondary">Cancelled</span>;
      default:
        return <span className="status-badge">{status}</span>;
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading claim details...</p>
        </div>
      </div>
    );
  }

  if (error || !claim) {
    return (
      <div className="page-container">
        <div className="page-header">
          <h1 className="page-title">Claim Details</h1>
          <Link to="/claims" className="btn btn-secondary btn-sm">
            ← Back to Claims
          </Link>
        </div>
        <div className="alert alert-error">{error || 'Warranty claim not found.'}</div>
      </div>
    );
  }

  const canCancel = claim.status === 'PENDING' || claim.status === 'IN_PROGRESS';

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Warranty Claim Details</h1>
          <p className="page-subtitle">Claim ID: {claim.claimId}</p>
        </div>
        <div className="flex-gap">
          <Link to="/claims" className="btn btn-secondary">
            ← Back to Claims
          </Link>
          {canCancel && (
            <button
              onClick={handleCancelClaim}
              className="btn btn-danger"
              disabled={cancelling}
            >
              {cancelling ? 'Cancelling...' : 'Cancel Claim'}
            </button>
          )}
        </div>
      </div>

      {successMessage && <div className="alert alert-success">{successMessage}</div>}

      <div className="details-grid">
        {/* Claim Status Card */}
        <div className="detail-card">
          <h2 className="card-title">Status & Timeline</h2>
          <div className="detail-row">
            <span className="detail-label">Current Status:</span>
            <span className="detail-value">{getStatusBadge(claim.status)}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Submitted On:</span>
            <span className="detail-value">{formatDate(claim.createdAt)}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Last Updated:</span>
            <span className="detail-value">{formatDate(claim.updatedAt)}</span>
          </div>
        </div>

        {/* Product Information Card */}
        <div className="detail-card">
          <h2 className="card-title">Product Information</h2>
          <div className="detail-row">
            <span className="detail-label">Product Name:</span>
            <span className="detail-value">
              {claim.productId ? (
                <Link to={`/products/${claim.productId}`} className="link-text">
                  {claim.productName || 'View Product'}
                </Link>
              ) : (
                claim.productName || 'N/A'
              )}
            </span>
          </div>
          {claim.warrantyId && (
            <div className="detail-row">
              <span className="detail-label">Warranty ID:</span>
              <span className="detail-value font-mono">
                <Link to={`/warranties/${claim.warrantyId}`} className="link-text">
                  {claim.warrantyId.substring(0, 8)}...
                </Link>
              </span>
            </div>
          )}
        </div>

        {/* Issue Description Card */}
        <div className="detail-card full-width">
          <h2 className="card-title">Issue Description</h2>
          <div className="description-box">
            <p>{claim.issueDescription}</p>
          </div>
        </div>

        {/* Resolution Notes (if any) */}
        {claim.resolutionNotes && (
          <div className="detail-card full-width highlight-card">
            <h2 className="card-title">Admin Resolution Notes</h2>
            <div className="description-box">
              <p>{claim.resolutionNotes}</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
