import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import productService from '../services/productService';
import warrantyService from '../services/warrantyService';
import invoiceService from '../services/invoiceService';
import InvoiceUpload from '../components/InvoiceUpload';
import claimService from '../services/claimService';
import SubmitClaimModal from '../components/SubmitClaimModal';

/**
 * Product Specification & Warranty Monitoring Page.
 * Displays granular product attributes, real-time warranty lifecycle countdown,
 * purchase invoice document management, and warranty claims.
 * Phase 10 — Warranty Claims
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

  // Invoice state
  const [invoice, setInvoice] = useState(null);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [invoiceSuccess, setInvoiceSuccess] = useState('');
  const [invoiceError, setInvoiceError] = useState('');

  // Invoice delete modal state
  const [showInvoiceDeleteModal, setShowInvoiceDeleteModal] = useState(false);
  const [deletingInvoice, setDeletingInvoice] = useState(false);
  const [invoiceDeleteError, setInvoiceDeleteError] = useState('');

  // Claims state (Phase 10)
  const [claims, setClaims] = useState([]);
  const [showClaimModal, setShowClaimModal] = useState(false);
  const [claimSuccess, setClaimSuccess] = useState('');
  const [claimError, setClaimError] = useState('');

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

        // Fetch invoice metadata via GET /api/products/{productId}/invoice
        try {
          const invoiceData = await invoiceService.getProductInvoice(id);
          setInvoice(invoiceData);
        } catch (invErr) {
          setInvoice(null);
        }

        // Fetch claims for this product via GET /api/products/{productId}/claims
        try {
          const claimsData = await claimService.getProductClaims(id);
          setClaims(claimsData || []);
        } catch (cErr) {
          setClaims([]);
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

  const handleClaimSuccess = (newClaim) => {
    setClaims((prev) => [newClaim, ...prev]);
    setClaimSuccess('Warranty claim submitted successfully (Status: PENDING).');
    setShowClaimModal(false);
    setTimeout(() => setClaimSuccess(''), 5000);
  };

  const formatFileSize = (bytes) => {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const formatDate = (isoString) => {
    if (!isoString) return '—';
    try {
      return new Date(isoString).toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    } catch {
      return isoString;
    }
  };

  const handleDownloadInvoice = async () => {
    if (!invoice) return;
    try {
      setInvoiceError('');
      await invoiceService.downloadInvoice(invoice.id, invoice.fileName);
    } catch {
      setInvoiceError('Failed to download invoice file.');
    }
  };

  const handleViewInvoice = async () => {
    if (!invoice) return;
    try {
      setInvoiceError('');
      await invoiceService.viewInvoice(invoice.id);
    } catch {
      setInvoiceError('Failed to preview invoice file. Please try downloading.');
    }
  };

  const handleDeleteInvoice = async () => {
    if (!invoice) return;
    try {
      setDeletingInvoice(true);
      setInvoiceDeleteError('');
      await invoiceService.deleteInvoice(invoice.id);
      setInvoice(null);
      setShowInvoiceDeleteModal(false);
      setInvoiceSuccess('Purchase invoice successfully deleted.');
      setTimeout(() => setInvoiceSuccess(''), 4000);
    } catch (err) {
      setInvoiceDeleteError(err.response?.data?.message || 'Failed to delete invoice.');
    } finally {
      setDeletingInvoice(false);
    }
  };

  const handleUploadSuccess = (uploadedInvoice) => {
    setInvoice(uploadedInvoice);
    setInvoiceSuccess('Purchase invoice uploaded successfully.');
    setTimeout(() => setInvoiceSuccess(''), 4000);
  };

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

          {/* Purchase Invoice Panel (Phase 9 — Invoice Management) */}
          <div
            style={{
              marginTop: '1.5rem',
              padding: '1.25rem',
              borderRadius: 'var(--radius-lg)',
              backgroundColor: 'var(--surface)',
              border: '1px solid var(--border)',
              boxShadow: 'var(--shadow-card)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.875rem' }}>
              <h3 style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)', margin: 0 }}>
                Purchase Invoice
              </h3>
              {invoice && (
                <span
                  style={{
                    backgroundColor: 'var(--primary-light)',
                    color: 'var(--primary)',
                    fontSize: 'var(--font-size-xs)',
                    fontWeight: '700',
                    padding: '0.15rem 0.5rem',
                    borderRadius: 'var(--radius-sm)',
                    textTransform: 'uppercase'
                  }}
                >
                  {invoice.fileType?.includes('/') ? invoice.fileType.split('/')[1] : invoice.fileType}
                </span>
              )}
            </div>

            {invoiceSuccess && (
              <div
                style={{
                  padding: '0.625rem 0.75rem',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'var(--success-bg)',
                  color: 'var(--success-text)',
                  fontSize: 'var(--font-size-xs)',
                  marginBottom: '0.75rem'
                }}
              >
                {invoiceSuccess}
              </div>
            )}

            {invoiceError && (
              <div
                style={{
                  padding: '0.625rem 0.75rem',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'var(--danger-bg)',
                  color: 'var(--danger-text)',
                  fontSize: 'var(--font-size-xs)',
                  marginBottom: '0.75rem'
                }}
              >
                {invoiceError}
              </div>
            )}

            {invoice ? (
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.75rem' }}>
                  <div
                    style={{
                      width: '36px',
                      height: '36px',
                      borderRadius: 'var(--radius-sm)',
                      backgroundColor: 'var(--primary-light)',
                      color: 'var(--primary)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      flexShrink: 0
                    }}
                  >
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                      <polyline points="14 2 14 8 20 8" />
                    </svg>
                  </div>
                  <div style={{ overflow: 'hidden' }}>
                    <div style={{ fontWeight: '600', fontSize: 'var(--font-size-sm)', color: 'var(--text-primary)', wordBreak: 'break-all' }}>
                      {invoice.fileName}
                    </div>
                    <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                      {formatFileSize(invoice.fileSize)} • Uploaded {formatDate(invoice.uploadedAt)}
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginTop: '0.75rem' }}>
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={handleViewInvoice}
                    style={{ flex: 1 }}
                  >
                    View
                  </button>
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={handleDownloadInvoice}
                    style={{ flex: 1 }}
                  >
                    Download
                  </button>
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    onClick={() => setShowInvoiceDeleteModal(true)}
                    style={{ color: 'var(--danger)', padding: '0 0.5rem' }}
                    title="Delete invoice"
                  >
                    Delete
                  </button>
                </div>
              </div>
            ) : (
              <div>
                <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-secondary)', marginBottom: '0.875rem' }}>
                  No invoice uploaded yet.
                </p>
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  style={{ width: '100%' }}
                  onClick={() => setShowUploadModal(true)}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.35rem' }}>
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                    <polyline points="17 8 12 3 7 8" />
                    <line x1="12" y1="3" x2="12" y2="15" />
                  </svg>
                  Upload Invoice
                </button>
              </div>
            )}
          </div>

          {/* Warranty Claims Panel (Phase 10 — Warranty Claims) */}
          <div
            style={{
              marginTop: '1.5rem',
              padding: '1.25rem',
              borderRadius: 'var(--radius-lg)',
              backgroundColor: 'var(--surface)',
              border: '1px solid var(--border)',
              boxShadow: 'var(--shadow-card)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.875rem' }}>
              <h3 style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)', margin: 0 }}>
                Warranty Claims ({claims.length})
              </h3>
              {warranty && warranty.status !== 'EXPIRED' && (
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  style={{ padding: '0.25rem 0.6rem', fontSize: '0.75rem' }}
                  onClick={() => setShowClaimModal(true)}
                >
                  + File Claim
                </button>
              )}
            </div>

            {claimSuccess && (
              <div
                style={{
                  padding: '0.625rem 0.75rem',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'var(--success-bg)',
                  color: 'var(--success-text)',
                  fontSize: 'var(--font-size-xs)',
                  marginBottom: '0.75rem'
                }}
              >
                {claimSuccess}
              </div>
            )}

            {warranty?.status === 'EXPIRED' ? (
              <div style={{ padding: '0.75rem', borderRadius: 'var(--radius-sm)', backgroundColor: 'var(--danger-bg)', color: 'var(--danger)', fontSize: 'var(--font-size-xs)' }}>
                Warranty expired. New claims cannot be filed for this product.
              </div>
            ) : claims.length === 0 ? (
              <div>
                <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-secondary)', marginBottom: '0.875rem' }}>
                  No warranty claims filed for this product yet.
                </p>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ width: '100%' }}
                  onClick={() => setShowClaimModal(true)}
                >
                  File a Warranty Claim
                </button>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {claims.map((c) => (
                  <div
                    key={c.id}
                    style={{
                      padding: '0.75rem',
                      background: 'var(--background)',
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--border)'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
                      <span
                        className={`status-badge ${
                          c.status === 'PENDING'
                            ? 'badge-pending'
                            : c.status === 'APPROVED' || c.status === 'COMPLETED'
                            ? 'badge-active'
                            : c.status === 'IN_PROGRESS'
                            ? 'badge-expiring'
                            : 'badge-expired'
                        }`}
                        style={{ fontSize: '0.7rem', padding: '0.15rem 0.45rem' }}
                      >
                        {c.status}
                      </span>
                      <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                        {formatDate(c.createdAt)}
                      </span>
                    </div>
                    <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: '600', color: 'var(--text-primary)', marginBottom: '0.25rem' }}>
                      {c.claimReason}
                    </div>
                    {c.description && (
                      <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-secondary)', margin: '0 0 0.5rem 0', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {c.description}
                      </p>
                    )}
                    <Link to={`/claims/${c.id}`} className="btn btn-ghost btn-sm" style={{ padding: '0.2rem 0', fontSize: '0.75rem' }}>
                      View Claim Details →
                    </Link>
                  </div>
                ))}
              </div>
            )}
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

      {/* Invoice Upload Modal */}
      {showUploadModal && (
        <InvoiceUpload
          productId={product.id}
          productName={product.productName}
          onSuccess={handleUploadSuccess}
          onClose={() => setShowUploadModal(false)}
        />
      )}

      {/* Invoice Delete Confirmation Modal */}
      {showInvoiceDeleteModal && invoice && (
        <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="delete-invoice-modal-title">
          <div className="modal-dialog">
            <h3 id="delete-invoice-modal-title" className="modal-title">Delete Purchase Invoice?</h3>
            <p className="modal-desc">
              Are you sure you want to delete <strong>{invoice.fileName}</strong>?
              This document will be permanently removed from secure storage.
            </p>

            {invoiceDeleteError && (
              <div className="error-alert" style={{ marginBottom: '1rem' }} role="alert">
                {invoiceDeleteError}
              </div>
            )}

            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => {
                  setShowInvoiceDeleteModal(false);
                  setInvoiceDeleteError('');
                }}
                disabled={deletingInvoice}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                style={{ backgroundColor: 'var(--danger)', borderColor: 'var(--danger)' }}
                onClick={handleDeleteInvoice}
                disabled={deletingInvoice}
              >
                {deletingInvoice ? 'Deleting...' : 'Confirm Delete'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Submit Claim Modal (Phase 10 — Warranty Claims) */}
      {showClaimModal && (
        <SubmitClaimModal
          productId={product.id}
          productName={product.productName}
          onSuccess={handleClaimSuccess}
          onClose={() => setShowClaimModal(false)}
        />
      )}
    </div>
  );
}
