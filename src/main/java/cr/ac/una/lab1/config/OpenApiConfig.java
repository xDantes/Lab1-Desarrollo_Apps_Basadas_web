package cr.ac.una.lab1.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura springdoc-openapi para:
 * <ul>
 *   <li>Mostrar el esquema de seguridad {@code BearerAuth} (JWT) en la UI.
 *   <li>Requerir el token en todos los endpoints salvo los marcados con
 *       {@code @SecurityRequirements({})} en el controlador.
 * </ul>
 *
 * <p>Swagger UI disponible en: {@code /swagger-ui.html}
 * <p>Especificación OpenAPI en: {@code /v3/api-docs}
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        final String schemeName = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("LESCO CR API")
                        .description("API de gestión de cursos de Lengua de Señas Costarricense (LESCO). "
                                + "Autenticación con JWT vía POST /auth/login.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName,
                                new SecurityScheme()
                                        .name(schemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
