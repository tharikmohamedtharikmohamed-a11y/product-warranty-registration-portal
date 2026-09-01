# Frontend Architecture — Phase 11: Admin Management Module

## 1. Overview
The Admin Management Module introduces a centralized operations control center in the WarrantyHub React frontend. It provides privileged operators with a comprehensive operations dashboard, global registries (Users, Products, Warranties, Invoices), and a full-featured claim adjudication workflow with life-cycle tracking and confirmation modals.

Access is strictly protected by client-side route guards (`AdminRoute`) and backend role authorization.

---

## 2. Admin Route Architecture & Role Guards

### 2.1 Route Tree
Defined in `src/routes/AppRoutes.jsx`:

| Route Path | Page Component | Guard | Description |
| :--- | :--- | :--- | :--- |
| `/admin` | `AdminDashboardPage` | `AdminRoute` | System operations dashboard with global KPI telemetry |
| `/admin/users` | `AdminUsersPage` | `AdminRoute` | User directory with search by name/email |
| `/admin/products` | `AdminProductsPage` | `AdminRoute` | Cross-customer registered product registry |
| `/admin/warranties` | `AdminWarrantiesPage` | `AdminRoute` | Global warranty portfolio with status filters and countdowns |
| `/admin/invoices` | `AdminInvoicesPage` | `AdminRoute` | Global invoice document vault with preview/download |
| `/admin/claims` | `AdminClaimsPage` | `AdminRoute` | Warranty claim adjudication queue with status tabs |
| `/admin/claims/:id` | `AdminClaimDetailsPage` | `AdminRoute` | Granular claim adjudication & decision processing |

### 2.2 `AdminRoute` Implementation
The `AdminRoute` component wraps all `/admin/**` routes:
```jsx
export default function AdminRoute() {
  const { user, isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ minHeight: '60vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Loading size="lg" text="Verifying administrative privileges..." />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role !== 'ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}
```

### 2.3 Dashboard Role Redirection
- **Login Redirection (`LoginPage`):**
  - Authenticated `ADMIN` users are automatically navigated to `/admin`.
  - Authenticated `CUSTOMER` users are navigated to `/dashboard`.
- **Public Route Guard (`PublicRoute`):**
  - If already logged in, `ADMIN` users visiting `/login` or `/register` are redirected to `/admin`.
  - `CUSTOMER` users are redirected to `/dashboard`.

---

## 3. Dynamic Navigation Header (`Navbar`)

The global `Navbar` dynamically adapts based on the active session's role:

- **When Authenticated as `ADMIN`:**
  - Admin Dashboard (`/admin`)
  - Users (`/admin/users`)
  - Products (`/admin/products`)
  - Warranties (`/admin/warranties`)
  - Invoices (`/admin/invoices`)
  - Claims (`/admin/claims`)
  - Operator greeting and Logout action
- **When Authenticated as `CUSTOMER`:**
  - Dashboard (`/dashboard`)
  - My Products (`/products`)
  - My Warranties (`/warranties`)
  - My Invoices (`/invoices`)
  - My Claims (`/claims`)
  - Register Product (`/products/register`)
- **When Unauthenticated:**
  - Home, Features, How It Works, Login, Get Started

---

## 4. Admin Pages & Interface Features

### 4.1 Operations Dashboard (`AdminDashboardPage.jsx`)
- **Live Telemetry Banner:** Operations header with quick refresh and claim adjudication buttons.
- **Top Metrics Strip:** Displays Total Users (with Customer vs Admin breakdown), Registered Products, Active Warranties, and Pending Claims.
- **Adjudication Queue Card:** Status breakdown for Pending, Approved, In Progress, Completed, Rejected, and Cancelled claims.
- **Warranties Portfolio Card:** Breakdown for Active, Expiring Soon, Expired, and Total Provisioned records.
- **User & Storage Directory Card:** Overview of Customer Accounts, Administrators, Equipment Records, and Stored Invoices.

### 4.2 User Management (`AdminUsersPage.jsx`)
- **Search Bar:** Real-time query submission filtering across name and email.
- **User Table:** Full name, email, system role badge, registration date, and UUID identifier.
- **Standardized States:**
  - Loading: `"Loading users..."`
  - Empty: `"No users found."`
  - Error banner on failure.

