import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import productService from '../services/productService';
import warrantyService from '../services/warrantyService';

/**
 * Product Specification & Warranty Monitoring Page.
 * Displays granular product attributes and real-time warranty lifecycle countdown.
 * Phase 8 — Warranty Management
 */
export default function ProductDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [warranty, setWarranty] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [copiedSerial, setCopiedSerial] = useState(false);

  // Delete modal state
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  useEffect(() => {
    const fetchProductAndWarranty = async () => {
      try {
        setLoading(true);
        setError('');
        const productData = await productService.getProductById(id);
        setProduct(productData);

        // Fetch authoritative warranty details via GET /api/products/{productId}/warranty
        try {
          const warrantyData = await warrantyService.getProductWarranty(id);
          setWarranty(warrantyData);
        } catch (wErr) {
          // Fall back to nested warranty on product if endpoint returns error
          setWarranty(productData.warranty || null);
        }
      } catch (err) {
        if (err.response?.status === 404) {
          setError('Product not found or you do not have permission to view it.');
        } else {
          setError(err.response?.data?.message || 'Failed to load product details.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchProductAndWarranty();
  }, [id]);

  const handleCopySerial = () => {
    if (!product?.serialNumber) return;
    navigator.clipboard.writeText(product.serialNumber);
    setCopiedSerial(true);
    setTimeout(() => setCopiedSerial(false), 2000);
  };

  const handleDelete = async () => {
    try {
      setDeleting(true);
      setDeleteError('');
      await productService.deleteProduct(id);
      navigate('/products');
    } catch (err) {
      setDeleteError(err.response?.data?.message || 'Failed to delete product.');
    } finally {
      setDeleting(false);
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '4rem 1.5rem', flex: 1 }}>
        <div className="loading-container">
          <div className="spinner spinner-lg"></div>
          <span>Loading product specifications and warranty terms...</span>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="container" style={{ padding: '4rem 1.5rem', flex: 1, maxWidth: '640px' }}>
        <div className="empty-state">
          <div className="empty-state-icon" style={{ backgroundColor: 'var(--danger-bg)', color: 'var(--danger)' }}>
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
          </div>
          <h2 className="empty-state-title">Access Error</h2>
          <p className="empty-state-desc">{error || 'Unable to retrieve this product.'}</p>
          <Link to="/products" className="btn btn-secondary btn-md">
            Return to Products
          </Link>
        </div>
      </div>
    );
  }

  const progress = warranty?.progressPercentage !== undefined ? warranty.progressPercentage : 0;

  return (
    <div className="container" style={{ padding: '3rem 1.5rem', flex: 1, maxWidth: '1080px' }}>
      {/* Navigation and Actions */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <Link to="/products" className="btn btn-ghost btn-sm" style={{ paddingLeft: 0 }}>
          ← Back to Registered Products
        </Link>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to={`/products/${product.id}/edit`} className="btn btn-secondary btn-sm">
            Edit Details
          </Link>
          <button
            type="button"
            className="btn btn-ghost btn-sm"
            style={{ color: 'var(--danger)' }}
            onClick={() => setShowDeleteModal(true)}
          >
            Delete Product
          </button>
        </div>
      </div>

      {/* Header Info */}
      <div style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--primary)', letterSpacing: '0.05em' }}>
            {product.brand} • {product.category}
          </span>
          {warranty && (
            <span
              className={`status-badge ${
                warranty.status === 'ACTIVE'
                  ? 'badge-active'
                  : warranty.status === 'EXPIRING_SOON'
                  ? 'badge-expiring'
                  : 'badge-expired'
              }`}
            >
              <span className="status-dot"></span> {warranty.status?.replace('_', ' ')}
            </span>
          )}
        </div>
        <h1 className="page-title">{product.productName}</h1>
      </div>

      {/* 2-Column Detail Grid */}
      <div className="detail-grid">
        {/* Left Column: Product Specifications */}
        <div className="detail-card">
          <h2 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '1.25rem' }}>
            Product Specifications
          </h2>

          <div className="spec-list">
            <div className="spec-item">
              <span className="meta-label">Model Number</span>
              <span className="meta-value">{product.modelNumber}</span>
            </div>

            <div className="spec-item">
              <span className="meta-label">Serial Number</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span className="serial-tag">{product.serialNumber}</span>
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  style={{ padding: '0.2rem 0.5rem', fontSize: '0.7rem' }}
                  onClick={handleCopySerial}
                  title="Copy serial number"
                >
                  {copiedSerial ? '✓ Copied' : 'Copy'}
                </button>
              </div>
            </div>

            <div className="spec-item">
              <span className="meta-label">Retailer / Seller</span>
              <span className="meta-value">{product.sellerName}</span>
            </div>

            <div className="spec-item">
              <span className="meta-label">Purchase Date</span>
              <span className="meta-value">{product.purchaseDate}</span>
            </div>

            <div className="spec-item">
              <span className="meta-label">Purchase Price</span>
              <span className="meta-value" style={{ fontWeight: '700' }}>
                ${Number(product.price).toFixed(2)} USD
              </span>
            </div>

            <div className="spec-item">
              <span className="meta-label">Warranty Term</span>
              <span className="meta-value">{product.warrantyDurationMonths} Months</span>
            </div>

            <div className="spec-item">
              <span className="meta-label">Registration Date</span>
              <span className="meta-value" style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                {product.createdAt ? new Date(product.createdAt).toLocaleDateString() : 'N/A'}
              </span>
            </div>

            {product.description && (
              <div style={{ marginTop: '0.75rem', paddingTop: '1rem', borderTop: '1px solid var(--border)' }}>
                <span className="meta-label" style={{ display: 'block', marginBottom: '0.35rem' }}>Notes / Description</span>
                <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
                  {product.description}
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Warranty Protection Panel */}
        <div>
          <div className="warranty-highlight-card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="var(--primary-border)" strokeWidth="2.5">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                <path d="m9 12 2 2 4-4" />
              </svg>
              <h2 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '700', color: '#ffffff' }}>
                Warranty Protection
              </h2>
            </div>

            {warranty ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                <div>
                  <div style={{ fontSize: 'var(--font-size-xs)', color: '#94a3b8', marginBottom: '0.25rem' }}>
                    Coverage Status
                  </div>
                  <div style={{ fontSize: 'var(--font-size-2xl)', fontWeight: '800', color: '#ffffff' }}>
                    {warranty.status === 'ACTIVE'
                      ? 'Fully Protected'
                      : warranty.status === 'EXPIRING_SOON'
                      ? 'Expiring Soon'
                      : 'Expired'}
                  </div>
                  {warranty.status !== 'EXPIRED' ? (
                    <div style={{ fontSize: 'var(--font-size-sm)', color: '#6ee7b7', fontWeight: '600', marginTop: '0.25rem' }}>
                      {warranty.daysRemaining} days of protection remaining
                    </div>
                  ) : (
                    <div style={{ fontSize: 'var(--font-size-sm)', color: '#fca5a5', fontWeight: '600', marginTop: '0.25rem' }}>
                      Coverage expired on {warranty.expiryDate}
                    </div>
                  )}
                </div>

                {/* Accessible Progress Bar */}
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 'var(--font-size-xs)', color: '#94a3b8', marginBottom: '0.35rem' }}>
                    <span>{warranty.startDate}</span>
                    <span style={{ fontWeight: '700', color: '#ffffff' }}>{progress}% elapsed</span>
                    <span>{warranty.expiryDate}</span>
                  </div>
                  <div
                    role="progressbar"
                    aria-valuenow={progress}
                    aria-valuemin="0"
                    aria-valuemax="100"
                    aria-label={`Warranty progress: ${progress}%`}
                    style={{ height: '8px', width: '100%', background: '#334155', borderRadius: '4px', overflow: 'hidden' }}
                  >
                    <div
                      style={{
                        height: '100%',
                        width: `${progress}%`,
                        background:
                          warranty.status === 'ACTIVE'
                            ? 'var(--primary)'
                            : warranty.status === 'EXPIRING_SOON'
                            ? 'var(--warning)'
                            : 'var(--danger)',
                        borderRadius: '4px',
                        transition: 'width 0.4s ease'
                      }}
                    />
                  </div>
                  <div style={{ textAlign: 'right', fontSize: '0.7rem', color: '#94a3b8', marginTop: '0.25rem' }}>
                    {progress}% period elapsed
                  </div>
                </div>

                <div style={{ borderTop: '1px solid #334155', paddingTop: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 'var(--font-size-xs)', color: '#cbd5e1' }}>
                  <span>Dedicated Warranty View:</span>
                  <Link to={`/warranties/${warranty.id}`} className="btn btn-secondary btn-sm" style={{ padding: '0.25rem 0.6rem', fontSize: '0.75rem' }}>
                    View Warranty Details →
                  </Link>
                </div>
              </div>
            ) : (
              <p style={{ fontSize: 'var(--font-size-xs)', color: '#94a3b8' }}>
                No warranty record attached to this product.
              </p>
            )}
          </div>

          {/* Subsequent phase teaser */}
          <div
            style={{
              marginTop: '1.5rem',
              padding: '1.25rem',
              borderRadius: 'var(--radius-lg)',
              backgroundColor: 'var(--surface)',
              border: '1px solid var(--border)'
            }}
          >
            <h3 style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
              Warranty Invoices & Claims
            </h3>
            <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
              Proof of purchase PDF/receipt uploads and claim filing will be integrated in subsequent phases.
            </p>
          </div>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal-dialog">
            <h3 className="modal-title">Delete Product Registration?</h3>
            <p className="modal-desc">
              Are you sure you want to delete <strong>{product.productName}</strong>?
              This will permanently delete this equipment registration and its associated warranty record.
            </p>

            {deleteError && (
              <div className="error-alert" style={{ marginBottom: '1rem' }}>
                {deleteError}
              </div>
            )}

            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => {
                  setShowDeleteModal(false);
                  setDeleteError('');
                }}
                disabled={deleting}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                style={{ backgroundColor: 'var(--danger)', borderColor: 'var(--danger)' }}
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? 'Deleting...' : 'Confirm Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
