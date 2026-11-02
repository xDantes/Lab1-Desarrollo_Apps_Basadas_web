package cr.ac.una.lab1.business;

import cr.ac.una.lab1.business.exception.CredencialesInvalidasException;
import cr.ac.una.lab1.config.JwtTokenProvider;
import cr.ac.una.lab1.data.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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
     * @throws CredencialesInvalidasException (401) si las credenciales son incorrectas
     *         o el usuario autenticado no existe en la base de datos.
     */
    public LoginResponseDTO login(LoginRequestDTO dto) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.correo(), dto.contrasena()));

            // Recuperamos el usuario desde la BD para incluir rol, id y nombre en la respuesta
            var usuario = usuarioRepository.findByCorreo(dto.correo())
                    .orElseThrow(() -> {
                        log.warn("Evento de seguridad: inicio de sesión fallido — usuario no encontrado para '{}'", dto.correo());
                        return new CredencialesInvalidasException("Usuario no encontrado.");
                    });

            String rol = usuario.getRol().name();
            String token = tokenProvider.generarToken(auth.getName(), rol);
            log.info("Evento de seguridad: inicio de sesión exitoso para el correo '{}' [rol={}]", auth.getName(), rol);
            return new LoginResponseDTO(token, "Bearer", auth.getName(), rol, usuario.getId(), usuario.getNombre());

        } catch (AuthenticationException e) {
            // No exponemos si el correo no existe vs. contraseña incorrecta (OWASP API2)
            log.warn("Evento de seguridad: inicio de sesión fallido — credenciales incorrectas para '{}'", dto.correo());
            throw new CredencialesInvalidasException("Credenciales incorrectas.");
        }
    }
}
