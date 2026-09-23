package cr.ac.una.lab1.business.state;

import cr.ac.una.lab1.business.exception.CambioEstadoInvalidoException;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.Matricula;

/**
 * Estado CANCELADA: estado terminal del ciclo de vida de una matrícula.
 *
 * <p>Ninguna transición es válida desde este estado. Cualquier intento lanza
 * {@link CambioEstadoInvalidoException}.
 */
public class EstadoCancelada implements MatriculaEstado {

    public static final EstadoCancelada INSTANCIA = new EstadoCancelada();

    private EstadoCancelada() {}

    @Override
    public void activar(Matricula matricula) {
        throw new CambioEstadoInvalidoException(EstadoMatricula.CANCELADA, "activar");
    }

    @Override
    public void cancelar(Matricula matricula) {
        throw new CambioEstadoInvalidoException(EstadoMatricula.CANCELADA, "cancelar");
    }
}
