BEGIN;

TRUNCATE TABLE
	"Order_Detail",
	"Order_Detail_Product",
	"Order",
	"Dealer",
	"Client_Address",
	"Client",
	"Product",
	"Company_Commune",
	"Company",
	"Region",
	"Commune_Address",
	"Commune",
	"Address",
	"Type_Transport"
CASCADE;

INSERT INTO "Type_Transport" ("Transport_id", "TransportName") VALUES
	(1, 'BICICLETA'),
	(2, 'AUTO'),
	(3, 'MOTO');

INSERT INTO "Address" ("Address_id", "Name") VALUES
	(1, 'Av. Alameda 1234'),
	(2, 'Calle Los Aromos 455'),
	(3, 'Pasaje El Roble 89'),
	(4, 'Av. Libertad 2201'),
	(5, 'Camino Las Parcelas 780'),
	(6, 'Calle San Martin 350'),
	(7, 'Av. Argentina 1005'),
	(8, 'Calle Arturo Prat 640'),
	(9, 'Av. Vicuña Mackenna 912'),
	(10, 'Pasaje Los Canelos 71'),
	(11, 'Calle Bellavista 501'),
	(12, 'Av. Los Leones 1900');

INSERT INTO "Address" ("Address_id", "Name")
SELECT
	i,
	'Direccion Complementaria ' || i
FROM generate_series(13, 40) AS gs(i);

INSERT INTO "Region" ("Region_id", "Name") VALUES
	(1, 'Metropolitana');

INSERT INTO "Commune" ("Commune_id", "Name", "Region_id") VALUES
    (1, 'Santiago', 1),
    (2, 'Providencia', 1),
    (3, 'Ñuñoa', 1),
    (4, 'Maipú', 1),
    (5, 'La Florida', 1),
    (6, 'San Miguel', 1),
    (7, 'Las Condes', 1),
    (8, 'Vitacura', 1),
    (9, 'Lo Barnechea', 1),
    (10, 'Peñalolén', 1),
    (11, 'La Reina', 1),
    (12, 'Macul', 1),
    (13, 'Independencia', 1),
    (14, 'Recoleta', 1),
    (15, 'Conchalí', 1),
    (16, 'Renca', 1),
    (17, 'Quilicura', 1),
    (18, 'Pudahuel', 1),
    (19, 'Cerro Navia', 1),
    (20, 'Lo Prado', 1),
    (21, 'Estación Central', 1),
    (22, 'Cerrillos', 1),
    (23, 'Maipú Poniente', 1),
    (24, 'San Joaquín', 1),
    (25, 'San Ramón', 1),
    (26, 'La Granja', 1),
    (27, 'La Pintana', 1),
    (28, 'El Bosque', 1),
    (29, 'Puente Alto', 1),
    (30, 'Pirque', 1);

INSERT INTO "Commune_Address" ("Commune_Address_id", "Commune_id", "Address_id")
SELECT
	i,
	i,
	i
FROM generate_series(1, 30) AS gs(i);

INSERT INTO "Commune_Address" ("Commune_Address_id", "Commune_id", "Address_id")
SELECT
	30 + i,
	i,
	30 + i
FROM generate_series(1, 10) AS gs(i);



INSERT INTO "Company" ("Company_id", "Name") VALUES
	(1, 'Distribuidora Andina'),
	(2, 'Mercado Central Spa'),
	(3, 'Bodega Sur Ltda'),
	(4, 'FreshBox Chile');

INSERT INTO "Company_Commune" ("Company_Commune", "Company_id", "Commune_id") VALUES
	(1, 1, 1),
	(2, 2, 2),
	(3, 3, 4),
	(4, 4, 5);

INSERT INTO "Client" ("Client_id", "Name") VALUES
	(1, 'Camila Rojas'),
	(2, 'Matias Herrera'),
	(3, 'Sofia Gonzalez'),
	(4, 'Diego Silva'),
	(5, 'Valentina Moya'),
	(6, 'Nicolas Fuentes'),
	(7, 'Javiera Pizarro'),
	(8, 'Benjamin Araya'),
	(9, 'Francisca Medina'),
	(10, 'Tomas Castillo'),
	(11, 'Martina Soto'),
	(12, 'Ignacio Vega');

