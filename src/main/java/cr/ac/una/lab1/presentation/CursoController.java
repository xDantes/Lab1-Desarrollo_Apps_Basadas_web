package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.CursoCatalogoDTO;
import cr.ac.una.lab1.business.CursoService;
import cr.ac.una.lab1.business.PublicarCursoRequestDTO;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cursos")
class CursoController {

    private final CursoService cursoService; // nunca el repositorio

    CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    /**
     * GET /api/v1/cursos — colección paginada y filtrable de cursos.
     *
     * <p>Filtros opcionales (patrón Specification, se combinan con AND):
     * {@code texto} (nombre o código), {@code nivel}, {@code precioMax},
     * {@code publicado}, {@code fechaInicioDespuesDe}. Orden y paginación via
     * {@code ?page=&size=&sort=campo,asc|desc} (p. ej. {@code sort=precio,asc}).
     *
     * <p>El catálogo público de antes es un caso particular: {@code ?publicado=true}.
     */
    @GetMapping
    Page<CursoCatalogoDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) Boolean publicado,
            @RequestParam(required = false) LocalDate fechaInicioDespuesDe,
            @PageableDefault(size = 10, sort = "fechaInicio") Pageable pageable) {
        return cursoService.buscar(texto, nivel, precioMax, publicado, fechaInicioDespuesDe, pageable);
    }

    /** GET /api/v1/cursos/{id} — un curso. 404 si no existe. */
    @GetMapping("/{id}")
    CursoCatalogoDTO obtener(@PathVariable Long id) {
        return cursoService.obtenerPorId(id);
    }

    /**
     * POST /api/v1/cursos/{id}/publicar — publica un curso aplicando todas las reglas de negocio.
     *
     * <p>El cuerpo debe indicar el ID del administrador que autoriza la operación.
     * Bean Validation rechaza solicitudes con {@code administradorId} nulo antes
     * de invocar el servicio.
     *
     * <p>Respuestas posibles:
     * <ul>
     *   <li>200 — publicado correctamente; devuelve el DTO del catálogo.
     *   <li>400 — cuerpo inválido ({@code administradorId} ausente).
     *   <li>404 — curso no encontrado.
     *   <li>409/422 — alguna regla de negocio no se cumple.
     * </ul>
     */
    @PostMapping("/{id}/publicar")
    ResponseEntity<CursoCatalogoDTO> publicar(
            @PathVariable Long id,
            @RequestBody @Valid PublicarCursoRequestDTO request) {
        CursoCatalogoDTO dto = cursoService.publicarCurso(id, request);
        return ResponseEntity.ok(dto);
    }
}
