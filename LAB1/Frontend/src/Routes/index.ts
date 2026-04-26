import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import HomeView from '../Views/HomeView.vue'

/*
    ¿Cómo utilizar este archivo?
    1. Define tus rutas en el array `routes`. Cada ruta es un objeto con las siguientes propiedades:
       - `path`: La URL de la ruta (ejemplo: '/login').
       - `name`: Un nombre único para la ruta (ejemplo: 'login').
       - `component`: El componente que se renderizará cuando se acceda a la ruta. Puedes usar importación directa o carga perezosa.
       - `meta`: Un objeto opcional para almacenar información adicional, como el título de la página.
*/

const routes: Array<RouteRecordRaw> = [
  
  // Ruta principal que muestra el HomeView
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { title: 'Inicio' }
  },

  // Ruta para capturar errores 404 (página no encontrada)
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../Views/NotFound.vue')
  }
]

const router = createRouter({
  // Utiliza el historial del navegador para URLs limpias (sin el #)
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// Guardia de navegación para actualizar el título de la pestaña
router.beforeEach((to, from, next) => {
  const title = to.meta.title as string
  if (title) {
    document.title = `${title} - Mi Proyecto`
  }
  next()
})

export default router