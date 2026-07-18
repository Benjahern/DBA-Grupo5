<template>
  <aside v-if="isVisible" class="sidebar" :class="{ 'collapsed': isCollapsed }">
    <div class="logo-container">
      <button class="toggle-btn" @click="toggleSidebar">
        ☰
      </button>
      <span v-if="!isCollapsed" class="logo-text">USACH Cloud</span>"
    </div>
    
    <nav class="sidebar-nav">
      <!-- Administration (acceso ADMIN) -->
      <div v-if="isAdmin" class="navGroup" :class="{ 'is-open': openGroups.admin && !isCollapsed }">
        <div class="nav-title" @click="toggleGroup('admin')">
          <span class="icon">🛡️</span>
          <span v-if="!isCollapsed" class="text">Administración</span>
          <span v-if="!isCollapsed" class="arrow">{{ openGroups.admin ? '▼' : '▶' }}</span>
        </div>
        <transition name="expand">
          <ul v-if="openGroups.admin && !isCollapsed" class="nav-list">
            <li @click="goToAdminInstances">Todas las instancias</li>
            <li @click="goToAdminCost">Todos los costos</li>
          </ul>
        </transition>
      </div>

      <!-- Servers (acceso ADMIN + USER) -->
      <div v-if="isAdmin || isUser" class="nav-group" :class="{ 'is-open': openGroups.servers && !isCollapsed }">
        <div class="nav-title" @click="toggleGroup('servers')">
          <span class="icon">🖥️</span>
          <span v-if="!isCollapsed" class="text">Servidores</span>
          <span v-if="!isCollapsed" class="arrow">{{ openGroups.servers ? '▼' : '▶' }}</span>
        </div>
        <transition name="expand">
          <ul v-if="openGroups.servers && !isCollapsed" class="nav-list">
            <li @click="goToHome">Crear instancia</li>
            <li @click="goToStats">Ver estadísticas</li>
          </ul>
        </transition>
      </div>

      <!-- Costs (según rol va a uno u otro) -->
      <div v-if="isAdmin || isUser" class="nav-group" :class="{ 'is-open': openGroups.costs && !isCollapsed }">
        <div class="nav-title" @click="toggleGroup('costs')">
          <span class="icon">💰</span>
          <span v-if="!isCollapsed" class="text">Costos</span>
          <span v-if="!isCollapsed" class="arrow">{{ openGroups.costs ? '▼' : '▶' }}</span>
        </div>
        <transition name="expand">
          <ul v-if="openGroups.costs && !isCollapsed" class="nav-list">

            <!-- ADMIN -->
            <li v-if="isAdmin" @click="goToAdminCost">
              Todos los costos
            </li>

            <!-- USER -->
            <li v-if="isUser" @click="goToCost">
              Boletas y facturación
            </li>
          </ul>
        </transition>
      </div>

      <!-- Regions (acceso ADMIN) -->
      <div v-if="isAdmin" class="nav-group" :class="{ 'is-open': openGroups.regions && !isCollapsed }">
        <div class="nav-title" @click="toggleGroup('regions')">
          <span class="icon">🌍</span>
          <span v-if="!isCollapsed" class="text">Regiones</span>
          <span v-if="!isCollapsed" class="arrow">{{ openGroups.regions ? '▼' : '▶' }}</span>
        </div>
        <transition name="expand">
          <ul v-if="openGroups.regions && !isCollapsed" class="nav-list">
            <li @click="goToRegions">Uso de recursos</li>
            <li @click="goToRegionsManagement">Gestión de recursos</li>
            <li @click="goToDatacentersManagement">Gestión de datacenters</li>
          </ul>
        </transition>
      </div>

      <!-- Ping (todos los autenticados) -->
      <div class="nav-group" :class="{ 'is-open': openGroups.ping && !isCollapsed }">
        <div class="nav-title" @click="toggleGroup('ping')">
          <span class="icon">📡</span>
          <span v-if="!isCollapsed" class="text">Ping</span>
          <span v-if="!isCollapsed" class="arrow">{{ openGroups.ping ? '▼' : '▶' }}</span>
        </div>
        <transition name="expand">
          <div v-if="openGroups.ping && !isCollapsed" class="ping-panel">
            <PingSection />
          </div>
        </transition>
      </div>
    </nav>

    <div class="user-footer">
      <div v-if="!isCollapsed" class="user-info">
        <span class="user-name">{{ userName }}</span>
        <span class="user-role">{{ roles.join(', ') }}</span>
      </div>
      <button @click="$emit('logout')" class="btn-logout">
        {{ isCollapsed ? '🚪' : 'Cerrar Sesión' }}
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getUser, subscribe } from '../../services/auth.js';
import PingSection from '../Ping/PingSection.vue';

const isUser = computed(() => roles.value.includes("user"));

// Roles reactivos
const roles = ref<string[]>([]);
const userId = ref<string>('');

