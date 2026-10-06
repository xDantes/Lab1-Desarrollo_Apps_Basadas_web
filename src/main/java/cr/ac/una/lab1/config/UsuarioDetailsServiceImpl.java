package cr.ac.una.lab1.config;

import cr.ac.una.lab1.data.UsuarioRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga un {@link cr.ac.una.lab1.data.Usuario} por correo electrónico para que
 * Spring Security pueda verificar la contraseña durante el login.
 *
 * <p>El nombre de usuario en Spring Security es el correo del usuario.
 * La autoridad almacenada es {@code "ROLE_<ROL>"} (p. ej. {@code "ROLE_ADMINISTRADOR"}).
 *
 * <p>Ninguna entidad JPA se expone fuera de esta clase: se devuelve
 * {@link org.springframework.security.core.userdetails.User} (tipo de Spring).
 */
@Service
public class UsuarioDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        var usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No se encontró un usuario con correo: " + correo));

        return new User(
                usuario.getCorreo(),
                usuario.getContrasenaHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
        );
    }
}
