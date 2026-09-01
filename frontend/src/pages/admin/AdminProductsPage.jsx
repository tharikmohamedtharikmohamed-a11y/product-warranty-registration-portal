import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import adminService from '../../services/adminService';
import Loading from '../../components/Loading';
import ErrorMessage from '../../components/ErrorMessage';

/**
 * Admin Product Management Directory Page.
 * Displays all customer registered products across the system.
 * Read-only administrative view for hardware verification and auditing.
 * Phase 11 — Admin Management Module
 */
export default function AdminProductsPage() {
  const [products, setProducts] = useState([]);
  const [filterQuery, setFilterQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await adminService.getProducts();
      setProducts(data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load product registry.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

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

  const filteredProducts = products.filter((p) => {
    if (!filterQuery.trim()) return true;
    const q = filterQuery.toLowerCase();
    return (
      (p.productName && p.productName.toLowerCase().includes(q)) ||
      (p.brand && p.brand.toLowerCase().includes(q)) ||
      (p.modelNumber && p.modelNumber.toLowerCase().includes(q)) ||
      (p.serialNumber && p.serialNumber.toLowerCase().includes(q)) ||
      (p.customerName && p.customerName.toLowerCase().includes(q)) ||
      (p.customerEmail && p.customerEmail.toLowerCase().includes(q))
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
          <h1 className="page-title" style={{ marginBottom: '0.25rem' }}>Global Product Registry</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)', margin: 0 }}>
            Audit all customer-registered equipment, serial numbers, retailers, and baseline warranty terms.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--primary-light)', color: 'var(--primary)' }}>
            {products.length} Registered Products
          </span>
        </div>
      </div>

      {error && (
        <div style={{ marginBottom: '1.5rem' }}>
          <ErrorMessage message={error} />
        </div>
      )}

      {/* Filter Input */}
      <div className="detail-card" style={{ marginBottom: '1.5rem', padding: '1rem 1.25rem' }}>
        <input
          type="text"
          className="form-input"
          placeholder="Filter by product name, brand, serial number, customer name, or email..."
          value={filterQuery}
          onChange={(e) => setFilterQuery(e.target.value)}
        />
      </div>

      {/* Products Table */}
      <div className="detail-card" style={{ padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '4rem 1.5rem', textAlign: 'center' }}>
            <Loading size="md" text="Loading products..." />
          </div>
        ) : filteredProducts.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '4rem 1.5rem' }}>
            <div className="empty-state-icon" style={{ margin: '0 auto 1rem auto' }}>
              📦
            </div>
            <h3 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', marginBottom: '0.5rem' }}>
              No products found.
            </h3>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)', margin: 0 }}>
              {filterQuery ? 'No registered equipment matches your filter criteria.' : 'No equipment records in the database.'}
            </p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: 'var(--font-size-sm)' }}>
              <thead>
                <tr style={{ backgroundColor: 'var(--surface-hover)', borderBottom: '1px solid var(--border)' }}>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Product & Model</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Customer</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Serial Number</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Purchase Info</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Warranty Status</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Registered</th>
                </tr>
              </thead>
              <tbody>
                {filteredProducts.map((p) => (
                  <tr key={p.id} style={{ borderBottom: '1px solid var(--border)', transition: 'background-color 0.15s' }}>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ fontWeight: '700', color: 'var(--text-primary)' }}>{p.productName}</div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                        {p.brand} • {p.modelNumber}
                      </div>
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ fontWeight: '600', color: 'var(--text-primary)' }}>{p.customerName || 'N/A'}</div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>{p.customerEmail}</div>
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <span className="serial-tag">{p.serialNumber}</span>
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div>${Number(p.price).toFixed(2)} USD</div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                        {p.purchaseDate} ({p.warrantyDurationMonths} mos)
                      </div>
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      {p.warrantyStatus ? (
                        <span
                          className={`status-badge ${
                            p.warrantyStatus === 'ACTIVE'
                              ? 'badge-active'
                              : p.warrantyStatus === 'EXPIRING_SOON'
                              ? 'badge-expiring'
                              : 'badge-expired'
                          }`}
                        >
                          {p.warrantyStatus.replace('_', ' ')}
                        </span>
                      ) : (
                        <span style={{ color: 'var(--text-muted)', fontSize: 'var(--font-size-xs)' }}>—</span>
                      )}
                    </td>
                    <td style={{ padding: '1rem 1.25rem', color: 'var(--text-muted)', fontSize: 'var(--font-size-xs)', whiteSpace: 'nowrap' }}>
                      {formatDate(p.createdAt)}
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
