

CREATE TABLE "Ip" (
  "Ip_id"      SERIAL PRIMARY KEY,
  "Ip_address" VARCHAR(80)  NOT NULL,
  "Used"       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE "CPU" (
  "CPU_id"   SERIAL PRIMARY KEY,
  "Quantity" INTEGER        NOT NULL,
  "Cost_ph"  DECIMAL(10,2)  NOT NULL
);

CREATE TABLE "Ram" (
  "Ram_id"   SERIAL PRIMARY KEY,
  "Quantity" INTEGER        NOT NULL,
  "Cost_ph"  DECIMAL(10,2)  NOT NULL
);

CREATE TABLE "Storage" (
  "Storage_id" SERIAL PRIMARY KEY,
  "Quantity"   INTEGER        NOT NULL,
  "Cost_ph"    DECIMAL(10,2)  NOT NULL
);

CREATE TABLE "Region" (
  "Region_id" SERIAL PRIMARY KEY,
  "Name"      VARCHAR(80) NOT NULL
);

CREATE TABLE "Role" (
  "Role_id" SERIAL PRIMARY KEY,
  "Role"    VARCHAR(30) NOT NULL
);


CREATE TABLE "Users" (
  "User_id"       SERIAL PRIMARY KEY,
  "Email"         VARCHAR(80) NOT NULL UNIQUE,
  "Password_hash" TEXT        NOT NULL,   
  "Name"          VARCHAR(80) NOT NULL,
  "Max_Instance"  INTEGER     NOT NULL DEFAULT 5,
  "Lock"          BOOLEAN     NOT NULL DEFAULT FALSE
);


CREATE TABLE "User_role" (
  "User_role_id" SERIAL PRIMARY KEY,
  "User_id"      INTEGER NOT NULL,
  "Role_id"      INTEGER NOT NULL,
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
  "Instance_id"  SERIAL PRIMARY KEY,
  "Name"         VARCHAR(80)    NOT NULL,
  "Ram_id"       INTEGER        NOT NULL,
  "CPU_id"       INTEGER        NOT NULL,
  "Storage_id"   INTEGER        NOT NULL,
  "State"        VARCHAR(80)    NOT NULL DEFAULT 'stopped',
  "User_id"      INTEGER        NOT NULL,
  "Region_id"    INTEGER        NOT NULL,
  "container_id" VARCHAR(80),            
  "Started_at"   TIMESTAMP,              
  "Active_hours" DECIMAL(10,2)  NOT NULL DEFAULT 0,
  "Ip_id"        INTEGER,
  CONSTRAINT "FK_Instance_Ram_id"
    FOREIGN KEY ("Ram_id")      REFERENCES "Ram"("Ram_id"),
  CONSTRAINT "FK_Instance_CPU_id"
    FOREIGN KEY ("CPU_id")      REFERENCES "CPU"("CPU_id"),
  CONSTRAINT "FK_Instance_Storage_id"
    FOREIGN KEY ("Storage_id")  REFERENCES "Storage"("Storage_id"),
  CONSTRAINT "FK_Instance_User_id"
    FOREIGN KEY ("User_id")     REFERENCES "Users"("User_id"),
  CONSTRAINT "FK_Instance_Region_id"
    FOREIGN KEY ("Region_id")   REFERENCES "Region"("Region_id"),
  CONSTRAINT "FK_Instance_Ip_id"
    FOREIGN KEY ("Ip_id")       REFERENCES "Ip"("Ip_id")
);


CREATE TABLE "Consumption" (
  "consumption_id" SERIAL PRIMARY KEY,
  "Instance_id"    INTEGER          NOT NULL,
  "CPU_Stats"      DOUBLE PRECISION NOT NULL DEFAULT 0,
  "Ram_Stats"      DOUBLE PRECISION NOT NULL DEFAULT 0,
  "Storage_Stats"  DOUBLE PRECISION NOT NULL DEFAULT 0,
  CONSTRAINT "FK_Consumption_Instance_id"
    FOREIGN KEY ("Instance_id")
      REFERENCES "Instance"("Instance_id")
      ON DELETE CASCADE
);


CREATE TABLE "Ticket" (
  "Ticket_id"   SERIAL PRIMARY KEY,
  "Instance_id" INTEGER        NOT NULL,
  "Usage"       TIMESTAMP      NOT NULL DEFAULT NOW(),
  "Price"       DECIMAL(10,2)  NOT NULL,
  CONSTRAINT "FK_Ticket_Instance_id"
    FOREIGN KEY ("Instance_id")
      REFERENCES "Instance"("Instance_id")
      ON DELETE CASCADE
);
