import React, { useState, useEffect } from 'react';
import client, { getErrorMessage } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Products() {
  const { canWrite, isManager } = useAuth();
  const { showToast } = useToast();

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters & Pagination
  const [name, setName] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [barcode, setBarcode] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Scan state
  const [scanInput, setScanInput] = useState('');
  const [scannedProduct, setScannedProduct] = useState(null);
  const [scanning, setScanning] = useState(false);

  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [formData, setFormData] = useState({
    sku: '',
    name: '',
    barcode: '',
    rfidTag: '',
    description: '',
    price: '',
    costPrice: '',
    categoryId: '',
    active: true,
  });
  const [saving, setSaving] = useState(false);

  // Import state
  const [importing, setImporting] = useState(false);

  const fetchCategories = async () => {
    try {
      const res = await client.get('/api/categories');
      setCategories(res.data || []);
    } catch (err) {
      console.error('Failed to load categories', err);
    }
  };

  const fetchProducts = async (pageNumber = page) => {
    setLoading(true);
    try {
      const params = {
        page: pageNumber,
        size,
      };
      if (name.trim()) params.name = name.trim();
      if (categoryId) params.categoryId = categoryId;
      if (barcode.trim()) params.barcode = barcode.trim();
      if (minPrice) params.minPrice = minPrice;
      if (maxPrice) params.maxPrice = maxPrice;

      const res = await client.get('/api/products', { params });
      if (res.data && res.data.content) {
        setProducts(res.data.content);
        setTotalPages(res.data.totalPages);
        setTotalElements(res.data.totalElements);
      } else if (Array.isArray(res.data)) {
        setProducts(res.data);
        setTotalPages(1);
        setTotalElements(res.data.length);
      }
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    fetchProducts(0);
    setPage(0);
  }, [name, categoryId, barcode, minPrice, maxPrice]);

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
      fetchProducts(newPage);
    }
  };

  const clearFilters = () => {
    setName('');
    setCategoryId('');
    setBarcode('');
    setMinPrice('');
    setMaxPrice('');
    setPage(0);
  };

  // Scan handler
  const handleScan = async (e) => {
    e.preventDefault();
    const code = scanInput.trim();
    if (!code) {
      showToast('Please enter a Barcode or RFID code to scan', 'warning');
      return;
    }
    setScanning(true);
    try {
      const res = await client.get(`/api/scan/${encodeURIComponent(code)}`);
      setScannedProduct(res.data);
      showToast(`Scanned: ${res.data.name} (${res.data.sku})`, 'success');
    } catch (err) {
      setScannedProduct(null);
      showToast(getErrorMessage(err), 'error');
    } finally {
      setScanning(false);
    }
  };

  // Open Create/Edit modal
  const openCreateModal = () => {
    setEditingProduct(null);
    setFormData({
      sku: '',
      name: '',
      barcode: '',
      rfidTag: '',
      description: '',
      price: '',
      costPrice: '',
      categoryId: categories[0]?.id || '',
      active: true,
    });
    setShowModal(true);
  };

  const openEditModal = (p) => {
    setEditingProduct(p);
    setFormData({
      sku: p.sku || '',
      name: p.name || '',
      barcode: p.barcode || '',
      rfidTag: p.rfidTag || '',
      description: p.description || '',
      price: p.price ?? '',
      costPrice: p.costPrice ?? '',
      categoryId: p.categoryId || '',
      active: p.active ?? true,
    });
    setShowModal(true);
  };

  const handleSaveProduct = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...formData,
        price: parseFloat(formData.price),
        costPrice: formData.costPrice ? parseFloat(formData.costPrice) : 0.0,
        categoryId: formData.categoryId ? Number(formData.categoryId) : null,
      };

      if (editingProduct) {
        await client.put(`/api/products/${editingProduct.id}`, payload);
        showToast(`Product "${formData.name}" updated successfully`, 'success');
      } else {
        await client.post('/api/products', payload);
        showToast(`Product "${formData.name}" created successfully`, 'success');
      }
      setShowModal(false);
      fetchProducts(page);
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteProduct = async (id, pName) => {
    if (!window.confirm(`Are you sure you want to delete product "${pName}"?`)) {
      return;
    }
    try {
      await client.delete(`/api/products/${id}`);
      showToast(`Product "${pName}" deleted`, 'success');
      fetchProducts(page);
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    }
  };

  // CSV Export
  const handleExportCsv = async () => {
    try {
      const res = await client.get('/api/products/export', { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'text/csv' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'products.csv');
      document.body.appendChild(link);
      link.click();
      link.remove();
      showToast('Products CSV exported successfully', 'success');
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    }
  };

  // CSV Import
  const handleImportCsv = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const data = new FormData();
    data.append('file', file);
    setImporting(true);
    try {
      const res = await client.post('/api/products/import', data, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      showToast(res.data.message || 'Import successful', 'success');
      fetchProducts(0);
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setImporting(false);
      e.target.value = '';
    }
  };

  return (
    <div className="products-page">
      {/* Barcode / RFID Scanner Banner */}
      <div className="card scan-card">
        <form onSubmit={handleScan} className="scan-form">
          <div className="scan-input-group">
            <span className="scan-icon">📡</span>
            <input
              type="text"
              className="form-control scan-input"
              placeholder="Scan Barcode or RFID Tag (e.g. 789012345601 or RFID-0001)..."
              value={scanInput}
              onChange={(e) => setScanInput(e.target.value)}
            />
            <button type="submit" className="btn btn-primary" disabled={scanning}>
              {scanning ? 'Scanning...' : 'Scan / Lookup'}
            </button>
            {scannedProduct && (
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => setScannedProduct(null)}
              >
                Clear
              </button>
            )}
          </div>
        </form>

        {scannedProduct && (
          <div className="scan-result-box">
            <div className="scan-badge">MATCH FOUND</div>
            <div className="scan-details">
              <h4>{scannedProduct.name}</h4>
              <p>
                <strong>SKU:</strong> {scannedProduct.sku} | <strong>Price:</strong> ${Number(scannedProduct.price).toFixed(2)} |{' '}
                <strong>Barcode:</strong> {scannedProduct.barcode || 'N/A'} | <strong>RFID:</strong> {scannedProduct.rfidTag || 'N/A'} |{' '}
                <strong>Category:</strong> {scannedProduct.categoryName || 'General'}
              </p>
            </div>
          </div>
        )}
      </div>

      {/* Filter and Actions Bar */}
      <div className="card filter-card">
        <div className="filter-grid">
          <div className="form-group">
            <label>Product Name</label>
            <input
              type="text"
              className="form-control"
              placeholder="Search by name..."
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Category</label>
            <select
              className="form-control"
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Barcode</label>
            <input
              type="text"
              className="form-control"
              placeholder="Filter by barcode..."
              value={barcode}
              onChange={(e) => setBarcode(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Price Range</label>
            <div className="flex-align-gap">
              <input
                type="number"
                className="form-control"
                placeholder="Min"
                value={minPrice}
                onChange={(e) => setMinPrice(e.target.value)}
              />
              <span>-</span>
              <input
                type="number"
                className="form-control"
                placeholder="Max"
                value={maxPrice}
                onChange={(e) => setMaxPrice(e.target.value)}
              />
            </div>
          </div>
        </div>

        <div className="filter-actions-row">
          <button className="btn btn-secondary btn-sm" onClick={clearFilters}>
            Reset Filters
          </button>

          <div className="actions-right">
            <button className="btn btn-outline btn-sm" onClick={handleExportCsv}>
              📥 Export CSV
            </button>

            {isManager() && (
              <>
                <label className="btn btn-outline btn-sm cursor-pointer">
                  {importing ? 'Importing...' : '📤 Import CSV'}
                  <input
                    type="file"
                    accept=".csv"
                    style={{ display: 'none' }}
                    onChange={handleImportCsv}
                    disabled={importing}
                  />
                </label>

                <button className="btn btn-primary btn-sm" onClick={openCreateModal}>
                  + Add Product
                </button>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Products Table */}
      <div className="card mt-3">
        <div className="table-responsive">
          {loading ? (
            <div className="loading-spinner-container">
              <div className="spinner"></div>
              <p>Loading products...</p>
            </div>
          ) : products.length === 0 ? (
            <div className="empty-state">No products found matching criteria.</div>
          ) : (
            <table className="rims-table">
              <thead>
                <tr>
                  <th>SKU</th>
                  <th>Product Name</th>
                  <th>Category</th>
                  <th>Price</th>
                  <th>Cost</th>
                  <th>Barcode</th>
                  <th>RFID Tag</th>
                  <th>Status</th>
                  {isManager() && <th>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {products.map((p) => (
                  <tr key={p.id}>
                    <td><strong>{p.sku}</strong></td>
                    <td>{p.name}</td>
                    <td><span className="badge badge-light">{p.categoryName || 'General'}</span></td>
                    <td>${Number(p.price).toFixed(2)}</td>
                    <td>${Number(p.costPrice || 0).toFixed(2)}</td>
                    <td><code>{p.barcode || '-'}</code></td>
                    <td><code>{p.rfidTag || '-'}</code></td>
                    <td>
                      <span className={`badge ${p.active ? 'badge-success' : 'badge-danger'}`}>
                        {p.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    {isManager() && (
                      <td>
                        <div className="action-buttons">
                          <button
                            className="btn btn-sm btn-secondary"
                            onClick={() => openEditModal(p)}
                            title="Edit Product"
                          >
                            Edit
                          </button>
                          <button
                            className="btn btn-sm btn-danger"
                            onClick={() => handleDeleteProduct(p.id, p.name)}
                            title="Delete Product"
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

        {/* Pagination footer */}
        <div className="table-pagination">
          <span className="pagination-info">
            Showing Page {page + 1} of {Math.max(1, totalPages)} ({totalElements} total products)
          </span>
          <div className="pagination-controls">
            <button
              className="btn btn-secondary btn-sm"
              disabled={page === 0}
              onClick={() => handlePageChange(page - 1)}
            >
              Previous
            </button>
            <button
              className="btn btn-secondary btn-sm"
              disabled={page >= totalPages - 1}
              onClick={() => handlePageChange(page + 1)}
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {/* Product Form Modal */}
      {showModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <h3>{editingProduct ? 'Edit Product' : 'Create New Product'}</h3>
              <button className="modal-close" onClick={() => setShowModal(false)}>×</button>
            </div>
            <form onSubmit={handleSaveProduct}>
              <div className="modal-body">
                <div className="form-row">
                  <div className="form-group flex-1">
                    <label>SKU *</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.sku}
                      onChange={(e) => setFormData({ ...formData, sku: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group flex-2">
                    <label>Product Name *</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      required
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group flex-1">
                    <label>Category</label>
                    <select
                      className="form-control"
                      value={formData.categoryId}
                      onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                    >
                      <option value="">Select Category</option>
                      {categories.map((c) => (
                        <option key={c.id} value={c.id}>{c.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group flex-1">
                    <label>Price ($) *</label>
                    <input
                      type="number"
                      step="0.01"
                      className="form-control"
                      value={formData.price}
                      onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group flex-1">
                    <label>Cost Price ($)</label>
                    <input
                      type="number"
                      step="0.01"
                      className="form-control"
                      value={formData.costPrice}
                      onChange={(e) => setFormData({ ...formData, costPrice: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group flex-1">
                    <label>Barcode</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.barcode}
                      onChange={(e) => setFormData({ ...formData, barcode: e.target.value })}
                    />
                  </div>
                  <div className="form-group flex-1">
                    <label>RFID Tag</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.rfidTag}
                      onChange={(e) => setFormData({ ...formData, rfidTag: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Description</label>
                  <textarea
                    className="form-control"
                    rows="2"
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  />
                </div>

                <div className="form-group checkbox-group">
                  <label>
                    <input
                      type="checkbox"
                      checked={formData.active}
                      onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                    />{' '}
                    Active in catalog
                  </label>
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
                  disabled={saving}
                >
                  {saving ? 'Saving...' : 'Save Product'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
