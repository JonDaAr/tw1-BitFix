INSERT IGNORE INTO Usuario (email, password, rol, activo) VALUES ('admin@bitfix.com', 'admin123', 'ADMIN', true);

-- ALMACENAMIENTO (Alta rotación - Tier 1 y 2)
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (1, 'Disco Sólido SSD Kingston A400 480GB SATA', 35000.00, 30);
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (2, 'Disco Sólido SSD Kingston A400 960GB SATA', 65000.00, 15);
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (3, 'Disco M.2 NVMe WD Blue SN570 1TB', 85000.00, 20);

-- MEMORIA RAM (Alta rotación - Tier 1 y 2)
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (4, 'Memoria RAM SODIMM DDR4 8GB 3200MHz Kingston', 24000.00, 40);
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (5, 'Memoria RAM SODIMM DDR4 16GB 3200MHz Crucial', 42000.00, 25);
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (6, 'Memoria RAM DIMM DDR4 16GB 3200MHz Fury Beast', 45000.00, 20);

-- PANTALLAS Y BATERÍAS (Mayor costo, menor rotación - Tier 2)
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (7, 'Pantalla Notebook LED 15.6" Slim 30 Pines HD', 95000.00, 10);
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (8, 'Pantalla Notebook LED 14.0" Slim 30 Pines HD', 88000.00, 8);
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (9, 'Batería Original HP JC04 4 Celdas', 48000.00, 12);
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (10, 'Batería Alternativa Dell Inspiron 15', 35000.00, 15);

-- REFRIGERACIÓN Y MANTENIMIENTO (Consumibles - Tier 1)
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (11, 'Pasta Térmica Arctic MX-4 Jeringa 4g', 12000.00, 50);
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (12, 'Pasta Térmica Thermal Grizzly Kryonaut 1g', 18000.00, 30);
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (13, 'Cooler Fan Notebook Universal 5V', 15000.00, 20);

-- MICROELECTRÓNICA Y COMPONENTES (Bajo costo, alta cantidad - Tier 3)
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (14, 'Pin de Carga DC Jack Lenovo IdeaPad', 6000.00, 45);
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (15, 'Pin de Carga DC Jack Asus VivoBook', 6000.00, 35);
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (16, 'Chip KBC I/O ENE KB9022Q D', 9000.00, 15);

-- PERIFÉRICOS Y VARIOS (Tier 2)
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (17, 'Teclado Notebook Lenovo ThinkPad T480 Español', 32000.00, 5);
INSERT IGNORE INTO Repuesto (id, nombre, precio, stock) VALUES (18, 'Cargador Universal Notebook 65W Multi-Pin', 22000.00, 25);

-- Usuarios
INSERT IGNORE INTO Usuario (email, password, rol, activo, apellido, nivel_soporte, nombre, nombreUsuario, telefono)
VALUES ('admin.test@bitfix.com', 'Admin123!', 'ADMIN', true, 'Prueba', NULL, 'Admin', 'admin_test', '1100000001');

INSERT IGNORE INTO Usuario (email, password, rol, activo, apellido, nivel_soporte, nombre, nombreUsuario, telefono)
VALUES ('tecnico.test@bitfix.com', 'Tecnico123!', 'TECNICO', true, 'Prueba', 'NIVEL_3', 'Tecnico', 'tecnico_test', '1100000002');

INSERT IGNORE INTO Usuario (email, password, rol, activo, apellido, nivel_soporte, nombre, nombreUsuario, telefono)
VALUES ('cliente.test@bitfix.com', 'Cliente123!', 'CLIENTE', true, 'Prueba', NULL, 'Cliente', 'cliente_test', '1100000003');

-- SUCURSALES
INSERT IGNORE INTO Sucursal (id, nombre, direccion, latitud, longitud, activo)
VALUES
    (1,'BitFix Centro', 'Av. Corrientes 1234, CABA', -34.603722, -58.381592, 1),
    (2,'BitFix Palermo', 'Av. Santa Fe 3456, CABA', -34.588889, -58.411111, 1),
    (3,'BitFix Caballito', 'Av. Rivadavia 5200, CABA', -34.618056, -58.437500, 1),
    (4, 'BitFix Galeria Jardin', 'Florida 537, CABA', -34.601561, -58.375189, 1),
    (5, 'BitFix Abasto', 'Av. Corrientes 3247, CABA', -34.602778, -58.410278, 1),
    (6, 'BitFix Belgrano', 'Av. Cabildo 2040, CABA', -34.562778, -58.456111, 1),
    (7, 'BitFix Morón', 'Almirante Brown 850, Morón', -34.649210, -58.621530, 1);

