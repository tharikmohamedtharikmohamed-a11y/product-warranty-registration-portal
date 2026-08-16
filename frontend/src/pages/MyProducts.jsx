import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '../services/productService';

export const MyProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        setError('');
        const data = await productService.getUserProducts();
        setProducts(data || []);
      } catch (err) {
        setError(err.message || 'Unable to load products. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
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

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">My Registered Products</h1>
        <p className="page-subtitle">View all products registered under your account.</p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <div className="loading-spinner" style={{ borderColor: 'var(--primary)', borderTopColor: 'transparent', width: '32px', height: '32px' }}></div>
          <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading products...</p>
        </div>
      ) : products.length === 0 ? (
        <div className="empty-state">
          <span className="empty-icon">📦</span>
          <h3 className="empty-title">No products registered yet</h3>
          <p className="empty-desc">You do not have any products registered in your account.</p>
        </div>
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Product Name</th>
                <th>Brand</th>
                <th>Model</th>
                <th>Serial Number</th>
                <th>Purchase Date</th>
                <th>Warranty Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id}>
                  <td><strong>{product.productName}</strong></td>
                  <td>{product.brand}</td>
                  <td>{product.modelNumber || 'N/A'}</td>
                  <td><code>{product.serialNumber || 'N/A'}</code></td>
                  <td>{product.purchaseDate ? new Date(product.purchaseDate).toLocaleDateString() : 'N/A'}</td>
                  <td>{renderStatusBadge(product.warranty?.status)}</td>
                  <td>
                    <Link to={`/products/${product.id}`} className="btn btn-secondary btn-sm">
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
