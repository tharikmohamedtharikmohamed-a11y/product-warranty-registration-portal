import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { claimService } from '../services/claimService';

export const MyClaims = () => {
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const navigate = useNavigate();

  useEffect(() => {
    fetchClaims();
  }, []);

  const fetchClaims = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await claimService.getUserClaims();
      setClaims(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load warranty claims.');
    } finally {
      setLoading(false);
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

  const filteredClaims = claims.filter((claim) => {
    if (statusFilter === 'ALL') return true;
    return claim.status === statusFilter;
  });

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
          <h1 className="page-title">My Warranty Claims</h1>
          <p className="page-subtitle">Track and manage your submitted warranty claims</p>
        </div>
        <Link to="/claims/submit" className="btn btn-primary">
          + Submit Warranty Claim
        </Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {/* Filter Tabs */}
      <div className="filter-bar">
        <button
          className={`filter-btn ${statusFilter === 'ALL' ? 'active' : ''}`}
          onClick={() => setStatusFilter('ALL')}
        >
          All ({claims.length})
        </button>
        <button
          className={`filter-btn ${statusFilter === 'PENDING' ? 'active' : ''}`}
          onClick={() => setStatusFilter('PENDING')}
        >
          Pending ({claims.filter((c) => c.status === 'PENDING').length})
        </button>
        <button
          className={`filter-btn ${statusFilter === 'IN_PROGRESS' ? 'active' : ''}`}
          onClick={() => setStatusFilter('IN_PROGRESS')}
        >
          In Progress ({claims.filter((c) => c.status === 'IN_PROGRESS').length})
        </button>
        <button
          className={`filter-btn ${statusFilter === 'APPROVED' ? 'active' : ''}`}
          onClick={() => setStatusFilter('APPROVED')}
        >
          Approved ({claims.filter((c) => c.status === 'APPROVED').length})
        </button>
        <button
          className={`filter-btn ${statusFilter === 'REJECTED' ? 'active' : ''}`}
          onClick={() => setStatusFilter('REJECTED')}
        >
          Rejected ({claims.filter((c) => c.status === 'REJECTED').length})
        </button>
        <button
          className={`filter-btn ${statusFilter === 'COMPLETED' ? 'active' : ''}`}
          onClick={() => setStatusFilter('COMPLETED')}
        >
          Completed ({claims.filter((c) => c.status === 'COMPLETED').length})
        </button>
        <button
          className={`filter-btn ${statusFilter === 'CANCELLED' ? 'active' : ''}`}
          onClick={() => setStatusFilter('CANCELLED')}
        >
          Cancelled ({claims.filter((c) => c.status === 'CANCELLED').length})
        </button>
      </div>

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading claims...</p>
        </div>
      ) : filteredClaims.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">🛠️</div>
          <h3>No warranty claims found</h3>
          <p>
            {statusFilter === 'ALL'
              ? "You haven't submitted any warranty claims yet."
              : `No claims found with status '${statusFilter}'.`}
          </p>
          <Link to="/claims/submit" className="btn btn-primary">
            Submit Warranty Claim
          </Link>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Claim ID</th>
                <th>Product</th>
                <th>Submitted Date</th>
                <th>Status</th>
                <th>Issue Summary</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredClaims.map((claim) => (
                <tr key={claim.claimId}>
                  <td className="font-mono">{claim.claimId ? claim.claimId.substring(0, 8) + '...' : 'N/A'}</td>
                  <td>
                    <strong>{claim.productName || 'Product'}</strong>
                  </td>
                  <td>{formatDate(claim.createdAt)}</td>
                  <td>{getStatusBadge(claim.status)}</td>
                  <td className="truncate-text" style={{ maxWidth: '250px' }}>
                    {claim.issueDescription}
                  </td>
                  <td>
                    <button
                      onClick={() => navigate(`/claims/${claim.claimId}`)}
                      className="btn btn-secondary btn-sm"
                    >
                      View Details
                    </button>
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
