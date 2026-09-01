import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import adminService from '../../services/adminService';
import Loading from '../../components/Loading';
import ErrorMessage from '../../components/ErrorMessage';

/**
 * Admin Invoice Documents Vault Page.
 * Displays all uploaded purchase invoice metadata across the platform.
 * Supports direct administrative view and download streamed securely through the backend.
 * Phase 11 — Admin Management Module
 */
export default function AdminInvoicesPage() {
  const [invoices, setInvoices] = useState([]);
  const [filterQuery, setFilterQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [actionError, setActionError] = useState('');

  const fetchInvoices = async () => {
    try {
      setLoading(true);
      setActionError('');
      const data = await adminService.getInvoices();
      setInvoices(data || []);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to load invoice archive.');
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
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return isoString;
    }
  };

  const handleDownload = async (id, fileName) => {
    try {
      setActionError('');
      await adminService.downloadInvoice(id, fileName);
    } catch {
      setActionError('Failed to download invoice asset.');
    }
  };

  const handleView = async (id) => {
    try {
      setActionError('');
      await adminService.viewInvoice(id);
    } catch {
      setActionError('Failed to preview invoice document.');
    }
  };

  const filteredInvoices = invoices.filter((i) => {
    if (!filterQuery.trim()) return true;
    const q = filterQuery.toLowerCase();
    return (
      (i.fileName && i.fileName.toLowerCase().includes(q)) ||
      (i.productName && i.productName.toLowerCase().includes(q)) ||
      (i.customerName && i.customerName.toLowerCase().includes(q)) ||
      (i.customerEmail && i.customerEmail.toLowerCase().includes(q))
    );
  });

  return (
    <div className="container" style={{ padding: '3rem 1.5rem', flex: 1, maxWidth: '1180px' }}>
      {/* Navigation Breadcrumb */}
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to="/admin" className="btn btn-ghost btn-sm" style={{ paddingLeft: 0 }}>
          ← Back to Operations Dashboard
        </Link>
      </div>

      {/* Page Title & Counters */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '2rem' }}>
        <div>
          <h1 className="page-title" style={{ marginBottom: '0.25rem' }}>Global Invoice Documents Vault</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)', margin: 0 }}>
            Audit purchase proofs and receipts stored securely in private Supabase Storage without exposing backend credentials.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--primary-light)', color: 'var(--primary)' }}>
            {invoices.length} Stored Invoices
          </span>
        </div>
      </div>

      {actionError && (
        <div style={{ marginBottom: '1.5rem' }}>
          <ErrorMessage message={actionError} />
        </div>
      )}

      {/* Filter Input */}
      <div className="detail-card" style={{ marginBottom: '1.5rem', padding: '1rem 1.25rem' }}>
        <input
          type="text"
          className="form-input"
          placeholder="Search by invoice file name, product, customer name, or email..."
          value={filterQuery}
          onChange={(e) => setFilterQuery(e.target.value)}
        />
      </div>

      {/* Invoices Table */}
      <div className="detail-card" style={{ padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '4rem 1.5rem', textAlign: 'center' }}>
            <Loading size="md" text="Loading invoices..." />
          </div>
        ) : filteredInvoices.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '4rem 1.5rem' }}>
            <div className="empty-state-icon" style={{ margin: '0 auto 1rem auto' }}>
              📄
            </div>
            <h3 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', marginBottom: '0.5rem' }}>
              No invoices found.
            </h3>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)', margin: 0 }}>
              {filterQuery ? 'No invoice records match your search filter.' : 'No invoices uploaded to the platform.'}
            </p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: 'var(--font-size-sm)' }}>
              <thead>
                <tr style={{ backgroundColor: 'var(--surface-hover)', borderBottom: '1px solid var(--border)' }}>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Invoice Document</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Customer</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Associated Equipment</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>File Size</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Uploaded At</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredInvoices.map((inv) => (
                  <tr key={inv.id} style={{ borderBottom: '1px solid var(--border)', transition: 'background-color 0.15s' }}>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <span
                          style={{
                            padding: '0.2rem 0.45rem',
                            borderRadius: 'var(--radius-sm)',
                            backgroundColor: 'var(--primary-light)',
                            color: 'var(--primary)',
                            fontSize: '0.7rem',
                            fontWeight: '700',
                            textTransform: 'uppercase'
                          }}
                        >
                          {inv.fileType?.includes('/') ? inv.fileType.split('/')[1] : inv.fileType}
                        </span>
                        <span style={{ fontWeight: '600', color: 'var(--text-primary)', wordBreak: 'break-all' }}>
                          {inv.fileName}
                        </span>
                      </div>
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ fontWeight: '600', color: 'var(--text-primary)' }}>{inv.customerName || 'N/A'}</div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>{inv.customerEmail}</div>
                    </td>
                    <td style={{ padding: '1rem 1.25rem', color: 'var(--text-primary)' }}>
                      {inv.productName || '—'}
                    </td>
                    <td style={{ padding: '1rem 1.25rem', color: 'var(--text-secondary)' }}>
                      {formatFileSize(inv.fileSize)}
                    </td>
                    <td style={{ padding: '1rem 1.25rem', color: 'var(--text-muted)', fontSize: 'var(--font-size-xs)', whiteSpace: 'nowrap' }}>
                      {formatDate(inv.uploadedAt)}
                    </td>
                    <td style={{ padding: '1rem 1.25rem', textAlign: 'right', whiteSpace: 'nowrap' }}>
                      <button
                        type="button"
                        className="btn btn-secondary btn-sm"
                        style={{ marginRight: '0.5rem' }}
                        onClick={() => handleView(inv.id)}
                      >
                        Preview
                      </button>
                      <button
                        type="button"
                        className="btn btn-primary btn-sm"
                        onClick={() => handleDownload(inv.id, inv.fileName)}
                      >
                        Download
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
