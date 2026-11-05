-- Catalogo y ordenes de demostracion. Importes de ejemplo, no precios de mercado.
-- El marcador impide restaurar datos modificados o eliminados al reiniciar.
CREATE TABLE IF NOT EXISTS carga_demo (id INTEGER PRIMARY KEY, fechaCarga TIMESTAMP NOT NULL);

START TRANSACTION;

-- Stock disponible: ya excluye las unidades reservadas/consumidas de las ordenes demo.

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'DEMO - Memoria RAM DDR4 8GB', 28000.00, 8, 0
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'DEMO - Memoria RAM DDR4 8GB')
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'DEMO - SSD SATA 480GB', 48000.00, 4, 0
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'DEMO - SSD SATA 480GB')
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'DEMO - Bateria notebook', 65000.00, 3, 0
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'DEMO - Bateria notebook')
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'DEMO - Teclado notebook', 35000.00, 2, 0
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'DEMO - Teclado notebook')
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'DEMO - Pantalla LED 15.6 pulgadas', 90000.00, 0, 0
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'DEMO - Pantalla LED 15.6 pulgadas')
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'DEMO - Pasta termica', 7500.00, 20, 0
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'DEMO - Pasta termica')
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'DEMO - Fuente ATX 500W', 55000.00, 1, 0
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'DEMO - Fuente ATX 500W')
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'DEMO - Cable SATA', 3000.00, 15, 0
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'DEMO - Cable SATA')
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

-- Todos los equipos pertenecen al cliente de demostracion.

INSERT INTO OrdenReparacion
  (nombreCliente, telefonoCliente, emailCliente, modeloEquipo, descripcionFalla, accesorios,
   codigoSeguimiento, estado, nivel_reparacion, fechaIngreso, fechaAsignacion, fechaEntrega,
   notaTecnica, tecnico_id, montoTotal, subtotalPresupuestado, manoDeObraPresupuestada, version)
SELECT 'Cliente Demo', '11 5555-0100', 'cliente@bitfix.com', 'Lenovo IdeaPad 3', '[DEMO] Limpieza preventiva y optimizacion', 'Accesorios de demostracion',
  910001, 'RECIBIDO', 'NIVEL_1', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, NULL,
  'Equipo recibido, pendiente de diagnostico.', tecnico.id, NULL, NULL, NULL, 0
FROM Usuario tecnico
WHERE tecnico.email = 'tecnico.nivel1@bitfix.com' AND tecnico.rol = 'TECNICO' AND tecnico.activo = TRUE
  AND tecnico.nivel_soporte = 'NIVEL_1'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion WHERE codigoSeguimiento = 910001)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO OrdenReparacion
  (nombreCliente, telefonoCliente, emailCliente, modeloEquipo, descripcionFalla, accesorios,
   codigoSeguimiento, estado, nivel_reparacion, fechaIngreso, fechaAsignacion, fechaEntrega,
   notaTecnica, tecnico_id, montoTotal, subtotalPresupuestado, manoDeObraPresupuestada, version)
SELECT 'Cliente Demo', '11 5555-0100', 'cliente@bitfix.com', 'Dell Inspiron 15', '[DEMO] Se apaga despues de varios minutos', 'Accesorios de demostracion',
  910002, 'EN_DIAGNOSTICO', 'NIVEL_2', CURRENT_TIMESTAMP - INTERVAL '5' DAY, CURRENT_TIMESTAMP - INTERVAL '5' DAY, NULL,
  'Revisando temperaturas y alimentacion.', tecnico.id, NULL, NULL, NULL, 0
FROM Usuario tecnico
WHERE tecnico.email = 'tecnico.nivel2@bitfix.com' AND tecnico.rol = 'TECNICO' AND tecnico.activo = TRUE
  AND tecnico.nivel_soporte = 'NIVEL_2'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion WHERE codigoSeguimiento = 910002)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO OrdenReparacion
  (nombreCliente, telefonoCliente, emailCliente, modeloEquipo, descripcionFalla, accesorios,
   codigoSeguimiento, estado, nivel_reparacion, fechaIngreso, fechaAsignacion, fechaEntrega,
   notaTecnica, tecnico_id, montoTotal, subtotalPresupuestado, manoDeObraPresupuestada, version)
