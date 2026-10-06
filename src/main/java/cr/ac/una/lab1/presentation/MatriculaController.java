package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.MatriculaRequestDTO;
import cr.ac.una.lab1.business.MatriculaResponseDTO;
import cr.ac.una.lab1.business.MatriculaService;
import cr.ac.una.lab1.data.EstadoMatricula;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
@Tag(name = "Matrículas", description = "Gestión del proceso de matriculación de estudiantes")
class MatriculaController {

    private final MatriculaService matriculaService;

    MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    /**
     * POST /api/v1/matriculas — matricula un estudiante en una lección de un curso publicado.
     *
     * <p>OWASP API1: si el token corresponde a un ESTUDIANTE, verifica en el servicio
     * que {@code estudianteId} del cuerpo coincida con el usuario autenticado.
     * Un ADMINISTRADOR puede matricular a cualquier estudiante.
     * Devuelve 201 Created con {@code Location} apuntando al recurso creado.
     */
    @PostMapping
    @Operation(summary = "Matricular estudiante")
    ResponseEntity<MatriculaResponseDTO> matricular(
            @Valid @RequestBody MatriculaRequestDTO dto,
            Authentication auth) {
        boolean esAdmin = tieneRol(auth, "ROLE_ADMINISTRADOR");
        MatriculaResponseDTO respuesta = matriculaService.matricular(dto, auth.getName(), esAdmin);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(respuesta.matriculaId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    /** GET /api/v1/matriculas/{id} — una matrícula. 404 si no existe. */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener matrícula por ID")
    MatriculaResponseDTO obtener(@PathVariable Long id) {
        return matriculaService.obtenerPorId(id);
    }

    /**
     * GET /api/v1/matriculas — colección paginada y filtrable (patrón Specification).
     * Filtros opcionales: {@code usuarioId}, {@code cursoId}, {@code estado},
     * {@code fechaDesde}, {@code fechaHasta}, {@code precioFinalMin}, {@code precioFinalMax}.
     */
    @GetMapping
    @Operation(summary = "Buscar matrículas")
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
                usuarioId, cursoId, estado, fechaDesde, fechaHasta,
                precioFinalMin, precioFinalMax, pageable);
    }

    /**
     * POST /api/v1/matriculas/{id}/aprobar — aprueba el pago y activa la matrícula
     * (PENDIENTE → ACTIVA, patrón State). Solo ADMINISTRADOR.
     */
    @PostMapping("/{id}/aprobar")
    @Operation(summary = "Aprobar pago de matrícula")
    ResponseEntity<Void> aprobar(@PathVariable Long id) {
        matriculaService.aprobarPago(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/matriculas/{id}/cancelar — cancela la matrícula
     * (PENDIENTE/ACTIVA → CANCELADA, patrón State).
     *
     * <p>OWASP API1: el servicio verifica que un ESTUDIANTE solo cancele sus propias matrículas.
     */
    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar matrícula")
    ResponseEntity<Void> cancelar(@PathVariable Long id, Authentication auth) {
        boolean esAdmin = tieneRol(auth, "ROLE_ADMINISTRADOR");
        matriculaService.cancelarMatricula(id, auth.getName(), esAdmin);
        return ResponseEntity.noContent().build();
    }

    private boolean tieneRol(Authentication auth, String rol) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(rol));
    }
}
