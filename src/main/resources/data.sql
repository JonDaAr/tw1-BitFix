INSERT INTO Usuario (email, password, rol, activo, nivel_soporte)
VALUES ('tecnico@bitfix.com', '{pbkdf2}1b4ce42c7c1ec662f901a69f2cdac74f219911e6cbaf0e1410df1a8f444870adc5d46f9f63682fba41a42a73b7ca933b', 'TECNICO', true, 'NIVEL_3')
    ON DUPLICATE KEY UPDATE rol = 'TECNICO', activo = true, nivel_soporte = 'NIVEL_3';

INSERT INTO Usuario (email, password, rol, activo, nivel_soporte)
VALUES ('martin@bitfix.com', '{pbkdf2}e00d99875155edad9624af6f05bde429a24e3a1f1a33888f0bbc5ef628ccfd078d9d82cba327c2404ed67fdcb61dfd62', 'TECNICO', true, 'NIVEL_2')
    ON DUPLICATE KEY UPDATE rol = 'TECNICO', activo = true, nivel_soporte = 'NIVEL_2';

INSERT INTO Usuario (email, password, rol, activo) VALUES ('admin@bitfix.com', '{pbkdf2}b7929865f1d22d682bc51a05b67132b6fd4db50b3d2add64637954868c7aa09df441d75fa7a321ff6f0d80f9a061bc92', 'ADMIN', true);
