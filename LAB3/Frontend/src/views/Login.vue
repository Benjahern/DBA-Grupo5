<template>
  <div class="login-wrapper">
    <div class="login-card">
      <h2 class="title">ACCEDA A SU CUENTA</h2>

      <form @submit.prevent="submit" class="form-grid">
        <div class="input-row">
          <div class="input-group">
            <label for="login-identifier">Correo electrónico</label>
            <div class="input-container">
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

          <div class="input-group">
            <label for="login-password">Contraseña</label>
            <div class="input-container">
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
          <button type="submit" class="btn-acceder" style="justify-content: center" @click="goToRegister">
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
import api from '../services/http-common';
import { useAlert } from '../components/Alerts/useAlert';
import { setUser } from '../services/auth.js';

const router = useRouter();
const { show } = useAlert();

const identifier = ref('');
const password = ref('');
const loading = ref(false);
const msg = ref(null);

// Tras la migración, los tokens viajan solo en cookies HttpOnly.
// Aquí solo guardamos el perfil del usuario devuelto por el backend.
const storeAuthData = (data) => {
  if (!data) return;
  if (data.user) {
    setUser(data.user);
  } else if (data.name || data.email) {
    setUser({ name: data.name || data.email, email: data.email });
  }
};

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
  show ({
    message: 'Ocurrió un error al iniciar sesión. Inténtalo de nuevo más tarde.',
    severity: 'warning',
    autoHideMs: 4000
  });
};

const goToRegister = () => {
  router.push('/register');
};

const submit = async () => {
  msg.value = null;
  loading.value = true;
  
  try {
    const resp = await api.post('/api/auth/login', { 
      email: identifier.value, 
      password: password.value 
    });
    
    const data = resp.data;
    const userName = data?.user?.name || data?.name || identifier.value;
    
    storeAuthData(data);
    
    show({ message: `¡Bienvenido ${userName}! Sesión iniciada correctamente`, severity: 'success', autoHideMs: 3500 });
    
    setTimeout(() => { router.push('/home'); }, 500);
    
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