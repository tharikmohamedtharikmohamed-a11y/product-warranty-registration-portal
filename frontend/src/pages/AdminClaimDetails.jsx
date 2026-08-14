import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { adminService } from '../services/adminService';

export const AdminClaimDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [claim, setClaim] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  const [newStatus, setNewStatus] = useState('');
  const [resolutionNotes, setResolutionNotes] = useState('');
  const [invoiceDownloadUrl, setInvoiceDownloadUrl] = useState(null);
  const [loadingInvoice, setLoadingInvoice] = useState(false);

  useEffect(() => {
    fetchClaimDetails();
  }, [id]);

  const fetchClaimDetails = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.getClaimById(id);
      setClaim(data);
      setNewStatus(data.claimStatus || data.status || '');
      setResolutionNotes(data.resolutionNotes || '');

      if (data.invoiceId) {
        fetchInvoiceDownload(data.invoiceId);
      }
    } catch (err) {
      setError(err.message || 'Failed to load claim details.');
    } finally {
      setLoading(false);
    }
  };

  const fetchInvoiceDownload = async (invoiceId) => {
    setLoadingInvoice(true);
    try {
      const res = await adminService.getInvoiceDownload(invoiceId);
      setInvoiceDownloadUrl(res.downloadUrl || res.url);
    } catch (e) {
      console.warn('Could not load invoice download link', e);
    } finally {
      setLoadingInvoice(false);
    }
  };

  const handleUpdateStatus = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccessMessage(null);

    if (!newStatus) {
      setError('Please select a valid claim status.');
      return;
    }

    setUpdating(true);
    try {
      const payload = {
        status: newStatus,
        resolutionNotes: resolutionNotes.trim(),
      };

      const updated = await adminService.updateClaimStatus(id, payload);
      setClaim(updated);
      setNewStatus(updated.claimStatus || updated.status || newStatus);
      setResolutionNotes(updated.resolutionNotes || resolutionNotes);
      setSuccessMessage('Claim status updated successfully.');
    } catch (err) {
      setError(err.message || 'Failed to update claim status.');
    } finally {
      setUpdating(false);
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

  if (error && !claim) {
    return (
      <div className="page-container">
        <div className="page-header">
          <h1 className="page-title">Process Claim</h1>
          <Link to="/admin/claims" className="btn btn-secondary btn-sm">
            ← Back to Claims
          </Link>
        </div>
        <div className="alert alert-error">{error}</div>
      </div>
    );
  }

  const currentStatus = claim?.claimStatus || claim?.status;

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Process Warranty Claim</h1>
          <p className="page-subtitle">Claim ID: {claim.claimId}</p>
        </div>
        <Link to="/admin/claims" className="btn btn-secondary">
          ← Back to All Claims
        </Link>
      </div>

      {successMessage && <div className="alert alert-success">{successMessage}</div>}
      {error && <div className="alert alert-error">{error}</div>}

      <div className="details-grid">
        {/* Status Update Form Card */}
        <div className="detail-card full-width highlight-card">
          <h2 className="card-title">Update Claim Status & Resolution</h2>
          <form onSubmit={handleUpdateStatus}>
            <div className="form-group">
              <label htmlFor="claimStatus" className="form-label">
                Claim Status <span className="required">*</span>
              </label>
              <select
                id="claimStatus"
                className="form-control"
                value={newStatus}
                onChange={(e) => setNewStatus(e.target.value)}
                disabled={updating}
                required
              >
                <option value="PENDING">PENDING (Submitted by customer)</option>
                <option value="IN_PROGRESS">IN_PROGRESS (Under inspection / processing)</option>
                <option value="APPROVED">APPROVED (Warranty replacement/repair approved)</option>
                <option value="REJECTED">REJECTED (Claim declined)</option>
                <option value="COMPLETED">COMPLETED (Resolution fulfilled)</option>
                <option value="CANCELLED">CANCELLED (Cancelled)</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="resolutionNotes" className="form-label">
                Resolution / Admin Notes
              </label>
              <textarea
                id="resolutionNotes"
                className="form-control"
                rows="4"
                placeholder="Enter details regarding approval, replacement, repair status, or reason for rejection..."
                value={resolutionNotes}
                onChange={(e) => setResolutionNotes(e.target.value)}
                disabled={updating}
              ></textarea>
            </div>

            <button type="submit" className="btn btn-primary" disabled={updating}>
              {updating ? 'Updating Status...' : 'Save & Update Status'}
            </button>
          </form>
        </div>

        {/* Claim Info Card */}
        <div className="detail-card">
          <h2 className="card-title">Claim Information</h2>
          <div className="detail-row">
            <span className="detail-label">Current Status:</span>
            <span className="detail-value">{getStatusBadge(currentStatus)}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Submitted Date:</span>
            <span className="detail-value">{formatDate(claim.createdAt)}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Last Updated:</span>
            <span className="detail-value">{formatDate(claim.updatedAt)}</span>
          </div>
        </div>

        {/* Customer Information Card */}
        <div className="detail-card">
          <h2 className="card-title">Customer Information</h2>
          <div className="detail-row">
            <span className="detail-label">Customer Name:</span>
            <span className="detail-value"><strong>{claim.userName || 'N/A'}</strong></span>
          </div>
          <div className="detail-row">
            <span className="detail-label">User ID:</span>
            <span className="detail-value font-mono">{claim.userId ? claim.userId.substring(0, 8) + '...' : 'N/A'}</span>
          </div>
        </div>

        {/* Product Information Card */}
        <div className="detail-card">
          <h2 className="card-title">Product & Warranty Info</h2>
          <div className="detail-row">
            <span className="detail-label">Product Name:</span>
            <span className="detail-value"><strong>{claim.productName || 'N/A'}</strong></span>
          </div>
          {claim.productId && (
            <div className="detail-row">
              <span className="detail-label">Product ID:</span>
              <span className="detail-value font-mono">{claim.productId.substring(0, 8)}...</span>
            </div>
          )}
          {claim.warrantyId && (
            <div className="detail-row">
              <span className="detail-label">Warranty ID:</span>
              <span className="detail-value font-mono">{claim.warrantyId.substring(0, 8)}...</span>
            </div>
          )}
          {claim.invoiceId && (
            <div className="detail-row">
              <span className="detail-label">Attached Invoice:</span>
              <span className="detail-value">
                {invoiceDownloadUrl ? (
                  <a href={invoiceDownloadUrl} target="_blank" rel="noopener noreferrer" className="link-text">
                    📄 Download Customer Invoice
                  </a>
                ) : loadingInvoice ? (
                  'Generating download link...'
                ) : (
                  'Attached'
                )}
              </span>
            </div>
          )}
        </div>

        {/* Issue Description Card */}
        <div className="detail-card full-width">
          <h2 className="card-title">Customer Issue Description</h2>
          <div className="description-box">
            <p>{claim.issueDescription}</p>
          </div>
        </div>
      </div>
    </div>
  );
};
