import { apiBaseUrl } from './http-common.js';
import { showGlobalAlert } from '../components/Alerts/useAlert.js';

/**
 * Abre una conexión SSE al endpoint de alertas del backend.
 * El backend usa Change Streams para monitorear la colección bandwidth_usage
 * y emite AlertDocument en tiempo real via Flux.
 * 
 * @param {Long} userId - ID del usuario para filtrar alertas
 * @param {Object} handlers - Callbacks para manejar eventos
 * @param {Function} handlers.onAlert - Llamado cuando se recibe una alerta
 * @param {Function} handlers.onError - Llamado en caso de error
*/

export function openAlertStream(userId, handlers = {}) {
  const { onAlert, onError } = handlers;

  // Construir URL del stream - el backend filtra por userId
  const streamUrl = `${apiBaseUrl}/api/alerts/stream/${userId}`;

  // Crear EventSource para conectar con SSE (Server-Sent Events)
  const eventSource = new EventSource(streamUrl, {
    withCredentials: true, // Enviar cookies HttpOnly
  });

  let buffer = '';
  const decoder = new TextDecoder();

  eventSource.onmessage = (event) => {
    try {
      buffer += event.data;

      // SSE emite los datos completos en el campo 'data'
      const alertData = JSON.parse(buffer);
      buffer = '';

      // Llamar al handler si existe
      if (typeof onAlert === 'function') {
        onAlert(alertData);
      }

      // Mostrar alerta global dependiendo del tipo
      if (alertData.alertType === 'BANDWIDTH_QUOTA_EXCEEDED' || alertData.alertType === 'CPU_USAGE_HIGH') {
        showGlobalAlert({
          message: alertData.message || 'Has superado un umbral de uso',
          severity: 'warning',
          autoHideMs: 0, // Mantener visible hasta que el usuario la cierre
        });
      }
    } catch (error) {
      console.error('Error parsing alert:', error);
    }
  };

  eventSource.onopen = () => {
    console.log('Alert stream connected');
  }

  eventSource.onerror = (error) => {
    console.error('Alert stream error:', error);

    if (eventSource.readyState === EventSource.CLOSED) {
      console.warn('Alert stream closed permanently');
      if (typeof onError === 'function') {
        onError(error);
      }
    }
  };

  // Retornar la instancia para que el componente pueda cerrar el stream
  return eventSource;
}

/**
 * Cierra la conexión SSE con el servidor
 * @param {EventSource} eventSource - Instancia del EventSource a cerrar
 */
export function closeAlertStream(eventSource) {
  if (eventSource) {
    eventSource.close();
  }
}