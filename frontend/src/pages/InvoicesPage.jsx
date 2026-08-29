import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import invoiceService from '../services/invoiceService';

/**
 * Invoices Page (/invoices).
 * Centralized list view of all uploaded purchase invoices across customer products.
 * Supports viewing, downloading, and deleting proof-of-purchase documents.
 * Phase 9 — Invoice Management
 */
export default function InvoicesPage() {
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Delete modal state
  const [invoiceToDelete, setInvoiceToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  const fetchInvoices = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await invoiceService.getInvoices();
      setInvoices(data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load invoices. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInvoices();
  }, []);

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

  const handleDownload = async (invoice) => {
    try {
      await invoiceService.downloadInvoice(invoice.id, invoice.fileName);
    } catch (err) {
      setError('Failed to download invoice file. Please try again.');
    }
  };

  const handleView = async (invoice) => {
    try {
      await invoiceService.viewInvoice(invoice.id);
    } catch (err) {
      setError('Failed to preview invoice file. Please try downloading instead.');
    }
  };

  const confirmDelete = async () => {
    if (!invoiceToDelete) return;

    try {
      setDeleting(true);
      setDeleteError('');
      await invoiceService.deleteInvoice(invoiceToDelete.id);
      setSuccessMessage(`Invoice "${invoiceToDelete.fileName}" was successfully deleted.`);
      setInvoiceToDelete(null);
      await fetchInvoices();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setDeleteError(err.response?.data?.message || 'Failed to delete invoice. Please try again.');
    } finally {
      setDeleting(false);
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '4rem 1.5rem', flex: 1 }}>
        <div className="loading-container">
          <div className="spinner spinner-lg"></div>
          <span>Loading your purchase invoices...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '2.5rem 1.5rem 4rem', flex: 1 }}>
      {/* Page Header */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '2rem'
        }}
      >
        <div>
          <h1 style={{ fontSize: 'var(--font-size-3xl)', fontWeight: '800', color: 'var(--text-primary)', margin: 0 }}>
            My Invoices
          </h1>
          <p style={{ color: 'var(--text-secondary)', marginTop: '0.25rem', fontSize: 'var(--font-size-base)' }}>
            Manage and view all stored proof-of-purchase receipts and documents.
          </p>
        </div>

        <Link to="/products" className="btn btn-secondary btn-md">
          Browse Products
        </Link>
      </div>

      {/* Alerts */}
      {successMessage && (
        <div
          style={{
            padding: '1rem 1.25rem',
            borderRadius: 'var(--radius-md)',
            backgroundColor: 'var(--success-bg)',
            color: 'var(--success-text)',
            marginBottom: '1.5rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.75rem',
            border: '1px solid #a7f3d0'
          }}
          role="status"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <path d="M20 6L9 17l-5-5" />
          </svg>
          <span style={{ fontWeight: '500' }}>{successMessage}</span>
        </div>
      )}

      {error && (
        <div className="error-alert" style={{ marginBottom: '1.5rem' }} role="alert">
          {error}
        </div>
      )}

      {/* Invoice List or Empty State */}
      {invoices.length === 0 ? (
        <div className="empty-state" style={{ maxWidth: '540px', margin: '3rem auto' }}>
          <div
            className="empty-state-icon"
            style={{ backgroundColor: 'var(--primary-light)', color: 'var(--primary)' }}
          >
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="16" y1="13" x2="8" y2="13" />
              <line x1="16" y1="17" x2="8" y2="17" />
              <polyline points="10 9 9 9 8 9" />
            </svg>
          </div>
          <h2 className="empty-state-title">No invoices uploaded yet</h2>
          <p className="empty-state-desc">
            Attach store receipts and invoices to your registered products to ensure fast, hassle-free warranty claims.
          </p>
          <Link to="/products" className="btn btn-primary btn-md">
            View Registered Products
          </Link>
        </div>
      ) : (
        <div
          style={{
            backgroundColor: 'var(--surface)',
            borderRadius: 'var(--radius-lg)',
            border: '1px solid var(--border)',
            boxShadow: 'var(--shadow-sm)',
            overflow: 'hidden'
          }}
        >
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ backgroundColor: 'var(--surface-hover)', borderBottom: '1px solid var(--border)' }}>
                  <th style={{ padding: '0.875rem 1.25rem', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                    File Name
                  </th>
                  <th style={{ padding: '0.875rem 1.25rem', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                    Associated Product
                  </th>
                  <th style={{ padding: '0.875rem 1.25rem', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                    File Format
                  </th>
                  <th style={{ padding: '0.875rem 1.25rem', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                    File Size
                  </th>
                  <th style={{ padding: '0.875rem 1.25rem', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                    Uploaded On
                  </th>
                  <th style={{ padding: '0.875rem 1.25rem', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)', textAlign: 'right' }}>
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {invoices.map((inv) => (
                  <tr
                    key={inv.id}
                    style={{
                      borderBottom: '1px solid var(--border)',
                      transition: 'background-color var(--transition-fast)'
                    }}
                  >
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <div
                          style={{
                            width: '32px',
                            height: '32px',
                            borderRadius: 'var(--radius-sm)',
                            backgroundColor: 'var(--primary-light)',
                            color: 'var(--primary)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            flexShrink: 0
                          }}
                        >
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                            <polyline points="14 2 14 8 20 8" />
                          </svg>
                        </div>
                        <span style={{ fontWeight: '600', color: 'var(--text-primary)', wordBreak: 'break-all' }}>
                          {inv.fileName}
                        </span>
                      </div>
                    </td>

                    <td style={{ padding: '1rem 1.25rem' }}>
                      {inv.productId ? (
                        <Link
                          to={`/products/${inv.productId}`}
                          style={{
                            color: 'var(--primary)',
                            fontWeight: '500',
                            textDecoration: 'none'
                          }}
                        >
                          {inv.productName || 'View Product'}
                        </Link>
                      ) : (
                        <span style={{ color: 'var(--text-muted)' }}>—</span>
                      )}
                    </td>

                    <td style={{ padding: '1rem 1.25rem' }}>
                      <span
                        style={{
                          backgroundColor: 'var(--surface-hover)',
                          border: '1px solid var(--border)',
                          color: 'var(--text-secondary)',
                          fontSize: 'var(--font-size-xs)',
                          fontWeight: '700',
                          padding: '0.2rem 0.5rem',
                          borderRadius: 'var(--radius-sm)',
                          textTransform: 'uppercase'
                        }}
                      >
                        {inv.fileType?.includes('/') ? inv.fileType.split('/')[1] : inv.fileType}
                      </span>
                    </td>

                    <td style={{ padding: '1rem 1.25rem', color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)' }}>
                      {formatFileSize(inv.fileSize)}
                    </td>

                    <td style={{ padding: '1rem 1.25rem', color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)' }}>
                      {formatDate(inv.uploadedAt)}
                    </td>

                    <td style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>
                      <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', alignItems: 'center' }}>
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          onClick={() => handleView(inv)}
                          title="Preview in browser"
                        >
                          View
                        </button>
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          onClick={() => handleDownload(inv)}
                          title="Download file"
                        >
                          Download
                        </button>
                        <button
                          type="button"
                          className="btn btn-ghost btn-sm"
                          style={{ color: 'var(--danger)' }}
                          onClick={() => setInvoiceToDelete(inv)}
                          title="Delete invoice"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {invoiceToDelete && (
        <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="delete-invoice-title">
          <div className="modal-dialog">
            <h3 id="delete-invoice-title" className="modal-title">Delete Invoice?</h3>
            <p className="modal-desc">
              Are you sure you want to delete <strong>{invoiceToDelete.fileName}</strong>?
              This will permanently remove the document from secure storage.
            </p>

            {deleteError && (
              <div className="error-alert" style={{ marginBottom: '1rem' }} role="alert">
                {deleteError}
              </div>
            )}

            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => {
                  setInvoiceToDelete(null);
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
                onClick={confirmDelete}
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
