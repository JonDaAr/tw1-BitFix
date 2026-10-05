INSERT INTO Usuario (email, password, rol, activo)
VALUES ('tecnico@bitfix.com', '1234', 'TECNICO', true)
    ON DUPLICATE KEY UPDATE rol = 'TECNICO', activo = true;

INSERT INTO Usuario (email, password, rol, activo)
VALUES ('martin@bitfix.com', '1234', 'TECNICO', true)
    ON DUPLICATE KEY UPDATE rol = 'TECNICO', activo = true;