package cr.ac.una.lab1.business;

import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import cr.ac.una.lab1.config.JwtTokenProvider;
import cr.ac.una.lab1.data.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Autentica credenciales y emite un JWT.
 *
 * <p>Delega la verificación de la contraseña al {@link AuthenticationManager} de
 * Spring Security, que a su vez llama a {@link cr.ac.una.lab1.config.UsuarioDetailsServiceImpl}
 * y al {@code BCryptPasswordEncoder}.
 *
 * <p>Si las credenciales son correctas, consulta el rol en la base de datos para incluirlo
 * en el token (Spring Security internamente ya lo tiene, pero lo recuperamos para el DTO).
 */
@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UsuarioRepository usuarioRepository;

    public AuthService(AuthenticationManager authManager,
                       JwtTokenProvider tokenProvider,
                       UsuarioRepository usuarioRepository) {
        this.authManager      = authManager;
        this.tokenProvider    = tokenProvider;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Verifica credenciales y devuelve un {@link LoginResponseDTO} con el JWT.
     *
     * @throws ReglaNegocioException (422 → manejado como 401 en el controlador) si
     *         las credenciales son incorrectas.
     */
    public LoginResponseDTO login(LoginRequestDTO dto) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.correo(), dto.contrasena()));

            // Recuperamos el rol desde la BD para incluirlo en el token y el DTO
            String rol = usuarioRepository.findByCorreo(dto.correo())
                    .map(u -> u.getRol().name())
                    .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado."));

            String token = tokenProvider.generarToken(auth.getName(), rol);
            return new LoginResponseDTO(token, auth.getName(), rol);

        } catch (AuthenticationException e) {
            // No exponemos si el correo no existe vs. contraseña incorrecta (OWASP API2)
            throw new ReglaNegocioException("Credenciales incorrectas.");
        }
    }
}
