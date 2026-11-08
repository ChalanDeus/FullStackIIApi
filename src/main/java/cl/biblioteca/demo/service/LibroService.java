package cl.biblioteca.demo.service;

import cl.biblioteca.demo.exception.IsbnDuplicadoException;
import cl.biblioteca.demo.exception.LibroNotFoundException;
import cl.biblioteca.demo.model.Libro;
import cl.biblioteca.demo.repository.LibroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibroService {

    private final LibroRepository repository;

    public LibroService(LibroRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Libro> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Libro> listarPaginado(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Libro buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new LibroNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Libro> buscar(String termino) {
        return repository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(termino, termino);
    }

    @Transactional(readOnly = true)
    public Page<Libro> buscar(String termino, Pageable pageable) {
        return repository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(termino, termino, pageable);
    }

    @Transactional
    public Libro guardar(Libro libro) {
        libro.setId(null);
        if (repository.existsByIsbn(libro.getIsbn())) {
            throw new IsbnDuplicadoException(libro.getIsbn());
        }
        return repository.save(libro);
    }

    @Transactional
    public Libro actualizar(Long id, Libro datos) {
        Libro existente = buscarPorId(id);

        if (repository.existsByIsbnAndIdNot(datos.getIsbn(), id)) {
            throw new IsbnDuplicadoException(datos.getIsbn());
        }

        existente.setIsbn(datos.getIsbn());
        existente.setTitulo(datos.getTitulo());
        existente.setEditorial(datos.getEditorial());
        existente.setFechaPublicacion(datos.getFechaPublicacion());
        existente.setAutor(datos.getAutor());

        return repository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        Libro libro = buscarPorId(id);
        repository.delete(libro);
    }

    @Transactional(readOnly = true)
    public long totalLibros() {
        return repository.count();
    }
}
