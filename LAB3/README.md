# Panel de Control de Infraestructura Cloud — Host Usach Cloud

Sistema para desplegar y gestionar instancias de servidores virtuales (contenedores Docker), recursos asociados (CPU, RAM, almacenamiento, IPs), regiones geográficas y datacenters. Incorpora una capa espacial sobre **PostGIS** para razonar sobre ubicación, latencia y riesgo geológico.

---

## Stack técnico

| Componente | Tecnología | Versión |
|------------|-------------|---------|
| Frontend | Vue 3 + Composition API + Vite + TypeScript | 3.4+ |
| Backend | Spring Boot (WebFlux reactivo) | 3.5.13 |
| Lenguaje backend | Java | 21 |
| Build backend | Gradle (wrapper incluido) | — |
| Persistencia | Spring `JdbcTemplate` (SQL crudo, sin JPA) | — |
| Contenedores | Docker Engine (docker-java 3.3.6 contra `/var/run/docker.sock`) | — |
| Base de datos | PostgreSQL + PostGIS | 16 / 3.5 |
| Herramientas geoespaciales | GDAL `ogr2ogr` (instaladas en la imagen de BD) | — |
| Autenticación | Spring Security + JWT propio HS256 (jjwt) + cookies HttpOnly | 0.12.5 |
| Acceso a BD imagen | `postgis/postgis:16-3.5` con GDAL | — |

---

## Geolocalización y PostGIS

Capa espacial construida sobre PostGIS. La base de datos extiende `postgis/postgis:16-3.5` con GDAL para poder importar capas geográficas externas.

### Tablas espaciales

| Tabla | Geometría | SRID | Índice |
|-------|-----------|------|--------|
| `Region` | `geometry(Polygon, 4326)` | WGS84 | GIST sobre `Geom` |
| `Datacenter` | `geometry(Point, 4326)` | WGS84 | — |
| `RiskZone` | `geometry(MultiPolygon, 4326)` (placas tectónicas) | WGS84 | — |

`RiskZone` se importa desde `BD/data/PB2002_plates.json` mediante `ogr2ogr` (script `BD/init/09_import_geodata.sh`), ejecutado durante el primer arranque del contenedor.

### Función espacial principal

`fn_latencia_a_regiones(p_lat, p_lng)` → tabla con `region_id`, `region_name`, `distance_m` y `latency_rtt_ms` por región.

- Distancia calculada con `ST_DistanceSpheroid` (WGS84, metros).
- RTT = `distance / 100000` (c/1.5 ≈ 200 km/ms; ida y vuelta).
- Si el usuario está dentro de un polígono de región, `distance_m = 0`.
- Usa el operador `<->` de PostGIS para que el `ORDER BY` aproveche el índice GIST (`region_geom_idx`) y haga Index Scan en vez de Seq Scan.

### Soberanía de datos (trigger)

`trg_check_datacenter_distance` rechaza `INSERT` en `Instance` si la distancia entre el `Datacenter` elegido y el centroide de la `Region` supera **4300 km**. La distancia se calcula con `ST_Distance` sobre el tipo `geography` (metros) y se compara en km.

### Endpoints espaciales

| Endpoint | Uso |
|----------|-----|
| `POST /api/datacenters/location-info` | Dada `(lat, lng)`, devuelve la región más cercana, RTT estimado, datacenter recomendado y si el punto cae en zona de riesgo. |
| `GET /api/datacenters/recommendations/{instanceId}` | Recomienda datacenters para una instancia, considerando región, riesgo y capacidad. |
| `GET /api/regions/ping` | Ping a las regiones (latencia sintética para el panel). |
| `GET /api/risks` | Devuelve el catálogo de zonas de riesgo (placas tectónicas). |
| `POST /api/billing/instances/{instanceId}/calculate-distance` | Calcula distancia y costo de transferencia entre dos puntos para una instancia. |

---

## Componentes avanzados de base de datos

