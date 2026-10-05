package cr.ac.una.lab1.business.state;

import cr.ac.una.lab1.data.EstadoMatricula;

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
    public EstadoMatricula activar() {
        return EstadoMatricula.ACTIVA;
    }

    @Override
    public EstadoMatricula cancelar() {
        return EstadoMatricula.CANCELADA;
    }
}
