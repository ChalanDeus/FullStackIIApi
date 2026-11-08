package cl.biblioteca.demo.controller;

import cl.biblioteca.demo.dto.PageResponse;
import cl.biblioteca.demo.model.Libro;
import cl.biblioteca.demo.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/libros")
@Validated
@Tag(name = "Libros", description = "Operaciones CRUD sobre el catalogo de libros")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los libros", description = "Devuelve la lista completa de libros registrados")
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    public List<Libro> listarLibros() {
        return libroService.listar();
    }

    @GetMapping("/paginado")
    @Operation(summary = "Listar libros paginados",
            description = "Devuelve una pagina de libros. Permite indicar numero de pagina, tamano y ordenamiento")
    @ApiResponse(responseCode = "200", description = "Pagina obtenida correctamente")
    public PageResponse<Libro> listarLibrosPaginado(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "La pagina no puede ser negativa")
            @Parameter(description = "Numero de pagina (empieza en 0)", example = "0") int page,

            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "El tamano minimo es 1")
            @Max(value = 100, message = "El tamano maximo es 100")
            @Parameter(description = "Cantidad de libros por pagina", example = "5") int size,

            @RequestParam(defaultValue = "id")
            @Parameter(description = "Campo de ordenamiento", example = "id") String sortBy,

            @RequestParam(defaultValue = "asc")
            @Parameter(description = "Direccion del ordenamiento: asc o desc", example = "asc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return PageResponse.from(libroService.listarPaginado(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar libros por titulo o autor",
            description = "Filtra los libros cuyo titulo o autor contenga el texto indicado (sin distinguir mayusculas)")
    @ApiResponse(responseCode = "200", description = "Busqueda ejecutada correctamente")
    public List<Libro> buscarLibros(
            @RequestParam("q")
            @Parameter(description = "Texto a buscar en el titulo o en el autor", example = "Clean") String termino) {

        return libroService.buscar(termino);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un libro por su id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro encontrado"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado",
                    content = @Content(schema = @Schema(implementation = cl.biblioteca.demo.dto.ErrorResponse.class)))
    })
    public Libro buscarLibro(
            @PathVariable @Parameter(description = "Id del libro", example = "1") Long id) {
        return libroService.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo libro")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Libro creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = cl.biblioteca.demo.dto.ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "ISBN duplicado",
                    content = @Content(schema = @Schema(implementation = cl.biblioteca.demo.dto.ErrorResponse.class)))
    })
    public ResponseEntity<Libro> agregarLibro(@Valid @RequestBody Libro libro) {
        Libro creado = libroService.guardar(libro);
        return ResponseEntity.created(URI.create("/api/v1/libros/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un libro existente",
            description = "Actualiza todos los campos del libro identificado por el id de la ruta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = cl.biblioteca.demo.dto.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado",
                    content = @Content(schema = @Schema(implementation = cl.biblioteca.demo.dto.ErrorResponse.class)))
    })
    public Libro actualizarLibro(
            @PathVariable @Parameter(description = "Id del libro a actualizar", example = "1") Long id,
            @Valid @RequestBody Libro libro) {
        return libroService.actualizar(id, libro);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un libro")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Libro eliminado"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado",
                    content = @Content(schema = @Schema(implementation = cl.biblioteca.demo.dto.ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminarLibro(
            @PathVariable @Parameter(description = "Id del libro a eliminar", example = "1") Long id) {
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total")
    @Operation(summary = "Contar el total de libros registrados")
    @ApiResponse(responseCode = "200", description = "Total de libros")
    public long totalLibros() {
        return libroService.totalLibros();
    }
}
