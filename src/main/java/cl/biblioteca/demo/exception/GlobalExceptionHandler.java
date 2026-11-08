package cl.biblioteca.demo.exception;

import cl.biblioteca.demo.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Manejo centralizado de excepciones (Desafio 4 de la guia).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LibroNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLibroNotFound(LibroNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()));
    }

    @ExceptionHandler(IsbnDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleIsbnDuplicado(IsbnDuplicadoException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.CampoError> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.CampoError(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "La peticion contiene campos invalidos",
                request.getRequestURI(),
                campos));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ErrorResponse.CampoError> campos = ex.getConstraintViolations().stream()
                .map(violation -> new ErrorResponse.CampoError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .toList();

        return ResponseEntity.badRequest().body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "La peticion contiene parametros invalidos",
                request.getRequestURI(),
                campos));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "La operacion viola una restriccion de la base de datos",
                request.getRequestURI()));
    }
}
