package cr.ac.una.lab1.business.strategy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cr.ac.una.lab1.business.MatriculaRequestDTO;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import cr.ac.una.lab1.data.MetodoPago;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias puras (sin mocks: no hay dependencias que simular) del Strategy SINPE_MOVIL. */
class ValidadorSinpeMovilTest {

    private final ValidadorSinpeMovil validador = new ValidadorSinpeMovil();

    private MatriculaRequestDTO dtoConReferencia(String referencia) {
        return new MatriculaRequestDTO(1L, 1L, 1L, MetodoPago.SINPE_MOVIL, referencia);
    }

    @Test
    void validar_referenciaDeOchoDigitos_noLanzaExcepcion() {
        assertThatCode(() -> validador.validar(dtoConReferencia("88001234"))).doesNotThrowAnyException();
    }

    @Test
    void validar_referenciaNula_lanzaReglaNegocio() {
        assertThatThrownBy(() -> validador.validar(dtoConReferencia(null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("requiere una referencia");
    }

    @Test
    void validar_referenciaConMenosDeOchoDigitos_lanzaReglaNegocio() {
        assertThatThrownBy(() -> validador.validar(dtoConReferencia("123")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("8 dígitos");
    }
}
