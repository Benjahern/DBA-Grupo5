<template>
  <aside v-if="isVisible" class="sidebar" :class="{ 'collapsed': isCollapsed }">
    <div class="logo-container">
      <button class="toggle-btn" @click="toggleSidebar">
        ☰
      </button>
      <span v-if="!isCollapsed" class="logo-text">USACH Cloud</span>
    </div>
    
    <nav class="sidebar-nav">
      <div class="nav-group" :class="{ 'is-open': openGroups.infra && !isCollapsed }">
        <div class="nav-title" @click="toggleGroup('infra')" title="Infraestructura">
          <span class="icon">⚙️</span>
          <span v-if="!isCollapsed" class="text">Infraestructura</span>
          <span v-if="!isCollapsed" class="arrow">{{ openGroups.infra ? '▼' : '▶' }}</span>
        </div>
        <transition name="expand">
          <ul v-if="openGroups.infra && !isCollapsed" class="nav-list">
            <li :class="{ 'active-item': activeSection === 'servidor' }" @click="selectSection('servidor')">
              Servidor
            </li>
            <li :class="{ 'active-item': activeSection === 'imagenes' }" @click="selectSection('imagenes')">
              Imágenes
            </li>
          </ul>
        </transition>
      </div>

      <div v-if="userRole === 'admin'" class="nav-group admin-panel" :class="{ 'is-open': openGroups.admin && !isCollapsed }">
        <div class="nav-title" @click="toggleGroup('admin')" title="Administración">
          <span class="icon">🛡️</span>
          <span v-if="!isCollapsed" class="text">Administración</span>
          <span v-if="!isCollapsed" class="arrow">{{ openGroups.admin ? '▼' : '▶' }}</span>
        </div>
        <transition name="expand">
          <ul v-if="openGroups.admin && !isCollapsed" class="nav-list">
            <li :class="{ 'active-item': activeSection === 'usuarios' }" @click="selectSection('usuarios')">
              Gestionar Usuarios
            </li>
            <li :class="{ 'active-item': activeSection === 'todas-instancias' }" @click="goToAdminInstances">
              Todas las Instancias
            </li>
          </ul>
        </transition>
      </div>

      <div class="nav-group" v-for="item in ['red', 'seguridad', 'gestion', 'costes']" :key="item">
        <div class="nav-title" @click="selectSection(item)" :title="item">
          <span class="icon">{{ getIcon(item) }}</span>
          <span v-if="!isCollapsed" class="text capitalize">{{ item }}</span>
        </div>
      </div>
    </nav>

    <div class="user-footer">
      <div v-if="!isCollapsed" class="user-info">
        <span class="user-name">{{ userName }}</span>
        <span class="user-role">{{ userRole }}</span>
      </div>
      <button @click="$emit('logout')" class="btn-logout">
        {{ isCollapsed ? '🚪' : 'Cerrar Sesión' }}
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

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
  infra: false,
  admin: false
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
  }
};

const toggleGroup = (group: 'infra' | 'admin') => {
  if (isCollapsed.value) {
    isCollapsed.value = false; // Expandir sidebar automáticamente si estaba cerrada
  }
  openGroups[group] = !openGroups[group];
};

const selectSection = (section: string) => {
  emit('update:section', section);
};

const goToAdminInstances = () => {
  router.push({ name: 'admin-instances' });
};

const getIcon = (item: string) => {
  const icons: any = { red: '🌐', seguridad: '🔒', gestion: '🛠️', costes: '📊' };
  return icons[item];
};
</script>

<style scoped>
.sidebar {
  width: 250px;
  background-color: #2c3e50;
  color: white;
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
  color: white;
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
  white-space: nowrap;
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
  color: #bdc3c7;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-list li:hover {
  color: white;
  background-color: #2c3e50;
}

.active-item {
  color: #1abc9c !important;
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
  color: white;
}

.capitalize { text-transform: capitalize; }
</style>