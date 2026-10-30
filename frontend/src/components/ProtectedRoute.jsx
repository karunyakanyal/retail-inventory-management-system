import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children, adminOnly = false }) {
  const { user, loading, isAdmin } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="loading-spinner-container">
        <div className="spinner"></div>
        <p>Loading application...</p>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (adminOnly && !isAdmin()) {
    return (
      <div className="forbidden-container card">
        <h2>Access Denied</h2>
        <p>You need administrator permissions (ROLE_ADMIN) to access this page.</p>
      </div>
    );
  }

  return children;
}
