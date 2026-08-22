import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import ErrorMessage from '../components/ErrorMessage';

/**
 * Customer Registration Page.
 * Registers a new user via POST /api/auth/register and redirects to /login.
 * Phase 6 — Frontend Authentication & Route Guards
 */
export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const validateForm = () => {
    const errors = {};
    const nameTrimmed = formData.name.trim();
    const emailTrimmed = formData.email.trim();

    if (!nameTrimmed) {
      errors.name = 'Full name is required';
    }

    if (!emailTrimmed) {
      errors.email = 'Email address is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailTrimmed)) {
      errors.email = 'Please enter a valid email address';
    }

    if (!formData.password) {
      errors.password = 'Password is required';
    } else if (formData.password.length < 8) {
      errors.password = 'Password must be at least 8 characters long';
    }

    if (!formData.confirmPassword) {
      errors.confirmPassword = 'Password confirmation is required';
    } else if (formData.password !== formData.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));

    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({
        ...prev,
        [name]: null
      }));
    }
    if (errorMessage) {
      setErrorMessage('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      await register(formData.name.trim(), formData.email.trim(), formData.password);
      setSuccessMessage('Registration successful! Redirecting to login...');
      setTimeout(() => {
        navigate('/login');
      }, 1500);
    } catch (error) {
      if (error.response) {
        if (error.response.status === 409) {
          setErrorMessage('Email is already registered.');
        } else if (error.response.data && error.response.data.message) {
          setErrorMessage(error.response.data.message);
        } else {
          setErrorMessage('Registration failed. Please check your details and try again.');
        }
      } else {
        setErrorMessage('Unable to connect to the server. Please try again.');
      }
      setIsSubmitting(false);
    }
  };

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
            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
            <circle cx="8.5" cy="7" r="4" />
            <line x1="20" y1="8" x2="20" y2="14" />
            <line x1="23" y1="11" x2="17" y2="11" />
          </svg>
        </div>

        <h1 className="auth-card-title">Create Account</h1>
        <p className="auth-card-subtitle">
          Join WarrantyHub to digitize, organize, and safeguard all your product warranties.
        </p>

        {errorMessage && (
          <div style={{ marginBottom: '1.25rem' }}>
            <ErrorMessage message={errorMessage} />
          </div>
        )}

        {successMessage && (
          <div
            className="success-alert"
            role="alert"
            style={{
              padding: '1rem',
              borderRadius: 'var(--radius-md)',
              backgroundColor: 'var(--success-bg)',
              border: '1px solid #a7f3d0',
              color: 'var(--success-text)',
              fontSize: 'var(--font-size-sm)',
              marginBottom: '1.25rem',
              textAlign: 'center',
              fontWeight: '500'
            }}
          >
            {successMessage}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate style={{ textAlign: 'left' }}>
          {/* Full Name */}
          <div className="form-group" style={{ marginBottom: '1.25rem' }}>
            <label htmlFor="reg-name" className="form-label">
              Full Name
            </label>
            <input
              id="reg-name"
              name="name"
              type="text"
              autoComplete="name"
              value={formData.name}
              onChange={handleChange}
              disabled={isSubmitting}
              className={`form-input ${fieldErrors.name ? 'input-error' : ''}`}
              placeholder="e.g. John Doe"
              required
            />
            {fieldErrors.name && (
              <span className="field-error-text" role="alert">
                {fieldErrors.name}
              </span>
            )}
          </div>

          {/* Email Address */}
          <div className="form-group" style={{ marginBottom: '1.25rem' }}>
            <label htmlFor="reg-email" className="form-label">
              Email Address
            </label>
            <input
              id="reg-email"
              name="email"
              type="email"
              autoComplete="email"
              value={formData.email}
              onChange={handleChange}
              disabled={isSubmitting}
              className={`form-input ${fieldErrors.email ? 'input-error' : ''}`}
              placeholder="you@example.com"
              required
            />
            {fieldErrors.email && (
              <span className="field-error-text" role="alert">
                {fieldErrors.email}
              </span>
            )}
          </div>

          {/* Password */}
          <div className="form-group" style={{ marginBottom: '1.25rem' }}>
            <label htmlFor="reg-password" className="form-label">
              Password (min. 8 characters)
            </label>
            <div style={{ position: 'relative' }}>
              <input
                id="reg-password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                value={formData.password}
                onChange={handleChange}
                disabled={isSubmitting}
                className={`form-input ${fieldErrors.password ? 'input-error' : ''}`}
                placeholder="At least 8 characters"
                required
              />
              <button
                type="button"
                className="password-toggle-btn"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
                tabIndex={-1}
              >
                {showPassword ? (
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                    <line x1="1" y1="1" x2="23" y2="23" />
                  </svg>
                ) : (
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                )}
              </button>
            </div>
            {fieldErrors.password && (
              <span className="field-error-text" role="alert">
                {fieldErrors.password}
              </span>
            )}
          </div>

          {/* Confirm Password */}
          <div className="form-group" style={{ marginBottom: '1.75rem' }}>
            <label htmlFor="reg-confirm-password" className="form-label">
              Confirm Password
            </label>
            <input
              id="reg-confirm-password"
              name="confirmPassword"
              type={showPassword ? 'text' : 'password'}
              autoComplete="new-password"
              value={formData.confirmPassword}
              onChange={handleChange}
              disabled={isSubmitting}
              className={`form-input ${fieldErrors.confirmPassword ? 'input-error' : ''}`}
              placeholder="Re-enter password"
              required
            />
            {fieldErrors.confirmPassword && (
              <span className="field-error-text" role="alert">
                {fieldErrors.confirmPassword}
              </span>
            )}
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            className="btn btn-primary btn-md"
            style={{ width: '100%', padding: '0.75rem', fontWeight: '600' }}
            disabled={isSubmitting || !!successMessage}
          >
            {isSubmitting ? (
              <>
                <span className="spinner spinner-sm" aria-hidden="true" />
                <span>Creating account...</span>
              </>
            ) : (
              'Create Account'
            )}
          </button>
        </form>

        <div className="auth-links" style={{ marginTop: '1.75rem' }}>
          <div>
            Already have an account?{' '}
            <Link to="/login" style={{ fontWeight: '600' }}>
              Sign in here
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
