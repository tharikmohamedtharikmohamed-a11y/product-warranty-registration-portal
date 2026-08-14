import React from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to={isAuthenticated() ? '/dashboard' : '/'} className="navbar-brand">
          <span>🛡️</span> Product Warranty Portal
        </Link>

        <ul className="navbar-links">
          {isAuthenticated() ? (
            user?.role === 'ADMIN' ? (
              <>
                <li>
                  <NavLink to="/admin" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} end>
                    Dashboard
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/admin/claims" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    Manage Claims
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/admin/customers" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    Customers
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/admin/products" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    Products
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/admin/warranties" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    Warranties
                  </NavLink>
                </li>
                <li>
                  <div className="user-badge">
                    <span>👑 {user?.name || user?.email}</span>
                    <span className="role-pill">ADMIN</span>
                  </div>
                </li>
                <li>
                  <button onClick={handleLogout} className="btn btn-secondary btn-inline btn-sm">
                    Logout
                  </button>
                </li>
              </>
            ) : (
              <>
                <li>
                  <NavLink to="/dashboard" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    Dashboard
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/products" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    My Products
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/warranties" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    My Warranties
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/claims" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    My Claims
                  </NavLink>
                </li>
                <li>
                  <div className="user-badge">
                    <span>👤 {user?.name || user?.email}</span>
                    {user?.role && <span className="role-pill">{user.role}</span>}
                  </div>
                </li>
                <li>
                  <button onClick={handleLogout} className="btn btn-secondary btn-inline btn-sm">
                    Logout
                  </button>
                </li>
              </>
            )
          ) : (
            <>
              <li>
                <NavLink to="/" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                  Home
                </NavLink>
              </li>
              <li>
                <NavLink to="/login" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                  Login
                </NavLink>
              </li>
              <li>
                <Link to="/register" className="btn btn-primary btn-inline btn-sm">
                  Register
                </Link>
              </li>
            </>
          )}
        </ul>
      </div>
    </nav>
  );
};
