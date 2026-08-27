import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import productService from '../services/productService';

/**
 * Product Listing & Portfolio Overview Page.
 * Displays customer's registered products, warranty statuses, metrics, and search/filtering.
 * Phase 7 — Product Management
 */
export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Deletion modal state
  const [productToDelete, setProductToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await productService.getProducts();
      setProducts(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load your registered products.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  // Compute metrics
  const metrics = useMemo(() => {
    const total = products.length;
    let active = 0;
    let expiring = 0;
    let expired = 0;

    products.forEach((p) => {
      const status = p.warranty?.status;
      if (status === 'ACTIVE') active += 1;
      else if (status === 'EXPIRING_SOON') expiring += 1;
      else if (status === 'EXPIRED') expired += 1;
    });

    return { total, active, expiring, expired };
  }, [products]);

  // Extract distinct categories
  const categories = useMemo(() => {
    const cats = new Set(products.map((p) => p.category).filter(Boolean));
    return Array.from(cats);
  }, [products]);

  // Filter products by search query, category, and status
  const filteredProducts = useMemo(() => {
    return products.filter((p) => {
      const matchesSearch =
        searchTerm.trim() === '' ||
        p.productName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.serialNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.brand?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.modelNumber?.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesCategory =
        categoryFilter === 'ALL' || p.category?.toLowerCase() === categoryFilter.toLowerCase();

      const matchesStatus =
        statusFilter === 'ALL' || p.warranty?.status === statusFilter;

      return matchesSearch && matchesCategory && matchesStatus;
    });
  }, [products, searchTerm, categoryFilter, statusFilter]);

  const handleDeleteConfirm = async () => {
    if (!productToDelete) return;
    try {
      setDeleting(true);
      setDeleteError('');
      await productService.deleteProduct(productToDelete.id);
      setProducts((prev) => prev.filter((p) => p.id !== productToDelete.id));
      setProductToDelete(null);
    } catch (err) {
      setDeleteError(err.response?.data?.message || 'Failed to delete product.');
    } finally {
      setDeleting(false);
    }
  };

  const renderStatusBadge = (status) => {
    switch (status) {
      case 'ACTIVE':
        return (
          <span className="status-badge badge-active">
            <span className="status-dot"></span> Active
          </span>
        );
      case 'EXPIRING_SOON':
        return (
          <span className="status-badge badge-expiring">
            <span className="status-dot"></span> Expiring Soon
          </span>
        );
      case 'EXPIRED':
        return (
          <span className="status-badge badge-expired">
            <span className="status-dot"></span> Expired
          </span>
        );
      default:
        return <span className="status-badge">{status || 'UNKNOWN'}</span>;
    }
  };

  return (
    <div className="container" style={{ padding: '3rem 1.5rem', flex: 1 }}>
      {/* Header Bar */}
      <div className="page-header-row">
        <div>
          <h1 className="page-title">My Registered Products</h1>
          <p className="page-subtitle">
            Manage your warranty protections, purchase records, and equipment terms.
          </p>
        </div>
        <Link to="/products/register" className="btn btn-primary btn-md">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          Register New Product
        </Link>
      </div>

      {/* Metrics Strip */}
      <div className="metrics-strip">
        <div className="metric-card">
          <span className="metric-label">Total Products</span>
          <span className="metric-value">{metrics.total}</span>
        </div>
        <div className="metric-card" style={{ borderLeft: '4px solid var(--success)' }}>
          <span className="metric-label">Active Warranties</span>
          <span className="metric-value" style={{ color: 'var(--success)' }}>{metrics.active}</span>
        </div>
        <div className="metric-card" style={{ borderLeft: '4px solid var(--warning)' }}>
          <span className="metric-label">Expiring Soon</span>
          <span className="metric-value" style={{ color: 'var(--warning)' }}>{metrics.expiring}</span>
        </div>
        <div className="metric-card" style={{ borderLeft: '4px solid var(--danger)' }}>
          <span className="metric-label">Expired</span>
          <span className="metric-value" style={{ color: 'var(--danger)' }}>{metrics.expired}</span>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="filter-bar">
        <div className="search-box">
          <span className="search-icon-slot">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
          </span>
          <input
            type="text"
            className="form-input"
            placeholder="Search by name, brand, model, or serial..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="filter-dropdowns">
          <select
            className="form-input"
            style={{ width: 'auto', minWidth: '160px' }}
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
          >
            <option value="ALL">All Categories</option>
            {categories.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>

          <select
            className="form-input"
            style={{ width: 'auto', minWidth: '160px' }}
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="ALL">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="EXPIRING_SOON">Expiring Soon</option>
            <option value="EXPIRED">Expired</option>
          </select>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="error-alert" style={{ marginBottom: '1.5rem' }}>
          <span>{error}</span>
          <button
            onClick={fetchProducts}
            className="btn btn-secondary btn-sm"
            style={{ marginLeft: 'auto' }}
          >
            Retry
          </button>
        </div>
      )}

      {/* Loading State */}
      {loading ? (
        <div className="loading-container">
          <div className="spinner spinner-lg"></div>
          <span>Loading your registered products...</span>
        </div>
      ) : products.length === 0 ? (
        /* Empty State (No Products Registered) */
        <div className="empty-state">
          <div className="empty-state-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
              <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
              <line x1="12" y1="22.08" x2="12" y2="12"></line>
            </svg>
          </div>
          <h2 className="empty-state-title">No products registered yet</h2>
          <p className="empty-state-desc">
            Get started by registering your first purchased appliance or electronic item to unlock automated warranty tracking.
          </p>
          <Link to="/products/register" className="btn btn-primary btn-md">
            Register Your First Product
          </Link>
        </div>
      ) : filteredProducts.length === 0 ? (
        /* Empty State (No Filter Matches) */
        <div className="empty-state">
          <h2 className="empty-state-title">No matching products found</h2>
          <p className="empty-state-desc">
            Try adjusting your search query or reset filter dropdowns to view your registered items.
          </p>
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => {
              setSearchTerm('');
              setCategoryFilter('ALL');
              setStatusFilter('ALL');
            }}
          >
            Reset Filters
          </button>
        </div>
      ) : (
        /* Products Grid */
        <div className="products-grid">
          {filteredProducts.map((p) => {
            const warranty = p.warranty;
            return (
              <div key={p.id} className="product-card">
                <div>
                  <div className="product-card-header">
                    <div>
                      <div className="product-card-brand">{p.brand} • {p.category}</div>
                      <h3 className="product-card-title">{p.productName}</h3>
                    </div>
                    {renderStatusBadge(warranty?.status)}
                  </div>

                  <div className="product-card-meta" style={{ marginTop: '1rem' }}>
                    <div className="meta-item">
                      <span className="meta-label">Model</span>
                      <span className="meta-value">{p.modelNumber}</span>
                    </div>
                    <div className="meta-item">
                      <span className="meta-label">Serial Number</span>
                      <span className="serial-tag">{p.serialNumber}</span>
                    </div>
                    <div className="meta-item">
                      <span className="meta-label">Purchase Date</span>
                      <span className="meta-value">{p.purchaseDate}</span>
                    </div>
                    <div className="meta-item">
                      <span className="meta-label">Warranty Expiry</span>
                      <span className="meta-value" style={{ color: warranty?.status === 'EXPIRED' ? 'var(--danger)' : 'var(--text-primary)' }}>
                        {warranty?.expiryDate || 'N/A'}
                      </span>
                    </div>
                    {warranty?.daysRemaining !== undefined && warranty?.status !== 'EXPIRED' && (
                      <div className="meta-item">
                        <span className="meta-label">Coverage Left</span>
                        <span className="meta-value" style={{ color: 'var(--primary)', fontWeight: '700' }}>
                          {warranty.daysRemaining} days remaining
                        </span>
                      </div>
                    )}
                  </div>
                </div>

                <div className="product-card-footer">
                  <Link to={`/products/${p.id}`} className="btn btn-ghost btn-sm">
                    View Details
                  </Link>

                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <Link to={`/products/${p.id}/edit`} className="btn btn-secondary btn-sm" aria-label={`Edit ${p.productName}`}>
                      Edit
                    </Link>
                    <button
                      type="button"
                      className="btn btn-ghost btn-sm"
                      style={{ color: 'var(--danger)' }}
                      onClick={() => setProductToDelete(p)}
                      aria-label={`Delete ${p.productName}`}
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {productToDelete && (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal-dialog">
            <h3 className="modal-title">Delete Product Registration?</h3>
            <p className="modal-desc">
              Are you sure you want to delete <strong>{productToDelete.productName}</strong> (SN: {productToDelete.serialNumber})?
              This action will permanently delete this product and its associated warranty record.
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
                  setProductToDelete(null);
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
                onClick={handleDeleteConfirm}
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
