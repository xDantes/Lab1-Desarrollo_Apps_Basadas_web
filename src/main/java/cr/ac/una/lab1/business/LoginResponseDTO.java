package cr.ac.una.lab1.business;

/**
 * Respuesta de {@code POST /auth/login}: token JWT y datos mínimos del usuario autenticado.
 *
 * @param token  JWT Bearer token para incluir en {@code Authorization: Bearer <token>}
 * @param tipo   siempre {@code "Bearer"}
 * @param correo correo del usuario autenticado
 * @param rol    rol asignado en el sistema (ADMINISTRADOR, INSTRUCTOR, ESTUDIANTE)
 */
public record LoginResponseDTO(
        String token,
        String tipo,
        String correo,
        String rol
) {
    public LoginResponseDTO(String token, String correo, String rol) {
        this(token, "Bearer", correo, rol);
    }
}
