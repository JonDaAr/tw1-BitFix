INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);


INSERT INTO OrdenReparacion (idOrdenReparacion, modeloEquipo, estado, notaTecnica)
VALUES (1, 'Notebook Dell Inspiron', 'RECIBIDO', NULL);

INSERT INTO OrdenReparacion (idOrdenReparacion, modeloEquipo, estado, notaTecnica)
VALUES (2, 'PC de Escritorio Gamer', 'EN_DIAGNOSTICO', 'Revisión pendiente de fuente de poder.');
