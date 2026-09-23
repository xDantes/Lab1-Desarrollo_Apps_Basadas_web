package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.MatriculaRequestDTO;
import cr.ac.una.lab1.business.MatriculaResponseDTO;
import cr.ac.una.lab1.business.MatriculaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matriculas")
class MatriculaController {

    private final MatriculaService matriculaService;

    MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    /**
     * POST /api/matriculas — matricula un estudiante en una lección de un curso publicado.
     * {@code @Valid} activa Bean Validation sobre el DTO antes de llegar al servicio.
     * Devuelve 201 Created con el resultado completo.
     */
    @PostMapping
    ResponseEntity<MatriculaResponseDTO> matricular(@Valid @RequestBody MatriculaRequestDTO dto) {
        MatriculaResponseDTO respuesta = matriculaService.matricular(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * POST /api/matriculas/{id}/aprobar — aprueba el pago y activa la matrícula.
     * Usa el patrón State internamente: PENDIENTE → ACTIVA.
     */
    @PostMapping("/{id}/aprobar")
    ResponseEntity<MatriculaResponseDTO> aprobar(@PathVariable Long id) {
        return ResponseEntity.ok(matriculaService.aprobarPago(id));
    }

    /**
     * POST /api/matriculas/{id}/cancelar — cancela la matrícula.
     * Usa el patrón State internamente: PENDIENTE/ACTIVA → CANCELADA.
     */
    @PostMapping("/{id}/cancelar")
    ResponseEntity<MatriculaResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(matriculaService.cancelarMatricula(id));
    }
}
