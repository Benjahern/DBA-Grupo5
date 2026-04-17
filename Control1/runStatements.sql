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


-- Consulta 3 {Daniel}
-- Medios de transporte más usados para repartir los pedidos
-- por comuna de un cliente.

-- Tabla temporal para hacer el conteo de pedidos y el medio de transporte mas usado
WITH ConteoTransportes AS (
    SELECT 
        c."Name" AS Comuna, 
        t."TransportName" AS MedioTransporte, 
        COUNT(o."Order_id") AS TotalPedidos
    FROM "Order" o
    INNER JOIN "Client_Address" ca ON o."Client_id" = ca."Client_id"
    INNER JOIN "Commune_Address" coa ON ca."Address_id" = coa."Address_id"
    INNER JOIN "Commune" c ON coa."Commune_id" = c."Commune_id"
    INNER JOIN "Dealer" d ON o."Dealer_id" = d."Dealer_id"
    INNER JOIN "Type_Transport" t ON d."Transport_id" = t."Transport_id"
    GROUP BY c."Name", t."TransportName"
)

-- Consultamos la tabla temporal y filtramos los máximos
SELECT 
    ct1.Comuna,
    ct1.MedioTransporte,
    ct1.TotalPedidos
FROM ConteoTransportes ct1
WHERE ct1.TotalPedidos = (
    -- Buscamos el valor maximo de la comuna , para obtener el transporte mas usado (Usando MAX , para obtener el maximo)
    SELECT MAX(ct2.TotalPedidos)
    FROM ConteoTransportes ct2
    WHERE ct1.Comuna = ct2.Comuna
);


-- Consulta 4 {Daniel}
-- Lista de regiones con más pedidos por mes, en los últimos 3 años.

-- Calculamos los pedidos totales por Año, Mes y Región
WITH ConteoMensual AS (
    SELECT 
        EXTRACT(YEAR FROM o."Date") AS Anio,
        EXTRACT(MONTH FROM o."Date") AS Mes,
        r."Name" AS Region,
        COUNT(o."Order_id") AS TotalPedidos
    FROM "Order" o
    INNER JOIN "Client_Address" ca ON o."Client_id" = ca."Client_id"
    INNER JOIN "Commune_Address" coa ON ca."Address_id" = coa."Address_id"
    INNER JOIN "Commune" c ON coa."Commune_id" = c."Commune_id"
    INNER JOIN "Region" r ON c."Region_id" = r."Region_id"
    
    -- Aca filtramos el intervalo que queremos en este caso 3 años
    WHERE o."Date" >= CURRENT_DATE - INTERVAL '3 years'
    -- Agrupamos
    GROUP BY 
        EXTRACT(YEAR FROM o."Date"), 
        EXTRACT(MONTH FROM o."Date"), 
        r."Name"
)

-- Filtramos para dejar solo la región con mas pedidos ("más pedidos por mes")
SELECT 
    cm1.Anio,
    cm1.Mes,
    cm1.Region,
    cm1.TotalPedidos
FROM ConteoMensual cm1
WHERE cm1.TotalPedidos = (
    -- Subconsulta que busca el valor máximo para el mes y año que se está evaluando
    SELECT MAX(cm2.TotalPedidos)
    FROM ConteoMensual cm2
    WHERE cm1.Anio = cm2.Anio 
      AND cm1.Mes = cm2.Mes
)
ORDER BY cm1.Anio DESC, cm1.Mes DESC; --Finalmente ordenamos de mayor a menor año


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

-- Consulta 7 {Elías Zúñiga}
-- Lista de repartidores con la mayor cantidad de despachos mensuales,
-- en los últimos 3 años.

WITH MonthlyCounts AS (
    -- Paso 1: Calculamos el total de despachos por repartidor en cada mes (igual que antes)
    SELECT
        d."Name" AS Dealer,
        TO_CHAR(o."Date", 'YYYY-MM') AS YearMonth,
        COUNT(o."Order_id") AS TotalOrders
    FROM "Dealer" d
    JOIN "Order" o
        ON d."Dealer_id" = o."Dealer_id"
    WHERE o."Date" >= CURRENT_DATE - INTERVAL '3 years'
    GROUP BY
        d."Name",
        TO_CHAR(o."Date", 'YYYY-MM')
),
RankedDealers AS (
    -- Paso 2: Le asignamos un "ranking" a cada repartidor dentro de su mes respectivo
    SELECT 
        YearMonth,
        Dealer,
        TotalOrders,
        -- ROW_NUMBER() Enumera los resultados de un conjunto de resultados.
        -- Concretamente, devuelve el número secuencial de una fila dentro de una partición de
        -- un conjunto de resultados, empezando por 1 para la primera fila de cada partición.
        ROW_NUMBER() OVER(PARTITION BY YearMonth ORDER BY TotalOrders DESC) AS Ranking
    FROM MonthlyCounts
)
-- Paso 3: Nos quedamos solo con los #1 de cada mes
SELECT 
    YearMonth,
    Dealer AS BestDealer,
    TotalOrders AS TotalDespachos
FROM RankedDealers
WHERE Ranking = 1
-- Ordenamos cronológicamente para ver la evolución mes a mes
ORDER BY 
    YearMonth DESC;

-- Consulta 8 {Elías Zúñiga}
-- Lista de compañías que han recibido más dinero en el último año.

