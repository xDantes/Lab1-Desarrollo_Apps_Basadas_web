package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.PagoResponseDTO;
import cr.ac.una.lab1.business.PagoService;
import cr.ac.una.lab1.data.EstadoPago;
import cr.ac.una.lab1.data.MetodoPago;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pagos", description = "Consulta de pagos asociados a las matrículas")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    /** GET /api/v1/pagos — lista pagos. OWASP API1: Estudiantes solo ven sus propios pagos. */
    @GetMapping
    @Operation(summary = "Listar pagos", description = "Devuelve pagos registrados. Aplica OWASP API1: los estudiantes solo ven sus propios pagos.")
    public List<PagoResponseDTO> listar(
            @RequestParam(required = false) EstadoPago estado,
            @RequestParam(required = false) MetodoPago metodo,
            @RequestParam(required = false) Long matriculaId,
            Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        return pagoService.listar(estado, metodo, matriculaId, auth.getName(), esAdmin);
    }

    /** GET /api/v1/pagos/{id} — obtiene un pago por ID. */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener pago por ID")
    public PagoResponseDTO obtenerPorId(@PathVariable Long id, Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        return pagoService.obtenerPorId(id, auth.getName(), esAdmin);
    }
}
