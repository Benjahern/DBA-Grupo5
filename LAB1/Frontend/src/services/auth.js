const STORAGE_KEY = 'app_token';

// Lista de funciones de callback que se llamarán cada vez que el token cambie (setToken se llame)
const subscribers = [];

// Parsea un JWT para extraer su payload. Si el token no es válido, devuelve null.
function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => {
          const hex = (`00${c.codePointAt(0).toString(16)}`).slice(-2);
          return `%${hex}`;
        })
        .join(''),
    );
    return JSON.parse(jsonPayload);
  } 
    // Si el token no es un JWT válido o no tiene un payload decodificable, atrapamos el error y devolvemos null
    catch (error_) { console.debug(error_); return null;
  }
}


export function setToken(token) {
  if (token) {
    localStorage.setItem(STORAGE_KEY, token);
  } else {
    localStorage.removeItem(STORAGE_KEY);
  }
  subscribers.forEach((s) => s());
}

// Devuelve el token de acceso almacenado en localStorage, o null si no hay ninguno
export function getToken() {
  return localStorage.getItem(STORAGE_KEY) || localStorage.getItem('access_token');
}

// Devuelve la información del usuario decodificada del token JWT, o null si no hay token o el token no es válido
export function getUser() {
  const token = getToken();
  if (!token) return null;
  return parseJwt(token);
}

// Permite a los componentes suscribirse a cambios en el token. Devuelve una función de limpieza para cancelar la suscripción.
export function subscribe(cb) {
  subscribers.push(cb);
  return () => {
    const idx = subscribers.indexOf(cb);
    if (idx >= 0) subscribers.splice(idx, 1);
  };
}

// Limpia todos los tokens y datos de sesión almacenados
export function clearSession() {
  setToken(null); // Esto limpia 'app_token' y avisa a los subscribers
  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
  localStorage.removeItem('user');
}

export default { setToken, getToken, getUser, subscribe, clearSession };
