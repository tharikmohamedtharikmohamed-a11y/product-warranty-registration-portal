import React, { useState, useRef } from 'react';
import invoiceService from '../services/invoiceService';

/**
 * Invoice Upload Modal Component.
 * Supports file selection, validation (PDF, JPG, JPEG, PNG <= 10MB),
 * metadata preview, upload progress, and error display.
 * Phase 9 — Invoice Management
 */
export default function InvoiceUpload({ productId, productName, onSuccess, onClose }) {
  const [selectedFile, setSelectedFile] = useState(null);
  const [fileError, setFileError] = useState('');
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');
  const [uploadSuccess, setUploadSuccess] = useState(false);
  const [isDragging, setIsDragging] = useState(false);

  const fileInputRef = useRef(null);

  const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
  const ALLOWED_EXTENSIONS = ['pdf', 'jpg', 'jpeg', 'png'];

  const formatFileSize = (bytes) => {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const validateSelectedFile = (file) => {
    if (!file) {
      return 'Please select an invoice file.';
    }

    if (file.size === 0) {
      return 'Invoice file is empty.';
    }

    if (file.size > MAX_FILE_SIZE) {
      return 'Maximum file size is 10 MB.';
    }

    const extension = file.name.split('.').pop()?.toLowerCase();
    if (!extension || !ALLOWED_EXTENSIONS.includes(extension)) {
      return 'Only PDF, JPG, JPEG, and PNG files are allowed.';
    }

    return '';
  };

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    setFileError('');
    setUploadError('');

    if (file) {
      const errorMsg = validateSelectedFile(file);
      if (errorMsg) {
        setFileError(errorMsg);
        setSelectedFile(null);
      } else {
        setSelectedFile(file);
      }
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    setFileError('');
    setUploadError('');

    const file = e.dataTransfer.files?.[0];
    if (file) {
      const errorMsg = validateSelectedFile(file);
      if (errorMsg) {
        setFileError(errorMsg);
        setSelectedFile(null);
      } else {
        setSelectedFile(file);
      }
    }
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    setFileError('');
    setUploadError('');

    if (!selectedFile) {
      setFileError('Please select an invoice file.');
      return;
    }

    const validationMsg = validateSelectedFile(selectedFile);
    if (validationMsg) {
      setFileError(validationMsg);
      return;
    }

    try {
      setUploading(true);
      const uploadedInvoice = await invoiceService.uploadInvoice(productId, selectedFile);
      setUploadSuccess(true);
      setTimeout(() => {
        if (onSuccess) {
          onSuccess(uploadedInvoice);
        }
        if (onClose) {
          onClose();
        }
      }, 1000);
    } catch (err) {
      const message = err.response?.data?.message || 'Failed to upload invoice. Please check the file and try again.';
      setUploadError(message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="upload-modal-title">
      <div className="modal-dialog" style={{ maxWidth: '520px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h3 id="upload-modal-title" className="modal-title" style={{ margin: 0 }}>
            Upload Purchase Invoice
          </h3>
          <button
            type="button"
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '1.25rem',
              cursor: 'pointer',
              color: 'var(--text-muted)'
            }}
            aria-label="Close modal"
          >
            ✕
          </button>
        </div>

        <p className="modal-desc" style={{ marginBottom: '1.25rem' }}>
          Attach official proof of purchase for <strong>{productName || 'your product'}</strong>.
        </p>

        {uploadSuccess && (
          <div
            style={{
              padding: '0.875rem 1rem',
              borderRadius: 'var(--radius-md)',
              backgroundColor: 'var(--success-bg)',
              color: 'var(--success-text)',
              marginBottom: '1.25rem',
              fontSize: 'var(--font-size-sm)',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <path d="M20 6L9 17l-5-5" />
            </svg>
            Invoice uploaded successfully!
          </div>
        )}

        {uploadError && (
          <div
            className="error-alert"
            style={{ marginBottom: '1.25rem', fontSize: 'var(--font-size-sm)' }}
            role="alert"
          >
            {uploadError}
          </div>
        )}

        {fileError && (
          <div
            className="error-alert"
            style={{ marginBottom: '1.25rem', fontSize: 'var(--font-size-sm)' }}
            role="alert"
          >
            {fileError}
          </div>
        )}

        <form onSubmit={handleUpload}>
          {/* Drag and drop upload zone */}
          <div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            style={{
              border: `2px dashed ${isDragging ? 'var(--primary)' : 'var(--border)'}`,
              borderRadius: 'var(--radius-lg)',
              padding: '2rem 1.5rem',
              textAlign: 'center',
              backgroundColor: isDragging ? 'var(--primary-light)' : 'var(--background)',
              cursor: 'pointer',
              transition: 'all var(--transition-fast)',
              marginBottom: '1.25rem'
            }}
          >
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              accept=".pdf,.jpg,.jpeg,.png"
              style={{ display: 'none' }}
              disabled={uploading || uploadSuccess}
            />

            <div style={{ color: 'var(--primary)', marginBottom: '0.75rem' }}>
              <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="15" />
              </svg>
            </div>

            <p style={{ fontWeight: '600', color: 'var(--text-primary)', marginBottom: '0.25rem' }}>
              Click to choose file or drag and drop
            </p>
            <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
              PDF, JPG, JPEG, or PNG up to 10 MB
            </p>
          </div>

          {/* Selected File Details */}
          {selectedFile && (
            <div
              style={{
                padding: '0.875rem 1rem',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--surface-hover)',
                border: '1px solid var(--border)',
                marginBottom: '1.25rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', overflow: 'hidden' }}>
                <span
                  style={{
                    backgroundColor: 'var(--primary-light)',
                    color: 'var(--primary)',
                    fontWeight: '700',
                    fontSize: 'var(--font-size-xs)',
                    padding: '0.25rem 0.5rem',
                    borderRadius: 'var(--radius-sm)',
                    textTransform: 'uppercase'
                  }}
                >
                  {selectedFile.name.split('.').pop() || 'FILE'}
                </span>
                <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: '600', color: 'var(--text-primary)' }}>
                    {selectedFile.name}
                  </div>
                  <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                    {formatFileSize(selectedFile.size)} • {selectedFile.type || 'application/octet-stream'}
                  </div>
                </div>
              </div>

              {!uploading && (
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    setSelectedFile(null);
                    if (fileInputRef.current) fileInputRef.current.value = '';
                  }}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: 'var(--text-muted)',
                    cursor: 'pointer',
                    fontSize: '1rem',
                    padding: '0.25rem'
                  }}
                  title="Remove selected file"
                >
                  ✕
                </button>
              )}
            </div>
          )}

          {/* Actions */}
          <div className="modal-actions">
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={onClose}
              disabled={uploading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary btn-sm"
              disabled={!selectedFile || uploading || uploadSuccess}
              style={{ minWidth: '130px' }}
            >
              {uploading ? (
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <div className="spinner spinner-sm"></div>
                  Uploading invoice...
                </span>
              ) : (
                'Upload Invoice'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
