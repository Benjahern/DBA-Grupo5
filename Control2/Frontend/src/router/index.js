import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated, restoreSession } from '../services/auth.js'

// Importación de vistas
import LoginView from '../views/Login.vue'
import RegisterView from '../views/Register.vue'
import DashboardView from '../views/Dashboard.vue'
import MainLayout from '../layouts/MainLayout.vue'
import TasksView from '../views/Tasks.vue'
import NotificationsView from '../views/Notifications.vue'
import SectorCreateView from '../views/SectorCreate.vue'

// Definición de rutas
const routes = [

  // Página inicial
  {
    path: '/',
    redirect: '/login'
  },

  // Públicas
  {
    path: '/login',
    component: LoginView
  },

  {
    path: '/register',
    component: RegisterView
  },

  // Privadas (requieren autenticación)
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },

    children: [

      {
        path: 'dashboard',
        component: DashboardView
      },

      {
        path: 'tasks',
        component: TasksView
      },

      {
        path: 'notifications',
        component: NotificationsView
      },

      {
        path: 'sectors',
        component: SectorCreateView
      }

    ]
  }
]

// Creación del router
const router = createRouter({
  history: createWebHistory(),
  routes
})

// Guard de navegación: protege rutas que requieren autenticación
router.beforeEach(async (to, from) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

  if (requiresAuth && !isAuthenticated()) {
    await restoreSession()
  }

  if (requiresAuth && !isAuthenticated()) {
    return '/login'
  }
})

// Exportación
export default router