- **Vista materializada**: `vista_recursos_globales` (totales de RAM/CPU/storage por región, geometría en GeoJSON, centroides en lng/lat). Refrescada cada 2 min por un `@Scheduled(fixedRate = 120000)` en `MaterializedViewService`, y refresco forzado al hacer `GET /api/admin/reports/global-resources`. Función SQL: `refrescar_vista_recursos()` (`REFRESH MATERIALIZED VIEW CONCURRENTLY`).
- **Índices**: GIST sobre `Region.Geom`, BTREE compuesto sobre `Instance(Ip_address, State)`.
- **Triggers**:
  - `trg_check_quota` — bloquea `INSERT` si el usuario supera `Users.Max_instances` activas.
  - `trg_release_ip` — al pasar `Terminated = TRUE`, libera la IP (`Ip.Assigned = FALSE`, `Ip_address = NULL`).
  - `trg_calculate_active_hours` — acumula `NOW() - Started_at` en `Active_hours` al pasar a Stopped/Terminated, y resetea `Started_at` al volver a Running.
  - `trg_check_datacenter_distance` — soberanía de datos (ver sección PostGIS).
- **Procedimientos almacenados**:
  - `provision_instance(p_ip_address, p_instance_id)` — marca la IP como ocupada y crea un `Ticket` inicial.
  - `generate_monthly_tickets(p_user_id)` — emite tickets mensuales por instancia y resetea contadores.
- **Función espacial**: `fn_latencia_a_regiones(p_lat, p_lng)`.

---

## Instalación y Despliegue

### Requisitos previos

- Docker Engine 24.0+
- Docker Compose 2.20+
- Puerto 5432 disponible (PostgreSQL/PostGIS)
- Puerto 8080 disponible (Backend)
- Puerto 5173 disponible (Frontend)

### Paso 1: Clonar el repositorio

```bash
git clone https://github.com/Benjahern/DBA-Grupo5.git
cd DBA-Grupo5/LAB3
```

### Paso 2: Configurar variables de entorno

```bash
cp .env.example .env
```

Revisa al menos `POSTGRES_PASSWORD`, `JWT_SECRET` (mínimo 32 bytes), `ADMIN_INITIAL_PASSWORD` y `CORS_ALLOWED_ORIGINS`.

### Paso 3: Iniciar todos los servicios

```bash
docker compose up -d --build
```

Este comando:
- Construye la imagen del backend (Spring Boot, build con Gradle).
- Construye la imagen de base de datos desde `db-image/Dockerfile` (`postgis/postgis:16-3.5` + GDAL).
- Crea los contenedores `bda_db`, `bda_backend`, `bda_frontend`.
- Inicializa la BD ejecutando los scripts de `BD/init/` (esquema, índices, triggers, procedimientos, función espacial, vista materializada, import de placas tectónicas vía GDAL).
- Siembra el admin inicial (`admin@gmail.com`) vía `PasswordSeeder` al boot del backend.

### Paso 4: Verificar servicios

```bash
# Estado de contenedores — esperar ~2 minutos la primera vez
docker compose ps

# Logs del backend
docker compose logs -f backend
```

### URLs de acceso

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |

### Credenciales por defecto

**Admin (sembrado por `PasswordSeeder`):**
- Email: `admin@gmail.com`
- Contraseña: `Admin123!` (definida en `ADMIN_INITIAL_PASSWORD`)

**Base de datos:**
- Usuario: `bda_user`
- Contraseña: (definida en `POSTGRES_PASSWORD`)
- Base de datos: `bda_lab1`

### Detener servicios

```bash
docker compose down          # Detener sin eliminar datos
docker compose down -v       # Detener y eliminar volúmenes (limpia BD)
```

---

## Documentación de la API

> Todos los endpoints excepto `/api/auth/**` requieren un token JWT. La auth se hace vía cookies HttpOnly (`access_token`, `refresh_token`) emitidas en `POST /api/auth/login`. En flujos sin cookies, enviar `Authorization: Bearer <access_token>`.

### Autenticación

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@gmail.com",
  "password": "Admin123!"
}
```

**Respuesta exitosa:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 900
}
```

