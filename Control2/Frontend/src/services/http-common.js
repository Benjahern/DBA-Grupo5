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

export const apiBaseUrl = baseURL;

export default api;
