# WarrantyHub — Frontend Authentication & Route Guards Guide

**Brand:** WarrantyHub  
**Tagline:** *"Your Warranties. Organized. Protected. Always Accessible."*  
**Phase:** Phase 6 — Frontend Authentication & Route Guards  

This document details the client-side authentication architecture, token storage mechanisms, route protection guards, and end-to-end authentication flows connecting the React frontend to the Spring Boot backend.

---

## 1. Authentication Architecture Overview

The frontend authentication system relies on **JWT (JSON Web Tokens)** managed through a centralized React Context (`AuthContext`) and synchronized with browser `localStorage`.

```
┌─────────────────┐      Credentials (POST /api/auth/login)     ┌─────────────────────┐
│  React Frontend │ ───────────────────────────────────────────> │ Spring Boot Backend │
│  (LoginPage)    │ <─────────────────────────────────────────── │ (AuthController)    │
└────────┬────────┘          JWT Token + User Profile            └─────────────────────┘
         │
         │ Store Token
         ▼
┌─────────────────────────┐
│ localStorage            │
│ key: warrantyhub_token  │
└────────┬────────────────┘
         │
         │ Request Interceptor: Authorization: Bearer <token>
         ▼
┌─────────────────┐      Authenticated API (GET /api/auth/me)   ┌─────────────────────┐
│ Axios Client    │ ───────────────────────────────────────────> │ Spring Boot Backend │
│ (api.js)        │ <─────────────────────────────────────────── │ (JwtAuthFilter)     │
└─────────────────┘              User Profile                    └─────────────────────┘
```

---

## 2. JWT Storage & Lifecycle

- **Storage Target:** `localStorage`
- **Key Name:** `warrantyhub_token`
- **Security Scope:** Only the signed JWT access token string is stored. Passwords, password hashes, and database secrets are **never** stored in client-side storage, state, or cookies.
- **Persistence:** Preserved across browser refreshes and restored automatically on initial application mount.

---

## 3. Global Authentication Context (`AuthContext`)

Defined in [src/context/AuthContext.jsx](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/src/context/AuthContext.jsx), providing:

| Property / Method | Type | Description |
| :--- | :--- | :--- |
| `user` | `Object \| null` | Authenticated user profile (`id`, `name`, `email`, `role`). |
| `loading` | `boolean` | Indicates whether initial session restoration from localStorage is ongoing. |
| `isAuthenticated`| `boolean` | `true` if a verified authenticated user object exists. |
| `login(email, password)` | `async Function` | Dispatches login request, stores JWT, updates state, and returns user. |
| `register(name, email, password)`| `async Function`| Dispatches customer registration request. |
| `logout()` | `Function` | Clears stored JWT token and resets user state to `null`. |
| `refreshUser()` | `async Function` | Re-fetches current user profile from `/api/auth/me`. |

Components access this context via the custom hook [useAuth()](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/src/hooks/useAuth.js).

---

## 4. Route Guards

### Protected Routes (`ProtectedRoute.jsx`)
Guards private application views (e.g., `/dashboard`):
- If `loading === true`: Renders an accessible loading spinner (`"Checking authentication..."`).
- If `isAuthenticated === false`: Redirects visitor to `/login` using React Router `<Navigate to="/login" replace />`.
- If `isAuthenticated === true`: Renders children via `<Outlet />`.

### Public Authentication Guards (`PublicRoute.jsx`)
Guards public authentication pages (`/login`, `/register`):
- If visitor is already authenticated (`isAuthenticated === true`), automatically redirects them to `/dashboard` to prevent unnecessary re-authentication.

---

## 5. Axios Request & Response Interceptors

Implemented in [src/services/api.js](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/src/services/api.js):

### Request Interceptor
Automatically reads `warrantyhub_token` from `localStorage`. If present, attaches the header:
```http
Authorization: Bearer <JWT_TOKEN>
```

