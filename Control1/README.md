# DBA-Grupo 5

## Prerrequisitos
Se requiere tener Docker instalado.

## Guía de ejecución
Una vez obtenido el código fuente, la ejecución se realiza desde la terminal.

**Primero, se debe ingresar al directorio "/Control1":**

cd /Control1

**Luego, se debe levantar el contenedor con el siguiente comando:**

docker compose up -d

**A continuación, se debe ejecutar el script de creación de la base de datos:**

docker compose exec -T db psql -U postgres -d postgres -v ON_ERROR_STOP=1 -f - < dbCreate.sql

**Después, se debe ejecutar el script de carga de datos:**

docker compose exec -T db psql -U postgres -d postgres -v ON_ERROR_STOP=1 -f - < loadData.sql

**Finalmente, se deben ejecutar las consultas:**

docker compose exec -T db psql -U postgres -d postgres -v ON_ERROR_STOP=1 -f - < runStatements.sql

## Explicación de las modificaciones del esquema
Se eliminó la entidad "Precio", ya que, tras un análisis, se determinó que tenía mayor sentido modelarla como un atributo y no como una entidad.

Además, a medida que se establecieron las relaciones, se identificaron múltiples relaciones de tipo "muchos a muchos". Por esta razón, se optó por crear tablas intermedias para mejorar el modelado y facilitar el uso de las consultas SQL.