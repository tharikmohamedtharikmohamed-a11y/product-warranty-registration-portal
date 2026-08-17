import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { productService } from '../services/productService';

export const EditProduct = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    productName: '',
    category: '',
    brand: '',
    modelNumber: '',
    serialNumber: '',
    purchaseDate: '',
    sellerName: '',
    price: '',
    warrantyDurationMonths: 12,
    description: '',
  });

  const [initialLoading, setInitialLoading] = useState(true);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');

  useEffect(() => {
    const fetchProductDetails = async () => {
      try {
        setInitialLoading(true);
        setApiError('');
        const data = await productService.getProductById(id);
        
        setFormData({
          productName: data.productName || '',
          category: data.category || '',
          brand: data.brand || '',
          modelNumber: data.modelNumber || '',
          serialNumber: data.serialNumber || '',
          purchaseDate: data.purchaseDate || '',
          sellerName: data.sellerName || '',
          price: data.price !== null && data.price !== undefined ? data.price : '',
          warrantyDurationMonths: data.warranty?.warrantyPeriodMonths || data.warranty?.warrantyDurationMonths || 12,
          description: data.description || '',
        });
      } catch (err) {
        setApiError(err.message || 'Unable to load product for editing.');
      } finally {
        setInitialLoading(false);
      }
    };

    fetchProductDetails();
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.productName.trim()) {
      newErrors.productName = 'Product name is required';
    } else if (formData.productName.length > 150) {
      newErrors.productName = 'Product name must not exceed 150 characters';
    }

    if (!formData.brand.trim()) {
      newErrors.brand = 'Brand is required';
    } else if (formData.brand.length > 100) {
      newErrors.brand = 'Brand must not exceed 100 characters';
    }

    if (formData.category && formData.category.length > 100) {
      newErrors.category = 'Category must not exceed 100 characters';
    }

    if (formData.modelNumber && formData.modelNumber.length > 100) {
      newErrors.modelNumber = 'Model number must not exceed 100 characters';
    }

    if (!formData.serialNumber.trim()) {
      newErrors.serialNumber = 'Serial number is required';
    } else if (formData.serialNumber.length > 100) {
      newErrors.serialNumber = 'Serial number must not exceed 100 characters';
    }

    if (!formData.purchaseDate) {
      newErrors.purchaseDate = 'Purchase date is required';
    }

    if (formData.sellerName && formData.sellerName.length > 150) {
      newErrors.sellerName = 'Seller name must not exceed 150 characters';
    }

    if (formData.price !== '' && formData.price !== null) {
      const parsedPrice = parseFloat(formData.price);
      if (isNaN(parsedPrice) || parsedPrice < 0) {
        newErrors.price = 'Price cannot be negative';
      }
    }

    if (!formData.warrantyDurationMonths) {
      newErrors.warrantyDurationMonths = 'Warranty duration is required';
    } else {
      const duration = parseInt(formData.warrantyDurationMonths, 10);
      if (isNaN(duration) || duration < 1) {
        newErrors.warrantyDurationMonths = 'Warranty duration must be greater than 0';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError('');

    if (!validateForm()) {
      return;
    }

    try {
      setLoading(true);

      const payload = {
        productName: formData.productName.trim(),
        category: formData.category.trim() || null,
        brand: formData.brand.trim(),
        modelNumber: formData.modelNumber.trim() || null,
        serialNumber: formData.serialNumber.trim(),
        purchaseDate: formData.purchaseDate,
        sellerName: formData.sellerName.trim() || null,
        price: formData.price !== '' ? parseFloat(formData.price) : null,
        warrantyDurationMonths: parseInt(formData.warrantyDurationMonths, 10),
        description: formData.description.trim() || null,
      };

      await productService.updateProduct(id, payload);

      navigate(`/products/${id}`, {
        state: { successMessage: 'Product updated successfully.' },
      });
    } catch (err) {
      setApiError(err.message || 'Unable to update product. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title">Edit Product</h1>
          <p className="page-subtitle">Update your product information and details.</p>
        </div>
        <Link to={`/products/${id}`} className="btn btn-secondary btn-sm">
          ← Back to Details
        </Link>
      </div>

      {apiError && <div className="alert alert-error">{apiError}</div>}

      {initialLoading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <div className="loading-spinner" style={{ borderColor: 'var(--primary)', borderTopColor: 'transparent', width: '32px', height: '32px' }}></div>
          <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading product details for editing...</p>
        </div>
      ) : (
        <div className="form-card-wide">
          <form onSubmit={handleSubmit} noValidate>
            {/* Section 1: Product Information */}
            <div className="form-section">
              <h2 className="form-section-title">Product Information</h2>
              <div className="form-grid-2">
                <div className="form-group">
                  <label className="form-label" htmlFor="productName">
                    Product Name <span className="required-star">*</span>
                  </label>
                  <input
                    type="text"
                    id="productName"
                    name="productName"
                    className="form-input"
                    placeholder="e.g. MacBook Pro 16-inch"
                    value={formData.productName}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                  {errors.productName && <div className="form-error">{errors.productName}</div>}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="brand">
                    Brand <span className="required-star">*</span>
                  </label>
                  <input
                    type="text"
                    id="brand"
                    name="brand"
                    className="form-input"
                    placeholder="e.g. Apple"
                    value={formData.brand}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                  {errors.brand && <div className="form-error">{errors.brand}</div>}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="category">
                    Category
                  </label>
                  <input
                    type="text"
                    id="category"
                    name="category"
                    className="form-input"
                    placeholder="e.g. Laptops & Electronics"
                    value={formData.category}
                    onChange={handleChange}
                    disabled={loading}
                  />
                  {errors.category && <div className="form-error">{errors.category}</div>}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="modelNumber">
                    Model Number
                  </label>
                  <input
                    type="text"
                    id="modelNumber"
                    name="modelNumber"
                    className="form-input"
                    placeholder="e.g. A2485"
                    value={formData.modelNumber}
                    onChange={handleChange}
                    disabled={loading}
                  />
                  {errors.modelNumber && <div className="form-error">{errors.modelNumber}</div>}
                </div>

                <div className="form-group" style={{ gridColumn: 'span 2' }}>
                  <label className="form-label" htmlFor="serialNumber">
                    Serial Number <span className="required-star">*</span>
                  </label>
                  <input
                    type="text"
                    id="serialNumber"
                    name="serialNumber"
                    className="form-input"
                    placeholder="e.g. C02G1234MD6R"
                    value={formData.serialNumber}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                  {errors.serialNumber && <div className="form-error">{errors.serialNumber}</div>}
                </div>
              </div>
            </div>

            {/* Section 2: Purchase Information */}
            <div className="form-section">
              <h2 className="form-section-title">Purchase Information</h2>
              <div className="form-grid-2">
                <div className="form-group">
                  <label className="form-label" htmlFor="purchaseDate">
                    Purchase Date <span className="required-star">*</span>
                  </label>
                  <input
                    type="date"
                    id="purchaseDate"
                    name="purchaseDate"
                    className="form-input"
                    value={formData.purchaseDate}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                  {errors.purchaseDate && <div className="form-error">{errors.purchaseDate}</div>}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="warrantyDurationMonths">
                    Warranty Duration (Months) <span className="required-star">*</span>
                  </label>
                  <input
                    type="number"
                    id="warrantyDurationMonths"
                    name="warrantyDurationMonths"
                    className="form-input"
                    placeholder="e.g. 12"
                    min="1"
                    value={formData.warrantyDurationMonths}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                  {errors.warrantyDurationMonths && (
                    <div className="form-error">{errors.warrantyDurationMonths}</div>
                  )}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="price">
                    Price Paid ($)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    id="price"
                    name="price"
                    className="form-input"
                    placeholder="e.g. 1299.99"
                    min="0"
                    value={formData.price}
                    onChange={handleChange}
                    disabled={loading}
                  />
                  {errors.price && <div className="form-error">{errors.price}</div>}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="sellerName">
                    Seller / Retailer Name
                  </label>
                  <input
                    type="text"
                    id="sellerName"
                    name="sellerName"
                    className="form-input"
                    placeholder="e.g. Best Buy / Official Store"
                    value={formData.sellerName}
                    onChange={handleChange}
                    disabled={loading}
                  />
                  {errors.sellerName && <div className="form-error">{errors.sellerName}</div>}
                </div>
              </div>
            </div>

            {/* Section 3: Additional Information */}
            <div className="form-section">
              <h2 className="form-section-title">Additional Information</h2>
              <div className="form-group">
                <label className="form-label" htmlFor="description">
                  Description / Notes
                </label>
                <textarea
                  id="description"
                  name="description"
                  className="form-input"
                  rows="4"
                  placeholder="Optional notes or additional details about this product..."
                  value={formData.description}
                  onChange={handleChange}
                  disabled={loading}
                  style={{ resize: 'vertical' }}
                />
                {errors.description && <div className="form-error">{errors.description}</div>}
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '2rem' }}>
              <Link to={`/products/${id}`} className="btn btn-secondary">
                Cancel
              </Link>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? (
                  <>
                    <span className="loading-spinner" /> Updating product...
                  </>
                ) : (
                  'Save Changes'
                )}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
