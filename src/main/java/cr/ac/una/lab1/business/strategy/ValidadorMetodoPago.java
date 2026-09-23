package cr.ac.una.lab1.business.strategy;

import cr.ac.una.lab1.business.MatriculaRequestDTO;

/**
 * Patrón de diseño Strategy — algoritmo de validación de un método de pago.
 *
 * <p>Cada método de pago (TARJETA, SINPE_MOVIL, TRANSFERENCIA) tiene reglas de
 * formato distintas para el campo {@code referencia}. En lugar de usar un
 * {@code switch} en {@link cr.ac.una.lab1.business.MatriculaService}, se inyecta
 * la implementación correspondiente al método elegido, cumpliendo el Principio
 * Abierto/Cerrado: agregar un nuevo método de pago solo requiere añadir una clase,
 * no modificar el servicio.
 *
 * @see ValidadorTarjeta
 * @see ValidadorSinpeMovil
 * @see ValidadorTransferencia
 */
public interface ValidadorMetodoPago {

    /**
     * Valida que el DTO tenga la referencia correcta para este método de pago.
     *
     * @param dto datos de la solicitud de matrícula
     * @throws cr.ac.una.lab1.business.exception.ReglaNegocioException si la referencia no cumple el formato
     */
    void validar(MatriculaRequestDTO dto);
}
