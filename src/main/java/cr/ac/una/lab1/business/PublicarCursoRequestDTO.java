package cr.ac.una.lab1.business;

import jakarta.validation.constraints.NotNull;

/**
 * Solicitud mínima para publicar un curso.
 *
 * <p>El administrador que autoriza la operación debe identificarse.
 * Bean Validation verifica que el campo sea no nulo antes de que el
 * servicio ejecute las reglas de negocio.
 *
 * @param administradorId ID del usuario con rol ADMINISTRADOR que autoriza
 *                        la publicación del curso.
 */
public record PublicarCursoRequestDTO(
        @NotNull(message = "El ID del administrador que autoriza la publicación es obligatorio")
        Long administradorId
) {}
