import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts';
import client, { getErrorMessage } from '../api/client';
import { useToast } from '../context/ToastContext';

export default function Dashboard() {
  const [kpi, setKpi] = useState(null);
  const [salesTrends, setSalesTrends] = useState([]);
  const [stockByLocation, setStockByLocation] = useState([]);
  const [lowStockAlerts, setLowStockAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  const { showToast } = useToast();

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const [kpiRes, trendsRes, stockRes, lowStockRes] = await Promise.all([
        client.get('/api/reports/kpi'),
        client.get('/api/reports/sales-trends'),
        client.get('/api/inventory/stock'),
        client.get('/api/inventory/low-stock'),
      ]);

      setKpi(kpiRes.data);
      setSalesTrends(trendsRes.data || []);
      setLowStockAlerts(lowStockRes.data || []);

      // Aggregate stock by location for the BarChart
      const locMap = {};
      (stockRes.data || []).forEach((item) => {
        const loc = item.location || 'Unknown';
        locMap[loc] = (locMap[loc] || 0) + (item.qty || 0);
      });
      const locationData = Object.entries(locMap).map(([location, totalQty]) => ({
        location,
        totalQty,
      }));
      setStockByLocation(locationData);
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <div className="loading-spinner-container">
        <div className="spinner"></div>
        <p>Loading dashboard metrics...</p>
      </div>
    );
  }

  return (
    <div className="dashboard-page">
      {/* KPI Cards Grid */}
      <section className="kpi-grid">
        <div className="kpi-card">
          <div className="kpi-icon kpi-icon-blue">💰</div>
          <div className="kpi-content">
            <span className="kpi-label">Total Stock Value</span>
            <span className="kpi-value">
              ${kpi?.totalStockValue ? Number(kpi.totalStockValue).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '0.00'}
            </span>
          </div>
        </div>

        <div className="kpi-card">
          <div className="kpi-icon kpi-icon-purple">📦</div>
          <div className="kpi-content">
            <span className="kpi-label">Total Stock Items</span>
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
            <span className="kpi-label">Overstock Count</span>
            <span className="kpi-value text-warning">{kpi?.overstockCount ?? 0}</span>
          </div>
        </div>

        <div className="kpi-card">
          <div className="kpi-icon kpi-icon-green">🎯</div>
          <div className="kpi-content">
            <span className="kpi-label">Fulfilment Rate</span>
            <span className="kpi-value text-success">
              {kpi?.orderFulfilmentRate != null ? `${Number(kpi.orderFulfilmentRate).toFixed(1)}%` : '0.0%'}
            </span>
          </div>
        </div>

        <div className="kpi-card">
          <div className="kpi-icon kpi-icon-indigo">🛍️</div>
          <div className="kpi-content">
            <span className="kpi-label">Total Orders</span>
            <span className="kpi-value">{kpi?.totalOrders ?? 0}</span>
          </div>
        </div>
      </section>

      {/* Charts Section */}
      <div className="charts-grid">
        <div className="card chart-card">
          <div className="card-header">
            <h3>Sales Trends (Revenue & Orders)</h3>
          </div>
          <div className="chart-wrapper">
            {salesTrends.length === 0 ? (
              <div className="empty-state">No sales trends recorded yet.</div>
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <LineChart data={salesTrends} margin={{ top: 10, right: 25, left: 0, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis dataKey="date" stroke="#6b7280" />
                  <YAxis yAxisId="left" stroke="#3b82f6" />
                  <YAxis yAxisId="right" orientation="right" stroke="#10b981" />
                  <Tooltip />
                  <Legend />
                  <Line
                    yAxisId="left"
                    type="monotone"
                    dataKey="totalSales"
                    name="Sales ($)"
                    stroke="#3b82f6"
                    strokeWidth={2}
                    dot={{ r: 4 }}
                  />
                  <Line
                    yAxisId="right"
                    type="monotone"
                    dataKey="orderCount"
                    name="Order Count"
                    stroke="#10b981"
                    strokeWidth={2}
                    dot={{ r: 4 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        <div className="card chart-card">
          <div className="card-header">
            <h3>Stock Quantity by Location</h3>
          </div>
          <div className="chart-wrapper">
            {stockByLocation.length === 0 ? (
              <div className="empty-state">No stock locations recorded.</div>
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={stockByLocation} margin={{ top: 10, right: 25, left: 0, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis dataKey="location" stroke="#6b7280" />
                  <YAxis stroke="#6b7280" />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="totalQty" name="Units in Stock" fill="#8b5cf6" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      </div>

      {/* Low Stock Alerts Quick Table */}
      <div className="card mt-4">
        <div className="card-header flex-between">
          <div className="flex-align-gap">
            <span className="badge badge-danger">{lowStockAlerts.length} Alerts</span>
            <h3>Low Stock & Reorder Alerts</h3>
          </div>
          <Link to="/inventory" className="btn btn-sm btn-outline">
            Manage All Inventory →
          </Link>
        </div>

        <div className="table-responsive">
          {lowStockAlerts.length === 0 ? (
            <div className="empty-state">All inventory levels are within normal safety limits.</div>
          ) : (
            <table className="rims-table">
              <thead>
                <tr>
                  <th>Product SKU</th>
                  <th>Product Name</th>
                  <th>Location</th>
                  <th>Current Qty</th>
                  <th>Reorder Point</th>
                  <th>Safety Stock</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {lowStockAlerts.slice(0, 8).map((alert) => (
                  <tr key={alert.id}>
                    <td><strong>{alert.product?.sku}</strong></td>
                    <td>{alert.product?.name}</td>
                    <td><span className="location-chip">{alert.location}</span></td>
                    <td>
                      <span className={alert.qty === 0 ? 'text-danger fw-bold' : 'text-warning fw-bold'}>
                        {alert.qty}
                      </span>
                    </td>
                    <td>{alert.reorderPoint}</td>
                    <td>{alert.safetyStock}</td>
                    <td>
                      {alert.qty === 0 ? (
                        <span className="badge badge-danger">STOCKOUT</span>
                      ) : (
                        <span className="badge badge-warning">LOW STOCK</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
