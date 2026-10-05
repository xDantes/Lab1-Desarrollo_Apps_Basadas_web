package cr.ac.una.lab1.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import cr.ac.una.lab1.data.Curso;
import cr.ac.una.lab1.data.CursoRepository;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.Instructor;
import cr.ac.una.lab1.data.Leccion;
import cr.ac.una.lab1.data.LeccionRepository;
import cr.ac.una.lab1.data.Matricula;
import cr.ac.una.lab1.data.MatriculaRepository;
import cr.ac.una.lab1.data.mongo.RecursoMultimedia;
import cr.ac.una.lab1.data.mongo.RecursoMultimediaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

/*
  Pruebas unitarias del Proceso 1 — Publicación y habilitación de un
  curso con Mockito: repositorios simulados, sin base de datos.
 */
@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock private CursoRepository             cursoRepository;
    @Mock private MatriculaRepository         matriculaRepository;
    @Mock private LeccionRepository           leccionRepository;
    @Mock private RecursoMultimediaRepository recursoMultimediaRepository;

    private CursoService service;

    /** DTO mínimo de publicación: cualquier administradorId válido. */
    private static final PublicarCursoRequestDTO REQ = new PublicarCursoRequestDTO(1L);

    @BeforeEach
    void setUp() {
        service = new CursoService(
                cursoRepository, matriculaRepository,
                leccionRepository, recursoMultimediaRepository);
    }

    // -----------------------------------------------------------------------
    // Helpers de construcción de entidades
    // -----------------------------------------------------------------------

    /** Curso no publicado con fechas coherentes (inicio < fin). */
    private Curso cursoNoPublicado() {
        Curso curso = new Curso("LESCO-201", "LESCO para el Ámbito Laboral", "desc", "AVANZADO", 12,
                new BigDecimal("60000.00"), new BigDecimal("15"),
                LocalDate.of(2026, 10, 5), LocalDate.of(2027, 1, 11), false);
        ReflectionTestUtils.setField(curso, "id", 3L);
        return curso;
    }

    /** Lección con instructor asignado (mockito mock). */
    private Leccion leccionConInstructor(Curso curso) {
        Instructor instructor = mock(Instructor.class);
        Leccion l = new Leccion("Vocabulario laboral", 1, curso, instructor);
        ReflectionTestUtils.setField(l, "id", 10L);
        return l;
    }

    // -----------------------------------------------------------------------
    // listarCatalogoPublico()
    // -----------------------------------------------------------------------

    @Test
    void listarCatalogoPublico_calculaCuposDisponiblesYPrecioFinalConDescuento() {
        Curso curso = new Curso("LESCO-102", "LESCO Intermedio", "desc", "INTERMEDIO", 15,
                new BigDecimal("55000.00"), new BigDecimal("10"),
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 12, 4), true);
        ReflectionTestUtils.setField(curso, "id", 2L);

        when(cursoRepository.findByPublicadoTrue()).thenReturn(List.of(curso));
        // 3 matrículas ACTIVA de las 15 plazas
        when(matriculaRepository.findByCursoIdAndEstado(2L, EstadoMatricula.ACTIVA))
                .thenReturn(List.of(mock(Matricula.class), mock(Matricula.class), mock(Matricula.class)));

        List<CursoCatalogoDTO> catalogo = service.listarCatalogoPublico();

        assertThat(catalogo).hasSize(1);
        CursoCatalogoDTO dto = catalogo.get(0);
        assertThat(dto.cuposDisponibles()).isEqualTo(12);
        assertThat(dto.precioFinal()).isEqualByComparingTo("49500.00");
    }

    // -----------------------------------------------------------------------
    // publicarCurso() — camino feliz (todas las reglas pasan)
    // -----------------------------------------------------------------------

    @Test
    void publicarCurso_conTodasLasReglasValidas_quedaPublicado() {
        Curso curso = cursoNoPublicado();
        Leccion leccion = leccionConInstructor(curso);

        when(cursoRepository.findById(3L)).thenReturn(Optional.of(curso));
        when(cursoRepository.existsByCodigoAndPublicadoTrueAndIdNot("LESCO-201", 3L)).thenReturn(false);
        when(leccionRepository.findByCursoIdOrderByOrdenAsc(3L)).thenReturn(List.of(leccion));
        when(recursoMultimediaRepository.findByLeccionIdOrderByOrdenAsc(10L))
                .thenReturn(List.of(mock(RecursoMultimedia.class)));
        when(matriculaRepository.findByCursoIdAndEstado(3L, EstadoMatricula.ACTIVA)).thenReturn(List.of());
        when(cursoRepository.save(curso)).thenReturn(curso);

        CursoCatalogoDTO resultado = service.publicarCurso(3L, REQ);

        assertThat(curso.isPublicado()).isTrue();
        assertThat(resultado.codigo()).isEqualTo("LESCO-201");
        assertThat(resultado.cuposDisponibles()).isEqualTo(12);
    }

    // -----------------------------------------------------------------------
    // publicarCurso() — Regla 1: curso no existe
    // -----------------------------------------------------------------------

    @Test
    void publicarCurso_cursoNoExiste_lanzaEntidadNoEncontrada() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publicarCurso(99L, REQ))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }

    // -----------------------------------------------------------------------
    // publicarCurso() — Regla 2: ya publicado
    // -----------------------------------------------------------------------

    @Test
    void publicarCurso_yaPublicado_lanzaReglaNegocio() {
        Curso cursoPublicado = new Curso("LESCO-101", "LESCO Básico I", "desc", "BASICO", 20,
                new BigDecimal("45000.00"), BigDecimal.ZERO,
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 11, 13), true);
        ReflectionTestUtils.setField(cursoPublicado, "id", 1L);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoPublicado));

        assertThatThrownBy(() -> service.publicarCurso(1L, REQ))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya está publicado");
    }

    // -----------------------------------------------------------------------
    // publicarCurso() — Regla 3: fechas incoherentes (inicio >= fin)
    // -----------------------------------------------------------------------

    @Test
    void publicarCurso_fechaInicioIgualOPosteriorAFin_lanzaReglaNegocio() {
        Curso cursoFechasMal = new Curso("LESCO-201", "LESCO Avanzado", "desc", "AVANZADO", 12,
                new BigDecimal("60000.00"), new BigDecimal("15"),
                LocalDate.of(2027, 1, 11), LocalDate.of(2026, 10, 5), // inicio > fin
                false);
        ReflectionTestUtils.setField(cursoFechasMal, "id", 3L);
        when(cursoRepository.findById(3L)).thenReturn(Optional.of(cursoFechasMal));

        assertThatThrownBy(() -> service.publicarCurso(3L, REQ))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("fecha de inicio");
    }

    // -----------------------------------------------------------------------
    // publicarCurso() — Regla 4: duplicado publicado con el mismo código
    // -----------------------------------------------------------------------

    @Test
    void publicarCurso_duplicadoPublicadoMismoCodigo_lanzaReglaNegocio() {
        Curso curso = cursoNoPublicado();
        when(cursoRepository.findById(3L)).thenReturn(Optional.of(curso));
        when(cursoRepository.existsByCodigoAndPublicadoTrueAndIdNot("LESCO-201", 3L)).thenReturn(true);

        assertThatThrownBy(() -> service.publicarCurso(3L, REQ))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("LESCO-201");
    }

    // -----------------------------------------------------------------------
    // publicarCurso() — Regla 5: sin lecciones
    // -----------------------------------------------------------------------

    @Test
    void publicarCurso_sinLecciones_lanzaReglaNegocio() {
        Curso curso = cursoNoPublicado();
        when(cursoRepository.findById(3L)).thenReturn(Optional.of(curso));
        when(cursoRepository.existsByCodigoAndPublicadoTrueAndIdNot("LESCO-201", 3L)).thenReturn(false);
        when(leccionRepository.findByCursoIdOrderByOrdenAsc(3L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.publicarCurso(3L, REQ))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no tiene ninguna lección asignada");
    }

    // -----------------------------------------------------------------------
    // publicarCurso() — Regla 6: lección sin instructor
    // -----------------------------------------------------------------------

    @Test
    void publicarCurso_leccionSinInstructor_lanzaReglaNegocio() {
        Curso curso = cursoNoPublicado();
        // Leccion creada con instructor = null
        Leccion sinInstructor = new Leccion("Vocabulario laboral", 1, curso, null);
        ReflectionTestUtils.setField(sinInstructor, "id", 10L);

        when(cursoRepository.findById(3L)).thenReturn(Optional.of(curso));
        when(cursoRepository.existsByCodigoAndPublicadoTrueAndIdNot("LESCO-201", 3L)).thenReturn(false);
        when(leccionRepository.findByCursoIdOrderByOrdenAsc(3L)).thenReturn(List.of(sinInstructor));

        assertThatThrownBy(() -> service.publicarCurso(3L, REQ))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("sin instructor");
    }

    // -----------------------------------------------------------------------
    // publicarCurso() — Regla 7: ninguna lección con recurso multimedia
    // -----------------------------------------------------------------------

    @Test
    void publicarCurso_sinRecursoMultimedia_lanzaReglaNegocio() {
        Curso curso = cursoNoPublicado();
        Leccion leccion = leccionConInstructor(curso);

        when(cursoRepository.findById(3L)).thenReturn(Optional.of(curso));
        when(cursoRepository.existsByCodigoAndPublicadoTrueAndIdNot("LESCO-201", 3L)).thenReturn(false);
        when(leccionRepository.findByCursoIdOrderByOrdenAsc(3L)).thenReturn(List.of(leccion));
        when(recursoMultimediaRepository.findByLeccionIdOrderByOrdenAsc(anyLong())).thenReturn(List.of());

        assertThatThrownBy(() -> service.publicarCurso(3L, REQ))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("recursos multimedia");
    }

    // -----------------------------------------------------------------------
    // obtenerPorId() / buscar() — Laboratorio 5: lectura por id y colección paginada
    // -----------------------------------------------------------------------

    @Test
    void obtenerPorId_existente_retornaDTO() {
        Curso curso = cursoNoPublicado();
        when(cursoRepository.findById(3L)).thenReturn(Optional.of(curso));
        when(matriculaRepository.findByCursoIdAndEstado(3L, EstadoMatricula.ACTIVA)).thenReturn(List.of());

        CursoCatalogoDTO dto = service.obtenerPorId(3L);

        assertThat(dto.codigo()).isEqualTo("LESCO-201");
    }

    @Test
    void obtenerPorId_noExiste_lanzaEntidadNoEncontrada() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }

    @Test
    void buscar_delegaEnLaSpecificationYMapeaLaPagina() {
        Curso curso = cursoNoPublicado();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Curso> pagina = new PageImpl<>(List.of(curso), pageable, 1);

        when(cursoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);
        when(matriculaRepository.findByCursoIdAndEstado(3L, EstadoMatricula.ACTIVA)).thenReturn(List.of());

        Page<CursoCatalogoDTO> resultado = service.buscar(null, "AVANZADO", null, false, null, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).codigo()).isEqualTo("LESCO-201");
    }
}
