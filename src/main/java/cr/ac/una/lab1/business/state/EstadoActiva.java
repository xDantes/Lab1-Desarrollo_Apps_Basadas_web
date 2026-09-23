package cr.ac.una.lab1.business.state;

import cr.ac.una.lab1.business.exception.CambioEstadoInvalidoException;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.Matricula;

/**
 * Estado ACTIVA: el pago fue aprobado y la matrícula está vigente.
 *
 * <p>Transiciones permitidas:
 * <ul>
 *   <li>{@code cancelar()} → CANCELADA (baja del curso)
 * </ul>
 * <p>Transiciones prohibidas:
 * <ul>
 *   <li>{@code activar()} → lanza {@link CambioEstadoInvalidoException}
 *       (ya está activa; no tiene sentido activarla de nuevo)
 * </ul>
 */
public class EstadoActiva implements MatriculaEstado {

    public static final EstadoActiva INSTANCIA = new EstadoActiva();

    private EstadoActiva() {}

    @Override
    public void activar(Matricula matricula) {
        throw new CambioEstadoInvalidoException(EstadoMatricula.ACTIVA, "activar");
    }

    @Override
    public void cancelar(Matricula matricula) {
        matricula.setEstado(EstadoMatricula.CANCELADA);
    }
}
