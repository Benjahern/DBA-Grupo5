CREATE TABLE "Order" (
  "Order_id" INTEGER PRIMARY KEY,
  "Dealer_id" INTEGER REFERENCES "Dealer"("Dealer_id"),
  "Client_id" INTEGER REFERENCES "Client"("Client_id"),
  "Date" TIMESTAMP
);

CREATE TABLE "Order_Detail_Product" (
  "Order_Detail_Product_id" INTEGER PRIMARY KEY,
  "Order_id" INTEGER REFERENCES "Order"("Order_id"),
  "Product_id" INTEGER REFERENCES "Product"("Product_id")
);

CREATE TABLE "Dealer" (
  "Dealer_id" INTEGER PRIMARY KEY,
  "Transport_id" INTEGER REFERENCES "Type_Transport"("Transport_id"),
  "Name" VARCHAR(80)
);

CREATE TABLE "Client" (
  "Client_id" INTEGER PRIMARY KEY,
  "Name" VARCHAR(80)
);

CREATE TABLE "Type_Transport" (
  "Transport_id" INTEGER PRIMARY KEY,
  "TransportName" VARCHAR(80)
);

CREATE TABLE "Order_Detail" (
  "OrderDetail_id" INTEGER PRIMARY KEY,
  "Client_id" INTEGER REFERENCES "Client"("Client_id"),
  "Order_id" INTEGER REFERENCES "Order"("Order_id"),
  "Total_Price" INTEGER
);

CREATE TABLE "Client_Addres" (
  "Client_Address_id" INTEGER PRIMARY KEY,
  "Client_id" INTEGER REFERENCES "Client"("Client_id"),
  "Addres_id" INTEGER REFERENCES "Addres"("Addres_id")
);

CREATE TABLE "Product" (
  "Product_id" INTEGER PRIMARY KEY,
  "Name" VARCHAR(80),
  "Company_id" INTEGER REFERENCES "Company"("Company_id"),
  "Price" INTEGER
);

CREATE TABLE "Addres" (
  "Addres_id" INTEGER PRIMARY KEY,
  "Name" VARCHAR(80)
);

CREATE TABLE "Company" (
  "Company_id" INTEGER PRIMARY KEY,
  "Name" VARCHAR(80)
);

CREATE TABLE "Company_Commune" (
  "Company_Commune" INTEGER PRIMARY KEY,
  "Company_id" INTEGER REFERENCES "Company"("Company_id"),
  "Commune_id" INTEGER REFERENCES "Commune"("Commune_id")
);

CREATE TABLE "Commune" (
  "Commune_id" INTEGER PRIMARY KEY,
  "Addres_id" INTEGER REFERENCES "Addres"("Addres_id"),
  "Name" VARCHAR(80)
);

CREATE TABLE "Region" (
  "Region_id" INTEGER PRIMARY KEY,
  "Commune_id" INTEGER REFERENCES "Commune"("Commune_id"),
  "Name" VARCHAR(80)
);

