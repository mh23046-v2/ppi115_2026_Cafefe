-- =============================================================================
-- DATOS DE DEMOSTRACIÓN CAFEFE (UUID fijos, re-ejecutable)
-- Archivo: sql/datos-demo.sql
--
-- Cómo usarlo: sobre la base ya creada con el esquema (en UTF-8, por las tildes):
--   psql -U <usuario> -d <base> -f sql/datos-demo.sql
-- Es re-ejecutable: cada INSERT lleva ON CONFLICT DO NOTHING. Para partir de
-- cero, correr antes sql/limpiar-bd.sql.
-- No deja ninguna factura en estado PENDIENTE a propósito: una factura PENDIENTE
-- esconde sus líneas de la cocina y de la caja (ver contrato REST).
--
-- LOGIN (nombre + UID):
--   nombre | UID                                    | roles activos             | pantallas
--   -------+----------------------------------------+---------------------------+-----------------
--   Juan   | bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb   | Cajero                    | caja
--   Ana    | b2222222-2222-2222-2222-222222222222   | Camarero                  | cocina
--   Luis   | b3333333-3333-3333-3333-333333333333   | Barista                   | cocina
--   María  | b4444444-4444-4444-4444-444444444444   | Atención al Cliente       | (sin pantalla definida)
--   Carla  | b5555555-5555-5555-5555-555555555555   | Cajero + Camarero         | caja + cocina
--   Casos que deben dar 401:
--   Pedro  | b6666666-6666-6666-6666-666666666666   | empleado INACTIVO
--   Sofía  | b7777777-7777-7777-7777-777777777777   | su única asignación está inactiva
--   Mateo  | b8888888-8888-8888-8888-888888888888   | su único rol (Supervisor) está inactivo
--   (y cualquier nombre que no coincida con el UID, o un UID que no sea UUID)
--   Mesero Virtual (aaaaaaaa-...): empleado de sistema, rol "Tienda en línea"; no inicia sesión.
--
-- ÓRDENES DE PRUEBA:
--   ab000001, ab000002  pendientes, tomadas por el Mesero Virtual (observaciones "Cliente: ...")
--   ab000003            pendiente, tomada por una camarera (observaciones "Mesa 4", sin prefijo "Cliente:")
--   ab000004            pagada completa  -> factura fa000001 PAGADA (úsela para GET /factura/{id}/ticket)
--   ab000005            pagada parcialmente: 2 líneas en factura fa000002 PAGADA y 1 línea pendiente
-- =============================================================================

BEGIN;

-- 1. ROLES --------------------------------------------------------------------
INSERT INTO public.rol (id_rol, nombre, activo, observaciones) VALUES
('11111111-1111-1111-1111-111111111111', 'Cajero', true, 'Cobra facturas en sucursal'),
('22222222-2222-2222-2222-222222222222', 'Camarero', true, 'Toma órdenes en mesa'),
('33333333-3333-3333-3333-333333333333', 'Tienda en línea', true, 'Rol exclusivo para pedidos del e-commerce'),
('44444444-4444-4444-4444-444444444444', 'Barista', true, 'Prepara bebidas en barra'),
('55555555-5555-5555-5555-555555555555', 'Atención al Cliente', true, 'Atiende y toma órdenes en mostrador'),
('66666666-6666-6666-6666-666666666666', 'Supervisor', false, 'Rol INACTIVO (caso de prueba de login)')
ON CONFLICT DO NOTHING;

-- 2. EMPLEADOS ----------------------------------------------------------------
INSERT INTO public.empleado (id_empleado, nombre, apellido, activo, comentarios) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Mesero', 'Virtual', true, 'Usuario de sistema para pedidos online'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Juan', 'Pérez', true, 'Cajero turno matutino'),
('b2222222-2222-2222-2222-222222222222', 'Ana', 'Gómez', true, 'Camarera'),
('b3333333-3333-3333-3333-333333333333', 'Luis', 'Martínez', true, 'Barista'),
('b4444444-4444-4444-4444-444444444444', 'María', 'Rivera', true, 'Atención al cliente'),
('b5555555-5555-5555-5555-555555555555', 'Carla', 'Mejía', true, 'Con dos roles (cajera y camarera)'),
('b6666666-6666-6666-6666-666666666666', 'Pedro', 'Alas', false, 'INACTIVO (caso de prueba de login)'),
('b7777777-7777-7777-7777-777777777777', 'Sofía', 'Ramos', true, 'Asignación de rol inactiva (caso de prueba de login)'),
('b8888888-8888-8888-8888-888888888888', 'Mateo', 'Cruz', true, 'Rol inactivo (caso de prueba de login)')
ON CONFLICT DO NOTHING;

