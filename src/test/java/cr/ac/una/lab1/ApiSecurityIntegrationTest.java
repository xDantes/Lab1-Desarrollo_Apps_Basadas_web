package cr.ac.una.lab1;

import cr.ac.una.lab1.business.LoginRequestDTO;
import cr.ac.una.lab1.business.LoginResponseDTO;
import cr.ac.una.lab1.business.MatriculaRequestDTO;
import cr.ac.una.lab1.data.CursoRepository;
import cr.ac.una.lab1.data.Leccion;
import cr.ac.una.lab1.data.LeccionRepository;
import cr.ac.una.lab1.data.MetodoPago;
import cr.ac.una.lab1.data.RolUsuario;
import cr.ac.una.lab1.data.Usuario;
import cr.ac.una.lab1.data.UsuarioRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integración de la API HTTP con Testcontainers.
 *
 * <p>Levanta el servidor en un puerto aleatorio ({@code RANDOM_PORT}) y usa
 * {@link TestRestTemplate} para ejecutar peticiones HTTP reales contra los
 * contenedores de PostgreSQL y MongoDB iniciados en {@link AbstractIntegrationTest}.
 *
 * <h3>Códigos de estado verificados</h3>
 * <ul>
 *   <li>201 — matrícula creada correctamente (ESTUDIANTE autenticado).
 *   <li>400 — cuerpo inválido (campo obligatorio ausente).
 *   <li>401 — petición sin token JWT.
 *   <li>403 — token válido pero rol incorrecto (ESTUDIANTE intenta publicar curso).
 *   <li>404 — recurso no encontrado.
 *   <li>422 — regla de negocio violada (referencia SINPE inválida).
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiSecurityIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private LeccionRepository leccionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // Credenciales de los usuarios de prueba creados en @BeforeAll
    private static final String ADMIN_CORREO    = "api.admin@test.cr";
    private static final String ADMIN_PASS      = "Admin1234!";
    private static final String STUDENT_CORREO  = "api.student@test.cr";
    private static final String STUDENT_PASS    = "Student1234!";

    private String adminToken;
    private String studentToken;
    private Long studentId;
    private Long cursoPublicadoId;
    private Long leccionId;
    /** Segundo curso publicado — usado en test_422 para no colisionar con la matrícula del test_201. */
    private Long curso2Id;
    private Long leccion2Id;

    @BeforeAll
    void setUp() {
        // Crear usuarios de prueba con hashes BCrypt reales
        Usuario admin = new Usuario("Admin Test", "9-0000-0001",
                ADMIN_CORREO, passwordEncoder.encode(ADMIN_PASS), RolUsuario.ADMINISTRADOR);
        Usuario student = new Usuario("Student Test", "9-0000-0002",
                STUDENT_CORREO, passwordEncoder.encode(STUDENT_PASS), RolUsuario.ESTUDIANTE);

        // Guardar solo si no existen (re-ejecución segura)
        if (usuarioRepository.findByCorreo(ADMIN_CORREO).isEmpty()) {
            usuarioRepository.save(admin);
        }
        if (usuarioRepository.findByCorreo(STUDENT_CORREO).isEmpty()) {
            student = usuarioRepository.save(student);
        } else {
            student = usuarioRepository.findByCorreo(STUDENT_CORREO).get();
        }
        studentId = student.getId();

        // Obtener tokens mediante login real
        adminToken   = login(ADMIN_CORREO, ADMIN_PASS);
        studentToken = login(STUDENT_CORREO, STUDENT_PASS);

        // Curso 1: LESCO-101 — usado para 201, 403-OWASP
        cursoPublicadoId = cursoRepository.findByCodigo("LESCO-101").orElseThrow().getId();
        List<Leccion> lecciones1 = leccionRepository.findByCursoIdOrderByOrdenAsc(cursoPublicadoId);
        leccionId = lecciones1.get(0).getId();

        // Curso 2: LESCO-102 — usado para 422 (evita duplicado-matricula 409 del test_201)
        curso2Id = cursoRepository.findByCodigo("LESCO-102").orElseThrow().getId();
        List<Leccion> lecciones2 = leccionRepository.findByCursoIdOrderByOrdenAsc(curso2Id);
        leccion2Id = lecciones2.get(0).getId();
    }

    // -----------------------------------------------------------------------
    // 201 — recurso creado correctamente
    // -----------------------------------------------------------------------

    @Test
    @Order(1)
    @DisplayName("API 201: POST /api/v1/matriculas — ESTUDIANTE crea su propia matrícula")
    void test_201_matricularEstudiante() {
        MatriculaRequestDTO body = new MatriculaRequestDTO(
                studentId, cursoPublicadoId, leccionId, MetodoPago.TARJETA, null);

        ResponseEntity<String> resp = post("/api/v1/matriculas", body, studentToken);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getHeaders().getLocation()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // 400 — cuerpo inválido (Bean Validation)
    // -----------------------------------------------------------------------

    @Test
    @Order(2)
    @DisplayName("API 400: POST /auth/login — correo ausente lanza validación Bean")
    void test_400_loginSinCorreo() {
        // Cuerpo con contraseña pero sin correo
        String bodyJson = """
                {"contrasena":"Admin1234!"}
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> resp = restTemplate.postForEntity(
                url("/auth/login"),
                new HttpEntity<>(bodyJson, headers),
                String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // -----------------------------------------------------------------------
    // 401 — sin token
    // -----------------------------------------------------------------------

    @Test
    @Order(3)
    @DisplayName("API 401: GET /api/v1/matriculas — petición sin Authorization header")
    void test_401_sinToken() {
        ResponseEntity<String> resp = restTemplate.getForEntity(
                url("/api/v1/matriculas"), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        // Verificar que es Problem Detail (sin traza Java)
        assertThat(resp.getBody()).contains("no-autenticado");
        assertThat(resp.getBody()).doesNotContain("Exception");
    }

    // -----------------------------------------------------------------------
    // 403 — rol incorrecto (ESTUDIANTE intenta endpoint de ADMINISTRADOR)
    // -----------------------------------------------------------------------

    @Test
    @Order(4)
    @DisplayName("API 403: POST /api/cursos/{id}/publicar — ESTUDIANTE no tiene rol ADMINISTRADOR")
    void test_403_estudianteIntentaPublicar() {
        String body = """
                {"administradorId":999}
                """;
        ResponseEntity<String> resp = post(
                "/api/cursos/" + cursoPublicadoId + "/publicar", body, studentToken);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody()).contains("acceso-denegado");
        assertThat(resp.getBody()).doesNotContain("Exception");
    }

    // -----------------------------------------------------------------------
    // 403 — OWASP API1: ESTUDIANTE intenta cancelar la matrícula de otro
    // -----------------------------------------------------------------------

    @Test
    @Order(5)
    @DisplayName("API 403 (OWASP API1): POST cancelar — ESTUDIANTE no puede cancelar matrícula ajena")
    void test_403_owaspApi1_cancelarMatriculaAjena() {
        // MAT-2026-000001 pertenece a Luis (semilla), no al studentTest
        ResponseEntity<String> resp = post("/api/v1/matriculas/1/cancelar", null, studentToken);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody()).contains("OWASP API1");
    }

    // -----------------------------------------------------------------------
    // 404 — recurso inexistente
    // -----------------------------------------------------------------------

    @Test
    @Order(6)
    @DisplayName("API 404: GET /api/v1/matriculas/99999 — matrícula inexistente")
    void test_404_matriculaNoExiste() {
        ResponseEntity<String> resp = get("/api/v1/matriculas/99999", adminToken);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).contains("entidad-no-encontrada");
        assertThat(resp.getBody()).doesNotContain("Exception");
    }

    // -----------------------------------------------------------------------
    // 422 — regla de negocio (referencia SINPE inválida)
    // -----------------------------------------------------------------------

    @Test
    @Order(7)
    @DisplayName("API 422: POST /api/v1/matriculas — referencia SINPE inválida (no 8 dígitos)")
    void test_422_reglaNegocioSinpeInvalido() {
        // Usamos LESCO-102 (curso2Id) para no colisionar con la matrícula PENDIENTE en LESCO-101
        MatriculaRequestDTO body = new MatriculaRequestDTO(
                studentId, curso2Id, leccion2Id,
                MetodoPago.SINPE_MOVIL, "LETRAS"); // referencia no numérica

        ResponseEntity<String> resp = post("/api/v1/matriculas", body, studentToken);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resp.getBody()).contains("regla-negocio");
    }

    // -----------------------------------------------------------------------
    // 401 — token inválido / malformado
    // -----------------------------------------------------------------------

    @Test
    @Order(8)
    @DisplayName("API 401: token JWT malformado es rechazado")
    void test_401_tokenInvalido() {
        ResponseEntity<String> resp = get("/api/v1/matriculas", "token-invalido-xxx");

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String login(String correo, String pass) {
        LoginRequestDTO req = new LoginRequestDTO(correo, pass);
        ResponseEntity<LoginResponseDTO> resp = restTemplate.postForEntity(
                url("/auth/login"), req, LoginResponseDTO.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        return resp.getBody().token();
    }

    private ResponseEntity<String> get(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return restTemplate.exchange(url(path), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
    }

    private ResponseEntity<String> post(String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.setBearerAuth(token);
        return restTemplate.postForEntity(url(path),
                new HttpEntity<>(body, headers), String.class);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
