import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import adminService from '../../services/adminService';
import Loading from '../../components/Loading';
import ErrorMessage from '../../components/ErrorMessage';

/**
 * Admin User Management Directory Page.
 * Displays all registered user accounts with role badges, registration dates,
 * and search filtering. Never exposes password hashes or security credentials.
 * Phase 11 — Admin Management Module
 */
export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchUsers = async (query = '') => {
    try {
      setLoading(true);
      setError('');
      const data = await adminService.getUsers(query);
      setUsers(data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load user directory.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchUsers(searchTerm);
  };

  const handleClearSearch = () => {
    setSearchTerm('');
    fetchUsers('');
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

  const customerCount = users.filter((u) => u.role === 'CUSTOMER').length;
  const adminCount = users.filter((u) => u.role === 'ADMIN').length;

  return (
    <div className="container" style={{ padding: '3rem 1.5rem', flex: 1, maxWidth: '1120px' }}>
      {/* Navigation Breadcrumb */}
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to="/admin" className="btn btn-ghost btn-sm" style={{ paddingLeft: 0 }}>
          ← Back to Operations Dashboard
        </Link>
      </div>

      {/* Page Title & Counters */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '2rem' }}>
        <div>
          <h1 className="page-title" style={{ marginBottom: '0.25rem' }}>User Management Directory</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)', margin: 0 }}>
            Audit registered accounts, role entitlements, and identity timestamps across the system.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--primary-light)', color: 'var(--primary)' }}>
            {users.length} Total Users
          </span>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--success-bg)', color: 'var(--success)' }}>
            {customerCount} Customers
          </span>
          <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', padding: '0.35rem 0.75rem', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--warning-bg)', color: 'var(--warning)' }}>
            {adminCount} Admins
          </span>
        </div>
      </div>

      {error && (
        <div style={{ marginBottom: '1.5rem' }}>
          <ErrorMessage message={error} />
        </div>
      )}

      {/* Search Bar */}
      <div className="detail-card" style={{ marginBottom: '1.5rem', padding: '1rem 1.25rem' }}>
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <input
            type="text"
            className="form-input"
            style={{ flex: 1, minWidth: '220px' }}
            placeholder="Search users by name or email address..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button type="submit" className="btn btn-primary btn-md">
            Search
          </button>
          {searchTerm && (
            <button type="button" className="btn btn-secondary btn-md" onClick={handleClearSearch}>
              Clear
            </button>
          )}
        </form>
      </div>

      {/* Users Table */}
      <div className="detail-card" style={{ padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '4rem 1.5rem', textAlign: 'center' }}>
            <Loading size="md" text="Loading users..." />
          </div>
        ) : users.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '4rem 1.5rem' }}>
            <div className="empty-state-icon" style={{ margin: '0 auto 1rem auto' }}>
              👥
            </div>
            <h3 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', marginBottom: '0.5rem' }}>
              No users found.
            </h3>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)', margin: 0 }}>
              {searchTerm ? 'No registered accounts match your search filter.' : 'No users registered in the database.'}
            </p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: 'var(--font-size-sm)' }}>
              <thead>
                <tr style={{ backgroundColor: 'var(--surface-hover)', borderBottom: '1px solid var(--border)' }}>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Full Name</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Email Address</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>System Role</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>Registered At</th>
                  <th style={{ padding: '0.875rem 1.25rem', fontWeight: '700', color: 'var(--text-secondary)' }}>User Identifier</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id} style={{ borderBottom: '1px solid var(--border)', transition: 'background-color 0.15s' }}>
                    <td style={{ padding: '1rem 1.25rem', fontWeight: '600', color: 'var(--text-primary)' }}>
                      {u.name}
                    </td>
                    <td style={{ padding: '1rem 1.25rem', color: 'var(--text-secondary)' }}>
                      {u.email}
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <span
                        className={`status-badge ${u.role === 'ADMIN' ? 'badge-expiring' : 'badge-active'}`}
                        style={{ fontSize: '0.75rem', textTransform: 'uppercase' }}
                      >
                        {u.role}
                      </span>
                    </td>
                    <td style={{ padding: '1rem 1.25rem', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                      {formatDate(u.createdAt)}
                    </td>
                    <td style={{ padding: '1rem 1.25rem', fontFamily: 'monospace', fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                      {u.id}
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
