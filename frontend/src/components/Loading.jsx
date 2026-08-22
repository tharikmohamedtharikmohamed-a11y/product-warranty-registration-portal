import React from 'react';

/**
 * Reusable Loading spinner component.
 *
 * @param {Object} props
 * @param {'sm' | 'md' | 'lg'} [props.size='md']
 * @param {string} [props.text='Loading...']
 */
export default function Loading({ size = 'md', text = 'Loading...' }) {
  const spinnerSizeClass = size === 'sm' ? 'spinner-sm' : size === 'lg' ? 'spinner-lg' : '';

  return (
    <div className="loading-container" role="status" aria-live="polite">
      <div className={`spinner ${spinnerSizeClass}`} aria-hidden="true" />
      {text && <span className="loading-text">{text}</span>}
      <span className="sr-only" style={{ position: 'absolute', width: '1px', height: '1px', padding: 0, margin: '-1px', overflow: 'hidden', clip: 'rect(0, 0, 0, 0)', whiteSpace: 'nowrap', border: 0 }}>
        {text}
      </span>
    </div>
  );
}
