package cr.ac.una.lab1.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import cr.ac.una.lab1.data.Curso;
import cr.ac.una.lab1.data.CursoRepository;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.Leccion;
import cr.ac.una.lab1.data.LeccionRepository;
import cr.ac.una.lab1.data.Matricula;
import cr.ac.una.lab1.data.MatriculaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/*
  Pruebas unitarias del Proceso 1 — Publicación y habilitación de un
  curso con Mockito: repositorios simulados, sin base de datos.
 */
@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock private CursoRepository cursoRepository;
    @Mock private MatriculaRepository matriculaRepository;
    @Mock private LeccionRepository leccionRepository;

    private CursoService service;

    @BeforeEach
    void setUp() {
        service = new CursoService(cursoRepository, matriculaRepository, leccionRepository);
    }

    private Curso cursoNoPublicado() {
        Curso curso = new Curso("LESCO-201", "LESCO para el Ámbito Laboral", "desc", "AVANZADO", 12,
                new BigDecimal("60000.00"), new BigDecimal("15"),
                LocalDate.of(2026, 10, 5), LocalDate.of(2027, 1, 11), false);
        ReflectionTestUtils.setField(curso, "id", 3L);
        return curso;
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
    // publicarCurso() — camino feliz
    // -----------------------------------------------------------------------

    @Test
    void publicarCurso_conLecciones_quedaPublicado() {
        Curso curso = cursoNoPublicado();
        Leccion leccion = new Leccion("Vocabulario laboral", 1, curso, null);

        when(cursoRepository.findById(3L)).thenReturn(Optional.of(curso));
        when(leccionRepository.findByCursoIdOrderByOrdenAsc(3L)).thenReturn(List.of(leccion));
        when(matriculaRepository.findByCursoIdAndEstado(3L, EstadoMatricula.ACTIVA)).thenReturn(List.of());
        when(cursoRepository.save(curso)).thenReturn(curso);

        CursoCatalogoDTO resultado = service.publicarCurso(3L);

        assertThat(curso.isPublicado()).isTrue();
        assertThat(resultado.codigo()).isEqualTo("LESCO-201");
        assertThat(resultado.cuposDisponibles()).isEqualTo(12);
    }

    // -----------------------------------------------------------------------
    // publicarCurso() — caminos de regla
    // -----------------------------------------------------------------------

    @Test
    void publicarCurso_cursoNoExiste_lanzaEntidadNoEncontrada() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publicarCurso(99L))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }

    @Test
    void publicarCurso_yaPublicado_lanzaReglaNegocio() {
        Curso cursoPublicado = new Curso("LESCO-101", "LESCO Básico I", "desc", "BASICO", 20,
                new BigDecimal("45000.00"), BigDecimal.ZERO,
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 11, 13), true);
        ReflectionTestUtils.setField(cursoPublicado, "id", 1L);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoPublicado));

        assertThatThrownBy(() -> service.publicarCurso(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya está publicado");
    }

    @Test
    void publicarCurso_sinLecciones_lanzaReglaNegocio() {
        Curso curso = cursoNoPublicado();
        when(cursoRepository.findById(3L)).thenReturn(Optional.of(curso));
        when(leccionRepository.findByCursoIdOrderByOrdenAsc(3L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.publicarCurso(3L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no tiene ninguna lección asignada");
    }
}
