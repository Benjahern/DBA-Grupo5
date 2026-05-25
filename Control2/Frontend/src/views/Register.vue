<template>
  <div class="login-wrapper">
    <div class="login-card">

      <h2 class="title">CREA TU CUENTA AHORA</h2>

      <form @submit.prevent="submit" class="form-grid">

        <!-- USERNAME Y EMAIL -->
        <div class="input-row">

          <div class="input-group">
            <label for="register-username">
              Nombre de usuario
            </label>

            <div class="input-container">
              <span class="icon" aria-hidden>
                <svg xmlns="http://www.w3.org/2000/svg"
                     width="16"
                     height="16"
                     fill="currentColor"
                     class="bi bi-person"
                     viewBox="0 0 16 16">
                  <path d="M8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6m2-3a2 2 0 1 1-4 0 2 2 0 0 1 4 0m4 8c0 1-1 1-1 1H3s-1 0-1-1 1-4 6-4 6 3 6 4m-1-.004c-.001-.246-.154-.986-.832-1.664C11.516 10.68 10.289 10 8 10s-3.516.68-4.168 1.332c-.678.678-.83 1.418-.832 1.664z"/>
                </svg>
              </span>

              <input
                id="register-username"
                type="text"
                v-model="username"
                placeholder="tu nombre de usuario"
                required
              />
            </div>
          </div>

          <div class="input-group">
            <label for="register-email">
              Correo electrónico
            </label>

            <div class="input-container">
              <span class="icon" aria-hidden>
                <svg xmlns="http://www.w3.org/2000/svg"
                     width="16"
                     height="16"
                     fill="currentColor"
                     viewBox="0 0 16 16">
                  <path d="M0 4a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V4zm2-1a1 1 0 0 0-1 1v.217l7 4.2 7-4.2V4a1 1 0 0 0-1-1H2zm13 2.383l-4.758 2.855L15 11.114v-5.73zm-.034 6.878L9.643 8.03 8 12.317l1.643-1.484zm3.574-6.671a.533.533 0 0 0-.493.12l-3.522 2.09 2.402 1.453 4.29-2.088a.533.533 0 0 0-.277-.469l-3.18-.004z"/>
                </svg>
              </span>

              <input
                id="register-email"
                type="email"
                v-model="email"
                placeholder="tu correo electrónico"
                required
              />
            </div>
          </div>

        </div>

        <!-- PASSWORD -->
        <div class="input-row">

          <div class="input-group">
            <label for="register-password">
              Contraseña
            </label>

            <div class="input-container">
              <span class="icon" aria-hidden>
                <svg width="16"
                     height="16"
                     viewBox="0 0 24 24"
                     fill="none"
                     xmlns="http://www.w3.org/2000/svg">
                  <rect x="3"
                        y="11"
                        width="18"
                        height="11"
                        rx="2"
                        stroke="currentColor"
                        stroke-width="1.2"
                        stroke-linecap="round"
                        stroke-linejoin="round"/>
                  <path d="M7 11V8a5 5 0 0 1 10 0v3"
                        stroke="currentColor"
                        stroke-width="1.2"
                        stroke-linecap="round"
                        stroke-linejoin="round"/>
                </svg>
              </span>

              <input
                id="register-password"
                type="password"
                v-model="password"
                placeholder="tu contraseña"
                required
              />
            </div>
          </div>

        </div>

        <!-- LOCATION PICKER -->
        <div class="input-row">

          <div class="input-group full-width">

            <label>
              Selecciona tu ubicación
            </label>

            <LocationPicker
              @location-selected="handleLocationSelected"
            />

          </div>

        </div>

        <!-- BOTONES -->
        <div class="actions">

          <button
            class="btn-acceder"
            type="submit"
            :disabled="loading"
            style="width: 100%; justify-content: center"
          >
            {{ loading ? 'Registrando...' : 'Crear cuenta' }}
          </button>

          <button
            type="button"
            class="btn-acceder"
            style="width: 100%; justify-content: center"
            @click="goToLogin"
          >
            ¿Ya tienes cuenta? Inicia sesión
          </button>

        </div>

      </form>

      <div
        v-if="msg"
        class="form-message"
        style="white-space: pre-wrap;"
      >
        {{ msg }}
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import LocationPicker from '../components/Maps/LocationPicker.vue';
import { register } from '../services/auth.js';
import { useAlert } from '../components/Alerts/useAlert.js';

const router = useRouter();
const { show } = useAlert();

const username = ref('');
const email = ref('');
const password = ref('');
const loading = ref(false);
const msg = ref(null);

const latitude = ref(null);
const longitude = ref(null);

const getregisterError = (err) => {
  const backendError = err.response?.data?.error || err.response?.data?.message || null;

  if (err.response?.status === 409) {
    show({
      message: 'El usuario ya se encuentra registrado. Por favor, intenta iniciar sesión.',
      severity: 'warning',
      autoHideMs: 5000
    });
    return 'El usuario ya se encuentra registrado.';
  }
  if (err.response?.status === 400) {
    if (backendError) {
      show({
        message: `Error de registro: ${backendError}`,
        severity: 'error',
        autoHideMs: 5000
      });
      return `Error de registro: ${backendError}`;
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

const handleLocationSelected = (location) => {
  latitude.value = location.latitude;
  longitude.value = location.longitude;
};

const submit = async () => {
  msg.value = null;

  if (!username.value.trim() || !password.value || !email.value.trim()) {
    msg.value = 'Completa usuario, email y contraseña.';
    show({ message: 'Completa usuario, email y contraseña.', severity: 'warning', autoHideMs: 4000 });
    return;
  }

  if (latitude.value == null || longitude.value == null) {
    msg.value = 'Selecciona una ubicación en el mapa.';
    show({ message: 'Selecciona una ubicación en el mapa.', severity: 'warning', autoHideMs: 4000 });
    return;
  }

  const payload = {
    username: username.value.trim(),
    email: email.value.trim(),
    password: password.value,
    latitude: latitude.value,
    longitude: longitude.value,
  };

  loading.value = true;

  try {
    await register(payload);

    show({
      message: 'Cuenta creada exitosamente. Iniciando sesión...',
      severity: 'success',
      autoHideMs: 3000
    });

    setTimeout(() => { router.push('/dashboard'); }, 500);
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

.full-width {
  width: 100%;
}

/* Responsividad para pantallas pequeñas */
@media (max-width: 600px) {
  .input-row {
    grid-template-columns: 1fr; /* Cambia a 1 columna en móviles */
  }
}
</style>