-- Cuentas adicionales para probar los roles de tecnico y cliente.
INSERT IGNORE INTO Usuario (email, password, rol, activo, apellido, nivel_soporte, nombre, nombreUsuario, telefono)
VALUES ('tecnico.nivel1@bitfix.com', 'Tecnico123!', 'TECNICO', true, 'Demo', 'NIVEL_1', 'Tomas', 'tecnico_nivel1', '1100000011');

INSERT IGNORE INTO Usuario (email, password, rol, activo, apellido, nivel_soporte, nombre, nombreUsuario, telefono)
VALUES ('tecnico.nivel2@bitfix.com', 'Tecnico123!', 'TECNICO', true, 'Demo', 'NIVEL_2', 'Lucia', 'tecnico_nivel2', '1100000012');

INSERT IGNORE INTO Usuario (email, password, rol, activo, apellido, nivel_soporte, nombre, nombreUsuario, telefono)
VALUES ('tecnico.nivel3@bitfix.com', 'Tecnico123!', 'TECNICO', true, 'Demo', 'NIVEL_3', 'Mateo', 'tecnico_nivel3', '1100000013');

INSERT IGNORE INTO Usuario (email, password, rol, activo, apellido, nivel_soporte, nombre, nombreUsuario, telefono)
VALUES ('cliente.demo1@bitfix.com', 'Cliente123!', 'CLIENTE', true, 'Demo', NULL, 'Martina', 'cliente_demo1', '1100000021');

INSERT IGNORE INTO Usuario (email, password, rol, activo, apellido, nivel_soporte, nombre, nombreUsuario, telefono)
VALUES ('cliente.demo2@bitfix.com', 'Cliente123!', 'CLIENTE', true, 'Demo', NULL, 'Lucas', 'cliente_demo2', '1100000022');

INSERT IGNORE INTO Usuario (email, password, rol, activo, apellido, nivel_soporte, nombre, nombreUsuario, telefono)
VALUES ('cliente.demo3@bitfix.com', 'Cliente123!', 'CLIENTE', true, 'Demo', NULL, 'Sofia', 'cliente_demo3', '1100000023');

INSERT IGNORE INTO Usuario (email, password, rol, activo, apellido, nivel_soporte, nombre, nombreUsuario, telefono)
VALUES ('cliente.demo4@bitfix.com', 'Cliente123!', 'CLIENTE', true, 'Demo', NULL, 'Nicolas', 'cliente_demo4', '1100000024');

-- Publicaciones de ejemplo para navegar el catalogo de canje.
INSERT INTO ComputadoraUsada (marca, modelo, descripcion, condicion, precio, stock, activa, retirada)
SELECT 'Lenovo', 'ThinkPad E14 Demo', 'Notebook de prueba para solicitudes pendientes.', 'Casi nueva', 650000.00, 3, true, false
WHERE NOT EXISTS (SELECT 1 FROM ComputadoraUsada WHERE marca = 'Lenovo' AND modelo = 'ThinkPad E14 Demo');

INSERT INTO ComputadoraUsada (marca, modelo, descripcion, condicion, precio, stock, activa, retirada)
SELECT 'Dell', 'Latitude 5420 Demo', 'Notebook reacondicionada para probar aprobaciones.', 'Reacondicionada', 580000.00, 2, true, false
WHERE NOT EXISTS (SELECT 1 FROM ComputadoraUsada WHERE marca = 'Dell' AND modelo = 'Latitude 5420 Demo');

INSERT INTO ComputadoraUsada (marca, modelo, descripcion, condicion, precio, stock, activa, retirada)
SELECT 'ASUS', 'VivoBook 15 Demo', 'Equipo de prueba para una contraoferta.', 'Outlet', 495000.00, 2, true, false
WHERE NOT EXISTS (SELECT 1 FROM ComputadoraUsada WHERE marca = 'ASUS' AND modelo = 'VivoBook 15 Demo');

