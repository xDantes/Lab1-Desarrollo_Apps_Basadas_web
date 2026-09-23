package cr.ac.una.lab1.business.exception;

/**
 * Se lanza cuando una operación viola una regla de negocio del dominio
 * (cupo agotado, curso sin lecciones, estudiante ya matriculado, etc.).
 * Corresponde a un HTTP 422 (Unprocessable Entity) en la capa de presentación.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
