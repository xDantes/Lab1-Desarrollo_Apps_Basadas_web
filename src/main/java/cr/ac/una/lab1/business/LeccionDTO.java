package cr.ac.una.lab1.business;

/**
 * DTO que representa una lección dentro de un curso para su selección en la interfaz de usuario.
 * Garantiza que ninguna entidad JPA se exponga en la API.
 */
public record LeccionDTO(
        Long id,
        Integer orden,
        String titulo
) {}
