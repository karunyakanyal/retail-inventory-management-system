import React, { useState, useEffect } from 'react';
import client, { getErrorMessage } from '../api/client';
import { useToast } from '../context/ToastContext';

export default function Reports() {
  const { showToast } = useToast();

  const [kpi, setKpi] = useState(null);
  const [salesTrends, setSalesTrends] = useState([]);
  const [loading, setLoading] = useState(true);
  const [exportingInventory, setExportingInventory] = useState(false);
  const [exportingOrders, setExportingOrders] = useState(false);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const [kpiRes, trendsRes] = await Promise.all([
        client.get('/api/reports/kpi'),
        client.get('/api/reports/sales-trends'),
      ]);
      setKpi(kpiRes.data);
      setSalesTrends(trendsRes.data || []);
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReports();
  }, []);

  const downloadCsv = async (endpoint, filename, setExporting) => {
    setExporting(true);
    try {
      const res = await client.get(endpoint, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'text/csv' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      showToast(`${filename} downloaded successfully`, 'success');
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className="reports-page">
      {/* CSV Downloads Bar */}
      <div className="card filter-card flex-between">
        <div>
          <h3>Enterprise Data Reports & CSV Exports</h3>
          <p className="text-muted">Export clean CSV datasets for ERP auditing and external analysis.</p>
        </div>
        <div className="flex-align-gap">
          <button
            className="btn btn-outline"
            disabled={exportingInventory}
            onClick={() => downloadCsv('/api/reports/export/inventory', 'inventory_report.csv', setExportingInventory)}
          >
            {exportingInventory ? 'Exporting...' : '📥 Export Inventory CSV'}
          </button>
          <button
            className="btn btn-outline"
            disabled={exportingOrders}
            onClick={() => downloadCsv('/api/reports/export/orders', 'orders_report.csv', setExportingOrders)}
          >
            {exportingOrders ? 'Exporting...' : '📥 Export Orders CSV'}
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="kpi-grid mt-3">
        <div className="kpi-card">
          <div className="kpi-icon kpi-icon-blue">💰</div>
          <div className="kpi-content">
            <span className="kpi-label">Total Stock Valuation</span>
            <span className="kpi-value">
              ${kpi?.totalStockValue ? Number(kpi.totalStockValue).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '0.00'}
            </span>
          </div>
        </div>

        <div className="kpi-card">
          <div className="kpi-icon kpi-icon-purple">📦</div>
          <div className="kpi-content">
            <span className="kpi-label">Units on Hand</span>
            <span className="kpi-value">{kpi?.totalStockItems ?? 0}</span>
          </div>
        </div>

        <div className="kpi-card">
          <div className="kpi-icon kpi-icon-red">⚠️</div>
          <div className="kpi-content">
            <span className="kpi-label">Stockout Count</span>
            <span className="kpi-value text-danger">{kpi?.stockoutCount ?? 0}</span>
          </div>
        </div>

        <div className="kpi-card">
          <div className="kpi-icon kpi-icon-yellow">⚡</div>
          <div className="kpi-content">
            <span className="kpi-label">Overstock Alerts</span>
            <span className="kpi-value text-warning">{kpi?.overstockCount ?? 0}</span>
          </div>
        </div>

        <div className="kpi-card">
          <div className="kpi-icon kpi-icon-green">🎯</div>
          <div className="kpi-content">
            <span className="kpi-label">Order Fulfilment Rate</span>
            <span className="kpi-value text-success">
              {kpi?.orderFulfilmentRate != null ? `${Number(kpi.orderFulfilmentRate).toFixed(1)}%` : '0.0%'}
            </span>
          </div>
        </div>

        <div className="kpi-card">
          <div className="kpi-icon kpi-icon-indigo">🛍️</div>
          <div className="kpi-content">
            <span className="kpi-label">Fulfilled Orders</span>
            <span className="kpi-value">{kpi?.fulfilledOrders ?? 0} / {kpi?.totalOrders ?? 0}</span>
          </div>
        </div>
      </div>

      {/* Sales Trends Table */}
      <div className="card mt-4">
        <div className="card-header">
          <h3>Daily Sales Performance History</h3>
        </div>
        <div className="table-responsive">
          {loading ? (
            <div className="loading-spinner-container">
              <div className="spinner"></div>
              <p>Loading sales history...</p>
            </div>
          ) : salesTrends.length === 0 ? (
            <div className="empty-state">No sales trends recorded yet.</div>
          ) : (
            <table className="rims-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Order Count</th>
                  <th>Total Sales Revenue</th>
                  <th>Average Order Value</th>
                </tr>
              </thead>
              <tbody>
                {salesTrends.map((trend, idx) => {
                  const avg = trend.orderCount > 0 ? (Number(trend.totalSales) / trend.orderCount).toFixed(2) : '0.00';
                  return (
                    <tr key={idx}>
                      <td><strong>{trend.date}</strong></td>
                      <td>{trend.orderCount} orders</td>
                      <td><strong>${Number(trend.totalSales || 0).toFixed(2)}</strong></td>
                      <td>${avg}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
