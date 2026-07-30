<template>
    <div class="modal-overlay" @click.self="$emit('close')">
        <div class="modal-container">
            <header class="modal-header">
                <h3>🌍 Resiliencia Geográfica</h3>
                <button class="close-btn" @click="$emit('close')">&times;</button>
            </header>

            <div class="modal-body">
                <p class="description">
                    Ingresa el ID de la instancia para previsualizarla y encontrar datacenters recomendados.
                </p>

                <div class="search-group">
                    <input
                        v-model="localInstanceId"
                        type="number"
                        min="1"
                        step="1"
                        placeholder="ID numérico de la instancia (ej. 1, 2, 3)"
                        @keyup.enter="fetchInstancePreview"
                    />
                    <button
                        class="search-btn"
                        @click="fetchInstancePreview"
                        :disabled="isSearchingInstance || !isValidInstanceId(localInstanceId)"
                    >
                        {{ isSearchingInstance ? 'Buscando...' : 'Buscar Instancia' }}
                    </button>
                </div>

                <div v-if="localInstanceId && !isValidInstanceId(localInstanceId)" class="alert warning">
                    El ID debe ser un número entero positivo (mayor o igual a 1).
                </div>

                <div v-if="instanceErrorMsg" class="alert error">{{ instanceErrorMsg }}</div>

                <div v-if="instancePreview" class="preview-section">
                    <h4 class="section-title">Instancia Seleccionada</h4>
                    <div class="instance-preview-card">
                        
                        <div class="preview-header">
                            <div class="header-left">
                                <h5>{{ instancePreview.name || 'Instancia ' + instancePreview.id }}</h5>
                                <span class="instance-state">{{ instancePreview.state || 'Desconocido' }}</span>
                            </div>
                            
                            <button 
                                v-if="searched" 
                                class="toggle-details-btn" 
                                @click="isExpanded = !isExpanded"
                            >
                                {{ isExpanded ? 'Ocultar' : 'Ver detalles' }}
                            </button>
                        </div>
                        
                        <div class="accordion-wrapper" :class="{ 'is-open': isExpanded }">
                            <div class="accordion-inner">
                                <div class="preview-details-box">
                                    <div class="detail-item"><strong>IP:</strong> {{ instancePreview.ip || instancePreview.ip_address || 'N/A' }}</div>
                                    <div class="detail-item"><strong>Región:</strong> {{ getRegionName(instancePreview.regionId ?? instancePreview.region_id ?? instancePreview.region) }}</div>
                                    <div class="detail-item"><strong>CPU:</strong> {{ getCpuInfo(instancePreview.cpuId ?? instancePreview.cpu_id ?? instancePreview.cpu) }}</div>
                                    <div class="detail-item"><strong>RAM:</strong> {{ getRamInfo(instancePreview.ramId ?? instancePreview.ram_id ?? instancePreview.ram) }}</div>
                                    <div class="detail-item"><strong>Storage:</strong> {{ getStorageInfo(instancePreview.storageId ?? instancePreview.storage_id ?? instancePreview.storage) }}</div>
                                </div>
                                
                                <button 
                                    class="action-btn" 
                                    @click="searched ? (isExpanded = false) : fetchRecommendations()"
                                    :disabled="isLoadingRecommendations"
                                >
                                    <span v-if="isLoadingRecommendations">Calculando...</span>
                                    <span v-else>Ver Datacenters Recomendados</span>
                                </button>
                            </div>
                        </div>

                    </div>
                </div>

                <div v-if="recErrorMsg" class="alert error">{{ recErrorMsg }}</div>
                <div v-if="!isLoadingRecommendations && searched && recommendations.length === 0 && !recErrorMsg" class="alert warning">
                    No se encontraron recomendaciones para esta instancia.
                </div>

                <div v-if="recommendations.length > 0" class="results-container">
                    <h4>Top 3 Datacenters Recomendados</h4>
                    
                    <div class="datacenter-list">
                        <div 
                            v-for="(item, index) in recommendations" 
                            :key="index" 
                            class="datacenter-card"
                        >
                            <div class="card-header">
                                <h5>{{ item.name }}</h5>
                                <span 
                                    class="status-badge" 
                                    :class="statusClass(item.status)"
                                >
                                    {{ item.status }}
                                </span>
                            </div>
                            
                            <div class="card-body">
                                <div class="info-grid">
                                    <div class="info-item">
                                        <span class="label">Instancias:</span>
                                        <span class="value">{{ item.currentInstances }} / {{ item.capacity }}</span>
                                    </div>
                                    <div class="info-item">
                                        <span class="label">Región:</span>
                                        <span class="value">{{ item.regionName }}</span>
                                    </div>
                                    <div class="info-item">
                                        <span class="label">Zona de Riesgo:</span>
                                        <span class="value">{{ item.riskZoneName }}</span>
                                    </div>
                                    <div class="info-item">
                                        <span class="label">Ubicación:</span>
                                        <span class="value">{{ item.latitude }}, {{ item.longitude }}</span>
                                    </div>
                                </div>
                                
                                <div class="distance-row" v-if="item.distanceKm != null">
                                    <span class="label">Distancia estimada:</span>
                                    <span class="value highlight">{{ Number(item.distanceKm).toFixed(2) }} km</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import api from '../../services/http-common.js'; 
