<template>
  <div class="not-found-container">
    <h1 class="error-code">404</h1>
    <h2 class="error-title">¡Ups! Te has perdido</h2>
    <p class="error-message">
      La página que estás buscando no existe, ha sido movida o la URL está mal escrita.
    </p>
    
    <button @click="goHome" class="back-btn">
      Volver al Inicio
    </button>
  </div>
</template>

<script setup>
// Importamos el hook useRouter para poder navegar desde el código (programáticamente)
import { useRouter } from 'vue-router'

// Instanciamos el router
const router = useRouter()

// Función que se ejecuta al hacer clic en el botón
const goHome = () => {
  // Redirigimos usando el "name" de la ruta que definimos en router/index.ts
  router.push({ name: 'home' })
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh; /* Ocupa la mayor parte de la pantalla */
  text-align: center;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  padding: 2rem;
}

.error-code {
  font-size: 8rem;
  font-weight: bold;
  color: #42b883; /* Verde característico de Vue */
  margin: 0;
  line-height: 1;
  /* Un pequeño efecto de sombra para darle estilo */
  text-shadow: 4px 4px 0px rgba(66, 184, 131, 0.2); 
}

.error-title {
  font-size: 2rem;
  color: #2c3e50;
  margin-top: 1rem;
  margin-bottom: 1rem;
}

.error-message {
  font-size: 1.2rem;
  color: #666;
  max-width: 500px;
  margin-bottom: 2.5rem;
  line-height: 1.5;
}

.back-btn {
  background-color: #2c3e50;
  color: white;
  border: none;
  padding: 12px 28px;
  font-size: 1.1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: bold;
}

.back-btn:hover {
  background-color: #42b883; /* Cambia al verde de Vue al pasar el ratón */
  transform: translateY(-2px); /* Pequeño salto visual */
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
</style>