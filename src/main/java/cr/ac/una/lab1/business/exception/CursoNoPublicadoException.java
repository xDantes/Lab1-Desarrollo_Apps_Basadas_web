package cr.ac.una.lab1.business.exception;

/** Se lanza al intentar matricular a un estudiante en un curso que todavía no está publicado. */
public class CursoNoPublicadoException extends ReglaNegocioException {

    public CursoNoPublicadoException(String codigoCurso) {
        super("El curso '" + codigoCurso + "' no está publicado.");
    }
}
