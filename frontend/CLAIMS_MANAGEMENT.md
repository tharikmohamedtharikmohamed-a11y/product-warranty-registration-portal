# WarrantyHub — Frontend Warranty Claims Architecture & Implementation
**Phase 10 — Warranty Claims**

---

## 1. Overview
The Warranty Claims UI module provides a seamless, accessible interface for customers to file defect claims, monitor claim lifecycles, view detailed claim timelines, and cancel pending claims if needed.

---

## 2. Components & Pages

### 2.1 Services & State
- **`claimService.js`**:
  - `submitClaim({ productId, claimReason, description })`: `POST /api/claims`
  - `getClaims()`: `GET /api/claims`
  - `getClaim(id)`: `GET /api/claims/{id}`
  - `cancelClaim(id)`: `PATCH /api/claims/{id}/cancel`
  - `getProductClaims(productId)`: `GET /api/products/{productId}/claims`

### 2.2 Modal Components
- **`SubmitClaimModal.jsx`**:
  - Accessible modal dialog for submitting a claim with ARIA attributes (`role="dialog"`, `aria-modal="true"`, `aria-labelledby`).
  - Supports submitting with a pre-selected product (e.g. from `ProductDetailsPage`) or dropdown selection of user's registered products with active warranties (e.g. from `ClaimsPage`).
  - Form validation:
    - Product selection required.
    - Issue Reason / Title required (minimum 5 chars, maximum 255 chars).
    - Detailed explanation optional (up to 2000 chars).
  - Handles submission loading state, server error alerts, and triggers success callback.

### 2.3 Page Views
- **`ClaimsPage.jsx` (`/claims`)**:
  - Dedicated claim dashboard listing all warranty claims filed by the user.
  - Filter claims by status (`ALL`, `PENDING`, `APPROVED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`).
  - Search claims by product name, brand, or issue description.
  - Empty state with direct "File Your First Claim" action.
  - "+ File Claim" button opens `SubmitClaimModal`.
- **`ClaimDetailsPage.jsx` (`/claims/:id`)**:
  - Granular claim detail view with status badge, submission date, product specifications, and issue description.
  - Interactive status timeline showing lifecycle progression (`Submitted` -> `Under Review` -> `In Progress` -> `Resolution`).
  - Accessible cancellation modal dialog: lets customers cancel `PENDING` claims with real-time UI status updates.
  - Protects unowned claims with 404 access error handling and redirect options.
- **`ProductDetailsPage.jsx` (`/products/:id`)**:
  - Integrated **Warranty Claims** panel displaying claims associated with the specific product.
  - Direct "+ File Claim" action button if warranty is active.
  - Clear message if warranty is expired explaining new claims cannot be filed.
- **`WarrantyDetailsPage.jsx` (`/warranties/:id`)**:
  - Updated Claim Eligibility box confirming active coverage and linking directly to claim filing.

---

## 3. UI Styling & Accessibility
- **CSS Status Tokens**:
  - `badge-pending`: Amber/Yellow styling for claims awaiting triage.
  - `badge-active`: Green styling for approved/completed claims.
  - `badge-expiring`: Blue/Indigo styling for in-progress claims.
  - `badge-expired`: Slate/Neutral styling for cancelled claims or red for rejected claims.
- **Accessibility (a11y)**:
  - Accessible dialogs with proper `aria-modal`, `aria-labelledby`, and focus trap management.
  - Descriptive labels, contrast-compliant status badges, and semantic alert roles (`role="alert"`).
