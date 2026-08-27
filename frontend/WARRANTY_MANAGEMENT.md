# WarrantyHub — Frontend Warranty Management

## 📌 Module Overview

The **Frontend Warranty Management Module** provides customers with real-time visibility into their registered warranties, proactive countdown timers, dynamic elapsed progress bars, and status categorization (`ACTIVE`, `EXPIRING_SOON`, `EXPIRED`).

---

## 🧭 Protected Routes

All warranty routes are protected by `<ProtectedRoute />` and require valid JWT authentication.

| Route | Component | Description |
| :--- | :--- | :--- |
| `/warranties` | `WarrantiesPage.jsx` | Portfolio overview with metrics strip, search, filter, status badges, and progress bars. |
| `/warranties/:id` | `WarrantyDetailsPage.jsx` | Deep-dive warranty specification and timeline view with status advisories and accessible progress bar. |

---

## 🛠️ Frontend Service (`warrantyService.js`)

Centralized REST interaction module using the shared Axios instance with automated JWT Bearer token attachment:

```javascript
import api from './api';

export const warrantyService = {
  // Fetch all warranties belonging to the current user
  async getWarranties() {
    const response = await api.get('/api/warranties');
    return response.data;
  },

  // Fetch a single warranty by ID
  async getWarranty(id) {
    const response = await api.get(`/api/warranties/${id}`);
    return response.data;
  },

  // Fetch warranty by associated product ID
  async getProductWarranty(productId) {
    const response = await api.get(`/api/products/${productId}/warranty`);
    return response.data;
  }
};

export default warrantyService;
```

---

## 🖥️ Page Components & Features

### 1. Warranty Portfolio (`WarrantiesPage.jsx`)
- **Metrics Strip:** Real-time counters showing Total Warranties, Active Coverage, Expiring Soon (30d), and Expired terms.
- **Search & Filters:** Search by product name, brand, or model number; filter by status dropdown (`ALL`, `ACTIVE`, `EXPIRING_SOON`, `EXPIRED`).
- **Warranty Cards:** Displays product title, model, start date, expiry date, duration, dynamic status badge, days remaining, and visual progress bar.
- **Empty States:** Friendly empty states for users with no warranties ("Register a Product") and query filter misses ("Reset Filters").
- **Navigation Links:** Quick links to "View Details" (`/warranties/:id`) and "View Product" (`/products/:productId`).

### 2. Warranty Details (`WarrantyDetailsPage.jsx`)
- **Header & Badges:** Product name, brand, model number, and official status badge.
- **Status Advisories:**
  - **Active:** "✓ Warranty Active — Your product is currently protected under active terms."
  - **Expiring Soon:** "⚠️ Your warranty expires soon — This warranty will expire on [date] ([days] days remaining)."
  - **Expired:** "✕ Warranty Expired — This warranty concluded on [date]."
- **Specifications List:** Warranty UUID, product title, brand/model, start date, expiry date, duration in months, and registration date.
- **Dynamic Coverage Card:** Large countdown number, days remaining label, start/expiry dates, elapsed duration progress bar, and authoritative status badge.

### 3. Product Details Integration (`ProductDetailsPage.jsx`)
- Fetches authoritative warranty metrics via `warrantyService.getProductWarranty(productId)`.
- Renders the **Warranty Protection** panel with real-time status badge, remaining days, accessible progress bar, and direct link to `/warranties/:id`.

---

## 📊 Accessible Progress Bar

Progress bars visually represent the backend-calculated `progressPercentage` and adhere to accessibility guidelines:
```jsx
<div
  role="progressbar"
  aria-valuenow={progress}
  aria-valuemin="0"
  aria-valuemax="100"
  aria-label={`Warranty progress: ${progress}%`}
  style={{ height: '8px', width: '100%', backgroundColor: 'var(--border)', borderRadius: 'var(--radius-full)', overflow: 'hidden' }}
>
  <div
    style={{
      height: '100%',
      width: `${progress}%`,
      backgroundColor:
        status === 'ACTIVE'
          ? 'var(--primary)'
          : status === 'EXPIRING_SOON'
          ? 'var(--warning)'
          : 'var(--danger)',
      borderRadius: 'var(--radius-full)',
      transition: 'width 0.4s ease'
    }}
  />
</div>
```

---

## 🎨 Visual Language & Design Tokens

- **Status Badges:**
  - `ACTIVE`: `badge-active` (`#064e3b` bg, `#6ee7b7` text)
  - `EXPIRING_SOON`: `badge-expiring` (`#78350f` bg, `#fde68a` text)
  - `EXPIRED`: `badge-expired` (`#881337` bg, `#fca5a5` text)
- **Responsive Layout:** CSS grid adapting seamlessly across mobile, tablet, and desktop viewports.
