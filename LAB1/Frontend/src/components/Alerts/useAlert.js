import { reactive } from 'vue';

const alerts = reactive({
    isVisible: false,
    message: '',
    severity: 'info',
    autoHideMs: 4000,
});

let timeoutId = null;

export const showGlobalAlert = ({ message, severity = 'info', autoHideMs = 4000 }) => {
    alerts.message = message;
    alerts.severity = severity;
    alerts.autoHideMs = autoHideMs;
    alerts.isVisible = true;
    
    if (timeoutId) {
        clearTimeout(timeoutId);
    }

    if (autoHideMs > 0) {
        timeoutId = setTimeout(() => {
            alerts.isVisible = false;
            timeoutId = null;
        }, autoHideMs);
    }
};

export const hideGlobalAlert = () => {
    alerts.isVisible = false;
};

export function useAlert () {
    return { alerts,
        show: showGlobalAlert, hide: hideGlobalAlert};
}
