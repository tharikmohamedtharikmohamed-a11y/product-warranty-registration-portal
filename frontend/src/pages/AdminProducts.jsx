import React, { useState, useEffect } from 'react';
import { adminService } from '../services/adminService';

export const AdminProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.getAllProducts();
      setProducts(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load system products.');
    } finally {
      setLoading(false);
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
          <h1 className="page-title">System Products</h1>
          <p className="page-subtitle">All products registered by customers across the portal</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading system products...</p>
        </div>
      ) : products.length === 0 ? (
        <div className="empty-state">
          <p>No products found in system.</p>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Product Name</th>
                <th>Owner / Customer</th>
                <th>Category</th>
                <th>Brand</th>
                <th>Serial Number</th>
                <th>Purchase Date</th>
                <th>Price</th>
              </tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.productId}>
                  <td><strong>{p.productName}</strong></td>
                  <td>{p.userName || 'N/A'}</td>
                  <td>{p.category || 'N/A'}</td>
                  <td>{p.brand || 'N/A'}</td>
                  <td><code className="font-mono">{p.serialNumber || 'N/A'}</code></td>
                  <td>{formatDate(p.purchaseDate)}</td>
                  <td>{p.price !== null && p.price !== undefined ? `$${p.price.toFixed(2)}` : 'N/A'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
