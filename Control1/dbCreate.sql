DROP TABLE IF EXISTS "Client" CASCADE;
CREATE TABLE "Client" (
  "Client_id" INTEGER PRIMARY KEY,
  "Name" VARCHAR(80)
);


DROP TABLE IF EXISTS "Address" CASCADE;
CREATE TABLE "Address" (
  "Address_id" INTEGER PRIMARY KEY,
  "Name" VARCHAR(80)
);

DROP TABLE IF EXISTS "Commune" CASCADE;
CREATE TABLE "Commune" (
  "Commune_id" INTEGER PRIMARY KEY,
  "Region_id" INTEGER REFERENCES "Region"("Region_id"),
  "Name" VARCHAR(80)
);

DROP TABLE IF EXISTS "Region" CASCADE;
CREATE TABLE "Region" (
  "Region_id" INTEGER PRIMARY KEY,
  "Name" VARCHAR(80)
);

DROP TABLE IF EXISTS "Company" CASCADE;
CREATE TABLE "Company" (
  "Company_id" INTEGER PRIMARY KEY,
  "Name" VARCHAR(80)
);

DROP TABLE IF EXISTS "Client_Address" CASCADE;
CREATE TABLE "Client_Address" (
  "Client_Address_id" INTEGER PRIMARY KEY,
  "Client_id" INTEGER REFERENCES "Client"("Client_id"),
  "Address_id" INTEGER REFERENCES "Address"("Address_id")
);

DROP TABLE IF EXISTS "Commune_Address" CASCADE;
CREATE TABLE "Commune_Address" (
  "Commune_Address_id" INTEGER PRIMARY KEY,
  "Commune_id" INTEGER REFERENCES "Commune"("Commune_id"),
  "Address_id" INTEGER REFERENCES "Address"("Address_id")
);

DROP TABLE IF EXISTS "Company_Commune" CASCADE;
CREATE TABLE "Company_Commune" (
  "Company_Commune" INTEGER PRIMARY KEY,
  "Company_id" INTEGER REFERENCES "Company"("Company_id"),
  "Commune_id" INTEGER REFERENCES "Commune"("Commune_id")
);

DROP TABLE IF EXISTS "Product" CASCADE;
CREATE TABLE "Product" (
  "Product_id" INTEGER PRIMARY KEY,
  "Name" VARCHAR(80),
  "Company_id" INTEGER REFERENCES "Company"("Company_id"),
  "Price" INTEGER
);


DROP TABLE IF EXISTS "Type_Transport" CASCADE;
CREATE TABLE "Type_Transport" (
  "Transport_id" INTEGER PRIMARY KEY,
  "TransportName" VARCHAR(80) CHECK ("TransportName" IN ('BICICLETA', 'AUTO', 'MOTO'))
);

DROP TABLE IF EXISTS "Dealer" CASCADE;
CREATE TABLE "Dealer" (
  "Dealer_id" INTEGER PRIMARY KEY,
  "Transport_id" INTEGER REFERENCES "Type_Transport"("Transport_id"),
  "Name" VARCHAR(80)
);

DROP TABLE IF EXISTS "Order" CASCADE;
CREATE TABLE "Order" (
  "Order_id" INTEGER PRIMARY KEY,
  "Dealer_id" INTEGER REFERENCES "Dealer"("Dealer_id"),
  "Client_id" INTEGER REFERENCES "Client"("Client_id"),
  "Date" TIMESTAMP
);

DROP TABLE IF EXISTS "Order_Detail" CASCADE;
CREATE TABLE "Order_Detail" (
  "OrderDetail_id" INTEGER PRIMARY KEY,
  "Client_id" INTEGER REFERENCES "Client"("Client_id"),
  "Order_id" INTEGER REFERENCES "Order"("Order_id"),
  "Total_Price" INTEGER
);

DROP TABLE IF EXISTS "Order_Detail_Product" CASCADE;
CREATE TABLE "Order_Detail_Product" (
  "Order_Detail_Product_id" INTEGER PRIMARY KEY,
  "OrderDetail_id" INTEGER REFERENCES "Order"("Order_id"),
  "Product_id" INTEGER REFERENCES "Product"("Product_id")
);



