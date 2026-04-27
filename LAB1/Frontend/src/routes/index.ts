import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import Home from '../views/HomeView.vue';
import NotFound from '../views/NotFound.vue';

const routes = [
  { path: '/', name: 'home', component: Home , meta : { title: 'Inicio' }},             
  { path: '/login', name: 'login', component: Login, meta: { title: 'Iniciar Sesión' } },       
  { path: '/register', name: 'register', component: Register, meta: { title: 'Registrarse' } },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFound , meta: { title: 'Página No Encontrada' }}
];


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
export default router;

