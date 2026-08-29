import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

/**
 * Global Navigation Header.
 * Brand: WarrantyHub
 * Tagline: "Your Warranties. Organized. Protected. Always Accessible."
 * Phase 6 — Frontend Authentication & Route Guards
 */
export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar" role="banner">
      <div className="container navbar-container">
        <Link to="/" className="navbar-brand" aria-label="WarrantyHub Home">
          <div className="brand-icon" aria-hidden="true">
            <svg
              width="20"
              height="20"
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
          <span className="brand-name">WarrantyHub</span>
          <span className="brand-tagline-badge">Portal</span>
        </Link>

        <nav aria-label="Main Navigation">
          <ul className="navbar-nav">
            <li>
              <Link to="/" className="nav-link">
                Home
              </Link>
            </li>
            {isAuthenticated ? (
              <>
                <li>
                  <Link to="/dashboard" className="nav-link">
                    Dashboard
                  </Link>
                </li>
                <li>
                  <Link to="/products" className="nav-link">
                    My Products
                  </Link>
                </li>
                <li>
                  <Link to="/warranties" className="nav-link">
                    My Warranties
                  </Link>
                </li>
                <li>
                  <Link to="/invoices" className="nav-link">
                    My Invoices
                  </Link>
                </li>
                <li>
                  <Link to="/claims" className="nav-link">
                    My Claims
                  </Link>
                </li>
                <li>
                  <Link to="/products/register" className="nav-link">
                    Register Product
                  </Link>
                </li>
              </>
            ) : (
              <>
                <li>
                  <a href="/#features" className="nav-link">
                    Features
                  </a>
                </li>
                <li>
                  <a href="/#how-it-works" className="nav-link">
                    How It Works
                  </a>
                </li>
              </>
            )}
          </ul>
        </nav>

        <div className="navbar-actions">
          {isAuthenticated ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: '500', color: 'var(--text-secondary)' }}>
                {user?.name}
              </span>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={handleLogout}
              >
                Logout
              </button>
            </div>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost btn-sm">
                Login
              </Link>
              <Link to="/register" className="btn btn-primary btn-sm">
                Get Started
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
