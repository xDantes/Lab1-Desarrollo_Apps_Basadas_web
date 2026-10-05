package cr.ac.una.lab1.business.state;

import cr.ac.una.lab1.data.EstadoMatricula;

/**
 * Patrón de diseño State — estado de una {@link cr.ac.una.lab1.data.Matricula}.
 *
 * <p>Cada implementación concreta ({@link EstadoPendiente}, {@link EstadoActiva},
 * {@link EstadoCancelada}) decide el siguiente {@link EstadoMatricula} para cada
 * transición, o lanza {@link cr.ac.una.lab1.business.exception.CambioEstadoInvalidoException}
 * si no es válida. Ninguna implementación muta la matrícula directamente: solo
 * devuelve el valor que {@code Matricula} debe asignarse a sí misma. Así,
 * {@code Matricula.estado} no necesita exponer ningún setter público — el campo
 * se escribe únicamente desde dentro de la propia entidad.
 *
 * <p>El campo persistido en base de datos sigue siendo {@code estado VARCHAR(20)};
 * el patrón State vive únicamente en la capa Java.
 */
public interface MatriculaEstado {

    /**
     * Siguiente estado tras aprobar el pago (transición a {@code ACTIVA}).
     * Solo válida desde {@code PENDIENTE}.
     */
    EstadoMatricula activar();

    /**
     * Siguiente estado tras cancelar (transición a {@code CANCELADA}).
     * Válida desde {@code PENDIENTE} o {@code ACTIVA}.
     */
    EstadoMatricula cancelar();
}
