import React from 'react';
import { useAuth } from '../context/AuthContext';

export default function Header({ pageTitle }) {
  const { user, logout } = useAuth();

  return (
    <header className="app-header">
      <div className="header-left">
        <h1 className="header-title">{pageTitle || 'Retail Inventory Management System'}</h1>
      </div>
      <div className="header-right">
        {user && (
          <div className="user-profile">
            <span className="user-name">{user.fullName || user.username}</span>
            <span className={`role-badge role-${user.role ? user.role.toLowerCase().replace('role_', '') : 'viewer'}`}>
              {user.role}
            </span>
            <button className="btn btn-secondary btn-sm" onClick={logout} title="Log out of RIMS">
              Logout
            </button>
          </div>
        )}
      </div>
    </header>
  );
}
