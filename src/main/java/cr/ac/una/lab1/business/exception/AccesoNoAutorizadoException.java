package cr.ac.una.lab1.business.exception;

/**
 * Se lanza cuando un usuario autenticado intenta operar sobre un recurso
 * que pertenece a otro usuario (OWASP API1 — Broken Object Level Authorization).
 *
 * <p>Mapeada a HTTP 403 por {@link cr.ac.una.lab1.presentation.GlobalExceptionHandler}.
 */
public class AccesoNoAutorizadoException extends RuntimeException {

    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
