package cr.ac.una.lab1.business.state;

import cr.ac.una.lab1.business.exception.CambioEstadoInvalidoException;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.Matricula;

/**
 * Estado PENDIENTE: matrícula creada, pago aún no aprobado.
 *
 * <p>Transiciones permitidas:
 * <ul>
 *   <li>{@code activar()} → ACTIVA (el pago fue aprobado)
 *   <li>{@code cancelar()} → CANCELADA (el estudiante desiste antes del pago)
 * </ul>
 */
public class EstadoPendiente implements MatriculaEstado {

    public static final EstadoPendiente INSTANCIA = new EstadoPendiente();

    private EstadoPendiente() {}

    @Override
    public void activar(Matricula matricula) {
        matricula.setEstado(EstadoMatricula.ACTIVA);
    }

    @Override
    public void cancelar(Matricula matricula) {
        matricula.setEstado(EstadoMatricula.CANCELADA);
    }
}
