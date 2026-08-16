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
import { ProductDetails } from './pages/ProductDetails';
import { MyWarranties } from './pages/MyWarranties';
import { WarrantyDetails } from './pages/WarrantyDetails';

const HomeOrDashboardRedirect = () => {
  const { isAuthenticated, loading } = useAuth();
  if (loading) return null;
  if (isAuthenticated()) {
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

            {/* Phase 10 Protected Customer Routes */}
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
