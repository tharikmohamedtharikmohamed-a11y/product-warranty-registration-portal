import React from 'react';

/**
 * Reusable ErrorMessage alert component.
 *
 * @param {Object} props
 * @param {string} props.message
 */
export default function ErrorMessage({ message }) {
  if (!message) return null;

  return (
    <div className="error-alert" role="alert">
      <svg
        style={{ width: '20px', height: '20px', flexShrink: 0 }}
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="8" x2="12" y2="12" />
        <line x1="12" y1="16" x2="12.01" y2="16" />
      </svg>
      <div>{message}</div>
    </div>
  );
}
