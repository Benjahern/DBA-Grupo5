<template>
  <div class="login-page">
    
    <main class="login-main">
      <div class="form-card">
        <h2 class="form-title">Iniciar sesión</h2>
        <p class="form-sub">Ingresa con tu email</p>

        <form @submit.prevent="submit" class="form-grid">
          
          <div class="input-group">
            <label for="login-identifier">Email</label>
            <div class="input-with-icon">
              <span class="icon" aria-hidden>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 8l9 6 9-6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M21 8v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </span>
              <input id="login-identifier" v-model="identifier" placeholder="usuario o email" required />
            </div>
          </div>

          <div class="input-group">
            <label for="login-password">Contraseña</label>
            <div class="input-with-icon">
              <span class="icon" aria-hidden>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/><path d="M7 11V8a5 5 0 0 1 10 0v3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </span>
              <input id="login-password" type="password" v-model="password" placeholder="tu contraseña" required />
            </div>
          </div>

          <div class="actions">
            <button class="primary-cta" type="submit" :disabled="loading" style="width: 100%; justify-content: center">
              {{ loading ? 'Entrando...' : 'Ingresar' }}
            </button>
            <button type="button" class="link" style="width: 100%; justify-content: center" @click="goToRegister">
              Crear cuenta
            </button>
          </div>
        </form>

        <div v-if="msg" class="form-message" style="white-space: pre-wrap;">{{ msg }}</div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router'; 
import api from '../services/http-common';
import { useAlert } from '../components/Alerts/useAlert';
import { setToken } from '../services/auth.js';

const router = useRouter();
const { show } = useAlert();

const identifier = ref('');
const password = ref('');
const loading = ref(false);
const msg = ref(null);

// Lógica de guardado de token
const storeAuthData = (data) => {
  if (!data) return;
  if (data.access_token) {
    localStorage.setItem('access_token', data.access_token);
    if (data.refresh_token) localStorage.setItem('refresh_token', data.refresh_token);
    setToken(data.access_token);
  } else if (data.token?.access_token) {
    localStorage.setItem('access_token', data.token.access_token);
    if (data.token.refresh_token) localStorage.setItem('refresh_token', data.token.refresh_token);
    localStorage.setItem('app_token', data.token.access_token);
    setToken(data.token.access_token);
  }
  if (data.user) {
    localStorage.setItem('user', JSON.stringify(data.user));
  } else if (data.name || data.email) {
    localStorage.setItem('user', JSON.stringify({
      name: data.name || data.email,
      email: data.email
    }));
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

.login-page {
  background-color: #f9fafb; 
  min-height: 100vh;
}

.login-main {
  padding: 0 24px;
  padding-top: 40px; 
}

.form-card {
  margin: 0 auto; 
  max-width: 450px; 
  
  background-color: #ffffff;
  padding: 32px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
</style>