<template>
  <div class="home-container">
    <header class="hero">
      <h1>Bienvenido al Proyecto</h1>
      <p>Esta es la página principal (HomeView). El enrutador está funcionando correctamente.</p>
      
      <button @click="triggerWelcomeAlert" class="action-btn">
        Probar Alerta Global
      </button>
    </header>

    <section class="features-grid">
      <div class="card">
        <h3>⚡ Vue 3 & Vite</h3>
        <p>Renderizado ultrarrápido usando la Composition API.</p>
      </div>
      <div class="card">
        <h3>🛡️ TypeScript</h3>
        <p>Código más seguro y autocompletado inteligente en el editor.</p>
      </div>
      <div class="card">
        <h3>🧩 Componentes</h3>
        <p>Arquitectura modular y escalable para trabajar en equipo.</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
// Importamos el hook que creó Mharko. 
// Ajusta la ruta dependiendo de dónde pusiste la carpeta Components
import { useAlert } from '../Components/Alerts/useAlert';

// Extraemos la función para mostrar la alerta
const { show } = useAlert()

// Función que se ejecuta al hacer clic en el botón
const triggerWelcomeAlert = () => {
  show({
    message: '¡Excelente! La vista se comunicó con el componente global.',
    severity: 'success',
    autoHideMs: 4000
  })
}
</script>

<style scoped>
/* Scoped asegura que estos estilos SOLO afecten a HomeView */
.home-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  text-align: center;
}

.hero {
  margin-bottom: 4rem;
  padding: 3rem;
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