-- 3. EMPLEADO_ROL -------------------------------------------------------------
INSERT INTO public.empleado_rol (id_empleado_rol, id_empleado, id_rol, activo, observaciones) VALUES
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', true, 'Mesero web (lo usa POST /orden)'),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111', true, 'Cajero matutino'),
('d2222222-2222-2222-2222-222222222222', 'b2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', true, 'Camarera'),
('d3333333-3333-3333-3333-333333333333', 'b3333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', true, 'Barista'),
('d4444444-4444-4444-4444-444444444444', 'b4444444-4444-4444-4444-444444444444', '55555555-5555-5555-5555-555555555555', true, 'Atención al cliente'),
('d5555551-5555-5555-5555-555555555555', 'b5555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', true, 'Carla como cajera'),
('d5555552-5555-5555-5555-555555555555', 'b5555555-5555-5555-5555-555555555555', '22222222-2222-2222-2222-222222222222', true, 'Carla como camarera'),
('d6666666-6666-6666-6666-666666666666', 'b6666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111', true, 'Asignación activa de un empleado inactivo'),
('d7777777-7777-7777-7777-777777777777', 'b7777777-7777-7777-7777-777777777777', '44444444-4444-4444-4444-444444444444', false, 'Asignación INACTIVA'),
('d8888888-8888-8888-8888-888888888888', 'b8888888-8888-8888-8888-888888888888', '66666666-6666-6666-6666-666666666666', true, 'Asignación a un rol inactivo')
ON CONFLICT DO NOTHING;

-- 4. CAJAS --------------------------------------------------------------------
INSERT INTO public.caja (id_caja, nombre, activo, observaciones) VALUES
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Caja Principal', true, 'Caja ubicada en la entrada'),
('e2222222-e222-e222-e222-e22222222222', 'Caja Express', true, 'Caja rápida, pocos productos'),
('e3333333-e333-e333-e333-e33333333333', 'Caja Antigua', false, 'INACTIVA (no debe ofrecerse al cobrar)')
ON CONFLICT DO NOTHING;

-- 5. TIPOS DE PRODUCTO (categorías) ---------------------------------------------
INSERT INTO public.tipo_producto (id_tipo_producto, nombre, activo, observaciones) VALUES
('f0000000-f000-f000-f000-f00000000000', 'Bebidas Calientes', true, 'Cafés e infusiones'),
('f1111111-f111-f111-f111-f11111111111', 'Postres', true, 'Pastelería y panes'),
('f2222222-f222-f222-f222-f22222222222', 'Bebidas Frías', true, 'Frappés y bebidas heladas'),
('f3333333-f333-f333-f333-f33333333333', 'Temporada', false, 'Categoría INACTIVA (no debe mostrarse al cliente)')
ON CONFLICT DO NOTHING;

-- 6. PRODUCTOS --------------------------------------------------------------------
INSERT INTO public.producto (id_producto, nombre, activo, comentarios, precio_sugerido) VALUES
('00000001-0000-0000-0000-000000000000', 'Café Americano', true, 'Taza 8 oz', 2.50),
('00000002-0000-0000-0000-000000000000', 'Latte Vainilla', true, 'Taza 12 oz', 3.75),
('00000003-0000-0000-0000-000000000000', 'Croissant de Mantequilla', true, 'Horneado del día', 2.00),
('00000004-0000-0000-0000-000000000000', 'Cappuccino', true, 'Taza 10 oz', 3.25),
('00000005-0000-0000-0000-000000000000', 'Chocolate Caliente', true, 'Con crema batida', 3.50),
('00000006-0000-0000-0000-000000000000', 'Cheesecake de Fresa', true, 'Porción individual', 4.25),
('00000007-0000-0000-0000-000000000000', 'Brownie', true, 'Con nueces', 2.75),
('00000008-0000-0000-0000-000000000000', 'Muffin de Arándano', false, 'Producto INACTIVO en categoría activa', 2.50),
('00000009-0000-0000-0000-000000000000', 'Frappé de Caramelo', true, 'Vaso 16 oz', 4.50),
('0000000a-0000-0000-0000-000000000000', 'Limonada', true, 'Natural, vaso 12 oz', 2.25),
('0000000b-0000-0000-0000-000000000000', 'Cold Brew', true, 'Infusión en frío', 3.50),
('0000000c-0000-0000-0000-000000000000', 'Latte de Calabaza', true, 'Producto activo en categoría INACTIVA', 4.00)
ON CONFLICT DO NOTHING;

