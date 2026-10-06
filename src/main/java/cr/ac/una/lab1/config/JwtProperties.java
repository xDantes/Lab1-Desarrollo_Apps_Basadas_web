package cr.ac.una.lab1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades del token JWT leídas desde {@code application.properties}.
 * Prefijo {@code app.jwt}.
 *
 * <p>En producción sobreescribir {@code app.jwt.secret} con una variable de entorno
 * {@code JWT_SECRET} de al menos 32 bytes (256 bits para HS256).
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long expirationMs
) {}
