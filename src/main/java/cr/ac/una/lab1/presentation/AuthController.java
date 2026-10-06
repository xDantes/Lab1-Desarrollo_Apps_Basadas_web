package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.business.AuthService;
import cr.ac.una.lab1.business.LoginRequestDTO;
import cr.ac.una.lab1.business.LoginResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de autenticación. Público (sin token requerido).
 *
 * <p>{@code POST /auth/login} — verifica credenciales y emite un JWT.
 * La respuesta incluye el token y el rol; el cliente debe enviarlo en cada
 * petición posterior como {@code Authorization: Bearer <token>}.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Obtención de token JWT")
class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /auth/login — emite un JWT si las credenciales son correctas.
     *
     * <p>Devuelve 200 con el token, 400 si el cuerpo es inválido
     * (correo o contraseña ausentes) y 422 si las credenciales son incorrectas.
     */
    @PostMapping("/login")
    @SecurityRequirements   // este endpoint no requiere token
    @Operation(summary = "Login", description = "Autentica con correo y contraseña y devuelve un JWT.")
    ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
