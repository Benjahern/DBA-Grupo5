<!-- Trabajando en el modal-->
<template>
	<Teleport to="body">
		<div class="modal-overlay" @click.self="emit('close')">
			<div class="modal-card">
				<header class="modal-header">
					<h2>Nueva instancia</h2>
					<button class="close-btn" @click="emit('close')">X</button>
				</header>
				<div class="modal-body">
					<form class="form-grid" @submit.prevent>
						<label class="field">
							<span>Nombre</span>
							<input v-model="name" type="text" placeholder="Nombre de la instancia" />
						</label>

						<label class="field">
							<span>Region</span>
							<select v-model="regionId">
								<option value="">Selecciona Region</option>
								<option v-for="region in regions" :key="region.Region_id" :value="region.Region_id">
									{{ region.Name }}
								</option>
							</select>
						</label>

						<label class="field">
							<span>CPU</span>
							<select v-model="cpuId">
								<option value="">Selecciona CPU</option>
								<option v-for="cpu in cpus" :key="cpu.Cpu_id" :value="cpu.Cpu_id">
									{{ cpu.Quantity }} vCPU
								</option>
							</select>
						</label>

						<label class="field">
							<span>RAM</span>
							<select v-model="ramId">
								<option value="">Selecciona RAM</option>
								<option v-for="ram in rams" :key="ram.Ram_id" :value="ram.Ram_id">
									{{ ram.Quantity }} GB
								</option>
							</select>
						</label>

						<label class="field">
							<span>Almacenamiento</span>
							<select v-model="storageId">
								<option value="">Selecciona Almacenamiento</option>
								<option v-for="storage in storages" :key="storage.Storage_id" :value="storage.Storage_id">
									{{ storage.Quantity }} GB
								</option>
							</select>
						</label>
					</form>
					<div class="form-actions">
						<button type="button" class="btn primary" @click="openConfirm">Confirmar</button>
						<button type="button" class="btn ghost" @click="handleCancel">Cancelar</button>
					</div>
				</div>
			</div>
		</div>

		<ConfirmInstance
			v-if="confirmOpen"
			:summary="confirmSummary"
			@confirm="handleConfirm"
			@cancel="closeConfirm"
		/>
	</Teleport>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import api from '../../services/http-common.js';
import { useAlert } from '../Alerts/useAlert.js';
import ConfirmInstance from './ConfirmInstance.vue';

const emit = defineEmits(['close', 'confirm']);
const { show } = useAlert();

const name = ref('');
const cpuId = ref('');
const ramId = ref('');
const storageId = ref('');
const regionId = ref('');

const cpus = ref([]);
const rams = ref([]);
const storages = ref([]);
const regions = ref([]);
const confirmOpen = ref(false);
const isSubmitting = ref(false);

const findLabel = (items, id, valueKey = 'id', labelKey = 'Name') => {
	const match = items.find((item) => String(item[valueKey]) === String(id));
	return match ? match[labelKey] : '';
};

const confirmSummary = computed(() => ({
	name: name.value,
	region: findLabel(regions.value, regionId.value, 'Region_id', 'Name'),
	cpu: findLabel(cpus.value, cpuId.value, 'Cpu_id', 'Quantity')
		? `${findLabel(cpus.value, cpuId.value, 'Cpu_id', 'Quantity')} vCPU`
		: '',
	ram: findLabel(rams.value, ramId.value, 'Ram_id', 'Quantity')
		? `${findLabel(rams.value, ramId.value, 'Ram_id', 'Quantity')} GB`
		: '',
	storage: findLabel(storages.value, storageId.value, 'Storage_id', 'Quantity')
		? `${findLabel(storages.value, storageId.value, 'Storage_id', 'Quantity')} GB`
		: ''
}));

const fetchOptions = async () => {
	try {
		const [cpuResp, ramResp, storageResp, regionResp] = await Promise.all([
			api.get('/api/cpus'),
			api.get('/api/rams'),
			api.get('/api/storages'),
			api.get('/api/regions'),
		]);
		cpus.value = Array.isArray(cpuResp.data) ? cpuResp.data : [];
		rams.value = Array.isArray(ramResp.data) ? ramResp.data : [];
		storages.value = Array.isArray(storageResp.data) ? storageResp.data : [];
		regions.value = Array.isArray(regionResp.data) ? regionResp.data : [];
	} catch (error_) {
		cpus.value = [];
		rams.value = [];
		storages.value = [];
		regions.value = [];
	}
};

onMounted(fetchOptions);

const handleCancel = () => {
	emit('close');
};

const openConfirm = () => {
	confirmOpen.value = true;
};

const closeConfirm = () => {
	confirmOpen.value = false;
};

const resolveUserId = () => {
	try {
		const raw = localStorage.getItem('user');
		const stored = raw ? JSON.parse(raw) : null;
		const candidate = stored?.User_id ?? stored?.user_id ?? stored?.id;
		const parsed = Number(candidate);
		return Number.isFinite(parsed) ? parsed : null;
	} catch (error_) {
		return null;
	}
};

const handleConfirm = () => {
	if (isSubmitting.value) return;

	const userId = resolveUserId();
	if (!userId) {
		show({
			message: 'No se encontro el usuario para crear la instancia.',
			severity: 'error',
			autoHideMs: 4000
		});
		return;
	}

	const payload = {
		name: name.value,
		cpuId: cpuId.value || null,
		ramId: ramId.value || null,
		storageId: storageId.value || null,
		regionId: regionId.value || null,
		userId,
		color: '#609df0',
		baseImage: 'ubuntu:latest'
	};

	isSubmitting.value = true;

	api.post('/api/instances', payload)
		.then((response) => {
			emit('confirm', response?.data || payload);
			confirmOpen.value = false;
			emit('close');
		})
		.catch(() => {
			show({
				message: 'No se pudo crear la instancia.',
				severity: 'error',
				autoHideMs: 4000
			});
		})
		.finally(() => {
			isSubmitting.value = false;
		});
};
</script>

<style scoped>
.modal-overlay {
	position: fixed;
	inset: 0;
	background: rgba(15, 23, 42, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1500;
}

.modal-card {
	width: min(520px, 92vw);
	background: #ffffff;
	border-radius: 12px;
	padding: 20px;
	box-shadow: 0 16px 40px rgba(15, 23, 42, 0.2);
}

.modal-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 12px;
}

.modal-header h2 {
	margin: 0;
	font-size: 20px;
}

.close-btn {
	border: none;
	background: #e2e8f0;
	color: #0f172a;
	padding: 3px 6px;
	border-radius: 999px;
	cursor: pointer;
	font-weight: 600;
}

.modal-body {
	color: #64748b;
}

.form-grid {
	display: grid;
	gap: 12px;
}

.field {
	display: grid;
	gap: 6px;
	color: #0f172a;
}

.field span {
	font-size: 13px;
	font-weight: 600;
}

.field input,
.field select {
	border: 1px solid #e2e8f0;
	border-radius: 8px;
	padding: 10px 12px;
	font-size: 14px;
	background: #ffffff;
	color: #0f172a;
}

.form-actions {
	display: flex;
	justify-content: flex-end;
	gap: 10px;
	margin-top: 16px;
}

.btn {
	border: none;
	border-radius: 999px;
	padding: 8px 16px;
	font-weight: 600;
	cursor: pointer;
}

.btn.ghost {
	background: #e2e8f0;
	color: #0f172a;
}

.btn.primary {
	background: #2563eb;
	color: #ffffff;
}

</style>