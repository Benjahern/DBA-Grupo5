<template>

  <div class="filters-container">

    <!-- Buscar -->
    <input
      type="text"
      placeholder="Buscar notificaciones..."
      :value="search"
      @input="$emit('update:search', $event.target.value)"
      class="search-input"
    />

    <!-- Estado -->
    <select
      :value="status"
      @change="$emit('update:status', $event.target.value)"
      class="filter-select"
    >
      <option value="all">Todas</option>
      <option value="unread">No leídas</option>
      <option value="read">Leídas</option>
    </select>

    <!-- Tipo -->
    <select
      :value="type"
      @change="$emit('update:type', $event.target.value)"
      class="filter-select"
    >
      <option value="all">Todos los tipos</option>
      <option value="reminder">Recordatorios</option>
      <option value="overdue">Vencidas</option>
      <option value="completed">Completadas</option>
    </select>

  </div>

</template>

<script setup>

defineProps({
  search: String,
  status: String,
  type: String
})

defineEmits([
  'update:search',
  'update:status',
  'update:type'
])

</script>

<style scoped>

.filters-container {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.search-input,
.filter-select {
  background-color: white;
  border: 1px solid #d1d5db;
  color: black;
  padding: 12px 14px;
  border-radius: 12px;
  font-size: 14px;
}

.search-input {
  flex: 1;
  min-width: 240px;
}

.filter-select {
  min-width: 180px;
}

</style>