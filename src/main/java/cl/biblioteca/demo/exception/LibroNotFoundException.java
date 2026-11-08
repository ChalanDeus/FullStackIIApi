package cl.biblioteca.demo.exception;

/**
 * Se lanza cuando un libro no existe en la base de datos.
 */
public class LibroNotFoundException extends RuntimeException {

    private final Long id;

    public LibroNotFoundException(Long id) {
        super("No existe un libro con id " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
