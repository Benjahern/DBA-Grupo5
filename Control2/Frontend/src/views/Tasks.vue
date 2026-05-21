<template>
  <div class="tasks-view">

    <!-- HEADER -->
    <div class="tasks-header">

      <div>
        <h1>Tareas</h1>
        <p>Administra las tareas geoespaciales del sistema</p>
      </div>

      <button class="create-task-btn">
        + Nueva tarea
      </button>

    </div>

    <!-- TOOLBAR -->
    <div class="tasks-toolbar">

      <!-- SEARCH -->
      <input
        type="text"
        placeholder="Buscar tarea..."
        class="search-input"
      />

      <!-- FILTERS -->
      <select class="filter-select">
        <option>Estado</option>
        <option>Pendiente</option>
        <option>En progreso</option>
        <option>Completada</option>
      </select>

      <select class="filter-select">
        <option>Prioridad</option>
        <option>Alta</option>
        <option>Media</option>
        <option>Baja</option>
      </select>

    </div>

    <!-- MAIN CONTENT -->
    <div class="tasks-content">

      <!-- LEFT PANEL -->
      <div class="tasks-list">

        <div class="task-item active">
          <div class="task-item-left">
            <input type="checkbox" />
            
            <div class="task-item-info">
              <span class="task-title">
                Inspección Sector Norte
              </span>

              <span class="task-date">
                📅 20 Mayo
              </span>
            </div>
          </div>

          <span class="task-priority high">
            Alta
          </span>
        </div>

        <div class="task-item">
          <div class="task-item-left">
            <input type="checkbox" checked />

            <div class="task-item-info">
              <span class="task-title">
                Actualización de mapa
              </span>

              <span class="task-date">
                📅 22 Mayo
              </span>
            </div>
          </div>

          <span class="task-priority medium">
            Media
          </span>
        </div>

        <div class="task-item">
          <div class="task-item-left">
            <input type="checkbox" />

            <div class="task-item-info">
              <span class="task-title">
                Revisar sensores zona sur
              </span>

              <span class="task-date">
                📅 25 Mayo
              </span>
            </div>
          </div>

          <span class="task-priority low">
            Baja
          </span>
        </div>

      </div>

      <!-- RIGHT PANEL -->
      <div class="task-details">

        <div class="task-details-card">

          <div class="details-header">

            <div>
              <h2>Inspección Sector Norte</h2>

              <p>
                Revisión de infraestructura y sensores
                geoespaciales en zona norte.
              </p>
            </div>

            <span class="status pending">
              Pendiente
            </span>

          </div>

          <div class="details-section">
            <h3>Información</h3>

            <div class="details-grid">

              <div class="detail-item">
                <span class="detail-label">Prioridad</span>
                <span>Alta</span>
              </div>

              <div class="detail-item">
                <span class="detail-label">Sector</span>
                <span>Sector Norte</span>
              </div>

              <div class="detail-item">
                <span class="detail-label">Responsable</span>
                <span>Juan Pérez</span>
              </div>

              <div class="detail-item">
                <span class="detail-label">Vencimiento</span>
                <span>20 Mayo 2026</span>
              </div>

            </div>
          </div>

          <div class="details-section">

            <h3>Ubicación</h3>

            <div class="map-placeholder">
              🗺 Aquí podría mostrarse el mapa de la tarea
            </div>

          </div>

          <div class="details-actions">

            <button class="secondary-btn">
              Editar
            </button>

            <button class="danger-btn">
              Eliminar
            </button>

          </div>

        </div>

      </div>

    </div>

  </div>
</template>

<script setup>

// Sin lógica por ahora

</script>

<style scoped>

.tasks-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
  height: 100%;
}

/* HEADER */

.tasks-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tasks-header h1 {
  margin: 0;
  font-size: 32px;
}

.tasks-header p {
  margin-top: 6px;
  color: #666;
}

/* BUTTON */

.create-task-btn {
  background-color: #374151;
  color: white;
  border: none;
  padding: 12px 18px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.create-task-btn:hover {
  background-color: #1f2937;
}

/* TOOLBAR */

.tasks-toolbar {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 250px;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
}

.filter-select {
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background-color: white;
  font-size: 14px;
}

/* MAIN CONTENT */

.tasks-content {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 24px;
  min-height: 650px;
}

/* LEFT PANEL */

.tasks-list {
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);

  display: flex;
  flex-direction: column;
  gap: 12px;

  overflow-y: auto;
}

/* TASK ITEM */

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 14px;
  border-radius: 10px;

  border: 1px solid #e5e7eb;

  cursor: pointer;
  transition: 0.2s;
}

.task-item:hover {
  background-color: #f9fafb;
}

.task-item.active {
  border: 2px solid #3b82f6;
  background-color: #eff6ff;
}

.task-item-left {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.task-item-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.task-title {
  font-weight: 600;
}

.task-date {
  color: #666;
  font-size: 14px;
}

/* PRIORITY */

.task-priority {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: bold;
}

.high {
  background-color: #fee2e2;
  color: #b91c1c;
}

.medium {
  background-color: #dbeafe;
  color: #1d4ed8;
}

.low {
  background-color: #dcfce7;
  color: #166534;
}

/* RIGHT PANEL */

.task-details {
  display: flex;
}

.task-details-card {
  width: 100%;
  background-color: white;
  border-radius: 12px;
  padding: 24px;

  box-shadow: 0 2px 8px rgba(0,0,0,0.08);

  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* DETAILS HEADER */

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.details-header h2 {
  margin: 0;
}

.details-header p {
  color: #666;
  margin-top: 8px;
  line-height: 1.5;
}

/* STATUS */

.status {
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: bold;
}

.pending {
  background-color: #fef3c7;
  color: #92400e;
}

/* DETAILS */

.details-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.details-section h3 {
  margin: 0;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-label {
  color: #666;
  font-size: 14px;
}

/* MAP */

.map-placeholder {
  height: 220px;
  border-radius: 12px;
  border: 2px dashed #cbd5e1;

  display: flex;
  justify-content: center;
  align-items: center;

  color: #64748b;
  background-color: #f8fafc;
}

/* ACTIONS */

.details-actions {
  display: flex;
  gap: 12px;
}

.secondary-btn,
.danger-btn {
  padding: 12px 18px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.secondary-btn {
  background-color: #374151;
  color: white;
}

.danger-btn {
  background-color: #ef4444;
  color: white;
}

/* RESPONSIVE */

@media (max-width: 1000px) {

  .tasks-content {
    grid-template-columns: 1fr;
  }

}

</style>