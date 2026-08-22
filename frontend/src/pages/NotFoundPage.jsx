import React from 'react';
import { Link } from 'react-router-dom';

/**
 * 404 Not Found Page.
 */
export default function NotFoundPage() {
  return (
    <div className="not-found-container">
      <div>
        <div className="not-found-code">404</div>
        <h1 className="not-found-title">Page Not Found</h1>
        <p className="not-found-text">
          The page you are looking for does not exist, has been removed, or is temporarily unavailable.
        </p>
        <Link to="/" className="btn btn-primary btn-md">
          Return to Home
        </Link>
      </div>
    </div>
  );
}
