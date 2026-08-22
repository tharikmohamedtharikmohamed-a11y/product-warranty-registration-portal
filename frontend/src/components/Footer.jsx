import React from 'react';
import { Link } from 'react-router-dom';

/**
 * Global Footer Component.
 * Brand: WarrantyHub
 */
export default function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="footer" role="contentinfo">
      <div className="container">
        <div className="footer-grid">
          <div className="footer-brand">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
              <div className="brand-icon" style={{ width: '28px', height: '28px' }} aria-hidden="true">
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                  <path d="m9 12 2 2 4-4" />
                </svg>
              </div>
              <span className="brand-name" style={{ fontSize: '1.25rem' }}>
                WarrantyHub
              </span>
            </div>
            <p>
              Your Warranties. Organized. Protected. Always Accessible.
              Digital warranty tracking and cloud invoice vault for smart consumers.
            </p>
          </div>

          <div>
            <h4 className="footer-heading">Platform</h4>
            <ul className="footer-links">
              <li>
                <a href="#features">Features</a>
              </li>
              <li>
                <Link to="/register">Create Account</Link>
              </li>
              <li>
                <Link to="/login">Customer Login</Link>
              </li>
              <li>
                <Link to="/forgot-password">Forgot Password</Link>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="footer-heading">System</h4>
            <ul className="footer-links">
              <li>
                <a href="http://localhost:8080/api/health" target="_blank" rel="noopener noreferrer">
                  Backend API Health
                </a>
              </li>
              <li>
                <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-secondary)' }}>
                  Spring Boot 3.2 + React 18
                </span>
              </li>
              <li>
                <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-secondary)' }}>
                  Supabase PostgreSQL
                </span>
              </li>
            </ul>
          </div>
        </div>

        <div className="footer-bottom">
          <div>
            &copy; {currentYear} WarrantyHub. All rights reserved.
          </div>
          <div>
            Phase 5 — Frontend Initial Setup
          </div>
        </div>
      </div>
    </footer>
  );
}