SELECT 'Cliente Demo', '11 5555-0100', 'cliente@bitfix.com', 'ASUS VivoBook 15', '[DEMO] Pantalla rota', 'Accesorios de demostracion',
  910003, 'ESPERANDO_REPUESTO', 'NIVEL_3', CURRENT_TIMESTAMP - INTERVAL '10' DAY, CURRENT_TIMESTAMP - INTERVAL '10' DAY, NULL,
  'Pendiente de reposicion de pantalla de 15.6 pulgadas.', tecnico.id, NULL, NULL, NULL, 0
FROM Usuario tecnico
WHERE tecnico.email = 'tecnico.nivel3@bitfix.com' AND tecnico.rol = 'TECNICO' AND tecnico.activo = TRUE
  AND tecnico.nivel_soporte = 'NIVEL_3'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion WHERE codigoSeguimiento = 910003)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO OrdenReparacion
  (nombreCliente, telefonoCliente, emailCliente, modeloEquipo, descripcionFalla, accesorios,
   codigoSeguimiento, estado, nivel_reparacion, fechaIngreso, fechaAsignacion, fechaEntrega,
   notaTecnica, tecnico_id, montoTotal, subtotalPresupuestado, manoDeObraPresupuestada, version)
SELECT 'Cliente Demo', '11 5555-0100', 'cliente@bitfix.com', 'HP Pavilion 14', '[DEMO] Ampliacion de memoria RAM', 'Accesorios de demostracion',
  910004, 'PRESUPUESTO_ENVIADO', 'NIVEL_2', CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY, NULL,
  'Presupuesto enviado: una RAM reservada.', tecnico.id, 48000.00, 28000.00, 20000.00, 0
FROM Usuario tecnico
WHERE tecnico.email = 'tecnico.nivel2@bitfix.com' AND tecnico.rol = 'TECNICO' AND tecnico.activo = TRUE
  AND tecnico.nivel_soporte = 'NIVEL_2'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion WHERE codigoSeguimiento = 910004)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO OrdenReparacion
  (nombreCliente, telefonoCliente, emailCliente, modeloEquipo, descripcionFalla, accesorios,
   codigoSeguimiento, estado, nivel_reparacion, fechaIngreso, fechaAsignacion, fechaEntrega,
   notaTecnica, tecnico_id, montoTotal, subtotalPresupuestado, manoDeObraPresupuestada, version)
SELECT 'Cliente Demo', '11 5555-0100', 'cliente@bitfix.com', 'Acer Aspire 5', '[DEMO] Disco mecanico con sectores defectuosos', 'Accesorios de demostracion',
  910005, 'EN_REPARACION', 'NIVEL_3', CURRENT_TIMESTAMP - INTERVAL '6' DAY, CURRENT_TIMESTAMP - INTERVAL '6' DAY, NULL,
  'Presupuesto aceptado; reemplazando disco por SSD.', tecnico.id, 68000.00, 48000.00, 20000.00, 0
FROM Usuario tecnico
WHERE tecnico.email = 'tecnico.nivel3@bitfix.com' AND tecnico.rol = 'TECNICO' AND tecnico.activo = TRUE
  AND tecnico.nivel_soporte = 'NIVEL_3'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion WHERE codigoSeguimiento = 910005)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO OrdenReparacion
  (nombreCliente, telefonoCliente, emailCliente, modeloEquipo, descripcionFalla, accesorios,
   codigoSeguimiento, estado, nivel_reparacion, fechaIngreso, fechaAsignacion, fechaEntrega,
   notaTecnica, tecnico_id, montoTotal, subtotalPresupuestado, manoDeObraPresupuestada, version)
SELECT 'Cliente Demo', '11 5555-0100', 'cliente@bitfix.com', 'PC de escritorio - limpieza', '[DEMO] Temperatura elevada del procesador', 'Accesorios de demostracion',
  910006, 'REPARADO', 'NIVEL_1', CURRENT_TIMESTAMP - INTERVAL '4' DAY, CURRENT_TIMESTAMP - INTERVAL '4' DAY, NULL,
  'Limpieza y cambio de pasta termica completados.', tecnico.id, 27500.00, 7500.00, 20000.00, 0
