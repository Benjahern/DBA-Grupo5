# Quick Start: Verificar Change Streams Funcionando

## ⚡ Prueba en 5 Minutos

### Paso 1: Iniciar la Aplicación

```bash
cd Frontend
npm install  # o pnpm install
npm run dev  # o pnpm dev
```

Accede a `http://localhost:5173`

### Paso 2: Loguearse

- Usuario autenticado debe verse en la aplicación
- Sidebar debe aparecer con el nombre del usuario

### Paso 3: Verificar que AlertsPanel Existe

**En la esquina inferior derecha de la pantalla debe aparecer:**

```
┌─────────────────────────┐
│ Alertas en Tiempo Real  │ [✕]
├─────────────────────────┤
│                         │
│  No hay alertas por el  │
│  momento                │
│                         │
└─────────────────────────┘
```

Si no ves el panel:
- ✅ Verifica que estés logueado
- ✅ Abre la consola del navegador (F12) y busca errores
- ✅ Verifica que `App.vue` incluye `<AlertsPanel>`

### Paso 4: Abrir DevTools de Navegador

```
F12 → Network → Filtrar por "alerts"
```

Deberías ver:
```
GET /api/alerts/stream/[userId]
Status: 101 (Switching Protocols) o 200
Type: fetch (EventSource)
```

Si ves error 401/403: El backend rechaza la cookie de autenticación.

### Paso 5: Insertar Documento de Prueba en MongoDB

En tu terminal, conectate a MongoDB:

```bash
mongosh
```

Luego ejecuta:

```javascript
// Cambia "123" por el userId real del usuario logueado
use your_database;
db.bandwidth_usage.insertOne({
  userId: 123,
  billingPeriod: "2026-08",
  totalBytes: 1100000000,  // 1.1 GB (excede 1 GB)
  timestamp: new Date()
});
```

### Paso 6: Observar Cambios

Dentro de **1-2 segundos**, deberías ver en el frontend:

```
1. ⚠️ Panel se actualiza con nueva alerta
2. 📊 Badge rojo aparece con contador "1 nueva"
3. 🔔 Notificación flotante en pantalla
4. 📝 Mensaje: "Has superado el umbral de consumo de ancho de banda mensual."
```

---

## 🔍 Troubleshooting

### Panel no aparece después de loguearse

**Síntoma:** AlertsPanel no se ve en esquina inferior derecha

**Soluciones:**
```bash
# Opción 1: Limpiar cache y recargar
Ctrl+Shift+R  # Hard refresh

# Opción 2: Verificar que App.vue tiene AlertsPanel
# En App.vue debe estar:
# <AlertsPanel v-if="showSidebar && userId" :user-id="userId" />

# Opción 3: Verificar console del navegador
F12 → Console → Buscar errores
```

### SSE muestra error 401/403

**Síntoma:** En Network → alerts/stream muestra 401

**Causas:**
- Backend no está validando la cookie correctamente
- CORS no está configurado con `allowCredentials: true`

**Solución:**
```java
// Backend SecurityConfig.java
config.setAllowCredentials(true);
config.setAllowedOriginPatterns(Arrays.asList("*"));
```

### Alerta no aparece después de insertar en MongoDB

**Síntoma:** Documento se inserta pero no hay alerta en frontend

**Posibles causas:**

1. **Change Stream no está registrado**
   ```bash
   # Verifica logs del backend
   # Busca: "Change Stream Listener para ancho de banda registrado exitosamente"
   ```

2. **MongoDB no es Replica Set**
   ```bash
   mongosh
   > rs.status()  # Si falla → no es replica set
   
   # Inicializar replica set (desarrollo):
   > rs.initiate()
   ```

3. **userId no coincide**
   ```javascript
   // En Frontend, verifica:
   const user = getUser();
   console.log("userId:", user.sub || user.id);
   
   // Debe ser el mismo que usaste en MongoDB:
   db.bandwidth_usage.find({ userId: 123 })
   ```

