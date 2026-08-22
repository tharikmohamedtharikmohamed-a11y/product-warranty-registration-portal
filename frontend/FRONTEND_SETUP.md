# WarrantyHub — Frontend Setup & Developer Guide

**Brand:** WarrantyHub  
**Tagline:** *"Your Warranties. Organized. Protected. Always Accessible."*  
**Phase:** Phase 5 — Frontend Initial Setup  

This document provides complete instructions for configuring, running, and developing the **WarrantyHub** React frontend application.

---

## 1. Environment & Runtime Specifications

| Component | Target Version | Installed Environment | Purpose |
| :--- | :--- | :--- | :--- |
| **Node.js** | 18.x / 20.x+ | `v24.19.0` | JavaScript runtime |
| **npm** | 9.x+ | `11.17.0` | Node package manager |
| **React** | 18.x (`^18.3.1`) | `18.3.1` | Component UI library (Strictly React 18) |
| **React DOM** | 18.x (`^18.3.1`) | `18.3.1` | React DOM renderer |
| **Vite** | 5.x (`^5.4.14`) | `5.4.21` | High-performance frontend build tool & HMR dev server |
| **React Router** | 6.x (`^6.28.0`) | `6.30.6` | Client-side declarative routing |
| **Axios** | 1.x (`^1.7.9`) | `1.20.0` | Promise-based HTTP client |
| **Styling** | Vanilla CSS | CSS3 / Custom Properties | Modern responsive design system |

---

## 2. Directory Structure

```
frontend/
├── public/
│   └── favicon.svg              # WarrantyHub shield brand icon
├── src/
│   ├── assets/                  # Static images, icons, and illustrations
│   ├── components/              # Reusable presentational components
│   │   ├── Button.jsx           # Button with variants (primary, secondary, outline, ghost)
│   │   ├── ErrorMessage.jsx     # Accessible alert component
│   │   ├── Footer.jsx           # Global SaaS footer with brand & links
│   │   ├── Loading.jsx          # Accessible spinner with ARIA labels
│   │   └── Navbar.jsx           # Responsive header with navigation & CTA buttons
│   ├── context/                 # Prepared for Phase 6 (e.g., AuthContext)
│   ├── hooks/                   # Custom React hooks directory
│   ├── layouts/
│   │   └── PublicLayout.jsx     # Master layout wrapping Navbar, main content, and Footer
│   ├── pages/                   # Application views
│   │   ├── ForgotPasswordPage.jsx # Forgot password placeholder view
│   │   ├── LandingPage.jsx      # Core marketing & feature showcase page
│   │   ├── LoginPage.jsx        # Customer sign-in placeholder view
│   │   ├── NotFoundPage.jsx     # 404 error page with home redirect
│   │   ├── RegisterPage.jsx     # Customer registration placeholder view
│   │   └── ResetPasswordPage.jsx  # Reset password placeholder view
│   ├── routes/
│   │   ├── AppRoutes.jsx        # Central route definitions
│   │   └── ProtectedRoute.jsx   # Route guard prepared for Phase 6 auth checks
│   ├── services/
│   │   ├── api.js               # Reusable Axios instance with base URL configuration
│   │   └── healthService.js     # Backend health verification service (GET /api/health)
│   ├── utils/                   # Helper utilities
│   ├── App.jsx                  # Root component rendering AppRoutes
│   ├── index.css                # Global design system tokens, reset, & responsive styles
│   └── main.jsx                 # Application entry point with BrowserRouter
├── .env.example                 # Environment variable template
├── .gitignore                   # Ignored files (node_modules, dist, .env)
├── FRONTEND_SETUP.md            # Frontend documentation (this file)
├── index.html                   # HTML5 template with Inter font and metadata
├── package.json                 # Pinned project dependencies and scripts
└── vite.config.js               # Vite 5 configuration with React plugin and port 5173
```

---

## 3. Environment Configuration

The frontend communicates with the Spring Boot backend using the `VITE_API_BASE_URL` environment variable.

### Create Local `.env`
Create a `.env` file in `frontend/` based on `frontend/.env.example`:

```bash
# frontend/.env
VITE_API_BASE_URL=http://localhost:8080
```

> [!CAUTION]
> **Security Rule:** Any variable prefixed with `VITE_` is bundled and exposed publicly to browser clients. Never place private keys, database credentials, Supabase service-role keys, or JWT secrets inside frontend `.env` files.

---

## 4. Installation & Build Commands

All commands should be executed from within the `frontend/` directory:

```bash
cd frontend
```

### Install Dependencies
```bash
npm install
```

### Run Local Development Server
Starts the Vite development server with Hot Module Replacement (HMR) on port `5173`:
```bash
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) in your browser.

### Production Build
Compiles and bundles production-optimized assets into `frontend/dist/`:
```bash
npm run build
```

### Preview Production Build
Locally preview the generated production build:
```bash
npm run preview
```

---

## 5. Available Application Routes

| Route | View Component | Access | Purpose |
| :--- | :--- | :--- | :--- |
| `/` | `LandingPage.jsx` | Public | Hero showcase, feature highlights, how it works, and live backend health indicator |
| `/login` | `LoginPage.jsx` | Public | Customer login placeholder view (interactive form arrives in Phase 6) |
| `/register` | `RegisterPage.jsx` | Public | Customer registration placeholder view (interactive form arrives in Phase 6) |
| `/forgot-password`| `ForgotPasswordPage.jsx`| Public | Forgot password recovery placeholder view |
| `/reset-password` | `ResetPasswordPage.jsx` | Public | Reset password placeholder view |
| `*` | `NotFoundPage.jsx` | Public | Custom 404 page for unmatched routes |

---

## 6. Backend Integration & Verification

The frontend is configured to consume APIs from the Spring Boot backend (`http://localhost:8080`):
- **Health Check Service:** [healthService.js](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/src/services/healthService.js) issues `GET /api/health`.
- **CORS Support:** The backend Spring Boot configuration allows requests originating from `http://localhost:5173`.
- **Axios Client:** [api.js](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/src/services/api.js) centralizes headers, JSON encoding, and timeout configurations.
