# WarrantyHub — Frontend Product Management

## 📌 Module Overview

The **Frontend Product Management Module** provides a user interface for customers to view, register, edit, and delete their registered products. The interface communicates directly with the Spring Boot backend (`/api/products`) using Axios and automatically attaches signed JWT Bearer tokens to all requests.

---

## 🧭 Protected Product Routes

All product routes are strictly protected by `<ProtectedRoute />`. Unauthenticated visitors attempting to reach any product route are automatically redirected to `/login` while preserving their navigation intent.

| Route | Component | Access Control | Description |
| :--- | :--- | :--- | :--- |
| `/products` | `ProductsPage.jsx` | Authenticated | Portfolio overview, search, filters, metrics strip, product cards. |
| `/products/register` | `RegisterProductPage.jsx` | Authenticated | Product registration form with real-time warranty preview. |
| `/products/:id` | `ProductDetailsPage.jsx` | Authenticated | Full product specifications and warranty timeline progress. |
| `/products/:id/edit` | `EditProductPage.jsx` | Authenticated | Product edit form with dynamic warranty recalculation preview. |

---

## 🛠️ Frontend Service (`productService.js`)

The `productService` module centralizes all REST interactions with `/api/products`:

```javascript
import api from './api';

export const productService = {
  // Fetch all products registered by the current user
  async getProducts() {
    const response = await api.get('/api/products');
    return response.data;
  },

  // Fetch a single product by UUID
  async getProductById(id) {
    const response = await api.get(`/api/products/${id}`);
    return response.data;
  },

  // Alias for getProductById
  async getProduct(id) {
    return this.getProductById(id);
  },

  // Register a new product
  async createProduct(productData) {
    const response = await api.post('/api/products', productData);
    return response.data;
  },

  // Update existing product
  async updateProduct(id, productData) {
    const response = await api.put(`/api/products/${id}`, productData);
    return response.data;
  },

  // Delete product and cascade warranty
  async deleteProduct(id) {
    await api.delete(`/api/products/${id}`);
  }
};

export default productService;
```

---

## 🖥️ Page Architectures & Features

### 1. Products Page (`ProductsPage.jsx`)
- **Metrics Strip:** Real-time counters showing Total Products, Active Warranties, Expiring Soon, and Expired items.
- **Search & Filters:** Search by product name, model number, brand, or serial number; filter by category and warranty status.
- **Card Grid:** Displays product metadata, serial tag, purchase date, warranty status badge (`ACTIVE`, `EXPIRING_SOON`, `EXPIRED`), and coverage countdown.
- **Empty States:** Custom empty state illustration for first-time users ("No products registered yet") and filtered queries ("No matching products found").
- **Delete Confirmation Modal:** Accessible confirmation dialog before sending `DELETE /api/products/{id}`.

### 2. Register Product Page (`RegisterProductPage.jsx`)
- **Validated Input Fields:**
  - Product Name (`text`, required, max 255)
  - Category (`select`, required)
  - Brand (`text`, required, max 100)
  - Model Number (`text`, required, max 100)
  - Serial Number (`text`, required, max 150)
  - Purchase Date (`date`, required, max today)
  - Seller Name (`text`, required, max 255)
  - Purchase Price (`number`, required, min 0.00)
  - Warranty Duration (`number`, required, min 1 month)
  - Notes / Description (`textarea`, optional)
- **Live Warranty Preview:** Side-by-side card calculating the calculated expiry date, remaining days, and status in real-time as the user types.
- **Duplicate Serial Validation:** Surfaces HTTP 409 Conflict messages directly under the serial number field.

### 3. Product Details Page (`ProductDetailsPage.jsx`)
- **Comprehensive Specifications:** Structured breakdown of brand, category, model, serial (with 1-click clipboard copy), retailer, purchase date, price, and registration timestamp.
- **Warranty Monitoring:** Real-time coverage progress bar showing percentage of warranty period elapsed, days remaining, and status indicator.
- **Customer Scoping:** Displays safe 404 alert if product ID does not belong to the user.

### 4. Edit Product Page (`EditProductPage.jsx`)
- **Form Prepopulation:** Fetches existing product data on load.
- **Dynamic Warranty Recalibration:** Displays preview of new expiry date when purchase date or warranty duration is modified.
- **Navigation:** Redirects back to `/products/:id` upon successful save.

---

## 🎨 UI/UX Design System

The product pages use WarrantyHub's modern, accessible CSS design tokens:
- **Card Elevation & Depth:** Consistent shadows, dark-mode cards with subtle border contrasts (`var(--surface)`, `var(--border)`).
- **Status Badges:** Tailored color tokens for Active (`#064e3b` / `#6ee7b7`), Expiring Soon (`#78350f` / `#fde68a`), and Expired (`#881337` / `#fca5a5`).
- **Loading & Error States:** Clear spinners (`spinner-lg`), accessible alert banners, and disabled button states during network operations.
