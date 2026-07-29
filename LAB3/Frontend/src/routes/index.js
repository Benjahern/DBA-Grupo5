import { createRouter, createWebHistory } from 'vue-router';
import LandingPage from '../views/LandingPage.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import Home from '../views/HomeView.vue';
import InstanceStats from '../views/InstanceStats.vue';
import NotFound from '../views/NotFound.vue';
import AdminAllInstances from '../views/AdminAllInstances.vue';
import AdminCosts from '../views/AdminCosts.vue';
import { getUser } from '../services/auth.js';
import Cost from '../views/IntanceTicket.vue';
import Regions from '../views/MaterializedView.vue';
import RegionsManagement from "../views/RegionCreate.vue";
import DatacenterManagement from "../views/DatacenterCreate.vue";

const routes = [
  { path: '/', name: 'landing', component: LandingPage, meta: { title: 'Host Cloud Usach' } },
  { path: '/home', name: 'home', component: Home, meta: { title: 'Inicio', requiresAuth: true } },
  { path: '/admin/instances', name: 'admin-instances', component: AdminAllInstances, meta: { title: 'Admin - Todas las Instancias', requiresAuth: true } },
  { path: '/admin/cost', name: 'admin-cost', component: AdminCosts, meta: { title: 'Admin - Todos los Costos', requiresAuth: true } },
  { path: '/instances/:id/stats', name: 'instance-stats', component: InstanceStats, meta: { title: 'Estadisticas', requiresAuth: true } },
  { path: '/login', name: 'login', component: Login, meta: { title: 'Iniciar Sesión' } },
  { path: '/register', name: 'register', component: Register, meta: { title: 'Registrarse' } },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFound, meta: { title: 'Página No Encontrada' } },
  { path: '/cost', name: 'cost', component: Cost, meta: { title: 'Costos', requiresAuth: true } },
  { path: '/regions', name: 'regions', component: Regions, meta: {title: 'Regiones', requiresAuth: true} },
  { path: '/regions/management', name: 'regions-management', component: RegionsManagement, meta: {title: 'Gestión de Regiones', requiresAuth: true} },
  { path: '/datacenters/management', name: 'datacenters-management', component: DatacenterManagement, meta: {title: 'Gestión de Datacenters', requiresAuth: true} }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

router.beforeEach((to, from, next) => {
  const title = to.meta.title;
  if (title) {
    document.title = `${title} - Host Cloud Usach`;
  }

  if (to.meta.requiresAuth && !getUser()) {
    next({ name: 'landing' });
  } else {
    next();
  }
});

export default router;

