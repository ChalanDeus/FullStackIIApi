package cl.biblioteca.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un libro del catalogo de la biblioteca.
 */
@Entity
@Table(name = "libros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Libro del catalogo de la biblioteca")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador unico generado por la base de datos", example = "1")
    private Long id;

    @NotBlank(message = "El ISBN es obligatorio")
    @Size(max = 20, message = "El ISBN no puede superar los 20 caracteres")
    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    @Schema(description = "Codigo ISBN del libro", example = "9780132350884")
    private String isbn;

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 150, message = "El titulo no puede superar los 150 caracteres")
    @Column(name = "titulo", nullable = false, length = 150)
    @Schema(description = "Titulo del libro", example = "Clean Code")
    private String titulo;

    @NotBlank(message = "La editorial es obligatoria")
    @Size(max = 120, message = "La editorial no puede superar los 120 caracteres")
    @Column(name = "editorial", nullable = false, length = 120)
    @Schema(description = "Editorial del libro", example = "Prentice Hall")
    private String editorial;

    @NotNull(message = "La fecha de publicacion es obligatoria")
    @Min(value = 1450, message = "La fecha de publicacion debe ser posterior a 1450")
    @Max(value = 2100, message = "La fecha de publicacion no puede ser futura")
    @Column(name = "fecha_publicacion", nullable = false)
    @Schema(description = "Anio de publicacion", example = "2008")
    private Integer fechaPublicacion;

    @NotBlank(message = "El autor es obligatorio")
    @Size(max = 120, message = "El autor no puede superar los 120 caracteres")
    @Column(name = "autor", nullable = false, length = 120)
    @Schema(description = "Autor del libro", example = "Robert C. Martin")
    private String autor;
}
