import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import productService from '../services/productService';

/**
 * Customer Dashboard Page.
 * Displays high-level account status, quick metrics, recent registered products, and actions.
 * Phase 7 — Product Management
 */
export default function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadOverview = async () => {
      try {
        setLoading(true);
        const data = await productService.getProducts();
        setProducts(data);
      } catch (e) {
        // Fallback gracefully if request fails
      } finally {
        setLoading(false);
      }
    };

    loadOverview();
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const activeCount = products.filter((p) => p.warranty?.status === 'ACTIVE').length;
  const expiringCount = products.filter((p) => p.warranty?.status === 'EXPIRING_SOON').length;

  return (
    <div className="container" style={{ padding: '3rem 1.5rem', flex: 1, maxWidth: '1000px' }}>
      {/* Welcome Banner */}
      <div
        style={{
          background: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)',
          color: '#ffffff',
          borderRadius: 'var(--radius-xl)',
          padding: '2.5rem',
          marginBottom: '2rem',
          boxShadow: 'var(--shadow-md)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1.5rem'
        }}
      >
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', background: 'rgba(37, 99, 235, 0.2)', color: '#93c5fd', padding: '0.25rem 0.75rem', borderRadius: 'var(--radius-full)', fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', marginBottom: '0.75rem' }}>
            Customer Portal • Phase 7
          </div>
          <h1 style={{ fontSize: 'var(--font-size-3xl)', fontWeight: '800', marginBottom: '0.5rem', letterSpacing: '-0.02em' }}>
            Welcome back, {user?.name || 'User'}!
          </h1>
          <p style={{ color: '#94a3b8', fontSize: 'var(--font-size-sm)', maxWidth: '520px', lineHeight: '1.5' }}>
            WarrantyHub is currently tracking your equipment lifecycle, active warranty validity dates, and proof-of-purchase terms.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <Link to="/products/register" className="btn btn-primary btn-md">
            + Register Product
          </Link>
          <Link to="/warranties" className="btn btn-secondary btn-md" style={{ color: '#ffffff', borderColor: '#475569', backgroundColor: '#334155' }}>
            View Warranties
          </Link>
          <Link to="/products" className="btn btn-ghost btn-md" style={{ color: '#93c5fd' }}>
            Products
          </Link>
        </div>
      </div>

      {/* Quick Metrics */}
      <div className="metrics-strip">
        <div className="metric-card">
          <span className="metric-label">Registered Products</span>
          <span className="metric-value">{loading ? '...' : products.length}</span>
        </div>
        <div className="metric-card" style={{ borderLeft: '4px solid var(--success)' }}>
          <span className="metric-label">Active Warranties</span>
          <span className="metric-value" style={{ color: 'var(--success)' }}>{loading ? '...' : activeCount}</span>
        </div>
        <div className="metric-card" style={{ borderLeft: '4px solid var(--warning)' }}>
          <span className="metric-label">Expiring Soon (30d)</span>
          <span className="metric-value" style={{ color: 'var(--warning)' }}>{loading ? '...' : expiringCount}</span>
        </div>
        <div className="metric-card">
          <span className="metric-label">Account Role</span>
          <span className="metric-value" style={{ fontSize: 'var(--font-size-xl)', color: 'var(--primary)' }}>
            {user?.role || 'CUSTOMER'}
          </span>
        </div>
      </div>

      {/* Recent Products / Activity */}
      <div className="detail-card" style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
          <h2 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '700', color: 'var(--text-primary)' }}>
            Recently Registered Products
          </h2>
          <Link to="/products" className="btn btn-ghost btn-sm">
            View All ({products.length}) →
          </Link>
        </div>

        {loading ? (
          <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            <div className="spinner spinner-sm" style={{ marginRight: '0.5rem' }}></div>
            Loading products...
          </div>
        ) : products.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '2.5rem 1rem', background: 'var(--background)', borderRadius: 'var(--radius-lg)', border: '1px dashed var(--border)' }}>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)', marginBottom: '1rem' }}>
              No products registered in your account yet.
            </p>
            <Link to="/products/register" className="btn btn-primary btn-sm">
              Register Your First Product
            </Link>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {products.slice(0, 3).map((p) => (
              <div
                key={p.id}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  padding: '1rem',
                  background: 'var(--background)',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border)'
                }}
              >
                <div>
                  <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                    {p.brand} • {p.category}
                  </div>
                  <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: '700', color: 'var(--text-primary)' }}>
                    {p.productName}
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <span
                    className={`status-badge ${
                      p.warranty?.status === 'ACTIVE'
                        ? 'badge-active'
                        : p.warranty?.status === 'EXPIRING_SOON'
                        ? 'badge-expiring'
                        : 'badge-expired'
                    }`}
                  >
                    <span className="status-dot"></span> {p.warranty?.status?.replace('_', ' ') || 'ACTIVE'}
                  </span>
                  <Link to={`/products/${p.id}`} className="btn btn-ghost btn-sm">
                    Details
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Account Info Footer */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
          padding: '1.25rem',
          background: 'var(--surface)',
          borderRadius: 'var(--radius-lg)',
          border: '1px solid var(--border)'
        }}
      >
        <div>
          <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>Logged in as:</div>
          <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: '600', color: 'var(--text-primary)' }}>
            {user?.name} ({user?.email})
          </div>
        </div>
        <button
          type="button"
          className="btn btn-secondary btn-sm"
          onClick={handleLogout}
        >
          Sign Out
        </button>
      </div>
    </div>
  );
}
