package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.exception.CambioEstadoInvalidoException;
import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.business.exception.MatriculaDuplicadaException;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import cr.ac.una.lab1.business.exception.SinCupoDisponibleException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduce cada excepción del dominio a <strong>Problem Details</strong>
 * (RFC 9457) — {@code type}/{@code title}/{@code status}/{@code detail} — en
 * vez de un cuerpo JSON ad hoc. Ninguna respuesta de error incluye una traza
 * de Java: el detalle siempre es el mensaje de negocio (o uno genérico para
 * lo verdaderamente inesperado).
 *
 * <p>Los handlers de las subclases de {@link ReglaNegocioException}
 * ({@link CambioEstadoInvalidoException}, {@link MatriculaDuplicadaException},
 * {@link SinCupoDisponibleException}) existen aparte del handler genérico
 * para poder darles un {@code status}/{@code type} más preciso (409, no 422):
 * Spring resuelve siempre por el tipo más específico declarado, sin importar
 * el orden de los métodos en esta clase.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private static final String TYPE_BASE = "https://lescocr.cr/problems/";

    /** 404 — la entidad referenciada (por id) no existe. */
    @ExceptionHandler(EntidadNoEncontradaException.class)
    ProblemDetail handleEntidadNoEncontrada(EntidadNoEncontradaException ex) {
        return problema(HttpStatus.NOT_FOUND, "Entidad no encontrada", "entidad-no-encontrada", ex.getMessage());
    }

    /** 409 — se intentó una transición de estado que el ciclo de vida no permite (patrón State). */
    @ExceptionHandler(CambioEstadoInvalidoException.class)
    ProblemDetail handleCambioEstadoInvalido(CambioEstadoInvalidoException ex) {
        return problema(HttpStatus.CONFLICT, "Transición de estado inválida", "cambio-estado-invalido", ex.getMessage());
    }

    /** 409 — el estudiante ya tiene una matrícula vigente en ese curso. */
    @ExceptionHandler(MatriculaDuplicadaException.class)
    ProblemDetail handleMatriculaDuplicada(MatriculaDuplicadaException ex) {
        return problema(HttpStatus.CONFLICT, "Matrícula duplicada", "matricula-duplicada", ex.getMessage());
    }

    /** 409 — el curso no tiene cupo disponible (al matricular o al aprobar un pago tardío). */
    @ExceptionHandler(SinCupoDisponibleException.class)
    ProblemDetail handleSinCupo(SinCupoDisponibleException ex) {
        return problema(HttpStatus.CONFLICT, "Sin cupo disponible", "sin-cupo-disponible", ex.getMessage());
    }

    /** 422 — cualquier otra regla de negocio sin excepción propia (ver docs/patrones.md). */
    @ExceptionHandler(ReglaNegocioException.class)
    ProblemDetail handleReglaNegocio(ReglaNegocioException ex) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio violada", "regla-negocio", ex.getMessage());
    }

    /** 400 — @Valid rechazó el DTO de entrada (campo faltante o con formato inválido). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Error de validación.");
        return problema(HttpStatus.BAD_REQUEST, "Error de validación", "validacion", detalle);
    }

    /** 400 — un parámetro de ruta/consulta no tiene el tipo esperado (p. ej. {id} no numérico). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
        String detalle = "El valor '" + ex.getValue() + "' no es válido para el parámetro '" + ex.getName() + "'.";
        return problema(HttpStatus.BAD_REQUEST, "Parámetro inválido", "parametro-invalido", detalle);
    }

    /**
     * 500 — red de seguridad para cualquier excepción no prevista. Nunca expone
     * {@code ex.getMessage()} ni la traza: solo confirma que algo falló. Las
     * reglas de negocio esperadas ya quedan cubiertas por los handlers de arriba.
     */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleInesperada(Exception ex) {
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "error-interno",
                "Ocurrió un error inesperado. Contacte al administrador si persiste.");
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String tipo, String detalle) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalle);
        pd.setTitle(titulo);
        pd.setType(URI.create(TYPE_BASE + tipo));
        return pd;
    }
}
