import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { productService } from '../services/productService';
import { invoiceService } from '../services/invoiceService';

export const InvoiceManagement = () => {
  const { id: productId } = useParams();

  const [product, setProduct] = useState(null);
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [deleting, setDeleting] = useState(false);
  
  const [selectedFile, setSelectedFile] = useState(null);
  const [fileError, setFileError] = useState('');
  const [apiError, setApiError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const [invoiceToDelete, setInvoiceToDelete] = useState(null);

  const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
  const ALLOWED_TYPES = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];

  const fetchData = async () => {
    try {
      setLoading(true);
      setApiError('');
      const [productData, invoiceData] = await Promise.all([
        productService.getProductById(productId),
        invoiceService.getInvoicesByProductId(productId),
      ]);
      setProduct(productData);
      setInvoices(invoiceData || []);
    } catch (err) {
      setApiError(err.message || 'Unable to load invoice information.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [productId]);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setSelectedFile(null);
    setFileError('');

    if (!file) return;

    // Validate type
    const lowerName = file.name.toLowerCase();
    const isExtensionValid =
      lowerName.endsWith('.pdf') ||
      lowerName.endsWith('.jpg') ||
      lowerName.endsWith('.jpeg') ||
      lowerName.endsWith('.png');

    if (!ALLOWED_TYPES.includes(file.type) && !isExtensionValid) {
      setFileError('This file type is not supported. Please upload a PDF, JPG, or PNG file.');
      return;
    }

    // Validate size (<= 10MB)
    if (file.size > MAX_FILE_SIZE_BYTES) {
      setFileError('File size exceeds the allowed limit (10 MB).');
      return;
    }

    setSelectedFile(file);
  };

  const handleUploadSubmit = async (e) => {
    e.preventDefault();
    setApiError('');
    setSuccessMessage('');

    if (!selectedFile) {
      setFileError('Please select a valid invoice file.');
      return;
    }

    try {
      setUploading(true);
      await invoiceService.uploadInvoice(productId, selectedFile);
      setSuccessMessage('Invoice uploaded successfully.');
      setSelectedFile(null);
      // Reset input element
      const fileInput = document.getElementById('invoiceFileInput');
      if (fileInput) fileInput.value = '';
      
      // Refresh list
      const updatedInvoices = await invoiceService.getInvoicesByProductId(productId);
      setInvoices(updatedInvoices || []);
    } catch (err) {
      setApiError(err.message || 'Unable to upload invoice. Please try again.');
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = async (invoiceId) => {
    try {
      setApiError('');
      const data = await invoiceService.getInvoiceDownload(invoiceId);
      if (data && data.downloadUrl) {
        window.open(data.downloadUrl, '_blank');
      } else {
        setApiError('Unable to generate download link.');
      }
    } catch (err) {
      setApiError(err.message || 'Failed to download invoice.');
    }
  };

  const handleDeleteConfirm = async () => {
    if (!invoiceToDelete) return;

    try {
      setDeleting(true);
      setApiError('');
      setSuccessMessage('');
      await invoiceService.deleteInvoice(invoiceToDelete.invoiceId);
      setSuccessMessage('Invoice deleted successfully.');
      setInvoiceToDelete(null);

      // Refresh list
      const updatedInvoices = await invoiceService.getInvoicesByProductId(productId);
      setInvoices(updatedInvoices || []);
    } catch (err) {
      setApiError(err.message || 'Unable to delete invoice. Please try again.');
      setInvoiceToDelete(null);
    } finally {
      setDeleting(false);
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes && bytes !== 0) return 'N/A';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 className="page-title">Invoice Management</h1>
          <p className="page-subtitle">Upload and manage purchase invoices for your product.</p>
        </div>
        <Link to={`/products/${productId}`} className="btn btn-secondary btn-sm">
          ← Back to Product Details
        </Link>
      </div>

      {successMessage && <div className="alert alert-success">{successMessage}</div>}
      {apiError && <div className="alert alert-error">{apiError}</div>}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <div className="loading-spinner" style={{ borderColor: 'var(--primary)', borderTopColor: 'transparent', width: '32px', height: '32px' }}></div>
          <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading invoices...</p>
        </div>
      ) : (
        <>
          {/* Product Info Banner */}
          {product && (
            <div className="details-card" style={{ marginBottom: '2rem', padding: '1.5rem 2rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
                <div>
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>{product.productName}</h3>
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                    {product.brand} {product.modelNumber ? `• Model: ${product.modelNumber}` : ''} • Serial: <code>{product.serialNumber}</code>
                  </p>
                </div>
                <span className="badge badge-active">Owner Verified</span>
              </div>
            </div>
          )}

          {/* Upload Section */}
          <div className="dashboard-section">
            <h2 className="section-title" style={{ marginBottom: '1rem' }}>Upload Purchase Invoice</h2>
            <form onSubmit={handleUploadSubmit}>
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label className="form-label" htmlFor="invoiceFileInput">
                  Select File (PDF, JPG, PNG — Max 10 MB) <span className="required-star">*</span>
                </label>
                <input
                  type="file"
                  id="invoiceFileInput"
                  className="form-input"
                  accept=".pdf, .jpg, .jpeg, .png, application/pdf, image/jpeg, image/png"
                  onChange={handleFileChange}
                  disabled={uploading}
                />
                {fileError && <div className="form-error">{fileError}</div>}
                {selectedFile && !fileError && (
                  <div style={{ fontSize: '0.85rem', color: 'var(--success)', marginTop: '0.35rem', fontWeight: 600 }}>
                    Selected: {selectedFile.name} ({formatFileSize(selectedFile.size)})
                  </div>
                )}
              </div>

              <button
                type="submit"
                className="btn btn-primary"
                disabled={uploading || !selectedFile || !!fileError}
              >
                {uploading ? (
                  <>
                    <span className="loading-spinner" /> Uploading invoice...
                  </>
                ) : (
                  'Upload Invoice'
                )}
              </button>
            </form>
          </div>

          {/* Invoice List Section */}
          <div className="dashboard-section">
            <h2 className="section-title" style={{ marginBottom: '1.25rem' }}>
              Attached Invoices ({invoices.length})
            </h2>

            {invoices.length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon">📄</span>
                <h3 className="empty-title">No invoice uploaded yet</h3>
                <p className="empty-desc">
                  Upload your purchase receipt or proof of purchase invoice above to link it with this product.
                </p>
              </div>
            ) : (
              <div className="table-container" style={{ margin: 0 }}>
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>File Name</th>
                      <th>Type</th>
                      <th>Size</th>
                      <th>Uploaded Date</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {invoices.map((inv) => (
                      <tr key={inv.invoiceId}>
                        <td>
                          <strong>{inv.fileName}</strong>
                        </td>
                        <td>
                          <span className="role-pill" style={{ textTransform: 'uppercase' }}>
                            {inv.fileType?.includes('/') ? inv.fileType.split('/')[1] : inv.fileType || 'FILE'}
                          </span>
                        </td>
                        <td>{formatFileSize(inv.fileSize)}</td>
                        <td>{inv.uploadedAt ? new Date(inv.uploadedAt).toLocaleDateString() : 'N/A'}</td>
                        <td>
                          <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <button
                              onClick={() => handleDownload(inv.invoiceId)}
                              className="btn btn-secondary btn-sm"
                            >
                              📥 View / Download
                            </button>
                            <button
                              onClick={() => setInvoiceToDelete(inv)}
                              className="btn btn-danger btn-sm"
                              disabled={deleting}
                            >
                              🗑️ Delete
                            </button>
                          </div>
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

      {/* Delete Confirmation Modal */}
      {invoiceToDelete && (
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
              Confirm Invoice Deletion
            </h3>
            <p style={{ color: 'var(--text-dark)', marginBottom: '0.5rem', fontSize: '0.95rem' }}>
              Are you sure you want to delete this invoice?
            </p>
            <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', fontSize: '0.85rem' }}>
              File: <strong>{invoiceToDelete.fileName}</strong>
            </p>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setInvoiceToDelete(null)}
                disabled={deleting}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-danger"
                onClick={handleDeleteConfirm}
                disabled={deleting}
              >
                {deleting ? (
                  <>
                    <span className="loading-spinner" /> Deleting invoice...
                  </>
                ) : (
                  'Yes, Delete Invoice'
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
