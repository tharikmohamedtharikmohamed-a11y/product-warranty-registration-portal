import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { productService } from '../services/productService';
import { warrantyService } from '../services/warrantyService';

export const CustomerDashboard = () => {
  const { user } = useAuth();
  const [products, setProducts] = useState([]);
  const [warranties, setWarranties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        setError('');
        const [productsData, warrantiesData] = await Promise.all([
          productService.getUserProducts(),
          warrantyService.getUserWarranties(),
        ]);
        setProducts(productsData || []);
        setWarranties(warrantiesData || []);
      } catch (err) {
        setError(err.message || 'Unable to load dashboard data.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  // Compute statistics dynamically from real backend data
  const totalProducts = products.length;
  const activeWarranties = warranties.filter((w) => w.status === 'ACTIVE').length;
  const expiringSoonWarranties = warranties.filter((w) => w.status === 'EXPIRING_SOON').length;
  const expiredWarranties = warranties.filter((w) => w.status === 'EXPIRED').length;

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

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Welcome, {user?.name || 'Customer'}</h1>
        <p className="page-subtitle">Manage your products and track your warranty coverage.</p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <div className="loading-spinner" style={{ borderColor: 'var(--primary)', borderTopColor: 'transparent', width: '32px', height: '32px' }}></div>
          <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading your dashboard...</p>
        </div>
      ) : (
        <>
          {/* Summary Statistics Cards */}
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon-wrapper icon-blue">📦</div>
              <div>
                <div className="stat-number">{totalProducts}</div>
                <div className="stat-label">Total Products</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon-wrapper icon-green">🛡️</div>
              <div>
                <div className="stat-number">{activeWarranties}</div>
                <div className="stat-label">Active Warranties</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon-wrapper icon-amber">⏳</div>
              <div>
                <div className="stat-number">{expiringSoonWarranties}</div>
                <div className="stat-label">Expiring Soon</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon-wrapper icon-red">⚠️</div>
              <div>
                <div className="stat-number">{expiredWarranties}</div>
                <div className="stat-label">Expired Warranties</div>
              </div>
            </div>
          </div>

          {/* Recent Products Section */}
          <div className="dashboard-section">
            <div className="section-header">
              <h2 className="section-title">My Registered Products</h2>
              <Link to="/products" className="btn btn-secondary btn-sm">
                View All ({products.length})
              </Link>
            </div>

            {products.length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon">📦</span>
                <h3 className="empty-title">No products registered yet</h3>
                <p className="empty-desc">Your registered products will appear here once added to your account.</p>
              </div>
            ) : (
              <div className="table-container" style={{ margin: 0 }}>
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Product Name</th>
                      <th>Brand</th>
                      <th>Model Number</th>
                      <th>Serial Number</th>
                      <th>Purchase Date</th>
                      <th>Warranty Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {products.slice(0, 5).map((p) => (
                      <tr key={p.id}>
                        <td><strong>{p.productName}</strong></td>
                        <td>{p.brand}</td>
                        <td>{p.modelNumber || 'N/A'}</td>
                        <td><code>{p.serialNumber || 'N/A'}</code></td>
                        <td>{p.purchaseDate ? new Date(p.purchaseDate).toLocaleDateString() : 'N/A'}</td>
                        <td>{renderStatusBadge(p.warranty?.status)}</td>
                        <td>
                          <Link to={`/products/${p.id}`} className="btn btn-secondary btn-sm">
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

          {/* Recent Warranties Section */}
          <div className="dashboard-section">
            <div className="section-header">
              <h2 className="section-title">My Warranties</h2>
              <Link to="/warranties" className="btn btn-secondary btn-sm">
                View All ({warranties.length})
              </Link>
            </div>

            {warranties.length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon">🛡️</span>
                <h3 className="empty-title">No warranties found</h3>
                <p className="empty-desc">Active and expired warranties associated with your products will appear here.</p>
              </div>
            ) : (
              <div className="table-container" style={{ margin: 0 }}>
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Product Name</th>
                      <th>Brand</th>
                      <th>Start Date</th>
                      <th>End Date</th>
                      <th>Duration</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {warranties.slice(0, 5).map((w) => (
                      <tr key={w.warrantyId}>
                        <td><strong>{w.productName || 'Product'}</strong></td>
                        <td>{w.brand || 'N/A'}</td>
                        <td>{w.warrantyStartDate ? new Date(w.warrantyStartDate).toLocaleDateString() : 'N/A'}</td>
                        <td>{w.warrantyEndDate ? new Date(w.warrantyEndDate).toLocaleDateString() : 'N/A'}</td>
                        <td>{w.warrantyDurationMonths ? `${w.warrantyDurationMonths} Months` : 'N/A'}</td>
                        <td>{renderStatusBadge(w.status)}</td>
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
        </>
      )}
    </div>
  );
};