#### Registrar usuario

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "usuario@ejemplo.com",
  "name": "Juan Pérez",
  "password": "contraseña123"
}
```

#### Refrescar token

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Logout

```http
POST /api/auth/logout
```

---

### Instancias

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/instances` | Listar instancias (filtros opcionales: `userId`, `state`). |
| `GET` | `/api/instances/{id}` | Obtener instancia por ID. |
| `POST` | `/api/instances` | Crear instancia (pasa por el trigger de soberanía de datos). |
| `PUT` | `/api/instances/{id}` | Actualizar nombre/estado. |
| `PUT` | `/api/instances/{id}/state` | Cambiar estado (`Running`, `Stopped`, `Terminated`). |
| `DELETE` | `/api/instances/{id}` | Eliminar instancia (libera la IP vía trigger). |
| `GET` | `/api/instances/{id}/stats` | Stream NDJSON de métricas (CPU, RAM, red). |

**Crear instancia:**
```http
POST /api/instances
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Servidor-Web-01",
  "userId": 1,
  "regionId": 1,
  "datacenterId": 1,
  "cpuId": 1,
  "ramId": 2,
  "storageId": 3,
  "color": "#3B82F6",
  "baseImage": "nginx:latest"
}
```

**Métricas (stream NDJSON):**
```
{"timestamp":"2026-07-22T10:30:00Z","cpuPercent":45.2,"memoryPercent":62.1,"networkRx":1024,"networkTx":512}
{"timestamp":"2026-07-22T10:30:05Z","cpuPercent":48.7,"memoryPercent":63.5,"networkRx":1536,"networkTx":768}
```

---

### Regiones y datacenters

#### Regiones

| Método | Path |
|--------|------|
| `GET` | `/api/regions` |
| `GET` | `/api/regions/{id}` |
| `POST` | `/api/regions` |
| `PUT` | `/api/regions/{id}` |
| `DELETE` | `/api/regions/{id}` |
| `GET` | `/api/regions/ping` |

Las regiones se almacenan con `geometry(Polygon, 4326)` y se exponen a través de la vista materializada `vista_recursos_globales` (con `ST_AsGeoJSON` y centroides en lng/lat).

#### Datacenters

| Método | Path |
|--------|------|
| `GET` | `/api/datacenters` |
| `GET` | `/api/datacenters/{id}` |
| `POST` | `/api/datacenters` |
| `PUT` | `/api/datacenters/{id}` |
| `DELETE` | `/api/datacenters/{id}` |
| `POST` | `/api/datacenters/location-info` | Recibe `{latitude, longitude}` y devuelve región, RTT, datacenter recomendado y riesgo. |
| `GET` | `/api/datacenters/recommendations/{instanceId}` | Recomienda datacenters para una instancia. |

#### Zonas de riesgo

| Método | Path |
|--------|------|
| `GET` | `/api/risks` | Catálogo de zonas de riesgo importadas desde `PB2002_plates.json`. |

---

### Recursos (CPU, RAM, Almacenamiento)

Todos los recursos exponen CRUD completo:

```http
GET    /api/cpus
GET    /api/cpus/{id}
POST   /api/cpus
PUT    /api/cpus/{id}
DELETE /api/cpus/{id}
```

(Equivalente para `/api/rams` y `/api/storages`.)

---

### Facturación y consumo

| Método | Path | Descripción |
|--------|------|-------------|
| `POST` | `/api/billing/users/{userId}/monthly-tickets` | Genera tickets mensuales (procedure `generate_monthly_tickets`). |
| `POST` | `/api/billing/instances/{instanceId}/calculate-distance` | Calcula distancia y costo de transferencia entre dos puntos. |
| `GET` | `/api/consumption/users/{userId}/monthly-projection` | Proyección mensual de consumo (horas activas, instancias, costo). |

---

### Usuarios

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/users/me` | Devuelve el usuario autenticado. |

---

### Reportes (vista materializada)

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/admin/reports/global-resources` | Refresca `vista_recursos_globales` y devuelve el agregado por región. |

---

### Códigos de error

| Código | Descripción |
|--------|-------------|
| 400 | Solicitud inválida |
| 401 | No autorizado (token inválido o expirado) |
| 403 | Prohibido (sin permisos suficientes) |
| 404 | Recurso no encontrado |
| 409 | Conflicto (cuota excedida, IP no disponible, soberanía de datos) |
| 500 | Error interno del servidor |

---

## Roles y Permisos

| Rol | Permisos |
|-----|----------|
| `user` | Crear/gestionar sus propias instancias, ver su facturación y consumo. |
| `admin` | Todas las operaciones, gestión de usuarios, ver toda la infraestructura, ver reportes globales. |

