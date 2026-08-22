import React from 'react';

/**
 * Reusable Button component.
 *
 * @param {Object} props
 * @param {'primary' | 'secondary' | 'outline' | 'ghost'} [props.variant='primary']
 * @param {'sm' | 'md' | 'lg'} [props.size='md']
 * @param {boolean} [props.isLoading=false]
 * @param {React.ReactNode} props.children
 */
export default function Button({
  children,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  disabled = false,
  className = '',
  type = 'button',
  onClick,
  ...rest
}) {
  const variantClass = `btn-${variant}`;
  const sizeClass = `btn-${size}`;
  const customClass = className ? ` ${className}` : '';

  return (
    <button
      type={type}
      disabled={disabled || isLoading}
      className={`btn ${variantClass} ${sizeClass}${customClass}`}
      onClick={onClick}
      {...rest}
    >
      {isLoading && <span className="spinner spinner-sm" aria-hidden="true" />}
      <span>{children}</span>
    </button>
  );
}
