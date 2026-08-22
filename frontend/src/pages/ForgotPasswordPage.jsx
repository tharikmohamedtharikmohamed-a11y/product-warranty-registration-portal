import React from 'react';
import { Link } from 'react-router-dom';

/**
 * Forgot Password Page (Phase 5 Placeholder).
 */
export default function ForgotPasswordPage() {
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
            <circle cx="12" cy="12" r="10" />
            <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
            <line x1="12" y1="17" x2="12.01" y2="17" />
          </svg>
        </div>

        <h1 className="auth-card-title">Forgot Password</h1>
        <p className="auth-card-subtitle">
          Enter your registered email address to receive password reset instructions.
        </p>

        <div className="phase-notice">
          <strong>Phase 5 Placeholder</strong>
          <p style={{ marginTop: '0.25rem' }}>
            Password recovery workflows will be activated in upcoming phases.
          </p>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          <button type="button" className="btn btn-primary btn-md" disabled>
            Send Reset Link (Upcoming)
          </button>
          <Link to="/login" className="btn btn-secondary btn-md">
            Back to Login
          </Link>
        </div>

        <div className="auth-links">
          <div>
            Remembered your credentials? <Link to="/login">Sign in</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
