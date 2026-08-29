# WarrantyHub — Frontend Invoice Management

## 📌 Module Overview

The **Frontend Invoice Management Module** enables customers to seamlessly upload, preview, download, and manage their equipment purchase receipts and warranty invoices.

It provides a standalone invoice dashboard (`/invoices`), an integrated **Purchase Invoice** panel on the **Product Details** page (`/products/:id`), an accessible upload modal with drag-and-drop support, client-side validation, and secure communication with the backend.

---

## 🧭 Routes & Navigation

| Route | Component | Guard | Description |
| :--- | :--- | :--- | :--- |
| `/invoices` | `InvoicesPage.jsx` | `ProtectedRoute` | Full listing of all customer-uploaded invoices with view/download/delete actions. |
| `/products/:id` | `ProductDetailsPage.jsx` | `ProtectedRoute` | Contains the embedded Purchase Invoice panel and triggers `InvoiceUpload` modal. |

### Global Navigation (`Navbar.jsx`)
- Added **My Invoices** to authenticated top-level navigation, positioned between *My Warranties* and *Register Product*.
- Added an **Invoices** quick-action shortcut button on `DashboardPage.jsx`.

---

## 🧩 Components & Architecture

### 1. `InvoiceUpload.jsx`
- **Location:** `frontend/src/components/InvoiceUpload.jsx`
- **Props:**
  - `productId`: Target product UUID.
  - `productName`: Name of product for display in header and description.
  - `onSuccess(invoice)`: Callback invoked upon successful upload to refresh UI.
  - `onClose()`: Dismisses modal.
- **Features:**
  - Drag-and-drop file upload area and file chooser.
  - Allowed file formats: `.pdf`, `.jpg`, `.jpeg`, `.png`.
  - Client-side size validation: rejects files > 10 MB.
  - Client-side empty file validation: rejects 0-byte files.
  - Displays selected file name, formatted size (`KB`/`MB`), and MIME badge.
  - Prevents double submission with dynamic loading indicator: `"Uploading invoice..."`.
  - Accessible dialog (`role="dialog"`, `aria-modal="true"`).

### 2. `InvoicesPage.jsx`
- **Location:** `frontend/src/pages/InvoicesPage.jsx`
- **Features:**
  - Displays all invoices belonging to the authenticated customer in a clean, responsive table.
  - Shows file name, associated product link, file format badge, file size, and upload date.
  - **Inline Preview:** Opens document in a secure new tab via `invoiceService.viewInvoice(id)`.
  - **Download:** Triggers local browser download via blob URL in `invoiceService.downloadInvoice(id, fileName)`.
  - **Delete Confirmation:** Prompts user with a confirmation modal before permanent deletion.
  - Empty state with CTA to view registered products when no invoices exist.

### 3. `ProductDetailsPage.jsx` Integration
- **Location:** `frontend/src/pages/ProductDetailsPage.jsx`
- Replaced future-phase teaser placeholder with an interactive **Purchase Invoice** card:
  - When invoice exists: Displays invoice file name, format badge, file size, upload date, and action buttons (`View`, `Download`, `Delete`).
  - When no invoice exists: Displays `"No invoice uploaded yet."` and an `"Upload Invoice"` button opening `InvoiceUpload`.
  - Real-time updates without full page reloads upon upload or delete.

### 4. `invoiceService.js`
- **Location:** `frontend/src/services/invoiceService.js`
- API client wrapper utilizing Axios instance from `api.js`:
  - `uploadInvoice(productId, file)`: sends `multipart/form-data` to `POST /api/invoices/upload`.
  - `getInvoices()`: `GET /api/invoices`.
  - `getInvoiceById(id)`: `GET /api/invoices/${id}`.
  - `getProductInvoice(productId)`: `GET /api/products/${productId}/invoice`.
  - `downloadInvoice(id, fileName)`: fetches blob via `GET /api/invoices/${id}/download` and initiates file save.
  - `viewInvoice(id)`: fetches blob via `GET /api/invoices/${id}/view` and opens in new window.
  - `deleteInvoice(id)`: `DELETE /api/invoices/${id}`.

---

## 🔒 Security & Client Safety

- **No Secret Exposure:** Neither Supabase credentials, service-role keys, nor database credentials exist in frontend code or environment files.
- **Protected Endpoints:** All requests automatically include Bearer JWT tokens attached by Axios interceptors.
- **Storage Isolation:** Frontend never constructs storage paths; all paths are generated and resolved server-side.
