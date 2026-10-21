package cr.ac.una.lab1.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import cr.ac.una.lab1.business.exception.AccesoNoAutorizadoException;
import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.EstadoPago;
import cr.ac.una.lab1.data.Matricula;
import cr.ac.una.lab1.data.MetodoPago;
import cr.ac.una.lab1.data.Pago;
import cr.ac.una.lab1.data.PagoRepository;
import cr.ac.una.lab1.data.RolUsuario;
import cr.ac.una.lab1.data.Usuario;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Pruebas unitarias de {@link PagoService} con Mockito. */
@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock private PagoRepository pagoRepository;

    private PagoService service;
    private Usuario luis;
    private Usuario paola;

    @BeforeEach
    void setUp() {
        service = new PagoService(pagoRepository);

        luis = new Usuario("Luis Fernández Rojas", "4-4444-4444", "luis@lescocr.cr", "hash", RolUsuario.ESTUDIANTE);
        ReflectionTestUtils.setField(luis, "id", 4L);

        paola = new Usuario("Paola Méndez Castro", "5-5555-5555", "paola@lescocr.cr", "hash", RolUsuario.ESTUDIANTE);
        ReflectionTestUtils.setField(paola, "id", 5L);
    }

    private Pago pagoDe(Usuario titular, Long id) {
        Matricula m = new Matricula("MAT-2026-00000" + id, titular, null, EstadoMatricula.ACTIVA, new BigDecimal("45000.00"));
        ReflectionTestUtils.setField(m, "id", id);
        Pago p = new Pago(m, new BigDecimal("45000.00"), MetodoPago.TARJETA, null, EstadoPago.APROBADO);
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    // -----------------------------------------------------------------------
    // listar() — cuál repositorio usa según el filtro
    // -----------------------------------------------------------------------

    @Test
    void listar_porEstado_usaFindByEstadoConDetalleCompleto() {
        when(pagoRepository.findByEstadoConDetalleCompleto(EstadoPago.APROBADO))
                .thenReturn(List.of(pagoDe(luis, 1L)));

        List<PagoResponseDTO> resultado = service.listar(EstadoPago.APROBADO, null, null, "luis@lescocr.cr", true);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).estado()).isEqualTo(EstadoPago.APROBADO);
    }

    @Test
    void listar_porMetodo_usaFindByMetodo() {
        when(pagoRepository.findByMetodo(MetodoPago.TARJETA)).thenReturn(List.of(pagoDe(luis, 1L)));

        List<PagoResponseDTO> resultado = service.listar(null, MetodoPago.TARJETA, null, "luis@lescocr.cr", true);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).metodo()).isEqualTo(MetodoPago.TARJETA);
    }

    @Test
    void listar_porMatriculaId_usaFindByMatriculaId() {
        when(pagoRepository.findByMatriculaId(1L)).thenReturn(Optional.of(pagoDe(luis, 1L)));

        List<PagoResponseDTO> resultado = service.listar(null, null, 1L, "luis@lescocr.cr", true);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).matriculaId()).isEqualTo(1L);
    }

    @Test
    void listar_porMatriculaIdSinPago_devuelveListaVacia() {
        when(pagoRepository.findByMatriculaId(99L)).thenReturn(Optional.empty());

        List<PagoResponseDTO> resultado = service.listar(null, null, 99L, "luis@lescocr.cr", true);

        assertThat(resultado).isEmpty();
    }

    @Test
    void listar_sinFiltros_usaFindAll() {
        when(pagoRepository.findAll()).thenReturn(List.of(pagoDe(luis, 1L), pagoDe(paola, 2L)));

        List<PagoResponseDTO> resultado = service.listar(null, null, null, "luis@lescocr.cr", true);

        assertThat(resultado).hasSize(2);
    }

    // -----------------------------------------------------------------------
    // listar() — verificación de propiedad (OWASP API1)
    // -----------------------------------------------------------------------

    @Test
    void listar_estudiante_soloVeSusPropiosPagos() {
        when(pagoRepository.findAll()).thenReturn(List.of(pagoDe(luis, 1L), pagoDe(paola, 2L)));

        List<PagoResponseDTO> resultado = service.listar(null, null, null, "luis@lescocr.cr", false);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).correoUsuario()).isEqualTo("luis@lescocr.cr");
    }

    @Test
    void listar_administrador_veTodosLosPagos() {
        when(pagoRepository.findAll()).thenReturn(List.of(pagoDe(luis, 1L), pagoDe(paola, 2L)));

        List<PagoResponseDTO> resultado = service.listar(null, null, null, "admin@lescocr.cr", true);

        assertThat(resultado).hasSize(2);
    }

    // -----------------------------------------------------------------------
    // obtenerPorId()
    // -----------------------------------------------------------------------

    @Test
    void obtenerPorId_propietario_retornaDTO() {
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoDe(luis, 1L)));

        PagoResponseDTO dto = service.obtenerPorId(1L, "luis@lescocr.cr", false);

        assertThat(dto.id()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_administrador_retornaCualquierPago() {
        when(pagoRepository.findById(2L)).thenReturn(Optional.of(pagoDe(paola, 2L)));

        PagoResponseDTO dto = service.obtenerPorId(2L, "admin@lescocr.cr", true);

        assertThat(dto.correoUsuario()).isEqualTo("paola@lescocr.cr");
    }

    @Test
    void obtenerPorId_ajeno_lanzaAccesoNoAutorizado() {
        when(pagoRepository.findById(2L)).thenReturn(Optional.of(pagoDe(paola, 2L)));

        assertThatThrownBy(() -> service.obtenerPorId(2L, "luis@lescocr.cr", false))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    @Test
    void obtenerPorId_noExiste_lanzaEntidadNoEncontrada() {
        when(pagoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L, "luis@lescocr.cr", false))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }
}
