CREATE TABLE categoria (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE producto (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    precio DOUBLE PRECISION,
    categoria_id INTEGER,
    CONSTRAINT fk_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categoria(id)
);
CREATE TABLE compra (
  id SERIAL PRIMARY KEY,
  fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE detalle_compra (
  id SERIAL PRIMARY KEY,
  compra_id INT,
  producto_id INT,
  FOREIGN KEY (compra_id) REFERENCES compra(id),
  FOREIGN KEY (producto_id) REFERENCES producto(id)
);