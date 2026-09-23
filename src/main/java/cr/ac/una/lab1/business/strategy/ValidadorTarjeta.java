package cr.ac.una.lab1.business.strategy;

import cr.ac.una.lab1.business.MatriculaRequestDTO;
import org.springframework.stereotype.Component;

/**
 * Strategy TARJETA: el campo {@code referencia} es opcional en el momento de crear
 * la matrícula (se asigna cuando se procesa el pago). No se valida formato aquí.
 */
@Component
public class ValidadorTarjeta implements ValidadorMetodoPago {

    @Override
    public void validar(MatriculaRequestDTO dto) {
        // TARJETA no exige referencia al momento de matricular;
        // la referencia se genera cuando la pasarela confirma el cobro.
    }
}
