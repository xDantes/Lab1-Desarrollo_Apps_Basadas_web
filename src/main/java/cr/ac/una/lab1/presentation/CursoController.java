package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.CursoCatalogoDTO;
import cr.ac.una.lab1.business.CursoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cursos")
class CursoController {

    private final CursoService cursoService; // nunca el repositorio

    CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    List<CursoCatalogoDTO> catalogoPublico() {
        return cursoService.listarCatalogoPublico();
    }
}
