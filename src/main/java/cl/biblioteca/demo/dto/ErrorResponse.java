package cl.biblioteca.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Estructura unica de respuesta para todos los errores de la API.
 */
@Schema(description = "Detalle de un error devuelto por la API")
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        List<CampoError> campos) {

    public static ErrorResponse of(int status, String error, String mensaje, String ruta) {
        return new ErrorResponse(Instant.now(), status, error, mensaje, ruta, List.of());
    }

    public static ErrorResponse of(int status, String error, String mensaje, String ruta, List<CampoError> campos) {
        return new ErrorResponse(Instant.now(), status, error, mensaje, ruta, campos);
    }

    @Schema(description = "Error de validacion asociado a un campo")
    public record CampoError(String campo, String mensaje) {
    }
}
