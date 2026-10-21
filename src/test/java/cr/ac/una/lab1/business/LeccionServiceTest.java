package cr.ac.una.lab1.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.data.Curso;
import cr.ac.una.lab1.data.Instructor;
import cr.ac.una.lab1.data.Leccion;
import cr.ac.una.lab1.data.LeccionRepository;
import cr.ac.una.lab1.data.RolUsuario;
import cr.ac.una.lab1.data.Usuario;
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

/** Pruebas unitarias de {@link LeccionService} con Mockito. */
@ExtendWith(MockitoExtension.class)
class LeccionServiceTest {

    @Mock private LeccionRepository leccionRepository;

    private LeccionService service;
    private Curso curso;

    @BeforeEach
    void setUp() {
        service = new LeccionService(leccionRepository);
        curso = new Curso("LESCO-101", "LESCO Básico I", "desc", "BASICO", 20,
                new BigDecimal("45000.00"), BigDecimal.ZERO,
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 11, 13), true);
        ReflectionTestUtils.setField(curso, "id", 1L);
    }

    private Leccion leccionConInstructor() {
        Usuario carlos = new Usuario("Carlos Jiménez Vargas", "2-2222-2222", "carlos.instructor@lescocr.cr", "hash", RolUsuario.INSTRUCTOR);
        ReflectionTestUtils.setField(carlos, "id", 2L);
        Instructor instructor = new Instructor(carlos, "Lengua de señas para principiantes");
        Leccion l = new Leccion("Alfabeto dactilológico", 1, curso, instructor);
        ReflectionTestUtils.setField(l, "id", 1L);
        return l;
    }

    @Test
    void listar_conCursoId_usaFindByCursoIdConDetalle() {
        when(leccionRepository.findByCursoIdConDetalle(1L)).thenReturn(List.of(leccionConInstructor()));

        List<LeccionDetalleResponseDTO> resultado = service.listar(1L);

        assertThat(resultado).hasSize(1);
        LeccionDetalleResponseDTO dto = resultado.get(0);
        assertThat(dto.titulo()).isEqualTo("Alfabeto dactilológico");
        assertThat(dto.codigoCurso()).isEqualTo("LESCO-101");
        assertThat(dto.instructorId()).isEqualTo(2L);
        assertThat(dto.nombreInstructor()).isEqualTo("Carlos Jiménez Vargas");
        verify(leccionRepository, never()).findAll();
    }

    @Test
    void listar_sinCursoId_usaFindAll() {
        when(leccionRepository.findAll()).thenReturn(List.of(leccionConInstructor()));

        List<LeccionDetalleResponseDTO> resultado = service.listar(null);

        assertThat(resultado).hasSize(1);
        verify(leccionRepository, never()).findByCursoIdConDetalle(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void obtenerPorId_existente_retornaDTO() {
        when(leccionRepository.findById(1L)).thenReturn(Optional.of(leccionConInstructor()));

        LeccionDetalleResponseDTO dto = service.obtenerPorId(1L);

        assertThat(dto.titulo()).isEqualTo("Alfabeto dactilológico");
        assertThat(dto.cursoId()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_noExiste_lanzaEntidadNoEncontrada() {
        when(leccionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }

    @Test
    void obtenerPorId_sinInstructorAsignado_dejaCamposDeInstructorEnNulo() {
        Leccion sinInstructor = new Leccion("Vocabulario laboral", 1, curso, null);
        ReflectionTestUtils.setField(sinInstructor, "id", 3L);
        when(leccionRepository.findById(3L)).thenReturn(Optional.of(sinInstructor));

        LeccionDetalleResponseDTO dto = service.obtenerPorId(3L);

        assertThat(dto.instructorId()).isNull();
        assertThat(dto.nombreInstructor()).isNull();
        assertThat(dto.cursoId()).isEqualTo(1L);
    }
}
