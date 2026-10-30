import React, { createContext, useContext, useState, useEffect } from 'react';
import client from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const cached = sessionStorage.getItem('rims_user');
    return cached ? JSON.parse(cached) : null;
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = sessionStorage.getItem('rims_auth');
    if (token) {
      client
        .get('/api/auth/me')
        .then((res) => {
          const userData = {
            username: res.data.username,
            role: res.data.role,
            fullName: res.data.fullName,
            email: res.data.email,
          };
          setUser(userData);
          sessionStorage.setItem('rims_user', JSON.stringify(userData));
        })
        .catch(() => {
          sessionStorage.removeItem('rims_auth');
          sessionStorage.removeItem('rims_user');
          setUser(null);
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (username, password) => {
    const token = btoa(`${username}:${password}`);
    const res = await client.get('/api/auth/me', {
      headers: {
        Authorization: `Basic ${token}`,
      },
    });

    const userData = {
      username: res.data.username,
      role: res.data.role,
      fullName: res.data.fullName,
      email: res.data.email,
    };

    sessionStorage.setItem('rims_auth', token);
    sessionStorage.setItem('rims_user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const logout = () => {
    sessionStorage.removeItem('rims_auth');
    sessionStorage.removeItem('rims_user');
    setUser(null);
    window.location.href = '/login';
  };

  const isAdmin = () => user?.role === 'ROLE_ADMIN';
  const isManager = () => user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_MANAGER';
  const isStaff = () => user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_MANAGER' || user?.role === 'ROLE_STAFF';
  const isViewer = () => user?.role === 'ROLE_VIEWER';
  const canWrite = () => user && user.role !== 'ROLE_VIEWER';

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        logout,
        isAdmin,
        isManager,
        isStaff,
        isViewer,
        canWrite,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
