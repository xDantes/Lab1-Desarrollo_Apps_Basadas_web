package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.MatriculaRequestDTO;
import cr.ac.una.lab1.business.MatriculaResponseDTO;
import cr.ac.una.lab1.business.MatriculaService;
import cr.ac.una.lab1.data.EstadoMatricula;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/matriculas")
class MatriculaController {

    private final MatriculaService matriculaService;

    MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    /**
     * POST /api/v1/matriculas — matricula un estudiante en una lección de un curso publicado.
     * {@code @Valid} activa Bean Validation sobre el DTO antes de llegar al servicio.
     * Devuelve 201 Created, con {@code Location} apuntando al recurso creado.
     */
    @PostMapping
    ResponseEntity<MatriculaResponseDTO> matricular(@Valid @RequestBody MatriculaRequestDTO dto) {
        MatriculaResponseDTO respuesta = matriculaService.matricular(dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(respuesta.matriculaId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    /** GET /api/v1/matriculas/{id} — una matrícula. 404 si no existe. */
    @GetMapping("/{id}")
    MatriculaResponseDTO obtener(@PathVariable Long id) {
        return matriculaService.obtenerPorId(id);
    }

    /**
     * GET /api/v1/matriculas — colección paginada y filtrable.
     *
     * <p>Filtros opcionales (patrón Specification, se combinan con AND):
     * {@code usuarioId}, {@code cursoId}, {@code estado}, {@code fechaDesde},
     * {@code fechaHasta}, {@code precioFinalMin}, {@code precioFinalMax}.
     * Orden y paginación vía {@code ?page=&size=&sort=campo,asc|desc}.
     */
    @GetMapping
    Page<MatriculaResponseDTO> buscar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) EstadoMatricula estado,
            @RequestParam(required = false) OffsetDateTime fechaDesde,
            @RequestParam(required = false) OffsetDateTime fechaHasta,
            @RequestParam(required = false) BigDecimal precioFinalMin,
            @RequestParam(required = false) BigDecimal precioFinalMax,
            @PageableDefault(size = 10, sort = "fecha") Pageable pageable) {
        return matriculaService.buscar(
                usuarioId, cursoId, estado, fechaDesde, fechaHasta, precioFinalMin, precioFinalMax, pageable);
    }

    /**
     * POST /api/v1/matriculas/{id}/aprobar — aprueba el pago y activa la matrícula.
     * Usa el patrón State internamente: PENDIENTE → ACTIVA. 204: ya se puede
     * releer el recurso actualizado con GET /api/v1/matriculas/{id}.
     */
    @PostMapping("/{id}/aprobar")
    ResponseEntity<Void> aprobar(@PathVariable Long id) {
        matriculaService.aprobarPago(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/matriculas/{id}/cancelar — cancela la matrícula.
     * Usa el patrón State internamente: PENDIENTE/ACTIVA → CANCELADA. 204, igual que "aprobar".
     */
    @PostMapping("/{id}/cancelar")
    ResponseEntity<Void> cancelar(@PathVariable Long id) {
        matriculaService.cancelarMatricula(id);
        return ResponseEntity.noContent().build();
    }
}
