package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.CursoCatalogoDTO;
import cr.ac.una.lab1.business.CursoService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cursos")
class CursoController {

    private final CursoService cursoService; // nunca el repositorio

    CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    /** GET /api/cursos — catálogo público de cursos publicados con cupos y precio final. */
    @GetMapping
    List<CursoCatalogoDTO> catalogoPublico() {
        return cursoService.listarCatalogoPublico();
    }

    /**
     * POST /api/cursos/{id}/publicar — publica un curso.
     * Proceso 1: valida lecciones, cambia publicado = true.
     * Devuelve 200 con el DTO actualizado, o 422 / 404 si las reglas de negocio fallan.
     */
    @PostMapping("/{id}/publicar")
    ResponseEntity<CursoCatalogoDTO> publicar(@PathVariable Long id) {
        CursoCatalogoDTO dto = cursoService.publicarCurso(id);
        return ResponseEntity.ok(dto);
    }
}
