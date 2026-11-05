-- Cuentas de demostracion con correos normalizados por rol y nivel.
INSERT INTO Usuario (email, password, rol, activo, nivel_soporte)
SELECT 'tecnico.nivel3@bitfix.com', '{pbkdf2}1b4ce42c7c1ec662f901a69f2cdac74f219911e6cbaf0e1410df1a8f444870adc5d46f9f63682fba41a42a73b7ca933b', 'TECNICO', true, 'NIVEL_3'
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE email = 'tecnico.nivel3@bitfix.com');

INSERT INTO Usuario (email, password, rol, activo, nivel_soporte)
SELECT 'tecnico.nivel2@bitfix.com', '{pbkdf2}e00d99875155edad9624af6f05bde429a24e3a1f1a33888f0bbc5ef628ccfd078d9d82cba327c2404ed67fdcb61dfd62', 'TECNICO', true, 'NIVEL_2'
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE email = 'tecnico.nivel2@bitfix.com');

INSERT INTO Usuario (email, password, rol, activo, nivel_soporte)
SELECT 'tecnico.nivel1@bitfix.com', '{pbkdf2}ceae92803e554f98c12f781654be038aa6c28ad4dfcc9d0c5dd131fbc0b4a052504a429763bc659ffcfb46196f273beb', 'TECNICO', true, 'NIVEL_1'
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE email = 'tecnico.nivel1@bitfix.com');

INSERT INTO Usuario (email, password, rol, activo)
SELECT 'cliente@bitfix.com', '{pbkdf2}a1f14790c885ed381abe9c74f1e8560efc9a40a53be420c9d92fcc63db5bc6f07ebcaa811e2a54647adfaaad4c591898', 'CLIENTE', true
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE email = 'cliente@bitfix.com');

INSERT INTO Usuario (email, password, rol, activo)
SELECT 'admin@bitfix.com', '{pbkdf2}b7929865f1d22d682bc51a05b67132b6fd4db50b3d2add64637954868c7aa09df441d75fa7a321ff6f0d80f9a061bc92', 'ADMIN', true
FROM (SELECT 1 AS semilla) AS origen
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE email = 'admin@bitfix.com');
