package cr.ac.una.lab1.business.exception;

/**
 * Se lanza cuando se busca una entidad por su identificador y no existe en la base de datos.
 * Corresponde a un HTTP 404 en la capa de presentación.
 */
public class EntidadNoEncontradaException extends RuntimeException {

    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
    }

    public EntidadNoEncontradaException(String entidad, Object id) {
        super(entidad + " con id " + id + " no encontrado/a.");
    }
}
