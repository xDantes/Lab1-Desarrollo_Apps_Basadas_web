package cr.ac.una.lab1.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import cr.ac.una.lab1.business.exception.CredencialesInvalidasException;
import cr.ac.una.lab1.config.JwtTokenProvider;
import cr.ac.una.lab1.data.RolUsuario;
import cr.ac.una.lab1.data.Usuario;
import cr.ac.una.lab1.data.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

/** Pruebas unitarias de {@link AuthService} (login + emisión de JWT) con Mockito. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authManager;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private Authentication authenticacionExitosa;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(authManager, tokenProvider, usuarioRepository);
    }

    @Test
    void login_credencialesValidas_devuelveTokenYDatosDelUsuario() {
        Usuario luis = new Usuario("Luis Fernández Rojas", "4-4444-4444", "luis@lescocr.cr", "hash", RolUsuario.ESTUDIANTE);
        ReflectionTestUtils.setField(luis, "id", 4L);

        when(authManager.authenticate(any())).thenReturn(authenticacionExitosa);
        when(authenticacionExitosa.getName()).thenReturn("luis@lescocr.cr");
        when(usuarioRepository.findByCorreo("luis@lescocr.cr")).thenReturn(Optional.of(luis));
        when(tokenProvider.generarToken("luis@lescocr.cr", "ESTUDIANTE")).thenReturn("jwt-falso");

        LoginResponseDTO resultado = service.login(new LoginRequestDTO("luis@lescocr.cr", "Password123!"));

        assertThat(resultado.token()).isEqualTo("jwt-falso");
        assertThat(resultado.tipo()).isEqualTo("Bearer");
        assertThat(resultado.correo()).isEqualTo("luis@lescocr.cr");
        assertThat(resultado.rol()).isEqualTo("ESTUDIANTE");
        assertThat(resultado.usuarioId()).isEqualTo(4L);
        assertThat(resultado.nombre()).isEqualTo("Luis Fernández Rojas");
    }

    @Test
    void login_credencialesIncorrectas_lanzaCredencialesInvalidas() {
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("credenciales inválidas"));

        assertThatThrownBy(() -> service.login(new LoginRequestDTO("luis@lescocr.cr", "mala-clave")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessageContaining("Credenciales incorrectas");
    }

    @Test
    void login_autenticadoPeroSinUsuarioEnBD_lanzaCredencialesInvalidas() {
        // auth.getName() no se llega a usar: findByCorreo(...).orElseThrow() lanza antes.
        when(authManager.authenticate(any())).thenReturn(authenticacionExitosa);
        when(usuarioRepository.findByCorreo("fantasma@lescocr.cr")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequestDTO("fantasma@lescocr.cr", "Password123!")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}
