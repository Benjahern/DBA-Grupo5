<template>
  <div class="datacenters-page">

    <!-- Header -->
    <div class="page-header">

      <div>
        <h1>Gestión de Datacenters</h1>
        <p>
          Administra los centros de datos de la plataforma
        </p>
      </div>

      <button
        class="create-btn"
        @click="openCreateModal"
      >
        + Nuevo Datacenter
      </button>

    </div>


    <!-- Resumen -->
    <div class="stats-grid">

      <div class="stat-card">
        <span>Total</span>
        <strong>{{ datacenters.length }}</strong>
      </div>


      <div class="stat-card">
        <span>Operativos</span>
        <strong>{{ operationalCount }}</strong>
      </div>


      <div class="stat-card">
        <span>Mantenimiento</span>
        <strong>{{ maintenanceCount }}</strong>
      </div>


      <div class="stat-card">
        <span>Fuera de Servicio</span>
        <strong>{{ offlineCount }}</strong>
      </div>

    </div>


    <!-- Buscador -->
    <div class="toolbar">

      <input
        v-model="search"
        type="text"
        placeholder="Buscar datacenter..."
        class="search-input"
      />

    </div>


    <!-- Tabla -->
    <div class="table-container">

      <table>

        <thead>

          <tr>

            <th>Nombre</th>

            <th>Estado</th>

            <th>Instancias</th>

            <th>Capacidad</th>

            <th>Región</th>

            <th>Zona de Riesgo</th>

            <th>Latitud</th>

            <th>Longitud</th>

            <th>Acciones</th>

          </tr>

        </thead>


        <tbody>

          <tr
            v-for="datacenter in filteredDatacenters"
            :key="datacenter.id"
          >

            <td>
              {{ datacenter.name }}
            </td>


            <td>

              <span
                class="status-badge"
                :class="statusClass(datacenter.status)"
              >
                {{ datacenter.status }}
              </span>

            </td>


            <td>
              {{ datacenter.currentInstances }}
            </td>


            <td>
              {{ datacenter.capacity }}
            </td>


            <td>
              {{ datacenter.regionId }}
            </td>


            <td>
              {{ datacenter.riskZoneId }}
            </td>


            <td>
              {{ datacenter.latitude }}
            </td>


            <td>
              {{ datacenter.longitude }}
            </td>


            <td class="actions">

              <button
                class="edit-btn"
                @click="openEditModal(datacenter)"
              >
                Editar
              </button>


              <button
                class="delete-btn"
                @click="confirmDelete(datacenter)"
              >
                Eliminar
              </button>

            </td>


          </tr>


          <tr
            v-if="filteredDatacenters.length === 0"
          >

            <td
              colspan="9"
              class="empty-row"
            >
              No existen datacenters registrados
            </td>

          </tr>


        </tbody>

      </table>

    </div>

    <!-- Modal Crear Datacenter -->

    <DatacenterModal

      v-if="showCreateModal"

      @close="closeCreateModal"

      @created="handleCreated"

    />

    <!-- Modal Editar Datacenter -->

    <DatacenterEditModal

      v-if="showEditModal"

      :datacenter="selectedDatacenter"

      @close="closeEditModal"

      @updated="handleUpdated"

    />

    <!-- Modal Eliminar Datacenter -->

    <ConfirmDeleteModal

      v-if="showDeleteModal"

      :datacenter="selectedDatacenter"

      @close="closeDeleteModal"

      @deleted="handleDeleted"

    />


  </div>
</template>

<script setup>

import { ref, computed, onMounted } from 'vue'
import api from '@/services/http-common'

import DatacenterModal from '@/views/DatacenterModal.vue'
//import DatacenterEditModal from '@/components/datacenters/DatacenterEditModal.vue'
//import ConfirmDeleteModal from '@/components/common/ConfirmDeleteModal.vue'

const datacenters = ref([])
const search = ref('')

/*
 * Modales
 */

const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)


/*
 * Datacenter seleccionado
 * usado para editar/eliminar
 */

const selectedDatacenter = ref(null)

/*
 * Estadísticas
 */

const operationalCount = computed(() =>
  datacenters.value.filter(
    dc => dc.status === 'OPERATIVO'
  ).length
)


const maintenanceCount = computed(() =>
  datacenters.value.filter(
    dc => dc.status === 'MANTENIMIENTO'
  ).length
)


const offlineCount = computed(() =>
  datacenters.value.filter(
    dc => dc.status === 'FUERA_DE_SERVICIO'
  ).length
)

/*
 * Filtro búsqueda
 */

const filteredDatacenters = computed(() => {

  const text =
    search.value.toLowerCase().trim()


  if (!text) {
    return datacenters.value
  }


  return datacenters.value.filter(dc =>
    dc.name
      ?.toLowerCase()
      .includes(text)
  )

})

/*
 * Cargar datacenters
 */

const loadDatacenters = async () => {

  try {

    const response =
      await api.get('/api/datacenters')


    datacenters.value =
      response.data || []


  } catch (error) {

    console.error(
      'Error cargando datacenters:',
      error
    )

  }

}

/*
 * Crear
 */

