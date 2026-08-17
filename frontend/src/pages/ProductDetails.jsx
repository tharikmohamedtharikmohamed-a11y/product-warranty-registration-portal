import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link, useLocation } from 'react-router-dom';
import { productService } from '../services/productService';

export const ProductDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState(location.state?.successMessage || '');

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

  const handleDelete = async () => {
    try {
      setDeleting(true);
      setError('');
      await productService.deleteProduct(id);
      navigate('/products', {
        state: { successMessage: 'Product deleted successfully.' },
      });
    } catch (err) {
      setError(err.message || 'Unable to delete product. Please try again.');
      setShowDeleteModal(false);
    } finally {
      setDeleting(false);
    }
  };

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
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 className="page-title">Product Details</h1>
          <p className="page-subtitle">Complete specs and warranty status for this product.</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <Link to="/products" className="btn btn-secondary btn-sm">
            ← Back to Products
          </Link>
          {product && (
            <>
              <Link to={`/products/${id}/invoices`} className="btn btn-primary btn-sm">
                📄 Manage Invoices
              </Link>
              <Link to={`/products/${id}/edit`} className="btn btn-secondary btn-sm">
                ✏️ Edit Product
              </Link>
              <button
                onClick={() => setShowDeleteModal(true)}
                className="btn btn-danger btn-sm"
                disabled={deleting}
              >
                🗑️ Delete Product
              </button>
            </>
          )}
        </div>
      </div>

      {successMessage && <div className="alert alert-success">{successMessage}</div>}
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
                <span className="details-value">{product.price !== null && product.price !== undefined ? `$${product.price.toFixed(2)}` : 'N/A'}</span>
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
                    {product.warranty.warrantyDurationMonths || product.warranty.warrantyPeriodMonths
                      ? `${product.warranty.warrantyDurationMonths || product.warranty.warrantyPeriodMonths} Months`
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

          {/* Invoice Management Shortcut Card */}
          <div className="details-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Purchase Invoice & Receipts</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '0.2rem' }}>
                Upload or view stored proof-of-purchase files for warranty verification.
              </p>
            </div>
            <Link to={`/products/${id}/invoices`} className="btn btn-primary btn-sm">
              📄 Manage Invoices
            </Link>
          </div>
        </>
      ) : null}

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000,
          padding: '1rem'
        }}>
          <div style={{
            background: 'white',
            borderRadius: 'var(--radius)',
            padding: '2rem',
            maxWidth: '450px',
            width: '100%',
            boxShadow: 'var(--shadow-lg)'
          }}>
            <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.75rem', color: 'var(--error)' }}>
              Confirm Product Deletion
            </h3>
            <p style={{ color: 'var(--text-dark)', marginBottom: '1.5rem', fontSize: '0.95rem' }}>
              Are you sure you want to delete this product? This action cannot be undone.
            </p>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setShowDeleteModal(false)}
                disabled={deleting}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-danger"
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? (
                  <>
                    <span className="loading-spinner" /> Deleting product...
                  </>
                ) : (
                  'Yes, Delete Product'
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
