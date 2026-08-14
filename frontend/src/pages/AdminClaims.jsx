import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { adminService } from '../services/adminService';

export const AdminClaims = () => {
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchClaims();
  }, []);

  const fetchClaims = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.getAllClaims();
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

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const filteredClaims = claims.filter((claim) => {
    const status = claim.claimStatus || claim.status;
    const matchesStatus = statusFilter === 'ALL' || status === statusFilter;

    if (!matchesStatus) return false;

    if (!searchTerm.trim()) return true;

    const term = searchTerm.toLowerCase();
    const claimIdMatch = claim.claimId?.toLowerCase().includes(term);
    const userNameMatch = claim.userName?.toLowerCase().includes(term);
    const productNameMatch = claim.productName?.toLowerCase().includes(term);
    const issueMatch = claim.issueDescription?.toLowerCase().includes(term);

    return claimIdMatch || userNameMatch || productNameMatch || issueMatch;
  });

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Manage Warranty Claims</h1>
          <p className="page-subtitle">Review, update status, and manage all customer claims</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {/* Filter and Search Bar */}
      <div className="filter-bar-container" style={{ marginBottom: '1.5rem' }}>
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
            Pending ({claims.filter((c) => (c.claimStatus || c.status) === 'PENDING').length})
          </button>
          <button
            className={`filter-btn ${statusFilter === 'IN_PROGRESS' ? 'active' : ''}`}
            onClick={() => setStatusFilter('IN_PROGRESS')}
          >
            In Progress ({claims.filter((c) => (c.claimStatus || c.status) === 'IN_PROGRESS').length})
          </button>
          <button
            className={`filter-btn ${statusFilter === 'APPROVED' ? 'active' : ''}`}
            onClick={() => setStatusFilter('APPROVED')}
          >
            Approved ({claims.filter((c) => (c.claimStatus || c.status) === 'APPROVED').length})
          </button>
          <button
            className={`filter-btn ${statusFilter === 'REJECTED' ? 'active' : ''}`}
            onClick={() => setStatusFilter('REJECTED')}
          >
            Rejected ({claims.filter((c) => (c.claimStatus || c.status) === 'REJECTED').length})
          </button>
          <button
            className={`filter-btn ${statusFilter === 'COMPLETED' ? 'active' : ''}`}
            onClick={() => setStatusFilter('COMPLETED')}
          >
            Completed ({claims.filter((c) => (c.claimStatus || c.status) === 'COMPLETED').length})
          </button>
          <button
            className={`filter-btn ${statusFilter === 'CANCELLED' ? 'active' : ''}`}
            onClick={() => setStatusFilter('CANCELLED')}
          >
            Cancelled ({claims.filter((c) => (c.claimStatus || c.status) === 'CANCELLED').length})
          </button>
        </div>

        <div className="search-box" style={{ marginTop: '1rem' }}>
          <input
            type="text"
            className="form-control"
            placeholder="Search by Claim ID, Customer Name, or Product Name..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading warranty claims...</p>
        </div>
      ) : filteredClaims.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">🛠️</div>
          <h3>No warranty claims found</h3>
          <p>
            {searchTerm
              ? `No claims matched search term '${searchTerm}'.`
              : statusFilter === 'ALL'
              ? 'No warranty claims have been submitted by customers yet.'
              : `No claims found with status '${statusFilter}'.`}
          </p>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Claim ID</th>
                <th>Customer Name</th>
                <th>Product Name</th>
                <th>Submitted Date</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredClaims.map((claim) => (
                <tr key={claim.claimId}>
                  <td className="font-mono">{claim.claimId ? claim.claimId.substring(0, 8) + '...' : 'N/A'}</td>
                  <td>{claim.userName || 'Customer'}</td>
                  <td>
                    <strong>{claim.productName || 'Product'}</strong>
                  </td>
                  <td>{formatDate(claim.createdAt)}</td>
                  <td>{getStatusBadge(claim.claimStatus || claim.status)}</td>
                  <td>
                    <button
                      onClick={() => navigate(`/admin/claims/${claim.claimId}`)}
                      className="btn btn-primary btn-sm"
                    >
                      Process Claim
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
