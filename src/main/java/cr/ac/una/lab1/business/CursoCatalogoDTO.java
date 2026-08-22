package cr.ac.una.lab1.business;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representación de un Curso para el catálogo público: solo lo que un
 * estudiante necesita ver, con los cálculos de negocio ya resueltos
 * (precio final con descuento, cupos disponibles).
 */
public record CursoCatalogoDTO(
        Long id,
        String codigo,
        String nombre,
        String nivel,
        BigDecimal precioFinal,
        int cupoTotal,
        int cuposDisponibles,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}
