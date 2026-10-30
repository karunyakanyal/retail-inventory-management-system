import React, { useState, useEffect } from 'react';
import client, { getErrorMessage } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Inventory() {
  const { canWrite } = useAuth();
  const { showToast } = useToast();

  const [stockList, setStockList] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [selectedLocation, setSelectedLocation] = useState('');
  const [selectedProductId, setSelectedProductId] = useState('');
  const [showLowStockOnly, setShowLowStockOnly] = useState(false);

  // Adjustment Modal
  const [showAdjustModal, setShowAdjustModal] = useState(false);
  const [adjustData, setAdjustData] = useState({
    productId: '',
    location: 'WH-001',
    quantityDelta: 0,
    reason: 'Count correction',
  });
  const [adjusting, setAdjusting] = useState(false);

  const locations = [
    { code: 'WH-001', name: 'WH-001 - Main Central Warehouse' },
    { code: 'WH-002', name: 'WH-002 - North Distribution Center' },
    { code: 'WH-003', name: 'WH-003 - South Hub' },
    { code: 'STR-001', name: 'STR-001 - Downtown Flagship Store' },
    { code: 'STR-002', name: 'STR-002 - Uptown Boutique' },
    { code: 'STR-003', name: 'STR-003 - Suburban Outlet' },
    { code: 'STR-004', name: 'STR-004 - Metro Mall Kiosk' },
    { code: 'STR-005', name: 'STR-005 - Airport Express' },
  ];

  const fetchInitialData = async () => {
    try {
      const prodRes = await client.get('/api/products');
      setProducts(Array.isArray(prodRes.data) ? prodRes.data : prodRes.data.content || []);
    } catch (err) {
      console.error('Error fetching products list', err);
    }
  };

  const fetchStock = async () => {
    setLoading(true);
    try {
      if (showLowStockOnly) {
        const res = await client.get('/api/inventory/low-stock');
        setStockList(res.data || []);
      } else {
        const params = {};
        if (selectedLocation) params.location = selectedLocation;
        if (selectedProductId) params.productId = selectedProductId;

        const res = await client.get('/api/inventory/stock', { params });
        setStockList(res.data || []);
      }
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInitialData();
  }, []);

  useEffect(() => {
    fetchStock();
  }, [selectedLocation, selectedProductId, showLowStockOnly]);

  const openAdjustForStock = (stockItem) => {
    setAdjustData({
      productId: stockItem?.product?.id || products[0]?.id || '',
      location: stockItem?.location || 'WH-001',
      quantityDelta: 0,
      reason: 'Physical count adjustment',
    });
    setShowAdjustModal(true);
  };

  const handleAdjustSubmit = async (e) => {
    e.preventDefault();
    if (adjustData.quantityDelta === 0) {
      showToast('Quantity delta cannot be zero', 'warning');
      return;
    }
    setAdjusting(true);
    try {
      await client.post('/api/inventory/adjust', {
        productId: Number(adjustData.productId),
        location: adjustData.location,
        quantityDelta: Number(adjustData.quantityDelta),
        reason: adjustData.reason,
      });
      showToast('Stock adjusted successfully', 'success');
      setShowAdjustModal(false);
      fetchStock();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setAdjusting(false);
    }
  };

  return (
    <div className="inventory-page">
      {/* Filters and Actions */}
      <div className="card filter-card">
        <div className="filter-grid">
          <div className="form-group">
            <label>Filter by Location</label>
            <select
              className="form-control"
              value={selectedLocation}
              disabled={showLowStockOnly}
              onChange={(e) => setSelectedLocation(e.target.value)}
            >
              <option value="">All Locations</option>
              {locations.map((loc) => (
                <option key={loc.code} value={loc.code}>
                  {loc.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Filter by Product</label>
            <select
              className="form-control"
              value={selectedProductId}
              disabled={showLowStockOnly}
              onChange={(e) => setSelectedProductId(e.target.value)}
            >
              <option value="">All Products</option>
              {products.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.sku} - {p.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group checkbox-align">
            <label className="checkbox-label cursor-pointer">
              <input
                type="checkbox"
                checked={showLowStockOnly}
                onChange={(e) => setShowLowStockOnly(e.target.checked)}
              />{' '}
              <strong className="text-danger">⚠️ Low Stock Alerts Only</strong>
            </label>
          </div>
        </div>

        <div className="filter-actions-row">
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => {
              setSelectedLocation('');
              setSelectedProductId('');
              setShowLowStockOnly(false);
            }}
          >
            Reset Filters
          </button>

          {canWrite() && (
            <button
              className="btn btn-primary btn-sm"
              onClick={() => openAdjustForStock(null)}
            >
              ⚡ Adjust Stock
            </button>
          )}
        </div>
      </div>

      {/* Stock Levels Table */}
      <div className="card mt-3">
        <div className="table-responsive">
          {loading ? (
            <div className="loading-spinner-container">
              <div className="spinner"></div>
              <p>Loading inventory stock levels...</p>
            </div>
          ) : stockList.length === 0 ? (
            <div className="empty-state">No stock entries found matching criteria.</div>
          ) : (
            <table className="rims-table">
              <thead>
                <tr>
                  <th>Product SKU</th>
                  <th>Product Name</th>
                  <th>Location Code</th>
                  <th>Quantity on Hand</th>
                  <th>Reorder Point</th>
                  <th>Safety Stock</th>
                  <th>Health Status</th>
                  {canWrite() && <th>Quick Action</th>}
                </tr>
              </thead>
              <tbody>
                {stockList.map((item) => {
                  const isStockout = item.qty === 0;
                  const isLow = item.qty <= item.reorderPoint;
                  return (
                    <tr key={item.id}>
                      <td><strong>{item.product?.sku}</strong></td>
                      <td>{item.product?.name}</td>
                      <td><span className="location-chip">{item.location}</span></td>
                      <td>
                        <strong className={isStockout ? 'text-danger' : isLow ? 'text-warning' : 'text-success'}>
                          {item.qty} units
                        </strong>
                      </td>
                      <td>{item.reorderPoint}</td>
                      <td>{item.safetyStock}</td>
                      <td>
                        {isStockout ? (
                          <span className="badge badge-danger">OUT OF STOCK</span>
                        ) : isLow ? (
                          <span className="badge badge-warning">REORDER REQUIRED</span>
                        ) : (
                          <span className="badge badge-success">HEALTHY</span>
                        )}
                      </td>
                      {canWrite() && (
                        <td>
                          <button
                            className="btn btn-sm btn-outline"
                            onClick={() => openAdjustForStock(item)}
                          >
                            Adjust
                          </button>
                        </td>
                      )}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Adjustment Modal */}
      {showAdjustModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <h3>Stock Level Adjustment</h3>
              <button className="modal-close" onClick={() => setShowAdjustModal(false)}>×</button>
            </div>
            <form onSubmit={handleAdjustSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Product *</label>
                  <select
                    className="form-control"
                    value={adjustData.productId}
                    onChange={(e) => setAdjustData({ ...adjustData, productId: e.target.value })}
                    required
                  >
                    <option value="">Select a Product</option>
                    {products.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.sku} - {p.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Location *</label>
                  <select
                    className="form-control"
                    value={adjustData.location}
                    onChange={(e) => setAdjustData({ ...adjustData, location: e.target.value })}
                    required
                  >
                    {locations.map((loc) => (
                      <option key={loc.code} value={loc.code}>
                        {loc.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Quantity Delta (+ to increase, - to decrease) *</label>
                  <input
                    type="number"
                    className="form-control"
                    placeholder="e.g. 50 or -10"
                    value={adjustData.quantityDelta}
                    onChange={(e) => setAdjustData({ ...adjustData, quantityDelta: e.target.value })}
                    required
                  />
                  <small className="form-text">
                    Use positive numbers for restock / found items, negative for scrap / shrinkage.
                  </small>
                </div>

                <div className="form-group">
                  <label>Reason *</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="e.g. Audit correction, Damaged stock, Emergency intake"
                    value={adjustData.reason}
                    onChange={(e) => setAdjustData({ ...adjustData, reason: e.target.value })}
                    required
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowAdjustModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={adjusting}
                >
                  {adjusting ? 'Adjusting...' : 'Commit Adjustment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