const updateRoles = () => {
  const user = getUser();

  // Tras la migración a JWT propio, el backend devuelve el objeto user con
  // { User_id, Email, Name, Roles, Sub }. Mantenemos compatibilidad con tokens
  // viejos (Keycloak: realm_access.roles) por si quedara alguno en localStorage.
  if (user) {
    userId.value = String(user.User_id ?? user.sub ?? '');
  }
  const rawRoles =
      user?.Roles
      ?? user?.roles
      ?? user?.realm_access?.roles
      ?? [];
  roles.value = (Array.isArray(rawRoles) ? rawRoles : []).filter(
    r => !["offline_access", "uma_authorization", "default-roles-host-usach"].includes(r)
  );
}

onMounted(() => {
  updateRoles();
  subscribe(updateRoles);
});

// Helpers de roles
const isAdmin =computed(() =>
  roles.value.includes("admin") || roles.value.includes("SysAdmin")
);

const props = defineProps({
  userRole: { type: String, default: 'admin' },
  userName: { type: String, default: 'Admin User' },
  activeSection: { type: String, default: 'servidor' }
});

const emit = defineEmits(['update:section', 'logout']);
const route = useRoute();
const router = useRouter();
const isCollapsed = ref(false);

const openGroups = reactive({
  admin: false,
  servers: false,
  costs: false,
  regions: false,
  ping: false
});

const isVisible = computed(() => {
  const excluded = ['login', 'register', 'not-found'];
  return !excluded.includes(route.name as string);
});

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
  // Opcional: Cerrar los grupos al colapsar para que no "vuelen" fuera
  if (isCollapsed.value) {
    openGroups.infra = false;
    openGroups.admin = false;
    openGroups.ping = false;
  }
};

const toggleGroup = (group: 'infra' | 'admin' | 'servers' | 'costs' | 'regions' | 'ping') => {
  if (isCollapsed.value) {
    isCollapsed.value = false; // Expandir sidebar automáticamente si estaba cerrada
  }
  openGroups[group] = !openGroups[group];
};

const goToAdminInstances = () => {
  router.push({ name: 'admin-instances' });
};

const goToAdminCost = () => {
  router.push({ name: 'admin-cost' });
};

const goToHome = () => {
  router.push({ name: 'home' });
};

const goToStats = () => {
  if(userId.value) {
    router.push({
      name: 'instance-stats',
      params: { id: userId.value }
    });
  } else {
    console.warn("No se pudo obtener el ID del usuario (sub)");
  }
};

const goToCost = () => {
  router.push({ name: 'cost' });
};

const goToRegions = () => {
  router.push({ name: 'regions' });
};

const goToRegionsManagement = () => {
  router.push({ name: 'regions-management' });
};

const goToDatacentersManagement = () => {
  router.push({ name: 'datacenters-management' });
};
</script>

<style scoped>
.sidebar {
  width: 250px;
  background-color: #2c3e50;
  color: rgb(255, 255, 255);
  display: flex;
  flex-direction: column;
  height: 100vh;
  transition: width 0.3s ease;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 70px;
}

.logo-container {
  display: flex;
  align-items: center;
  padding: 0 20px;
  background-color: #1a252f;
  height: 64px;
  flex-shrink: 0;
}

.toggle-btn {
  background: none;
  border: none;
  color: rgb(255, 255, 255);
  font-size: 20px;
  cursor: pointer;
  margin-right: 15px; /* Espacio con el texto USACH Cloud */
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: rgb(255, 255, 255);
  background: none;
  border: none;

}

.sidebar-nav {
  flex-grow: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-group {
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.nav-title {
  padding: 15px 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: background 0.2s;
}

.nav-title:hover {
  background-color: #34495e;
}

.icon {
  font-size: 1.2rem;
  min-width: 30px;
  display: flex;
  justify-content: center;
}

.text {
  flex-grow: 1;
  margin-left: 10px;
  white-space: nowrap;
}

.arrow {
  font-size: 0.7rem;
  opacity: 0.5;
}

.nav-list {
  list-style: none;
  background-color: #1a252f;
  padding: 0;
}

.nav-list li {
  padding: 12px 20px 12px 60px;
  font-size: 0.9rem;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-list li:hover {
  color: rgb(255, 255, 255);
  background-color: #2c3e50;
}

.active-item {
  color: #ffffff !important;
  font-weight: bold;
  background-color: rgba(26, 188, 156, 0.1);
}

.user-footer {
  padding: 20px;
  background-color: #1a252f;
  flex-shrink: 0;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.user-info {
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
}

.user-name { font-weight: bold; font-size: 14px; }
.user-role { font-size: 11px; color: #1abc9c; text-transform: uppercase; }

/* Transición de expansión para el acordeón */
.expand-enter-active, .expand-leave-active {
  transition: all 0.3s ease;
  max-height: 200px;
  overflow: hidden;
}
.expand-enter-from, .expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.btn-logout {
  width: 100%;
  background: transparent;
  border: 1px solid #e74c3c;
  color: #e74c3c;
  padding: 8px;
  cursor: pointer;
  border-radius: 4px;
}

.btn-logout:hover {
  background: #e74c3c;
  color: rgb(175, 28, 28);
}

.capitalize { text-transform: capitalize; }

.ping-panel {
  background-color: #1a252f;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  max-height: 70vh;
  overflow-y: auto;
}
</style>