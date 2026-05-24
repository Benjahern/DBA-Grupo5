<template>
  <div class="location-picker">

    <!-- Botón ubicación actual -->
    <button
      class="location-button"
      type="button"
      @click="getCurrentLocation"
    >
      Usar mi ubicación actual
    </button>

    <!-- Coordenadas seleccionadas -->
    <div class="coordinates-box">

      <p>
        <strong>Latitud:</strong>
        {{ latitude ?? 'No seleccionada' }}
      </p>

      <p>
        <strong>Longitud:</strong>
        {{ longitude ?? 'No seleccionada' }}
      </p>

    </div>

    <!-- Mapa -->
    <l-map
      :zoom="zoom"
      :center="center"
      style="height: 400px; width: 100%; border-radius: 8px;"
      @click="handleMapClick"
    >

      <!-- Tiles -->
      <l-tile-layer
        :url="tileUrl"
        :attribution="attribution"
      />

      <!-- Marcador -->
      <l-marker
        v-if="latitude && longitude"
        :lat-lng="[latitude, longitude]"
      />

    </l-map>

  </div>
</template>

<script setup>
import { ref } from 'vue';

// Componentes Vue Leaflet
import {
  LMap,
  LTileLayer,
  LMarker
} from '@vue-leaflet/vue-leaflet';

// Emite evento al componente padre
const emit = defineEmits(['location-selected']);

// CSS Leaflet
import 'leaflet/dist/leaflet.css';

// Coordenadas iniciales (Santiago)
const center = ref([-33.4489, -70.6693]);

// Zoom inicial
const zoom = ref(13);

// Coordenadas seleccionadas
const latitude = ref(null);
const longitude = ref(null);

// URL OpenStreetMap
const tileUrl =
  'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';

// Créditos
const attribution =
  '&copy; OpenStreetMap contributors';

/**
 * Maneja click manual sobre el mapa
 */
const handleMapClick = (event) => {

  latitude.value = event.latlng.lat;
  longitude.value = event.latlng.lng;

  center.value = [
    latitude.value,
    longitude.value
  ];

  emit('location-selected', {
    latitude: latitude.value,
    longitude: longitude.value
  });

};

/**
 * Obtiene ubicación actual del usuario
 */
const getCurrentLocation = () => {

  // Verifica soporte navegador
  if (!navigator.geolocation) {

    alert('Tu navegador no soporta geolocalización');
    return;

  }

  navigator.geolocation.getCurrentPosition(

    // Éxito
    (position) => {

      latitude.value = position.coords.latitude;
      longitude.value = position.coords.longitude;

      // Centrar mapa
      center.value = [
        latitude.value,
        longitude.value
      ];

      // Zoom más cercano
      zoom.value = 16;

      emit('location-selected', {
        latitude: latitude.value,
        longitude: longitude.value
      });

    },

    // Error
    (error) => {

      console.error(error);

      alert(
        'No fue posible obtener tu ubicación actual'
      );

    }

  );

};
</script>

<style scoped>
.location-picker {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.location-button {
  background-color: #3b424d;
  color: white;
  border: none;
  padding: 0.8rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s;
}

.location-button:hover {
  background-color: #2f353d;
}

.coordinates-box {
  background-color: #f4f4f4;
  padding: 1rem;
  border-radius: 8px;
  color: #333;
}

.coordinates-box p {
  margin: 0.2rem 0;
}
</style>