Feature: CRUD completo de Libro - Relatos de Papel
  Como usuario del sistema Relatos de Papel
  Quiero gestionar libros en el catálogo
  Para mantener el inventario actualizado

  Background:
    * url baseUrl
    * def timestamp = java.lang.System.currentTimeMillis()
    * def uniqueCode = 'KRT-' + timestamp
    * def uniqueIsbn = '978-KRT-' + timestamp

  Scenario: Viaje completo - Crear, Consultar, Actualizar, Eliminar un libro

    # ---- PASO 1: Verificar que el servicio está activo ----
    Given path '/actuator/health'
    When method GET
    Then status 200
    And match response.status == 'UP'

    # ---- PASO 2: Listar libros existentes (seed data) ----
    Given path booksPath
    When method GET
    Then status 200
    And match response.content == '#present'
    And match response.content == '#[_ > 0]'

    # ---- PASO 3: Crear un libro nuevo ----
    Given path booksPath
    And request
    """
    {
      "codigo": "#(uniqueCode)",
      "codigoIsbn": "#(uniqueIsbn)",
      "titulo": "Libro Karate BDD Test",
      "descripcion": "Libro creado por prueba de aceptacion Karate",
      "precio": 24.99,
      "visible": true,
      "fechaPublicacion": "2026-01-20",
      "rating": 4,
      "autorIds": [1],
      "categoriaIds": [1]
    }
    """
    When method POST
    Then status 201
    And match response.titulo == 'Libro Karate BDD Test'
    And match response.precio == 24.99
    And match response.idLibro == '#number'
    * def bookId = response.idLibro

    # ---- PASO 4: Consultar el libro creado ----
    Given path booksPath, bookId
    When method GET
    Then status 200
    And match response.idLibro == bookId
    And match response.titulo == 'Libro Karate BDD Test'
    And match response.precio == 24.99
    And match response.rating == 4
    And match response.autores == '#present'
    And match response.categorias == '#present'

    # ---- PASO 5: Actualizar el libro (PUT) ----
    Given path booksPath, bookId
    And request
    """
    {
      "codigo": "#(uniqueCode)",
      "codigoIsbn": "#(uniqueIsbn)",
      "titulo": "Libro Karate Actualizado",
      "descripcion": "Libro actualizado por prueba Karate",
      "precio": 34.99,
      "visible": true,
      "fechaPublicacion": "2026-02-15",
      "rating": 5,
      "autorIds": [1, 2],
      "categoriaIds": [1]
    }
    """
    When method PUT
    Then status 200
    And match response.titulo == 'Libro Karate Actualizado'
    And match response.precio == 34.99
    And match response.rating == 5

    # ---- PASO 6: Verificar que el cambio persistió ----
    Given path booksPath, bookId
    When method GET
    Then status 200
    And match response.titulo == 'Libro Karate Actualizado'
    And match response.precio == 34.99

    # ---- PASO 7: Actualización parcial (PATCH) ----
    Given path booksPath, bookId
    And request { "descripcion": "Parcheado por Karate" }
    When method PATCH
    Then status 200
    And match response.descripcion == 'Parcheado por Karate'
    And match response.titulo == 'Libro Karate Actualizado'

    # ---- PASO 8: Eliminar el libro ----
    Given path booksPath, bookId
    When method DELETE
    Then status 204

    # ---- PASO 9: Confirmar que ya no existe (404) ----
    Given path booksPath, bookId
    When method GET
    Then status 404
    And match response.status == 404
