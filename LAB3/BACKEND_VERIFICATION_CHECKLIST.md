# Verificación Backend - Change Streams

## Checklist de Configuración Requerida

Antes de que el frontend funcione correctamente, verifica lo siguiente en el backend:

### ✅ 1. MongoDB Change Streams Habilitado

**Verificar:**
```bash
# En MongoDB, confirmar que es un Replica Set o Sharded Cluster
mongo
> rs.status()  # Debe mostrar configuración de replica set
```

Change Streams requiere MongoDB 3.6+ en Replica Set.

### ✅ 2. MessageListenerContainer Configurado

**Archivo:** `Backend/src/main/java/Host_Usach_Cloud/Backend/Mongo/Config/AlertConfig.java`

Verificar que existe y tiene:
```java
@Bean
public MessageListenerContainer messageListenerContainer(MongoTemplate mongoTemplate) {
    return new DefaultMessageListenerContainer(mongoTemplate);
}
```

### ✅ 3. BandwidthAlertListenerService Registrado

**Archivo:** `Backend/src/main/java/Host_Usach_Cloud/Backend/Mongo/Services/BandwidthAlertListenerService.java`

Verificar:
- ✅ Tiene anotación `@Service`
- ✅ `@PostConstruct public void init()` registra el Change Stream
- ✅ Filtra por operationType = "insert"
- ✅ Llama a `alertService.pushAlert()` cuando se supera umbral

### ✅ 4. AlertService con Sinks

**Archivo:** `Backend/src/main/java/Host_Usach_Cloud/Backend/Mongo/Services/AlertService.java`

Verificar:
```java
private final Sinks.Many<AlertDocument> alertSink = 
    Sinks.many().multicast().onBackpressureBuffer();

public void pushAlert(AlertDocument alert) {
    alertSink.tryEmitNext(alert);
}

public Flux<AlertDocument> getAlertStreamForUser(Long userId) {
    List<AlertDocument> unread = alertRepository.findByUserIdAndRead(userId, false);
    Flux<AlertDocument> historical = Flux.fromIterable(unread);
    Flux<AlertDocument> live = alertSink.asFlux()
            .filter(alert -> userId.equals(alert.getUserId()));
    return Flux.concat(historical, live);
}
```

### ✅ 5. AlertController con SSE Endpoint

**Archivo:** `Backend/src/main/java/Host_Usach_Cloud/Backend/Controllers/AlertController.java`

Verificar:
```java
@GetMapping(value = "/stream/{userId}", 
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<AlertDocument> streamUserAlerts(@PathVariable Long userId) {
    return alertService.getAlertStreamForUser(userId);
}
```

- Produce: `text/event-stream`
- Retorna `Flux<AlertDocument>`
- Acepta `userId` como path variable

### ✅ 6. AlertDocument Entity

**Archivo:** `Backend/src/main/java/Host_Usach_Cloud/Backend/Mongo/Entity/AlertDocument.java`

Debe tener:
```java
@Id
private ObjectId id;
private Long userId;
private String alertType;
private String message;
private LocalDateTime timestamp;
private boolean read;
```

### ✅ 7. AlertMongoRepository

**Archivo:** `Backend/src/main/java/Host_Usach_Cloud/Backend/Mongo/Repository/AlertMongoRepository.java`

Debe tener:
```java
List<AlertDocument> findByUserIdAndRead(Long userId, boolean read);
```

### ✅ 8. CORS Headers (Importante)

Para que SSE funcione con cookies HttpOnly, verificar SecurityConfig:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .cors().configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOriginPatterns(Arrays.asList("*"));
            config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(Arrays.asList("*"));
            config.setAllowCredentials(true);  // ← CRÍTICO para cookies
            config.setMaxAge(3600L);
            return config;
        })
        // ... resto de config
}
```

### ✅ 9. Logs de Verificación

Al iniciar la aplicación, deberías ver en logs:
```
Change Stream Listener para ancho de banda registrado exitosamente.
```

Si no ves este mensaje:
- Verificar que `BandwidthAlertListenerService` se instancia como `@Service`
- Verificar que no hay excepciones en logs
- Verificar que MongoDB está corriendo como Replica Set

---

## Test Manual

### 1. Verificar que el endpoint SSE está disponible

```bash
curl -v http://localhost:8080/api/alerts/stream/123 \
  -H "Cookie: access_token=YOUR_TOKEN"
```

Debe responder con:
```
Content-Type: text/event-stream
Cache-Control: no-cache
```

### 2. Insertar documento de prueba en MongoDB

```javascript
use your_db;
db.bandwidth_usage.insertOne({
  userId: 123,
  billingPeriod: "2026-08",
  totalBytes: 1100000000,  // 1.1 GB (supera umbral de 1 GB)
  timestamp: new Date()
});
```

### 3. Verificar que se creó AlertDocument

```javascript
db.alerts.find({ userId: 123 }).pretty();
```

Debería haber un documento con:
```json
{
  "userId": 123,
  "alertType": "BANDWIDTH_QUOTA_EXCEEDED",
  "message": "Has superado el umbral de consumo de ancho de banda mensual.",
  "timestamp": ISODate("2026-08-08T..."),
  "read": false
}
```

### 4. Verificar que SSE recibe la alerta

El navegador (en el frontend) debería recibir el evento y mostrarlo en AlertsPanel.

---

## Posibles Problemas

| Problema | Solución |
|----------|----------|
| Change Stream no se registra | Verificar que MongoDB es Replica Set, no standalone |
| No se crean AlertDocuments | Verificar que el umbral BANDWIDTH_THRESHOLD es correcto |
| SSE no envía datos | Verificar CORS con `allowCredentials: true` |
| Frontend no recibe alertas | Verificar userId es correcto en `getUser()` |
| "withCredentials" error en console | Verificar backend permite credentials en CORS |

---

## Variables de Configuración

En el backend, puedes ajustar el umbral:

```java
// BandwidthAlertListenerService.java
private static final long BANDWIDTH_THRESHOLD = (long) 1024 * 1024 * 1024;  // 1 GB
```

Cambia este valor según tus requerimientos.

---

## Referencias

- [Spring WebFlux SSE](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/SseEmitter.html)
- [MongoDB Change Streams](https://docs.mongodb.com/manual/changeStreams/)
- [Spring Data MongoDB Messaging](https://spring.io/blog/2020/08/11/reactive-streams-with-spring-data-mongodb)
