# Objetivos

1. Diseñar esquemas de bases de datos relacionales normalizados.

2. Implementar lógica de servidor persistente mediante procedimientos almacenados y
disparadores.

3. Desarrollar una capa de servicios (API) que garantice la integridad y seguridad de los
datos.

4. Gestionar la autenticación y autorización de usuarios mediante el estándar JSON Web
Tokens (JWT).

5. Documentar técnicamente procesos de despliegue y consumo de servicios.

Motor de base de datos PostgreSQL
Backend -> springboot
Frontend ->  React

# Cosas Necesarias 
- middleware
- Vista materializada
- Indexacion
- Triggers
- Procedimientos almacenados

# Objetivo del proyecto
## Panel de control de infraestructura Cloud
Contexto: Un sistema para que los usuarios desplieguen y gestionen instancias de
servidores virtuales, bases de datos y consumo de ancho de banda.

1. API: CRUD de Máquinas Virtuales y Regiones de Datacenter.

2. API/JWT: El usuario inicia, apaga o destruye sus propias instancias; el SysAdmin ve toda la infraestructura.

3. Procedimiento Almacenado 1: Aprovisionar un nuevo servidor: asignarle una IP pública disponible, marcar la IP como ocupada y crear el registro de facturación por hora.

4. Procedimiento Almacenado 2: Generar la factura mensual consolidada calculando el tiempo de actividad de todas las instancias de un usuario.

5. Trigger 1: Bloquear la creación de un servidor si el usuario ha superado el límite de su cuota contratada.

6. Trigger 2: Liberar automáticamente la dirección IP asociada a un servidor en el momento en que este sea eliminado (destruido).

7. Vista Materializada: "Uso de Recursos Globales": suma de RAM, CPU y almacenamiento en uso agrupado por cada región de Datacenter.

8. Índices: Indexar el estado del servidor (ej. 'Running', 'Stopped') y la dirección IP.

9. API: Endpoint para cambiar el estado de un servidor (encender/apagar).

10. API: Endpoint de métricas que muestre el consumo proyectado del mes actual para un usuario, consumiendo datos directamente desde la base de datos.