FROM Usuario tecnico
WHERE tecnico.email = 'tecnico.nivel1@bitfix.com' AND tecnico.rol = 'TECNICO' AND tecnico.activo = TRUE
  AND tecnico.nivel_soporte = 'NIVEL_1'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion WHERE codigoSeguimiento = 910006)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO OrdenReparacion
  (nombreCliente, telefonoCliente, emailCliente, modeloEquipo, descripcionFalla, accesorios,
   codigoSeguimiento, estado, nivel_reparacion, fechaIngreso, fechaAsignacion, fechaEntrega,
   notaTecnica, tecnico_id, montoTotal, subtotalPresupuestado, manoDeObraPresupuestada, version)
SELECT 'Cliente Demo', '11 5555-0100', 'cliente@bitfix.com', 'PC de escritorio - fuente', '[DEMO] No enciende por falla de fuente', 'Accesorios de demostracion',
  910007, 'ENTREGADO', 'NIVEL_2', CURRENT_TIMESTAMP - INTERVAL '12' DAY, CURRENT_TIMESTAMP - INTERVAL '12' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY,
  'Fuente reemplazada y equipo entregado.', tecnico.id, 74250.00, 55000.00, 19250.00, 0
FROM Usuario tecnico
WHERE tecnico.email = 'tecnico.nivel2@bitfix.com' AND tecnico.rol = 'TECNICO' AND tecnico.activo = TRUE
  AND tecnico.nivel_soporte = 'NIVEL_2'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion WHERE codigoSeguimiento = 910007)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO OrdenReparacion
  (nombreCliente, telefonoCliente, emailCliente, modeloEquipo, descripcionFalla, accesorios,
   codigoSeguimiento, estado, nivel_reparacion, fechaIngreso, fechaAsignacion, fechaEntrega,
   notaTecnica, tecnico_id, montoTotal, subtotalPresupuestado, manoDeObraPresupuestada, version)
SELECT 'Cliente Demo', '11 5555-0100', 'cliente@bitfix.com', 'MacBook Air', '[DEMO] Solicitud online por falla de encendido', 'Accesorios de demostracion',
  910008, 'SOLICITUD_COTIZACION', 'NIVEL_3', CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY, NULL,
  'Solicitud online pendiente de cotizacion previa.', tecnico.id, NULL, NULL, NULL, 0
FROM Usuario tecnico
WHERE tecnico.email = 'tecnico.nivel3@bitfix.com' AND tecnico.rol = 'TECNICO' AND tecnico.activo = TRUE
  AND tecnico.nivel_soporte = 'NIVEL_3'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion WHERE codigoSeguimiento = 910008)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO OrdenReparacion
  (nombreCliente, telefonoCliente, emailCliente, modeloEquipo, descripcionFalla, accesorios,
   codigoSeguimiento, estado, nivel_reparacion, fechaIngreso, fechaAsignacion, fechaEntrega,
   notaTecnica, tecnico_id, montoTotal, subtotalPresupuestado, manoDeObraPresupuestada, version)
SELECT 'Cliente Demo', '11 5555-0100', 'cliente@bitfix.com', 'Toshiba Satellite', '[DEMO] Consulta online por mantenimiento', 'Accesorios de demostracion',
  910009, 'COTIZACION_PREVIA_ENVIADA', 'NIVEL_1', CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY, NULL,
  'Cotizacion previa enviada, pendiente de respuesta del cliente.', tecnico.id, NULL, NULL, NULL, 0
FROM Usuario tecnico
WHERE tecnico.email = 'tecnico.nivel1@bitfix.com' AND tecnico.rol = 'TECNICO' AND tecnico.activo = TRUE
  AND tecnico.nivel_soporte = 'NIVEL_1'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion WHERE codigoSeguimiento = 910009)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO OrdenReparacion
  (nombreCliente, telefonoCliente, emailCliente, modeloEquipo, descripcionFalla, accesorios,
   codigoSeguimiento, estado, nivel_reparacion, fechaIngreso, fechaAsignacion, fechaEntrega,
   notaTecnica, tecnico_id, montoTotal, subtotalPresupuestado, manoDeObraPresupuestada, version)
