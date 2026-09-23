package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte las excepciones propias del dominio en respuestas HTTP con cuerpo JSON.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /** 404 — entidad no encontrada. */
    @ExceptionHandler(EntidadNoEncontradaException.class)
    ResponseEntity<Map<String, Object>> handleNotFound(EntidadNoEncontradaException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** 422 — regla de negocio violada (incluye CambioEstadoInvalidoException). */
    @ExceptionHandler(ReglaNegocioException.class)
    ResponseEntity<Map<String, Object>> handleBusinessRule(ReglaNegocioException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /** 400 — fallo de Bean Validation (@NotNull, @Size, etc.). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Error de validación.");
        return error(HttpStatus.BAD_REQUEST, detalle);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
