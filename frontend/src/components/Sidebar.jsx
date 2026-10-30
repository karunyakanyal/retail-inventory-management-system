import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Sidebar() {
  const { isAdmin, user } = useAuth();

  return (
    <aside className="app-sidebar">
      <div className="sidebar-brand">
        <div className="brand-logo">RIMS</div>
        <div className="brand-text">
          <h2>Retail Inventory</h2>
          <span className="brand-sub">Enterprise HCL</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        <NavLink
          to="/"
          end
          className={({ isActive }) => `nav-item ${isActive ? 'nav-item-active' : ''}`}
        >
          <span className="nav-icon">📊</span>
          <span className="nav-label">Dashboard</span>
        </NavLink>

        <NavLink
          to="/products"
          className={({ isActive }) => `nav-item ${isActive ? 'nav-item-active' : ''}`}
        >
          <span className="nav-icon">🏷️</span>
          <span className="nav-label">Products & Catalog</span>
        </NavLink>

        <NavLink
          to="/inventory"
          className={({ isActive }) => `nav-item ${isActive ? 'nav-item-active' : ''}`}
        >
          <span className="nav-icon">📦</span>
          <span className="nav-label">Stock & Inventory</span>
        </NavLink>

        <NavLink
          to="/transfers"
          className={({ isActive }) => `nav-item ${isActive ? 'nav-item-active' : ''}`}
        >
          <span className="nav-icon">🔄</span>
          <span className="nav-label">Transfers</span>
        </NavLink>

        <NavLink
          to="/orders"
          className={({ isActive }) => `nav-item ${isActive ? 'nav-item-active' : ''}`}
        >
          <span className="nav-icon">🛒</span>
          <span className="nav-label">Sales Orders</span>
        </NavLink>

        <NavLink
          to="/suppliers"
          className={({ isActive }) => `nav-item ${isActive ? 'nav-item-active' : ''}`}
        >
          <span className="nav-icon">🏭</span>
          <span className="nav-label">Suppliers & POs</span>
        </NavLink>

        <NavLink
          to="/reports"
          className={({ isActive }) => `nav-item ${isActive ? 'nav-item-active' : ''}`}
        >
          <span className="nav-icon">📈</span>
          <span className="nav-label">Reports & Analytics</span>
        </NavLink>

        {isAdmin() && (
          <NavLink
            to="/users"
            className={({ isActive }) => `nav-item ${isActive ? 'nav-item-active' : ''}`}
          >
            <span className="nav-icon">👥</span>
            <span className="nav-label">Users & RBAC</span>
          </NavLink>
        )}
      </nav>

      <div className="sidebar-footer">
        <div className="user-summary">
          <div className="user-avatar">{user?.username?.[0]?.toUpperCase() || 'U'}</div>
          <div className="user-details">
            <span className="user-name-truncated">{user?.fullName || user?.username}</span>
            <span className="user-role-label">{user?.role?.replace('ROLE_', '')}</span>
          </div>
        </div>
      </div>
    </aside>
  );
}
