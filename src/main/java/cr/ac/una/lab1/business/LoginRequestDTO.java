package cr.ac.una.lab1.business;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de la petición {@code POST /auth/login}.
 *
 * <p>Bean Validation rechaza la petición con 400 antes de que llegue al servicio
 * si el correo o la contraseña están ausentes o en blanco.
 *
 * @param correo     correo electrónico del usuario (nombre de usuario en el sistema)
 * @param contrasena contraseña en texto plano (solo viaja en la petición HTTPS; nunca persiste)
 */
public record LoginRequestDTO(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe tener formato válido")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        String contrasena
) {}
