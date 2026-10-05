package cr.ac.una.lab1.business.state;

import cr.ac.una.lab1.business.exception.CambioEstadoInvalidoException;
import cr.ac.una.lab1.data.EstadoMatricula;

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
    public EstadoMatricula activar() {
        throw new CambioEstadoInvalidoException(EstadoMatricula.CANCELADA, "activar");
    }

    @Override
    public EstadoMatricula cancelar() {
        throw new CambioEstadoInvalidoException(EstadoMatricula.CANCELADA, "cancelar");
    }
}