-- 7. PRODUCTO_TIPO_PRODUCTO ---------------------------------------------------------
INSERT INTO public.producto_tipo_producto (id_producto_tipo_producto, id_producto, id_tipo_producto, fecha_creacion, observaciones) VALUES
('99999991-9999-9999-9999-999999999999', '00000001-0000-0000-0000-000000000000', 'f0000000-f000-f000-f000-f00000000000', now(), ''),
('99999992-9999-9999-9999-999999999999', '00000002-0000-0000-0000-000000000000', 'f0000000-f000-f000-f000-f00000000000', now(), ''),
('99999993-9999-9999-9999-999999999999', '00000003-0000-0000-0000-000000000000', 'f1111111-f111-f111-f111-f11111111111', now(), ''),
('99999994-9999-9999-9999-999999999999', '00000004-0000-0000-0000-000000000000', 'f0000000-f000-f000-f000-f00000000000', now(), ''),
('99999995-9999-9999-9999-999999999999', '00000005-0000-0000-0000-000000000000', 'f0000000-f000-f000-f000-f00000000000', now(), ''),
('99999996-9999-9999-9999-999999999999', '00000006-0000-0000-0000-000000000000', 'f1111111-f111-f111-f111-f11111111111', now(), ''),
('99999997-9999-9999-9999-999999999999', '00000007-0000-0000-0000-000000000000', 'f1111111-f111-f111-f111-f11111111111', now(), ''),
('99999998-9999-9999-9999-999999999999', '00000008-0000-0000-0000-000000000000', 'f1111111-f111-f111-f111-f11111111111', now(), ''),
('99999999-9999-9999-9999-999999999999', '00000009-0000-0000-0000-000000000000', 'f2222222-f222-f222-f222-f22222222222', now(), ''),
('9999999a-9999-9999-9999-999999999999', '0000000a-0000-0000-0000-000000000000', 'f2222222-f222-f222-f222-f22222222222', now(), ''),
('9999999b-9999-9999-9999-999999999999', '0000000b-0000-0000-0000-000000000000', 'f2222222-f222-f222-f222-f22222222222', now(), ''),
('9999999c-9999-9999-9999-999999999999', '0000000c-0000-0000-0000-000000000000', 'f3333333-f333-f333-f333-f33333333333', now(), '')
ON CONFLICT DO NOTHING;

-- 8. ÓRDENES ------------------------------------------------------------------------
-- ab000001 / ab000002: pendientes, pedidos en línea. ab000003: pendiente, tomada por una camarera.
-- ab000004: pagada completa. ab000005: pagada parcialmente.
INSERT INTO public.orden (id_orden, id_empleado_rol, fecha_creacion) VALUES
('ab000001-abab-abab-abab-abababababab', 'cccccccc-cccc-cccc-cccc-cccccccccccc', now() - interval '15 minutes'),
('ab000002-abab-abab-abab-abababababab', 'cccccccc-cccc-cccc-cccc-cccccccccccc', now() - interval '10 minutes'),
('ab000003-abab-abab-abab-abababababab', 'd2222222-2222-2222-2222-222222222222', now() - interval '5 minutes'),
('ab000004-abab-abab-abab-abababababab', 'cccccccc-cccc-cccc-cccc-cccccccccccc', now() - interval '90 minutes'),
('ab000005-abab-abab-abab-abababababab', 'cccccccc-cccc-cccc-cccc-cccccccccccc', now() - interval '60 minutes')
ON CONFLICT DO NOTHING;

