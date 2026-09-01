import React, { useState, useEffect } from 'react';
import notificationService from '../services/notificationService';

/**
 * Notifications Page (/notifications).
 * Comprehensive inbox of user alerts: warranty expirations, claim updates, invoice additions, system notices.
 * Phase 12 — Dashboard & Notifications
 */
export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL'); // 'ALL' or 'UNREAD'
  const [processingId, setProcessingId] = useState(null);
  const [isMarkingAll, setIsMarkingAll] = useState(false);

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await notificationService.getNotifications();
      setNotifications(data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load notifications. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const handleMarkAsRead = async (id) => {
    try {
      setProcessingId(id);
      await notificationService.markAsRead(id);
      setNotifications((prev) =>
        prev.map((item) => (item.id === id ? { ...item, read: true } : item))
      );
    } catch (err) {
      // Graceful fallback
    } finally {
      setProcessingId(null);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      setIsMarkingAll(true);
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((item) => ({ ...item, read: true })));
    } catch (err) {
      setError('Failed to mark all as read. Please try again.');
    } finally {
      setIsMarkingAll(false);
    }
  };

  const formatTimestamp = (dateString) => {
    if (!dateString) return '—';
    try {
      const date = new Date(dateString);
      const now = new Date();
      const diffMs = now - date;
      const diffMins = Math.floor(diffMs / 60000);
      const diffHours = Math.floor(diffMins / 60);
      const diffDays = Math.floor(diffHours / 24);

      if (diffMins < 1) return 'Just now';
      if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
      if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
      if (diffDays === 1) return 'Yesterday';
      if (diffDays < 7) return `${diffDays} days ago`;

      return date.toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      return dateString;
    }
  };

  const getTypeBadge = (type) => {
    switch (type) {
      case 'EXPIRATION':
        return { label: 'Expiration Alert', color: 'var(--warning, #f59e0b)', bg: 'rgba(245, 158, 11, 0.12)' };
      case 'CLAIM_UPDATE':
        return { label: 'Claim Update', color: 'var(--info, #0284c7)', bg: 'rgba(2, 132, 199, 0.12)' };
      case 'SYSTEM':
        return { label: 'System Notice', color: 'var(--primary, #2563eb)', bg: 'rgba(37, 99, 235, 0.12)' };
      case 'INFO':
      default:
        return { label: 'Information', color: 'var(--text-secondary, #64748b)', bg: 'rgba(100, 116, 139, 0.12)' };
    }
  };

  const unreadCount = notifications.filter((n) => !n.read).length;
  const filteredNotifications = filter === 'UNREAD' ? notifications.filter((n) => !n.read) : notifications;

  return (
    <div className="container" style={{ padding: '2rem 1rem', maxWidth: '800px', margin: '0 auto' }}>
      {/* Header */}
      <div
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '1rem',
          marginBottom: '2rem'
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <h1 style={{ fontSize: 'var(--font-size-2xl, 1.75rem)', fontWeight: '800', margin: 0, color: 'var(--text-primary)' }}>
              Notifications
            </h1>
            {unreadCount > 0 && (
              <span
                style={{
                  background: 'rgba(37, 99, 235, 0.15)',
                  color: 'var(--primary, #2563eb)',
                  padding: '0.2rem 0.6rem',
                  borderRadius: '9999px',
                  fontSize: '0.8125rem',
                  fontWeight: '700'
                }}
              >
                {unreadCount} unread
              </span>
            )}
          </div>
          <p style={{ color: 'var(--text-secondary)', margin: '0.375rem 0 0', fontSize: 'var(--font-size-sm)' }}>
            Stay updated on warranty expiries, claim statuses, and account activity.
          </p>
        </div>

        {unreadCount > 0 && (
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={handleMarkAllAsRead}
            disabled={isMarkingAll}
          >
            {isMarkingAll ? 'Marking...' : 'Mark all as read'}
          </button>
        )}
      </div>

      {/* Filter Tabs */}
      <div
        style={{
          display: 'flex',
          gap: '0.5rem',
          borderBottom: '1px solid var(--border, #e2e8f0)',
          marginBottom: '1.5rem',
          paddingBottom: '0.5rem'
        }}
      >
        <button
          type="button"
          onClick={() => setFilter('ALL')}
          style={{
            background: 'none',
            border: 'none',
            padding: '0.5rem 1rem',
            cursor: 'pointer',
            fontWeight: filter === 'ALL' ? '700' : '500',
            color: filter === 'ALL' ? 'var(--primary, #2563eb)' : 'var(--text-secondary)',
            borderBottom: filter === 'ALL' ? '2px solid var(--primary, #2563eb)' : '2px solid transparent',
            marginBottom: '-0.5rem',
            transition: 'all 0.15s ease'
          }}
        >
          All ({notifications.length})
        </button>

        <button
          type="button"
          onClick={() => setFilter('UNREAD')}
          style={{
            background: 'none',
            border: 'none',
            padding: '0.5rem 1rem',
            cursor: 'pointer',
            fontWeight: filter === 'UNREAD' ? '700' : '500',
            color: filter === 'UNREAD' ? 'var(--primary, #2563eb)' : 'var(--text-secondary)',
            borderBottom: filter === 'UNREAD' ? '2px solid var(--primary, #2563eb)' : '2px solid transparent',
            marginBottom: '-0.5rem',
            transition: 'all 0.15s ease'
          }}
        >
          Unread ({unreadCount})
        </button>
      </div>

      {/* Error alert */}
      {error && (
        <div
          className="alert alert-danger"
          style={{
            marginBottom: '1.5rem',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
          }}
        >
          <span>{error}</span>
          <button type="button" className="btn btn-ghost btn-xs" onClick={fetchNotifications}>
            Retry
          </button>
        </div>
      )}

      {/* Loading state */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem 1rem', color: 'var(--text-muted)' }}>
          <div className="spinner spinner-md" style={{ marginBottom: '1rem' }}></div>
          <p>Loading your notifications...</p>
        </div>
      ) : filteredNotifications.length === 0 ? (
        /* Empty State */
        <div
          style={{
            textAlign: 'center',
            padding: '4rem 1.5rem',
            background: 'var(--surface, #ffffff)',
            borderRadius: 'var(--radius-lg, 0.75rem)',
            border: '1px solid var(--border, #e2e8f0)'
          }}
        >
          <div
            style={{
              width: '48px',
              height: '48px',
              borderRadius: '50%',
              background: 'rgba(37, 99, 235, 0.1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 1rem',
              color: 'var(--primary, #2563eb)'
            }}
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
              <path d="M13.73 21a2 2 0 0 1-3.46 0" />
            </svg>
          </div>
          <h3 style={{ margin: '0 0 0.5rem', fontSize: 'var(--font-size-lg)', color: 'var(--text-primary)' }}>
            {filter === 'UNREAD' ? 'No unread notifications' : 'No notifications yet'}
          </h3>
          <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)', maxWidth: '400px', marginLeft: 'auto', marginRight: 'auto' }}>
            {filter === 'UNREAD'
              ? 'You are all caught up! Great job staying on top of your warranties.'
              : 'Alerts regarding expiring warranties, claim approvals, and product updates will appear here.'}
          </p>
        </div>
      ) : (
        /* Notification List */
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.875rem' }}>
          {filteredNotifications.map((notification) => {
            const badge = getTypeBadge(notification.type);
            return (
              <div
                key={notification.id}
                style={{
                  background: 'var(--surface, #ffffff)',
                  borderRadius: 'var(--radius-lg, 0.75rem)',
                  border: '1px solid var(--border, #e2e8f0)',
                  borderLeft: !notification.read ? '4px solid var(--primary, #2563eb)' : '1px solid var(--border, #e2e8f0)',
                  padding: '1.25rem',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.625rem',
                  transition: 'box-shadow 0.15s ease',
                  backgroundColor: !notification.read ? 'rgba(37, 99, 235, 0.02)' : 'var(--surface, #ffffff)'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem', flexWrap: 'wrap' }}>
                    <span
                      style={{
                        fontSize: '0.6875rem',
                        fontWeight: '700',
                        color: badge.color,
                        background: badge.bg,
                        padding: '0.2rem 0.5rem',
                        borderRadius: '0.25rem',
                        textTransform: 'uppercase',
                        letterSpacing: '0.04em'
                      }}
                    >
                      {badge.label}
                    </span>
                    <h2
                      style={{
                        margin: 0,
                        fontSize: 'var(--font-size-base, 1rem)',
                        fontWeight: notification.read ? '600' : '700',
                        color: 'var(--text-primary)'
                      }}
                    >
                      {notification.title}
                    </h2>
                  </div>

                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                    {formatTimestamp(notification.createdAt)}
                  </span>
                </div>

                <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)', lineHeight: '1.5' }}>
                  {notification.message}
                </p>

                <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '0.25rem' }}>
                  {!notification.read ? (
                    <button
                      type="button"
                      className="btn btn-ghost btn-xs"
                      onClick={() => handleMarkAsRead(notification.id)}
                      disabled={processingId === notification.id}
                      style={{ color: 'var(--primary, #2563eb)', fontWeight: '600' }}
                    >
                      {processingId === notification.id ? 'Marking...' : 'Mark as read'}
                    </button>
                  ) : (
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Read</span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
