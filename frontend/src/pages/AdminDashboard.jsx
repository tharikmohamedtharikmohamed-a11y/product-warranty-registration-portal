import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { adminService } from '../services/adminService';

export const AdminDashboard = () => {
  const [stats, setStats] = useState(null);
  const [recentClaims, setRecentClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [statsData, claimsData] = await Promise.all([
        adminService.getDashboardStats(),
        adminService.getAllClaims().catch(() => []),
      ]);
      setStats(statsData);
      setRecentClaims(claimsData || []);
    } catch (err) {
      setError(err.message || 'Unable to load admin dashboard data.');
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

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading Admin Dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Admin Dashboard</h1>
          <p className="page-subtitle">System-wide overview, claims processing, and portal management</p>
        </div>
        <div className="flex-gap">
          <Link to="/admin/claims" className="btn btn-primary">
            Manage Warranty Claims
          </Link>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {stats && (
        <>
          {/* Main Statistics Grid */}
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon-wrapper icon-blue">👥</div>
              <div>
                <div className="stat-number">{stats.totalUsers}</div>
                <div className="stat-label">Total Users</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon-wrapper icon-blue">📦</div>
              <div>
                <div className="stat-number">{stats.totalProducts}</div>
                <div className="stat-label">Total Products</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon-wrapper icon-green">🛡️</div>
              <div>
                <div className="stat-number">{stats.activeWarranties}</div>
                <div className="stat-label">Active Warranties</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon-wrapper icon-amber">📄</div>
              <div>
                <div className="stat-number">{stats.totalInvoices}</div>
                <div className="stat-label">Uploaded Invoices</div>
              </div>
            </div>
          </div>

          {/* Warranty Claim Breakdown */}
          <h2 className="section-title" style={{ marginTop: '2rem' }}>
            Warranty Claims Overview
          </h2>
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon-wrapper icon-amber">⏳</div>
              <div>
                <div className="stat-number">{stats.pendingClaims}</div>
                <div className="stat-label">Pending Claims</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon-wrapper icon-blue">⚙️</div>
              <div>
                <div className="stat-number">{stats.inProgressClaims}</div>
                <div className="stat-label">In Progress</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon-wrapper icon-green">✅</div>
              <div>
                <div className="stat-number">{stats.approvedClaims + stats.completedClaims}</div>
                <div className="stat-label">Approved & Completed</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon-wrapper icon-red">❌</div>
              <div>
                <div className="stat-number">{stats.rejectedClaims}</div>
                <div className="stat-label">Rejected Claims</div>
              </div>
            </div>
          </div>

          {/* Quick Navigation Cards */}
          <div className="grid-3-col" style={{ marginTop: '2rem', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.25rem' }}>
            <Link to="/admin/claims" className="detail-card link-card">
              <h3>🛠️ Manage Claims ({stats.totalClaims})</h3>
              <p>Process customer claims, update status, and add resolution notes.</p>
            </Link>
            <Link to="/admin/customers" className="detail-card link-card">
              <h3>👥 Customers List ({stats.totalUsers})</h3>
              <p>View registered user accounts and contact info.</p>
            </Link>
            <Link to="/admin/products" className="detail-card link-card">
              <h3>📦 All Products ({stats.totalProducts})</h3>
              <p>Inspect registered products across all users.</p>
            </Link>
            <Link to="/admin/warranties" className="detail-card link-card">
              <h3>🛡️ All Warranties ({stats.totalWarranties})</h3>
              <p>View system warranties and expiration status.</p>
            </Link>
          </div>

          {/* Recent Claims Section */}
          <div className="dashboard-section" style={{ marginTop: '2.5rem' }}>
            <div className="section-header">
              <h2 className="section-title">Recent Customer Claims</h2>
              <Link to="/admin/claims" className="btn btn-secondary btn-sm">
                View All Claims ({recentClaims.length})
              </Link>
            </div>

            {recentClaims.length === 0 ? (
              <div className="empty-state">
                <p>No warranty claims submitted yet.</p>
              </div>
            ) : (
              <div className="table-responsive">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Claim ID</th>
                      <th>Customer</th>
                      <th>Product</th>
                      <th>Submitted Date</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentClaims.slice(0, 5).map((claim) => (
                      <tr key={claim.claimId}>
                        <td className="font-mono">{claim.claimId ? claim.claimId.substring(0, 8) + '...' : 'N/A'}</td>
                        <td>{claim.userName || 'Customer'}</td>
                        <td><strong>{claim.productName || 'Product'}</strong></td>
                        <td>{formatDate(claim.createdAt)}</td>
                        <td>{getStatusBadge(claim.claimStatus || claim.status)}</td>
                        <td>
                          <Link to={`/admin/claims/${claim.claimId}`} className="btn btn-secondary btn-sm">
                            Process Claim
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
};
