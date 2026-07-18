<template>
  <div class="modal-backdrop" @click.self="emitClose">

    <div class="modal-card">

      <!-- Header -->
      <div class="modal-header">

        <h2>
          Nuevo Datacenter
        </h2>

        <button
          class="icon-btn"
          type="button"
          @click="emitClose"
        >
          ×
        </button>

      </div>


      <!-- Body -->
      <form
        class="modal-body"
        @submit.prevent="handleSubmit"
      >

        <div class="form-group">

          <label for="datacenter-name">
            Nombre
          </label>

          <input
            id="datacenter-name"
            v-model.trim="form.name"
            type="text"
            placeholder="Ej: Datacenter Santiago"
            required
          />

        </div>


        <div class="form-row">

          <div class="form-group">

            <label for="datacenter-status">
              Estado
            </label>

            <select
              id="datacenter-status"
              v-model="form.status"
            >

              <option value="OPERATIVO">
                Operativo
              </option>

              <option value="MANTENIMIENTO">
                Mantenimiento
              </option>

              <option value="DEGRADADO">
                Degradado
              </option>

              <option value="FUERA_DE_SERVICIO">
                Fuera de servicio
              </option>

            </select>

          </div>


          <div class="form-group">

            <label for="datacenter-capacity">
              Capacidad
            </label>

            <input
              id="datacenter-capacity"
              v-model.number="form.capacity"
              type="number"
              min="1"
              placeholder="Cantidad máxima de instancias"
              required
            />

          </div>

        </div>


        <!-- Mapa -->
        <div class="map-panel">

            <div class="map-header">

                <h3>
                Ubicación del Datacenter
                </h3>

                <span v-if="selectedLatLng">
                {{ selectedLatLng[0].toFixed(5) }},
                {{ selectedLatLng[1].toFixed(5) }}
                </span>

                <span v-else>
                Selecciona un punto en el mapa
                </span>

                <div
                v-if="locationInfo"
                class="location-details"
                >

                <div>
                    Región:
                    <strong>
                    {{ locationInfo.regionName }}
                    </strong>
                </div>

                <div>
                    Placa tectónica:
                    <strong>
                    {{ locationInfo.riskZoneName }}
                    </strong>
                </div>

                </div>

            </div>

            <div
                ref="mapEl"
                class="map-container"
            ></div>

        </div>


        <div
          v-if="error"
          class="form-error"
        >
          {{ error }}
        </div>


        <!-- Actions -->
        <div class="modal-actions">

          <button
            class="secondary-btn"
            type="button"
            @click="emitClose"
          >
            Cancelar
          </button>


          <button
            class="primary-btn"
            type="submit"
            :disabled="loading"
          >

            {{
              loading
                ? 'Creando...'
                : 'Crear Datacenter'
            }}

          </button>

        </div>


      </form>

    </div>

  </div>
</template>


<script setup>

import { ref, onMounted, onBeforeUnmount } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

import api from '@/services/http-common.js'

import { useAlert } from '@/components/Alerts/useAlert.js'


const emit = defineEmits([
  'close',
  'created'
])


const { show } = useAlert()


const form = ref({

  name: '',

  status: 'OPERATIVO',

  capacity: 1,

  latitude: null,

  longitude: null,

  regionId: null,

  riskZoneId: null

})

const mapEl = ref(null)

let map = null

let marker = null

const locationInfo = ref(null)

const loading = ref(false)

const error = ref(null)


const selectedLatLng = ref(null)


const mapCenter = ref([
  -33.4489,
  -70.6693
])


const mapZoom = ref(5)


const tileUrl =
  'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'


const attribution =
  '&copy; OpenStreetMap contributors'



const emitClose = () => {

  emit('close')

}

const fetchLocationInfo = async (
  latitude,
  longitude
) => {

  try {

    const response = await api.post(
      '/api/datacenters/location-info',
      {
        latitude,
        longitude
      }
    )

    locationInfo.value =
      response.data

    form.value.regionId =
      response.data.regionId

    form.value.riskZoneId =
      response.data.riskZoneId

  } catch (error) {

    console.error(error)

    locationInfo.value = {
        regionName: 'No encontrada',
        riskZoneName: 'No disponible'
    }

    form.value.regionId = null
    form.value.riskZoneId = null
  }

}

const handleMapClick = async (event) => {

  const lat =
    event.latlng.lat

  const lng =
    event.latlng.lng

  form.value.latitude = lat

  form.value.longitude = lng

  selectedLatLng.value = [
    lat,
    lng
  ]

  await fetchLocationInfo(
    lat,
    lng
  )

  if (marker) {
    marker.remove()
  }

  marker = L.marker([
    lat,
    lng
  ]).addTo(map)

}


