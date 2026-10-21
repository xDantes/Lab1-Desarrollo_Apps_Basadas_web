package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.CursoCatalogoDTO;
import cr.ac.una.lab1.business.CursoService;
import cr.ac.una.lab1.business.PublicarCursoRequestDTO;
import cr.ac.una.lab1.business.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Módulo MVC con Thymeleaf (Laboratorio 6) — caso de uso 2: catálogo de cursos
 * y su publicación. Reutiliza {@link CursoService} tal cual lo usa la API REST.
 */
@Controller
@RequestMapping("/mvc/cursos")
class CursoMvcController {

    private final CursoService cursoService;
    private final UsuarioService usuarioService;

    CursoMvcController(CursoService cursoService, UsuarioService usuarioService) {
        this.cursoService = cursoService;
        this.usuarioService = usuarioService;
    }

    /** GET /mvc/cursos — catálogo paginado, público (igual que GET /api/v1/cursos). */
    @GetMapping
    String listar(@PageableDefault(size = 10, sort = "fechaInicio") Pageable pageable, Model model) {
        Page<CursoCatalogoDTO> pagina = cursoService.buscar(null, null, null, null, null, pageable);
        model.addAttribute("pagina", pagina);
        return "cursos/listado";
    }

    /** GET /mvc/cursos/{id}/publicar — confirmación (solo ADMINISTRADOR, ver SecurityConfig). */
    @GetMapping("/{id}/publicar")
    String formularioPublicar(@PathVariable Long id, Model model) {
        model.addAttribute("curso", cursoService.obtenerPorId(id));
        return "cursos/publicar";
    }

    /**
     * POST /mvc/cursos/{id}/publicar — publica el curso con las mismas 7
     * reglas que valida {@link CursoService#publicarCurso}. El administrador
     * que autoriza se resuelve de la sesión, nunca de un campo del formulario.
     *
     * <p>Post/Redirect/Get: éxito → redirige al catálogo; error de regla de
     * negocio → re-renderiza el mismo formulario con el mensaje (no es un
     * reenvío de POST, es la primera respuesta a ese POST en particular).
     */
    @PostMapping("/{id}/publicar")
    String publicar(@PathVariable Long id, Authentication auth, Model model,
                    RedirectAttributes redirect) {
        try {
            Long administradorId = usuarioService.obtenerPorCorreo(auth.getName()).id();
            cursoService.publicarCurso(id, new PublicarCursoRequestDTO(administradorId));
        } catch (RuntimeException ex) {
            model.addAttribute("curso", cursoService.obtenerPorId(id));
            model.addAttribute("error", ex.getMessage());
            return "cursos/publicar";
        }

        redirect.addFlashAttribute("mensaje", "Curso publicado correctamente.");
        return "redirect:/mvc/cursos";
    }
}
