-- =========================================================
--  BD: relatos_de_papel (MySQL)
--  Script basado en el ERD adjunto
-- =========================================================

DROP DATABASE IF EXISTS relatos_de_papel;
CREATE DATABASE relatos_de_papel
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE relatos_de_papel;

-- ----------------------------
-- TABLA: categoria
-- ----------------------------
CREATE TABLE categoria (
  id_categoria SMALLINT NOT NULL AUTO_INCREMENT,
  nombre       VARCHAR(250) NOT NULL,
  descripcion  TEXT NULL,
  PRIMARY KEY (id_categoria)
) ENGINE=InnoDB;

-- ----------------------------
-- TABLA: libro
-- ----------------------------
CREATE TABLE libro (
  id_libro           INT NOT NULL AUTO_INCREMENT,
  codigo             VARCHAR(20)  NOT NULL,
  codigo_isbn        VARCHAR(250) NULL,
  titulo             VARCHAR(250) NOT NULL,
  descripcion        TEXT NULL,
  precio             DECIMAL(13,2) NOT NULL DEFAULT 0.00,
  visible            BOOLEAN NOT NULL,
  fecha_publicacion  DATE NULL,
  rating             INT NULL CHECK (rating >= 1 AND rating <= 5),
  PRIMARY KEY (id_libro),
  UNIQUE KEY uq_libro_codigo_isbn (codigo_isbn)
) ENGINE=InnoDB;

-- ----------------------------
-- TABLA: autor
-- ----------------------------
CREATE TABLE autor (
  id_autor INT NOT NULL AUTO_INCREMENT,
  nombre   VARCHAR(250) NOT NULL,
  pais     VARCHAR(250) NULL,
  PRIMARY KEY (id_autor)
) ENGINE=InnoDB;

-- ----------------------------
-- TABLA: libro_autor (N..N)
-- ----------------------------
CREATE TABLE libro_autor (
  id_libro INT NOT NULL,
  id_autor INT NOT NULL,
  PRIMARY KEY (id_libro, id_autor),
  KEY idx_libro_autor_id_autor (id_autor),
  CONSTRAINT fk_libro_autor_libro
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_libro_autor_autor
    FOREIGN KEY (id_autor) REFERENCES autor(id_autor)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ----------------------------
-- TABLA: libro_categoria (N..N)
-- ----------------------------
CREATE TABLE libro_categoria (
  id_libro     INT NOT NULL,
  id_categoria SMALLINT NOT NULL,
  PRIMARY KEY (id_libro, id_categoria),
  KEY idx_libro_categoria_id_categoria (id_categoria),
  CONSTRAINT fk_libro_categoria_libro
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_libro_categoria_categoria
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ----------------------------
-- TABLA: inventario
-- ----------------------------
CREATE TABLE inventario (
  id_inventario       INT NOT NULL AUTO_INCREMENT,
  id_libro            INT NOT NULL,
  cantidad_disponible INT NOT NULL DEFAULT 0,
  fecha_actualizacion DATETIME NULL,
  PRIMARY KEY (id_inventario, id_libro),
  KEY idx_inventario_id_libro (id_libro),
  CONSTRAINT fk_inventario_libro
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ----------------------------
-- TABLA: usuario
-- ----------------------------
CREATE TABLE usuario (
  id_usuario BIGINT NOT NULL AUTO_INCREMENT,
  nombre     VARCHAR(250) NOT NULL,
  email      VARCHAR(250) NOT NULL,
  telefono   VARCHAR(50)  NULL,
  password   VARCHAR(50)  NOT NULL,
  rol        CHAR(1)      NOT NULL,
  estado     CHAR(1)      NOT NULL,
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uq_usuario_email (email)
) ENGINE=InnoDB;

-- ----------------------------
-- TABLA: direccion
-- ----------------------------
CREATE TABLE direccion (
  id_direccion          SMALLINT NOT NULL AUTO_INCREMENT,
  id_usuario            BIGINT NOT NULL,
  nombre                VARCHAR(250) NOT NULL,
  direccion             VARCHAR(250) NOT NULL,
  complemento_direccion VARCHAR(250) NULL,
  ciudad                VARCHAR(250) NOT NULL,
  departamento          VARCHAR(250) NOT NULL,
  pais                  VARCHAR(250) NOT NULL,
  codigo_postal         VARCHAR(50)  NULL,
  PRIMARY KEY (id_direccion, id_usuario),
  KEY idx_direccion_id_usuario (id_usuario),
  CONSTRAINT fk_direccion_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ----------------------------
-- TABLA: carrito
-- ----------------------------
CREATE TABLE carrito (
  id_carrito     BIGINT NOT NULL AUTO_INCREMENT,
  usuario        BIGINT NOT NULL,
  fecha_creacion DATETIME NULL,
  estado         CHAR(1) NOT NULL,
  PRIMARY KEY (id_carrito),
  KEY idx_carrito_usuario (usuario),
  CONSTRAINT fk_carrito_usuario
    FOREIGN KEY (usuario) REFERENCES usuario(id_usuario)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ----------------------------
-- TABLA: carrito_item
-- ----------------------------
CREATE TABLE carrito_item (
  id_item         INT NOT NULL AUTO_INCREMENT,
  id_carrito      BIGINT NOT NULL,
  id_libro        INT NOT NULL,
  cantidad        SMALLINT NOT NULL DEFAULT 1,
  precio_unitario DECIMAL(13,2) NOT NULL DEFAULT 0.00,
  subtotal        DECIMAL(13,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (id_item, id_carrito, id_libro),
  KEY idx_carrito_item_carrito (id_carrito),
  KEY idx_carrito_item_libro (id_libro),
  CONSTRAINT fk_carrito_item_carrito
    FOREIGN KEY (id_carrito) REFERENCES carrito(id_carrito)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_carrito_item_libro
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ----------------------------
-- TABLA: pedido
-- ----------------------------
CREATE TABLE pedido (
  id_pedido        BIGINT NOT NULL AUTO_INCREMENT,
  usuario          BIGINT NOT NULL,
  fecha_pedido     DATETIME NULL,
  estado           CHAR(1) NOT NULL,
  total            DECIMAL(13,2) NOT NULL DEFAULT 0.00,
  email_contacto   VARCHAR(250) NULL,
  nombre_contacto  VARCHAR(250) NULL,
  PRIMARY KEY (id_pedido),
  KEY idx_pedido_usuario (usuario),
  CONSTRAINT fk_pedido_usuario
    FOREIGN KEY (usuario) REFERENCES usuario(id_usuario)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ----------------------------
-- TABLA: detalle_pedido
-- ----------------------------
CREATE TABLE detalle_pedido (
  id_detalle      INT NOT NULL AUTO_INCREMENT,
  id_pedido       BIGINT NOT NULL,
  id_libro        INT NOT NULL,
  cantidad        SMALLINT NOT NULL DEFAULT 1,
  precio_unitario DECIMAL(13,2) NOT NULL DEFAULT 0.00,
  subtotal        DECIMAL(13,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (id_detalle, id_pedido, id_libro),
  KEY idx_detalle_pedido (id_pedido),
  KEY idx_detalle_libro (id_libro),
  CONSTRAINT fk_detalle_pedido_pedido
    FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_detalle_pedido_libro
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;
