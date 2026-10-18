package cr.ac.una.lab1.business;

import cr.ac.una.lab1.data.RolUsuario;

/**
 * DTO que representa un usuario para la API REST.
 * Oculta la contraseña hash y protege la entidad JPA de ser expuesta directamente.
 */
public record UsuarioResponseDTO(
        Long id,
        String nombre,
        String identificacion,
        String correo,
        RolUsuario rol
) {}
