package cr.ac.una.lab1.business.strategy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cr.ac.una.lab1.business.MatriculaRequestDTO;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import cr.ac.una.lab1.data.MetodoPago;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias puras del Strategy TRANSFERENCIA. */
class ValidadorTransferenciaTest {

    private final ValidadorTransferencia validador = new ValidadorTransferencia();

    private MatriculaRequestDTO dtoConReferencia(String referencia) {
        return new MatriculaRequestDTO(1L, 1L, 1L, MetodoPago.TRANSFERENCIA, referencia);
    }

    @Test
    void validar_referenciaAlfanumericaValida_noLanzaExcepcion() {
        assertThatCode(() -> validador.validar(dtoConReferencia("BNCR20261001X9"))).doesNotThrowAnyException();
    }

    @Test
    void validar_referenciaVacia_lanzaReglaNegocio() {
        assertThatThrownBy(() -> validador.validar(dtoConReferencia("")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("requiere un código de referencia");
    }

    @Test
    void validar_referenciaConEspacios_lanzaReglaNegocio() {
        assertThatThrownBy(() -> validador.validar(dtoConReferencia("BNCR 2026 1001")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("alfanumérica");
    }
}
