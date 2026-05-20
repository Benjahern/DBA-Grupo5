<template>
  <div class="login-wrapper">
    <div class="login-card">

      <h2 class="title">CREA TU CUENTA AHORA</h2>

      <form @submit.prevent="submit" class="form-grid">

        <!-- USERNAME -->
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

          <!-- PASSWORD -->
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

const router = useRouter();

const username = ref('');
const password = ref('');
const loading = ref(false);
const msg = ref(null);

// Coordenadas seleccionadas
const latitude = ref(null);
const longitude = ref(null);

/**
 * Navega al login
 */
const goToLogin = () => {
  router.push('/login');
};

/**
 * Recibe ubicación desde LocationPicker
 */
const handleLocationSelected = (location) => {

  latitude.value = location.latitude;
  longitude.value = location.longitude;

  console.log('Ubicación recibida desde hijo:', {
    lat: latitude.value,
    lng: longitude.value
  });

};

/**
 * Submit registro
 */
const submit = async () => {

  msg.value = null;
  loading.value = true;

  try {

    // Payload temporal
    console.log({
      username: username.value,
      password: password.value,
      latitude: latitude.value,
      longitude: longitude.value
    });

    msg.value = 'Usuario registrado correctamente';

    setTimeout(() => {
      router.push('/login');
    }, 1000);

  } catch (err) {

    console.error(err);
    msg.value = 'Ocurrió un error al registrar el usuario';

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