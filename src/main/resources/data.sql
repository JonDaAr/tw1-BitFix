-- ============================================================================
-- 1. USUARIOS (Clave para todos: 1234)
-- ============================================================================
INSERT IGNORE INTO Usuario (id, email, password, rol, activo, nombre, apellido, telefono, nombreUsuario, nivel_soporte) VALUES
(1, 'admin@bitfix.com', '1234', 'ADMIN', true, 'Laura', 'Gómez', '1144556677', 'admin', NULL),
(2, 'tec1@bitfix.com', '1234', 'TECNICO', true, 'Lucas', 'Martínez', '1155667788', 'lmartinez', 'NIVEL_1'),
(3, 'tec2@bitfix.com', '1234', 'TECNICO', true, 'Sofía', 'Ramírez', '1166778899', 'sramirez', 'NIVEL_2'),
(4, 'tec3@bitfix.com', '1234', 'TECNICO', true, 'Martín', 'Silva', '1177889900', 'msilva', 'NIVEL_3'),
(5, 'cliente@bitfix.com', '1234', 'CLIENTE', true, 'Carlos', 'Pérez', '1133445566', 'cperez', NULL),
(6, 'maria@gmail.com', '1234', 'CLIENTE', true, 'María', 'López', '1122334455', 'mlopez', NULL);

-- ============================================================================
-- 2. SUCURSALES (Para mapa, cercanía y cálculo de logística hasta 15 km)
-- ============================================================================
INSERT IGNORE INTO Sucursal (id, nombre, direccion, latitud, longitud, activo) VALUES
(1, 'BitFix Morón Centro', 'Av. Rivadavia 18200, Morón', -34.6534, -58.6198, true),
(2, 'BitFix Ramos Mejía', 'Av. de Mayo 450, Ramos Mejía', -34.6408, -58.5647, true),
(3, 'BitFix Caballito', 'Av. Rivadavia 4950, CABA', -34.6186, -58.4426, true);

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

-- ============================================================================
-- 4. MÁQUINAS USADAS (Catálogo para Plan Canje)
-- ============================================================================
INSERT IGNORE INTO ComputadoraUsada (id, marca, modelo, descripcion, condicion, precio, stock, activa, retirada) VALUES
(1, 'Lenovo', 'ThinkPad T14 Gen 2', 'Intel i5 11va, 16GB RAM, SSD 512GB, batería al 90%.', 'Reacondicionada', 650000.00, 2, true, false),
(2, 'Dell', 'Latitude 5420', 'Intel i7, 16GB RAM, SSD 256GB, teclado retroiluminado.', 'Casi nueva', 720000.00, 1, true, false),
(3, 'HP', 'ProBook 450 G8', 'Intel i5, 8GB RAM, SSD 256GB, detalles estéticos mínimos.', 'Usada', 480000.00, 3, true, false);

-- ============================================================================
-- 5. ÓRDENES PRECARGADAS (Cubren Semáforo Rojo/Amarillo/Gris, Facturas y Logística)
-- ============================================================================
-- Orden 1: PRIORIDAD ALTA (Roja - hace 10 días), en diagnóstico con Técnico 3 (lista para presupuestar)
INSERT IGNORE INTO OrdenReparacion (
    idOrdenReparacion, codigoSeguimiento, nombreCliente, emailCliente, telefonoCliente,
    modeloEquipo, descripcionFalla, accesorios, estado, nivel_reparacion, tecnico_id,
    fechaIngreso, fechaAsignacion, solicita_retiro, costo_logistica, notaTecnica
) VALUES (
    1, 100101, 'Carlos Pérez', 'cliente@bitfix.com', '1133445566',
    'Notebook Asus VivoBook 15', 'Pantalla con líneas verticales y parpadeo', 'Cargador original',
    'EN_DIAGNOSTICO', 'NIVEL_3', 4,
    DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), false, 0.0,
    'Se detecta daño en el panel LED. Requiere reemplazo de pantalla.'
);

-- Orden 2: PRIORIDAD MEDIA (Amarilla - hace 5 días), en reparación con Técnico 2 (sirve para Contingencia)
INSERT IGNORE INTO OrdenReparacion (
    idOrdenReparacion, codigoSeguimiento, nombreCliente, emailCliente, telefonoCliente,
    modeloEquipo, descripcionFalla, accesorios, estado, nivel_reparacion, tecnico_id,
    fechaIngreso, fechaAsignacion, montoTotal, solicita_retiro, costo_logistica, notaTecnica
) VALUES (
    2, 100102, 'María López', 'maria@gmail.com', '1122334455',
    'PC Escritorio Gamer', 'Sobrecalentamiento y apagado repentino', 'Solo gabinete',
    'EN_REPARACION', 'NIVEL_2', 3,
    DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 57000.00, false, 0.0,
    'Limpieza general y cambio de pasta térmica en curso.'
);

-- Orden 3: ENTREGADA (Gris - hace 12 días), para que Carlos ya tenga una Factura/Comprobante lista
INSERT IGNORE INTO OrdenReparacion (
    idOrdenReparacion, codigoSeguimiento, nombreCliente, emailCliente, telefonoCliente,
    modeloEquipo, descripcionFalla, accesorios, estado, nivel_reparacion, tecnico_id,
    fechaIngreso, fechaAsignacion, fechaEntrega, montoTotal, solicita_retiro, costo_logistica, notaTecnica
) VALUES (
    3, 100103, 'Carlos Pérez', 'cliente@bitfix.com', '1133445566',
    'Notebook Lenovo IdeaPad 3', 'Muy lenta al iniciar Windows', 'Cargador y funda',
    'ENTREGADO', 'NIVEL_1', 2,
    DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY),
    65000.00, false, 0.0, 'Instalación de SSD NVMe 500GB y clonación de sistema operativo.'
);

-- Orden 4: REPARADA con retiro a domicilio (lista en Admin -> Logística para mostrar Entrega)
INSERT IGNORE INTO OrdenReparacion (
    idOrdenReparacion, codigoSeguimiento, nombreCliente, emailCliente, telefonoCliente,
    modeloEquipo, descripcionFalla, accesorios, estado, nivel_reparacion, tecnico_id,
    fechaIngreso, fechaAsignacion, montoTotal, solicita_retiro, direccion_retiro,
    latitud_retiro, longitud_retiro, costo_logistica, notaTecnica
) VALUES (
    4, 100104, 'María López', 'maria@gmail.com', '1122334455',
    'Notebook HP Pavilion', 'No carga la batería', 'Cargador original',
    'REPARADO', 'NIVEL_2', 3,
    DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 79000.00,
    true, 'Av. Sarmiento 850, Morón', -34.6510, -58.6220, 3500.00,
    'Reemplazo de batería realizado y testeado al 100%.'
);

-- ============================================================================
-- 6. HISTORIAL INICIAL (Para que la línea de tiempo del modal se vea poblada)
-- ============================================================================
INSERT IGNORE INTO HistorialOrden (historialOrdenId, orden_id, estado, fechaHora) VALUES
(1, 1, 'RECIBIDO', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 1, 'EN_DIAGNOSTICO', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(3, 2, 'RECIBIDO', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 2, 'EN_REPARACION', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 3, 'RECIBIDO', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(6, 3, 'REPARADO', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(7, 3, 'ENTREGADO', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(8, 4, 'RECIBIDO', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9, 4, 'REPARADO', DATE_SUB(NOW(), INTERVAL 1 DAY));