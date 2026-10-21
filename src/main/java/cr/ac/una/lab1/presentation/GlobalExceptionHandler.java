package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.exception.AccesoNoAutorizadoException;
import cr.ac.una.lab1.business.exception.CambioEstadoInvalidoException;
import cr.ac.una.lab1.business.exception.CredencialesInvalidasException;
import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.business.exception.MatriculaDuplicadaException;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import cr.ac.una.lab1.business.exception.SinCupoDisponibleException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
 *
 * <p><strong>Ojo con {@code @ExceptionHandler(Exception.class)}:</strong> al
 * capturar todo, también capturaba excepciones que Spring ya sabe traducir
 * correctamente por su cuenta ({@link HttpMessageNotReadableException} → 400,
 * {@link NoResourceFoundException} → 404), convirtiéndolas en 500. Por eso
 * cada una de esas tiene su propio handler explícito aquí: más específico que
 * {@code Exception}, así que Spring lo usa en vez del genérico.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private static final String TYPE_BASE = "https://lescocr.cr/problems/";

    /** 401 — el login falló: correo inexistente o contraseña incorrecta. */
    @ExceptionHandler(CredencialesInvalidasException.class)
    ProblemDetail handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", "credenciales-invalidas", ex.getMessage());
    }

    /** 404 — la entidad referenciada (por id) no existe. */
    @ExceptionHandler(EntidadNoEncontradaException.class)
    ProblemDetail handleEntidadNoEncontrada(EntidadNoEncontradaException ex) {
        return problema(HttpStatus.NOT_FOUND, "Entidad no encontrada", "entidad-no-encontrada", ex.getMessage());
    }

    /**
     * 403 — un ESTUDIANTE intentó operar sobre un recurso que no le pertenece
     * (verificación de propiedad del recurso, OWASP API1).
     */
    @ExceptionHandler(AccesoNoAutorizadoException.class)
    ProblemDetail handleAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado", "acceso-no-autorizado", ex.getMessage());
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

    /** 400 — el cuerpo de la petición no es JSON válido o no calza con el DTO esperado. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleCuerpoInvalido(HttpMessageNotReadableException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Cuerpo de la petición inválido", "cuerpo-invalido",
                "El cuerpo de la petición no es JSON válido o no corresponde al formato esperado.");
    }

    /** 404 — no existe ningún endpoint para esa ruta (o para ese verbo en esa ruta). */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    ProblemDetail handleRutaNoEncontrada(Exception ex) {
        return problema(HttpStatus.NOT_FOUND, "Ruta no encontrada", "ruta-no-encontrada",
                "No existe ningún endpoint para esta ruta.");
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
