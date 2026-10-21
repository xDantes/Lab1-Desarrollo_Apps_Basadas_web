package cr.ac.una.lab1.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.data.Instructor;
import cr.ac.una.lab1.data.InstructorRepository;
import cr.ac.una.lab1.data.RolUsuario;
import cr.ac.una.lab1.data.Usuario;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Pruebas unitarias de {@link InstructorService} con Mockito. */
@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock private InstructorRepository instructorRepository;

    private InstructorService service;

    @BeforeEach
    void setUp() {
        service = new InstructorService(instructorRepository);
    }

    private Instructor instructorConUsuario() {
        Usuario carlos = new Usuario("Carlos Jiménez Vargas", "2-2222-2222", "carlos.instructor@lescocr.cr", "hash", RolUsuario.INSTRUCTOR);
        ReflectionTestUtils.setField(carlos, "id", 2L);
        return new Instructor(carlos, "Lengua de señas para principiantes");
    }

    @Test
    void listar_mapeaUsuarioDeCadaInstructor() {
        when(instructorRepository.findAllWithUsuario()).thenReturn(List.of(instructorConUsuario()));

        List<InstructorResponseDTO> resultado = service.listar();

        assertThat(resultado).hasSize(1);
        InstructorResponseDTO dto = resultado.get(0);
        assertThat(dto.usuarioId()).isEqualTo(2L);
        assertThat(dto.nombre()).isEqualTo("Carlos Jiménez Vargas");
        assertThat(dto.correo()).isEqualTo("carlos.instructor@lescocr.cr");
        assertThat(dto.especialidad()).isEqualTo("Lengua de señas para principiantes");
    }

    @Test
    void obtenerPorId_existente_retornaDTO() {
        when(instructorRepository.findById(2L)).thenReturn(Optional.of(instructorConUsuario()));

        InstructorResponseDTO dto = service.obtenerPorId(2L);

        assertThat(dto.nombre()).isEqualTo("Carlos Jiménez Vargas");
    }

    @Test
    void obtenerPorId_noExiste_lanzaEntidadNoEncontrada() {
        when(instructorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }

    @Test
    void obtenerPorId_sinUsuarioCargado_dejaCamposDeUsuarioEnNulo() {
        // Defiende el caso l.getUsuario() == null del mapeo (p. ej. consulta sin JOIN FETCH).
        Instructor instructorSinUsuario = mock(Instructor.class);
        when(instructorSinUsuario.getUsuarioId()).thenReturn(5L);
        when(instructorSinUsuario.getUsuario()).thenReturn(null);
        when(instructorSinUsuario.getEspecialidad()).thenReturn("Gramática de LESCO");
        when(instructorRepository.findById(5L)).thenReturn(Optional.of(instructorSinUsuario));

        InstructorResponseDTO dto = service.obtenerPorId(5L);

        assertThat(dto.usuarioId()).isEqualTo(5L);
        assertThat(dto.nombre()).isNull();
        assertThat(dto.identificacion()).isNull();
        assertThat(dto.correo()).isNull();
        assertThat(dto.especialidad()).isEqualTo("Gramática de LESCO");
    }
}
