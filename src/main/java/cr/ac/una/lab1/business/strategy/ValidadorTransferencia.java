package cr.ac.una.lab1.business.strategy;

import cr.ac.una.lab1.business.MatriculaRequestDTO;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import org.springframework.stereotype.Component;

/**
 * Strategy TRANSFERENCIA: la referencia es obligatoria y debe ser un código
 * alfanumérico de entre 6 y 30 caracteres (sin espacios).
 *
 * <p>Ejemplo válido: {@code "BNCR20261001X9"}.
 */
@Component
public class ValidadorTransferencia implements ValidadorMetodoPago {

    private static final java.util.regex.Pattern PATRON_TRANSFERENCIA =
            java.util.regex.Pattern.compile("^[A-Za-z0-9]{6,30}$");

    @Override
    public void validar(MatriculaRequestDTO dto) {
        String ref = dto.referencia();
        if (ref == null || ref.isBlank()) {
            throw new ReglaNegocioException(
                    "Transferencia bancaria requiere un código de referencia alfanumérico.");
        }
        if (!PATRON_TRANSFERENCIA.matcher(ref.trim()).matches()) {
            throw new ReglaNegocioException(
                    "La referencia de transferencia debe ser alfanumérica (6-30 caracteres, sin espacios). " +
                    "Recibido: '" + ref + "'.");
        }
    }
}
