package cr.ac.una.lab1.business.exception;

/**
 * Se lanza cuando {@code POST /auth/login} falla: correo inexistente o
 * contraseña incorrecta. A propósito no se distingue cuál de las dos fue
 * (OWASP API2 — no revelar si el correo existe).
 *
 * <p>Mapeada a HTTP 401 por {@link cr.ac.una.lab1.presentation.GlobalExceptionHandler}
 * — no a 422: unas credenciales incorrectas no son una entidad "inválida",
 * son una autenticación que falló.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
