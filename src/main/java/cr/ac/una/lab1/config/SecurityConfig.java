package cr.ac.una.lab1.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
 * Configura la seguridad HTTP de la aplicación con <strong>dos cadenas de
 * filtros independientes</strong>, cada una con el modelo que le corresponde:
 *
 * <ol>
 *   <li>{@link #mvcFilterChain} — rutas {@code /mvc/**} (Laboratorio 6: módulo
 *       MVC con Thymeleaf). Con <strong>sesión</strong> (login por formulario)
 *       y <strong>CSRF habilitado</strong>: CSRF protege formularios donde el
 *       navegador manda cookies automáticamente, que es exactamente el caso
 *       aquí — no tendría sentido en la API stateless de abajo.
 *   <li>{@link #apiFilterChain} — todo lo demás (API REST, Swagger, login JWT).
 *       <strong>Sin sesión</strong> (cada petición lleva su propio Bearer
 *       token) y <strong>CSRF deshabilitado</strong>: un atacante no puede
 *       forzar a un navegador a adjuntar un header {@code Authorization} que
 *       no tiene, así que no hay nada que un ataque CSRF pueda explotar aquí.
 * </ol>
 *
 * <p>Spring prueba las cadenas en el orden de {@code @Order}; la primera cuyo
 * {@code securityMatcher} calce con la petición es la que se usa. Las dos
 * comparten el mismo {@link UsuarioDetailsServiceImpl} y {@link PasswordEncoder}
 * — un usuario autentica con las mismas credenciales en ambas, solo cambia el
 * mecanismo (JWT vs. sesión) según por dónde entre.
 *
 * <h3>Matriz de autorización — API (JWT)</h3>
 * <pre>
 * POST /auth/login                       → público
 * GET  /api/v1/cursos                    → público
 * GET  /v3/api-docs/**, /swagger-ui/**   → público
 * POST /api/v1/cursos/{id}/publicar      → ADMINISTRADOR
 * POST /api/v1/matriculas                → ESTUDIANTE
 * POST /api/v1/matriculas/{id}/aprobar   → ADMINISTRADOR
 * POST /api/v1/matriculas/{id}/cancelar  → ESTUDIANTE, ADMINISTRADOR
 * GET  /api/v1/matriculas/**             → autenticado
 * cualquier otro                         → autenticado
 * </pre>
 *
 * <h3>Matriz de autorización — MVC (sesión)</h3>
 * <pre>
 * GET  /mvc/login, /mvc/logout           → público
 * GET  /mvc/cursos                       → público
 * GET/POST /mvc/cursos/{id}/publicar     → ADMINISTRADOR
 * GET/POST /mvc/matriculas/nueva         → ESTUDIANTE
 * cualquier otro bajo /mvc/**            → autenticado
 * </pre>
 */
@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private static final String TYPE_BASE = "https://lescocr.cr/problems/";

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, ObjectMapper objectMapper) {
        this.jwtFilter    = jwtFilter;
        this.objectMapper = objectMapper;
    }

    // -------------------------------------------------------------------
    // Cadena 1: módulo MVC con Thymeleaf (sesión, formulario, CSRF)
    // -------------------------------------------------------------------

    @Bean
    @Order(1)
    public SecurityFilterChain mvcFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/mvc/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/mvc/login", "/mvc/logout").permitAll()
                .requestMatchers(HttpMethod.GET, "/mvc/cursos").permitAll()
                .requestMatchers("/mvc/cursos/*/publicar").hasRole("ADMINISTRADOR")
                .requestMatchers("/mvc/matriculas/nueva").hasRole("ESTUDIANTE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/mvc/login")
                .defaultSuccessUrl("/mvc/cursos", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/mvc/logout")
                .logoutSuccessUrl("/mvc/login?logout")
                .permitAll()
            );
            // Nada de .csrf(disable) ni .sessionManagement(STATELESS) aquí:
            // esta cadena se queda con los valores por defecto de Spring
            // Security (sesión IF_REQUIRED + CSRF con HttpSessionCsrfTokenRepository),
            // que es justo lo que un módulo con formularios necesita.

        return http.build();
    }

    // -------------------------------------------------------------------
    // Cadena 2: API REST (stateless, JWT, sin CSRF)
    // -------------------------------------------------------------------

    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Público
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/cursos", "/api/v1/cursos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/instructores", "/api/v1/instructores/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/lecciones", "/api/v1/lecciones/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                 "/swagger-ui.html", "/webjars/**").permitAll()
                // Sólo ADMINISTRADOR
                .requestMatchers(HttpMethod.POST, "/api/v1/cursos/*/publicar").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/v1/matriculas/*/aprobar").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/v1/usuarios/**").hasRole("ADMINISTRADOR")
                // Sólo ESTUDIANTE
                .requestMatchers(HttpMethod.POST, "/api/v1/matriculas").hasRole("ESTUDIANTE")
                // ESTUDIANTE o ADMINISTRADOR (ownership check en servicio – OWASP API1)
                .requestMatchers(HttpMethod.POST, "/api/v1/matriculas/*/cancelar")
                    .hasAnyRole("ESTUDIANTE", "ADMINISTRADOR")
                // Autenticado (cualquier rol)
                .requestMatchers("/api/v1/matriculas/**").authenticated()
                .requestMatchers("/api/v1/pagos/**").authenticated()
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

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        java.util.List<String> origins = java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        config.setExposedHeaders(java.util.List.of("Authorization", "Location"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private ProblemDetail problemDetail(HttpStatus status, String titulo,
                                        String tipo, String detalle) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalle);
        pd.setTitle(titulo);
        pd.setType(URI.create(TYPE_BASE + tipo));
        return pd;
    }
}
