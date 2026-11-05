INSERT INTO Usuario (email, password, rol, activo) VALUES ('admin@bitfix.com', 'admin123', 'ADMIN', true);

-- ALMACENAMIENTO (Alta rotación - Tier 1 y 2)
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (1, 'Disco Sólido SSD Kingston A400 480GB SATA', 35000.00, 30);
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (2, 'Disco Sólido SSD Kingston A400 960GB SATA', 65000.00, 15);
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (3, 'Disco M.2 NVMe WD Blue SN570 1TB', 85000.00, 20);

-- MEMORIA RAM (Alta rotación - Tier 1 y 2)
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (4, 'Memoria RAM SODIMM DDR4 8GB 3200MHz Kingston', 24000.00, 40);
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (5, 'Memoria RAM SODIMM DDR4 16GB 3200MHz Crucial', 42000.00, 25);
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (6, 'Memoria RAM DIMM DDR4 16GB 3200MHz Fury Beast', 45000.00, 20);

-- PANTALLAS Y BATERÍAS (Mayor costo, menor rotación - Tier 2)
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (7, 'Pantalla Notebook LED 15.6" Slim 30 Pines HD', 95000.00, 10);
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (8, 'Pantalla Notebook LED 14.0" Slim 30 Pines HD', 88000.00, 8);
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (9, 'Batería Original HP JC04 4 Celdas', 48000.00, 12);
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (10, 'Batería Alternativa Dell Inspiron 15', 35000.00, 15);

-- REFRIGERACIÓN Y MANTENIMIENTO (Consumibles - Tier 1)
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (11, 'Pasta Térmica Arctic MX-4 Jeringa 4g', 12000.00, 50);
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (12, 'Pasta Térmica Thermal Grizzly Kryonaut 1g', 18000.00, 30);
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (13, 'Cooler Fan Notebook Universal 5V', 15000.00, 20);

-- MICROELECTRÓNICA Y COMPONENTES (Bajo costo, alta cantidad - Tier 3)
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (14, 'Pin de Carga DC Jack Lenovo IdeaPad', 6000.00, 45);
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (15, 'Pin de Carga DC Jack Asus VivoBook', 6000.00, 35);
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (16, 'Chip KBC I/O ENE KB9022Q D', 9000.00, 15);

-- PERIFÉRICOS Y VARIOS (Tier 2)
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (17, 'Teclado Notebook Lenovo ThinkPad T480 Español', 32000.00, 5);
INSERT INTO repuesto (id, nombre, precio, stock) VALUES (18, 'Cargador Universal Notebook 65W Multi-Pin', 22000.00, 25);