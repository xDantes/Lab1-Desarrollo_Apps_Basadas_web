package cr.ac.una.lab1.business;

/**
 * DTO que representa una lección con detalle del curso y del instructor asignado.
 */
public record LeccionDetalleResponseDTO(
        Long id,
        Integer orden,
        String titulo,
        Long cursoId,
        String codigoCurso,
        String nombreCurso,
        Long instructorId,
        String nombreInstructor
) {}
