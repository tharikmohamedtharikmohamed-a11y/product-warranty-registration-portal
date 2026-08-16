import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { productService } from '../services/productService';

export const ProductDetails = () => {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        setLoading(true);
        setError('');
        const data = await productService.getProductById(id);
        setProduct(data);
      } catch (err) {
        setError(err.message || 'Product not found or access denied.');
      } finally {
        setLoading(false);
      }
    };

    fetchProduct();
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
    if (!endDateStr) return null;
    const endDate = new Date(endDateStr);
    const today = new Date();
    const diffTime = endDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays > 0 ? diffDays : 0;
  };

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title">Product Details</h1>
          <p className="page-subtitle">Complete specs and warranty status for this product.</p>
        </div>
        <Link to="/products" className="btn btn-secondary btn-sm">
          ← Back to Products
        </Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <div className="loading-spinner" style={{ borderColor: 'var(--primary)', borderTopColor: 'transparent', width: '32px', height: '32px' }}></div>
          <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading product details...</p>
        </div>
      ) : product ? (
        <>
          <div className="details-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
              <div>
                <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>{product.productName}</h2>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
                  {product.brand} {product.modelNumber ? `• Model: ${product.modelNumber}` : ''}
                </p>
              </div>
              <div>{renderStatusBadge(product.warranty?.status)}</div>
            </div>

            <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '1.5rem 0' }} />

            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>Product Specifications</h3>
            <div className="details-grid">
              <div className="details-item">
                <span className="details-label">Category</span>
                <span className="details-value">{product.category || 'N/A'}</span>
              </div>
              <div className="details-item">
                <span className="details-label">Brand</span>
                <span className="details-value">{product.brand || 'N/A'}</span>
              </div>
              <div className="details-item">
                <span className="details-label">Model Number</span>
                <span className="details-value">{product.modelNumber || 'N/A'}</span>
              </div>
              <div className="details-item">
                <span className="details-label">Serial Number</span>
                <span className="details-value"><code>{product.serialNumber || 'N/A'}</code></span>
              </div>
              <div className="details-item">
                <span className="details-label">Purchase Date</span>
                <span className="details-value">
                  {product.purchaseDate ? new Date(product.purchaseDate).toLocaleDateString() : 'N/A'}
                </span>
              </div>
              <div className="details-item">
                <span className="details-label">Seller / Retailer</span>
                <span className="details-value">{product.sellerName || 'N/A'}</span>
              </div>
              <div className="details-item">
                <span className="details-label">Price Paid</span>
                <span className="details-value">{product.price ? `$${product.price.toFixed(2)}` : 'N/A'}</span>
              </div>
            </div>

            {product.description && (
              <div style={{ marginTop: '1.5rem' }}>
                <span className="details-label">Description</span>
                <p style={{ marginTop: '0.25rem', color: 'var(--text-dark)', fontSize: '0.95rem' }}>
                  {product.description}
                </p>
              </div>
            )}
          </div>

          {/* Attached Warranty Information Card */}
          {product.warranty && (
            <div className="details-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Warranty Information</h3>
                <Link to={`/warranties/${product.warranty.warrantyId}`} className="btn btn-secondary btn-sm">
                  View Full Warranty
                </Link>
              </div>

              <div className="details-grid" style={{ marginTop: '1rem' }}>
                <div className="details-item">
                  <span className="details-label">Warranty Start Date</span>
                  <span className="details-value">
                    {product.warranty.warrantyStartDate
                      ? new Date(product.warranty.warrantyStartDate).toLocaleDateString()
                      : 'N/A'}
                  </span>
                </div>
                <div className="details-item">
                  <span className="details-label">Warranty End Date</span>
                  <span className="details-value">
                    {product.warranty.warrantyEndDate
                      ? new Date(product.warranty.warrantyEndDate).toLocaleDateString()
                      : 'N/A'}
                  </span>
                </div>
                <div className="details-item">
                  <span className="details-label">Duration</span>
                  <span className="details-value">
                    {product.warranty.warrantyDurationMonths
                      ? `${product.warranty.warrantyDurationMonths} Months`
                      : 'N/A'}
                  </span>
                </div>
                <div className="details-item">
                  <span className="details-label">Time Remaining</span>
                  <span className="details-value">
                    {product.warranty.status === 'EXPIRED'
                      ? 'Expired'
                      : `${calculateDaysRemaining(product.warranty.warrantyEndDate)} Days`}
                  </span>
                </div>
              </div>
            </div>
          )}
        </>
      ) : null}
    </div>
  );
};
