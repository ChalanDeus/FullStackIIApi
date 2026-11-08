package cl.biblioteca.demo.exception;

/**
 * Se lanza cuando se intenta registrar un ISBN que ya existe.
 */
public class IsbnDuplicadoException extends RuntimeException {

    public IsbnDuplicadoException(String isbn) {
        super("Ya existe un libro con el ISBN " + isbn);
    }
}
