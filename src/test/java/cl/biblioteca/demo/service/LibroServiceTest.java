package cl.biblioteca.demo.service;

import cl.biblioteca.demo.exception.IsbnDuplicadoException;
import cl.biblioteca.demo.exception.LibroNotFoundException;
import cl.biblioteca.demo.model.Libro;
import cl.biblioteca.demo.repository.LibroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LibroServiceTest {

    @Mock
    private LibroRepository repository;

    private LibroService service;
    private Libro libro;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new LibroService(repository);
        libro = new Libro(1L, "9780132350884", "Clean Code", "Prentice Hall", 2008, "Robert C. Martin");
    }

    @Test
    void listarDevuelveTodosLosLibros() {
        when(repository.findAll()).thenReturn(List.of(libro));

        List<Libro> resultado = service.listar();

        assertEquals(1, resultado.size());
        assertEquals("Clean Code", resultado.get(0).getTitulo());
    }

    @Test
    void listarPaginadoDevuelveUnaPagina() {
        Page<Libro> pagina = new PageImpl<>(List.of(libro));
        when(repository.findAll(any(Pageable.class))).thenReturn(pagina);

        Page<Libro> resultado = service.listarPaginado(PageRequest.of(0, 5));

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void buscarPorIdDevuelveElLibroCuandoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(libro));

        Libro resultado = service.buscarPorId(1L);

        assertEquals("Clean Code", resultado.getTitulo());
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(LibroNotFoundException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void guardarPersisteElLibroCuandoElIsbnEsNuevo() {
        when(repository.existsByIsbn(libro.getIsbn())).thenReturn(false);
        when(repository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Libro resultado = service.guardar(libro);

        assertEquals("9780132350884", resultado.getIsbn());
        verify(repository).save(libro);
    }

    @Test
    void guardarLanzaExcepcionCuandoElIsbnYaExiste() {
        when(repository.existsByIsbn(libro.getIsbn())).thenReturn(true);

        assertThrows(IsbnDuplicadoException.class, () -> service.guardar(libro));
        verify(repository, never()).save(any(Libro.class));
    }

    @Test
    void actualizarModificaLosCamposDelLibroExistente() {
        Libro cambios = new Libro(null, "9780132350884", "Clean Code 2da edicion", "Prentice Hall", 2009, "Robert C. Martin");
        when(repository.findById(1L)).thenReturn(Optional.of(libro));
        when(repository.existsByIsbnAndIdNot(cambios.getIsbn(), 1L)).thenReturn(false);
        when(repository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Libro resultado = service.actualizar(1L, cambios);

        assertEquals(1L, resultado.getId());
        assertEquals("Clean Code 2da edicion", resultado.getTitulo());
        assertEquals(2009, resultado.getFechaPublicacion());
    }

    @Test
    void actualizarLanzaExcepcionCuandoElLibroNoExiste() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(LibroNotFoundException.class, () -> service.actualizar(99L, libro));
    }

    @Test
    void eliminarBorraElLibroCuandoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(libro));

        service.eliminar(1L);

        verify(repository).delete(libro);
    }

    @Test
    void eliminarLanzaExcepcionCuandoElLibroNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(LibroNotFoundException.class, () -> service.eliminar(99L));
    }

    @Test
    void buscarFiltraPorTituloOAutor() {
        when(repository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase("clean", "clean"))
                .thenReturn(List.of(libro));

        List<Libro> resultado = service.buscar("clean");

        assertEquals(1, resultado.size());
    }

    @Test
    void totalLibrosDevuelveElConteoDelRepositorio() {
        when(repository.count()).thenReturn(10L);

        assertEquals(10L, service.totalLibros());
    }
}