INSERT INTO "Client" ("Client_id", "Name") VALUES
	(13, 'Paula Contreras'),
	(14, 'Rodrigo Escobar'),
	(15, 'Fernanda Bravo'),
	(16, 'Cristobal Salinas'),
	(17, 'Antonia Morales'),
	(18, 'Sebastian Riquelme'),
	(19, 'Catalina Jara'),
	(20, 'Vicente Rios'),
	(21, 'Daniela Muñoz'),
	(22, 'Emilio Paredes'),
	(23, 'Josefa Aguilar'),
	(24, 'Alonso Navarro'),
	(25, 'Florencia Ibarra'),
	(26, 'Maximiliano Tapia'),
	(27, 'Renata Cardenas'),
	(28, 'Gabriel Figueroa'),
	(29, 'Amanda Carrasco'),
	(30, 'Franco Toledo');


INSERT INTO "Client_Address" ("Client_Address_id", "Client_id", "Address_id")
SELECT
	i,
	i,
	i
FROM generate_series(1, 30) AS gs(i);

INSERT INTO "Product" ("Product_id", "Name", "Company_id", "Price") VALUES
	(1, 'Arroz 1kg', 1, 1800),
	(2, 'Fideos Espagueti', 1, 1400),
	(3, 'Aceite 1L', 2, 4200),
	(4, 'Cafe Molido 250g', 2, 5300),
	(5, 'Leche Entera 1L', 3, 1200),
	(6, 'Pan de Molde', 3, 2400),
	(7, 'Huevos Docena', 4, 3500),
	(8, 'Manzanas 1kg', 4, 2100),
	(9, 'Tomate 1kg', 4, 1900),
	(10, 'Pollo Entero', 3, 6200),
	(11, 'Queso Laminado', 2, 4500),
	(12, 'Yogurt Natural', 1, 1100);

INSERT INTO "Dealer" ("Dealer_id", "Transport_id", "Name") VALUES
	(1, 1, 'Pedro Torres'),
	(2, 2, 'Ana Mardones'),
	(3, 3, 'Luis Cifuentes'),
	(4, 1, 'Daniela Campos'),
	(5, 2, 'Jorge Saavedra'),
	(6, 3, 'Carla Sepulveda');



INSERT INTO "Order" ("Order_id", "Dealer_id", "Client_id", "Date")
SELECT
	i,
	((i - 1) % 6) + 1 AS dealer_id,
	((i - 1) % 30) + 1 AS client_id,
	(
		date '2023-01-01'
		+ (((i * 17) % 1180) || ' days')::interval
		+ ((9 + (i % 10)) || ' hours')::interval
		+ (((i * 7) % 60) || ' minutes')::interval
	)::timestamp AS order_date
FROM generate_series(1, 140) AS gs(i);

WITH detail_source AS (
    SELECT
        o."Order_id",
        o."Client_id",
        ((o."Order_id" * 3) % 12) + 1 AS product_id
    FROM "Order" o
)
INSERT INTO "Order_Detail" ("OrderDetail_id", "Client_id", "Order_id", "Total_Price")
SELECT
    ds."Order_id",
    ds."Client_id",
    ds."Order_id",
    p."Price" AS total_price
FROM detail_source ds
JOIN "Product" p ON p."Product_id" = ds.product_id;

WITH odp_source AS (
    SELECT
        o."Order_id" AS order_id,
        ((o."Order_id" * 3) % 12) + 1 AS product_id
    FROM "Order" o
)
INSERT INTO "Order_Detail_Product" ("Order_Detail_Product_id", "OrderDetail_id", "Product_id")
SELECT
    row_number() OVER (ORDER BY s.order_id),
    s.order_id,
    s.product_id
FROM odp_source s;

COMMIT;