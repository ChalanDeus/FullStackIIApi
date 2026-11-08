package cl.biblioteca.demo.controller;

import cl.biblioteca.demo.dto.PageResponse;
import cl.biblioteca.demo.exception.GlobalExceptionHandler;
import cl.biblioteca.demo.exception.IsbnDuplicadoException;
import cl.biblioteca.demo.exception.LibroNotFoundException;
import cl.biblioteca.demo.model.Libro;
import cl.biblioteca.demo.service.LibroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LibroControllerTest {

    @Mock
    private LibroService libroService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Libro libro;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new LibroController(libroService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper().findAndRegisterModules();
        libro = new Libro(1L, "9780132350884", "Clean Code", "Prentice Hall", 2008, "Robert C. Martin");
    }

    @Test
    void listarLibrosDevuelve200() throws Exception {
        when(libroService.listar()).thenReturn(List.of(libro));

        mockMvc.perform(get("/api/v1/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Clean Code"))
                .andExpect(jsonPath("$[0].autor").value("Robert C. Martin"));
    }

    @Test
    void buscarLibroPorIdDevuelve200() throws Exception {
        when(libroService.buscarPorId(1L)).thenReturn(libro);

        mockMvc.perform(get("/api/v1/libros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isbn").value("9780132350884"));
    }

    @Test
    void buscarLibroPorIdInexistenteDevuelve404() throws Exception {
        when(libroService.buscarPorId(99L)).thenThrow(new LibroNotFoundException(99L));

        mockMvc.perform(get("/api/v1/libros/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe un libro con id 99"));
    }

    @Test
    void crearLibroValidoDevuelve201ConLocation() throws Exception {
        when(libroService.guardar(any(Libro.class))).thenReturn(libro);

        mockMvc.perform(post("/api/v1/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(libro)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/libros/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crearLibroInvalidoDevuelve400ConDetalleDeCampos() throws Exception {
        Libro invalido = new Libro(null, "", "", "", 0, "");

        mockMvc.perform(post("/api/v1/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.length()").value(5));
    }

    @Test
    void crearLibroConIsbnDuplicadoDevuelve409() throws Exception {
        when(libroService.guardar(any(Libro.class))).thenThrow(new IsbnDuplicadoException("9780132350884"));

        mockMvc.perform(post("/api/v1/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(libro)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void actualizarLibroDevuelve200() throws Exception {
        Libro cambios = new Libro(null, "9780132350884", "Clean Code 2da edicion", "Prentice Hall", 2009, "Robert C. Martin");
        when(libroService.actualizar(anyLong(), any(Libro.class))).thenReturn(cambios);

        mockMvc.perform(put("/api/v1/libros/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Clean Code 2da edicion"));
    }

    @Test
    void eliminarLibroDevuelve204() throws Exception {
        doNothing().when(libroService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/libros/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarLibroInexistenteDevuelve404() throws Exception {
        doThrow(new LibroNotFoundException(99L)).when(libroService).eliminar(99L);

        mockMvc.perform(delete("/api/v1/libros/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPorTituloOAutorDevuelve200() throws Exception {
        when(libroService.buscar("clean")).thenReturn(List.of(libro));

        mockMvc.perform(get("/api/v1/libros/buscar").param("q", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Clean Code"));
    }

    @Test
    void listarPaginadoDevuelve200ConEstructuraDePagina() throws Exception {
        when(libroService.listarPaginado(any())).thenReturn(new PageImpl<>(List.of(libro)));

        mockMvc.perform(get("/api/v1/libros/paginado").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].titulo").value("Clean Code"))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.pagina").value(0));
    }

    @Test
    void totalLibrosDevuelve200() throws Exception {
        when(libroService.totalLibros()).thenReturn(10L);

        mockMvc.perform(get("/api/v1/libros/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(10));
    }
}
