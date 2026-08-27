import React, { useState, useMemo } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import productService from '../services/productService';

const CATEGORY_OPTIONS = [
  'Electronics',
  'Computers & Laptops',
  'Mobile & Tablets',
  'Audio & Headphones',
  'Home & Kitchen Appliances',
  'Cameras & Photography',
  'Gaming & Consoles',
  'Wearables & Smartwatches',
  'Automotive & Tools',
  'Other'
];

/**
 * Product Registration Form Page.
 * Collects product purchase metadata and provides real-time automated warranty preview.
 * Phase 7 — Product Management
 */
export default function RegisterProductPage() {
  const navigate = useNavigate();

  const todayStr = useMemo(() => new Date().toISOString().split('T')[0], []);

  const [formData, setFormData] = useState({
    productName: '',
    category: 'Electronics',
    brand: '',
    modelNumber: '',
    serialNumber: '',
    purchaseDate: todayStr,
    sellerName: '',
    price: '',
    warrantyDurationMonths: 12,
    description: ''
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Dynamic Warranty Projection Calculation
  const warrantyPreview = useMemo(() => {
    if (!formData.purchaseDate || !formData.warrantyDurationMonths) {
      return null;
    }

    try {
      const parts = formData.purchaseDate.split('-');
      if (parts.length !== 3) return null;

      const year = parseInt(parts[0], 10);
      const month = parseInt(parts[1], 10) - 1;
      const day = parseInt(parts[2], 10);

      const startDate = new Date(year, month, day);
      if (isNaN(startDate.getTime())) return null;

      const duration = parseInt(formData.warrantyDurationMonths, 10);
      if (isNaN(duration) || duration <= 0) return null;

      const expiryDate = new Date(year, month + duration, day);
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const msPerDay = 1000 * 60 * 60 * 24;
      const daysRemaining = Math.ceil((expiryDate - today) / msPerDay);

      let status = 'ACTIVE';
      if (daysRemaining < 0) {
        status = 'EXPIRED';
      } else if (daysRemaining <= 30) {
        status = 'EXPIRING_SOON';
      }

      return {
        startDateStr: formData.purchaseDate,
        expiryDateStr: expiryDate.toISOString().split('T')[0],
        status,
        daysRemaining: Math.max(0, daysRemaining)
      };
    } catch (e) {
      return null;
    }
  }, [formData.purchaseDate, formData.warrantyDurationMonths]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));

    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const errors = {};
    if (!formData.productName.trim()) errors.productName = 'Product name is required';
    if (!formData.category.trim()) errors.category = 'Category is required';
    if (!formData.brand.trim()) errors.brand = 'Brand is required';
    if (!formData.modelNumber.trim()) errors.modelNumber = 'Model number is required';
    if (!formData.serialNumber.trim()) errors.serialNumber = 'Serial number is required';

    if (!formData.purchaseDate) {
      errors.purchaseDate = 'Purchase date is required';
    } else if (formData.purchaseDate > todayStr) {
      errors.purchaseDate = 'Purchase date cannot be in the future';
    }

    if (!formData.sellerName.trim()) errors.sellerName = 'Seller name is required';

    if (formData.price === '' || formData.price === null) {
      errors.price = 'Price is required';
    } else if (parseFloat(formData.price) < 0) {
      errors.price = 'Price cannot be negative';
    }

    if (!formData.warrantyDurationMonths) {
      errors.warrantyDurationMonths = 'Warranty duration is required';
    } else if (parseInt(formData.warrantyDurationMonths, 10) < 1) {
      errors.warrantyDurationMonths = 'Warranty duration must be at least 1 month';
    }

    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setGeneralError('');

    const errors = validate();
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    try {
      setSubmitting(true);
      const payload = {
        ...formData,
        productName: formData.productName.trim(),
        brand: formData.brand.trim(),
        modelNumber: formData.modelNumber.trim(),
        serialNumber: formData.serialNumber.trim(),
        sellerName: formData.sellerName.trim(),
        price: parseFloat(formData.price),
        warrantyDurationMonths: parseInt(formData.warrantyDurationMonths, 10),
        description: formData.description ? formData.description.trim() : null
      };

      await productService.createProduct(payload);
      navigate('/products');
    } catch (err) {
      if (err.response && err.response.status === 409) {
        setFieldErrors((prev) => ({
          ...prev,
          serialNumber: err.response.data?.message || 'A product with this serial number is already registered in your account.'
        }));
      } else if (err.response?.data?.errors) {
        setFieldErrors(err.response.data.errors);
      } else {
        setGeneralError(err.response?.data?.message || 'Failed to register product. Please review all fields.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container" style={{ padding: '3rem 1.5rem', flex: 1, maxWidth: '980px' }}>
      <div style={{ marginBottom: '2rem' }}>
        <Link to="/products" className="btn btn-ghost btn-sm" style={{ marginBottom: '1rem', paddingLeft: 0 }}>
          ← Back to Products
        </Link>
        <h1 className="page-title">Register Purchased Product</h1>
        <p className="page-subtitle">
          Record your product purchase details. WarrantyHub will automatically provision and monitor your warranty protection.
        </p>
      </div>

      {generalError && (
        <div className="error-alert" style={{ marginBottom: '2rem' }}>
          <span>{generalError}</span>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '3fr 2fr', gap: '2rem', alignItems: 'start' }}>
        {/* Registration Form */}
        <div className="detail-card">
          <form onSubmit={handleSubmit} noValidate>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              {/* Product Name */}
              <div className="form-group">
                <label className="form-label" htmlFor="productName">Product Name *</label>
                <input
                  id="productName"
                  name="productName"
                  type="text"
                  className={`form-input ${fieldErrors.productName ? 'input-error' : ''}`}
                  placeholder="e.g. MacBook Pro 16 M3 Max"
                  value={formData.productName}
                  onChange={handleChange}
                  required
                />
                {fieldErrors.productName && <span className="field-error-text">{fieldErrors.productName}</span>}
              </div>

              {/* Category & Brand Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label" htmlFor="category">Category *</label>
                  <select
                    id="category"
                    name="category"
                    className={`form-input ${fieldErrors.category ? 'input-error' : ''}`}
                    value={formData.category}
                    onChange={handleChange}
                  >
                    {CATEGORY_OPTIONS.map((c) => (
                      <option key={c} value={c}>{c}</option>
                    ))}
                  </select>
                  {fieldErrors.category && <span className="field-error-text">{fieldErrors.category}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="brand">Brand / Manufacturer *</label>
                  <input
                    id="brand"
                    name="brand"
                    type="text"
                    className={`form-input ${fieldErrors.brand ? 'input-error' : ''}`}
                    placeholder="e.g. Apple, Sony, Samsung"
                    value={formData.brand}
                    onChange={handleChange}
                    required
                  />
                  {fieldErrors.brand && <span className="field-error-text">{fieldErrors.brand}</span>}
                </div>
              </div>

              {/* Model & Serial Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label" htmlFor="modelNumber">Model Number *</label>
                  <input
                    id="modelNumber"
                    name="modelNumber"
                    type="text"
                    className={`form-input ${fieldErrors.modelNumber ? 'input-error' : ''}`}
                    placeholder="e.g. A2991"
                    value={formData.modelNumber}
                    onChange={handleChange}
                    required
                  />
                  {fieldErrors.modelNumber && <span className="field-error-text">{fieldErrors.modelNumber}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="serialNumber">Serial Number *</label>
                  <input
                    id="serialNumber"
                    name="serialNumber"
                    type="text"
                    className={`form-input ${fieldErrors.serialNumber ? 'input-error' : ''}`}
                    placeholder="Unique Hardware S/N"
                    value={formData.serialNumber}
                    onChange={handleChange}
                    required
                  />
                  {fieldErrors.serialNumber && <span className="field-error-text">{fieldErrors.serialNumber}</span>}
                </div>
              </div>

              {/* Purchase Date & Seller Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label" htmlFor="purchaseDate">Purchase Date *</label>
                  <input
                    id="purchaseDate"
                    name="purchaseDate"
                    type="date"
                    max={todayStr}
                    className={`form-input ${fieldErrors.purchaseDate ? 'input-error' : ''}`}
                    value={formData.purchaseDate}
                    onChange={handleChange}
                    required
                  />
                  {fieldErrors.purchaseDate && <span className="field-error-text">{fieldErrors.purchaseDate}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="sellerName">Seller / Store Name *</label>
                  <input
                    id="sellerName"
                    name="sellerName"
                    type="text"
                    className={`form-input ${fieldErrors.sellerName ? 'input-error' : ''}`}
                    placeholder="e.g. Best Buy, Amazon, Official Store"
                    value={formData.sellerName}
                    onChange={handleChange}
                    required
                  />
                  {fieldErrors.sellerName && <span className="field-error-text">{fieldErrors.sellerName}</span>}
                </div>
              </div>

              {/* Price & Duration Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label" htmlFor="price">Purchase Price ($ USD) *</label>
                  <input
                    id="price"
                    name="price"
                    type="number"
                    step="0.01"
                    min="0"
                    className={`form-input ${fieldErrors.price ? 'input-error' : ''}`}
                    placeholder="0.00"
                    value={formData.price}
                    onChange={handleChange}
                    required
                  />
                  {fieldErrors.price && <span className="field-error-text">{fieldErrors.price}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="warrantyDurationMonths">Warranty Duration (Months) *</label>
                  <input
                    id="warrantyDurationMonths"
                    name="warrantyDurationMonths"
                    type="number"
                    min="1"
                    className={`form-input ${fieldErrors.warrantyDurationMonths ? 'input-error' : ''}`}
                    value={formData.warrantyDurationMonths}
                    onChange={handleChange}
                    required
                  />
                  {fieldErrors.warrantyDurationMonths && <span className="field-error-text">{fieldErrors.warrantyDurationMonths}</span>}
                </div>
              </div>

              {/* Description */}
              <div className="form-group">
                <label className="form-label" htmlFor="description">Notes / Description (Optional)</label>
                <textarea
                  id="description"
                  name="description"
                  rows={3}
                  className="form-input"
                  placeholder="Optional details, condition notes, or accessory inclusions..."
                  value={formData.description}
                  onChange={handleChange}
                />
              </div>

              {/* Form Actions */}
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
                <Link to="/products" className="btn btn-secondary btn-md">
                  Cancel
                </Link>
                <button
                  type="submit"
                  className="btn btn-primary btn-md"
                  disabled={submitting}
                >
                  {submitting ? 'Registering Product...' : 'Register Product'}
                </button>
              </div>
            </div>
          </form>
        </div>

        {/* Real-time Automated Warranty Preview Card */}
        <div>
          <div className="warranty-highlight-card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="var(--primary-border)" strokeWidth="2.5">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                <path d="m9 12 2 2 4-4" />
              </svg>
              <h3 style={{ fontSize: 'var(--font-size-base)', fontWeight: '700', color: '#ffffff' }}>
                Automated Warranty Preview
              </h3>
            </div>

            <p style={{ fontSize: 'var(--font-size-xs)', color: '#94a3b8', lineHeight: '1.6', marginBottom: '1.5rem' }}>
              Warranty terms are generated instantaneously upon registration. When saved, this 1-to-1 record will be created in your database.
            </p>

            {warrantyPreview ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontSize: 'var(--font-size-xs)', color: '#94a3b8' }}>Status</span>
                  <span
                    className="status-badge"
                    style={{
                      backgroundColor:
                        warrantyPreview.status === 'ACTIVE'
                          ? '#064e3b'
                          : warrantyPreview.status === 'EXPIRING_SOON'
                          ? '#78350f'
                          : '#881337',
                      color:
                        warrantyPreview.status === 'ACTIVE'
                          ? '#6ee7b7'
                          : warrantyPreview.status === 'EXPIRING_SOON'
                          ? '#fde68a'
                          : '#fca5a5',
                      border: 'none'
                    }}
                  >
                    <span className="status-dot"></span> {warrantyPreview.status.replace('_', ' ')}
                  </span>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontSize: 'var(--font-size-xs)', color: '#94a3b8' }}>Start Date</span>
                  <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: '600', color: '#f8fafc' }}>
                    {warrantyPreview.startDateStr}
                  </span>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontSize: 'var(--font-size-xs)', color: '#94a3b8' }}>Calculated Expiry</span>
                  <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: '600', color: '#f8fafc' }}>
                    {warrantyPreview.expiryDateStr}
                  </span>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontSize: 'var(--font-size-xs)', color: '#94a3b8' }}>Active Duration</span>
                  <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: '600', color: 'var(--primary-border)' }}>
                    {warrantyPreview.daysRemaining} days remaining
                  </span>
                </div>
              </div>
            ) : (
              <div style={{ fontSize: 'var(--font-size-xs)', color: '#64748b', fontStyle: 'italic' }}>
                Enter purchase date and duration months to preview warranty calculation.
              </div>
            )}
          </div>

          <div
            style={{
              marginTop: '1.5rem',
              padding: '1.25rem',
              borderRadius: 'var(--radius-lg)',
              backgroundColor: 'var(--surface)',
              border: '1px solid var(--border)'
            }}
          >
            <h4 style={{ fontSize: 'var(--font-size-xs)', fontWeight: '700', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
              Data Isolation Guarantee
            </h4>
            <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
              Products registered are strictly bound to your authenticated account ID. Cross-customer querying is rejected at the database query layer.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
