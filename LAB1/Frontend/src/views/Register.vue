<template>
  <div class="register-page">
    
    <main class="register-main">
      <div class="form-card">
        <h2 class="form-title">Crea tu cuenta ahora</h2>
        <p class="form-sub">Ingresa tu email</p>

        <form @submit.prevent="submit" class="form-grid">
          
          <div class="input-group">
            <label for="register-identifier">Email</label>
            <div class="input-with-icon">
              <span class="icon" aria-hidden>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 8l9 6 9-6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M21 8v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </span>
              <input id="register-identifier" v-model="identifier" placeholder="tu email" required />
            </div>
          </div>

          <div class="input-group">
            <label for="register-password">Contraseña</label>
            <div class="input-with-icon">
              <span class="icon" aria-hidden>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/><path d="M7 11V8a5 5 0 0 1 10 0v3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </span>
              <input id="register-password" type="password" v-model="password" placeholder="tu contraseña" required />
            </div>
          </div>

          <div class="input-group">
            <label for="register-password-confirm">Confirmar contraseña</label>
            <div class="input-with-icon">
              <span class="icon" aria-hidden>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/><path d="M7 11V8a5 5 0 0 1 10 0v3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </span>
              <input id="register-password-confirm" type="password" v-model="passwordConfirm" placeholder="confirma tu contraseña" required />
            </div>
          </div>

          <div class="actions">
            <button class="primary-cta" type="submit" :disabled="loading" style="width: 100%; justify-content: center">
              {{ loading ? 'Registrando...' : 'Crear cuenta' }}
            </button>
            <button type="button" class="link" style="width: 100%; justify-content: center" @click="goToLogin">
              ¿Ya tienes cuenta? Inicia sesión
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

const router = useRouter();
const { show } = useAlert();

const identifier = ref('');
const password = ref('');
const passwordConfirm = ref('');
const loading = ref(false);
const msg = ref(null);

// Lógica de guardado de token
const storeAuthData = (data) => {
  if (!data) return;
  if (data.access_token) {
    localStorage.setItem('access_token', data.access_token);
    if (data.refresh_token) localStorage.setItem('refresh_token', data.refresh_token);
  } else if (data.token?.access_token) {
    localStorage.setItem('access_token', data.token.access_token);
    if (data.token.refresh_token) localStorage.setItem('refresh_token', data.token.refresh_token);
    localStorage.setItem('app_token', data.token.access_token);
  }
  if (data.user) {
    localStorage.setItem('user', JSON.stringify(data.user));
  }
};

const getregisterError = (err) => {
  if (err.response?.status === 409) {
    show({
      message: 'El usuario o correo ya se encuentra registrado. Por favor, intenta iniciar sesión.',
      severity: 'warning',
      autoHideMs: 5000
    });
    return 'El usuario o correo ya se encuentra registrado.';
  } 
  if (err.response?.status === 400) {
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

  if (password.value !== passwordConfirm.value) {
    msg.value = 'Las contraseñas no coinciden.';
    show({ message: 'Las contraseñas no coinciden.', severity: 'warning', autoHideMs: 4000 });
    return;
  }

  loading.value = true;
  
  try {
    const resp = await api.post('/api/auth/register', { 
      email: identifier.value, 
      password: password.value 
    });
    
    const data = resp.data;
    const email = data?.user?.name || data?.name || identifier.value;
    
    storeAuthData(data);
    
    show({ message: `¡Bienvenido ${email}! Cuenta creada exitosamente`, severity: 'success', autoHideMs: 3500 });
    
    setTimeout(() => { router.push('/'); }, 500);
    
  } catch (err) {
    console.error(err);
    msg.value = getregisterError(err);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>

.register-page {
  background-color: #f9fafb; 
  min-height: 100vh;
}

.register-main {
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