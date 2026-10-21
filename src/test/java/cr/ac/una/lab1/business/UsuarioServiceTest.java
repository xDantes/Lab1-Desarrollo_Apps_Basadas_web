package cr.ac.una.lab1.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.data.RolUsuario;
import cr.ac.una.lab1.data.Usuario;
import cr.ac.una.lab1.data.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Pruebas unitarias de {@link UsuarioService} con Mockito. */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository);
    }

    private Usuario usuario(String nombre, RolUsuario rol, Long id) {
        Usuario u = new Usuario(nombre, id + "-0000-0000", nombre.toLowerCase() + "@lescocr.cr", "hash", rol);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    @Test
    void listar_conRol_usaFindByRol() {
        when(usuarioRepository.findByRol(RolUsuario.ESTUDIANTE))
                .thenReturn(List.of(usuario("Luis", RolUsuario.ESTUDIANTE, 4L)));

        List<UsuarioResponseDTO> resultado = service.listar(RolUsuario.ESTUDIANTE);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).rol()).isEqualTo(RolUsuario.ESTUDIANTE);
        verify(usuarioRepository, never()).findAll();
    }

    @Test
    void listar_sinRol_usaFindAll() {
        when(usuarioRepository.findAll()).thenReturn(List.of(
                usuario("Luis", RolUsuario.ESTUDIANTE, 4L),
                usuario("Carlos", RolUsuario.INSTRUCTOR, 2L)));

        List<UsuarioResponseDTO> resultado = service.listar(null);

        assertThat(resultado).hasSize(2);
        verify(usuarioRepository, never()).findByRol(any());
    }

    @Test
    void obtenerPorId_existente_retornaDTO() {
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(usuario("Luis", RolUsuario.ESTUDIANTE, 4L)));

        UsuarioResponseDTO dto = service.obtenerPorId(4L);

        assertThat(dto.id()).isEqualTo(4L);
        assertThat(dto.correo()).isEqualTo("luis@lescocr.cr");
        assertThat(dto.rol()).isEqualTo(RolUsuario.ESTUDIANTE);
    }

    @Test
    void obtenerPorId_noExiste_lanzaEntidadNoEncontrada() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }

    @Test
    void obtenerPorCorreo_existente_retornaDTO() {
        when(usuarioRepository.findByCorreo("luis@lescocr.cr"))
                .thenReturn(Optional.of(usuario("Luis", RolUsuario.ESTUDIANTE, 4L)));

        UsuarioResponseDTO dto = service.obtenerPorCorreo("luis@lescocr.cr");

        assertThat(dto.id()).isEqualTo(4L);
        assertThat(dto.correo()).isEqualTo("luis@lescocr.cr");
    }

    @Test
    void obtenerPorCorreo_noExiste_lanzaEntidadNoEncontrada() {
        when(usuarioRepository.findByCorreo("ausente@lescocr.cr")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorCorreo("ausente@lescocr.cr"))
                .isInstanceOf(EntidadNoEncontradaException.class);
    }
}
