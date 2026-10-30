import axios from 'axios';

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const client = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

client.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('rims_auth');
    if (token) {
      config.headers.Authorization = `Basic ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      sessionStorage.removeItem('rims_auth');
      sessionStorage.removeItem('rims_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export function getErrorMessage(error) {
  if (error.response?.data) {
    const data = error.response.data;
    if (data.validationErrors) {
      return Object.entries(data.validationErrors)
        .map(([field, msg]) => `${field}: ${msg}`)
        .join(', ');
    }
    return data.message || data.error || 'Server request failed';
  }
  return error.message || 'Network error occurred';
}

export default client;
