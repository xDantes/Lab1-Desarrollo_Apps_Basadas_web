package cr.ac.una.lab1.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * Crea y valida tokens JWT firmados con HS256.
 *
 * <h3>Estructura del token</h3>
 * <ul>
 *   <li>{@code sub} — correo del usuario (nombre de usuario en Spring Security).
 *   <li>{@code rol} — nombre del {@link cr.ac.una.lab1.data.RolUsuario} (sin prefijo ROLE_).
 *   <li>{@code iat} — emisión.
 *   <li>{@code exp} — expiración (configurable vía {@code app.jwt.expiration-ms}).
 * </ul>
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(JwtProperties props) {
        this.secretKey = Keys.hmacShaKeyFor(
                props.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = props.expirationMs();
    }

    /** Genera un JWT con el correo como {@code subject} y el rol como claim {@code "rol"}. */
    public String generarToken(String correo, String rol) {
        long ahora = System.currentTimeMillis();
        return Jwts.builder()
                .subject(correo)
                .claim("rol", rol)
                .issuedAt(new Date(ahora))
                .expiration(new Date(ahora + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extrae los claims del token sin lanzar excepción controlada.
     *
     * @throws JwtException si el token es inválido o expiró
     */
    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Devuelve {@code true} si el token es válido y no expiró. */
    public boolean esValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extraerCorreo(String token) {
        return extraerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }
}
