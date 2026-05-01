<template>
	<section class="dashboard">
		<div class="instances-card">
			<header class="card-header">
				<h2>Instancias activas del usuario {{ activeCount }} de {{ totalCount }}</h2>
			</header>

			<div class="table-header">
				<div class="table-cell"></div>
				<div class="table-cell">Name</div>
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
				/>
			</div>
		</div>
	</section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import api from '../../services/http-common.js';
import { useAlert } from '../Alerts/useAlert.js';
import { getUser } from '../../services/auth.js';
import InstanceContainer from './InstanceContainer.vue';

const { show } = useAlert();

const instances = ref([]);
const isLoading = ref(false);

const readStoredUser = () => {
	try {
		const raw = localStorage.getItem('user');
		return raw ? JSON.parse(raw) : null;
	} catch (error_) {
		return null;
	}
};

const resolveUserIdFromUser = (user) => {
	const candidate = user?.userId ?? user?.user_id ?? user?.User_id ?? user?.id;
	const parsed = Number(candidate);
	return Number.isFinite(parsed) ? parsed : null;
};

const resolveUserIdFromToken = () => {
	const user = getUser();
	const candidate = user?.userId ?? user?.user_id ?? user?.User_id ?? user?.id ?? user?.sub;
	const parsed = Number(candidate);
	return Number.isFinite(parsed) ? parsed : null;
};

const fetchCurrentUser = async () => {
	try {
		const response = await api.get('/api/users/me');
		if (response?.data) {
			localStorage.setItem('user', JSON.stringify(response.data));
			return response.data;
		}
	} catch (error_) {
		return null;
	}
	return null;
};

const toViewInstance = (raw) => ({
	id: raw?.instance_id ?? raw?.Instance_id ?? raw?.id,
	name: raw?.name ?? raw?.Name ?? '-',
	region: raw?.region ?? raw?.Region ?? raw?.region_id ?? raw?.Region_id ?? '-',
	ip: raw?.ip ?? raw?.Ip_address ?? '-',
	state: raw?.state ?? raw?.State ?? '-',
	cpu: raw?.cpu ?? raw?.Cpu_id ?? '-',
	ram: raw?.ram ?? raw?.Ram_id ?? '-',
	storage: raw?.storage ?? raw?.Storage_id ?? '-',
});

const viewInstances = computed(() => instances.value.map(toViewInstance));

const activeCount = computed(() =>
	viewInstances.value.filter((instance) =>
		String(instance.state || '').toLowerCase() === 'running'
	).length
);

const totalCount = computed(() => viewInstances.value.length);

const fetchInstances = async () => {
	let userId = resolveUserIdFromUser(readStoredUser());
	if (!userId) {
		userId = resolveUserIdFromToken();
	}
	if (!userId) {
		const fetchedUser = await fetchCurrentUser();
		userId = resolveUserIdFromUser(fetchedUser);
	}
	if (!userId) {
		show({
			message: 'No se pudo cargar instancias: No se encontro el id del usuario.',
			severity: 'error',
			autoHideMs: 4000
		});
		instances.value = [];
		return;
	}

	isLoading.value = true;

	try {
		const response = await api.get('/api/instances', { params: { userId } });
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

onMounted(fetchInstances);
</script>

<style scoped>
.dashboard {
	display: flex;
	justify-content: center;
	padding: 24px 16px;
}

.instances-card {
	width: 100%;
	max-width: 980px;
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
	grid-template-columns: 28px 1fr 1fr 1fr 1fr 1fr 1fr 1fr;
	gap: 4px;
	padding: 8px 0;
	margin-bottom: 8px;
	border-bottom: 1px solid #e2e8f0;
	color: #64748b;
	font-weight: 600;
	text-transform: uppercase;
	font-size: 12px;
	letter-spacing: 0.04em;
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
