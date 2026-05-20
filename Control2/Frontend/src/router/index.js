import { createRouter, createWebHistory } from 'vue-router'

// Importación de vistas
import LoginView from '../views/Login.vue'
import RegisterView from '../views/Register.vue'
import DashboardView from '../views/Dashboard.vue'

// Definición de rutas
const routes = [
  {
    path: '/',
    redirect: '/login'
  },

  {
    path: '/login',
    name: 'login',
    component: LoginView
  },

  {
    path: '/register',
    name: 'register',
    component: RegisterView
  },

  {
    path: '/dashboard',
    name: 'dashboard',
    component: DashboardView
  }
]

// Creación del router
const router = createRouter({
  history: createWebHistory(),
  routes
})

// Exportación
export default router