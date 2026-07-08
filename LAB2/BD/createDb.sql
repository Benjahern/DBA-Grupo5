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
  "Map_top"   REAL,
  "Map_left"  REAL
);

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


CREATE TABLE "Instance" (
  "Instance_id"  BIGSERIAL PRIMARY KEY,
  "Name"         VARCHAR(80)    NOT NULL,
  "Ram_id"       BIGINT         NOT NULL,
  "Cpu_id"       BIGINT         NOT NULL,
  "Storage_id"   BIGINT         NOT NULL,
  "State"        VARCHAR(80)    NOT NULL CHECK ("State" IN ('Running', 'Stopped', 'Terminated')),
  "User_id"      BIGINT         NOT NULL,
  "Region_id"    BIGINT         NOT NULL,
  "Terminated"   BOOLEAN        NOT NULL DEFAULT FALSE,
  "Container_id" VARCHAR(80),            
  "Started_at"   TIMESTAMP WITHOUT TIME ZONE,
  "Active_hours" INTERVAL       NOT NULL DEFAULT INTERVAL '0 seconds',
  "Ip_address"   VARCHAR(80),
  "Color"        VARCHAR(80)    NOT NULL,
  CONSTRAINT "FK_Instance_Ram_id"
    FOREIGN KEY ("Ram_id")      REFERENCES "Ram"("Ram_id"),
  CONSTRAINT "FK_Instance_Cpu_id"
    FOREIGN KEY ("Cpu_id")      REFERENCES "CPU"("Cpu_id"),
  CONSTRAINT "FK_Instance_Storage_id"
    FOREIGN KEY ("Storage_id")  REFERENCES "Storage"("Storage_id"),
  CONSTRAINT "FK_Instance_User_id"
    FOREIGN KEY ("User_id")     REFERENCES "Users"("User_id"),
  CONSTRAINT "FK_Instance_Region_id"
    FOREIGN KEY ("Region_id")   REFERENCES "Region"("Region_id")
);


CREATE TABLE "Consumption" (
  "Consumption_id" BIGSERIAL PRIMARY KEY,
  "Instance_id"    BIGINT           NOT NULL,
  "Cpu_stats"      DOUBLE PRECISION NOT NULL DEFAULT 0,
  "Ram_stats"      DOUBLE PRECISION NOT NULL DEFAULT 0,
  "Storage_stats"  DOUBLE PRECISION NOT NULL DEFAULT 0,
  "Created_at"     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT "FK_Consumption_Instance_id"
    FOREIGN KEY ("Instance_id")
      REFERENCES "Instance"("Instance_id")
      ON DELETE CASCADE
);


CREATE TABLE "Ticket" (
  "Ticket_id"   BIGSERIAL PRIMARY KEY,
  "Instance_id" BIGINT     NOT NULL,
  "Usage"       INTERVAL   NOT NULL,
  "Price"       REAL       NOT NULL,
  CONSTRAINT "FK_Ticket_Instance_id"
    FOREIGN KEY ("Instance_id")
      REFERENCES "Instance"("Instance_id")
      ON DELETE CASCADE
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

INSERT INTO "Region" ("Name", "Map_top", "Map_left") VALUES
('us-east', 33.0, 22.0), ('us-west', 34.0, 13.0), ('europe', 28.0, 50.0);
