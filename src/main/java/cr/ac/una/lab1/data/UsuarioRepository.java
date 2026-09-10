package cr.ac.una.lab1.data;

import cr.ac.una.lab1.data.base.BaseRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends BaseRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByIdentificacion(String identificacion);

    List<Usuario> findByRol(RolUsuario rol);
}
