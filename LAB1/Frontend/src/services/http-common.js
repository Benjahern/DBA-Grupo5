import axios from 'axios';
import keycloak from './keycloak.js';
import { useAlert } from '../components/Alerts/useAlert.js';
import { setToken, getToken, clearSession } from './auth.js';

const backendServer = import.meta.env.VITE_BACKEND_SERVER;
const backendPort = import.meta.env.VITE_BACKEND_PORT;
const baseURL = (backendServer && backendPort) ? `${backendServer}:${backendPort}` : (import.meta.env.VITE_API_URL || 'http://localhost:8080');

const { show } = useAlert();

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const apiBaseUrl = baseURL;

api.interceptors.request.use(
  async (config) => {
    // Con esto no bloqueamos el login ni el registro
    const url = config.url || '';
    const isAuthEndpoint = url.includes('/api/auth/login') || url.includes('/api/auth/register');

    // Si la solicitud es para un endpoint de autenticación, no intentamos agregar el token ni refrescarlo, simplemente la dejamos pasar
    if (isAuthEndpoint) {
      return config;
    }

    // Si el usuario está autenticado con Keycloak, intentamos refrescar su token antes de cada solicitud para asegurarnos de que siempre usamos un token válido
    if (keycloak?.authenticated) {
      try {
        await keycloak.updateToken(30);
        config.headers.Authorization = `Bearer ${keycloak.token}`;
      } 
        // Si ocurre un error al intentar refrescar el token de Keycloak, lo registramos pero no bloqueamos la solicitud, ya que podría ser un error temporal o el token aún podría ser válido
        catch (e) {
        console.warn('Error refrescando el token', e);
      }
    }
    // Si no se está usando Keycloak, intentamos obtener el token de localStorage y agregarlo al header de autorización
    else {

      // Intentamos obtener el token de localStorage 
      // Usamos un try-catch para manejar cualquier error que pueda ocurrir al acceder a localStorage 
      try {
        const localToken = typeof globalThis !== 'undefined' ? getToken() : null;
        // Si obtenemos un token, lo agregamos al header de autorización de esta solicitud
        if (localToken) {
          config.headers.Authorization = `Bearer ${localToken}`;
        }
      } 
        // Si ocurre un error al acceder a localStorage, lo registramos pero no bloqueamos la solicitud
        // ya que podría ser un error temporal o el token aún podría ser válido
        // (Al igual que antes)
        catch (error_) { console.debug(error_); 
      }
    }
    return config;
  },
  // Si ocurre un error al configurar la solicitud, simplemente lo rechazamos para que pueda ser manejado por el código que hizo la solicitud
  (error) => { throw error; },
);

// Interceptor de tokens caducados
let isRefreshing = false;
let failedQueue = [];

// Si ocurrió un problema (por ejemplo, falló el intento de refrescar el token) 
// se pasa el error aquí. Si el refresco fue exitoso, se pasa el nuevo token para reintentar las solicitudes fallidas
const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

const tryKeycloakRefresh = async () => {
  if (!keycloak?.authenticated) return null;
  // Fuerza a Keycloak a renovar el token, incluso si aún no ha expirado, para asegurarnos de que obtenemos
  // un nuevo token válido
  await keycloak.updateToken(-1);
  return keycloak.token;
};

const tryLocalRefresh = async () => {
  // Si no hay refresh token, no se puede refrescar
  const refreshToken = localStorage.getItem('refresh_token');
  if (!refreshToken) return null;
  // Intentamos refrescar el token usando el endpoint de refresh del backend
  const response = await axios.post(`${baseURL}/api/auth/refresh`, {
    refresh_token: refreshToken,
  });
  // Si el backend respondió con un nuevo token, lo guardamos y lo retornamos
  // Si no se obtuvo un nuevo token, retornamos null para indicar que el refresco falló
  const newToken = response.data?.token?.access_token;
  const newRefreshToken = response.data?.token?.refresh_token;
  if (!newToken) return null;
  // Guardamos el nuevo token (y refresh token si se proporcionó) en localStorage para futuros intentos
  // También actualizamos el token en nuestro módulo de auth para que cualquier componente que lo use tenga el nuevo token
  setToken(newToken);
  if (newRefreshToken) {
    // Guardamos el nuevo refresh token si el backend lo proporcionó
    localStorage.setItem('refresh_token', newRefreshToken);
  }

  return newToken;
};

