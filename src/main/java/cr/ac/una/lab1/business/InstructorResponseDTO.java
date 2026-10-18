package cr.ac.una.lab1.business;

/**
 * DTO que representa un instructor para la API pública.
 */
public record InstructorResponseDTO(
        Long usuarioId,
        String nombre,
        String identificacion,
        String correo,
        String especialidad
) {}
