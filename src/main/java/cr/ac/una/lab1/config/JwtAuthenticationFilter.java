package cr.ac.una.lab1.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Intercepta cada petición HTTP, extrae el Bearer token del encabezado
 * {@code Authorization} y, si es válido, establece el contexto de seguridad.
 *
 * <p>Si el token está ausente o es inválido, el filtro deja la petición pasar
 * sin autenticación: la capa de autorización de Spring Security decidirá luego
 * si el endpoint requiere autenticación (401) o no.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String token = extraerToken(request);

        if (StringUtils.hasText(token)) {
            try {
                if (tokenProvider.esValido(token)) {
                    String correo = tokenProvider.extraerCorreo(token);
                    String rol    = tokenProvider.extraerRol(token);

                    var auth = new UsernamePasswordAuthenticationToken(
                            correo, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + rol)));

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                // Token inválido: se deja sin autenticación; Spring Security
                // responderá 401 si el endpoint lo requiere.
            }
        }

        chain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
