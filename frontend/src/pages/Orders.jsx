import React, { useState, useEffect } from 'react';
import client, { getErrorMessage } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Orders() {
  const { canWrite } = useAuth();
  const { showToast } = useToast();

  const [orders, setOrders] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');

  // Create Order Modal
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [orderForm, setOrderForm] = useState({
    customerName: 'Direct Retail Customer',
    location: 'WH-001',
    notes: 'In-store online order',
    lines: [{ productId: '', quantity: 1, unitPrice: '' }],
  });
  const [creatingOrder, setCreatingOrder] = useState(false);

  // Return Modal
  const [showReturnModal, setShowReturnModal] = useState(false);
  const [returnOrderId, setReturnOrderId] = useState(null);
  const [returnData, setReturnData] = useState({
    restockLocation: 'WH-001',
    reason: 'Customer return - Unopened',
    refundAmount: '',
  });
  const [returning, setReturning] = useState(false);

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

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await client.get('/api/orders/sales');
      setOrders(res.data || []);
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
    } catch (err) {
      console.error('Failed to fetch products', err);
    }
  };

  useEffect(() => {
    fetchOrders();
    fetchProducts();
  }, []);

  // Action handlers
  const handleAllocate = async (orderId) => {
    try {
      await client.post(`/api/orders/sales/${orderId}/allocate`);
      showToast(`Stock allocated successfully for order #${orderId}`, 'success');
      fetchOrders();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    }
  };

  const handleUpdateStatus = async (orderId, nextStatus) => {
    try {
      await client.put(`/api/orders/sales/${orderId}/status`, {
        status: nextStatus,
        notes: `Advanced to ${nextStatus}`,
      });
      showToast(`Order status updated to ${nextStatus}`, 'success');
      fetchOrders();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    }
  };

  const handleCancel = async (orderId) => {
    const reason = window.prompt('Please enter cancellation reason:', 'Customer requested cancellation');
    if (!reason) return;

    try {
      await client.post(`/api/orders/sales/${orderId}/cancel?reason=${encodeURIComponent(reason)}`);
      showToast(`Order #${orderId} has been cancelled`, 'info');
      fetchOrders();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    }
  };

  const openReturnModal = (order) => {
    setReturnOrderId(order.id);
    setReturnData({
      restockLocation: order.location || 'WH-001',
      reason: 'Customer return - Unopened',
      refundAmount: order.totalAmount || '',
    });
    setShowReturnModal(true);
  };

  const handleReturnSubmit = async (e) => {
    e.preventDefault();
    setReturning(true);
    try {
      const payload = {
        restockLocation: returnData.restockLocation,
        reason: returnData.reason,
        refundAmount: returnData.refundAmount ? parseFloat(returnData.refundAmount) : 0,
      };

      await client.post(`/api/orders/sales/${returnOrderId}/return`, payload);
      showToast(`Order #${returnOrderId} processed for return & restocked`, 'success');
      setShowReturnModal(false);
      fetchOrders();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setReturning(false);
    }
  };

  // Add/remove lines in Create Order form
  const addLine = () => {
    setOrderForm({
      ...orderForm,
      lines: [...orderForm.lines, { productId: '', quantity: 1, unitPrice: '' }],
    });
  };

  const removeLine = (index) => {
    if (orderForm.lines.length === 1) return;
    setOrderForm({
      ...orderForm,
      lines: orderForm.lines.filter((_, i) => i !== index),
    });
  };

  const updateLine = (index, field, value) => {
    const newLines = [...orderForm.lines];
    newLines[index][field] = value;

    if (field === 'productId') {
      const selected = products.find((p) => String(p.id) === String(value));
      if (selected) {
        newLines[index].unitPrice = selected.price;
      }
    }

    setOrderForm({ ...orderForm, lines: newLines });
  };

  const handleCreateOrderSubmit = async (e) => {
    e.preventDefault();
    if (orderForm.lines.some((l) => !l.productId || l.quantity <= 0)) {
      showToast('Please select valid products and quantities for all lines', 'warning');
      return;
    }

    setCreatingOrder(true);
    try {
      const payload = {
        location: orderForm.location,
        notes: orderForm.notes,
        lines: orderForm.lines.map((l) => ({
          productId: Number(l.productId),
          quantity: Number(l.quantity),
          unitPrice: parseFloat(l.unitPrice || 0),
        })),
      };

      await client.post('/api/orders/sales', payload);
      showToast('Sales order created successfully', 'success');
      setShowCreateModal(false);
      fetchOrders();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setCreatingOrder(false);
    }
  };

  const filteredOrders = statusFilter
    ? orders.filter((o) => o.status === statusFilter)
    : orders;

  const getStatusBadge = (status) => {
    switch (status) {
      case 'NEW':
        return <span className="badge badge-primary">NEW</span>;
      case 'ALLOCATED':
        return <span className="badge badge-purple">ALLOCATED</span>;
      case 'PICKED':
        return <span className="badge badge-yellow">PICKED</span>;
      case 'PACKED':
        return <span className="badge badge-teal">PACKED</span>;
      case 'SHIPPED':
        return <span className="badge badge-orange">SHIPPED</span>;
      case 'DELIVERED':
        return <span className="badge badge-success">DELIVERED</span>;
      case 'CANCELLED':
        return <span className="badge badge-danger">CANCELLED</span>;
      case 'RETURNED':
        return <span className="badge badge-secondary">RETURNED</span>;
      default:
        return <span className="badge">{status}</span>;
    }
  };

  return (
    <div className="orders-page">
      <div className="card filter-card">
        <div className="flex-between">
          <div className="flex-align-gap">
            <label>Filter by Status:</label>
            <select
              className="form-control"
              style={{ width: 'auto' }}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All Statuses ({orders.length})</option>
              <option value="NEW">NEW</option>
              <option value="ALLOCATED">ALLOCATED</option>
              <option value="PICKED">PICKED</option>
              <option value="PACKED">PACKED</option>
              <option value="SHIPPED">SHIPPED</option>
              <option value="DELIVERED">DELIVERED</option>
              <option value="CANCELLED">CANCELLED</option>
              <option value="RETURNED">RETURNED</option>
            </select>
          </div>

          {canWrite() && (
            <button
              className="btn btn-primary"
              onClick={() => {
                setOrderForm({
                  customerName: 'Direct Retail Customer',
                  location: 'WH-001',
                  notes: 'Direct warehouse store order',
                  lines: [{ productId: products[0]?.id || '', quantity: 1, unitPrice: products[0]?.price || '' }],
                });
                setShowCreateModal(true);
              }}
            >
              + Create Sales Order
            </button>
          )}
        </div>
      </div>

      <div className="card mt-3">
        <div className="table-responsive">
          {loading ? (
            <div className="loading-spinner-container">
              <div className="spinner"></div>
              <p>Loading sales orders...</p>
            </div>
          ) : filteredOrders.length === 0 ? (
            <div className="empty-state">No sales orders found.</div>
          ) : (
            <table className="rims-table">
              <thead>
                <tr>
                  <th>Order #</th>
                  <th>Customer</th>
                  <th>Location</th>
                  <th>Date</th>
                  <th>Items</th>
                  <th>Total Amount</th>
                  <th>Status</th>
                  {canWrite() && <th>Next Valid Step & Actions</th>}
                </tr>
              </thead>
              <tbody>
                {filteredOrders.map((order) => (
                  <tr key={order.id}>
                    <td><strong>{order.orderNumber}</strong></td>
                    <td>{order.customerName || 'Retail Customer'}</td>
                    <td><span className="location-chip">{order.location}</span></td>
                    <td>
                      {order.orderDate ? new Date(order.orderDate).toLocaleDateString() : 'N/A'}
                    </td>
                    <td>{order.lines ? order.lines.length : 0} items</td>
                    <td>
                      <strong>${Number(order.totalAmount || 0).toFixed(2)}</strong>
                    </td>
                    <td>{getStatusBadge(order.status)}</td>
                    {canWrite() && (
                      <td>
                        <div className="order-actions-flex">
                          {order.status === 'NEW' && (
                            <>
                              <button
                                className="btn btn-sm btn-primary"
                                onClick={() => handleAllocate(order.id)}
                                title="Allocate stock from warehouse inventory"
                              >
                                Allocate Stock
                              </button>
                              <button
                                className="btn btn-sm btn-danger"
                                onClick={() => handleCancel(order.id)}
                              >
                                Cancel
                              </button>
                            </>
                          )}

                          {order.status === 'ALLOCATED' && (
                            <>
                              <button
                                className="btn btn-sm btn-yellow"
                                onClick={() => handleUpdateStatus(order.id, 'PICKED')}
                              >
                                Pick Order
                              </button>
                              <button
                                className="btn btn-sm btn-danger"
                                onClick={() => handleCancel(order.id)}
                              >
                                Cancel
                              </button>
                            </>
                          )}

                          {order.status === 'PICKED' && (
                            <>
                              <button
                                className="btn btn-sm btn-teal"
                                onClick={() => handleUpdateStatus(order.id, 'PACKED')}
                              >
                                Pack Order
                              </button>
                              <button
                                className="btn btn-sm btn-danger"
                                onClick={() => handleCancel(order.id)}
                              >
                                Cancel
                              </button>
                            </>
                          )}

                          {order.status === 'PACKED' && (
                            <>
                              <button
                                className="btn btn-sm btn-orange"
                                onClick={() => handleUpdateStatus(order.id, 'SHIPPED')}
                              >
                                Ship Order
                              </button>
                              <button
                                className="btn btn-sm btn-danger"
                                onClick={() => handleCancel(order.id)}
                              >
                                Cancel
                              </button>
                            </>
                          )}

                          {order.status === 'SHIPPED' && (
                            <>
                              <button
                                className="btn btn-sm btn-success"
                                onClick={() => handleUpdateStatus(order.id, 'DELIVERED')}
                              >
                                Mark Delivered
                              </button>
                              <button
                                className="btn btn-sm btn-outline"
                                onClick={() => openReturnModal(order)}
                              >
                                Return
                              </button>
                            </>
                          )}

                          {order.status === 'DELIVERED' && (
                            <button
                              className="btn btn-sm btn-outline"
                              onClick={() => openReturnModal(order)}
                            >
                              Process Return
                            </button>
                          )}

                          {(order.status === 'CANCELLED' || order.status === 'RETURNED') && (
                            <span className="text-muted text-sm">Completed</span>
                          )}
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

      {/* Create Order Modal */}
      {showCreateModal && (
        <div className="modal-backdrop">
          <div className="modal-card modal-large">
            <div className="modal-header">
              <h3>Create New Sales Order</h3>
              <button className="modal-close" onClick={() => setShowCreateModal(false)}>×</button>
            </div>
            <form onSubmit={handleCreateOrderSubmit}>
              <div className="modal-body">
                <div className="form-row">
                  <div className="form-group flex-1">
                    <label>Fulfillment Location *</label>
                    <select
                      className="form-control"
                      value={orderForm.location}
                      onChange={(e) => setOrderForm({ ...orderForm, location: e.target.value })}
                      required
                    >
                      {locations.map((loc) => (
                        <option key={loc.code} value={loc.code}>
                          {loc.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group flex-2">
                    <label>Notes / Customer Reference</label>
                    <input
                      type="text"
                      className="form-control"
                      value={orderForm.notes}
                      onChange={(e) => setOrderForm({ ...orderForm, notes: e.target.value })}
                    />
                  </div>
                </div>

                <div className="lines-section mt-3">
                  <div className="flex-between mb-2">
                    <h4>Order Line Items</h4>
                    <button type="button" className="btn btn-sm btn-outline" onClick={addLine}>
                      + Add Item
                    </button>
                  </div>

                  {orderForm.lines.map((line, idx) => (
                    <div key={idx} className="line-row form-row">
                      <div className="form-group flex-3">
                        <label>Product</label>
                        <select
                          className="form-control"
                          value={line.productId}
                          onChange={(e) => updateLine(idx, 'productId', e.target.value)}
                          required
                        >
                          <option value="">Select a Product</option>
                          {products.map((p) => (
                            <option key={p.id} value={p.id}>
                              {p.sku} - {p.name} (${Number(p.price).toFixed(2)})
                            </option>
                          ))}
                        </select>
                      </div>

                      <div className="form-group flex-1">
                        <label>Qty</label>
                        <input
                          type="number"
                          min="1"
                          className="form-control"
                          value={line.quantity}
                          onChange={(e) => updateLine(idx, 'quantity', e.target.value)}
                          required
                        />
                      </div>

                      <div className="form-group flex-1">
                        <label>Unit Price ($)</label>
                        <input
                          type="number"
                          step="0.01"
                          className="form-control"
                          value={line.unitPrice}
                          onChange={(e) => updateLine(idx, 'unitPrice', e.target.value)}
                          required
                        />
                      </div>

                      <div className="form-group line-action-col">
                        <button
                          type="button"
                          className="btn btn-sm btn-danger mt-4"
                          disabled={orderForm.lines.length === 1}
                          onClick={() => removeLine(idx)}
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
                  onClick={() => setShowCreateModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={creatingOrder}
                >
                  {creatingOrder ? 'Creating Order...' : 'Submit Sales Order'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Return Order Modal */}
      {showReturnModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <h3>Process Order Return</h3>
              <button className="modal-close" onClick={() => setShowReturnModal(false)}>×</button>
            </div>
            <form onSubmit={handleReturnSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Restock Warehouse / Location *</label>
                  <select
                    className="form-control"
                    value={returnData.restockLocation}
                    onChange={(e) => setReturnData({ ...returnData, restockLocation: e.target.value })}
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
                  <label>Return Reason *</label>
                  <input
                    type="text"
                    className="form-control"
                    value={returnData.reason}
                    onChange={(e) => setReturnData({ ...returnData, reason: e.target.value })}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Refund Amount ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    className="form-control"
                    value={returnData.refundAmount}
                    onChange={(e) => setReturnData({ ...returnData, refundAmount: e.target.value })}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowReturnModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={returning}
                >
                  {returning ? 'Restocking Items...' : 'Complete Return'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