// Este es denso así que voy por partes
api.interceptors.response.use(
  // Si la respuesta es exitosa, simplemente pasa
  (response) => response,
  // Si la respuesta es un error, verificamos si es un 401 no autorizado
  async (error) => {
    const originalRequest = error.config;

    // Si el error no es un 401 o ya hemos intentado refrescar este request, no hacemos nada especial, solo rechazamos el error
    if (error.response?.status !== 401 || originalRequest._retry) {
      throw error;
    }

    // No queremos intentar refrescar el token si el error ocurrió en los endpoints de login, registro o refresh
    // porque eso podría causar un bucle infinito de errores
    // Así que simplemente rechazamos el error sin intentar refrescar
    const url = originalRequest.url || '';
    if (url.includes('/api/auth/login') || url.includes('/api/auth/register') || url.includes('/api/auth/refresh')) {
      throw error;
    }

    // Chequeamos si el usuario tiene tokens (ya sea de Keycloak o local) 
    // para decidir si intentamos refrescar o simplemente rechazamos
    const hasAccessToken = !!localStorage.getItem('access_token');
    const hasRefreshToken = !!localStorage.getItem('refresh_token');
    const isKeycloakAuth = keycloak?.authenticated;

    // Si no hay tokens disponibles y el error no ocurrió en un endpoint de autenticación
    // no tiene sentido intentar refrescar, simplemente rechazamos el error
    if (!hasAccessToken && !hasRefreshToken && !isKeycloakAuth) {
      throw error;
    }

    // Si ya estamos intentando refrescar el token, no hacemos nada más que poner esta solicitud en la cola 
    // para que se reintente una vez que el intento de refresco actual termine
    if (isRefreshing) {
      // Retornamos una "promesa" que se resolverá o rechazará una vez que el proceso de refresco termine
      return new Promise((resolve, reject) => {
        // Agregamos esta solicitud a la cola de solicitudes fallidas, junto con sus funciones de resolución y rechazo
        // Esta es la lista que declaramos antes
        failedQueue.push({ resolve, reject });
      })
      // Si el proceso de refresco actual termina exitosamente, esta solicitud se reintentará con el nuevo token
        .then(token => {
          // Si el refresco fue exitoso, el token se pasará aquí y podemos actualizar el header de esta solicitud y reintentarlo
          originalRequest.headers['Authorization'] = `Bearer ${  token}`;
          return api(originalRequest);
        })
        // Si el proceso de refresco falla, esta solicitud se rechazará 
        .catch(err => { throw err; });
    }

    // Marcamos esta solicitud como que ya intentó refrescar para evitar bucles infinitos
    originalRequest._retry = true;
    isRefreshing = true;

    // Aquí es donde intentamos refrescar el token. Primero intentamos con Keycloak, y si eso falla
    // intentamos con el refresh token local
    try {
      // Intentamos refrescar el token usando Keycloak si el usuario está autenticado con Keycloak
      let newToken = null;
      try {
        // Si el usuario está autenticado con Keycloak, intentamos refrescar usando Keycloak
        newToken = await tryKeycloakRefresh();
      }
      // Si el intento falla (por ejemplo, si el token de Keycloak ya expiró y no se pudo refrescar) 
      // simplemente lo registramos y seguimos intentando con el refresh token local 
        catch (kcError) {
        console.warn('Error al intentar refrescar el token de Keycloak', kcError);
      }

      // Si no obtuvimos un nuevo token de Keycloak, intentamos refrescar usando el refresh token local
      if (!newToken) {
        newToken = await tryLocalRefresh();
      }

      // Si después de ambos intentos no tenemos un nuevo token, significa que el refresco falló completamente
      if (!newToken) {
        throw new Error('No se pudo refrescar el token');
      }

      // Si llegamos aquí, significa que el token se refrescó exitosamente (ya sea por Keycloak o localmente)
      processQueue(null, newToken);
      // Quitamos la marca de que estamos refrescando para que futuros errores puedan intentar refrescar de nuevo si es necesario
      isRefreshing = false;

      // Actualizamos el header de esta solicitud original con el nuevo token y la reintentamos
      originalRequest.headers['Authorization'] = `Bearer ${  newToken}`;
      return api(originalRequest);
    } 
      // Si cualquier parte del proceso de refresco falla (ya sea el intento de Keycloak o el intento local), llegamos aquí
      catch (refreshError) {
      console.warn('No se pudo refrescar el token', refreshError);
      
      // Limpiamos cualquier token que pueda estar almacenado localmente 
      processQueue(refreshError, null);

      // Reseteamos el estado de autenticación, lo que efectivamente desconecta al usuario
      isRefreshing = false;
      
      // Limpiamos tokens y datos de usuario almacenados usando nuestra función centralizada
      // para asegurarnos de que el estado de la aplicación y componentes se enteren
      clearSession();

      // Mostramos la alerta de sesión expirada para informar al usuario que su sesión ha terminado 
      // y que será redirigido a la página principal
      show({
        message: 'Tu sesión ha expirado. Serás redirigido a la página principal.',
        severity: 'warning',
        autoHideMs: 4000,
      });

      // Redirigimos al usuario a la página principal después de un breve retraso para que pueda ver la alerta
      if (typeof globalThis !== 'undefined' && !globalThis.location.pathname.includes('/login')) {
        setTimeout(() => {
          globalThis.location.href = '/';
        }, 1500);
      }

      throw refreshError;
    }
  },
);

export default api;
