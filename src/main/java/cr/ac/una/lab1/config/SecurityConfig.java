package cr.ac.una.lab1.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configura la seguridad HTTP de la API:
 * <ul>
 *   <li>Stateless (sin sesión ni cookie): cada petición debe llevar el JWT.
 *   <li>CSRF desactivado (API sin formularios HTML).
 *   <li>Autorización por endpoint según rol ({@link cr.ac.una.lab1.data.RolUsuario}).
 *   <li>Filtro JWT antes del filtro de usuario/contraseña estándar.
 *   <li>Respuestas 401/403 en formato Problem Detail (RFC 9457), sin traza de Java.
 * </ul>
 *
 * <h3>Matriz de autorización</h3>
 * <pre>
 * POST /auth/login                       → público
 * GET  /api/cursos                       → público
 * GET  /v3/api-docs/**, /swagger-ui/**   → público
 * POST /api/cursos/{id}/publicar         → ADMINISTRADOR
 * POST /api/v1/matriculas                → ESTUDIANTE
 * POST /api/v1/matriculas/{id}/aprobar   → ADMINISTRADOR
 * POST /api/v1/matriculas/{id}/cancelar  → ESTUDIANTE, ADMINISTRADOR
 * GET  /api/v1/matriculas/**             → autenticado
 * cualquier otro                         → autenticado
 * </pre>
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private static final String TYPE_BASE = "https://lescocr.cr/problems/";

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, ObjectMapper objectMapper) {
        this.jwtFilter    = jwtFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Público
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/cursos").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                 "/swagger-ui.html", "/webjars/**").permitAll()
                // Sólo ADMINISTRADOR
                .requestMatchers(HttpMethod.POST, "/api/cursos/*/publicar")
                    .hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/v1/matriculas/*/aprobar")
                    .hasRole("ADMINISTRADOR")
                // Sólo ESTUDIANTE
                .requestMatchers(HttpMethod.POST, "/api/v1/matriculas")
                    .hasRole("ESTUDIANTE")
                // ESTUDIANTE o ADMINISTRADOR (ownership check en servicio – OWASP API1)
                .requestMatchers(HttpMethod.POST, "/api/v1/matriculas/*/cancelar")
                    .hasAnyRole("ESTUDIANTE", "ADMINISTRADOR")
                // Autenticado (cualquier rol)
                .requestMatchers("/api/v1/matriculas/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                // 401 — sin token o token inválido
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    ProblemDetail pd = problemDetail(
                            HttpStatus.UNAUTHORIZED,
                            "No autenticado",
                            "no-autenticado",
                            "Se requiere un token JWT válido en el encabezado Authorization.");
                    res.getWriter().write(objectMapper.writeValueAsString(pd));
                })
                // 403 — token válido pero sin el rol requerido
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(HttpStatus.FORBIDDEN.value());
                    res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    ProblemDetail pd = problemDetail(
                            HttpStatus.FORBIDDEN,
                            "Acceso denegado",
                            "acceso-denegado",
                            "No tiene permiso para realizar esta acción.");
                    res.getWriter().write(objectMapper.writeValueAsString(pd));
                })
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private ProblemDetail problemDetail(HttpStatus status, String titulo,
                                        String tipo, String detalle) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalle);
        pd.setTitle(titulo);
        pd.setType(URI.create(TYPE_BASE + tipo));
        return pd;
    }
}