### Response Interceptor
Inspects responses for `HTTP 401 Unauthorized`:
- Excludes `/api/auth/login` and `/api/auth/register` endpoints to preserve field-level login failure alerts (`"Invalid email or password"`).
- For all other requests (e.g. expired session on `/api/auth/me`), purges `warrantyhub_token` and redirects to `/login`.

---

## 6. Authentication Flows

### 6.1 Registration Flow (`/register`)
1. User enters Full Name, Email, Password, and Password Confirmation.
2. Client-side validation verifies:
   - Full name is non-empty.
   - Email format is valid (`user@domain.com`).
   - Password is at least 8 characters.
   - Confirmation password matches.
3. Submits `POST /api/auth/register` with `{ name, email, password }`.
4. On success (`201 Created`): Displays confirmation alert and automatically redirects to `/login`.
5. On duplicate email (`409 Conflict`): Displays friendly error alert (`"Email is already registered."`).

### 6.2 Login Flow (`/login`)
1. User enters Email and Password.
2. Submits `POST /api/auth/login` with `{ email, password }`.
3. On success (`200 OK`):
   - JWT stored in `localStorage` under `warrantyhub_token`.
   - `user` profile stored in `AuthContext`.
   - Automatically navigates to `/dashboard`.
4. On invalid credentials (`401 Unauthorized`): Displays generic `"Invalid email or password"` alert.

### 6.3 Session Restoration (`GET /api/auth/me`)
1. App mounts in `main.jsx`.
2. `AuthContext` checks `localStorage.getItem('warrantyhub_token')`.
3. If found, calls `GET /api/auth/me` with Bearer token.
4. If token is valid: populates user profile and sets `loading = false`.
5. If token is expired / invalid: removes token and sets `user = null`.

### 6.4 Logout Flow
1. User clicks **Logout** in Navbar or Dashboard.
2. `logout()` is invoked:
   - Purges `warrantyhub_token` from `localStorage`.
   - Sets `user = null`.
   - Redirects to `/login`.

---

## 7. Manual Testing & Verification Guide

Follow these steps to verify Phase 6 functionality:

| Step | Action | Expected Result |
| :--- | :--- | :--- |
| **1. Registration** | Navigate to `http://localhost:5173/register`, enter new credentials, submit | User created with role `CUSTOMER`, success alert displays, redirects to `/login`. |
| **2. Duplicate Email** | Re-attempt registering the exact same email | Displays 409 error: `"Email is already registered."` |
| **3. Valid Login** | On `/login`, enter valid email & password | Receives JWT, stores `warrantyhub_token`, redirects to `/dashboard`. |
| **4. Guard Verification** | Clear localStorage token, manually visit `/dashboard` | Immediately redirected to `/login`. |
| **5. Session Refresh** | Login, navigate to `/dashboard`, press browser Refresh (F5) | Session persists seamlessly; user details reload without redirect. |
| **6. Logout** | Click "Logout" button | Token deleted from localStorage, redirected to `/login`. |
| **7. Invalid Login** | Enter correct email with wrong password | Displays `"Invalid email or password"` alert; no token saved. |
| **8. Expired Token** | In browser DevTools, set `warrantyhub_token` to `invalid.token.here` and open `/dashboard` | Backend returns 401; interceptor purges token and redirects to `/login`. |
| **9. Guarded Login** | While logged in, open `/login` | PublicRoute automatically redirects to `/dashboard`. |
| **10. Guarded Register** | While logged in, open `/register` | PublicRoute automatically redirects to `/dashboard`. |

---

## 8. Security Rules Checklist

- [x] Passwords are never logged or stored in client storage.
- [x] JWT token is stored only in `localStorage` under `warrantyhub_token`.
- [x] Passwords require a minimum length of 8 characters.
- [x] No fake/mock authentication state or hardcoded JWT tokens.
- [x] Public registration strictly assigns role `CUSTOMER`.
- [x] Sensitive backend exceptions and database stack traces are suppressed from UI.