### 4.3 Product Registry (`AdminProductsPage.jsx`)
- **Live Filter:** Instant client filtering across product name, brand, model, serial, customer name, and customer email.
- **Registry Table:** Product & Model, Customer identity, Serial number tag, Purchase info (price, date, duration), Warranty status badge, Registration timestamp.
- **Standardized States:**
  - Loading: `"Loading products..."`
  - Empty: `"No products found."`

### 4.4 Warranties Portfolio (`AdminWarrantiesPage.jsx`)
- **Status Filter Tabs:** `ALL`, `ACTIVE`, `EXPIRING_SOON`, `EXPIRED`.
- **Search Input:** Search by product, brand, customer.
- **Warranties Table:** Product, Customer, Status badge, Validity window, Days left countdown, Accessible progress bar (`role="progressbar"`).
- **Standardized States:**
  - Loading: `"Loading warranties..."`
  - Empty: `"No warranties found."`

### 4.5 Invoice Vault (`AdminInvoicesPage.jsx`)
- **Metadata Inspection:** Document name, format badge, Customer profile, Associated equipment, File size, Upload timestamp.
- **Document Actions:**
  - *Preview:* Streams binary document directly in a new browser tab.
  - *Download:* Streams binary download with original file name.
- **Standardized States:**
  - Loading: `"Loading invoices..."`
  - Empty: `"No invoices found."`

### 4.6 Claims Adjudication Queue (`AdminClaimsPage.jsx`)
- **Lifecycle Filter Tabs:** `ALL`, `PENDING`, `APPROVED`, `IN_PROGRESS`, `COMPLETED`, `REJECTED`, `CANCELLED`.
- **Claims Table:** Issue/Reason, Customer name & email, Product & Brand, Lifecycle status badge, Submission timestamp, "View Details →" action.
- **Standardized States:**
  - Loading: `"Loading claims..."`
  - Empty: `"No claims found."`

### 4.7 Claim Details & Adjudication (`AdminClaimDetailsPage.jsx`)
- **Two-Column Adjudication Layout:**
  - *Left Column:* Claimant Profile (Name, Email, User ID), Associated Equipment, Reported Issue & Customer Statement.
  - *Right Column:* Lifecycle Progression Stepper, Administrative Decision Panel.
- **Lifecycle Progression Stepper:** Visual numbered steps indicating Submitted, Triage & Review, Service Dispatch, and Resolution.
- **Admin Decision Controls:**
  - `PENDING` status: **✓ Approve Claim** and **✕ Reject Claim** buttons.
  - `APPROVED` status: **⚙️ Start Processing (IN PROGRESS)** button.
  - `IN_PROGRESS` status: **✓ Mark Claim as COMPLETED** button.
  - Terminal statuses (`COMPLETED`, `REJECTED`, `CANCELLED`): Controls disabled with immutable status advisory.
- **Confirmation Modals & Safety Controls:**
  - Rejection requires mandatory explanation in administrative notes before modal activation.
  - Rejection confirmation title: *"Are you sure you want to reject this claim?"*
  - Completion confirmation title: *"Mark this claim as completed?"*
  - Loading state: `"Loading claims..."`

---

## 5. API Service Layer (`adminService.js`)

Connects to backend administrative endpoints with Axios Bearer token authorization:
- `getDashboardStats()`: Calls `GET /api/admin/dashboard/stats`
- `getUsers(search)`: Calls `GET /api/admin/users?search=...`
- `getProducts()`: Calls `GET /api/admin/products`
- `getWarranties()`: Calls `GET /api/admin/warranties`
- `getInvoices()`: Calls `GET /api/admin/invoices`
- `downloadInvoice(invoiceId, fileName)`: Streams blob and triggers file download
- `viewInvoice(invoiceId)`: Streams blob and opens in target window
- `getClaims()`: Calls `GET /api/admin/claims`
- `getClaimById(id)`: Calls `GET /api/admin/claims/{id}`
- `approveClaim(id, adminNotes)`: Calls `PATCH /api/admin/claims/{id}/approve`
- `rejectClaim(id, adminNotes)`: Calls `PATCH /api/admin/claims/{id}/reject`
- `startClaim(id, adminNotes)`: Calls `PATCH /api/admin/claims/{id}/start`
- `completeClaim(id, adminNotes)`: Calls `PATCH /api/admin/claims/{id}/complete`
