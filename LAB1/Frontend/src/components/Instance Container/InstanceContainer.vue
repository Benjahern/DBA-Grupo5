<template>
    <div class="instance-container">

        <div class="instance-info">
            <div class="status-cell">
                <Badge v-if="badgeVariant" :variant="badgeVariant" :title="localState" />
            </div>
            <p> {{ instance.name }}</p>
            <p> {{ instance.region }}</p>
            <p> {{ instance.ip }}</p>
            <p> {{ localState }}</p>
            <p> {{ instance.cpu }}</p>
            <p> {{ instance.ram }}</p>
            <p> {{ instance.storage }}</p>  
        </div>

        <div class="buttons">
            <RunButton v-if="isPaused" :instance-id="instanceId" @updated="handleUpdated" />
            <PauseButton v-if="isRunning" :instance-id="instanceId" @updated="handleUpdated" />
            <DeleteButton :instance-id="instanceId" @updated="handleUpdated" />
            <StatsButton :instance-id="instanceId" />
        </div>
    </div>
</template>



<script setup>
import { computed, ref, watch } from 'vue';
import RunButton from '../Instance Actions Buttons/RunButton.vue';
import PauseButton from '../Instance Actions Buttons/PauseButton.vue';
import DeleteButton from '../Instance Actions Buttons/DeleteButton.vue';
import StatsButton from '../Instance Actions Buttons/StatsButton.vue';
import Badge from '../Badges/Badge.vue';
import { statusToBadgeVariant } from '../Badges/statusToBadgeVariant.js';

const props = defineProps({
    instance: Object
});

const localState = ref(props.instance?.state ?? '');

watch(
    () => props.instance?.state,
    (nextState) => {
        localState.value = nextState ?? '';
    }
);

const stateValue = computed(() => String(localState.value || '').toLowerCase());
const isRunning = computed(() => stateValue.value === 'running');
const isPaused = computed(() => stateValue.value === 'stopped');
const instanceId = computed(() => props.instance?.id ?? props.instance?.instance_id);
const badgeVariant = computed(() => statusToBadgeVariant(localState.value));

const handleUpdated = (nextState) => {
    localState.value = nextState;
};

</script>

<style scoped>
.instance-container {
    border: 1px solid #ccc;
    border-radius: 7px;
    padding: 4px;
    margin: 16px 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.instance-info {
    display: grid;
    flex-direction: row;
    grid-template-columns: 28px 1fr 1fr 1fr 1fr 1fr 1fr 1fr;
    gap: 4px;
}

.status-cell {
    display: flex;
    align-items: center;
    justify-content: center;
}

.buttons {
    display: grid;
    grid-template-columns: repeat(3, auto);
    gap: 8px;
    flex-direction: row;
}

</style>