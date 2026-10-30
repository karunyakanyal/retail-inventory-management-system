import React, { useState, useEffect } from 'react';
import client, { getErrorMessage } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Users() {
  const { user: currentUser } = useAuth();
  const { showToast } = useToast();

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  // User Create Modal
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    fullName: '',
    email: '',
    role: 'ROLE_STAFF',
    enabled: true,
  });
  const [saving, setSaving] = useState(false);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await client.get('/api/users');
      setUsers(res.data || []);
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleCreateUser = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await client.post('/api/users', formData);
      showToast(`User "${formData.username}" created successfully`, 'success');
      setShowCreateModal(false);
      fetchUsers();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleToggleActive = async (u) => {
    if (u.username === currentUser?.username) {
      showToast('You cannot deactivate your own administrative account', 'warning');
      return;
    }

    try {
      if (u.enabled) {
        await client.patch(`/api/users/${u.id}/deactivate`);
        showToast(`User "${u.username}" deactivated`, 'info');
      } else {
        await client.patch(`/api/users/${u.id}/activate`);
        showToast(`User "${u.username}" activated`, 'success');
      }
      fetchUsers();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    }
  };

  const handleRoleChange = async (userId, newRole) => {
    try {
      await client.patch(`/api/users/${userId}/role?role=${newRole}`);
      showToast(`Role updated to ${newRole}`, 'success');
      fetchUsers();
    } catch (err) {
      showToast(getErrorMessage(err), 'error');
    }
  };

  return (
    <div className="users-page">
      <div className="card filter-card flex-between">
        <div>
          <h3>Enterprise Role-Based Access Control (RBAC)</h3>
          <p className="text-muted">Manage system users, assigned roles, and account active statuses.</p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => {
            setFormData({
              username: '',
              password: '',
              fullName: '',
              email: '',
              role: 'ROLE_STAFF',
              enabled: true,
            });
            setShowCreateModal(true);
          }}
        >
          + Add New User
        </button>
      </div>

      <div className="card mt-3">
        <div className="table-responsive">
          {loading ? (
            <div className="loading-spinner-container">
              <div className="spinner"></div>
              <p>Loading users...</p>
            </div>
          ) : users.length === 0 ? (
            <div className="empty-state">No users found.</div>
          ) : (
            <table className="rims-table">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Full Name</th>
                  <th>Email</th>
                  <th>Assigned Role</th>
                  <th>Account Status</th>
                  <th>Role Assignment</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td><strong>{u.username}</strong></td>
                    <td>{u.fullName || '-'}</td>
                    <td>{u.email || '-'}</td>
                    <td>
                      <span className={`role-badge role-${u.role ? u.role.toLowerCase().replace('role_', '') : 'viewer'}`}>
                        {u.role}
                      </span>
                    </td>
                    <td>
                      <span className={`badge ${u.enabled ? 'badge-success' : 'badge-danger'}`}>
                        {u.enabled ? 'Active' : 'Deactivated'}
                      </span>
                    </td>
                    <td>
                      <select
                        className="form-control form-control-sm"
                        value={u.role}
                        disabled={u.username === currentUser?.username}
                        onChange={(e) => handleRoleChange(u.id, e.target.value)}
                      >
                        <option value="ROLE_ADMIN">ROLE_ADMIN</option>
                        <option value="ROLE_MANAGER">ROLE_MANAGER</option>
                        <option value="ROLE_STAFF">ROLE_STAFF</option>
                        <option value="ROLE_VIEWER">ROLE_VIEWER</option>
                      </select>
                    </td>
                    <td>
                      <button
                        className={`btn btn-sm ${u.enabled ? 'btn-danger' : 'btn-success'}`}
                        disabled={u.username === currentUser?.username}
                        onClick={() => handleToggleActive(u)}
                      >
                        {u.enabled ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Create User Modal */}
      {showCreateModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <h3>Create New System User</h3>
              <button className="modal-close" onClick={() => setShowCreateModal(false)}>×</button>
            </div>
            <form onSubmit={handleCreateUser}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Username *</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.username}
                    onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Password *</label>
                  <input
                    type="password"
                    className="form-control"
                    placeholder="Min 6 characters"
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    required
                  />
                </div>

                <div className="form-row">
                  <div className="form-group flex-1">
                    <label>Full Name</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.fullName}
                      onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                    />
                  </div>
                  <div className="form-group flex-1">
                    <label>Email</label>
                    <input
                      type="email"
                      className="form-control"
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Role *</label>
                  <select
                    className="form-control"
                    value={formData.role}
                    onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                    required
                  >
                    <option value="ROLE_ADMIN">ROLE_ADMIN (Full control)</option>
                    <option value="ROLE_MANAGER">ROLE_MANAGER (Catalog, POs, Inventory)</option>
                    <option value="ROLE_STAFF">ROLE_STAFF (Stock adjustments, Transfers, Orders)</option>
                    <option value="ROLE_VIEWER">ROLE_VIEWER (Read-only)</option>
                  </select>
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
                  disabled={saving}
                >
                  {saving ? 'Creating User...' : 'Create User'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
