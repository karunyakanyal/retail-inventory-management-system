import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { getErrorMessage } from '../api/client';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const { login } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/';

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username.trim() || !password) {
      setError('Please provide both username and password');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const user = await login(username.trim(), password);
      showToast(`Welcome back, ${user.fullName || user.username}!`, 'success');
      navigate(from, { replace: true });
    } catch (err) {
      const msg = err.response?.status === 401
        ? 'Invalid username or password'
        : getErrorMessage(err);
      setError(msg);
      showToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const fillQuickLogin = (u, p) => {
    setUsername(u);
    setPassword(p);
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-header">
          <div className="login-logo">RIMS</div>
          <h2>Retail Inventory Management</h2>
          <p className="login-subtitle">Sign in with your enterprise credentials</p>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              className="form-control"
              placeholder="e.g. admin"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoFocus
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              className="form-control"
              placeholder="Enter password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={loading}
          >
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>

        <div className="quick-login-section">
          <p className="quick-login-title">Quick Demo Sign-In (Password: Pass@123)</p>
          <div className="quick-login-chips">
            <button
              type="button"
              className="chip chip-admin"
              onClick={() => fillQuickLogin('admin', 'Pass@123')}
            >
              Admin
            </button>
            <button
              type="button"
              className="chip chip-manager"
              onClick={() => fillQuickLogin('manager', 'Pass@123')}
            >
              Manager
            </button>
            <button
              type="button"
              className="chip chip-staff"
              onClick={() => fillQuickLogin('staff', 'Pass@123')}
            >
              Staff
            </button>
            <button
              type="button"
              className="chip chip-viewer"
              onClick={() => fillQuickLogin('viewer', 'Pass@123')}
            >
              Viewer (Read-only)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
