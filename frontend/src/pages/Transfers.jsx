import React, { useState, useEffect } from 'react';
import client, { getErrorMessage } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Transfers() {
  const { canWrite } = useAuth();
  const { showToast } = useToast();

  const [transfers, setTransfers] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal State
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    productId: '',
    sourceLocation: 'WH-001',
    destLocation: 'STR-001',
    quantity: 10,
    notes: 'Inter-facility stock replenishment',
  });
  const [submitting, setSubmitting] = useState(false);

  const locations = [
    { code: 'WH-001', name: 'WH-001 (Main Central Warehouse)' },
    { code: 'WH-002', name: 'WH-002 (North Distribution Center)' },
    { code: 'WH-003', name: 'WH-003 (South Hub)' },
    { code: 'STR-001', name: 'STR-001 (Downtown Flagship Store)' },
    { code: 'STR-002', name: 'STR-002 (Uptown Boutique)' },
    { code: 'STR-003', name: 'STR-003 (Suburban Outlet)' },
    { code: 'STR-004', name: 'STR-004 (Metro Mall Kiosk)' },
    { code: 'STR-005', name: 'STR-005 (Airport Express)' },
  ];

  const fetchTransfers = async () => {
    setLoading(true);
    try {
      const res = await client.get('/api/inventory/transfers');
      setTransfers(res.data || []);
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const res = await client.get('/api/products');
      const list = Array.isArray(res.data) ? res.data : res.data.content || [];
      setProducts(list);
      if (list.length > 0) {
        setFormData((prev) => ({ ...prev, productId: list[0].id }));
      }
    } catch (err) {
      console.error('Error fetching products', err);
    }
  };

  useEffect(() => {
    fetchTransfers();
    fetchProducts();
  }, []);

  const handleCreateTransfer = async (e) => {
    e.preventDefault();
    if (formData.sourceLocation === formData.destLocation) {
      showToast('Source and Destination locations must be different', 'warning');
      return;
    }
    if (formData.quantity <= 0) {
      showToast('Transfer quantity must be greater than zero', 'warning');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        productId: Number(formData.productId),
        sourceLocation: formData.sourceLocation,
        destLocation: formData.destLocation,
        quantity: Number(formData.quantity),
        notes: formData.notes,
      };

      const res = await client.post('/api/inventory/transfers', payload);
      showToast(`Transfer ${res.data.transferNumber} created successfully`, 'success');
      setShowModal(false);
      fetchTransfers();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="transfers-page">
      <div className="card filter-card flex-between">
        <div>
          <h3>Stock Transfer Operations</h3>
          <p className="text-muted">Atomic transfer of items across warehouses and store locations.</p>
        </div>
        {canWrite() && (
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>
            + Create Stock Transfer
          </button>
        )}
      </div>

      <div className="card mt-3">
        <div className="table-responsive">
          {loading ? (
            <div className="loading-spinner-container">
              <div className="spinner"></div>
              <p>Loading transfer history...</p>
            </div>
          ) : transfers.length === 0 ? (
            <div className="empty-state">No stock transfers found.</div>
          ) : (
            <table className="rims-table">
              <thead>
                <tr>
                  <th>Transfer #</th>
                  <th>Product SKU & Name</th>
                  <th>From (Source)</th>
                  <th>To (Destination)</th>
                  <th>Quantity</th>
                  <th>Status</th>
                  <th>Date & Time</th>
                </tr>
              </thead>
              <tbody>
                {transfers.map((t) => (
                  <tr key={t.id}>
                    <td><strong>{t.transferNumber}</strong></td>
                    <td>
                      {t.product?.sku} - {t.product?.name}
                    </td>
                    <td><span className="location-chip">{t.sourceLocation}</span></td>
                    <td><span className="location-chip">{t.destLocation}</span></td>
                    <td><strong>{t.quantity}</strong></td>
                    <td>
                      <span className={`badge badge-${t.status === 'COMPLETED' ? 'success' : 'primary'}`}>
                        {t.status}
                      </span>
                    </td>
                    <td>
                      {t.transferDate
                        ? new Date(t.transferDate).toLocaleString()
                        : 'Just now'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Create Transfer Modal */}
      {showModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <h3>Initiate Stock Transfer</h3>
              <button className="modal-close" onClick={() => setShowModal(false)}>×</button>
            </div>
            <form onSubmit={handleCreateTransfer}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Product *</label>
                  <select
                    className="form-control"
                    value={formData.productId}
                    onChange={(e) => setFormData({ ...formData, productId: e.target.value })}
                    required
                  >
                    <option value="">Select product to move</option>
                    {products.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.sku} - {p.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-row">
                  <div className="form-group flex-1">
                    <label>Source Location *</label>
                    <select
                      className="form-control"
                      value={formData.sourceLocation}
                      onChange={(e) => setFormData({ ...formData, sourceLocation: e.target.value })}
                      required
                    >
                      {locations.map((loc) => (
                        <option key={loc.code} value={loc.code}>
                          {loc.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group flex-1">
                    <label>Destination Location *</label>
                    <select
                      className="form-control"
                      value={formData.destLocation}
                      onChange={(e) => setFormData({ ...formData, destLocation: e.target.value })}
                      required
                    >
                      {locations.map((loc) => (
                        <option key={loc.code} value={loc.code}>
                          {loc.name}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="form-group">
                  <label>Quantity to Transfer *</label>
                  <input
                    type="number"
                    min="1"
                    className="form-control"
                    value={formData.quantity}
                    onChange={(e) => setFormData({ ...formData, quantity: e.target.value })}
                    required
                  />
                  <small className="form-text">
                    Note: Backend atomically deducts from source and increments destination.
                  </small>
                </div>

                <div className="form-group">
                  <label>Transfer Notes / Reason</label>
                  <textarea
                    rows="2"
                    className="form-control"
                    value={formData.notes}
                    onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting}
                >
                  {submitting ? 'Processing Transfer...' : 'Execute Transfer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
