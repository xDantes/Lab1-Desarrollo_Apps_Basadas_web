package cr.ac.una.lab1.business.exception;

import cr.ac.una.lab1.data.EstadoMatricula;

/**
 * Se lanza cuando se intenta una transición de estado inválida en una {@link cr.ac.una.lab1.data.Matricula}.
 * Extiende {@link ReglaNegocioException} porque es una violación de las reglas del ciclo de vida
 * de la matrícula definidas en el patrón State.
 *
 * <p>Transiciones válidas:
 * <pre>
 *   PENDIENTE → ACTIVA    (aprobar pago)
 *   PENDIENTE → CANCELADA (cancelar)
 *   ACTIVA    → CANCELADA (cancelar)
 *   CANCELADA → *         ilegal — estado terminal
 * </pre>
 */
public class CambioEstadoInvalidoException extends ReglaNegocioException {

    public CambioEstadoInvalidoException(EstadoMatricula desde, String operacion) {
        super("No se puede realizar '" + operacion + "' desde el estado " + desde + ".");
    }
}