4. **Consumo no supera umbral**
   ```javascript
   // Verifica que totalBytes > 1073741824 (1 GB)
   1100000000 > 1073741824  // true ✅
   ```

### Eventos SSE se reciben pero no se muestran

**Síntoma:** En Network veo eventos llegando pero no se renderizan

**Soluciones:**
```javascript
// En useAlertsStream.js, verifica onAlert:
onAlert: (alertData) => {
  console.log('Alerta recibida:', alertData);  // Agregá esto
  alerts.value.unshift(alertData);
}

// Si no ves logs → EventSource no está siendo invocado
```

---

## 📊 Debug Mode

### Ver qué datos recibe el frontend

En `useAlertsStream.js`, descomentar logs:

```javascript
onAlert: (alertData) => {
  console.log('Nueva alerta recibida:', alertData);  // ← Add this
  console.log('Total alertas:', alerts.value.length);
  
  alerts.value.unshift(alertData);
}
```

### Ver estado del EventSource

En la consola del navegador:

```javascript
// Copiar y pegar en console
const alerts = document.querySelector('.alerts-panel');
console.log('Panel existe:', !!alerts);
console.log('Panel visible:', alerts?.offsetParent !== null);
```

### Verificar que userId se pasa correctamente

En `App.vue`, en la template:

```vue
<AlertsPanel :user-id="userId" />
<!-- Presionar F12 → Vue DevTools → Ver props de AlertsPanel -->
```

---

## ✅ Checklist de Verificación

- [ ] AlertsPanel aparece en esquina inferior derecha
- [ ] Al loguearse, el contador dice "No hay alertas"
- [ ] En Network veo `/api/alerts/stream/[userId]` con status 200
- [ ] En DevTools Network → fetch → 200 OK (no 401/403)
- [ ] Inserto documento en `bandwidth_usage`
- [ ] Después de 1-2s, alerta aparece en el panel
- [ ] Contador muestra "1 nueva"
- [ ] Notificación flotante aparece en pantalla
- [ ] Puedo hacer click en "✓" para marcar como leído
- [ ] Puedo hacer click en "✕" para descartar

---

## 📱 Comandos Útiles

### Ver userId del usuario logueado
```javascript
// En console del navegador
const user = JSON.parse(localStorage.getItem('user') || '{}');
console.log('userId:', user.sub || user.id);
```

### Insertar múltiples alertas de prueba
```javascript
db.bandwidth_usage.insertMany([
  { userId: 123, billingPeriod: "2026-08", totalBytes: 1100000000, timestamp: new Date() },
  { userId: 123, billingPeriod: "2026-08", totalBytes: 1200000000, timestamp: new Date() },
  { userId: 123, billingPeriod: "2026-08", totalBytes: 1300000000, timestamp: new Date() }
]);
```

### Limpiar alertas (opcional)
```javascript
db.alerts.deleteMany({ userId: 123 });
```

### Ver la estructura de AlertDocument creado
```javascript
db.alerts.findOne({ userId: 123 });
```

---

## 🎉 Si Todo Funciona

Deberías ver:
1. ✅ Panel de alertas en esquina inferior derecha
2. ✅ Alertas nuevas aparecen **instantáneamente**
3. ✅ Contador de no leídas en badge rojo
4. ✅ Notificación global flotante
5. ✅ Timestamps relativos ("hace 2 min")
6. ✅ Botones de acción (marcar leído, descartar)

---

## 🔗 Documentación Completa

- `RESPUESTA_CHANGE_STREAMS.md` - Respuesta detallada
- `CHANGE_STREAMS_IMPLEMENTATION.md` - Arquitectura completa
- `BACKEND_VERIFICATION_CHECKLIST.md` - Checklist backend
- `IMPLEMENTATION_SUMMARY.md` - Resumen ejecutivo
