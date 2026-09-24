package cr.ac.una.lab1.business.strategy;

import static org.assertj.core.api.Assertions.assertThatCode;

import cr.ac.una.lab1.business.MatriculaRequestDTO;
import cr.ac.una.lab1.data.MetodoPago;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias puras del Strategy TARJETA: nunca exige referencia al matricular. */
class ValidadorTarjetaTest {

    private final ValidadorTarjeta validador = new ValidadorTarjeta();

    @Test
    void validar_sinReferencia_noLanzaExcepcion() {
        MatriculaRequestDTO dto = new MatriculaRequestDTO(1L, 1L, 1L, MetodoPago.TARJETA, null);
        assertThatCode(() -> validador.validar(dto)).doesNotThrowAnyException();
    }

    @Test
    void validar_conReferencia_noLanzaExcepcion() {
        MatriculaRequestDTO dto = new MatriculaRequestDTO(1L, 1L, 1L, MetodoPago.TARJETA, "cualquier-cosa");
        assertThatCode(() -> validador.validar(dto)).doesNotThrowAnyException();
    }
}
