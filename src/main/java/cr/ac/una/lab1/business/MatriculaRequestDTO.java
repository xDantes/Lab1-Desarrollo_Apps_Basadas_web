package cr.ac.una.lab1.business;

import cr.ac.una.lab1.data.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la solicitud de matrícula de un estudiante en una lección.
 *
 * <p>Bean Validation garantiza que los campos obligatorios lleguen completos antes
 * de que el servicio ejecute cualquier lógica de negocio.
 * La validación de formato del campo {@code referencia} depende del {@code metodoPago}
 * y la realiza el patrón Strategy en {@link MatriculaService}.
 */
public record MatriculaRequestDTO(

        /** Id del usuario que solicita la matrícula. Debe tener rol ESTUDIANTE. */
        @NotNull(message = "El id del estudiante es obligatorio.")
        Long estudianteId,

        /** Id del curso publicado en el que se desea matricular. */
        @NotNull(message = "El id del curso es obligatorio.")
        Long cursoId,

        /** Id de la lección específica (dentro del curso) por la que se matricula. */
        @NotNull(message = "El id de la lección es obligatorio.")
        Long leccionId,

        /** Método de pago elegido por el estudiante. */
        @NotNull(message = "El método de pago es obligatorio.")
        MetodoPago metodoPago,

        /**
         * Referencia del pago: número de teléfono para SINPE_MOVIL, código de banco
         * para TRANSFERENCIA, null para TARJETA.
         * El validador Strategy comprueba el formato según el método elegido.
         */
        @Size(max = 50, message = "La referencia no puede superar 50 caracteres.")
        String referencia
) {}
