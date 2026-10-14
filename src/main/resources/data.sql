INSERT INTO Usuario (email, password, rol, activo, nivel_soporte)
VALUES ('tecnico@bitfix.com', '1234', 'TECNICO', true, 'NIVEL_3')
    ON DUPLICATE KEY UPDATE rol = 'TECNICO', activo = true, nivel_soporte = 'NIVEL_3';

INSERT INTO Usuario (email, password, rol, activo, nivel_soporte)
VALUES ('martin@bitfix.com', '1234', 'TECNICO', true, 'NIVEL_2')
    ON DUPLICATE KEY UPDATE rol = 'TECNICO', activo = true, nivel_soporte = 'NIVEL_2';