<template>
  <!-- Estructura de la tarjeta de login -->
  <div class="login-wrapper">
    <div class="login-card">
      <h2 class="title">ACCEDA A SU CUENTA</h2>

      <!-- Formulario de login -->
      <form @submit.prevent="submit" class="form-grid">
        <!-- Fila de inputs con íconos -->
        <div class="input-row">
          <!-- Input de correo electrónico con ícono -->
          <div class="input-group">
            <label for="login-identifier">Correo electrónico</label>
            <div class="input-container">
              <!-- La entrada se guarda en la variable 'identifier' -->
                <input 
                id="login-identifier" 
                type="text" 
                placeholder="Correo electrónico" 
                v-model="identifier"
                required
              />
              <span class="icon">👤</span>
            </div>
          </div>

          <!-- Input de contraseña con ícono -->
          <div class="input-group">
            <label for="login-password">Contraseña</label>
            <div class="input-container">
              <!-- La entrada se guarda en la variable 'password' -->
              <input 
                id="login-password" 
                type="password" 
                placeholder="Contraseña" 
                v-model="password"
                required
              />
              <span class="icon">🔒</span>
            </div>
          </div>
        </div>
        
        <div class="actions">
          <button type="submit" class="btn-acceder">
            {{ loading ? 'Entrando...' : 'Ingresar' }}
          </button>
          <button type="button" class="btn-acceder" style="justify-content: center" @click="goToRegister">
            Crear cuenta
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
import { login } from '../services/auth.js';
import { useAlert } from '../components/Alerts/useAlert.js';

const router = useRouter();
const { show } = useAlert();
const identifier = ref('');
const password = ref('');
const loading = ref(false);
const msg = ref(null);

const getLoginError = (err) => {
  if (err.response?.status === 401){
    show({
      message: 'Credenciales incorrectas. Por favor verifica tu usuario y contraseña.',
      severity: 'error',
      autoHideMs: 4000
    });
    return 'Credenciales incorrectas. Por favor verifica tu usuario y contraseña.';
  } 
  if (err.response?.status === 404) {
    show({
      message: 'El usuario ingresado no se encuentra registrado. Crea tu cuenta ahora mismo presionando el botón "Crear cuenta".',
      severity: 'info',
      autoHideMs: 5000
    });
    return 'El usuario ingresado no se encuentra registrado.\nCrea tu cuenta ahora mismo presionando el botón:\n"Crear cuenta".';
  }
  
  show({
    message: 'Ocurrió un error al iniciar sesión. Inténtalo de nuevo más tarde.',
    severity: 'warning',
    autoHideMs: 4000
  });
  return 'Ocurrió un error al iniciar sesión. Inténtalo de nuevo más tarde.';
};

const goToRegister = () => {
  router.push('/register');
};

const submit = async () => {
  msg.value = null;
  loading.value = true;
  
  try {
    await login({
      username: identifier.value.trim(),
      password: password.value,
    });

    show({
      message: 'Sesión iniciada correctamente.',
      severity: 'success',
      autoHideMs: 3000
    });

    setTimeout(() => { router.push('/dashboard'); }, 500);
    
  } catch (err) {
    console.error(err);
    msg.value = getLoginError(err);
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

/* Acciones de formulario */
.actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
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