-- Una fila por unidad (la tabla no tiene cantidad). El nombre del cliente va en observaciones.
INSERT INTO public.orden_producto (id_orden_producto, id_orden, id_producto, precio, observaciones) VALUES
-- ab000001 (Ana): total 9.50
('ac000001-acac-acac-acac-acacacacacac', 'ab000001-abab-abab-abab-abababababab', '00000002-0000-0000-0000-000000000000', 3.75, 'Cliente: Ana | sin azúcar'),
('ac000002-acac-acac-acac-acacacacacac', 'ab000001-abab-abab-abab-abababababab', '00000002-0000-0000-0000-000000000000', 3.75, 'Cliente: Ana'),
('ac000003-acac-acac-acac-acacacacacac', 'ab000001-abab-abab-abab-abababababab', '00000003-0000-0000-0000-000000000000', 2.00, 'Cliente: Ana'),
-- ab000002 (Carlos): total 6.75
('ac000004-acac-acac-acac-acacacacacac', 'ab000002-abab-abab-abab-abababababab', '00000001-0000-0000-0000-000000000000', 2.50, 'Cliente: Carlos'),
('ac000005-acac-acac-acac-acacacacacac', 'ab000002-abab-abab-abab-abababababab', '00000006-0000-0000-0000-000000000000', 4.25, 'Cliente: Carlos'),
-- ab000003 (camarera, sin prefijo "Cliente:"): total 8.25
('ac000006-acac-acac-acac-acacacacacac', 'ab000003-abab-abab-abab-abababababab', '00000004-0000-0000-0000-000000000000', 3.25, 'Mesa 4'),
('ac000007-acac-acac-acac-acacacacacac', 'ab000003-abab-abab-abab-abababababab', '00000007-0000-0000-0000-000000000000', 2.75, 'Mesa 4'),
('ac000008-acac-acac-acac-acacacacacac', 'ab000003-abab-abab-abab-abababababab', '0000000a-0000-0000-0000-000000000000', 2.25, 'Mesa 4'),
-- ab000004 (Luisa, pagada): total 7.25
('ac000009-acac-acac-acac-acacacacacac', 'ab000004-abab-abab-abab-abababababab', '00000009-0000-0000-0000-000000000000', 4.50, 'Cliente: Luisa'),
('ac00000a-acac-acac-acac-acacacacacac', 'ab000004-abab-abab-abab-abababababab', '00000007-0000-0000-0000-000000000000', 2.75, 'Cliente: Luisa'),
-- ab000005 (Sofía, parcial): 2 líneas pagadas (6.00) y 1 pendiente (2.00)
('ac00000b-acac-acac-acac-acacacacacac', 'ab000005-abab-abab-abab-abababababab', '0000000b-0000-0000-0000-000000000000', 3.50, 'Cliente: Sofía'),
('ac00000c-acac-acac-acac-acacacacacac', 'ab000005-abab-abab-abab-abababababab', '00000001-0000-0000-0000-000000000000', 2.50, 'Cliente: Sofía'),
('ac00000d-acac-acac-acac-acacacacacac', 'ab000005-abab-abab-abab-abababababab', '00000003-0000-0000-0000-000000000000', 2.00, 'Cliente: Sofía')
ON CONFLICT DO NOTHING;

-- 9. FACTURAS, PAGOS Y DETALLES (solo PAGADA / COMPLETADO) ------------------------------
INSERT INTO public.factura (id_factura, id_caja, id_empleado_rol, cliente, fecha_facturacion, estado, observaciones) VALUES
('fa000001-fafa-fafa-fafa-fafafafafafa', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'Luisa', now() - interval '80 minutes', 'PAGADA', 'Pago mixto: efectivo y tarjeta'),
('fa000002-fafa-fafa-fafa-fafafafafafa', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'd5555551-5555-5555-5555-555555555555', 'Sofía', now() - interval '50 minutes', 'PAGADA', 'Pago en efectivo con cambio')
ON CONFLICT DO NOTHING;

INSERT INTO public.factura_orden_producto (id_factura_orden_producto, id_factura, id_orden_producto, precio, observaciones) VALUES
('fb000001-fbfb-fbfb-fbfb-fbfbfbfbfbfb', 'fa000001-fafa-fafa-fafa-fafafafafafa', 'ac000009-acac-acac-acac-acacacacacac', 4.50, 'Cliente: Luisa'),
('fb000002-fbfb-fbfb-fbfb-fbfbfbfbfbfb', 'fa000001-fafa-fafa-fafa-fafafafafafa', 'ac00000a-acac-acac-acac-acacacacacac', 2.75, 'Cliente: Luisa'),
('fb000003-fbfb-fbfb-fbfb-fbfbfbfbfbfb', 'fa000002-fafa-fafa-fafa-fafafafafafa', 'ac00000b-acac-acac-acac-acacacacacac', 3.50, 'Cliente: Sofía'),
('fb000004-fbfb-fbfb-fbfb-fbfbfbfbfbfb', 'fa000002-fafa-fafa-fafa-fafafafafafa', 'ac00000c-acac-acac-acac-acacacacacac', 2.50, 'Cliente: Sofía')
ON CONFLICT DO NOTHING;

