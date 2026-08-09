# Implementación de Change Streams - Frontend

## Resumen Ejecutivo

Se han implementado **3 componentes clave** en el frontend para completar la cadena de Change Streams desde MongoDB hasta la UI en tiempo real.

---

## Arquitectura End-to-End

### 1. **Backend (MongoDB Change Streams)**
```
MongoDB bandwidth_usage collection
        ↓
BandwidthAlertListenerService (escucha inserciones via Change Stream)
        ↓
checkAndAlertUser() → calcula consumo total
        ↓
si consumo >= THRESHOLD → AlertService.pushAlert(AlertDocument)
        ↓
Sinks.Many<AlertDocument> (broadcast reactor)
```

### 2. **Backend (REST Endpoint SSE)**
```
AlertController /api/alerts/stream/{userId}
        ↓
AlertService.getAlertStreamForUser(userId)
        ↓
Flux<AlertDocument> (Server-Sent Events)
        ↓ HTTP streaming
Frontend (EventSource connection)
```

### 3. **Frontend (Nuevos Componentes)**
```
alertService.js → openAlertStream(userId)
        ↓
EventSource → SSE connection con credentials
        ↓
useAlertsStream.js → Composable reactivo
        ↓
ref<alerts> → Flux de alertas en tiempo real
        ↓
AlertsPanel.vue → Renderiza alertas en UI
```

---

## Componentes Creados

### 1. **`services/alerts-stream.js`**
Servicio que establece y gestiona la conexión SSE con el backend.

**Funciones:**
- `openAlertStream(userId, handlers)` - Abre conexión SSE
  - Conecta a `/api/alerts/stream/{userId}`
  - Envía cookies HttpOnly automáticamente
  - Parsea JSON de eventos
  - Llama callbacks `onAlert()` y `onError()`
  - Muestra alerta global si es `BANDWIDTH_QUOTA_EXCEEDED`

- `closeAlertStream(eventSource)` - Cierra la conexión

**Código Clave:**
```javascript
const eventSource = new EventSource(`${apiBaseUrl}/api/alerts/stream/${userId}`, {
  withCredentials: true, // Cookies HttpOnly
});

eventSource.onmessage = (event) => {
  const alertData = JSON.parse(event.data);
  onAlert(alertData);
};
```

---

### 2. **`components/Alerts/useAlertsStream.js`**
Composable Vue 3 que gestiona el estado reactivo de alertas.

**Características:**
- ✅ Inicia stream en `onMounted()`
- ✅ Cierra conexión en `onUnmounted()`
- ✅ Mantiene array reactivo de alertas
- ✅ Métodos para marcar como leído, limpiar, eliminar
- ✅ Limita a últimas 50 alertas (evita memory leak)

**Uso en componentes:**
```javascript
const { alerts, markAsRead, clearAlerts, removeAlert } = useAlertsStream(userId);
```

---

### 3. **`components/Alerts/AlertsPanel.vue`**
Componente Vue que renderiza panel de alertas en tiempo real.

**Características:**
- 📊 Muestra alertas en orden cronológico inverso (más recientes primero)
- 🎨 Estilos responsivos y con animaciones
- 🔔 Badge con contador de alertas no leídas
- ⏱️ Tiempo relativo (hace 5 min, hace 2h, etc.)
- 🎯 Botones para marcar como leído y descartar
- 🌊 Scroll personalizado
- 📍 Posicionado en esquina inferior derecha (fixed)

**Tipos de alertas soportadas:**
```
- BANDWIDTH_QUOTA_EXCEEDED ⚠️
- CPU_THRESHOLD
- MEMORY_THRESHOLD
```

---

## Integración en App.vue

Se actualizó `App.vue` para:
1. Importar `AlertsPanel`
2. Extraer `userId` del usuario autenticado
3. Renderizar panel solo si usuario está logueado
4. Posicionar como widget flotante con animación de entrada

```vue
<template>
  <div class="alerts-widget" v-if="showSidebar && userId">
    <AlertsPanel :user-id="userId" />
  </div>
</template>
```

---

## Flujo de Datos Completo

