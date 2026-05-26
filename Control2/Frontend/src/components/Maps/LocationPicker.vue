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
      ref="mapRef"
      :zoom="zoom"
      :center="center"
      style="height: 280px; width: 100%; max-width: 500px; border-radius: 8px;"
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
import { ref, nextTick } from 'vue';

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

const mapRef = ref(null);

// URL OpenStreetMap
const tileUrl =
  'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';

// Créditos
const attribution =
  '&copy; OpenStreetMap contributors';

/**
 * Maneja click manual sobre el mapa
 */
const handleMapClick = async (event) => {

  latitude.value = event.latlng.lat;
  longitude.value = event.latlng.lng;

  center.value = [
    latitude.value,
    longitude.value
  ];

  await syncMapView();

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
    async (position) => {

      latitude.value = position.coords.latitude;
      longitude.value = position.coords.longitude;

      // Centrar mapa
      center.value = [
        latitude.value,
        longitude.value
      ];

      // Zoom mas cercano
      zoom.value = 16;

      await syncMapView();

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

const syncMapView = async () => {
  await nextTick();
  const map = mapRef.value?.leafletObject;
  if (!map) {
    return;
  }
  map.setView(center.value, zoom.value, { animate: true });
};
</script>

<style scoped>
.location-picker {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.8rem;
  width: 100%;
}

.location-button {
  background-color: #3b424d;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.15);
  padding: 0.65rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.85rem;
  transition: background-color 0.2s;
  width: 100%;
  max-width: 500px;
  text-align: center;
}

.location-button:hover {
  background-color: #2f353d;
}

.coordinates-box {
  background-color: #f4f4f4;
  padding: 0.6rem 1rem;
  border-radius: 6px;
  color: #333;
  width: 100%;
  max-width: 500px;
  text-align: center;
  font-size: 0.85rem;
}

.coordinates-box p {
  margin: 0.15rem 0;
}
</style>