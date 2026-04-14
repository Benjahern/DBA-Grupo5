-- Consulta 1 {Benja H}
-- Lista de clientes con más pedidos por compañía.
WITH Conteo AS ( 
    SELECT
    com."Name" AS Compania,
    c."Name" AS Cliente,
    COUNT(DISTINCT od."OrderDetail_id") AS total
    FROM "Client" c 
    JOIN "Order_Detail" od ON c."Client_id" = od."Client_id"
    JOIN "Order_Detail_Product" odp ON od."OrderDetail_id" = odp."OrderDetail_id"
    JOIN "Product" p ON odp."Product_id" = p."Product_id"
    JOIN "Company" com ON p."Company_id" = com."Company_id"
    GROUP BY com."Name", c."Name"
),
MasPedidos AS (
    SELECT Compania, Cliente, total,
    RANK() OVER(PARTITION BY Compania ORDER BY total DESC) as posicion FROM Conteo
)
SELECT Compania, Cliente, total
FROM MasPedidos WHERE posicion = 1;
        

-- Consulta 2 {Marco}
-- Producto menos pedidos por compañía.

-- Se cuenta los productos (Es casi lo mismo que el ejemplo de clases)
WITH CountProd AS (
    SELECT 
    p."Name" AS ProdName,
    p."Company_id",
    p."Product_id",
    COUNT(odp."Product_id") AS Cant
    FROM "Product" p
-- Acá la gracia, si no hay "match" del pedido y el producto lo deja como 0
-- y no inventa info, luego lo agrupé por producto y compañia
    LEFT JOIN "Order_Detail_Product" odp ON p."Product_id" = odp."Product_id"
    GROUP BY p."Product_id", p."Company_id", p."Name"
),

-- Ojo, solo conté en el paso anterior, ahora es cuando lo ordenamos

OrderProducts AS (
    SELECT
    "Company_id",
    ProdName,
    Cant,

-- La linea del RANK sirve para ordenar los productos por compañía, el que tenga menos pedidos
-- va a ser el número 1, el segundo número 2, etc.
-- Y como está ordenado ascendentemente, entonces el primero va a ser si o si el que tenga menos pedidos
    RANK() OVER (PARTITION BY "Company_id" ORDER BY Cant ASC) AS Order
    -- Llamado a la "Función" antes definida para contar los productos por compañía
    FROM CountProd
)

-- Finalmente muestra la consulta con el producto menos pedido por compañía.  
SELECT
    c."Name" AS Compania,
    op.ProdName AS Producto_Menos_Pedido,
    op.Cant AS Cantidad_Pedidos
FROM OrderProducts op
JOIN "Company" c ON op."Company_id" = c."Company_id"
-- Solo muestra el producto menos pedido por compañía (el que tenga el número 1 en la función RANK)
WHERE op.Order = 1;


-- Consulta 3 {}
-- Medios de transporte más usados para repartir los pedidos
-- por comuna de un cliente.

-- Consulta 4 {}
-- Lista de regiones con más pedidos por mes, en los últimos 3 años.

-- Consulta 5 {Benja S}
-- Lista de clientes por compañía que más ha pagado mensualmente.
WITH PagosMensuales AS (
    SELECT 
        DATE_TRUNC('month', o."Date") AS Mes,
        com."Name" AS Compania,
        c."Name" AS Cliente,
        SUM(od."Total_Price") AS Gasto_Total
    FROM "Order_Detail" od
    JOIN "Order" o ON od."Order_id" = o."Order_id"
    JOIN "Client" c ON od."Client_id" = c."Client_id"
    JOIN "Order_Detail_Product" odp ON od."OrderDetail_id" = odp."OrderDetail_id"
    JOIN "Product" p ON odp."Product_id" = p."Product_id"
    JOIN "Company" com ON p."Company_id" = com."Company_id"
    GROUP BY 1, 2, 3
),
RankingPagos AS (
    SELECT 
        Mes, Compania, Cliente, Gasto_Total,
        RANK() OVER(PARTITION BY Mes, Compania ORDER BY Gasto_Total DESC) as posicion
    FROM PagosMensuales
)
SELECT 
    TO_CHAR(Mes, 'YYYY-MM') AS "Periodo",
    Compania, 
    Cliente, 
    Gasto_Total
FROM RankingPagos 
WHERE posicion = 1
ORDER BY Mes DESC, Compania;

-- Consulta 6 {Benja S}
-- Pedido diario con más productos del último mes.
WITH ConteoProductos AS (
    SELECT 
        o."Date"::date AS Fecha,
        o."Order_id",
        COUNT(odp."Product_id") AS Cantidad_Productos
    FROM "Order" o
    JOIN "Order_Detail_Product" odp ON o."Order_id" = odp."OrderDetail_id"
    WHERE o."Date" >= CURRENT_DATE - INTERVAL '1 month'
    GROUP BY 1, 2
),
RankingDiario AS (
    SELECT 
        Fecha,
        "Order_id",
        Cantidad_Productos,
        RANK() OVER(PARTITION BY Fecha ORDER BY Cantidad_Productos DESC) as rank_dia
    FROM ConteoProductos
)
SELECT 
    Fecha,
    "Order_id" AS "ID_Pedido",
    Cantidad_Productos AS "Total_Productos"
FROM RankingDiario
WHERE rank_dia = 1
ORDER BY Fecha DESC;

-- Consulta 7 {}
-- Lista de repartidores con la mayor cantidad de despachos mensuales,
-- en los últimos 3 años.

-- Consulta 8 {}
-- Lista de compañías que han recibido más dinero en el último año.

-- Consulta 9 {}
-- Lista de repartidores que han llevado pedidos en moto o bicicleta
-- a las comunas de Providencia y Santiago Centro.

-- Consulta 10 {}
-- Lista de clientes que han gastado más diariamente el mes pasado.
