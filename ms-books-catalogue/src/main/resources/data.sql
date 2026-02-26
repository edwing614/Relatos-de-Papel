-- Seed Data para ms-books-catalogue

-- Categorías
INSERT INTO categoria (nombre, descripcion) VALUES ('Ficción', 'Libros de ficción y novelas');
INSERT INTO categoria (nombre, descripcion) VALUES ('No Ficción', 'Libros basados en hechos reales');
INSERT INTO categoria (nombre, descripcion) VALUES ('Ciencia Ficción', 'Ficción científica y futurista');
INSERT INTO categoria (nombre, descripcion) VALUES ('Fantasía', 'Mundos mágicos y fantásticos');
INSERT INTO categoria (nombre, descripcion) VALUES ('Romance', 'Historias de amor');
INSERT INTO categoria (nombre, descripcion) VALUES ('Misterio', 'Novelas de misterio y suspenso');
INSERT INTO categoria (nombre, descripcion) VALUES ('Historia', 'Libros históricos');
INSERT INTO categoria (nombre, descripcion) VALUES ('Tecnología', 'Libros de tecnología e informática');

-- Autores
INSERT INTO autor (nombre, pais) VALUES ('Gabriel García Márquez', 'Colombia');
INSERT INTO autor (nombre, pais) VALUES ('Jorge Luis Borges', 'Argentina');
INSERT INTO autor (nombre, pais) VALUES ('Isabel Allende', 'Chile');
INSERT INTO autor (nombre, pais) VALUES ('Mario Vargas Llosa', 'Perú');
INSERT INTO autor (nombre, pais) VALUES ('Julio Cortázar', 'Argentina');
INSERT INTO autor (nombre, pais) VALUES ('Carlos Ruiz Zafón', 'España');
INSERT INTO autor (nombre, pais) VALUES ('Paulo Coelho', 'Brasil');
INSERT INTO autor (nombre, pais) VALUES ('Stephen King', 'Estados Unidos');

-- Libros (con rating 1-5)
INSERT INTO libro (codigo, codigo_isbn, titulo, descripcion, precio, visible, fecha_publicacion, rating)
VALUES ('LIB001', '978-0307474728', 'Cien Años de Soledad', 'La obra maestra de Gabriel García Márquez', 45000.00, true, '1967-05-30', 5);

INSERT INTO libro (codigo, codigo_isbn, titulo, descripcion, precio, visible, fecha_publicacion, rating)
VALUES ('LIB002', '978-8420471839', 'Ficciones', 'Colección de cuentos de Borges', 38000.00, true, '1944-01-01', 5);

INSERT INTO libro (codigo, codigo_isbn, titulo, descripcion, precio, visible, fecha_publicacion, rating)
VALUES ('LIB003', '978-0525433477', 'La Casa de los Espíritus', 'Primera novela de Isabel Allende', 42000.00, true, '1982-01-01', 4);

INSERT INTO libro (codigo, codigo_isbn, titulo, descripcion, precio, visible, fecha_publicacion, rating)
VALUES ('LIB004', '978-8432217104', 'La Ciudad y los Perros', 'Novela de Mario Vargas Llosa', 40000.00, true, '1963-01-01', 4);

INSERT INTO libro (codigo, codigo_isbn, titulo, descripcion, precio, visible, fecha_publicacion, rating)
VALUES ('LIB005', '978-8437604572', 'Rayuela', 'Novela experimental de Julio Cortázar', 35000.00, true, '1963-06-28', 5);

INSERT INTO libro (codigo, codigo_isbn, titulo, descripcion, precio, visible, fecha_publicacion, rating)
VALUES ('LIB006', '978-8408163435', 'La Sombra del Viento', 'Primera novela de El Cementerio de los Libros Olvidados', 48000.00, true, '2001-04-01', 4);