const handleSubmit = async () => {

  error.value = null

  if (!form.value.name) {
    error.value =
      'El nombre es obligatorio'
    return
  }

  if (!form.value.capacity ||
      form.value.capacity <= 0) {
    error.value =
      'La capacidad debe ser mayor a cero'
    return
  }

  if (!selectedLatLng.value) {
    error.value =
      'Debes seleccionar una ubicación en el mapa'
    return
  }
  if (!form.value.regionId) {
    error.value =
        'No se pudo determinar la región'
    return
  }
  if (!form.value.riskZoneId) {
    error.value =
        'No se pudo determinar la placa tectónica'
    return
  }
  loading.value = true
  try {
    console.log('Payload enviado:', form.value)
    await api.post(
      '/api/datacenters',
      form.value
    )
    show({
      message:
        'Datacenter creado correctamente',
      severity:
        'success',
      autoHideMs:
        3000
    })
    emit('created')
    emitClose()
  } catch (err) {
    console.error(
      'Error creando datacenter:',
      err
    )
    error.value =
      'No se pudo crear el datacenter'
    show({
      message:
        'No se pudo crear el datacenter',
      severity:
        'error',
      autoHideMs:
        4000
    })
  } finally {
    loading.value = false
  }
}

onMounted(() => {

  map = L.map(mapEl.value)
    .setView(
      [-33.4489, -70.6693],
      5
    )


  L.tileLayer(
    'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    {
      attribution:
        '&copy; OpenStreetMap contributors'
    }
  ).addTo(map)


  map.on('click', handleMapClick)

})

onBeforeUnmount(() => {

  if(map){
    map.remove()
  }

})


</script>


<style scoped>

.modal-backdrop {

  position: fixed;

  inset: 0;

  background:
    rgba(15,23,42,0.6);

  display:flex;

  align-items:center;

  justify-content:center;

  padding:32px;

  z-index:1200;

}

.modal-card {

  width:min(88vw,820px);

  max-height: 90vh;

  background:white;

  border-radius:18px;

  box-shadow:
    0 20px 50px rgba(15,23,42,.25);

  overflow:hidden;

}

.modal-header {

  display:flex;

  justify-content:space-between;

  align-items:center;

  padding:22px 28px 10px;

  border-bottom:
    1px solid #e5e7eb;

}


.modal-header h2 {

  margin:0;

}



.icon-btn {

  border:none;

  background:transparent;

  font-size:1.4rem;

  cursor:pointer;

}



.modal-body {

  overflow-y:auto;

  max-height: calc(90vh - 80px);

  padding:20px 32px 28px;

  display:flex;

  flex-direction:column;

  gap:18px;

}



.form-row {

  display:grid;

  grid-template-columns:
    repeat(2,minmax(0,1fr));

  gap:18px;

}



.form-group {

  display:flex;

  flex-direction:column;

  gap:8px;

}



input,
select {

  padding:11px 12px;

  border-radius:10px;

  border:
    1px solid #d1d5db;

}



input:focus,
select:focus {

  outline:none;

  border-color:#2563eb;

}



.map-panel {

  background:#f8fafc;

  border:
    1px solid #e2e8f0;

  border-radius:14px;

  padding:16px;

}



.map-header {

  display:flex;

  justify-content:space-between;

  margin-bottom:12px;

  color:#475569;

}

.map-header h3 {

  margin:0;

  font-size:.95rem;

}

.location-details {

  margin-top: 8px;

  padding: 10px 12px;

  background: #f8fafc;

  border: 1px solid #e2e8f0;

  border-radius: 8px;

  display: flex;

  flex-direction: column;

  gap: 4px;

  font-size: 14px;

}

.map-container {
    width: 100%;
    height: 300px;
}



.form-error {

  color:#b91c1c;

}



.modal-actions {

  display:flex;

  justify-content:flex-end;

  gap:12px;

}



.primary-btn {

  background:#2563eb;

  color:white;

  border:none;

  padding:10px 18px;

  border-radius:10px;

  cursor:pointer;

}



.secondary-btn {

  background:#e2e8f0;

  color:#1f2937;

  border:none;

  padding:10px 18px;

  border-radius:10px;

  cursor:pointer;

}


.primary-btn:disabled {

  opacity:.6;

  cursor:not-allowed;

}



@media(max-width:600px){

  .form-row{

    grid-template-columns:1fr;

  }

}


</style>