import { getUser } from '../../services/auth.js';

const emit = defineEmits(['close']);

const props = defineProps({
    instanceId: {
        type: [String, Number],
        default: ''
    },
    userRole: {
        type: String,
        default: 'user'
    },
    userId: {
        type: [String, Number],
        default: ''
    }
});

const localInstanceId = ref(props.instanceId);

const regions = ref([]); 
const riskZones = ref([]); 
const cpus = ref([]);
const rams = ref([]);
const storages = ref([]);

const instancePreview = ref(null);
const isSearchingInstance = ref(false);
const instanceErrorMsg = ref('');
const isExpanded = ref(true); 

const recommendations = ref([]);
const isLoadingRecommendations = ref(false);
const recErrorMsg = ref('');
const searched = ref(false);

watch(localInstanceId, () => {
    instancePreview.value = null;
    recommendations.value = [];
    searched.value = false;
    instanceErrorMsg.value = '';
    recErrorMsg.value = '';
});

watch(() => props.instanceId, (newVal) => {
    if (newVal && newVal !== localInstanceId.value) {
        localInstanceId.value = newVal;
    }
});

const loadRegions = async () => {
    try {
        const response = await api.get('/api/regions');
        regions.value = Array.isArray(response.data) ? response.data : [];
    } catch (err) {
        console.error('Error cargando regiones', err);
    }
};

const loadRiskZones = async () => {
    try {
        const response = await api.get('/api/risks');
        let data = typeof response.data === 'string' ? JSON.parse(response.data) : response.data;
        if (data && data.features) {
            riskZones.value = data.features;
        }
    } catch (err) {
        console.error('Error cargando zonas de riesgo', err);
    }
};

const fetchOptions = async () => {
    try {
        const [cpuResp, ramResp, storageResp] = await Promise.all([
            api.get('/api/cpus').catch(() => ({ data: [] })),
            api.get('/api/rams').catch(() => ({ data: [] })),
            api.get('/api/storages').catch(() => ({ data: [] }))
        ]);
        cpus.value = Array.isArray(cpuResp.data) ? cpuResp.data : [];
        rams.value = Array.isArray(ramResp.data) ? ramResp.data : [];
        storages.value = Array.isArray(storageResp.data) ? storageResp.data : [];
    } catch (error) {
        console.error('Error fetching options', error);
    }
};

onMounted(() => {
    loadRegions();
    loadRiskZones();
    fetchOptions();

    if (localInstanceId.value) {
        fetchInstancePreview();
    }
});

const statusClass = (status) => {
    switch(status) {
        case 'OPERATIVO': return 'status-operational'
        case 'MANTENIMIENTO': return 'status-maintenance'
        case 'DEGRADADO': return 'status-degraded'
        case 'FUERA_DE_SERVICIO': return 'status-offline'
        default: return ''
    }
};

const getRegionName = (id) => {
    if (!id) return 'N/A';
    const match = regions.value.find(r => String(r.region_id) === String(id));
    return match ? match.name : `Región ${id}`;
};

const getCpuInfo = (id) => {
    if (!id) return 'N/A';
    const match = cpus.value.find(c => String(c.cpu_id || c.id) === String(id));
    return match ? `${match.quantity} vCPU` : `${id}`;
};

const getRamInfo = (id) => {
    if (!id) return 'N/A';
    const match = rams.value.find(r => String(r.ram_id || r.id) === String(id));
    return match ? `${match.quantity} GB` : `${id}`;
};

const getStorageInfo = (id) => {
    if (!id) return 'N/A';
    const match = storages.value.find(s => String(s.storage_id || s.id) === String(id));
    return match ? `${match.quantity} GB` : `${id}`;
};

const isValidInstanceId = (value) => {
    const n = Number(value);
    return Number.isFinite(n) && Number.isInteger(n) && n >= 1;
};

