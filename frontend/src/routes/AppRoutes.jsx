import React from 'react';
import { Routes, Route } from 'react-router-dom';
import PublicLayout from '../layouts/PublicLayout';
import LandingPage from '../pages/LandingPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import ForgotPasswordPage from '../pages/ForgotPasswordPage';
import ResetPasswordPage from '../pages/ResetPasswordPage';
import DashboardPage from '../pages/DashboardPage';
import ProductsPage from '../pages/ProductsPage';
import RegisterProductPage from '../pages/RegisterProductPage';
import ProductDetailsPage from '../pages/ProductDetailsPage';
import EditProductPage from '../pages/EditProductPage';
import WarrantiesPage from '../pages/WarrantiesPage';
import WarrantyDetailsPage from '../pages/WarrantyDetailsPage';
import InvoicesPage from '../pages/InvoicesPage';
import ClaimsPage from '../pages/ClaimsPage';
import ClaimDetailsPage from '../pages/ClaimDetailsPage';
import NotFoundPage from '../pages/NotFoundPage';
import ProtectedRoute from './ProtectedRoute';
import PublicRoute from './PublicRoute';

/**
 * Main Application Routing.
 * Phase 10 — Warranty Claims
 */
export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        {/* Unrestricted Public Routes */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />

        {/* Public Authentication Routes (Guarded: redirect to /dashboard if logged in) */}
        <Route element={<PublicRoute />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        {/* Protected Routes (Guarded: redirect to /login if unauthenticated) */}
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/products/register" element={<RegisterProductPage />} />
          <Route path="/products/:id" element={<ProductDetailsPage />} />
          <Route path="/products/:id/edit" element={<EditProductPage />} />
          <Route path="/warranties" element={<WarrantiesPage />} />
          <Route path="/warranties/:id" element={<WarrantyDetailsPage />} />
          <Route path="/invoices" element={<InvoicesPage />} />
          <Route path="/claims" element={<ClaimsPage />} />
          <Route path="/claims/:id" element={<ClaimDetailsPage />} />
        </Route>

        {/* 404 Catch-All Route */}
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
