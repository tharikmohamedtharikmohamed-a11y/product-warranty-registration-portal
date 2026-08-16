import React from 'react';
import { Link } from 'react-router-dom';

export const UnauthorizedPage = () => {
  return (
    <div className="card" style={{ textAlign: 'center' }}>
      <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🔒</div>
      <h2 className="card-title">Access Denied</h2>
      <p className="card-subtitle">
        You do not have permission to view this page or your authorization has expired.
      </p>

      <div style={{ marginTop: '2rem', display: 'flex', gap: '1rem', justifyContent: 'center' }}>
        <Link to="/login" className="btn btn-primary btn-inline">
          Log In
        </Link>
        <Link to="/" className="btn btn-secondary btn-inline">
          Back to Home
        </Link>
      </div>
    </div>
  );
};