INSERT INTO ComputadoraUsada (marca, modelo, descripcion, condicion, precio, stock, activa, retirada)
SELECT 'HP', 'ProBook 440 G8 Demo', 'Equipo de prueba para solicitudes rechazadas.', 'Usada', 425000.00, 1, true, false
WHERE NOT EXISTS (SELECT 1 FROM ComputadoraUsada WHERE marca = 'HP' AND modelo = 'ProBook 440 G8 Demo');

-- Solicitudes de canje de ejemplo en distintos estados.
INSERT INTO SolicitudCanje (cliente_id, computadora_id, equipoMarca, equipoModelo, equipoDescripcion, valorOfrecido, valorAprobado, comentarioRespuesta, estado, fechaSolicitud)
SELECT u.id, c.id, 'HP', 'Pavilion 14', 'Enciende, pero la pantalla tiene lineas verticales.', 150000.00, NULL, NULL, 'PENDIENTE', CURRENT_TIMESTAMP
FROM Usuario u JOIN ComputadoraUsada c ON c.marca = 'Lenovo' AND c.modelo = 'ThinkPad E14 Demo'
WHERE u.email = 'cliente.test@bitfix.com'
  AND NOT EXISTS (SELECT 1 FROM SolicitudCanje s WHERE s.cliente_id = u.id AND s.computadora_id = c.id AND s.equipoMarca = 'HP' AND s.equipoModelo = 'Pavilion 14');

INSERT INTO SolicitudCanje (cliente_id, computadora_id, equipoMarca, equipoModelo, equipoDescripcion, valorOfrecido, valorAprobado, comentarioRespuesta, estado, fechaSolicitud)
SELECT u.id, c.id, 'Acer', 'Aspire 5', 'Tiene marcas de uso y la bateria dura poco.', 175000.00, NULL, NULL, 'PENDIENTE', CURRENT_TIMESTAMP
FROM Usuario u JOIN ComputadoraUsada c ON c.marca = 'Dell' AND c.modelo = 'Latitude 5420 Demo'
WHERE u.email = 'cliente.demo1@bitfix.com'
  AND NOT EXISTS (SELECT 1 FROM SolicitudCanje s WHERE s.cliente_id = u.id AND s.computadora_id = c.id AND s.equipoMarca = 'Acer' AND s.equipoModelo = 'Aspire 5');

INSERT INTO SolicitudCanje (cliente_id, computadora_id, equipoMarca, equipoModelo, equipoDescripcion, valorOfrecido, valorAprobado, comentarioRespuesta, estado, fechaSolicitud)
SELECT u.id, c.id, 'Acer', 'Aspire 5', 'Buen estado general; incluye cargador.', 200000.00, 230000.00, 'Podemos reconocer $230.000 luego de revisar el equipo en el taller.', 'CONTRAOFERTA', CURRENT_TIMESTAMP
FROM Usuario u JOIN ComputadoraUsada c ON c.marca = 'ASUS' AND c.modelo = 'VivoBook 15 Demo'
WHERE u.email = 'cliente.demo2@bitfix.com'
  AND NOT EXISTS (SELECT 1 FROM SolicitudCanje s WHERE s.cliente_id = u.id AND s.computadora_id = c.id AND s.equipoMarca = 'Acer' AND s.equipoModelo = 'Aspire 5');

INSERT INTO SolicitudCanje (cliente_id, computadora_id, equipoMarca, equipoModelo, equipoDescripcion, valorOfrecido, valorAprobado, comentarioRespuesta, estado, fechaSolicitud)
SELECT u.id, c.id, 'Lenovo', 'IdeaPad 3', 'Funciona correctamente; tiene una marca en la carcasa.', 180000.00, 190000.00, 'Canje aprobado para probar el resumen y la diferencia.', 'APROBADA', CURRENT_TIMESTAMP
FROM Usuario u JOIN ComputadoraUsada c ON c.marca = 'Dell' AND c.modelo = 'Latitude 5420 Demo'
WHERE u.email = 'cliente.demo3@bitfix.com'
  AND NOT EXISTS (SELECT 1 FROM SolicitudCanje s WHERE s.cliente_id = u.id AND s.computadora_id = c.id AND s.equipoMarca = 'Lenovo' AND s.equipoModelo = 'IdeaPad 3');

