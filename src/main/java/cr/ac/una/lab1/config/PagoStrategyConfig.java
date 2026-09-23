package cr.ac.una.lab1.config;

import cr.ac.una.lab1.business.strategy.ValidadorMetodoPago;
import cr.ac.una.lab1.business.strategy.ValidadorSinpeMovil;
import cr.ac.una.lab1.business.strategy.ValidadorTarjeta;
import cr.ac.una.lab1.business.strategy.ValidadorTransferencia;
import cr.ac.una.lab1.data.MetodoPago;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra el mapa de validadores de método de pago (patrón Strategy) como bean
 * de Spring para que pueda ser inyectado en {@link cr.ac.una.lab1.business.MatriculaService}.
 */
@Configuration
public class PagoStrategyConfig {

    @Bean
    public Map<MetodoPago, ValidadorMetodoPago> validadoresMetodoPago(
            ValidadorTarjeta tarjeta,
            ValidadorSinpeMovil sinpe,
            ValidadorTransferencia transferencia) {
        return Map.of(
                MetodoPago.TARJETA,        tarjeta,
                MetodoPago.SINPE_MOVIL,    sinpe,
                MetodoPago.TRANSFERENCIA,  transferencia
        );
    }
}
