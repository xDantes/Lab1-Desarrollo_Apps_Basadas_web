package cr.ac.una.lab1.business;

import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.data.RolUsuario;
import cr.ac.una.lab1.data.Usuario;
import cr.ac.una.lab1.data.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar(RolUsuario rol) {
        List<Usuario> usuarios = (rol != null)
                ? usuarioRepository.findByRol(rol)
                : usuarioRepository.findAll();
        return usuarios.stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Usuario", id));
        return toDTO(u);
    }

    /**
     * Un usuario por correo — el nombre de usuario en Spring Security es el
     * correo (ver {@link cr.ac.una.lab1.config.UsuarioDetailsServiceImpl}), así
     * que esto es lo que usan los controladores (API y MVC) para resolver
     * "quién soy" a partir de {@code Authentication#getName()} sin tocar
     * {@code UsuarioRepository} directamente desde la capa de presentación.
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorCorreo(String correo) {
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new EntidadNoEncontradaException("Usuario", correo));
        return toDTO(u);
    }

    private UsuarioResponseDTO toDTO(Usuario u) {
        return new UsuarioResponseDTO(
                u.getId(),
                u.getNombre(),
                u.getIdentificacion(),
                u.getCorreo(),
                u.getRol()
        );
    }
}
