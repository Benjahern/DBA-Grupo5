import axios from 'axios';
import { useAlert } from '../components/Alerts/useAlert.js';
import { clearSession } from './auth.js';

const backendServer = import.meta.env.VITE_BACKEND_SERVER;
const backendPort = import.meta.env.VITE_BACKEND_PORT;
const baseURL = (backendServer && backendPort)
  ? `${backendServer}:${backendPort}`
  : (import.meta.env.VITE_API_URL || 'http://localhost:8080');

const { show } = useAlert();

const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const apiBaseUrl = baseURL;

// Los tokens viajan exclusivamente en cookies HttpOnly: el browser las envía
// automáticamente con cada request gracias a `withCredentials: true`.

api.interceptors.request.use(
  (config) => config,
  (error) => { throw error; },
);

// Manejo de 401 — intentar refresh una vez y reintentar el request original.
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error); else prom.resolve();
  });
  failedQueue = [];
};

const tryRefresh = async () => {
  // El browser envía la cookie `refresh_token` automáticamente.
  await axios.post(`${baseURL}/api/auth/refresh`, {}, {
    withCredentials: true,
  });
  return true;
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status !== 401 || originalRequest._retry) {
      throw error;
    }

    const url = originalRequest.url || '';
    if (
      url.includes('/api/auth/login') ||
      url.includes('/api/auth/register') ||
      url.includes('/api/auth/refresh') ||
      url.includes('/api/auth/logout')
    ) {
      throw error;
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then(() => api(originalRequest));
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      await tryRefresh();
      processQueue(null);
      isRefreshing = false;
      return api(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError);
      isRefreshing = false;
      clearSession();
      show({
        message: 'Tu sesión ha expirado. Serás redirigido a la página principal.',
        severity: 'warning',
        autoHideMs: 4000,
      });
      if (typeof globalThis !== 'undefined' && !globalThis.location.pathname.includes('/login')) {
        setTimeout(() => { globalThis.location.href = '/'; }, 1500);
      }
      throw refreshError;
    }
  },
);

export default api;
