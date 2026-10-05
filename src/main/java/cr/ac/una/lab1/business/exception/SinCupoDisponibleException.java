package cr.ac.una.lab1.business.exception;

/**
 * Se lanza cuando un curso no tiene cupo disponible para una nueva matrícula
 * (al matricular, o al aprobar un pago cuando el cupo se agotó mientras la
 * matrícula estaba PENDIENTE).
 */
public class SinCupoDisponibleException extends ReglaNegocioException {

    public SinCupoDisponibleException(String codigoCurso) {
        super("El curso '" + codigoCurso + "' no tiene cupo disponible.");
    }
}
