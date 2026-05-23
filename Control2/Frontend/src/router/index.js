import { createRouter, createWebHistory } from 'vue-router'

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

  // Privadas
  {
    path: '/',
    component: MainLayout,

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

// Exportación
export default router