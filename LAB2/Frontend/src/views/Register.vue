<template>
  <div class="login-wrapper">
    <div class="login-card">
      <h2 class="title">CREA TU CUENTA AHORA</h2>

      <form @submit.prevent="submit" class="form-grid">
        <div class="input-row">
          <div class="input-group">
            <label for="register-name">Nombre</label>
            <div class="input-container">
              <span class="icon" aria-hidden>
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-person" viewBox="0 0 16 16"><path d="M8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6m2-3a2 2 0 1 1-4 0 2 2 0 0 1 4 0m4 8c0 1-1 1-1 1H3s-1 0-1-1 1-4 6-4 6 3 6 4m-1-.004c-.001-.246-.154-.986-.832-1.664C11.516 10.68 10.289 10 8 10s-3.516.68-4.168 1.332c-.678.678-.83 1.418-.832 1.664z"/></svg>
              </span>
              <input id="register-name" v-model="name" placeholder="tu nombre" required />
            </div>
          </div>

          <div class="input-group">
            <label for="register-identifier">Email</label>
            <div class="input-container">
              <span class="icon" aria-hidden>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 8l9 6 9-6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M21 8v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </span>
              <input id="register-identifier" type="email" v-model="identifier" placeholder="tu email" required />
            </div>
          </div>
        </div>

        <div class="input-row">
          <div class="input-group">
            <label for="register-password">Contraseña</label>
            <div class="input-container">
              <span class="icon" aria-hidden>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/><path d="M7 11V8a5 5 0 0 1 10 0v3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </span>
              <input id="register-password" type="password" v-model="password" placeholder="tu contraseña" required />
            </div>
          </div>

          <div class="input-group">
            <label for="register-password-confirm">Confirmar contraseña</label>
            <div class="input-container">
              <span class="icon" aria-hidden>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/><path d="M7 11V8a5 5 0 0 1 10 0v3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </span>
              <input id="register-password-confirm" type="password" v-model="passwordConfirm" placeholder="confirma tu contraseña" required />
            </div>
          </div>
        </div>
        
        <div class="actions">
          <button class="btn-acceder" type="submit" :disabled="loading" style="width: 100%; justify-content: center">
            {{ loading ? 'Registrando...' : 'Crear cuenta' }}
          </button>
          <button type="button" class="btn-acceder" style="width: 100%; justify-content: center" @click="goToLogin">
            ¿Ya tienes cuenta? Inicia sesión
          </button>
        </div>
      </form>

      <div v-if="msg" class="form-message" style="white-space: pre-wrap;">{{ msg }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../services/http-common';
import { useAlert } from '../components/Alerts/useAlert';
import { setUser } from '../services/auth.js';

const router = useRouter();
const { show } = useAlert();

const identifier = ref('');
const password = ref('');
const passwordConfirm = ref('');
const loading = ref(false);
const msg = ref(null);
const name = ref('');

// Tras la migración, los tokens viajan solo en cookies HttpOnly.
const storeAuthData = (data) => {
  if (!data) return;
  if (data.user) {
    setUser(data.user);
  } else if (data.name || data.email) {
    setUser({ name: data.name || data.email, email: data.email });
  }
};

const getregisterError = (err) => {
  const backendError = err.response?.data?.error || err.response?.data?.message || null;

  if (err.response?.status === 409) {
    show({
      message: 'El usuario o correo ya se encuentra registrado. Por favor, intenta iniciar sesión.',
      severity: 'warning',
      autoHideMs: 5000
    });
    return 'El usuario o correo ya se encuentra registrado.';
  } 
  if (err.response?.status === 400) {
    if (backendError) {
      const isDuplicate = /status:\s*409|already exists|ya existe|duplicate/i.test(String(backendError));
      const message = isDuplicate
        ? 'El correo ya esta registrado. Intenta con otro correo o inicia sesion.'
        : `Error de registro: ${backendError}`;
      show({
        message,
        severity: isDuplicate ? 'warning' : 'error',
        autoHideMs: 5000
      });
      return message;
    }

    show({
      message: 'Los datos ingresados son inválidos. Verifica tu información.',
      severity: 'error',
      autoHideMs: 4000
    });
    return 'Los datos ingresados son inválidos.';
  }
  show({
    message: 'Ocurrió un error al intentar crear la cuenta. Inténtalo de nuevo.',
    severity: 'error',
    autoHideMs: 4000
  });
  return 'Ocurrió un error inesperado al registrar el usuario.';
};

const goToLogin = () => {
  router.push('/login');
};

const submit = async () => {
  msg.value = null;

  const payload = {
    email: identifier.value.trim(),
    name: name.value.trim(),
    password: password.value,
  };

  if (password.value !== passwordConfirm.value) {
    msg.value = 'Las contraseñas no coinciden.';
    show({ message: 'Las contraseñas no coinciden.', severity: 'warning', autoHideMs: 4000 });
    return;
  }

  if (!payload.email || !payload.name || !payload.password) {
    msg.value = 'Completa nombre, email y contraseña.';
    show({ message: 'Completa nombre, email y contraseña.', severity: 'warning', autoHideMs: 4000 });
    return;
  }

  loading.value = true;
  
  try {
    const resp = await api.post('/api/auth/register', payload);

    const data = resp.data;
    const displayName = data?.user?.name || data?.name || identifier.value;

    storeAuthData(data);

    // El backend emite las cookies HttpOnly con /register, así que la sesión
    // ya está abierta. Hacemos login explícito solo si la respuesta no incluye
    // un perfil de usuario (caso borde).
    if (!data?.user) {
      const loginResp = await api.post('/api/auth/login', {
        email: payload.email,
        password: payload.password,
      });
      storeAuthData(loginResp.data);
    }

    show({ message: `¡Bienvenido ${displayName}! Cuenta creada exitosamente`, severity: 'success', autoHideMs: 3500 });

    setTimeout(() => { router.push('/home'); }, 500);

  } catch (err) {
    console.error(err);
    msg.value = getregisterError(err);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
/* Contenedor principal para centrar la tarjeta */
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f4f4f4; /* Color de fondo general de la página */
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Tarjeta de Login */
.login-card {
  background-color: #3b424d; /* Color azul/gris oscuro de la imagen */
  padding: 5% 6%;
  border-radius: 0.4rem;
  box-shadow: 0 0.4rem 1rem rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 90vw;
  color: white;
}

/* Título */
.title {
  font-size: 1.25rem;
  font-weight: 700;
  margin-top: 0;
  margin-bottom: 3%;
  letter-spacing: 0.5px;
}

/* Fila de inputs (Grid de 2 columnas) */
.input-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2.5%;
  margin-bottom: 3%;
}

.input-group {
  display: flex;
  flex-direction: column;
}

.input-group label {
  font-size: 0.9rem;
  margin-bottom: 1.2%;
}

/* Contenedor del input y el ícono */
.input-container {
  position: relative;
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 0.2rem;
}

.input-container input {
  width: 100%;
  padding: 2.5% 2%;
  border: none;
  background: transparent;
  font-size: 0.95rem;
  color: #333;
  outline: none;
}

.input-container .icon {
  padding: 0 2%;
  color: #666;
  font-size: 1.1rem;
}

/* Botones y Checkbox inferior */
.btn-acceder {
  background-color: white;
  color: #555;
  border: none;
  padding: 12px 30px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  border-radius: 2px;
  margin-bottom: 20px;
  transition: background-color 0.2s;
}

.btn-acceder:hover {
  background-color: #f0f0f0;
}

/* Responsividad para pantallas pequeñas */
@media (max-width: 600px) {
  .input-row {
    grid-template-columns: 1fr; /* Cambia a 1 columna en móviles */
  }
}
</style>