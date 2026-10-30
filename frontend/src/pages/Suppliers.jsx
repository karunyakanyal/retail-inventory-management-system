import React, { useState, useEffect } from 'react';
import client, { getErrorMessage } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Suppliers() {
  const { canWrite, isManager } = useAuth();
  const { showToast } = useToast();

  const [activeTab, setActiveTab] = useState('pos'); // 'pos' | 'suppliers' | 'performance'
  const [suppliers, setSuppliers] = useState([]);
  const [purchaseOrders, setPurchaseOrders] = useState([]);
  const [performanceList, setPerformanceList] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  // Supplier CRUD Modal
  const [showSupplierModal, setShowSupplierModal] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState(null);
  const [supplierForm, setSupplierForm] = useState({
    name: '',
    contactPerson: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    state: '',
  });
  const [savingSupplier, setSavingSupplier] = useState(false);

  // Create PO Modal
  const [showPoModal, setShowPoModal] = useState(false);
  const [poForm, setPoForm] = useState({
    supplierId: '',
    warehouseLocation: 'WH-001',
    expectedDate: '',
    lines: [{ productId: '', quantity: 50, unitCost: '' }],
  });
  const [creatingPo, setCreatingPo] = useState(false);

  // Receive PO Modal
  const [showReceiveModal, setShowReceiveModal] = useState(false);
  const [receivingPo, setReceivingPo] = useState(null);
  const [receiveData, setReceiveData] = useState({
    warehouseLocation: 'WH-001',
    receivedDate: new Date().toISOString().split('T')[0],
    notes: 'Warehouse dock receiving confirmed',
  });
  const [receiving, setReceiving] = useState(false);

  const warehouseLocations = [
    { code: 'WH-001', name: 'WH-001 - Main Central Warehouse' },
    { code: 'WH-002', name: 'WH-002 - North Distribution Center' },
    { code: 'WH-003', name: 'WH-003 - South Hub' },
  ];

  const fetchData = async () => {
    setLoading(true);
    try {
      const [suppRes, poRes, perfRes, prodRes] = await Promise.all([
        client.get('/api/suppliers'),
        client.get('/api/orders/purchase'),
        client.get('/api/suppliers/performance'),
        client.get('/api/products'),
      ]);

      setSuppliers(suppRes.data || []);
      setPurchaseOrders(poRes.data || []);
      setPerformanceList(perfRes.data || []);
      const pList = Array.isArray(prodRes.data) ? prodRes.data : prodRes.data.content || [];
      setProducts(pList);
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Supplier CRUD
  const openCreateSupplier = () => {
    setEditingSupplier(null);
    setSupplierForm({
      name: '',
      contactPerson: '',
      email: '',
      phone: '',
      address: '',
      city: '',
      state: '',
    });
    setShowSupplierModal(true);
  };

  const openEditSupplier = (s) => {
    setEditingSupplier(s);
    setSupplierForm({
      name: s.name || '',
      contactPerson: s.contactPerson || '',
      email: s.email || '',
      phone: s.phone || '',
      address: s.address || '',
      city: s.city || '',
      state: s.state || '',
    });
    setShowSupplierModal(true);
  };

  const handleSupplierSubmit = async (e) => {
    e.preventDefault();
    setSavingSupplier(true);
    try {
      if (editingSupplier) {
        await client.put(`/api/suppliers/${editingSupplier.id}`, supplierForm);
        showToast(`Supplier "${supplierForm.name}" updated`, 'success');
      } else {
        await client.post('/api/suppliers', supplierForm);
        showToast(`Supplier "${supplierForm.name}" created`, 'success');
      }
      setShowSupplierModal(false);
      fetchData();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setSavingSupplier(false);
    }
  };

  const handleDeleteSupplier = async (id, sName) => {
    if (!window.confirm(`Are you sure you want to delete supplier "${sName}"?`)) return;
    try {
      await client.delete(`/api/suppliers/${id}`);
      showToast(`Supplier "${sName}" deleted`, 'success');
      fetchData();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    }
  };

  // PO Line handling
  const addPoLine = () => {
    setPoForm({
      ...poForm,
      lines: [...poForm.lines, { productId: '', quantity: 50, unitCost: '' }],
    });
  };

  const removePoLine = (index) => {
    if (poForm.lines.length === 1) return;
    setPoForm({
      ...poForm,
      lines: poForm.lines.filter((_, i) => i !== index),
    });
  };

  const updatePoLine = (index, field, value) => {
    const updated = [...poForm.lines];
    updated[index][field] = value;
    if (field === 'productId') {
      const p = products.find((prod) => String(prod.id) === String(value));
      if (p) {
        updated[index].unitCost = p.costPrice || p.price;
      }
    }
    setPoForm({ ...poForm, lines: updated });
  };

  const handleCreatePoSubmit = async (e) => {
    e.preventDefault();
    if (poForm.lines.some((l) => !l.productId || l.quantity <= 0)) {
      showToast('Please provide valid products and quantities for all PO lines', 'warning');
      return;
    }
    setCreatingPo(true);
    try {
      const payload = {
        supplierId: Number(poForm.supplierId),
        warehouseLocation: poForm.warehouseLocation,
        expectedDate: poForm.expectedDate || null,
        lines: poForm.lines.map((l) => ({
          productId: Number(l.productId),
          quantity: Number(l.quantity),
          unitCost: parseFloat(l.unitCost || 0),
        })),
      };

      const res = await client.post('/api/orders/purchase', payload);
      showToast(`Purchase Order ${res.data.poNumber} created successfully`, 'success');
      setShowPoModal(false);
      fetchData();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setCreatingPo(false);
    }
  };

  // Receive PO
  const openReceiveModal = (po) => {
    setReceivingPo(po);
    setReceiveData({
      warehouseLocation: po.warehouseLocation || 'WH-001',
      receivedDate: new Date().toISOString().split('T')[0],
      notes: 'PO Goods Receipt confirmed and stock credited',
    });
    setShowReceiveModal(true);
  };

  const handleReceiveSubmit = async (e) => {
    e.preventDefault();
    setReceiving(true);
    try {
      await client.post(`/api/orders/purchase/${receivingPo.id}/receive`, receiveData);
      showToast(`Purchase order ${receivingPo.poNumber} marked RECEIVED and inventory updated!`, 'success');
      setShowReceiveModal(false);
      fetchData();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setReceiving(false);
    }
  };

  return (
    <div className="suppliers-page">
      {/* Navigation Tabs */}
      <div className="tab-nav-bar flex-between card">
        <div className="tab-buttons">
          <button
            className={`tab-btn ${activeTab === 'pos' ? 'tab-btn-active' : ''}`}
            onClick={() => setActiveTab('pos')}
          >
            📋 Purchase Orders ({purchaseOrders.length})
          </button>
          <button
            className={`tab-btn ${activeTab === 'suppliers' ? 'tab-btn-active' : ''}`}
            onClick={() => setActiveTab('suppliers')}
          >
            🏭 Suppliers Directory ({suppliers.length})
          </button>
          <button
            className={`tab-btn ${activeTab === 'performance' ? 'tab-btn-active' : ''}`}
            onClick={() => setActiveTab('performance')}
          >
            📊 Vendor Performance
          </button>
        </div>

        <div className="tab-actions">
          {activeTab === 'suppliers' && isManager() && (
            <button className="btn btn-primary btn-sm" onClick={openCreateSupplier}>
              + Add Supplier
            </button>
          )}

          {activeTab === 'pos' && canWrite() && (
            <button
              className="btn btn-primary btn-sm"
              onClick={() => {
                setPoForm({
                  supplierId: suppliers[0]?.id || '',
                  warehouseLocation: 'WH-001',
                  expectedDate: new Date(Date.now() + 7 * 86400000).toISOString().split('T')[0],
                  lines: [{ productId: products[0]?.id || '', quantity: 50, unitCost: products[0]?.costPrice || '' }],
                });
                setShowPoModal(true);
              }}
            >
              + Create Purchase Order
            </button>
          )}
        </div>
      </div>

      {/* Tab 1: Purchase Orders */}
      {activeTab === 'pos' && (
        <div className="card mt-3">
          <div className="table-responsive">
            {loading ? (
              <div className="loading-spinner-container">
                <div className="spinner"></div>
                <p>Loading purchase orders...</p>
              </div>
            ) : purchaseOrders.length === 0 ? (
              <div className="empty-state">No purchase orders found.</div>
            ) : (
              <table className="rims-table">
                <thead>
                  <tr>
                    <th>PO #</th>
                    <th>Supplier</th>
                    <th>Warehouse Destination</th>
                    <th>Order Date</th>
                    <th>Expected Date</th>
                    <th>Total ($)</th>
                    <th>Status</th>
                    {canWrite() && <th>Action</th>}
                  </tr>
                </thead>
                <tbody>
                  {purchaseOrders.map((po) => (
                    <tr key={po.id}>
                      <td><strong>{po.poNumber}</strong></td>
                      <td>{po.supplierName || 'Unknown Vendor'}</td>
                      <td><span className="location-chip">{po.warehouseLocation}</span></td>
                      <td>{po.orderDate ? new Date(po.orderDate).toLocaleDateString() : '-'}</td>
                      <td>{po.expectedDate ? String(po.expectedDate) : '-'}</td>
                      <td><strong>${Number(po.totalAmount || 0).toFixed(2)}</strong></td>
                      <td>
                        <span
                          className={`badge ${
                            po.status === 'RECEIVED'
                              ? 'badge-success'
                              : po.status === 'CANCELLED'
                              ? 'badge-danger'
                              : 'badge-primary'
                          }`}
                        >
                          {po.status}
                        </span>
                      </td>
                      {canWrite() && (
                        <td>
                          {po.status !== 'RECEIVED' && po.status !== 'CANCELLED' ? (
                            <button
                              className="btn btn-sm btn-success"
                              onClick={() => openReceiveModal(po)}
                            >
                              Receive PO
                            </button>
                          ) : (
                            <span className="text-muted text-sm">Archived</span>
                          )}
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Tab 2: Suppliers Directory */}
      {activeTab === 'suppliers' && (
        <div className="card mt-3">
          <div className="table-responsive">
            {loading ? (
              <div className="loading-spinner-container">
                <div className="spinner"></div>
                <p>Loading suppliers...</p>
              </div>
            ) : suppliers.length === 0 ? (
              <div className="empty-state">No suppliers found.</div>
            ) : (
              <table className="rims-table">
                <thead>
                  <tr>
                    <th>Supplier Name</th>
                    <th>Contact Person</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>City, State</th>
                    {isManager() && <th>Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {suppliers.map((s) => (
                    <tr key={s.id}>
                      <td><strong>{s.name}</strong></td>
                      <td>{s.contactPerson || '-'}</td>
                      <td>{s.email || '-'}</td>
                      <td>{s.phone || '-'}</td>
                      <td>{[s.city, s.state].filter(Boolean).join(', ') || '-'}</td>
                      {isManager() && (
                        <td>
                          <div className="action-buttons">
                            <button
                              className="btn btn-sm btn-secondary"
                              onClick={() => openEditSupplier(s)}
                            >
                              Edit
                            </button>
                            <button
                              className="btn btn-sm btn-danger"
                              onClick={() => handleDeleteSupplier(s.id, s.name)}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Tab 3: Vendor Performance */}
      {activeTab === 'performance' && (
        <div className="card mt-3">
          <div className="card-header">
            <h3>Supplier Performance & On-Time Delivery KPIs</h3>
          </div>
          <div className="table-responsive">
            {performanceList.length === 0 ? (
              <div className="empty-state">No performance metrics recorded yet.</div>
            ) : (
              <table className="rims-table">
                <thead>
                  <tr>
                    <th>Supplier Name</th>
                    <th>Total POs</th>
                    <th>Received POs</th>
                    <th>On-Time POs</th>
                    <th>On-Time Delivery Rate</th>
                    <th>Performance Tier</th>
                  </tr>
                </thead>
                <tbody>
                  {performanceList.map((p) => {
                    const rate = Number(p.onTimeDeliveryRate || 0);
                    const isGood = rate >= 90;
                    const isMedium = rate >= 70;
                    return (
                      <tr key={p.supplierId}>
                        <td><strong>{p.supplierName}</strong></td>
                        <td>{p.totalPOs}</td>
                        <td>{p.receivedPOs}</td>
                        <td>{p.onTimePOs}</td>
                        <td>
                          <strong className={isGood ? 'text-success' : isMedium ? 'text-warning' : 'text-danger'}>
                            {rate.toFixed(1)}%
                          </strong>
                        </td>
                        <td>
                          <span className={`badge ${isGood ? 'badge-success' : isMedium ? 'badge-warning' : 'badge-danger'}`}>
                            {isGood ? 'Tier 1 Preferred' : isMedium ? 'Tier 2 Acceptable' : 'Needs Review'}
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Supplier Modal */}
      {showSupplierModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <h3>{editingSupplier ? 'Edit Supplier' : 'Add New Supplier'}</h3>
              <button className="modal-close" onClick={() => setShowSupplierModal(false)}>×</button>
            </div>
            <form onSubmit={handleSupplierSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Supplier / Vendor Name *</label>
                  <input
                    type="text"
                    className="form-control"
                    value={supplierForm.name}
                    onChange={(e) => setSupplierForm({ ...supplierForm, name: e.target.value })}
                    required
                  />
                </div>

                <div className="form-row">
                  <div className="form-group flex-1">
                    <label>Contact Person</label>
                    <input
                      type="text"
                      className="form-control"
                      value={supplierForm.contactPerson}
                      onChange={(e) => setSupplierForm({ ...supplierForm, contactPerson: e.target.value })}
                    />
                  </div>
                  <div className="form-group flex-1">
                    <label>Email</label>
                    <input
                      type="email"
                      className="form-control"
                      value={supplierForm.email}
                      onChange={(e) => setSupplierForm({ ...supplierForm, email: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group flex-1">
                    <label>Phone</label>
                    <input
                      type="text"
                      className="form-control"
                      value={supplierForm.phone}
                      onChange={(e) => setSupplierForm({ ...supplierForm, phone: e.target.value })}
                    />
                  </div>
                  <div className="form-group flex-1">
                    <label>City</label>
                    <input
                      type="text"
                      className="form-control"
                      value={supplierForm.city}
                      onChange={(e) => setSupplierForm({ ...supplierForm, city: e.target.value })}
                    />
                  </div>
                  <div className="form-group flex-1">
                    <label>State</label>
                    <input
                      type="text"
                      className="form-control"
                      value={supplierForm.state}
                      onChange={(e) => setSupplierForm({ ...supplierForm, state: e.target.value })}
                    />
                  </div>
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowSupplierModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={savingSupplier}
                >
                  {savingSupplier ? 'Saving...' : 'Save Supplier'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Create PO Modal */}
      {showPoModal && (
        <div className="modal-backdrop">
          <div className="modal-card modal-large">
            <div className="modal-header">
              <h3>Create Purchase Order (PO)</h3>
              <button className="modal-close" onClick={() => setShowPoModal(false)}>×</button>
            </div>
            <form onSubmit={handleCreatePoSubmit}>
              <div className="modal-body">
                <div className="form-row">
                  <div className="form-group flex-1">
                    <label>Supplier / Vendor *</label>
                    <select
                      className="form-control"
                      value={poForm.supplierId}
                      onChange={(e) => setPoForm({ ...poForm, supplierId: e.target.value })}
                      required
                    >
                      <option value="">Select Vendor</option>
                      {suppliers.map((s) => (
                        <option key={s.id} value={s.id}>{s.name}</option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group flex-1">
                    <label>Receiving Warehouse *</label>
                    <select
                      className="form-control"
                      value={poForm.warehouseLocation}
                      onChange={(e) => setPoForm({ ...poForm, warehouseLocation: e.target.value })}
                      required
                    >
                      {warehouseLocations.map((w) => (
                        <option key={w.code} value={w.code}>{w.name}</option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group flex-1">
                    <label>Expected Delivery Date</label>
                    <input
                      type="date"
                      className="form-control"
                      value={poForm.expectedDate}
                      onChange={(e) => setPoForm({ ...poForm, expectedDate: e.target.value })}
                    />
                  </div>
                </div>

                <div className="lines-section mt-3">
                  <div className="flex-between mb-2">
                    <h4>Purchase Order Items</h4>
                    <button type="button" className="btn btn-sm btn-outline" onClick={addPoLine}>
                      + Add Item
                    </button>
                  </div>

                  {poForm.lines.map((line, idx) => (
                    <div key={idx} className="line-row form-row">
                      <div className="form-group flex-3">
                        <label>Product</label>
                        <select
                          className="form-control"
                          value={line.productId}
                          onChange={(e) => updatePoLine(idx, 'productId', e.target.value)}
                          required
                        >
                          <option value="">Select Product</option>
                          {products.map((p) => (
                            <option key={p.id} value={p.id}>
                              {p.sku} - {p.name}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div className="form-group flex-1">
                        <label>Units</label>
                        <input
                          type="number"
                          min="1"
                          className="form-control"
                          value={line.quantity}
                          onChange={(e) => updatePoLine(idx, 'quantity', e.target.value)}
                          required
                        />
                      </div>

                      <div className="form-group flex-1">
                        <label>Unit Cost ($)</label>
                        <input
                          type="number"
                          step="0.01"
                          className="form-control"
                          value={line.unitCost}
                          onChange={(e) => updatePoLine(idx, 'unitCost', e.target.value)}
                          required
                        />
                      </div>

                      <div className="form-group line-action-col">
                        <button
                          type="button"
                          className="btn btn-sm btn-danger mt-4"
                          disabled={poForm.lines.length === 1}
                          onClick={() => removePoLine(idx)}
                        >
                          ✕
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowPoModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={creatingPo}
                >
                  {creatingPo ? 'Submitting PO...' : 'Create Purchase Order'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Receive PO Modal */}
      {showReceiveModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <h3>Receive Purchase Order ({receivingPo?.poNumber})</h3>
              <button className="modal-close" onClick={() => setShowReceiveModal(false)}>×</button>
            </div>
            <form onSubmit={handleReceiveSubmit}>
              <div className="modal-body">
                <div className="alert alert-info mb-3">
                  Confirming receipt will automatically increase inventory stock levels for all products in this order.
                </div>

                <div className="form-group">
                  <label>Receiving Warehouse *</label>
                  <select
                    className="form-control"
                    value={receiveData.warehouseLocation}
                    onChange={(e) => setReceiveData({ ...receiveData, warehouseLocation: e.target.value })}
                    required
                  >
                    {warehouseLocations.map((w) => (
                      <option key={w.code} value={w.code}>{w.name}</option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Actual Received Date *</label>
                  <input
                    type="date"
                    className="form-control"
                    value={receiveData.receivedDate}
                    onChange={(e) => setReceiveData({ ...receiveData, receivedDate: e.target.value })}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Receiving Inspection Notes</label>
                  <textarea
                    rows="2"
                    className="form-control"
                    value={receiveData.notes}
                    onChange={(e) => setReceiveData({ ...receiveData, notes: e.target.value })}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowReceiveModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-success"
                  disabled={receiving}
                >
                  {receiving ? 'Updating Inventory...' : 'Confirm Goods Receipt'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
