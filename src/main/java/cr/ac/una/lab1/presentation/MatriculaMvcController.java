package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.LeccionService;
import cr.ac.una.lab1.business.MatriculaRequestDTO;
import cr.ac.una.lab1.business.MatriculaResponseDTO;
import cr.ac.una.lab1.business.MatriculaService;
import cr.ac.una.lab1.business.UsuarioService;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.MetodoPago;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Módulo MVC con Thymeleaf (Laboratorio 6) — caso de uso 1: matrículas.
 *
 * <p>Reutiliza {@link MatriculaService} tal cual lo usa la API REST (mismas
 * reglas de negocio, mismo patrón de verificación de propiedad OWASP API1);
 * esta clase solo traduce entre formulario HTML y los DTOs de negocio.
 */
@Controller
@RequestMapping("/mvc/matriculas")
class MatriculaMvcController {

    private final MatriculaService matriculaService;
    private final LeccionService leccionService;
    private final UsuarioService usuarioService;

    MatriculaMvcController(MatriculaService matriculaService, LeccionService leccionService,
                           UsuarioService usuarioService) {
        this.matriculaService = matriculaService;
        this.leccionService = leccionService;
        this.usuarioService = usuarioService;
    }

    /**
     * GET /mvc/matriculas — listado paginado. Un ESTUDIANTE ve las suyas
     * (igual que en la API, {@code MatriculaService} fuerza el filtro); un
     * ADMINISTRADOR las ve todas.
     */
    @GetMapping
    String listar(
            @RequestParam(required = false) EstadoMatricula estado,
            @PageableDefault(size = 10, sort = "fecha") Pageable pageable,
            Authentication auth,
            Model model) {
        boolean esAdmin = tieneRol(auth, "ROLE_ADMINISTRADOR");
        Page<MatriculaResponseDTO> pagina = matriculaService.buscar(
                null, null, estado, null, null, null, null, pageable, auth.getName(), esAdmin);
        model.addAttribute("pagina", pagina);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("estados", EstadoMatricula.values());
        return "matriculas/listado";
    }

    /** GET /mvc/matriculas/nueva — formulario de alta (solo ESTUDIANTE, ver SecurityConfig). */
    @GetMapping("/nueva")
    String formularioNueva(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new NuevaMatriculaFormDTO());
        }
        cargarListas(model);
        return "matriculas/nueva";
    }

    /**
     * POST /mvc/matriculas/nueva — crea la matrícula con las mismas reglas que
     * la API (mismo {@link MatriculaService#matricular(MatriculaRequestDTO, String, boolean)}).
     *
     * <p><strong>Post/Redirect/Get:</strong> si todo sale bien, redirige a
     * {@code GET /mvc/matriculas} en vez de responder directo al POST — así un
     * refresh del navegador repite el GET, nunca reenvía el formulario.
     * {@code estudianteId} nunca lo escribe el usuario: se resuelve del
     * usuario autenticado en sesión, igual que hace la API vía OWASP API1.
     */
    @PostMapping("/nueva")
    String crear(@Valid @ModelAttribute("form") NuevaMatriculaFormDTO form,
                 BindingResult errores,
                 Authentication auth,
                 Model model,
                 RedirectAttributes redirect) {

        if (errores.hasErrors()) {
            cargarListas(model);
            return "matriculas/nueva";
        }

        try {
            Long estudianteId = usuarioService.obtenerPorCorreo(auth.getName()).id();
            Long cursoId = leccionService.obtenerPorId(form.getLeccionId()).cursoId();
            MatriculaRequestDTO dto = new MatriculaRequestDTO(
                    estudianteId, cursoId, form.getLeccionId(), form.getMetodoPago(), form.getReferencia());
            matriculaService.matricular(dto, auth.getName(), false);
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            cargarListas(model);
            return "matriculas/nueva";
        }

        redirect.addFlashAttribute("mensaje", "Matrícula creada correctamente.");
        return "redirect:/mvc/matriculas";
    }

    private void cargarListas(Model model) {
        model.addAttribute("lecciones", leccionService.listar(null));
        model.addAttribute("metodos", MetodoPago.values());
    }

    private boolean tieneRol(Authentication auth, String rol) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(rol));
    }
}
