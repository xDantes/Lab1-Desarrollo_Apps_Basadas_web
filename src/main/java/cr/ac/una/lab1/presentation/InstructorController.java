package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.InstructorResponseDTO;
import cr.ac.una.lab1.business.InstructorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/instructores")
@Tag(name = "Instructores", description = "Consulta del cuerpo docente e instructores de LESCO (acceso público)")
public class InstructorController {

    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    /** GET /api/v1/instructores — catálogo de instructores. */
    @GetMapping
    @Operation(summary = "Listar instructores", description = "Devuelve la lista de instructores disponibles en la plataforma.")
    public List<InstructorResponseDTO> listar() {
        return instructorService.listar();
    }

    /** GET /api/v1/instructores/{id} — obtiene un instructor por ID (usuarioId). */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener instructor por ID")
    public InstructorResponseDTO obtenerPorId(@PathVariable Long id) {
        return instructorService.obtenerPorId(id);
    }
}
