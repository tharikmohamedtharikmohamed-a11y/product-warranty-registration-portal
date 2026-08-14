import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { warrantyService } from '../services/warrantyService';

export const WarrantyDetails = () => {
  const { id } = useParams();
  const [warranty, setWarranty] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchWarranty = async () => {
      try {
        setLoading(true);
        setError('');
        const data = await warrantyService.getWarrantyById(id);
        setWarranty(data);
      } catch (err) {
        setError(err.message || 'Warranty details not found or access denied.');
      } finally {
        setLoading(false);
      }
    };

    fetchWarranty();
  }, [id]);

  const renderStatusBadge = (status) => {
    switch (status) {
      case 'ACTIVE':
        return <span className="badge badge-active">Active</span>;
      case 'EXPIRING_SOON':
        return <span className="badge badge-expiring">Expiring Soon</span>;
      case 'EXPIRED':
        return <span className="badge badge-expired">Expired</span>;
      default:
        return <span className="badge">{status || 'N/A'}</span>;
    }
  };

  const calculateDaysRemaining = (endDateStr) => {
    if (!endDateStr) return 'N/A';
    const endDate = new Date(endDateStr);
    const today = new Date();
    const diffTime = endDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays > 0 ? `${diffDays} days` : '0 days (Expired)';
  };

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title">Warranty Overview</h1>
          <p className="page-subtitle">Detailed coverage specifications and valid timeframe.</p>
        </div>
        <Link to="/warranties" className="btn btn-secondary btn-sm">
          ← Back to Warranties
        </Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <div className="loading-spinner" style={{ borderColor: 'var(--primary)', borderTopColor: 'transparent', width: '32px', height: '32px' }}></div>
          <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading warranty details...</p>
        </div>
      ) : warranty ? (
        <div className="details-card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>{warranty.productName || 'Product Warranty'}</h2>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
                Brand: {warranty.brand || 'N/A'}
              </p>
            </div>
            <div>{renderStatusBadge(warranty.status)}</div>
          </div>

          <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '1.5rem 0' }} />

          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>Coverage Breakdown</h3>
          <div className="details-grid">
            <div className="details-item">
              <span className="details-label">Warranty Start Date</span>
              <span className="details-value">
                {warranty.warrantyStartDate ? new Date(warranty.warrantyStartDate).toLocaleDateString() : 'N/A'}
              </span>
            </div>
            <div className="details-item">
              <span className="details-label">Warranty End Date</span>
              <span className="details-value">
                {warranty.warrantyEndDate ? new Date(warranty.warrantyEndDate).toLocaleDateString() : 'N/A'}
              </span>
            </div>
            <div className="details-item">
              <span className="details-label">Total Duration</span>
              <span className="details-value">
                {warranty.warrantyDurationMonths ? `${warranty.warrantyDurationMonths} Months` : 'N/A'}
              </span>
            </div>
            <div className="details-item">
              <span className="details-label">Time Remaining</span>
              <span className="details-value">{calculateDaysRemaining(warranty.warrantyEndDate)}</span>
            </div>
            <div className="details-item">
              <span className="details-label">Purchase Date</span>
              <span className="details-value">
                {warranty.purchaseDate ? new Date(warranty.purchaseDate).toLocaleDateString() : 'N/A'}
              </span>
            </div>
          </div>

          {warranty.productId && (
            <div style={{ marginTop: '2rem', display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
              <Link to={`/products/${warranty.productId}`} className="btn btn-secondary btn-inline">
                View Linked Product
              </Link>
              {warranty.status !== 'EXPIRED' ? (
                <Link
                  to={`/claims/submit?productId=${warranty.productId}&warrantyId=${warranty.warrantyId}`}
                  className="btn btn-primary btn-inline"
                >
                  🛠️ Submit Warranty Claim
                </Link>
              ) : (
                <button disabled className="btn btn-secondary btn-inline" style={{ opacity: 0.6, cursor: 'not-allowed' }}>
                  Warranty Expired (Claim Ineligible)
                </button>
              )}
            </div>
          )}
        </div>
      ) : null}
    </div>
  );
};
