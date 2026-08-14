import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { productService } from '../services/productService';
import { warrantyService } from '../services/warrantyService';
import { claimService } from '../services/claimService';

export const SubmitClaim = () => {
  const [searchParams] = useSearchParams();
  const initialProductId = searchParams.get('productId') || '';
  const initialWarrantyId = searchParams.get('warrantyId') || '';

  const [products, setProducts] = useState([]);
  const [warranties, setWarranties] = useState([]);
  const [selectedProductId, setSelectedProductId] = useState(initialProductId);
  const [selectedWarrantyId, setSelectedWarrantyId] = useState(initialWarrantyId);
  const [issueDescription, setIssueDescription] = useState('');

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [validationError, setValidationError] = useState('');

  const navigate = useNavigate();

  useEffect(() => {
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Load user products and user warranties
      const [prodsData, warrentiesData] = await Promise.all([
        productService.getUserProducts(),
        warrantyService.getUserWarranties(),
      ]);

      const userProds = prodsData || [];
      const userWars = warrentiesData || [];

      setProducts(userProds);
      setWarranties(userWars);

      // Preselect product/warranty if not explicitly set
      if (!initialProductId && userProds.length > 0) {
        setSelectedProductId(userProds[0].id);
      }
    } catch (err) {
      setError(err.message || 'Failed to load eligible products or warranties.');
    } finally {
      setLoading(false);
    }
  };

  // Filter warranties that belong to selected product and are not expired
  const eligibleWarranties = warranties.filter((w) => {
    if (!selectedProductId) return false;
    // Must match selected product ID
    const prodIdMatch = w.productId === selectedProductId || w.product?.id === selectedProductId;
    if (!prodIdMatch) return false;

    // Must be active (end date in future or status ACTIVE)
    if (w.warrantyEndDate) {
      const endDate = new Date(w.warrantyEndDate);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      if (endDate < today) return false;
    }

    return true;
  });

  // Automatically adjust selected warranty when selected product changes
  useEffect(() => {
    if (selectedProductId && eligibleWarranties.length > 0) {
      const currentlySelectedValid = eligibleWarranties.some((w) => w.id === selectedWarrantyId);
      if (!currentlySelectedValid) {
        setSelectedWarrantyId(eligibleWarranties[0].id);
      }
    } else {
      setSelectedWarrantyId('');
    }
  }, [selectedProductId, warranties]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setValidationError('');
    setError(null);

    if (!selectedProductId) {
      setValidationError('Please select a product.');
      return;
    }

    if (!selectedWarrantyId) {
      setValidationError('Please select an active warranty for the product.');
      return;
    }

    if (!issueDescription.trim()) {
      setValidationError('Please enter a description of the issue.');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        productId: selectedProductId,
        warrantyId: selectedWarrantyId,
        issueDescription: issueDescription.trim(),
      };

      const createdClaim = await claimService.submitClaim(payload);
      
      // Navigate to created claim details or claims list
      if (createdClaim?.claimId) {
        navigate(`/claims/${createdClaim.claimId}`, {
          state: { message: 'Warranty claim submitted successfully.' },
        });
      } else {
        navigate('/claims', {
          state: { message: 'Warranty claim submitted successfully.' },
        });
      }
    } catch (err) {
      setError(err.message || 'Failed to submit warranty claim.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading eligible products and warranties...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Submit Warranty Claim</h1>
          <p className="page-subtitle">Request repair or service for an eligible product</p>
        </div>
      </div>

      <div className="form-card max-w-2xl">
        {error && <div className="alert alert-error">{error}</div>}
        {validationError && <div className="alert alert-error">{validationError}</div>}

        {products.length === 0 ? (
          <div className="empty-state">
            <h3>No Products Registered</h3>
            <p>You must have a registered product with an active warranty to submit a claim.</p>
            <button onClick={() => navigate('/products/register')} className="btn btn-primary">
              Register Product First
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="productId" className="form-label">
                Select Product <span className="required">*</span>
              </label>
              <select
                id="productId"
                className="form-control"
                value={selectedProductId}
                onChange={(e) => setSelectedProductId(e.target.value)}
                required
                disabled={submitting}
              >
                <option value="">-- Select a Product --</option>
                {products.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.productName} ({p.brand || 'No Brand'}) — Serial: {p.serialNumber || 'N/A'}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="warrantyId" className="form-label">
                Select Warranty <span className="required">*</span>
              </label>
              <select
                id="warrantyId"
                className="form-control"
                value={selectedWarrantyId}
                onChange={(e) => setSelectedWarrantyId(e.target.value)}
                required
                disabled={submitting || eligibleWarranties.length === 0}
              >
                {eligibleWarranties.length === 0 ? (
                  <option value="">No active warranty found for this product</option>
                ) : (
                  eligibleWarranties.map((w) => (
                    <option key={w.id} value={w.id}>
                      {w.warrantyPeriodMonths ? `${w.warrantyPeriodMonths} Months` : 'Warranty'} (Valid until:{' '}
                      {w.warrantyEndDate ? new Date(w.warrantyEndDate).toLocaleDateString() : 'N/A'})
                    </option>
                  ))
                )}
              </select>
              {selectedProductId && eligibleWarranties.length === 0 && (
                <p className="form-help text-error">
                  This product has no active/valid warranty eligible for a claim.
                </p>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="issueDescription" className="form-label">
                Issue Description <span className="required">*</span>
              </label>
              <textarea
                id="issueDescription"
                className="form-control"
                rows="5"
                placeholder="Describe the issue or defect you are experiencing in detail..."
                value={issueDescription}
                onChange={(e) => setIssueDescription(e.target.value)}
                required
                disabled={submitting}
              ></textarea>
            </div>

            <div className="form-actions">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => navigate('/claims')}
                disabled={submitting}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={submitting || eligibleWarranties.length === 0}
              >
                {submitting ? 'Submitting Claim...' : 'Submit Claim'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