INSERT INTO SolicitudCanje (cliente_id, computadora_id, equipoMarca, equipoModelo, equipoDescripcion, valorOfrecido, valorAprobado, comentarioRespuesta, estado, fechaSolicitud)
SELECT u.id, c.id, 'Samsung', 'Book E30', 'No carga y falta una tecla.', 80000.00, NULL, 'No podemos tomar este equipo en las condiciones declaradas.', 'RECHAZADA', CURRENT_TIMESTAMP
FROM Usuario u JOIN ComputadoraUsada c ON c.marca = 'HP' AND c.modelo = 'ProBook 440 G8 Demo'
WHERE u.email = 'cliente.demo4@bitfix.com'
  AND NOT EXISTS (SELECT 1 FROM SolicitudCanje s WHERE s.cliente_id = u.id AND s.computadora_id = c.id AND s.equipoMarca = 'Samsung' AND s.equipoModelo = 'Book E30');

-- Ordenes asignadas para que los tecnicos de prueba tengan trabajo en su panel.
INSERT INTO OrdenReparacion (codigoSeguimiento, descripcionFalla, emailCliente, estado, fechaAsignacion, fechaIngreso, modeloEquipo, nivel_reparacion, nombreCliente, notaTecnica, telefonoCliente, tecnico_id)
SELECT 981501, 'Se apaga al mover el cable de carga.', 'cliente.demo1@bitfix.com', 'EN_DIAGNOSTICO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'HP Pavilion 14', 'NIVEL_1', 'Martina Demo', '', '1100000021', t.id
FROM Usuario t WHERE t.email = 'tecnico.nivel1@bitfix.com'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion o WHERE o.codigoSeguimiento = 981501);

INSERT INTO OrdenReparacion (codigoSeguimiento, descripcionFalla, emailCliente, estado, fechaAsignacion, fechaIngreso, modeloEquipo, nivel_reparacion, nombreCliente, notaTecnica, telefonoCliente, tecnico_id)
SELECT 981502, 'La bateria no retiene carga.', 'cliente.demo2@bitfix.com', 'ESPERANDO_REPUESTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Acer Aspire 5', 'NIVEL_2', 'Lucas Demo', 'Pendiente de bateria compatible.', '1100000022', t.id
FROM Usuario t WHERE t.email = 'tecnico.nivel2@bitfix.com'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion o WHERE o.codigoSeguimiento = 981502);

INSERT INTO OrdenReparacion (codigoSeguimiento, descripcionFalla, emailCliente, estado, fechaAsignacion, fechaIngreso, modeloEquipo, nivel_reparacion, nombreCliente, notaTecnica, telefonoCliente, tecnico_id)
SELECT 981503, 'Requiere reemplazo de teclado.', 'cliente.demo3@bitfix.com', 'EN_REPARACION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Lenovo IdeaPad 3', 'NIVEL_3', 'Sofia Demo', 'Repuesto recibido; reparacion iniciada.', '1100000023', t.id
FROM Usuario t WHERE t.email = 'tecnico.nivel3@bitfix.com'
  AND NOT EXISTS (SELECT 1 FROM OrdenReparacion o WHERE o.codigoSeguimiento = 981503);

-- Orden sin asignar para probar la bandeja de triaje del tecnico senior.
INSERT INTO OrdenReparacion (codigoSeguimiento, descripcionFalla, emailCliente, estado, fechaIngreso, modeloEquipo, nivel_reparacion, nombreCliente, notaTecnica, telefonoCliente)
SELECT 981504, 'No enciende despues de una descarga.', 'cliente.demo4@bitfix.com', 'PENDIENTE_TRIAJE', CURRENT_TIMESTAMP, 'Samsung Book E30', 'NIVEL_3', 'Nicolas Demo', '', '1100000024'
WHERE NOT EXISTS (SELECT 1 FROM OrdenReparacion WHERE codigoSeguimiento = 981504);
