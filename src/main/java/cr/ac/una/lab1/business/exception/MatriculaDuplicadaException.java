package cr.ac.una.lab1.business.exception;

/**
 * Se lanza cuando un estudiante intenta matricularse en un curso en el que ya
 * tiene una matrícula vigente (PENDIENTE o ACTIVA).
 */
public class MatriculaDuplicadaException extends ReglaNegocioException {

    public MatriculaDuplicadaException(String codigoCurso) {
        super("El estudiante ya tiene una matrícula vigente en el curso '" + codigoCurso + "'.");
    }
}