### Escenario: Cliente excede umbral de ancho de banda

1. **MongoDB:** Se inserta documento en `bandwidth_usage`
   ```json
   {
     "userId": 123,
     "billingPeriod": "2026-08",
     "totalBytes": 1500000000
   }
   ```

2. **Backend - BandwidthAlertListenerService:**
   - Change Stream detecta INSERT
   - Calcula consumo total del periodo
   - Si ≥ 1GB → crea `AlertDocument`

3. **Backend - AlertService:**
   - `pushAlert(alertDocument)` emite por Sinks.Many
   - Todos los subscribers del usuario lo reciben

4. **Frontend - EventSource:**
   - Recibe evento SSE con AlertDocument JSON
   - Trigger `onAlert()` handler

5. **Frontend - useAlertsStream:**
   - Añade alerta al inicio del array
   - Componente detecta reactivamente el cambio

6. **Frontend - AlertsPanel:**
   - Renderiza nueva alerta
   - Muestra badge con contador
   - `showGlobalAlert()` muestra notificación temporal

7. **Usuario:**
   - Ve alerta en panel de esquina inferior derecha
   - Ve notificación flotante en pantalla
   - Puede marcar como leído o descartar

---

## Consideraciones de Implementación

### Manejo de Errores
- Si EventSource falla, se cierra la conexión
- `onError()` handler permite implementar reconexión automática
- Errors se logean en console

### Performance
- Se limitan a 50 alertas máximo en memoria
- Las alertas más antiguas se descartan automáticamente
- SSE es más eficiente que polling

### Seguridad
- `withCredentials: true` envía cookies HttpOnly
- El backend filtra alertas por `userId` del token
- No se exponen alertas de otros usuarios

### UX
- Panel flotante se anima al aparecer
- Contador de no leídas (rojo) attrae atención
- Timestamps relativos son fáciles de leer
- Scrollbar personalizado mantiene coherencia visual

---

## Archivos Modificados/Creados

```
Frontend/
├── src/
│   ├── App.vue (MODIFICADO)
│   ├── services/
│   │   └── alerts-stream.js (NUEVO) ✨
│   └── components/
│       └── Alerts/
│           ├── useAlertsStream.js (NUEVO) ✨
│           └── AlertsPanel.vue (NUEVO) ✨
```

---

## Testing

### Prueba Manual
1. Loguearse en la aplicación
2. Verificar que `AlertsPanel` aparece en esquina inferior derecha
3. Insertar documento en `bandwidth_usage` en MongoDB:
   ```javascript
   db.bandwidth_usage.insertOne({
     userId: [ID_USUARIO],
     billingPeriod: "2026-08",
     totalBytes: 1100000000
   })
   ```
4. Observar que:
   - Nueva alerta aparece en el panel
   - Badge rojo muestra contador
   - `showGlobalAlert()` aparece en pantalla
   - Timestamp es "Ahora mismo"

### Prueba de Reconexión (Opcional)
Implementar en `useAlertsStream.js`:
```javascript
const reconnect = () => {
  closeAlertStream(eventSource);
  setTimeout(() => startStream(), 3000);
};
```

---

## Próximos Pasos

1. **Endpoint de actualización:** Crear `PUT /api/alerts/{id}/read` para persistir estado
2. **Notificaciones push:** Integrar Web Notifications API
3. **Base de datos:** Guardar alertas en MongoDB para histórico
4. **Reconexión automática:** Implementar exponential backoff
5. **Filtros:** Permitir usuario filtrar por tipo de alerta
6. **Sonido:** Reproducir sonido al recibir alerta crítica

---

## Referencias

- [MongoDB Change Streams](https://docs.mongodb.com/manual/changeStreams/)
- [Server-Sent Events (SSE)](https://developer.mozilla.org/es/docs/Web/API/Server-sent_events)
- [Spring Data MongoDB Messaging](https://docs.spring.io/spring-data/mongodb/docs/current/api/org/springframework/data/mongodb/core/messaging/package-summary.html)
- [Vue 3 Composition API](https://vuejs.org/guide/extras/composition-api-faq.html)
