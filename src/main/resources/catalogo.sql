-- Catalogo incorporado desde origin/Merge. Inserciones sin IDs fijos.

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Disco Sólido SSD Kingston A400 480GB SATA', 35000.00, 30, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Disco Sólido SSD Kingston A400 480GB SATA');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Disco Sólido SSD Kingston A400 960GB SATA', 65000.00, 15, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Disco Sólido SSD Kingston A400 960GB SATA');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Disco M.2 NVMe WD Blue SN570 1TB', 85000.00, 20, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Disco M.2 NVMe WD Blue SN570 1TB');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Memoria RAM SODIMM DDR4 8GB 3200MHz Kingston', 24000.00, 40, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Memoria RAM SODIMM DDR4 8GB 3200MHz Kingston');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Memoria RAM SODIMM DDR4 16GB 3200MHz Crucial', 42000.00, 25, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Memoria RAM SODIMM DDR4 16GB 3200MHz Crucial');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Memoria RAM DIMM DDR4 16GB 3200MHz Fury Beast', 45000.00, 20, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Memoria RAM DIMM DDR4 16GB 3200MHz Fury Beast');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Pantalla Notebook LED 15.6" Slim 30 Pines HD', 95000.00, 10, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Pantalla Notebook LED 15.6" Slim 30 Pines HD');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Pantalla Notebook LED 14.0" Slim 30 Pines HD', 88000.00, 8, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Pantalla Notebook LED 14.0" Slim 30 Pines HD');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Batería Original HP JC04 4 Celdas', 48000.00, 12, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Batería Original HP JC04 4 Celdas');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Batería Alternativa Dell Inspiron 15', 35000.00, 15, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Batería Alternativa Dell Inspiron 15');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Pasta Térmica Arctic MX-4 Jeringa 4g', 12000.00, 50, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Pasta Térmica Arctic MX-4 Jeringa 4g');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Pasta Térmica Thermal Grizzly Kryonaut 1g', 18000.00, 30, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Pasta Térmica Thermal Grizzly Kryonaut 1g');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Cooler Fan Notebook Universal 5V', 15000.00, 20, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Cooler Fan Notebook Universal 5V');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Pin de Carga DC Jack Lenovo IdeaPad', 6000.00, 45, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Pin de Carga DC Jack Lenovo IdeaPad');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Pin de Carga DC Jack Asus VivoBook', 6000.00, 35, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Pin de Carga DC Jack Asus VivoBook');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Chip KBC I/O ENE KB9022Q D', 9000.00, 15, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Chip KBC I/O ENE KB9022Q D');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Teclado Notebook Lenovo ThinkPad T480 Español', 32000.00, 5, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Teclado Notebook Lenovo ThinkPad T480 Español');

INSERT INTO Repuesto (nombre, precio, stock, version)
SELECT 'Cargador Universal Notebook 65W Multi-Pin', 22000.00, 25, 0 FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Repuesto WHERE nombre = 'Cargador Universal Notebook 65W Multi-Pin');
