package cr.ac.una.lab1.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cr.ac.una.lab1.business.exception.CambioEstadoInvalidoException;
import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import cr.ac.una.lab1.business.strategy.ValidadorMetodoPago;
import cr.ac.una.lab1.data.Curso;
import cr.ac.una.lab1.data.CursoRepository;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.Leccion;
import cr.ac.una.lab1.data.LeccionRepository;
import cr.ac.una.lab1.data.Matricula;
import cr.ac.una.lab1.data.MatriculaRepository;
import cr.ac.una.lab1.data.MetodoPago;
import cr.ac.una.lab1.data.Pago;
import cr.ac.una.lab1.data.PagoRepository;
import cr.ac.una.lab1.data.RolUsuario;
import cr.ac.una.lab1.data.Usuario;
import cr.ac.una.lab1.data.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Pruebas unitarias del Proceso 2 — Matriculación con Mockito:
 * los repositorios y el validador Strategy se simulan (no hay Spring context ni
 * base de datos), así que cada prueba corre en milisegundos y ejercita solo las
 * reglas de {@link MatriculaService}.
 */
@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock private MatriculaRepository matriculaRepository;
    @Mock private PagoRepository pagoRepository;
    @Mock private CursoRepository cursoRepository;
    @Mock private LeccionRepository leccionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ValidadorMetodoPago validadorTarjeta;

    private MatriculaService service;
    private Usuario estudiante;
    private Curso curso;
    private Leccion leccion;

    @BeforeEach
    void setUp() {
        Map<MetodoPago, ValidadorMetodoPago> validadores = Map.of(MetodoPago.TARJETA, validadorTarjeta);
        service = new MatriculaService(
                matriculaRepository, pagoRepository, cursoRepository, leccionRepository, usuarioRepository, validadores);

        estudiante = new Usuario("Luis Fernández Rojas", "4-4444-4444", "luis@lescocr.cr", "hash", RolUsuario.ESTUDIANTE);
        ReflectionTestUtils.setField(estudiante, "id", 4L);

        curso = new Curso("LESCO-101", "LESCO Básico I", "desc", "BASICO", 20,
                new BigDecimal("45000.00"), BigDecimal.ZERO,
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 11, 13), true);
        ReflectionTestUtils.setField(curso, "id", 1L);

        leccion = new Leccion("Alfabeto dactilológico", 1, curso, null);
        ReflectionTestUtils.setField(leccion, "id", 1L);
    }

    private MatriculaRequestDTO dtoTarjeta() {
        return new MatriculaRequestDTO(4L, 1L, 1L, MetodoPago.TARJETA, null);
    }

    // -----------------------------------------------------------------------
    // matricular() — camino feliz
    // -----------------------------------------------------------------------

    @Test
    void matricular_caminoFeliz_creaMatriculaYPagoPendientes() {
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(estudiante));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(leccionRepository.findById(1L)).thenReturn(Optional.of(leccion));
        when(matriculaRepository.findByCursoIdAndEstado(1L, EstadoMatricula.ACTIVA)).thenReturn(List.of());
        when(matriculaRepository.findByCursoIdAndEstado(1L, EstadoMatricula.PENDIENTE)).thenReturn(List.of());
        when(matriculaRepository.count()).thenReturn(3L);
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> {
            Matricula m = inv.getArgument(0);
            ReflectionTestUtils.setField(m, "id", 10L);
            return m;
        });
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 20L);
            return p;
        });

        MatriculaResponseDTO resultado = service.matricular(dtoTarjeta());

        assertThat(resultado.matriculaId()).isEqualTo(10L);
        assertThat(resultado.consecutivo())
                .isEqualTo(String.format("MAT-%d-000004", LocalDate.now().getYear()));
        assertThat(resultado.estadoMatricula()).isEqualTo(EstadoMatricula.PENDIENTE);
        assertThat(resultado.precioFinal()).isEqualByComparingTo("45000.00");
        assertThat(resultado.pagoId()).isEqualTo(20L);
        verify(validadorTarjeta).validar(dtoTarjeta());
    }

    // -----------------------------------------------------------------------
    // matricular() — caminos de regla
    // -----------------------------------------------------------------------

    @Test
    void matricular_estudianteNoExiste_lanzaEntidadNoEncontrada() {
        when(usuarioRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.matricular(dtoTarjeta()))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }

    @Test
    void matricular_usuarioNoEsEstudiante_lanzaReglaNegocio() {
        Usuario instructor = new Usuario("Carlos Jiménez Vargas", "2-2222-2222", "carlos@lescocr.cr", "hash", RolUsuario.INSTRUCTOR);
        ReflectionTestUtils.setField(instructor, "id", 4L);
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(instructor));

        assertThatThrownBy(() -> service.matricular(dtoTarjeta()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no tiene rol ESTUDIANTE");
    }

    @Test
    void matricular_cursoNoExiste_lanzaEntidadNoEncontrada() {
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(estudiante));
        when(cursoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.matricular(dtoTarjeta()))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }

    @Test
    void matricular_cursoNoPublicado_lanzaReglaNegocio() {
        Curso cursoSinPublicar = new Curso("LESCO-201", "LESCO Laboral", "desc", "AVANZADO", 12,
                new BigDecimal("60000.00"), new BigDecimal("15"),
                LocalDate.of(2026, 10, 5), LocalDate.of(2027, 1, 11), false);
        ReflectionTestUtils.setField(cursoSinPublicar, "id", 1L);

        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(estudiante));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoSinPublicar));

        assertThatThrownBy(() -> service.matricular(dtoTarjeta()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no está publicado");
    }

    @Test
    void matricular_leccionNoExiste_lanzaEntidadNoEncontrada() {
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(estudiante));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(leccionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.matricular(dtoTarjeta()))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }

    @Test
    void matricular_leccionNoPerteneceAlCurso_lanzaReglaNegocio() {
        Curso otroCurso = new Curso("LESCO-102", "LESCO Intermedio", "desc", "INTERMEDIO", 15,
                new BigDecimal("55000.00"), new BigDecimal("10"),
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 12, 4), true);
        ReflectionTestUtils.setField(otroCurso, "id", 2L);
        Leccion leccionDeOtroCurso = new Leccion("Estructura gramatical básica", 1, otroCurso, null);
        ReflectionTestUtils.setField(leccionDeOtroCurso, "id", 1L);

        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(estudiante));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(leccionRepository.findById(1L)).thenReturn(Optional.of(leccionDeOtroCurso));

        assertThatThrownBy(() -> service.matricular(dtoTarjeta()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no pertenece al curso");
    }

    @Test
    void matricular_sinCupoDisponible_lanzaReglaNegocio() {
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(estudiante));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(leccionRepository.findById(1L)).thenReturn(Optional.of(leccion));
        // curso.cupoTotal = 20: llenamos las 20 plazas con matrículas ACTIVA.
        when(matriculaRepository.findByCursoIdAndEstado(1L, EstadoMatricula.ACTIVA))
                .thenReturn(Collections.nCopies(20, mock(Matricula.class)));

        assertThatThrownBy(() -> service.matricular(dtoTarjeta()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no tiene cupo disponible");
    }

    @Test
    void matricular_estudianteYaTieneMatriculaVigenteEnElCurso_lanzaReglaNegocio() {
        Matricula matriculaExistente = new Matricula("MAT-2026-000001", estudiante, leccion,
                EstadoMatricula.ACTIVA, new BigDecimal("45000.00"));

        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(estudiante));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(leccionRepository.findById(1L)).thenReturn(Optional.of(leccion));
        when(matriculaRepository.findByCursoIdAndEstado(1L, EstadoMatricula.ACTIVA))
                .thenReturn(List.of(matriculaExistente));

        assertThatThrownBy(() -> service.matricular(dtoTarjeta()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya tiene una matrícula vigente");
    }

    @Test
    void matricular_metodoPagoNoSoportado_lanzaReglaNegocio() {
        // El mapa de validadores del servicio solo conoce TARJETA (ver setUp); SINPE_MOVIL no está.
        MatriculaRequestDTO dtoSinValidador = new MatriculaRequestDTO(4L, 1L, 1L, MetodoPago.SINPE_MOVIL, "88001234");

        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(estudiante));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(leccionRepository.findById(1L)).thenReturn(Optional.of(leccion));
        when(matriculaRepository.findByCursoIdAndEstado(1L, EstadoMatricula.ACTIVA)).thenReturn(List.of());
        when(matriculaRepository.findByCursoIdAndEstado(1L, EstadoMatricula.PENDIENTE)).thenReturn(List.of());

        assertThatThrownBy(() -> service.matricular(dtoSinValidador))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Método de pago no soportado");
    }

    @Test
    void matricular_validadorRechazaReferencia_propagaLaExcepcion() {
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(estudiante));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(leccionRepository.findById(1L)).thenReturn(Optional.of(leccion));
        when(matriculaRepository.findByCursoIdAndEstado(1L, EstadoMatricula.ACTIVA)).thenReturn(List.of());
        when(matriculaRepository.findByCursoIdAndEstado(1L, EstadoMatricula.PENDIENTE)).thenReturn(List.of());
        org.mockito.Mockito.doThrow(new ReglaNegocioException("referencia inválida"))
                .when(validadorTarjeta).validar(any());

        assertThatThrownBy(() -> service.matricular(dtoTarjeta()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("referencia inválida");
    }

    // -----------------------------------------------------------------------
    // aprobarPago() — patrón State
    // -----------------------------------------------------------------------

    private Matricula matriculaPersistida(EstadoMatricula estado) {
        Matricula m = new Matricula("MAT-2026-000001", estudiante, leccion, estado, new BigDecimal("45000.00"));
        ReflectionTestUtils.setField(m, "id", 1L);
        return m;
    }

    @Test
    void aprobarPago_desdePendiente_transicionaAActiva() {
        Matricula matricula = matriculaPersistida(EstadoMatricula.PENDIENTE);
        when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));
        when(pagoRepository.findByMatriculaId(1L)).thenReturn(Optional.empty());
        when(matriculaRepository.save(matricula)).thenReturn(matricula);

        MatriculaResponseDTO resultado = service.aprobarPago(1L);

        assertThat(resultado.estadoMatricula()).isEqualTo(EstadoMatricula.ACTIVA);
    }

    @Test
    void aprobarPago_matriculaNoExiste_lanzaEntidadNoEncontrada() {
        when(matriculaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.aprobarPago(1L))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }

    @Test
    void aprobarPago_desdeActiva_lanzaCambioEstadoInvalido() {
        Matricula matricula = matriculaPersistida(EstadoMatricula.ACTIVA);
        when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));

        assertThatThrownBy(() -> service.aprobarPago(1L))
                .isInstanceOf(CambioEstadoInvalidoException.class);
    }

    @Test
    void aprobarPago_desdeCancelada_lanzaCambioEstadoInvalido() {
        Matricula matricula = matriculaPersistida(EstadoMatricula.CANCELADA);
        when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));

        assertThatThrownBy(() -> service.aprobarPago(1L))
                .isInstanceOf(CambioEstadoInvalidoException.class);
    }

    // -----------------------------------------------------------------------
    // cancelarMatricula() — patrón State
    // -----------------------------------------------------------------------

    @Test
    void cancelarMatricula_desdePendiente_transicionaACancelada() {
        Matricula matricula = matriculaPersistida(EstadoMatricula.PENDIENTE);
        when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));
        when(pagoRepository.findByMatriculaId(1L)).thenReturn(Optional.empty());
        when(matriculaRepository.save(matricula)).thenReturn(matricula);

        MatriculaResponseDTO resultado = service.cancelarMatricula(1L);

        assertThat(resultado.estadoMatricula()).isEqualTo(EstadoMatricula.CANCELADA);
    }

    @Test
    void cancelarMatricula_desdeActiva_transicionaACancelada() {
        Matricula matricula = matriculaPersistida(EstadoMatricula.ACTIVA);
        when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));
        when(pagoRepository.findByMatriculaId(1L)).thenReturn(Optional.empty());
        when(matriculaRepository.save(matricula)).thenReturn(matricula);

        MatriculaResponseDTO resultado = service.cancelarMatricula(1L);

        assertThat(resultado.estadoMatricula()).isEqualTo(EstadoMatricula.CANCELADA);
    }

    @Test
    void cancelarMatricula_desdeCancelada_lanzaCambioEstadoInvalido() {
        Matricula matricula = matriculaPersistida(EstadoMatricula.CANCELADA);
        when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));

        assertThatThrownBy(() -> service.cancelarMatricula(1L))
                .isInstanceOf(CambioEstadoInvalidoException.class);
    }
}