INSERT INTO public.pago (id_pago, id_factura, estado, observaciones) VALUES
('fc000001-fcfc-fcfc-fcfc-fcfcfcfcfcfc', 'fa000001-fafa-fafa-fafa-fafafafafafa', 'COMPLETADO', ''),
('fc000002-fcfc-fcfc-fcfc-fcfcfcfcfcfc', 'fa000002-fafa-fafa-fafa-fafafafafafa', 'COMPLETADO', '')
ON CONFLICT DO NOTHING;

-- fa000001: total 7.25 = efectivo 5.00 + tarjeta 2.25. fa000002: total 6.00, efectivo 10.00 (cambio 4.00).
INSERT INTO public.pago_detalle (id_pago_detalle, id_pago, monto, tipo_pago, observaciones, referencia_externa) VALUES
('fd000001-fdfd-fdfd-fdfd-fdfdfdfdfdfd', 'fc000001-fcfc-fcfc-fcfc-fcfcfcfcfcfc', 5.00, 'EFECTIVO', '', NULL),
('fd000002-fdfd-fdfd-fdfd-fdfdfdfdfdfd', 'fc000001-fcfc-fcfc-fcfc-fcfcfcfcfcfc', 2.25, 'TARJETA', '', 'AUT-100245'),
('fd000003-fdfd-fdfd-fdfd-fdfdfdfdfdfd', 'fc000002-fcfc-fcfc-fcfc-fcfcfcfcfcfc', 10.00, 'EFECTIVO', '', NULL)
ON CONFLICT DO NOTHING;

-- 10. DATOS PARA LOS CRUD ADMINISTRATIVOS (no los usan las 4 historias) -----------------
INSERT INTO public.tipo_descuento (id_tipo_descuento, nombre, activo, descuento_maximo, observaciones) VALUES
('d0000001-d0d0-d0d0-d0d0-d0d0d0d0d0d0', 'Promoción', true, 20, 'Descuento porcentual hasta 20 %')
ON CONFLICT DO NOTHING;

INSERT INTO public.descuento (id_descuento, id_tipo_descuento, nombre, fecha_desde, fecha_hasta, observaciones) VALUES
('d1000001-d1d1-d1d1-d1d1-d1d1d1d1d1d1', 'd0000001-d0d0-d0d0-d0d0-d0d0d0d0d0d0', 'Promo de temporada', now() - interval '30 days', now() + interval '60 days', '')
ON CONFLICT DO NOTHING;

INSERT INTO public.descuento_producto (id_descuento_producto, id_descuento, id_producto, fecha_desde, fecha_hasta, valor, observaciones) VALUES
('d2000001-d2d2-d2d2-d2d2-d2d2d2d2d2d2', 'd1000001-d1d1-d1d1-d1d1-d1d1d1d1d1d1', '00000002-0000-0000-0000-000000000000', now() - interval '30 days', now() + interval '60 days', 10, '')
ON CONFLICT DO NOTHING;

INSERT INTO public.tipo_caracteristica (id_tipo_caracteristica, nombre, expresion_regular, activo, observaciones) VALUES
('c0000001-c0c0-c0c0-c0c0-c0c0c0c0c0c0', 'Tamaño', '^(Chico|Mediano|Grande)$', true, 'Solo admite esos tres valores')
ON CONFLICT DO NOTHING;

INSERT INTO public.caracteristica (id_caracteristica, id_tipo_caracteristica, nombre, activo, observaciones) VALUES
('c1000001-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'c0000001-c0c0-c0c0-c0c0-c0c0c0c0c0c0', 'Tamaño de taza', true, '')
ON CONFLICT DO NOTHING;

INSERT INTO public.producto_caracteristica (id_producto_caracteristica, id_producto, id_caracteristica, valor) VALUES
('c2000001-c2c2-c2c2-c2c2-c2c2c2c2c2c2', '00000002-0000-0000-0000-000000000000', 'c1000001-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'Mediano')
ON CONFLICT DO NOTHING;

COMMIT;
