import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';

export default function Layout() {
  const location = useLocation();

  const getPageTitle = (path) => {
    if (path === '/') return 'Operational Dashboard';
    if (path.startsWith('/products')) return 'Product Catalog & Barcode Scanner';
    if (path.startsWith('/inventory')) return 'Inventory Stock Levels & Adjustments';
    if (path.startsWith('/transfers')) return 'Inter-Location Stock Transfers';
    if (path.startsWith('/orders')) return 'Sales Orders & Fulfillment';
    if (path.startsWith('/suppliers')) return 'Suppliers & Purchase Orders';
    if (path.startsWith('/reports')) return 'Reports, KPIs & CSV Exports';
    if (path.startsWith('/users')) return 'User Management & Access Control';
    return 'Retail Inventory Management System';
  };

  return (
    <div className="rims-app-layout">
      <Sidebar />
      <div className="rims-main-content">
        <Header pageTitle={getPageTitle(location.pathname)} />
        <main className="page-body">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
