import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { checkBackendHealth } from '../services/healthService';

/**
 * Landing Page for WarrantyHub.
 * Brand: WarrantyHub
 * Tagline: "Your Warranties. Organized. Protected. Always Accessible."
 */
export default function LandingPage() {
  const [backendStatus, setBackendStatus] = useState('checking');

  useEffect(() => {
    let isMounted = true;
    checkBackendHealth().then((result) => {
      if (isMounted) {
        setBackendStatus(result.success ? 'online' : 'offline');
      }
    });
    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <div>
      {/* Hero Section */}
      <section className="hero-section" aria-labelledby="hero-heading">
        <div className="container">
          <div className="hero-badge">
            <svg
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              <path d="m9 12 2 2 4-4" />
            </svg>
            <span>WarrantyHub Digital Protection</span>
          </div>

          <h1 id="hero-heading" className="hero-title">
            Your Warranties.{' '}
            <span className="hero-title-highlight">Organized. Protected.</span>{' '}
            Always Accessible.
          </h1>

          <p className="hero-subtitle">
            Say goodbye to faded paper receipts, forgotten expiry dates, and stressful warranty claims.
            WarrantyHub digitizes your purchases and keeps your consumer rights secured in one central vault.
          </p>

          <div className="hero-cta">
            <Link to="/register" className="btn btn-primary btn-lg">
              Get Started Free
            </Link>
            <Link to="/login" className="btn btn-secondary btn-lg">
              Sign In
            </Link>
          </div>

          <div className="hero-health-status">
            <span
              className={`status-indicator ${backendStatus === 'online' ? 'online' : ''}`}
              aria-hidden="true"
            />
            <span>
              Backend Service:{' '}
              {backendStatus === 'online'
                ? 'Connected (HTTP 200)'
                : backendStatus === 'checking'
                ? 'Checking API...'
                : 'Offline (Start Spring Boot backend)'}
            </span>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="features-section" aria-labelledby="features-heading">
        <div className="container">
          <div className="section-header">
            <div className="section-tag">Core Capabilities</div>
            <h2 id="features-heading" className="section-title">
              Complete Warranty Lifecycle Management
            </h2>
            <p className="section-description">
              Engineered to protect your product investments with automated intelligence and secure cloud vaults.
            </p>
          </div>

          <div className="features-grid">
            {/* Feature 1 */}
            <article className="feature-card">
              <div className="feature-icon-wrapper" aria-hidden="true">
                <svg
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
                  <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
                </svg>
              </div>
              <h3 className="feature-title">Product Registration</h3>
              <p className="feature-description">
                Catalog purchased products with model specifications, serial numbers, purchase dates,
                prices, and retailer information in seconds.
              </p>
            </article>

            {/* Feature 2 */}
            <article className="feature-card">
              <div className="feature-icon-wrapper" aria-hidden="true">
                <svg
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
              </div>
              <h3 className="feature-title">Automated Warranty Tracking</h3>
              <p className="feature-description">
                Automatic calculation of expiration dates and countdowns. Instant classification into
                Active, Expiring Soon, or Expired states.
              </p>
            </article>

            {/* Feature 3 */}
            <article className="feature-card">
              <div className="feature-icon-wrapper" aria-hidden="true">
                <svg
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                  <line x1="16" y1="13" x2="8" y2="13" />
                  <line x1="16" y1="17" x2="8" y2="17" />
                  <polyline points="10 9 9 9 8 9" />
                </svg>
              </div>
              <h3 className="feature-title">Digital Invoice Vault</h3>
              <p className="feature-description">
                Upload receipts, invoices, and proofs of purchase (PDF, PNG, JPG). Stored securely in
                encrypted cloud storage for immediate access whenever needed.
              </p>
            </article>

            {/* Feature 4 */}
            <article className="feature-card">
              <div className="feature-icon-wrapper" aria-hidden="true">
                <svg
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                  <path d="m9 12 2 2 4-4" />
                </svg>
              </div>
              <h3 className="feature-title">Warranty Claims</h3>
              <p className="feature-description">
                Submit formal warranty repair or replacement claims directly against registered products.
                Track review status transparently from submission to resolution.
              </p>
            </article>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section id="how-it-works" style={{ padding: '4.5rem 0', backgroundColor: 'var(--background)' }}>
        <div className="container">
          <div className="section-header">
            <div className="section-tag">Simple Process</div>
            <h2 className="section-title">How WarrantyHub Works</h2>
            <p className="section-description">
              Three simple steps to keep every warranty and invoice organized for life.
            </p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '2rem' }}>
            <div style={{ background: 'var(--surface)', padding: '2rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)' }}>
              <div style={{ fontSize: 'var(--font-size-2xl)', fontWeight: '800', color: 'var(--primary)', marginBottom: '0.75rem' }}>
                01
              </div>
              <h3 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '600', marginBottom: '0.5rem' }}>
                Register Your Product
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)', lineHeight: '1.6' }}>
                Enter your purchased item details, serial number, and upload your digital receipt or photo of the invoice.
              </p>
            </div>

            <div style={{ background: 'var(--surface)', padding: '2rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)' }}>
              <div style={{ fontSize: 'var(--font-size-2xl)', fontWeight: '800', color: 'var(--primary)', marginBottom: '0.75rem' }}>
                02
              </div>
              <h3 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '600', marginBottom: '0.5rem' }}>
                Automated Tracking
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)', lineHeight: '1.6' }}>
                WarrantyHub computes your coverage window and actively tracks expiration dates with timely reminders.
              </p>
            </div>

            <div style={{ background: 'var(--surface)', padding: '2rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)' }}>
              <div style={{ fontSize: 'var(--font-size-2xl)', fontWeight: '800', color: 'var(--primary)', marginBottom: '0.75rem' }}>
                03
              </div>
              <h3 style={{ fontSize: 'var(--font-size-lg)', fontWeight: '600', marginBottom: '0.5rem' }}>
                Effortless Claims
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)', lineHeight: '1.6' }}>
                When an issue arises, pull up original receipts instantly or submit a structured claim with complete documentation.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Banner */}
      <section className="cta-banner" aria-labelledby="cta-heading">
        <div className="container">
          <h2 id="cta-heading">Ready to Secure Your Product Warranties?</h2>
          <p>
            Join WarrantyHub today. Organize your receipts, safeguard your warranties, and never miss a claim deadline again.
          </p>
          <Link to="/register" className="btn btn-cta btn-lg">
            Create Your Free Account
          </Link>
        </div>
      </section>
    </div>
  );
}
