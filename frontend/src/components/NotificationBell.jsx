import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import notificationService from '../services/notificationService';

/**
 * Global Notification Bell Component with Dropdown Preview.
 * Displays real unread count and compact notification preview.
 * Phase 12 — Dashboard & Notifications
 */
export default function NotificationBell() {
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  // Load unread count on mount and refresh every 60s or on user interaction
  const fetchUnreadCount = async () => {
    try {
      const count = await notificationService.getUnreadCount();
      setUnreadCount(count);
    } catch (e) {
      // Silently fail if unauthenticated or network drop
    }
  };

  useEffect(() => {
    fetchUnreadCount();
    const interval = setInterval(fetchUnreadCount, 60000);
    return () => clearInterval(interval);
  }, []);

  // Fetch recent notifications when dropdown is opened
  useEffect(() => {
    if (!isOpen) return;

    const loadNotifications = async () => {
      try {
        setLoading(true);
        const data = await notificationService.getNotifications();
        // Limit to 5 for dropdown preview
        setNotifications(data.slice(0, 5));
      } catch (e) {
        // Handle error gracefully
      } finally {
        setLoading(false);
      }
    };

    loadNotifications();
  }, [isOpen]);

  // Click outside to close
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen]);

  // Handle escape key
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && isOpen) {
        setIsOpen(false);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen]);

  const handleMarkAsRead = async (id, e) => {
    e.stopPropagation();
    try {
      await notificationService.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (e) {
      // Silently handle
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch (e) {
      // Silently handle
    }
  };

  const formatTime = (dateString) => {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      const now = new Date();
      const diffMs = now - date;
      const diffMins = Math.floor(diffMs / 60000);
      const diffHours = Math.floor(diffMins / 60);
      const diffDays = Math.floor(diffHours / 24);

      if (diffMins < 1) return 'Just now';
      if (diffMins < 60) return `${diffMins}m ago`;
      if (diffHours < 24) return `${diffHours}h ago`;
      if (diffDays < 7) return `${diffDays}d ago`;
      return date.toLocaleDateString();
    } catch (e) {
      return '';
    }
  };

  return (
    <div style={{ position: 'relative' }} ref={dropdownRef}>
      {/* Bell Button */}
      <button
        type="button"
        className="btn btn-ghost btn-sm"
        onClick={() => setIsOpen(!isOpen)}
        aria-label={`Notifications, ${unreadCount} unread`}
        aria-haspopup="true"
        aria-expanded={isOpen}
        style={{
          position: 'relative',
          padding: '0.5rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'var(--text-secondary)'
        }}
      >
        <svg
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>

        {unreadCount > 0 && (
          <span
            style={{
              position: 'absolute',
              top: '2px',
              right: '2px',
              background: 'var(--danger, #ef4444)',
              color: '#ffffff',
              fontSize: '10px',
              fontWeight: '700',
              borderRadius: '9999px',
              minWidth: '18px',
              height: '18px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              padding: '0 4px',
              lineHeight: 1
            }}
          >
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown Panel */}
      {isOpen && (
        <div
          style={{
            position: 'absolute',
            top: 'calc(100% + 0.5rem)',
            right: 0,
            width: '360px',
            maxWidth: '90vw',
            background: 'var(--surface, #ffffff)',
            border: '1px solid var(--border, #e2e8f0)',
            borderRadius: 'var(--radius-lg, 0.75rem)',
            boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)',
            zIndex: 1000,
            overflow: 'hidden'
          }}
        >
          {/* Header */}
          <div
            style={{
              padding: '0.875rem 1rem',
              borderBottom: '1px solid var(--border, #e2e8f0)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              background: 'var(--background, #f8fafc)'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span style={{ fontWeight: '700', fontSize: 'var(--font-size-sm, 0.875rem)', color: 'var(--text-primary)' }}>
                Notifications
              </span>
              {unreadCount > 0 && (
                <span
                  style={{
                    background: 'rgba(37, 99, 235, 0.15)',
                    color: 'var(--primary, #2563eb)',
                    fontSize: '11px',
                    fontWeight: '700',
                    padding: '0.125rem 0.5rem',
                    borderRadius: '9999px'
                  }}
                >
                  {unreadCount} new
                </span>
              )}
            </div>

            {unreadCount > 0 && (
              <button
                type="button"
                className="btn btn-ghost btn-xs"
                onClick={handleMarkAllAsRead}
                style={{ fontSize: '11px', color: 'var(--primary, #2563eb)', padding: '0.2rem 0.4rem' }}
              >
                Mark all as read
              </button>
            )}
          </div>

          {/* List */}
          <div style={{ maxHeight: '340px', overflowY: 'auto' }}>
            {loading ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                <div className="spinner spinner-sm" style={{ marginRight: '0.5rem' }}></div>
                Loading notifications...
              </div>
            ) : notifications.length === 0 ? (
              <div style={{ padding: '2rem 1rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                <p style={{ margin: 0, fontSize: 'var(--font-size-sm, 0.875rem)' }}>No notifications yet.</p>
              </div>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.id}
                  style={{
                    padding: '0.875rem 1rem',
                    borderBottom: '1px solid var(--border, #f1f5f9)',
                    backgroundColor: n.read ? 'transparent' : 'rgba(37, 99, 235, 0.04)',
                    transition: 'background-color 0.15s ease',
                    cursor: 'pointer'
                  }}
                  onClick={() => {
                    if (!n.read) {
                      notificationService.markAsRead(n.id);
                      setNotifications((prev) =>
                        prev.map((item) => (item.id === n.id ? { ...item, read: true } : item))
                      );
                      setUnreadCount((prev) => Math.max(0, prev - 1));
                    }
                    setIsOpen(false);
                    navigate('/notifications');
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '0.5rem', marginBottom: '0.25rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                      {!n.read && (
                        <span
                          style={{
                            width: '6px',
                            height: '6px',
                            borderRadius: '50%',
                            backgroundColor: 'var(--primary, #2563eb)',
                            flexShrink: 0
                          }}
                        />
                      )}
                      <span
                        style={{
                          fontWeight: n.read ? '600' : '700',
                          fontSize: 'var(--font-size-xs, 0.8125rem)',
                          color: 'var(--text-primary)'
                        }}
                      >
                        {n.title}
                      </span>
                    </div>

                    <span style={{ fontSize: '11px', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                      {formatTime(n.createdAt)}
                    </span>
                  </div>

                  <p
                    style={{
                      margin: 0,
                      fontSize: 'var(--font-size-xs, 0.75rem)',
                      color: 'var(--text-secondary)',
                      lineHeight: '1.4',
                      wordBreak: 'break-word'
                    }}
                  >
                    {n.message}
                  </p>

                  {!n.read && (
                    <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '0.375rem' }}>
                      <button
                        type="button"
                        style={{
                          background: 'none',
                          border: 'none',
                          padding: 0,
                          fontSize: '11px',
                          color: 'var(--primary, #2563eb)',
                          cursor: 'pointer',
                          fontWeight: '600'
                        }}
                        onClick={(e) => handleMarkAsRead(n.id, e)}
                      >
                        Mark as read
                      </button>
                    </div>
                  )}
                </div>
              ))
            )}
          </div>

          {/* Footer */}
          <div
            style={{
              padding: '0.75rem',
              borderTop: '1px solid var(--border, #e2e8f0)',
              background: 'var(--background, #f8fafc)',
              textAlign: 'center'
            }}
          >
            <Link
              to="/notifications"
              className="btn btn-ghost btn-sm"
              style={{ width: '100%', fontSize: 'var(--font-size-xs, 0.8125rem)', color: 'var(--primary, #2563eb)', fontWeight: '600' }}
              onClick={() => setIsOpen(false)}
            >
              View all notifications →
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}
