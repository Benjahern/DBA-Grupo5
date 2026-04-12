-- Consulta 1 {Benja H}
-- Lista de clientes con más pedidos por compañía.
WITH Conteo AS ( 
    SELECT
        com.Name AS Compania,
        c.Name AS Cliente,
        COUNT(od.OrderDetail_id) AS total
        
)

-- Consulta 2 {Marco}
-- Producto menos pedidos por compañía.

-- Consulta 3 {}
-- Medios de transporte más usados para repartir los pedidos
-- por comuna de un cliente.

-- Consulta 4 {}
-- Lista de regiones con más pedidos por mes, en los últimos 3 años.

-- Consulta 5 {}
-- Lista de clientes por compañía que más ha pagado mensualmente.

-- Consulta 6 {}
-- Pedido diario con más productos del último mes. 

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
