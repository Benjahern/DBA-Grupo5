# Panel de Control de Infraestructura Cloud

Sistema para desplegar y gestionar instancias de servidores virtuales, bases de datos y consumo de ancho de banda.

## Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend                                │
│                    Vue 3 + Vite + Pinia                         │
│                       Puerto 5173                               │
└─────────────────────────┬───────────────────────────────────────┘
                          │ HTTP + JWT
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Backend                                 │
│              Spring Boot 3.5 (Java 21)                          │
│                  Puerto 8080                                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │  Instance   │  │  Billing    │  │ Monitoring  │              │
│  │  Service    │  │  Service    │  │  Service    │              │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
│         │                │                │                     │
│         └────────────────┼────────────────┘                     │
│                          │                                      │
│                    Spring JDBC                                  │
└─────────────────────────┬───────────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
    ┌──────────┐   ┌──────────┐   ┌──────────┐
    │PostgreSQL│   │ Keycloak │   │  Docker  │
    │   5432   │   │   8081   │   │    API   │
    └──────────┘   └──────────┘   └──────────┘
```

### Tecnologías

| Componente | Tecnología | Versión |
|------------|-------------|---------|
| Frontend | Vue 3 + Composition API | 3.4+ |
| Estado | Pinia | 2.1+ |
| Build | Vite | 5.0+ |
| Backend | Spring Boot | 3.5 |
| Lenguaje | Java | 21 |
| Seguridad | Spring Security OAuth2 | 6.3+ |
| Base de datos | PostgreSQL | 16 |
| Auth | Keycloak | 24 |
| Contenedores | Docker API | Latest |

### Base de datos - Componentes avanzados

- **Vista materializada**: `Global_Resource_Usage` (RAM + CPU + almacenamiento por región)
- **Índices**: Estado de instancia, dirección IP
- **Triggers**: Control de cuota, liberación de IP, tracking de horas activas
- **Procedimientos almacenados**: Aprovisionamiento de instancias, facturación mensual

---

## Instalación y Despliegue

### Requisitos previos

- Docker Engine 24.0+
- Docker Compose 2.20+
- Puerto 5432 disponible (PostgreSQL)
- Puerto 8080 disponible (Backend)
- Puerto 8081 disponible (Keycloak)
- Puerto 5173 disponible (Frontend)

### Paso 1: Clonar el repositorio

```bash
git clone https://github.com/Benjahern/DBA-Grupo5.git
cd DBA-Grupo5/LAB1
```

### Paso 2: Configurar variables de entorno

Copiar el archivo de ejemplo y ajustar según sea necesario:

```bash
cp .env.example .env
```

### Paso 3: Iniciar todos los servicios

```bash
docker-compose up --build
```

Este comando:
- Construye la imagen del backend
- Crea los contenedores de PostgreSQL, Keycloak y backend
- Inicializa la base de datos con tablas, índices, triggers y procedimientos
- Importa el realm de Keycloak

### Paso 4: Verificar servicios

```bash
# Estado de contenedores, Por favor esperar al rededor de 2 minutos para probar cualquier cosa
docker-compose ps

# Logs del backend
docker-compose logs -f backend

