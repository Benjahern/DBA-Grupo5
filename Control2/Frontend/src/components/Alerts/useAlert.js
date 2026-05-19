import { reactive } from 'vue';

/**
 * Estado reactivo global de la alerta.
 * Al instanciarse fuera del composable, actúa como una única fuente de verdad
 * para toda la aplicación.
 */
const alerts = reactive({
    isVisible: false,
    message: '',
    severity: 'info',
    autoHideMs: 4000,
});

// Variable para almacenar el ID del temporizador y poder cancelarlo si es necesario
let timeoutId = null;

/**
 * Muestra una alerta en la interfaz y configura su cierre automático.
 * Si se lanza una nueva alerta mientras otra está visible, reinicia el 
 * temporizador para evitar que se cierre antes de tiempo.
 * * @param config - Objeto de configuración de la alerta.
 * @param config.message - El texto principal a mostrar.
 * @param config.severity - Tipo visual de la alerta (por defecto: 'info').
 * @param config.autoHideMs - Tiempo en milisegundos para auto-ocultarse (0 para mantener fija).
 */
export const showGlobalAlert = ({ message, severity = 'info', autoHideMs = 4000 }) => {
    alerts.message = message;
    alerts.severity = severity;
    alerts.autoHideMs = autoHideMs;
    alerts.isVisible = true;
    
    // Si ya existía una alerta corriendo, limpiamos su cronómetro
    if (timeoutId) {
        clearTimeout(timeoutId);
    }

    // Solo activamos el temporizador si el tiempo asignado es mayor a 0    
    if (autoHideMs > 0) {
        timeoutId = setTimeout(() => {
            alerts.isVisible = false;
            timeoutId = null;
        }, autoHideMs);
    }
};

/**
 * Oculta manualmente la alerta actual de forma inmediata, 
 * ignorando cualquier temporizador activo.
 */
export const hideGlobalAlert = () => {
    alerts.isVisible = false;
};

/**
 * Hook principal (Composable) para ser importado en los componentes de Vue.
 * * @returns Un objeto que contiene el estado reactivo (`alerts`) y los métodos de control (`show` y `hide`).
 */
export function useAlert () {
    return { alerts,
        show: showGlobalAlert, hide: hideGlobalAlert};
}
