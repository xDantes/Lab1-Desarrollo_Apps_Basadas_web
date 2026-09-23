package cr.ac.una.lab1;

import cr.ac.una.lab1.business.MatriculaRequestDTO;
import cr.ac.una.lab1.business.MatriculaService;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import cr.ac.una.lab1.data.Curso;
import cr.ac.una.lab1.data.CursoRepository;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.Leccion;
import cr.ac.una.lab1.data.LeccionRepository;
import cr.ac.una.lab1.data.Matricula;
import cr.ac.una.lab1.data.MatriculaRepository;
import cr.ac.una.lab1.data.MetodoPago;
import cr.ac.una.lab1.data.PagoRepository;
import cr.ac.una.lab1.data.Usuario;
import cr.ac.una.lab1.data.UsuarioRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIf;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

/**
 * Prueba de integración: verifica que el proceso multi-paso de matriculación
 * hace rollback completo cuando falla el paso de persistir el Pago.
 *
 * <h3>Escenario de rollback</h3>
 * <ol>
 *   <li>Diego (ESTUDIANTE) intenta matricularse en LESCO-102 con SINPE_MOVIL.
 *   <li>MatriculaService persiste la Matricula (paso 9).
 *   <li>El spy de {@code PagoRepository} lanza {@code RuntimeException} al llamar
 *       {@code save()} (paso 10).
 *   <li>La anotación {@code @Transactional} de MatriculaService revierte todo,
 *       incluida la Matricula del paso 9.
 *   <li>La prueba afirma que el conteo de matrículas NO aumentó.
 * </ol>
 *
 * <h3>Por qué {@code @SpyBean} y no {@code @MockBean}</h3>
 * {@code @SpyBean} delega las llamadas reales al repositorio excepto las que se
 * configuran explícitamente con {@code doThrow}. Los {@code findBy*} anteriores
 * al save siguen funcionando con datos reales de la BD.
 *
 * <h3>Por qué {@code any()} y no {@code any(Pago.class)}</h3>
 * {@code PagoRepository.save()} es genérico ({@code <S extends Pago> S save(S entity)}).
 * Tras el borrado de tipos en tiempo de ejecución, la firma real es
 * {@code Object save(Object)}, por lo que {@code any(Pago.class)} puede no
 * interceptar la llamada. {@code any()} captura cualquier argumento.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIf("cr.ac.una.lab1.DockerUtils#isDockerAvailable")
class TransaccionIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MatriculaService     matriculaService;
    @Autowired private MatriculaRepository  matriculaRepository;
    @Autowired private UsuarioRepository    usuarioRepository;
    @Autowired private CursoRepository      cursoRepository;
    @Autowired private LeccionRepository    leccionRepository;

    @SpyBean
    private PagoRepository pagoRepositorySpy;

    @BeforeEach
    void resetSpy() {
        Mockito.reset(pagoRepositorySpy);
    }

    // -----------------------------------------------------------------------
    // Prueba 9: Rollback verificado
    // -----------------------------------------------------------------------

    @Test
    @Order(9)
    @DisplayName("Prueba 9: @Transactional — fallo en paso 10 (Pago) revierte el paso 9 (Matricula)")
    void test9_RollbackVerificado_FalloEnPagoRevierteMatricula() {

        // Conteo inicial (datos semilla de Flyway)
        long matriculasAntes = matriculaRepository.count();

        // Diego (ESTUDIANTE, sembrado en V8). No tiene matrícula en LESCO-102.
        Usuario diego = usuarioRepository.findByCorreo("diego.estudiante@lescocr.cr").orElseThrow();

        // Recuperar LESCO-102 y su primera lección directamente de los repositorios
        // (evita navegar relaciones LAZY sin transacción activa).
        Curso lesco102 = cursoRepository.findByCodigo("LESCO-102")
                .orElseThrow(() -> new IllegalStateException("Curso LESCO-102 no encontrado en datos semilla"));

        List<Leccion> lecciones = leccionRepository.findByCursoIdOrderByOrdenAsc(lesco102.getId());
        assertThat(lecciones).as("LESCO-102 debe tener al menos una lección").isNotEmpty();
        Long leccionId = lecciones.get(0).getId();

        // Configurar el spy: al llamar save() en PagoRepository, lanzar excepción.
        // Se usa any() (no any(Pago.class)) porque save() es genérico y el tipo se borra en runtime.
        Mockito.doThrow(new RuntimeException("Fallo simulado en persistencia de Pago"))
               .when(pagoRepositorySpy)
               .save(any());

        MatriculaRequestDTO dto = new MatriculaRequestDTO(
                diego.getId(),
                lesco102.getId(),
                leccionId,
                MetodoPago.SINPE_MOVIL,
                "88001234"  // referencia válida: 8 dígitos
        );

        // El servicio debe propagar la excepción lanzada por el spy
        assertThatThrownBy(() -> matriculaService.matricular(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Fallo simulado en persistencia de Pago");

        // Verificar rollback: el conteo de matrículas no debe haber cambiado
        long matriculasDespues = matriculaRepository.count();
        assertThat(matriculasDespues)
                .as("@Transactional debe revertir la Matricula (paso 9) cuando el Pago (paso 10) falla. "
                        + "Antes=%d, Después=%d", matriculasAntes, matriculasDespues)
                .isEqualTo(matriculasAntes);
    }

    // -----------------------------------------------------------------------
    // Prueba 10: Reglas de negocio y patrón State
    // -----------------------------------------------------------------------

    @Test
    @Order(10)
    @DisplayName("Prueba 10: Reglas de negocio — validaciones del MatriculaService y patrón State")
    void test10_ReglasNegocioYPatronState() {

        // ── Regla 1: rol incorrecto ──────────────────────────────────────────
        // Carlos es INSTRUCTOR, no ESTUDIANTE
        Usuario carlos = usuarioRepository.findByCorreo("carlos.instructor@lescocr.cr").orElseThrow();
        MatriculaRequestDTO dtoRolInvalido = new MatriculaRequestDTO(
                carlos.getId(), 1L, 1L, MetodoPago.TARJETA, null);

        assertThatThrownBy(() -> matriculaService.matricular(dtoRolInvalido))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no tiene rol ESTUDIANTE");

        // ── Regla 2: curso inexistente ────────────────────────────────────────
        Usuario luis = usuarioRepository.findByCorreo("luis.estudiante@lescocr.cr").orElseThrow();
        MatriculaRequestDTO dtoCursoInexistente = new MatriculaRequestDTO(
                luis.getId(), 99999L, 1L, MetodoPago.TARJETA, null);

        assertThatThrownBy(() -> matriculaService.matricular(dtoCursoInexistente))
                .isInstanceOf(cr.ac.una.lab1.business.exception.EntidadNoEncontradaException.class);

        // ── Regla 3: Strategy — referencia SINPE inválida ─────────────────────
        Curso lesco102 = cursoRepository.findByCodigo("LESCO-102").orElseThrow();
        List<Leccion> lecciones = leccionRepository.findByCursoIdOrderByOrdenAsc(lesco102.getId());
        Usuario diego = usuarioRepository.findByCorreo("diego.estudiante@lescocr.cr").orElseThrow();

        MatriculaRequestDTO dtoReferenciaInvalida = new MatriculaRequestDTO(
                diego.getId(), lesco102.getId(), lecciones.get(0).getId(),
                MetodoPago.SINPE_MOVIL, "INVALIDO");   // letras, no 8 dígitos

        assertThatThrownBy(() -> matriculaService.matricular(dtoReferenciaInvalida))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("8 dígitos");

        // ── Regla 4: patrón State — transición ilegal ──────────────────────────
        // MAT-2026-000003 está CANCELADA; no se puede cancelar de nuevo.
        Matricula cancelada = matriculaRepository.findByConsecutivo("MAT-2026-000003").orElseThrow();
        assertThatThrownBy(cancelada::cancelar)
                .isInstanceOf(cr.ac.una.lab1.business.exception.CambioEstadoInvalidoException.class)
                .hasMessageContaining("CANCELADA");

        // ── Regla 5: patrón State — PENDIENTE puede activarse ──────────────────
        // MAT-2026-000002 está PENDIENTE
        Matricula pendiente = matriculaRepository.findByConsecutivo("MAT-2026-000002").orElseThrow();
        pendiente.activar(); // no debe lanzar excepción
        assertThat(pendiente.getEstado())
                .as("Después de activar(), el estado debe ser ACTIVA")
                .isEqualTo(EstadoMatricula.ACTIVA);
    }
}