# Logs de Keycloak
docker-compose logs -f keycloak
```

### URLs de acceso

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Keycloak Admin | http://localhost:8081 |

### Credenciales por defecto

**Keycloak:**
- Usuario: `admin`
- Contraseña: (definida en `KEYCLOAK_ADMIN_PASSWORD`)

**Base de datos:**
- Usuario: `bda_user`
- Contraseña: (definida en `POSTGRES_PASSWORD`)
- Base de datos: `bda_lab1`

### Detener servicios

```bash
docker-compose down          # Detener sin eliminar datos
docker-compose down -v       # Detener y eliminar volúmenes (limpia BD)
```

---

## Documentación de la API

### Autenticación

Todos los endpoints excepto `/api/auth/**` requieren un token JWT en el header:

```
Authorization: Bearer <token_jwt>
```

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "usuario@ejemplo.com",
  "password": "contraseña123"
}

si desea ingresar como admin
email = admin@gmail.com
pass = Admin123!
```

**Respuesta exitosa:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 300
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

**Respuesta:**
```json
{
  "id": 1,
  "email": "usuario@ejemplo.com",
  "name": "Juan Pérez"
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

---

### Instancias

#### Listar instancias

```http
GET /api/instances
GET /api/instances?userId={userId}
GET /api/instances?state={state}
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "name": "Servidor-Web-01",
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "regionId": 1,
    "state": "Running",
    "publicIp": "192.168.1.100",
    "cpus": 4,
    "ramId": 2,
    "storageId": 3,
    "createdAt": "2026-05-01T10:30:00Z"
  }
]
```

#### Crear instancia

```http
POST /api/instances
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Servidor-Web-01",
  "userId": 123,
  "regionId": 1,
  "cpuId": 1,
  "ramId": 2,
  "storageId": 3,
  "color": "#3B82F6",
  "baseImage": "nginx:latest"
}
```

**Respuesta:**
```json
{
  "instance_id": 1,
  "name": "Servidor-Web-01",
  "userId": 123,
  "regionId": 1,
  "state": "Pending",
  "publicIp": "192.168.1.100",
  "container_id": "abc123def456"
}
```

#### Cambiar estado de instancia

```http
PUT /api/instances/{id}/state
Authorization: Bearer <token>
Content-Type: application/json

{
  "state": "Running"
}
```

Estados válidos: `Terminated`, `Running`, `Stopped`

**Respuesta:**
```json
{
  "id": 1,
  "state": "Running",
  "message": "Estado actualizado correctamente"
}
```

#### Eliminar instancia

```http
DELETE /api/instances/{id}
Authorization: Bearer <token>
```

**Respuesta:** `204 No Content`

La IP asociada se libera automáticamente mediante trigger.

#### Actualizar instancia

```http
PUT /api/instances/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Servidor-Web-01-Actualizado",
  "state": "Running"
}
```

#### Obtener instancia por ID

```http
GET /api/instances/{id}
Authorization: Bearer <token>
```

#### Métricas de instancia (streaming NDJSON)

```http
GET /api/instances/{id}/stats
Authorization: Bearer <token>
```

**Respuesta (stream NDJSON):**
```json
{"timestamp":"2026-05-03T10:30:00Z","cpuPercent":45.2,"memoryPercent":62.1,"networkRx":1024,"networkTx":512}
{"timestamp":"2026-05-03T10:30:05Z","cpuPercent":48.7,"memoryPercent":63.5,"networkRx":1536,"networkTx":768}
```

---

### Regiones

#### Listar regiones

```http
GET /api/regions
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "name": "us-east-1",
    "location": "Virginia, USA",
    "availableCpus": 100,
    "availableRam": 512000,
    "availableStorage": 10000000
  }
]
```

#### Obtener región

```http
GET /api/regions/{id}
```

#### Crear región

```http
POST /api/regions
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "us-east-1",
  "location": "Virginia, USA",
  "availableCpus": 100,
  "availableRam": 512000,
  "availableStorage": 10000000
}
```

#### Actualizar región

```http
PUT /api/regions/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "us-east-1",
  "location": "Virginia, USA",
  "availableCpus": 150,
  "availableRam": 768000,
  "availableStorage": 15000000
}
```

#### Eliminar región

```http
DELETE /api/regions/{id}
Authorization: Bearer <token>
```

**Respuesta:** `204 No Content`

---

### Recursos (CPU, RAM, Almacenamiento)

Todos los recursos soportan CRUD completo. Ejemplos de endpoints:

#### CPUs

```http
GET /api/cpus                      # Listar todos
GET /api/cpus/{id}                 # Obtener por ID
POST /api/cpus                     # Crear CPU
PUT /api/cpus/{id}                 # Actualizar CPU
DELETE /api/cpus/{id}              # Eliminar CPU
```

#### RAM

```http
GET /api/rams                      # Listar todos
GET /api/rams/{id}                 # Obtener por ID
POST /api/rams                     # Crear RAM
PUT /api/rams/{id}                 # Actualizar RAM
DELETE /api/rams/{id}              # Eliminar RAM
```

#### Storage

```http
GET /api/storages                  # Listar todos
GET /api/storages/{id}             # Obtener por ID
POST /api/storages                 # Crear Storage
PUT /api/storages/{id}             # Actualizar Storage
DELETE /api/storages/{id}          # Eliminar Storage
```

**Ejemplo de respuesta para listar CPUs:**
```json
[
  {
    "cpu_id": 1,
    "name": "CPU 4 Núcleos",
    "cores": 4,
    "speed": "3.5 GHz",
    "pricePerHour": 0.05
  }
]
```

---

### Facturación

#### Generar tickets mensuales

```http
POST /api/billing/users/{userId}/monthly-tickets
Authorization: Bearer <token>
```

**Respuesta:**
```json
{
  "userId": 123,
  "ticketsGenerated": 5,
  "totalAmount": 156.50,
  "message": "Tickets mensuales generados exitosamente"
}
```

### Consumo

#### Proyección mensual de consumo

```http
GET /api/consumption/users/{userId}/monthly-projection
Authorization: Bearer <token>
```

**Respuesta:**
```json
{
  "userId": 123,
  "projectedMonth": "2026-05",
  "totalInstances": 3,
  "totalHours": 720,
  "projectedCost": 156.50,
  "currency": "USD"
}
```

---

### Usuarios

#### Obtener usuario actual

```http
GET /api/users/me
Authorization: Bearer <token>
```

**Respuesta:**
```json
{
  "id": 1,
  "email": "usuario@ejemplo.com",
  "name": "Juan Pérez",
  "createdAt": "2026-05-01T10:30:00Z"
}
```

---

### Reportes (Vista Materializada)

#### Obtener uso global de recursos

```http
GET /api/admin/reports/global-resources
Authorization: Bearer <token>
```

**Respuesta:**
```json
[
  {
    "region_id": 1,
    "region_name": "us-east-1",
    "total_cpu_used": 16,
    "total_ram_used": 65536,
    "total_storage_used": 500000
  }
]
```

> Esta vista se actualiza automáticamente al consultarla.

---

### Códigos de error

| Código | Descripción |
|--------|-------------|
| 400 | Solicitud inválida |
| 401 | No autorizado (token inválido o expirado) |
| 403 | Prohibido (sin permisos suficientes) |
| 404 | Recurso no encontrado |
| 409 | Conflicto (cuota excedida, IP no disponible) |
| 500 | Error interno del servidor |

---

## Roles y Permisos

| Rol | Permisos |
|-----|----------|
| `USER` | Crear/gestionar sus propias instancias, ver su facturación |
| `ADMIN` | Todas las operaciones, gestión de usuarios, ver toda la infraestructura |

---

## Desarrollo local (sin Docker)

### Backend

```bash
cd Backend
./gradlew build
./gradlew bootRun
```

### Frontend

```bash
cd Frontend
npm install
npm run dev
```

### Configuración manual

El backend requiere las siguientes variables de entorno:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bda_lab1
SPRING_DATASOURCE_USERNAME=bda_user
SPRING_DATASOURCE_PASSWORD=tu_password
KEYCLOAK_ISSUER_URI=http://localhost:8081/realms/host-usach
KEYCLOAK_JWK_SET_URI=http://localhost:8081/realms/host-usach/protocol/openid-connect/certs
```

---

## Estructura del proyecto

```
LAB1/
├── Frontend/                  # Vue 3 + Vite + TypeScript
│   ├── src/
│   │   ├── views/             # Páginas principales
│   │   ├── components/        # Componentes reutilizables
│   │   ├── stores/           # Pinia stores
│   │   └── router/           # Configuración de rutas
│   └── package.json
├── Backend/                   # Spring Boot 3.5
│   ├── src/main/java/        # Código fuente Java
│   │   ├── Controllers/       # Controladores REST
│   │   ├── Services/         # Lógica de negocio
│   │   ├── Repository/       # Acceso a datos
│   │   └── Config/           # Configuración de seguridad
│   └── src/main/resources/
│       └── db/               # SQL: índices, procedimientos, triggers
├── BD/                       # Scripts SQL de base de datos
│   ├── createDb.sql         # Esquema de tablas
│   └── triggers.sql         # Definición de triggers
├── Keycloak/                # Configuración de Keycloak
│   └── realm-export.json    # Realm exportado
├── docker-compose.yml       # Orquestación de servicios
└── .env                     # Variables de entorno
```