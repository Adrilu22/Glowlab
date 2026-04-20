-- Insertar categorías
INSERT INTO categoria (nombre) VALUES ('Limpieza');
INSERT INTO categoria (nombre) VALUES ('Hidratacion');
INSERT INTO categoria (nombre) VALUES ('Proteccion');
INSERT INTO categoria (nombre) VALUES ('Tratamiento');

-- Insertar productos
INSERT INTO producto (nombre, precio, categoria_id) VALUES ('Gel limpiador', 25000, 1);
INSERT INTO producto (nombre, precio, categoria_id) VALUES ('Crema hidratante', 30000, 2);
INSERT INTO producto (nombre, precio, categoria_id) VALUES ('Protector solar SPF 50', 45000, 3);
INSERT INTO producto (nombre, precio, categoria_id) VALUES ('Serum vitamina C', 55000, 4);