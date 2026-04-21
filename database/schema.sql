-- GlowLab — Esquema de Base de Datos PostgreSQL 15
-- Ejecutar en orden: primero schema.sql, luego seed.sql

CREATE TABLE IF NOT EXISTS categoria (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(100)  NOT NULL,
    descripcion VARCHAR(255),
    icono       VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS producto (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(255)  NOT NULL,
    marca       VARCHAR(100),
    descripcion TEXT,
    precio      DOUBLE PRECISION,
    tipos_piel  TEXT,
    categoria_id INTEGER,
    CONSTRAINT fk_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categoria(id)
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS compra (
    id    SERIAL PRIMARY KEY,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS detalle_compra (
    id          SERIAL PRIMARY KEY,
    compra_id   INT,
    producto_id INT,
    cantidad    INT DEFAULT 1,
    FOREIGN KEY (compra_id)   REFERENCES compra(id)   ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE SET NULL
);