const openCreateModal = () => {
  showCreateModal.value = true
}


const closeCreateModal = () => {
  showCreateModal.value = false
}


const handleCreated = async () => {
  closeCreateModal()
  await loadDatacenters()
}

/*
 * Editar
 */

const openEditModal = (datacenter) => {
  selectedDatacenter.value = {
    ...datacenter
  }

  showEditModal.value = true

}

const closeEditModal = () => {
  showEditModal.value = false
  selectedDatacenter.value = null
}

const handleUpdated = async () => {
  closeEditModal()
  await loadDatacenters()
}

/*
 * Eliminar
 */

const confirmDelete = (datacenter) => {
  selectedDatacenter.value = {
    ...datacenter
  }
  showDeleteModal.value = true
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  selectedDatacenter.value = null
}

const handleDeleted = async () => {
  closeDeleteModal()
  await loadDatacenters()
}

/*
 * Clases de estado
 */

const statusClass = (status) => {

  switch(status) {

    case 'OPERATIVO':
      return 'status-operational'


    case 'MANTENIMIENTO':
      return 'status-maintenance'


    case 'DEGRADADO':
      return 'status-degraded'


    case 'FUERA_DE_SERVICIO':
      return 'status-offline'


    default:
      return ''

  }

}

/*
 * Inicialización
 */

onMounted(async () => {

  await loadDatacenters()

})

</script>

<style scoped>

.datacenters-page {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}


/*
 * Header
 */

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}


.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  color: #1f2937;
}


.page-header p {
  margin-top: 6px;
  color: #64748b;
}

/*
 * Botón crear
 */

.create-btn {
  background: #2563eb;
  color: white;
  border: none;
  padding: 12px 18px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.95rem;
  transition: background .2s;
}

.create-btn:hover {
  background: #1d4ed8;
}

/*
 * Estadísticas
 */

.stats-grid {
  display: grid;
  grid-template-columns:
    repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.stat-card {
  background: white;
  border-radius: 14px;
  padding: 18px;
  border:
    1px solid #e5e7eb;
  box-shadow:
    0 4px 12px rgba(15,23,42,.05);
  display:flex;
  flex-direction:column;
  gap:8px;
}


.stat-card span {
  color:#64748b;
  font-size:.9rem;
}


.stat-card strong {
  font-size:1.8rem;
  color:#1e293b;
}



/*
 * Toolbar
 */

.toolbar {
  display:flex;
  justify-content:flex-end;
}


.search-input {
  width:300px;
  padding:11px 14px;
  border-radius:10px;
  border:
    1px solid #d1d5db;
  font-size:.95rem;
}


.search-input:focus {
  outline:none;
  border-color:#2563eb;
  box-shadow:
    0 0 0 3px rgba(37,99,235,.15);
}

/*
 * Tabla
 */

.table-container {
  background:white;
  border-radius:14px;
  border:
    1px solid #e5e7eb;
  overflow:hidden;
  box-shadow:
    0 4px 12px rgba(15,23,42,.05);
}

table {
  width:100%;
  border-collapse:collapse;
}

thead {
  background:#f8fafc;
}

th {
  text-align:left;
  padding:14px;
  font-size:.85rem;
  color:#475569;
  border-bottom:
    1px solid #e5e7eb;
}

td {
  padding:14px;
  border-bottom:
    1px solid #f1f5f9;
  color:#334155;
  font-size:.9rem;
}

tbody tr:hover {
  background:#f8fafc;
}

.empty-row {
  text-align:center;
  padding:30px;
  color:#64748b;
}

/*
 * Estados
 */

.status-badge {
  display:inline-flex;
  align-items:center;
  padding:5px 10px;
  border-radius:999px;
  font-size:.75rem;
  font-weight:600;
}

.status-operational {
  background:#dcfce7;
  color:#166534;
}

.status-maintenance {
  background:#fef3c7;
  color:#92400e;
}

.status-degraded {
  background:#fee2e2;
  color:#991b1b;
}

.status-offline {
  background:#e5e7eb;
  color:#374151;
}

/*
 * Acciones tabla
 */

.actions {
  display:flex;
  gap:8px;
}

.edit-btn,
.delete-btn {
  border:none;
  padding:7px 12px;
  border-radius:8px;
  cursor:pointer;
  font-size:.8rem;
}

.edit-btn {
  background:#dbeafe;
  color:#1d4ed8;
}

.edit-btn:hover {
  background:#bfdbfe;
}

.delete-btn {
  background:#fee2e2;
  color:#b91c1c;
}

.delete-btn:hover {
  background:#fecaca;
}

/*
 * Responsive
 */

@media(max-width:1000px) {
  .stats-grid {
    grid-template-columns:
      repeat(2,1fr);
  }

  .table-container {
    overflow-x:auto;
  }
}

@media(max-width:600px) {

  .datacenters-page {
    padding:16px;
  }

  .page-header {
    flex-direction:column;
    align-items:flex-start;
    gap:16px;
  }

  .stats-grid {
    grid-template-columns:1fr;
  }

  .toolbar {
    justify-content:stretch;
  }

  .search-input {
    width:100%;
  }
}

</style>