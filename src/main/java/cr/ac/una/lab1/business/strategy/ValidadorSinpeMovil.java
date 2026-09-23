package cr.ac.una.lab1.business.strategy;

import cr.ac.una.lab1.business.MatriculaRequestDTO;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import org.springframework.stereotype.Component;

/**
 * Strategy SINPE_MOVIL: la referencia es obligatoria y debe ser un número
 * de exactamente 8 dígitos (formato de número de teléfono costarricense).
 *
 * <p>Ejemplo válido: {@code "88001234"}.
 */
@Component
public class ValidadorSinpeMovil implements ValidadorMetodoPago {

    private static final java.util.regex.Pattern PATRON_SINPE =
            java.util.regex.Pattern.compile("^[0-9]{8}$");

    @Override
    public void validar(MatriculaRequestDTO dto) {
        String ref = dto.referencia();
        if (ref == null || ref.isBlank()) {
            throw new ReglaNegocioException(
                    "SINPE Móvil requiere una referencia (número de teléfono de 8 dígitos).");
        }
        if (!PATRON_SINPE.matcher(ref.trim()).matches()) {
            throw new ReglaNegocioException(
                    "La referencia SINPE Móvil debe ser exactamente 8 dígitos numéricos. Recibido: '" + ref + "'.");
        }
    }
}
