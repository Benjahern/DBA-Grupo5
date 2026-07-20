<template>
	<section class="dashboard">
		<div class="instances-card">
			<header class="card-header">
				<h2>Todas las instancias (Admin) {{ activeCount }} de {{ totalCount }}</h2>
				<div class="header-actions">
					<ResilienceButton />
				</div>
			</header>

			<div class="table-header">
				<div class="table-cell">Status</div>
				<div class="table-cell">Name</div>
				<div class="table-cell">Owner</div>
				<div class="table-cell">Region</div>
				<div class="table-cell">Ip</div>
				<div class="table-cell">State</div>
				<div class="table-cell">CPU</div>
				<div class="table-cell">RAM</div>
				<div class="table-cell">Storage</div>
			</div>

			<div v-if="isLoading" class="loading">Cargando instancias...</div>

			<div v-else class="instance-list">
				<InstanceContainer
					v-for="(instance, index) in viewInstances"
					:key="instance.id ?? index"
					:instance="instance"
					@updated="fetchInstances"
				/>
			</div>
		</div>
	</section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import api from '../services/http-common.js';
import { useAlert } from '../components/Alerts/useAlert.js';
import InstanceContainer from '../components/Instance Container/InstanceContainer.vue';
import ResilienceButton from '../components/Instance Actions Buttons/ResilienceButton.vue';

const { show } = useAlert();

const instances = ref([]);
const isLoading = ref(false);

const cpus = ref([]);
const rams = ref([]);
const storages = ref([]);
const regions = ref([]);

const fetchOptions = async () => {
	try {
		const cpuResp = await api.get('/api/cpus').catch(() => ({ data: [] }));
		const ramResp = await api.get('/api/rams').catch(() => ({ data: [] }));
		const storageResp = await api.get('/api/storages').catch(() => ({ data: [] }));
		const regionResp = await api.get('/api/regions').catch(() => ({ data: [] }));

		cpus.value = Array.isArray(cpuResp.data) ? cpuResp.data : [];
		rams.value = Array.isArray(ramResp.data) ? ramResp.data : [];
		storages.value = Array.isArray(storageResp.data) ? storageResp.data : [];
		regions.value = Array.isArray(regionResp.data) ? regionResp.data : [];
	} catch (error_) {
		console.error('Error fetching options', error_);
	}
};

const findLabel = (items, id, valueKey = 'id', labelKey = 'name') => {
	const match = items.find((item) => String(item[valueKey]) === String(id));
	return match ? match[labelKey] : id;
};

const toViewInstance = (raw) => {
	const cpuId = raw?.cpu ?? raw?.cpu_id ?? raw?.Cpu_id;
	const ramId = raw?.ram ?? raw?.ram_id ?? raw?.Ram_id;
	const storageId = raw?.storage ?? raw?.storage_id ?? raw?.Storage_id;
	const regionId = raw?.region ?? raw?.region_id ?? raw?.Region_id;

	return {
		id: raw?.instance_id ?? raw?.Instance_id ?? raw?.id,
		name: raw?.name ?? raw?.Name ?? '-',
		region: findLabel(regions.value, regionId, 'region_id', 'name') || regionId || '-',
		ip: raw?.ip ?? raw?.ip_address ?? raw?.Ip_address ?? '-',
		state: raw?.state ?? raw?.State ?? '-',
		cpu: (findLabel(cpus.value, cpuId, 'cpu_id', 'quantity') || cpuId || '-') + ' vCPU',
		ram: (findLabel(rams.value, ramId, 'ram_id', 'quantity') || ramId || '-') + ' GB',
		storage: (findLabel(storages.value, storageId, 'storage_id', 'quantity') || storageId || '-') + ' GB',
		userId: raw?.user_id ?? raw?.User_id ?? '-',
	};
};

const viewInstances = computed(() => instances.value.map(toViewInstance));

const activeCount = computed(() =>
	viewInstances.value.filter((instance) =>
		String(instance.state || '').toLowerCase() === 'running'
	).length
);

const totalCount = computed(() => viewInstances.value.length);

const fetchInstances = async () => {
	isLoading.value = true;

	try {
		// Fetch ALL instances (no userId filter for admin)
		const response = await api.get('/api/instances');
		instances.value = Array.isArray(response.data) ? response.data : [];

		if (instances.value.length === 0) {
			show({
				message: 'No se cuenta con ninguna instancia creada.',
				severity: 'warning',
				autoHideMs: 4000
			});
		}
	} catch (err) {
		show({
			message: 'No se pudieron cargar las instancias. Intenta nuevamente.',
			severity: 'error',
			autoHideMs: 4000
		});
	} finally {
		isLoading.value = false;
	}
};

onMounted(async () => {
	await fetchOptions();
	fetchInstances();
});
</script>

<style scoped>
.dashboard {
	display: flex;
	justify-content: center;
	padding: 24px 16px;
}

.instances-card {
	width: 100%;
	max-width: 1200px;
	border: 1px solid #e5e7eb;
	border-radius: 12px;
	background: #ffffff;
	box-shadow: 0 10px 20px rgba(0, 0, 0, 0.06);
	padding: 20px;
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12px;
}

.card-header h2 {
	margin: 0;
	font-size: 20px;
	color: #0f172a;
}

.instance-list {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.table-header {
	display: grid;
	grid-template-columns: 5% 15% 10% 15% 10% 10% 7% 7% 7%;
	gap: 4px;
	padding: 8px 0;
	margin-bottom: 8px;
	border-bottom: 1px solid #e2e8f0;
	color: #64748b;
	font-weight: 600;
	text-transform: uppercase;
	font-size: 12px;
	letter-spacing: 0.04em;
	align-items: center;
}

.table-cell {
	display: flex;
	align-items: center;
}

.loading {
	padding: 16px 0;
	color: #64748b;
	font-weight: 600;
}
</style>