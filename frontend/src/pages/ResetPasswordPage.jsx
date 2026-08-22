import React from 'react';
import { Link } from 'react-router-dom';

/**
 * Reset Password Page (Phase 5 Placeholder).
 */
export default function ResetPasswordPage() {
  return (
    <div className="auth-page-container">
      <div className="auth-card">
        <div className="auth-card-icon" aria-hidden="true">
          <svg
            width="28"
            height="28"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
            <path d="M7 11V7a5 5 0 0 1 10 0v4" />
          </svg>
        </div>

        <h1 className="auth-card-title">Reset Password</h1>
        <p className="auth-card-subtitle">
          Create a new secure password for your WarrantyHub account.
        </p>

        <div className="phase-notice">
          <strong>Phase 5 Placeholder</strong>
          <p style={{ marginTop: '0.25rem' }}>
            Password reset submission will be activated in upcoming phases.
          </p>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          <button type="button" className="btn btn-primary btn-md" disabled>
            Save New Password (Upcoming)
          </button>
          <Link to="/login" className="btn btn-secondary btn-md">
            Back to Login
          </Link>
        </div>
      </div>
    </div>
  );
}