---

## Desarrollo local (sin Docker)

### Backend

```bash
cd Backend
./gradlew build
./gradlew bootRun
```

El backend requiere una instancia de Postgres+PostGIS accesible. Los scripts de `BD/init/` están escritos para ejecutarse sobre la imagen `postgis/postgis:16-3.5`; en local puedes correrlos contra cualquier Postgres 16 con la extensión `postgis` instalada (`CREATE EXTENSION postgis;` antes de `01_createDb.sql`) y GDAL disponible si quieres ejecutar `09_import_geodata.sh`.

### Frontend

```bash
cd Frontend
npm install
npm run dev
```

### Variables de entorno del backend (modo local)

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bda_lab1
SPRING_DATASOURCE_USERNAME=bda_user
SPRING_DATASOURCE_PASSWORD=tu_password
JWT_SECRET=tu-secreto-de-al-menos-32-bytes
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=2592000000
ADMIN_EMAIL=admin@gmail.com
ADMIN_INITIAL_PASSWORD=cambia-esto-en-prod
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

---

## Estructura del proyecto

```
LAB2/
├── Frontend/                          # Vue 3 + Vite + TypeScript
│   ├── src/
│   │   ├── views/                     # Páginas principales
│   │   ├── components/                # Componentes reutilizables
│   │   ├── stores/                    # Pinia stores
│   │   └── router/                    # Configuración de rutas
│   ├── package.json
│   └── dockerfile
├── Backend/                           # Spring Boot 3.5 + Java 21
│   ├── dockerfile
│   ├── build.gradle
│   └── src/main/
│       ├── java/Host_Usach_Cloud/Backend/
│       │   ├── Controllers/           # REST: /api/{auth,users,instances,...}
│       │   ├── Services/              # Lógica de negocio + integración Docker
│       │   ├── Repository/            # JdbcTemplate, SQL crudo
│       │   ├── Entity/                # POJOs Lombok (1:1 con esquema Postgres)
│       │   ├── Security/              # JWT propio, PasswordSeeder, cookies
│       │   ├── Config/                # SecurityConfig, DockerConfig
│       │   └── Controllers/DTO, Services/DTO
│       └── resources/
│           └── application.yaml
├── db-image/                          # Dockerfile de la BD
│   └── Dockerfile                     # FROM postgis/postgis:16-3.5 + GDAL
├── BD/
│   ├── init/                          # Scripts de inicialización (ordenados)
│   │   ├── 01_createDb.sql            # Extensión PostGIS + esquema + seeds
│   │   ├── 02_spatial_functions.sql   # fn_latencia_a_regiones (PostGIS)
│   │   ├── 03_index.sql               # Índices BTREE
│   │   ├── 04_materialized_view.sql   # vista_recursos_globales + refresco
│   │   ├── 05_procedure.sql           # provision_instance
│   │   ├── 06_procedures2.sql         # generate_monthly_tickets
│   │   ├── 07_triggers.sql            # cuota, release IP, active hours, soberanía
│   │   ├── 08_migration_password.sql  # ALTER idempotente (Password_hash)
│   │   └── 09_import_geodata.sh       # ogr2ogr PB2002_plates.json → RiskZone
│   └── data/
│       └── PB2002_plates.json         # Placas tectónicas (Bird, 2002)
├── Presentación/
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Notas operativas

- **Volúmenes**: `pg_data` (Postgres) y `gradle_cache` (Gradle del backend). Un `docker compose down -v` los borra y vuelve a aplicar los scripts de `BD/init/`.
- **Volumen de desarrollo**: `docker-compose.yml` monta `./Backend` y `./Frontend` como volúmenes. Cualquier cambio en `src/` o en el código del frontend se refleja sin rebuild (el backend corre con `--continuous`).
- **Primer arranque**: la inicialización de la BD (incluido el import de `PB2002_plates.json` con GDAL) puede tardar ~1–2 minutos la primera vez. El backend espera vía `healthcheck` a que la BD esté lista.
- **Persistencia de la contraseña del admin**: el `PasswordSeeder` setea `Users.Password_hash` en el primer boot del backend y es idempotente. Cambiar `ADMIN_INITIAL_PASSWORD` después del primer arranque no reescribe el hash.