SELECT 'Cliente Demo', '11 5555-0100', 'cliente@bitfix.com', 'Samsung Book', '[DEMO] Diagnostico de falla intermitente', 'Accesorios de demostracion',
  910010, 'PRESUPUESTO_RECHAZADO', 'NIVEL_2', CURRENT_TIMESTAMP - INTERVAL '7' DAY, CURRENT_TIMESTAMP - INTERVAL '7' DAY, NULL,
  'Presupuesto de diagnostico rechazado, sin repuestos reservados.', tecnico.id, 22000.00, 0.00, 22000.00, 0
FROM Usuario tecnico
WHERE tecnico.email = 'tecnico.nivel2@bitfix.com' AND tecnico.rol = 'TECNICO' AND tecnico.activo = TRUE
  AND tecnico.nivel_soporte = 'NIVEL_2'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion WHERE codigoSeguimiento = 910010)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO reserva_presupuesto (orden_id, repuesto_id, cantidad, nombreRepuesto, precioUnitario)
SELECT orden.idOrdenReparacion, repuesto.id, 1, repuesto.nombre, repuesto.precio
FROM OrdenReparacion orden CROSS JOIN Repuesto repuesto
WHERE orden.codigoSeguimiento = 910004 AND orden.emailCliente = 'cliente@bitfix.com'
  AND orden.descripcionFalla LIKE '[DEMO]%'
  AND repuesto.nombre = 'DEMO - Memoria RAM DDR4 8GB'
  AND NOT EXISTS (SELECT 1 FROM reserva_presupuesto reserva
    WHERE reserva.orden_id = orden.idOrdenReparacion AND reserva.repuesto_id = repuesto.id)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO reserva_presupuesto (orden_id, repuesto_id, cantidad, nombreRepuesto, precioUnitario)
SELECT orden.idOrdenReparacion, repuesto.id, 1, repuesto.nombre, repuesto.precio
FROM OrdenReparacion orden CROSS JOIN Repuesto repuesto
WHERE orden.codigoSeguimiento = 910005 AND orden.emailCliente = 'cliente@bitfix.com'
  AND orden.descripcionFalla LIKE '[DEMO]%'
  AND repuesto.nombre = 'DEMO - SSD SATA 480GB'
  AND NOT EXISTS (SELECT 1 FROM reserva_presupuesto reserva
    WHERE reserva.orden_id = orden.idOrdenReparacion AND reserva.repuesto_id = repuesto.id)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO reserva_presupuesto (orden_id, repuesto_id, cantidad, nombreRepuesto, precioUnitario)
SELECT orden.idOrdenReparacion, repuesto.id, 1, repuesto.nombre, repuesto.precio
FROM OrdenReparacion orden CROSS JOIN Repuesto repuesto
WHERE orden.codigoSeguimiento = 910006 AND orden.emailCliente = 'cliente@bitfix.com'
  AND orden.descripcionFalla LIKE '[DEMO]%'
  AND repuesto.nombre = 'DEMO - Pasta termica'
  AND NOT EXISTS (SELECT 1 FROM reserva_presupuesto reserva
    WHERE reserva.orden_id = orden.idOrdenReparacion AND reserva.repuesto_id = repuesto.id)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO reserva_presupuesto (orden_id, repuesto_id, cantidad, nombreRepuesto, precioUnitario)
SELECT orden.idOrdenReparacion, repuesto.id, 1, repuesto.nombre, repuesto.precio
FROM OrdenReparacion orden CROSS JOIN Repuesto repuesto
WHERE orden.codigoSeguimiento = 910007 AND orden.emailCliente = 'cliente@bitfix.com'
  AND orden.descripcionFalla LIKE '[DEMO]%'
  AND repuesto.nombre = 'DEMO - Fuente ATX 500W'
  AND NOT EXISTS (SELECT 1 FROM reserva_presupuesto reserva
    WHERE reserva.orden_id = orden.idOrdenReparacion AND reserva.repuesto_id = repuesto.id)
  AND NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

INSERT INTO carga_demo (id, fechaCarga)
SELECT 1, CURRENT_TIMESTAMP FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM carga_demo WHERE id = 1);

COMMIT;
