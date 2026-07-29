// Tras la migración a JWT propio + cookies HttpOnly, los tokens NO se guardan
// en localStorage: el browser los maneja mediante la cookie. Este módulo solo
// conserva la información del usuario y utilidades de suscripción.

const USER_KEY = 'user';
const subscribers = [];

export function setUser(user) {
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  } else {
    localStorage.removeItem(USER_KEY);
  }
  subscribers.forEach((s) => s());
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function subscribe(cb) {
  subscribers.push(cb);
  return () => {
    const idx = subscribers.indexOf(cb);
    if (idx >= 0) subscribers.splice(idx, 1);
  };
}

export function clearSession() {
  setUser(null);
}

export default { setUser, getUser, subscribe, clearSession };
