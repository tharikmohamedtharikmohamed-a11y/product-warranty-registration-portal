# WarrantyHub — Frontend Dashboard Architecture & Operational Telemetry
**Phase 12 — Dashboard & Notifications**

---

## 1. Overview
The Dashboard views in WarrantyHub present users and administrators with actionable, real database-backed metrics, equipment expiration warnings, claims review pipelines, and chronological audit trails.

---

## 2. Customer Dashboard (`DashboardPage.jsx`)

### Key Capabilities & Data Layout
- **Real-Time KPIs**:
  - Registered Products: `dashboard.products.total`
  - Invoices Stored: `dashboard.invoices.total`
  - Active Warranties: `dashboard.warranties.active` (green accent border)
  - Expiring Soon (30d): `dashboard.warranties.expiringSoon` (warning amber accent border)
  - Expired Warranties: `dashboard.warranties.expired`
  - Claims Adjudication: Pending triage vs completed count
- **Expiring Warranties Preview (Action Required)**:
  - Lists up to 5 warranties expiring soonest.
  - Displays product name, brand, expiry date, days remaining badge (`X days left` or `Expired`), and direct inspection link.
  - Informative empty state if no policies expire within 30 days.
- **Recent Claims Preview**:
  - Lists up to 5 most recently filed claims with status badge (`PENDING`, `APPROVED`, `IN_PROGRESS`, `COMPLETED`, `REJECTED`, `CANCELLED`).
  - Direct navigation to claim inspection detail view.
- **Authoritative Recent Activity Timeline**:
  - Unified timeline generated from real database timestamps.
  - Identifies equipment additions (`PRODUCT`), invoice uploads (`INVOICE`), and claim filings (`CLAIM`).
  - Relative time display (`5m ago`, `2h ago`, `Yesterday`, date).
  - Direct navigation links to relevant entity pages.
- **Quick Action Bar**:
  - One-click shortcuts to `+ Register Product`, `Warranties`, `Invoices`, `Claims`, and `Inbox`.

---

## 3. Administrator Dashboard (`AdminDashboardPage.jsx`)

### Retained Core 16 KPIs + Extended Operational Telemetry
The administrative dashboard retains all 16 authoritative platform KPIs established in Phase 11 and incorporates 3 live operational previews:
1. **Recent Claims Received**: Displays the latest 4 claims submitted across all users, with customer contact, product name, claim reason, and status badge.
2. **Recently Registered Users**: Displays the latest 4 accounts registered on the platform, showing user name, email, and `ROLE` badge.
3. **Recently Registered Equipment**: Displays the latest 4 products registered by customers, with owner name, brand, and warranty coverage status.

---

## 4. Frontend Service Layer (`dashboardService.js`)

Centralizes telemetry communication:
```javascript
const dashboardService = {
  getDashboard: async () => {
    const response = await api.get('/api/dashboard');
    return response.data;
  }
};
```
Authentication headers (Bearer JWT) are automatically injected via the shared Axios interceptor in `api.js`.
