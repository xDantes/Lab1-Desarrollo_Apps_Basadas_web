package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.LeccionDetalleResponseDTO;
import cr.ac.una.lab1.business.LeccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lecciones")
@Tag(name = "Lecciones", description = "Consulta de lecciones de los cursos (acceso público)")
public class LeccionController {

    private final LeccionService leccionService;

    public LeccionController(LeccionService leccionService) {
        this.leccionService = leccionService;
    }

    /** GET /api/v1/lecciones — lista lecciones, opcionalmente filtradas por cursoId. */
    @GetMapping
    @Operation(summary = "Listar lecciones", description = "Devuelve lecciones del sistema con detalle de curso e instructor, filtrables por cursoId.")
    public List<LeccionDetalleResponseDTO> listar(@RequestParam(required = false) Long cursoId) {
        return leccionService.listar(cursoId);
    }

    /** GET /api/v1/lecciones/{id} — obtiene una lección por ID. */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener lección por ID")
    public LeccionDetalleResponseDTO obtenerPorId(@PathVariable Long id) {
        return leccionService.obtenerPorId(id);
    }
}
