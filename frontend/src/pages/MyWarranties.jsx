import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { warrantyService } from '../services/warrantyService';

export const MyWarranties = () => {
  const [warranties, setWarranties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchWarranties = async () => {
      try {
        setLoading(true);
        setError('');
        const data = await warrantyService.getUserWarranties();
        setWarranties(data || []);
      } catch (err) {
        setError(err.message || 'Unable to load warranties. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchWarranties();
  }, []);

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
    return diffDays > 0 ? `${diffDays} days` : 'Expired';
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">My Warranties</h1>
        <p className="page-subtitle">Track warranty coverage and expiration dates for your products.</p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <div className="loading-spinner" style={{ borderColor: 'var(--primary)', borderTopColor: 'transparent', width: '32px', height: '32px' }}></div>
          <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading warranties...</p>
        </div>
      ) : warranties.length === 0 ? (
        <div className="empty-state">
          <span className="empty-icon">🛡️</span>
          <h3 className="empty-title">No warranties found</h3>
          <p className="empty-desc">You do not have any registered warranties linked to your account.</p>
        </div>
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Product Name</th>
                <th>Brand</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Duration</th>
                <th>Status</th>
                <th>Days Remaining</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {warranties.map((w) => (
                <tr key={w.warrantyId}>
                  <td><strong>{w.productName || 'Product'}</strong></td>
                  <td>{w.brand || 'N/A'}</td>
                  <td>{w.warrantyStartDate ? new Date(w.warrantyStartDate).toLocaleDateString() : 'N/A'}</td>
                  <td>{w.warrantyEndDate ? new Date(w.warrantyEndDate).toLocaleDateString() : 'N/A'}</td>
                  <td>{w.warrantyDurationMonths ? `${w.warrantyDurationMonths} Months` : 'N/A'}</td>
                  <td>{renderStatusBadge(w.status)}</td>
                  <td>
                    <span style={{ fontWeight: 600, color: w.status === 'EXPIRED' ? 'var(--error)' : 'var(--text-dark)' }}>
                      {calculateDaysRemaining(w.warrantyEndDate)}
                    </span>
                  </td>
                  <td>
                    <Link to={`/warranties/${w.warrantyId}`} className="btn btn-secondary btn-sm">
                      View Details
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
