package cr.ac.una.lab1.business;

import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.EstadoPago;
import cr.ac.una.lab1.data.MetodoPago;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO de salida que representa el resultado de una operación de matrícula.
 * Devuelve al cliente los datos relevantes sin exponer la entidad JPA directamente.
 */
public record MatriculaResponseDTO(
        Long matriculaId,
        String consecutivo,
        Long estudianteId,
        String nombreEstudiante,
        Long cursoId,
        String codigoCurso,
        String nombreCurso,
        Long leccionId,
        String tituloLeccion,
        EstadoMatricula estadoMatricula,
        BigDecimal precioFinal,
        OffsetDateTime fechaMatricula,
        Long pagoId,
        EstadoPago estadoPago,
        MetodoPago metodoPago
) {}
