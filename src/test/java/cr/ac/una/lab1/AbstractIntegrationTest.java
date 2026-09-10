package cr.ac.una.lab1;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Clase base para pruebas de integración con Testcontainers.
 *
 * <p>Inicia contenedores reales de PostgreSQL y MongoDB para ejecutar
 * las migraciones de Flyway, validar el esquema JPA (ddl-auto=validate)
 * y probar repositorios y consultas de negocio contra motores de base de datos reales.
 */
@SpringBootTest
public abstract class AbstractIntegrationTest {

    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("lescocr")
            .withUsername("lescocr")
            .withPassword("lescocr");

    public static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    static {
        if (DockerUtils.isDockerAvailable()) {
            postgres.start();
            mongo.start();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (DockerUtils.isDockerAvailable()) {
            // Configuración dinámica de PostgreSQL real
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);

            // Validación estricta de esquema contra Flyway
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
            registry.add("spring.jpa.open-in-view", () -> "false");

            // Configuración dinámica de MongoDB real
            registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        }
    }
}
