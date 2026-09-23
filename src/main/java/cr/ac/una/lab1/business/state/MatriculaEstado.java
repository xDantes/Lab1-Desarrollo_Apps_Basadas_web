package cr.ac.una.lab1.business.state;

import cr.ac.una.lab1.data.Matricula;

/**
 * Patrón de diseño State — interfaz del estado de una {@link Matricula}.
 *
 * <p>Cada implementación concreta ({@link EstadoPendiente}, {@link EstadoActiva},
 * {@link EstadoCancelada}) encapsula las transiciones válidas desde ese estado.
 * Si la transición no está permitida, lanza
 * {@link cr.ac.una.lab1.business.exception.CambioEstadoInvalidoException}.
 *
 * <p>El campo persistido en base de datos sigue siendo {@code estado VARCHAR(20)};
 * el patrón State vive únicamente en la capa Java.
 */
public interface MatriculaEstado {

    /**
     * Transición a {@code ACTIVA} (aprobación de pago).
     * Solo válida desde {@code PENDIENTE}.
     */
    void activar(Matricula matricula);

    /**
     * Transición a {@code CANCELADA}.
     * Válida desde {@code PENDIENTE} o {@code ACTIVA}.
     */
    void cancelar(Matricula matricula);
}
