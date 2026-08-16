import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const LandingPage = () => {
  const { isAuthenticated, user } = useAuth();

  return (
    <div className="hero">
      <h1 className="hero-title">Product Warranty Registration Portal</h1>
      <p className="hero-subtitle">
        Securely manage your product warranties, purchase invoices, and warranty claims in one place.
      </p>

      <div className="hero-actions">
        {isAuthenticated() ? (
          <Link to="/protected-test" className="btn btn-primary">
            Go to Protected Demo Area
          </Link>
        ) : (
          <>
            <Link to="/login" className="btn btn-primary">
              Login
            </Link>
            <Link to="/register" className="btn btn-secondary">
              Register
            </Link>
          </>
        )}
      </div>

      <div className="features-grid">
        <div className="feature-card">
          <span className="feature-icon">📑</span>
          <h3 className="feature-title">Warranty Registration</h3>
          <p className="feature-desc">
            Easily record product details, serial numbers, and purchase dates for seamless tracking.
          </p>
        </div>

        <div className="feature-card">
          <span className="feature-icon">☁️</span>
          <h3 className="feature-title">Cloud Invoice Storage</h3>
          <p className="feature-desc">
            Safely store proof-of-purchase documents with encrypted cloud storage access.
          </p>
        </div>

        <div className="feature-card">
          <span className="feature-icon">🛠️</span>
          <h3 className="feature-title">Claim Management</h3>
          <p className="feature-desc">
            Submit warranty service claims and track approval status in real-time.
          </p>
        </div>
      </div>
    </div>
  );
};