const fetchInstancePreview = async () => {
    if (!localInstanceId.value) return;
    if (!isValidInstanceId(localInstanceId.value)) {
        instanceErrorMsg.value = 'El ID "' + localInstanceId.value + '" debe ser un número entero positivo.';
        return;
    }
    const numericId = Number(localInstanceId.value);

    isSearchingInstance.value = true;
    instanceErrorMsg.value = '';
    instancePreview.value = null;
    recommendations.value = [];
    searched.value = false;
    isExpanded.value = true;

    try {
        const response = await api.get(`/api/instances/${localInstanceId.value}`);
        const data = response.data;

        if (data) {
            const currentUser = getUser();

            if (!currentUser) {
                instanceErrorMsg.value = 'Debes iniciar sesión para ver esta información.';
                isSearchingInstance.value = false;
                return;
            }

            const currentUserId = currentUser.User_id || currentUser.Sub;
            const userRoles = currentUser.Roles || [];

            const isStrictlyUser = userRoles.length === 1 &&
                (userRoles[0].toLowerCase() === 'user' || userRoles[0].toLowerCase() === 'usuario');

            const instanceOwner = data.owner || data.user_id || data.User_id;

            if (isStrictlyUser && String(instanceOwner) !== String(currentUserId)) {
                instanceErrorMsg.value = 'Acceso denegado: Esta instancia no te pertenece.';
                isSearchingInstance.value = false;
                return;
            }

            instancePreview.value = data;
        } else {
            instanceErrorMsg.value = 'No se encontraron datos para esta instancia.';
        }
    } catch (error) {
        console.error('Error fetching instance:', error);
        const backendMsg = error?.response?.data?.error || error?.response?.data?.message;
        if (error.response?.status === 404) {
            instanceErrorMsg.value = 'La instancia con ID ' + localInstanceId.value + ' no existe.';
        } else if (error.response?.status === 403) {
            instanceErrorMsg.value = 'Acceso denegado a esta instancia.';
        } else if (error.response?.status >= 500) {
            instanceErrorMsg.value = 'Error del servidor (500). ' + (backendMsg || 'Verificá que el ObjectId sea válido.');
        } else {
            instanceErrorMsg.value = backendMsg || 'No se pudo obtener la instancia.';
        }
    } finally {
        isSearchingInstance.value = false;
    }
};

const fetchRecommendations = async () => {
    isLoadingRecommendations.value = true;
    recErrorMsg.value = '';
    recommendations.value = [];
    searched.value = true;
    isExpanded.value = false; 

    try {
        const recResponse = await api.get(`/api/datacenters/recommendations/${localInstanceId.value}`);
        const recData = Array.isArray(recResponse.data) ? recResponse.data : [];

        if (recData.length > 0) {
            const allDcResponse = await api.get('/api/datacenters');
            const allDatacenters = Array.isArray(allDcResponse.data) ? allDcResponse.data : [];

            recommendations.value = recData.map(rec => {
                const fullInfo = allDatacenters.find(dc => dc.id === rec.datacenterId);
                
                let regionName = 'N/A';
                let rawRegionId = fullInfo ? fullInfo.regionId : null;
                
                if (rawRegionId != null) {
                     const foundRegion = regions.value.find(r => r.region_id === Number(rawRegionId));
                     regionName = foundRegion ? foundRegion.name : `Región ${rawRegionId}`;
                }

                let riskZoneName = 'N/A';
                let rawRiskId = fullInfo ? fullInfo.riskZoneId : null;

                if (rawRiskId != null) {
                    const foundRisk = riskZones.value.find(f => f.properties.id === Number(rawRiskId));
                    riskZoneName = foundRisk && foundRisk.properties.name 
                        ? foundRisk.properties.name 
                        : `Zona ${rawRiskId}`;
                }

                return {
                    ...rec,
                    status: fullInfo ? fullInfo.status : 'DESCONOCIDO',
                    capacity: fullInfo ? fullInfo.capacity : 0,
                    currentInstances: fullInfo ? fullInfo.currentInstances : 0,
                    regionName: regionName,
                    riskZoneName: riskZoneName 
                };
            });
        }
        
    } catch (error) {
        console.error('Error fetching recommendations:', error);
        if (error.response?.status === 404) {
            recErrorMsg.value = 'No se encontró la instancia para recomendar.';
        } else {
            recErrorMsg.value = 'Ocurrió un error al buscar las recomendaciones.';
        }
    } finally {
        isLoadingRecommendations.value = false;
    }
};
</script>

<style scoped>
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(15, 23, 42, 0.6);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    backdrop-filter: blur(4px);
}

.modal-container {
    background: white;
    width: 100%;
    max-width: 550px;
    border-radius: 12px;
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
    margin: 0;
    color: #0f172a;
    font-size: 18px;
}

