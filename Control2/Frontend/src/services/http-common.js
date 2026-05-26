import axios from 'axios';

const backendServer = import.meta.env.VITE_BACKEND_SERVER;
const backendPort = import.meta.env.VITE_BACKEND_PORT;

const baseURL = (backendServer && backendPort)
  ? `${backendServer}:${backendPort}`
  : (import.meta.env.VITE_API_URL || 'http://localhost:8080');

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Add Authorization header from cookie for every request
api.interceptors.request.use((config) => {
  const cookies = document.cookie.split(';');
  for (const cookie of cookies) {
    const [name, value] = cookie.trim().split('=');
    if (name === 'auth_token') {
      config.headers.Authorization = `Bearer ${value}`;
      break;
    }
  }
  return config;
});

export const apiBaseUrl = baseURL;

export default api;
