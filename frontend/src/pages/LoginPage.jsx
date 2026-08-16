import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [credentials, setCredentials] = useState({
    email: '',
    password: '',
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const infoMsg = location.state?.message || '';

  const handleChange = (e) => {
    const { name, value } = e.target;
    setCredentials((prev) => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: '' }));
    }
    setServerError('');
  };

  const validate = () => {
    const errors = {};
    if (!credentials.email.trim()) {
      errors.email = 'Email address is required';
    } else if (!/\S+@\S+\.\S+/.test(credentials.email)) {
      errors.email = 'Invalid email address format';
    }

    if (!credentials.password) {
      errors.password = 'Password is required';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');

    if (!validate()) return;

    setIsSubmitting(true);

    try {
      await login(credentials);
      navigate('/');
    } catch (err) {
      setServerError(err.message || 'Invalid email or password');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="card">
      <h2 className="card-title">Welcome Back</h2>
      <p className="card-subtitle">Log in to access your warranty portal</p>

      {infoMsg && <div className="alert alert-success">{infoMsg}</div>}
      {serverError && <div className="alert alert-error">{serverError}</div>}

      <form onSubmit={handleSubmit} noValidate>
        <div className="form-group">
          <label className="form-label" htmlFor="login-email">Email Address *</label>
          <input
            id="login-email"
            type="email"
            name="email"
            className="form-input"
            value={credentials.email}
            onChange={handleChange}
            placeholder="john@example.com"
            disabled={isSubmitting}
          />
          {fieldErrors.email && <div className="form-error">{fieldErrors.email}</div>}
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="login-password">Password *</label>
          <input
            id="login-password"
            type="password"
            name="password"
            className="form-input"
            value={credentials.password}
            onChange={handleChange}
            placeholder="Enter your password"
            disabled={isSubmitting}
          />
          {fieldErrors.password && <div className="form-error">{fieldErrors.password}</div>}
        </div>

        <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
          {isSubmitting ? (
            <>
              <span className="loading-spinner"></span>
              Logging in...
            </>
          ) : (
            'Log In'
          )}
        </button>
      </form>

      <p style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
        Don't have an account? <Link to="/register" style={{ color: 'var(--primary)', fontWeight: 600 }}>Register Now</Link>
      </p>
    </div>
  );
};