INSERT INTO libro (codigo, codigo_isbn, titulo, descripcion, precio, visible, fecha_publicacion, rating)
VALUES ('LIB007', '978-0061122415', 'El Alquimista', 'La obra más famosa de Paulo Coelho', 32000.00, true, '1988-01-01', 3);

INSERT INTO libro (codigo, codigo_isbn, titulo, descripcion, precio, visible, fecha_publicacion, rating)
VALUES ('LIB008', '978-1501142970', 'It', 'Novela de terror de Stephen King', 55000.00, true, '1986-09-15', 4);

INSERT INTO libro (codigo, codigo_isbn, titulo, descripcion, precio, visible, fecha_publicacion, rating)
VALUES ('LIB009', '978-0000000000', 'Libro Oculto de Prueba', 'Este libro no es visible al público', 99000.00, false, '2020-01-01', 2);

-- Libro con stock bajo para probar caso FAIL de pedido
INSERT INTO libro (codigo, codigo_isbn, titulo, descripcion, precio, visible, fecha_publicacion, rating)
VALUES ('LIB010', '978-1111111111', 'Libro Stock Bajo', 'Solo 2 unidades disponibles para pruebas', 25000.00, true, '2023-01-01', 3);

-- Relación libro-autor
INSERT INTO libro_autor (id_libro, id_autor) VALUES (1, 1);
INSERT INTO libro_autor (id_libro, id_autor) VALUES (2, 2);
INSERT INTO libro_autor (id_libro, id_autor) VALUES (3, 3);
INSERT INTO libro_autor (id_libro, id_autor) VALUES (4, 4);
INSERT INTO libro_autor (id_libro, id_autor) VALUES (5, 5);
INSERT INTO libro_autor (id_libro, id_autor) VALUES (6, 6);
INSERT INTO libro_autor (id_libro, id_autor) VALUES (7, 7);
INSERT INTO libro_autor (id_libro, id_autor) VALUES (8, 8);
INSERT INTO libro_autor (id_libro, id_autor) VALUES (9, 1);
INSERT INTO libro_autor (id_libro, id_autor) VALUES (10, 1);

-- Relación libro-categoria
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (1, 1);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (1, 4);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (2, 1);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (3, 1);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (3, 4);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (4, 1);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (5, 1);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (6, 1);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (6, 6);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (7, 1);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (8, 1);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (8, 6);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (9, 1);
INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (10, 1);

-- Inventario (libro 10 con stock bajo = 2 para probar validación)
INSERT INTO inventario (id_libro, cantidad_disponible, fecha_actualizacion) VALUES (1, 50, CURRENT_TIMESTAMP);
INSERT INTO inventario (id_libro, cantidad_disponible, fecha_actualizacion) VALUES (2, 30, CURRENT_TIMESTAMP);
INSERT INTO inventario (id_libro, cantidad_disponible, fecha_actualizacion) VALUES (3, 25, CURRENT_TIMESTAMP);
INSERT INTO inventario (id_libro, cantidad_disponible, fecha_actualizacion) VALUES (4, 20, CURRENT_TIMESTAMP);
INSERT INTO inventario (id_libro, cantidad_disponible, fecha_actualizacion) VALUES (5, 40, CURRENT_TIMESTAMP);
INSERT INTO inventario (id_libro, cantidad_disponible, fecha_actualizacion) VALUES (6, 60, CURRENT_TIMESTAMP);
INSERT INTO inventario (id_libro, cantidad_disponible, fecha_actualizacion) VALUES (7, 100, CURRENT_TIMESTAMP);
INSERT INTO inventario (id_libro, cantidad_disponible, fecha_actualizacion) VALUES (8, 45, CURRENT_TIMESTAMP);
INSERT INTO inventario (id_libro, cantidad_disponible, fecha_actualizacion) VALUES (9, 5, CURRENT_TIMESTAMP);
INSERT INTO inventario (id_libro, cantidad_disponible, fecha_actualizacion) VALUES (10, 2, CURRENT_TIMESTAMP);
