package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.CursoCatalogoDTO;
import cr.ac.una.lab1.business.CursoService;
import cr.ac.una.lab1.business.PublicarCursoRequestDTO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cursos")
class CursoController {

    private final CursoService cursoService;

    CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    /** GET /api/cursos — catálogo público de cursos publicados con cupos y precio final. */
    @GetMapping
    List<CursoCatalogoDTO> catalogoPublico() {
        return cursoService.listarCatalogoPublico();
    }

    /**
     * POST /api/cursos/{id}/publicar — publica un curso aplicando todas las reglas de negocio.
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
     *   <li>422 — alguna regla de negocio no se cumple.
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
