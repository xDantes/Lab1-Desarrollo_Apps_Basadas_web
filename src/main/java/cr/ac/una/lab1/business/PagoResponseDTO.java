package cr.ac.una.lab1.business;

import cr.ac.una.lab1.data.EstadoPago;
import cr.ac.una.lab1.data.MetodoPago;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO que representa un pago para la API REST.
 */
public record PagoResponseDTO(
        Long id,
        Long matriculaId,
        String consecutivoMatricula,
        Long usuarioId,
        String correoUsuario,
        BigDecimal monto,
        MetodoPago metodo,
        String referencia,
        EstadoPago estado,
        OffsetDateTime fecha,
        OffsetDateTime actualizadoEn
) {}
