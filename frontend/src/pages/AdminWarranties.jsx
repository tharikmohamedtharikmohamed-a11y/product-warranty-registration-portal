import React, { useState, useEffect } from 'react';
import { adminService } from '../services/adminService';

export const AdminWarranties = () => {
  const [warranties, setWarranties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchWarranties();
  }, []);

  const fetchWarranties = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.getAllWarranties();
      setWarranties(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load system warranties.');
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'ACTIVE':
        return <span className="status-badge badge-success">Active</span>;
      case 'EXPIRING_SOON':
        return <span className="status-badge badge-warning">Expiring Soon</span>;
      case 'EXPIRED':
        return <span className="status-badge badge-danger">Expired</span>;
      default:
        return <span className="status-badge">{status}</span>;
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">System Warranties</h1>
          <p className="page-subtitle">All active and expired product warranties</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading system warranties...</p>
        </div>
      ) : warranties.length === 0 ? (
        <div className="empty-state">
          <p>No warranties found in system.</p>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Product Name</th>
                <th>Customer</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Duration</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {warranties.map((w) => (
                <tr key={w.warrantyId}>
                  <td><strong>{w.productName || 'Product'}</strong></td>
                  <td>{w.userName || 'N/A'}</td>
                  <td>{formatDate(w.startDate)}</td>
                  <td>{formatDate(w.endDate)}</td>
                  <td>{w.durationMonths ? `${w.durationMonths} Months` : 'N/A'}</td>
                  <td>{getStatusBadge(w.warrantyStatus)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
