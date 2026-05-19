import { createRouter, createWebHistory } from 'vue-router';
import LandingPage from '../views/LadingPage.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import Home from '../views/HomeView.vue';
import NotFound from '../views/NotFound.vue';
import { isAuthenticated, restoreSession } from '../services/auth.js';

const routes = [
  { path: '/', name: 'landing', component: LandingPage, meta: { title: 'Host Cloud Usach' } },
  { path: '/home', name: 'home', component: Home, meta: { title: 'Inicio', requiresAuth: true } },
  { path: '/login', name: 'login', component: Login, meta: { title: 'Iniciar Sesión' } },
  { path: '/register', name: 'register', component: Register, meta: { title: 'Registrarse' } },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFound, meta: { title: 'Página No Encontrada' } },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

router.beforeEach(async (to, from, next) => {
  const title = to.meta.title;
  if (title) {
    document.title = `${title} - Host Cloud Usach`;
  }

  if (to.meta.requiresAuth && !isAuthenticated()) {
    await restoreSession();
  }

  if (to.meta.requiresAuth && !isAuthenticated()) {
    next({ name: 'landing' });
  } else {
    next();
  }
});

export default router;

