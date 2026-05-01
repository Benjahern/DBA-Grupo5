CREATE SCHEMA IF NOT EXISTS keycloak;

CREATE TABLE "CPU" (
  "Cpu_id"   BIGSERIAL PRIMARY KEY,
  "Quantity" INTEGER        NOT NULL,
  "Cost_ph"  REAL           NOT NULL
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
  "Name"      VARCHAR(80) NOT NULL
);

-- NUEVA TABLA: Ip
-- Se incluye Region_id para que el SP sepa qué IPs ofrecer según la región
CREATE TABLE "Ip" (
  "Ip_id"      BIGSERIAL PRIMARY KEY,
  "Ip_address" VARCHAR(80)  NOT NULL UNIQUE,
  "Used"       BOOLEAN      NOT NULL DEFAULT FALSE,
  "Region_id"  BIGINT       NOT NULL,
  CONSTRAINT "FK_Ip_Region" FOREIGN KEY ("Region_id") REFERENCES "Region"("Region_id")
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
  "Lock"          BOOLEAN     NOT NULL DEFAULT FALSE
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
  -- He añadido 'Provisioning' al CHECK para que el SP no falle
  "State"        VARCHAR(80)    NOT NULL CHECK ("State" IN ('Provisioning', 'Running', 'Stopped', 'Terminated')),
  "User_id"      BIGINT         NOT NULL,
  "Region_id"    BIGINT         NOT NULL,
  "Terminated"   BOOLEAN        NOT NULL DEFAULT FALSE,
  "Container_id" VARCHAR(80),            
  "Started_at"   TIMESTAMP WITHOUT TIME ZONE,
  "Active_hours" INTERVAL       NOT NULL DEFAULT INTERVAL '0 seconds',
  "Ip_id"        BIGINT         UNIQUE, -- Relación 1:1 estricta
  "Color"        VARCHAR(80)    NOT NULL,
  "Base_image"   VARCHAR(80),   -- Añadido para compatibilidad con el SP
  CONSTRAINT "FK_Instance_Ram"     
    FOREIGN KEY ("Ram_id") REFERENCES "Ram"("Ram_id"),
  CONSTRAINT "FK_Instance_Cpu"     
    FOREIGN KEY ("Cpu_id")     REFERENCES "CPU"("Cpu_id"),
  CONSTRAINT "FK_Instance_Storage" 
    FOREIGN KEY ("Storage_id") REFERENCES "Storage"("Storage_id"),
  CONSTRAINT "FK_Instance_User"    
    FOREIGN KEY ("User_id")    REFERENCES "Users"("User_id"),
  CONSTRAINT "FK_Instance_Region"  
    FOREIGN KEY ("Region_id")  REFERENCES "Region"("Region_id"),
  CONSTRAINT "FK_Instance_Ip"      
    FOREIGN KEY ("Ip_id")      REFERENCES "Ip"("Ip_id")
);


CREATE TABLE "Consumption" (
  "Consumption_id" BIGSERIAL PRIMARY KEY,
  "Instance_id"    BIGINT           NOT NULL,
  "Cpu_stats"      DOUBLE PRECISION NOT NULL DEFAULT 0,
  "Ram_stats"      DOUBLE PRECISION NOT NULL DEFAULT 0,
  "Storage_stats"  DOUBLE PRECISION NOT NULL DEFAULT 0,
  "Created_at"     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT "FK_Consumption_Instance" 
    FOREIGN KEY ("Instance_id") 
    REFERENCES "Instance"("Instance_id") ON DELETE CASCADE
);

CREATE TABLE "Ticket" (
  "Ticket_id"   BIGSERIAL PRIMARY KEY,
  "Instance_id" BIGINT     NOT NULL,
  "Usage"       INTERVAL   NOT NULL,
  "Price"       REAL       NOT NULL,
  CONSTRAINT "FK_Ticket_Instance" 
    FOREIGN KEY ("Instance_id") 
      REFERENCES "Instance"("Instance_id") 
      ON DELETE CASCADE
);


INSERT INTO "Role" ("Role") VALUES ('user'), ('admin');

