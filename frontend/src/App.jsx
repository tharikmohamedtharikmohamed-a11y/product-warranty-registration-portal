import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';

import { LandingPage } from './pages/LandingPage';
import { RegisterPage } from './pages/RegisterPage';
import { LoginPage } from './pages/LoginPage';
import { UnauthorizedPage } from './pages/UnauthorizedPage';
import { CustomerDashboard } from './pages/CustomerDashboard';
import { MyProducts } from './pages/MyProducts';
import { RegisterProduct } from './pages/RegisterProduct';
import { EditProduct } from './pages/EditProduct';
import { ProductDetails } from './pages/ProductDetails';
import { InvoiceManagement } from './pages/InvoiceManagement';
import { MyWarranties } from './pages/MyWarranties';
import { WarrantyDetails } from './pages/WarrantyDetails';
import { MyClaims } from './pages/MyClaims';
import { SubmitClaim } from './pages/SubmitClaim';
import { ClaimDetails } from './pages/ClaimDetails';
import { AdminRoute } from './components/AdminRoute';
import { AdminDashboard } from './pages/AdminDashboard';
import { AdminClaims } from './pages/AdminClaims';
import { AdminClaimDetails } from './pages/AdminClaimDetails';
import { AdminCustomers } from './pages/AdminCustomers';
import { AdminProducts } from './pages/AdminProducts';
import { AdminWarranties } from './pages/AdminWarranties';

const HomeOrDashboardRedirect = () => {
  const { user, isAuthenticated, loading } = useAuth();
  if (loading) return null;
  if (isAuthenticated()) {
    if (user?.role === 'ADMIN') {
      return <Navigate to="/admin" replace />;
    }
    return <Navigate to="/dashboard" replace />;
  }
  return <LandingPage />;
};

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<HomeOrDashboardRedirect />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/unauthorized" element={<UnauthorizedPage />} />

            {/* Protected Customer Routes */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <CustomerDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/products"
              element={
                <ProtectedRoute>
                  <MyProducts />
                </ProtectedRoute>
              }
            />
            <Route
              path="/products/register"
              element={
                <ProtectedRoute>
                  <RegisterProduct />
                </ProtectedRoute>
              }
            />
            <Route
              path="/products/:id/edit"
              element={
                <ProtectedRoute>
                  <EditProduct />
                </ProtectedRoute>
              }
            />
            <Route
              path="/products/:id/invoices"
              element={
                <ProtectedRoute>
                  <InvoiceManagement />
                </ProtectedRoute>
              }
            />
            <Route
              path="/products/:id"
              element={
                <ProtectedRoute>
                  <ProductDetails />
                </ProtectedRoute>
              }
            />
            <Route
              path="/warranties"
              element={
                <ProtectedRoute>
                  <MyWarranties />
                </ProtectedRoute>
              }
            />
            <Route
              path="/warranties/:id"
              element={
                <ProtectedRoute>
                  <WarrantyDetails />
                </ProtectedRoute>
              }
            />
            <Route
              path="/claims"
              element={
                <ProtectedRoute>
                  <MyClaims />
                </ProtectedRoute>
              }
            />
            <Route
              path="/claims/submit"
              element={
                <ProtectedRoute>
                  <SubmitClaim />
                </ProtectedRoute>
              }
            />
            <Route
              path="/claims/:id"
              element={
                <ProtectedRoute>
                  <ClaimDetails />
                </ProtectedRoute>
              }
            />

            {/* Protected Admin Routes */}
            <Route
              path="/admin"
              element={
                <AdminRoute>
                  <AdminDashboard />
                </AdminRoute>
              }
            />
            <Route
              path="/admin/claims"
              element={
                <AdminRoute>
                  <AdminClaims />
                </AdminRoute>
              }
            />
            <Route
              path="/admin/claims/:id"
              element={
                <AdminRoute>
                  <AdminClaimDetails />
                </AdminRoute>
              }
            />
            <Route
              path="/admin/customers"
              element={
                <AdminRoute>
                  <AdminCustomers />
                </AdminRoute>
              }
            />
            <Route
              path="/admin/products"
              element={
                <AdminRoute>
                  <AdminProducts />
                </AdminRoute>
              }
            />
            <Route
              path="/admin/warranties"
              element={
                <AdminRoute>
                  <AdminWarranties />
                </AdminRoute>
              }
            />

            <Route path="*" element={<HomeOrDashboardRedirect />} />
          </Routes>
        </main>
        <footer className="footer">
          <p>© {new Date().getFullYear()} Product Warranty Registration Portal — College Capstone Project</p>
        </footer>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
