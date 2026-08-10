import { ref, onMounted, onUnmounted } from 'vue';
import { openAlertStream, closeAlertStream } from '../../services/alerts-stream.js';

/**
 * Composable que gestiona la conexión a Change Streams de alertas
 * y mantiene el estado reactivo de las alertas del usuario.
 * 
 * @param {Long} userId - ID del usuario (obtenido del contexto de autenticación)
 * @returns {Object} Objeto reactivo con alertas y métodos de control
 */
export function useAlertsStream(userId) {
  const alerts = ref([]);
  let eventSource = null;

  /**
   * Inicializa la conexión al stream de alertas del backend.
   * Este composable se suscribe al Change Stream y recibe alertas en tiempo real.
   */
  const startStream = () => {
    if (!userId) {
      console.warn('userId no disponible para iniciar stream de alertas');
      return;
    }

    eventSource = openAlertStream(userId, {
      // Cuando se recibe una alerta desde el Change Stream
      onAlert: (alertData) => {
        console.log('Nueva alerta recibida:', alertData);
        
        // Agregar la alerta al inicio del array (alertas más recientes primero)
        alerts.value.unshift(alertData);

        // Opcional: limitar a últimas 10 alertas para no saturar memoria
        if (alerts.value.length > 10) {
          alerts.value.pop();
        }
      },

      onError: (error) => {
        console.error('Error en stream de alertas:', error);
        // Aquí podrías implementar reconexión automática
      },
    });
  };

  /**
   * Marca una alerta como leída
   * En el futuro, esto llamaría a un endpoint para actualizar en BD
   */
  const markAsRead = (alertIndex) => {
    if (alerts.value[alertIndex]) {
      alerts.value[alertIndex].read = true;
    }
  };

  /**
   * Limpia la lista de alertas
   */
  const clearAlerts = () => {
    alerts.value = [];
  };

  /**
   * Elimina una alerta específica de la lista
   */
  const removeAlert = (alertIndex) => {
    alerts.value.splice(alertIndex, 1);
  };

  // Inicia el stream cuando el componente se monta
  onMounted(() => {
    startStream();
  });

  // Limpia la conexión cuando el componente se desmonta
  onUnmounted(() => {
    closeAlertStream(eventSource);
  });

  return {
    alerts,
    markAsRead,
    clearAlerts,
    removeAlert,
    startStream,
  };
}
