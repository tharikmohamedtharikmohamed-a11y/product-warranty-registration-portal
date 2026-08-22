import React from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';

/**
 * Public Layout wrapper.
 * Renders header Navbar, main dynamic route content, and Footer.
 */
export default function PublicLayout() {
  return (
    <div className="app-container">
      <Navbar />
      <main className="main-content" id="main-content" role="main">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}