.close-btn {
    background: none;
    border: none;
    font-size: 24px;
    color: #64748b;
    cursor: pointer;
}

.close-btn:hover {
    color: #ef4444;
}

.modal-body {
    padding: 20px;
    overflow-y: auto;
}

.description {
    color: #64748b;
    font-size: 14px;
    margin-bottom: 16px;
    margin-top: 0;
}

.search-group {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
}

.search-group input {
    flex: 1;
    padding: 10px 14px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    font-size: 14px;
    outline: none;
    transition: border-color 0.2s, box-shadow 0.2s;
}

.search-group input:focus {
    border-color: #2563eb;
    box-shadow: 0 0 0 3px rgba(37,99,235,.15);
}

.search-btn {
    background: #1e293b;
    color: white;
    border: none;
    padding: 0 20px;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.2s;
}

.search-btn:hover:not(:disabled) {
    background: #0f172a;
}

.search-btn:disabled {
    background: #9ca3af;
    cursor: not-allowed;
}

.alert {
    padding: 12px;
    border-radius: 8px;
    font-size: 14px;
    margin-bottom: 16px;
}

.alert.error {
    background-color: #fee2e2;
    color: #b91c1c;
    border: 1px solid #fecaca;
}

.alert.warning {
    background-color: #fef3c7;
    color: #92400e;
    border: 1px solid #fde68a;
}

.section-title {
    text-align: center;
    margin: 16px 0 12px 0;
    color: #1e293b;
    font-size: 15px;
}

.instance-preview-card {
    border: 1px solid #cbd5e1;
    border-radius: 10px;
    padding: 16px;
    background: #f8fafc;
    margin-bottom: 24px;
}

.preview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid #e2e8f0;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 12px;
}

.preview-header h5 {
    margin: 0;
    font-size: 16px;
    color: #0f172a;
}

.instance-state {
    font-size: 12px;
    font-weight: 600;
    color: #2563eb;
    background: #dbeafe;
    padding: 4px 10px;
    border-radius: 12px;
}

.toggle-details-btn {
    background: none;
    border: none;
    color: #2563eb;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    padding: 0;
    transition: color 0.2s;
}

.toggle-details-btn:hover {
    color: #1d4ed8;
    text-decoration: underline;
}

.accordion-wrapper {
    display: grid;
    grid-template-rows: 0fr;
    transition: grid-template-rows 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease, visibility 0.3s;
    opacity: 0;
    visibility: hidden;
}

.accordion-wrapper.is-open {
    grid-template-rows: 1fr;
    opacity: 1;
    visibility: visible;
}

.accordion-inner {
    overflow: hidden;
    padding-top: 16px;
}

.preview-details-box {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 10px 16px;
    background-color: #f1f5f9;
    padding: 12px 16px;
    border-radius: 8px;
    margin-bottom: 16px;
}

.detail-item {
    font-size: 13px;
    color: #475569;
}

.detail-item strong {
    color: #0f172a;
    font-weight: 600;
    margin-right: 4px;
}

.action-btn {
    width: 100%;
    background: #10b981;
    color: white;
    border: none;
    padding: 10px;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.2s;
}

.action-btn:hover:not(:disabled) {
    background: #059669;
}

.action-btn:disabled {
    background: #6ee7b7;
    cursor: not-allowed;
}

.results-container h4 {
    margin: 0 0 12px 0;
    color: #1e293b;
    font-size: 15px;
}

.datacenter-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.datacenter-card {
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    padding: 16px;
    background: #ffffff;
    box-shadow: 0 2px 4px rgba(15,23,42,.02);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f1f5f9;
}

.card-header h5 {
    margin: 0;
    font-size: 16px;
    color: #0f172a;
}

.status-badge {
    display: inline-flex;
    align-items: center;
    padding: 5px 10px;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 600;
}
.status-operational { background: #dcfce7; color: #166534; }
.status-maintenance { background: #fef3c7; color: #92400e; }
.status-degraded { background: #fee2e2; color: #991b1b; }
.status-offline { background: #e5e7eb; color: #374151; }

.info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    margin-bottom: 12px;
}

.info-item {
    display: flex;
    flex-direction: column;
    font-size: 13px;
}

.info-item .label {
    color: #64748b;
    margin-bottom: 2px;
}

.info-item .value {
    color: #334155;
    font-weight: 500;
}

.distance-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px dashed #e2e8f0;
    font-size: 14px;
}

.distance-row .label {
    color: #475569;
    font-weight: 500;
}

.distance-row .value.highlight {
    color: #2563eb;
    font-weight: 700;
    font-size: 15px;
}
</style>