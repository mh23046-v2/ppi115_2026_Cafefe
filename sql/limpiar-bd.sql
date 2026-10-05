-- =====================================================================
-- limpiar-bd.sql  |  Cafefe (PPI115 2026)
-- Deja la base de datos VACÍA (sin registros) para empezar una prueba
-- desde cero, como la revisión de la rúbrica ("Roles: sin registros previos").
--
-- * Es REUTILIZABLE: se puede ejecutar cuantas veces haga falta.
-- * Borra SOLO los datos; las tablas y sus columnas se conservan.
-- * Una sola sentencia TRUNCATE ... CASCADE: PostgreSQL resuelve el orden
--   de las llaves foráneas por sí mismo, no hay que ordenar las tablas.
-- * Todo ocurre en una transacción: si algo falla, no se borra nada.
--
-- Cómo ejecutarlo (con la app detenida):
--   docker exec -i <contenedor_postgres> psql -U postgres -d pos_ppi115_cafefe < limpiar-bd.sql
-- (el nombre del contenedor se ve con:  docker ps)
-- o en pgAdmin: Query Tool sobre pos_ppi115_cafefe -> pegar -> F5.
-- =====================================================================

BEGIN;

TRUNCATE TABLE
    pago_detalle,
    pago,
    factura_orden_producto,
    factura,
    caja,
    orden_producto,
    orden,
    descuento_producto,
    descuento,
    tipo_descuento,
    producto_caracteristica,
    producto_tipo_producto,
    caracteristica,
    tipo_caracteristica,
    producto,
    tipo_producto,
    empleado_rol,
    empleado,
    rol
RESTART IDENTITY CASCADE;

COMMIT;

-- ---------------------------------------------------------------------
-- Comprobación: todas las tablas deben mostrar 0 filas.
-- (Si aparece alguna tabla que no está en la lista de arriba, con filas,
--  agrégala al TRUNCATE.)
-- ---------------------------------------------------------------------
SELECT table_name AS tabla,
       (xpath('/row/c/text()',
              query_to_xml(format('SELECT count(*) AS c FROM %I.%I', table_schema, table_name),
                           false, true, '')))[1]::text::int AS filas
FROM information_schema.tables
WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
ORDER BY filas DESC, tabla;
