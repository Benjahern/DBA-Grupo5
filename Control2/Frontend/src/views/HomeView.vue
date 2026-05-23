<template>
  <div class="home-container">
    <header class="hero">
      <h1>¡Bienvenido, {{ userName }}!</h1>
      <p>Administra tus instancias ahora.</p>

      <div class="actions">
        <button class="action-btn" type="button" @click="handleLogout">
          Cerrar sesión
        </button>
      </div>
    </header>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { getUser, logout } from '../services/auth.js';

const router = useRouter();

const userName = computed(() => {
  const tokenUser = getUser();
  return tokenUser?.username || 'Usuario';
});

const handleLogout = async () => {
  await logout();
  router.push('/');
};

</script>

<style scoped>
/* Scoped asegura que estos estilos SOLO afecten a HomeView */
.home-container {
  align-content: center;
  max-width: 900px;
  width: 90%;
  margin: 0 auto; 
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  text-align: center;
  padding-top: 2rem;
}

.hero {
  align-content: center;
  width: 110%;
  margin-bottom: 4rem;
  padding: 2rem;
  background-color: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.hero h1 {
  color: #2c3e50;
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.hero p {
  color: #666;
  font-size: 1.2rem;
  margin-bottom: 2rem;
}

.action-btn {
  background-color: #42b883; /* Verde Vue */
  color: white;
  border: none;
  padding: 12px 24px;
  font-size: 1.1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-weight: bold;
}

.action-btn:hover {
  background-color: #33a06f;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.card {
  padding: 2rem;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px rgba(0,0,0,0.1);
}

.card h3 {
  color: #42b883;
  margin-bottom: 1rem;
}

.card p {
  color: #555;
  line-height: 1.5;
}
</style>