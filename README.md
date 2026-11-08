# Guía 3.1.2 — API REST con Spring Boot + Swagger

Proyecto de la asignatura **DSY1104 Desarrollo Full Stack II** (EA: Integración y Comunicación REST).

API REST para gestionar el catálogo de libros de una biblioteca, construida con Spring Boot,
Spring Data JPA, H2 y documentada con OpenAPI / Swagger UI.

---

## 1. Stack utilizado

| Componente | Versión | Nota |
|---|---|---|
| Java | 21 (compilado con JDK 24) | `java.version=21` |
| Spring Boot | 4.0.5 | versión estable del proyecto base |
| Spring Data JPA | gestionada por Boot | Pasos 1 y 8 de la guía |
| H2 Database | 2.4.240 | base de datos en memoria |
| springdoc-openapi | 3.2.0 | equivalente moderno a Springfox para Spring Boot 4 |
| Lombok | gestionada por Boot | getters/setters/constructores |
| JUnit 5 + Mockito | gestionada por Boot | 25 pruebas unitarias |

> **Desviación justificada respecto de la guía:** la guía pide Springfox 3.0.0, que quedó
> abandonado y no es compatible con Spring Boot 4 (ni con Java 24). Se reemplazó por
> **springdoc-openapi**, que produce la misma Swagger UI y usa OpenAPI 3. El resto de la
> guía (pasos, estructura y endpoints) se mantiene igual.

---

## 2. Estructura del proyecto

```
src/main/java/cl/biblioteca/demo
├── config/OpenApiConfig.java          # Paso 3: configuración de Swagger/OpenAPI
├── controller/LibroController.java    # Paso 7: endpoints REST
├── dto/ErrorResponse.java             # Desafío 4: cuerpo único de errores
├── dto/PageResponse.java              # Desafío 3: respuesta paginada
├── exception/GlobalExceptionHandler.java
├── exception/LibroNotFoundException.java
├── exception/IsbnDuplicadoException.java
├── model/Libro.java                   # Paso 4: entidad JPA + validaciones
├── repository/LibroRepository.java    # Paso 5: repositorio JPA
├── service/LibroService.java          # Paso 6: lógica de negocio
└── DemoApplication.java

src/main/resources
├── application.properties             # Paso 8: configuración de H2 y Swagger
└── data.sql                           # 10 libros de ejemplo
```

---

## 3. Cómo ejecutar

```powershell
# Desde la carpeta del proyecto
.\mvnw.cmd spring-boot:run
```

Luego abrir:

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Consola H2 | http://localhost:8080/h2-console |

Datos para la consola H2: JDBC URL `jdbc:h2:mem:biblioteca`, usuario `sa`, contraseña vacía.

### Ejecutar las pruebas

```powershell
.\mvnw.cmd test
```

Resultado esperado: `Tests run: 25, Failures: 0, Errors: 0`.

---

## 4. Endpoints

Base: `/api/v1/libros`

| Método | Ruta | Descripción | Código esperado |
|---|---|---|---|
| GET | `/api/v1/libros` | Lista todos los libros | 200 |
| GET | `/api/v1/libros/{id}` | Obtiene un libro por id | 200 / 404 |
| GET | `/api/v1/libros/buscar?q=clean` | Busca por título o autor | 200 |
| GET | `/api/v1/libros/paginado?page=0&size=5&sortBy=id&direction=asc` | Lista paginada | 200 |
| GET | `/api/v1/libros/total` | Cantidad total de libros | 200 |
| POST | `/api/v1/libros` | Crea un libro | 201 / 400 / 409 |
| PUT | `/api/v1/libros/{id}` | Actualiza un libro | 200 / 400 / 404 / 409 |
| DELETE | `/api/v1/libros/{id}` | Elimina un libro | 204 / 404 |

### Ejemplo de cuerpo (POST / PUT)

```json
{
  "isbn": "9780132350884",
  "titulo": "Clean Code",
  "editorial": "Prentice Hall",
  "fechaPublicacion": 2008,
  "autor": "Robert C. Martin"
}
```

### Ejemplo de error (validación)

```json
{
  "timestamp": "2026-11-08T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "La peticion contiene campos invalidos",
  "ruta": "/api/v1/libros",
  "campos": [
    { "campo": "titulo", "mensaje": "El titulo es obligatorio" }
  ]
}
```

---

## 5. Correspondencia con la guía

| Paso de la guía | Estado | Dónde |
|---|---|---|
| 1. Configuración del proyecto | ✅ | `pom.xml` |
| 2. Dependencias de Swagger | ✅ (springdoc 3.2.0) | `pom.xml` |
| 3. Configurar Swagger | ✅ | `config/OpenApiConfig.java` |
| 4. Crear modelo | ✅ | `model/Libro.java` (`@Entity`) |
| 5. Crear repositorio | ✅ | `repository/LibroRepository.java` (`JpaRepository`) |
| 6. Crear servicio | ✅ | `service/LibroService.java` |
| 7. Crear controlador | ✅ | `controller/LibroController.java` |
| 8. Configurar base de datos | ✅ | `application.properties` + `data.sql` |
| 9. Ejecutar la aplicación | ✅ | `.\mvnw.cmd spring-boot:run` |
| 10. Probar la API | ✅ | Swagger UI |

## 6. Desafíos adicionales

| Desafío | Estado | Implementación |
|---|---|---|
| 1. Validación de campos | ✅ | `@NotBlank`, `@Size`, `@Min`, `@Max`, `@NotNull`, `@Valid` |
| 2. Búsqueda por título o autor | ✅ | `GET /buscar?q=`, consulta derivada `ContainingIgnoreCase` |
| 3. Paginación | ✅ | `GET /paginado`, `PageResponse<T>` |
| 4. Manejo de excepciones | ✅ | `@RestControllerAdvice` + `ErrorResponse` |
| 5. Pruebas unitarias | ✅ | `LibroServiceTest` (12) y `LibroControllerTest` (12) + contexto (1) |

---

## 7. Correcciones aplicadas respecto de la versión anterior

1. `POST` ahora genera el `id` automáticamente (`IDENTITY`) y devuelve **201** con cabecera `Location`.
2. `PUT /{id}` usa el **id de la ruta** y actualiza el libro correcto (antes dependía del body).
3. `GET /{id}` inexistente devuelve **404** con detalle (antes 200 con body vacío).
4. `DELETE /{id}` inexistente devuelve **404** y el éxito devuelve **204**.
5. Se agregó validación, manejo de errores centralizado, ISBN duplicado (**409**), búsqueda y paginación.
6. La persistencia pasó de una lista en memoria a **Spring Data JPA + H2**, como pide la guía.
