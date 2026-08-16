import React from 'react';
import { useAuth } from '../context/AuthContext';

export const DashboardPlaceholder = () => {
  const { user } = useAuth();

  return (
    <div className="card" style={{ maxWidth: '600px' }}>
      <h2 className="card-title">Protected Area (Authentication Verified)</h2>
      <p className="card-subtitle">
        You are successfully authenticated via Spring Boot JWT!
      </p>

      <div style={{ background: 'var(--bg-light)', padding: '1.25rem', borderRadius: 'var(--radius)', marginTop: '1rem', border: '1px solid var(--border)' }}>
        <h4 style={{ marginBottom: '0.5rem', color: 'var(--primary)' }}>Authenticated User Information:</h4>
        <ul style={{ listStyle: 'none', lineHeight: '1.8', fontSize: '0.95rem' }}>
          <li><strong>ID:</strong> {user?.id}</li>
          <li><strong>Name:</strong> {user?.name}</li>
          <li><strong>Email:</strong> {user?.email}</li>
          <li><strong>Phone:</strong> {user?.phone || 'N/A'}</li>
          <li><strong>Role:</strong> {user?.role}</li>
        </ul>
      </div>
    </div>
  );
};
