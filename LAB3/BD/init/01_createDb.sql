CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE "CPU" (
  "Cpu_id"   BIGSERIAL PRIMARY KEY,
  "Quantity" INTEGER        NOT NULL,
  "Cost_ph"  REAL           NOT NULL
);

CREATE TABLE "Ip" (
  "Ip_id"      SERIAL PRIMARY KEY,
  "Ip_address" VARCHAR(80)  NOT NULL,
  "Assigned"       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE "Ram" (
  "Ram_id"   BIGSERIAL PRIMARY KEY,
  "Quantity" INTEGER        NOT NULL,
  "Cost_ph"  REAL           NOT NULL
);

CREATE TABLE "Storage" (
  "Storage_id" BIGSERIAL PRIMARY KEY,
  "Quantity"   INTEGER        NOT NULL,
  "Cost_ph"    REAL           NOT NULL
);

CREATE TABLE "Region" (
  "Region_id" BIGSERIAL PRIMARY KEY,
  "Name"      VARCHAR(80) NOT NULL,
  "Geom"      geometry(Polygon, 4326)
);

CREATE INDEX IF NOT EXISTS region_geom_idx ON "Region" USING GIST ("Geom");

CREATE TABLE "Role" (
  "Role_id" BIGSERIAL PRIMARY KEY,
  "Role"    VARCHAR(30) NOT NULL
);


CREATE TABLE "Users" (
  "User_id"       BIGSERIAL PRIMARY KEY,
  "Email"         VARCHAR(80) NOT NULL UNIQUE,
  "Name"          VARCHAR(80) NOT NULL,
  "Max_instances" INTEGER     NOT NULL DEFAULT 5,
  "Lock"          BOOLEAN     NOT NULL DEFAULT FALSE,
  "Password_hash" VARCHAR(255)
);


CREATE TABLE "User_role" (
  "User_role_id" BIGSERIAL PRIMARY KEY,
  "User_id"      BIGINT NOT NULL,
  "Role_id"      BIGINT NOT NULL,
  CONSTRAINT "FK_User_role_User_id"
    FOREIGN KEY ("User_id")
      REFERENCES "Users"("User_id")
      ON DELETE CASCADE,
  CONSTRAINT "FK_User_role_Role_id"
    FOREIGN KEY ("Role_id")
      REFERENCES "Role"("Role_id")
      ON DELETE RESTRICT
);

-- La entidad Instance vive en MongoDB. Las FKs en Postgres apuntan por
-- Instance_id (VARCHAR(24) = ObjectId hex) sin constraint referencial, porque
-- la integridad se valida en app.
CREATE TABLE "Consumption" (
  "Consumption_id" BIGSERIAL PRIMARY KEY,
  "Instance_id"    BIGINT              NOT NULL,
  "Cpu_stats"      DOUBLE PRECISION    NOT NULL DEFAULT 0,
  "Ram_stats"      DOUBLE PRECISION    NOT NULL DEFAULT 0,
  "Storage_stats"  DOUBLE PRECISION    NOT NULL DEFAULT 0,
  "Created_at"     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS consumption_instance_idx ON "Consumption" ("Instance_id");
CREATE INDEX IF NOT EXISTS consumption_created_idx  ON "Consumption" ("Created_at");


CREATE TABLE "Ticket" (
  "Ticket_id"   BIGSERIAL PRIMARY KEY,
  "Status"      VARCHAR(30)  NOT NULL DEFAULT 'Open',
  "Description" TEXT,
  "Instance_id" BIGINT       NOT NULL,
  "User_id"     BIGINT       NOT NULL,
  "Usage"       INTERVAL     NOT NULL DEFAULT INTERVAL '0 seconds',
  "Price"       REAL         NOT NULL DEFAULT 0,
  "Created_at"  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT "FK_Ticket_User_id"
    FOREIGN KEY ("User_id")
      REFERENCES "Users"("User_id")
      ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS ticket_instance_idx ON "Ticket" ("Instance_id");

CREATE TABLE "Datacenter" (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    capacity INTEGER NOT NULL,
    current_instances INTEGER DEFAULT 0,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    region_id BIGINT NOT NULL,
    risk_zone_id BIGINT NOT NULL,
    geom geometry(Point,4326)
);

-- Insertar rol admin
INSERT INTO "Role" ("Role") VALUES ('admin'), ('user');

-- Insertar usuario admin
INSERT INTO "Users" ("Email", "Name", "Max_instances", "Lock")
VALUES ('admin@gmail.com', 'Admin', 10, false);

-- Asignar rol admin al usuario
INSERT INTO "User_role" ("User_id", "Role_id")
SELECT u."User_id", r."Role_id"
FROM "Users" u, "Role" r
WHERE u."Email" = 'admin@gmail.com' AND r."Role" = 'admin';

-- Insertar datos de prueba para recursos
INSERT INTO "CPU" ("Quantity", "Cost_ph") VALUES
(1, 0.05), (2, 0.10), (4, 0.20), (8, 0.40);

INSERT INTO "Ram" ("Quantity", "Cost_ph") VALUES
(1, 0.02), (2, 0.04), (4, 0.08), (8, 0.16);

INSERT INTO "Storage" ("Quantity", "Cost_ph") VALUES
(10, 0.01), (20, 0.02), (50, 0.05), (100, 0.10);

INSERT INTO "Region" ("Name", "Geom") VALUES
('us-east', ST_GeomFromText('POLYGON((-80 25, -80 45, -67 45, -67 25, -80 25))', 4326)),
('us-west', ST_GeomFromText('POLYGON((-125 32, -125 49, -103 49, -103 32, -125 32))', 4326)),
('europe',  ST_GeomFromText('POLYGON((-10 36, -10 60, 40 60, 40 36, -10 36))', 4326));

INSERT INTO "Datacenter" ("name", "status", "capacity", "current_instances", "latitude", "longitude", "region_id", "risk_zone_id", "geom") VALUES
('DC1', 'OPERATIVO', 10, 3, 34.2481355458975,  -118.2568359375000, 2, 10, ST_GeomFromText('POINT(-118.2568359375 34.2481355458975)', 4326));