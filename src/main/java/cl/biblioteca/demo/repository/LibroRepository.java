package cl.biblioteca.demo.repository;

import cl.biblioteca.demo.model.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA de libros. Spring Data genera la implementacion en tiempo de ejecucion.
 */
public interface LibroRepository extends JpaRepository<Libro, Long> {

    Optional<Libro> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    List<Libro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(String titulo, String autor);

    Page<Libro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(
            String titulo, String autor, Pageable pageable);
}
