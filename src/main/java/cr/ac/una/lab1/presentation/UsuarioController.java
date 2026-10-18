package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.UsuarioResponseDTO;
import cr.ac.una.lab1.business.UsuarioService;
import cr.ac.una.lab1.data.RolUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Consulta y gestión de usuarios (requiere rol ADMINISTRADOR)")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** GET /api/v1/usuarios — lista usuarios, opcionalmente filtrados por rol. */
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Lista usuarios del sistema, opcionalmente filtrados por rol (ADMINISTRADOR, INSTRUCTOR, ESTUDIANTE).")
    public List<UsuarioResponseDTO> listar(@RequestParam(required = false) RolUsuario rol) {
        return usuarioService.listar(rol);
    }

    /** GET /api/v1/usuarios/{id} — obtiene un usuario por ID. */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public UsuarioResponseDTO obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }
}