WITH CountProdsMoney AS (
    SELECT 
    c."Name" AS Company,
    o."Order_id",
    SUM(od."Total_Price") AS TotalMoney
	FROM "Company" c
	-- Saltamos hasta el detalle de la orden (igual que antes)
	JOIN "Product" p 
	    ON c."Company_id" = p."Company_id"
	JOIN "Order_Detail_Product" odp 
	    ON p."Product_id" = odp."Product_id"
	JOIN "Order_Detail" od 
	    ON odp."OrderDetail_id" = od."OrderDetail_id"
	-- ¡El salto nuevo! Llegamos a la tabla Order para obtener la fecha
	JOIN "Order" o 
	    ON od."Order_id" = o."Order_id"
	-- Filtramos para que la fecha sea mayor o igual a "hoy menos 1 año"
	WHERE o."Date" >= CURRENT_DATE - INTERVAL '1 year'
	-- Agrupamos todo por el nombre de la compañía y el ID del pedido
	GROUP BY 
	    c."Name", 
	    o."Order_id"
	-- (Opcional) Lo ordenamos para que sea más fácil de leer
	ORDER BY 
	    Company, 
	    o."Order_id"
)

-- Consulta final: Tomamos la tabla temporal y sumamos los totales de los pedidos
SELECT 
    Company,
    SUM(TotalMoney) AS Total
FROM CountProdsMoney
GROUP BY 
    Company
-- Ordenamos de mayor a menor para ver qué empresa recaudó más
ORDER BY 
    Total DESC;

-- Consulta 9 {Felipe Hidalgo}
-- Lista de repartidores que han llevado pedidos en moto o bicicleta
-- a las comunas de Providencia y Santiago Centro.

-- Utilizamos DISTINCT para repetir repartidores
SELECT DISTINCT
    d."Name" AS Repartidor,
    tt."TransportName" AS Transporte,
    com."Name" AS Comuna
-- Hcemos match con order para llegar a dealer
FROM "Order" o
JOIN "Dealer" d ON o."Dealer_id" = d."Dealer_id"
-- luego a tipo de transporte y por otro lado con client_address para llegar a comuna
JOIN "Type_Transport" tt ON d."Transport_id" = tt."Transport_id"
-- Para saber a la comuna del pedido, tenemos que seguir una cadena de relaciones: pedido → dirección del cliente → comuna de esa dirección
-- Ya que no hay una relación directa entre pedido y comuna
JOIN "Client_Address" ca ON o."Client_id" = ca."Client_id"
JOIN "Commune_Address" coa ON ca."Address_id" = coa."Address_id"
JOIN "Commune" com ON coa."Commune_id" = com."Commune_id"
-- Finalmente filtramos que el repartidor haya utilizado moto o bicicleta y que esta haya sido para las comunas de Providencia o Santiago
-- El control pide santiago centro, pero en la base de datos solo hay "Santiago", así que asumí que se refería a esa comuna
WHERE tt."TransportName" IN ('MOTO', 'BICICLETA')
  AND com."Name" IN ('Providencia', 'Santiago')
ORDER BY com."Name", d."Name";

-- Consulta 10 {Felipe Hidalgo}
-- Lista de clientes que han gastado más diariamente el mes pasado.

-- Calcula cuánto gastó cada cliente en cada día del mes pasado
-- El ::date convierte el timestamp a solo fecha (sin hora), asi dejamos los pedidos del mismo día juntos
-- El SUM suma todos los gastos de ese cliente en ese día. El WHERE con DATE_TRUNC es la forma correcta de filtrar el mes anterior completo
WITH GastoDiario AS (
    SELECT
        o."Date"::date AS Fecha,
        c."Name" AS Cliente,
        SUM(od."Total_Price") AS Gasto_Total
    FROM "Order" o
    JOIN "Order_Detail" od ON o."Order_id" = od."Order_id"
    JOIN "Client" c ON od."Client_id" = c."Client_id"
-- Trunca ambas fechas al inicio de su mes y las compara, capturando exactamente del 1 al último día del mes pasado.
    WHERE DATE_TRUNC('month', o."Date") = DATE_TRUNC('month', CURRENT_DATE - INTERVAL '1 month')
    GROUP BY o."Date"::date, c."Name"
),
-- Tomamos los resultados del paso anterior y les asignamos un ranking por día
-- PARTITION BY Fecha reinicia el conteo para cada día y el ORDER BY Gasto_Total DESC ordena de mayor a menor gasto dentro de ese día
-- El cliente que más gastó ese día queda con posicion = 1
-- Se usa RANK() y no ROW_NUMBER() para que si dos clientes gastaron exactamente lo mismo en un día, ambos queden con posición 1
RankingDiario AS (
    SELECT
        Fecha,
        Cliente,
        Gasto_Total,
        RANK() OVER (PARTITION BY Fecha ORDER BY Gasto_Total DESC) AS posicion
    FROM GastoDiario
)
-- del ranking solo nos quedamos con los que tienen posicion = 1, quiere decir que nos quedamos con los que mas gastaron en cada día
-- El ORDER BY Fecha DESC muestra los días más recientes primero.
SELECT
    Fecha,
    Cliente,
    Gasto_Total
FROM RankingDiario
WHERE posicion = 1
ORDER BY Fecha DESC;