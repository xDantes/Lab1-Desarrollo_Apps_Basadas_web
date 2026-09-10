package cr.ac.una.lab1;

import cr.ac.una.lab1.data.Curso;
import cr.ac.una.lab1.data.CursoRepository;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.EstadoPago;
import cr.ac.una.lab1.data.Instructor;
import cr.ac.una.lab1.data.InstructorRepository;
import cr.ac.una.lab1.data.Leccion;
import cr.ac.una.lab1.data.LeccionRepository;
import cr.ac.una.lab1.data.Matricula;
import cr.ac.una.lab1.data.MatriculaRepository;
import cr.ac.una.lab1.data.MetodoPago;
import cr.ac.una.lab1.data.Pago;
import cr.ac.una.lab1.data.PagoRepository;
import cr.ac.una.lab1.data.RolUsuario;
import cr.ac.una.lab1.data.Usuario;
import cr.ac.una.lab1.data.UsuarioRepository;
import cr.ac.una.lab1.data.mongo.CategoriaSena;
import cr.ac.una.lab1.data.mongo.Comentario;
import cr.ac.una.lab1.data.mongo.ComentarioRepository;
import cr.ac.una.lab1.data.mongo.MultimediaSena;
import cr.ac.una.lab1.data.mongo.RecursoMultimedia;
import cr.ac.una.lab1.data.mongo.RecursoMultimediaRepository;
import cr.ac.una.lab1.data.mongo.RespuestaComentario;
import cr.ac.una.lab1.data.mongo.SenaLesco;
import cr.ac.una.lab1.data.mongo.SenaLescoRepository;
import cr.ac.una.lab1.data.mongo.TipoMultimedia;
import cr.ac.una.lab1.data.specification.CursoSpecification;
import cr.ac.una.lab1.data.specification.MatriculaSpecification;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIf("cr.ac.una.lab1.DockerUtils#isDockerAvailable")
class PersistenciaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private LeccionRepository leccionRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private SenaLescoRepository senaLescoRepository;

    @Autowired
    private RecursoMultimediaRepository recursoMultimediaRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Order(1)
    @DisplayName("Prueba 1: Validar esquema Flyway y persistencia de entidades JPA (ddl-auto=validate)")
    @Transactional(readOnly = true)
    void test1_ValidarEsquemaFlywayYEntidades() {
        // Verificar que los datos semilla aplicados por Flyway están mapeados correctamente
        List<Usuario> usuarios = usuarioRepository.findAll();
        assertThat(usuarios).isNotEmpty();
        assertThat(usuarios).hasSizeGreaterThanOrEqualTo(6);

        List<Curso> cursos = cursoRepository.findAll();
        assertThat(cursos).hasSizeGreaterThanOrEqualTo(3);

        List<Leccion> lecciones = leccionRepository.findAll();
        assertThat(lecciones).hasSizeGreaterThanOrEqualTo(3);

        List<Matricula> matriculas = matriculaRepository.findAll();
        assertThat(matriculas).hasSizeGreaterThanOrEqualTo(3);

        List<Pago> pagos = pagoRepository.findAll();
        assertThat(pagos).hasSizeGreaterThanOrEqualTo(3);

        // Validar subtipo Instructor con @MapsId
        Optional<Usuario> instructorUser = usuarioRepository.findByCorreo("carlos.instructor@lescocr.cr");
        assertThat(instructorUser).isPresent();
        assertThat(instructorUser.get().getRol()).isEqualTo(RolUsuario.INSTRUCTOR);

        Optional<Instructor> instructor = instructorRepository.findById(instructorUser.get().getId());
        assertThat(instructor).isPresent();
        assertThat(instructor.get().getEspecialidad()).isEqualTo("Lengua de señas para principiantes");
    }

    @Test
    @Order(2)
    @DisplayName("Prueba 2: Demostración del problema N+1 y corrección con JOIN FETCH y @EntityGraph")
    @Transactional
    void test2_DemostracionProblemaNMasUnoYCorreccionFetch() {
        // Limpiar el contexto de persistencia de Hibernate para forzar consultas reales a BD
        entityManager.flush();
        entityManager.clear();

        // 1. Consulta CORREGIDA con JOIN FETCH (1 sola consulta SQL)
        List<Curso> cursosConFetch = cursoRepository.findCursosPublicadosConLecciones();
        assertThat(cursosConFetch).isNotEmpty();
        for (Curso curso : cursosConFetch) {
            // Las lecciones ya están cargadas en memoria, no se dispara ninguna consulta adicional
            assertThat(curso.getLecciones()).isNotNull();
            assertThat(curso.isPublicado()).isTrue();
        }

        entityManager.clear();

        // 2. Consulta alternativa con @EntityGraph (1 sola consulta SQL)
        List<Curso> cursosConGraph = cursoRepository.findCursosPublicadosConEntityGraph();
        assertThat(cursosConGraph).isNotEmpty();
        for (Curso curso : cursosConGraph) {
            assertThat(curso.getLecciones()).isNotNull();
        }

        entityManager.clear();

        // 3. Consulta sin fetch (ejecución perezosa LAZY -> N+1 consultas si se acceden las colecciones)
        List<Curso> cursosSinFetch = cursoRepository.findByPublicadoTrue();
        assertThat(cursosSinFetch).isNotEmpty();
        for (Curso curso : cursosSinFetch) {
            // El acceso a getLecciones() dispararía una consulta SELECT por cada curso (N consultas)
            assertThat(curso.getLecciones()).isNotNull();
        }
    }

    @Test
    @Order(3)
    @DisplayName("Prueba 3: Consulta de Negocio 1 (JPQL) - Cursos publicados con lecciones")
    @Transactional(readOnly = true)
    void test3_ConsultaJPQL1_CursosPublicadosConLecciones() {
        List<Curso> cursosPublicados = cursoRepository.findCursosPublicadosConLecciones();

        assertThat(cursosPublicados).isNotEmpty();
        assertThat(cursosPublicados).allMatch(Curso::isPublicado);

        // Verificar que los cursos 'LESCO-101' y 'LESCO-102' están presentes y 'LESCO-201' (no publicado) está excluido
        List<String> codigos = cursosPublicados.stream().map(Curso::getCodigo).toList();
        assertThat(codigos).contains("LESCO-101", "LESCO-102");
        assertThat(codigos).doesNotContain("LESCO-201");

        // Validar que las lecciones vienen inicializadas
        Curso lesco101 = cursosPublicados.stream()
                .filter(c -> "LESCO-101".equals(c.getCodigo()))
                .findFirst()
                .orElseThrow();
        assertThat(lesco101.getLecciones()).hasSize(2);
    }

    @Test
    @Order(4)
    @DisplayName("Prueba 4: Consulta de Negocio 2 (JPQL) - Matrículas por estudiante con grafo completo")
    @Transactional(readOnly = true)
    void test4_ConsultaJPQL2_MatriculasPorEstudianteConDetalle() {
        Usuario luis = usuarioRepository.findByCorreo("luis.estudiante@lescocr.cr").orElseThrow();

        List<Matricula> matriculas = matriculaRepository.findMatriculasPorUsuarioConDetalleCompleto(luis.getId());

        assertThat(matriculas).isNotEmpty();
        Matricula m = matriculas.get(0);
        assertThat(m.getConsecutivo()).isEqualTo("MAT-2026-000001");
        assertThat(m.getUsuario().getCorreo()).isEqualTo("luis.estudiante@lescocr.cr");
        assertThat(m.getCurso().getCodigo()).isEqualTo("LESCO-101");
        assertThat(m.getLeccion().getTitulo()).isEqualTo("Alfabeto dactilológico");
        assertThat(m.getPago()).isNotNull();
        assertThat(m.getPago().getEstado()).isEqualTo(EstadoPago.APROBADO);
        assertThat(m.getPago().getMetodo()).isEqualTo(MetodoPago.SINPE_MOVIL);
    }

    @Test
    @Order(5)
    @DisplayName("Prueba 5: Consulta de Negocio 3 (Criteria / Specification) - Filtrado dinámico de Cursos")
    @Transactional(readOnly = true)
    void test5_ConsultaCriteriaSpecification_CursosFiltrosDinamicos() {
        // Filtro 1: por texto ("básico") y nivel ("BASICO")
        Specification<Curso> spec1 = CursoSpecification.conFiltros("básico", "BASICO", null, true, null);
        List<Curso> resultado1 = cursoRepository.findAll(spec1);
        assertThat(resultado1).hasSize(1);
        assertThat(resultado1.get(0).getCodigo()).isEqualTo("LESCO-101");

        // Filtro 2: por precio máximo (<= 50000) y publicado = true
        Specification<Curso> spec2 = CursoSpecification.conFiltros(null, null, new BigDecimal("50000.00"), true, null);
        List<Curso> resultado2 = cursoRepository.findAll(spec2);
        assertThat(resultado2).hasSize(1);
        assertThat(resultado2.get(0).getCodigo()).isEqualTo("LESCO-101");

        // Filtro 3: por fecha de inicio después de fecha dada
        Specification<Curso> spec3 = CursoSpecification.conFiltros(null, null, null, null, LocalDate.of(2026, 10, 1));
        List<Curso> resultado3 = cursoRepository.findAll(spec3);
        assertThat(resultado3).hasSize(1);
        assertThat(resultado3.get(0).getCodigo()).isEqualTo("LESCO-201");
    }

    @Test
    @Order(6)
    @DisplayName("Prueba 6: Consulta de Negocio 4 (Criteria / Specification) - Filtrado dinámico de Matrículas")
    @Transactional(readOnly = true)
    void test6_ConsultaCriteriaSpecification_MatriculasFiltrosDinamicos() {
        // Filtro 1: por estado ACTIVA
        Specification<Matricula> specActivas = MatriculaSpecification.conFiltros(null, null, EstadoMatricula.ACTIVA, null, null, null, null);
        List<Matricula> activas = matriculaRepository.findAll(specActivas);
        assertThat(activas).hasSize(1);
        assertThat(activas.get(0).getConsecutivo()).isEqualTo("MAT-2026-000001");

        // Filtro 2: por estado CANCELADA
        Specification<Matricula> specCanceladas = MatriculaSpecification.conFiltros(null, null, EstadoMatricula.CANCELADA, null, null, null, null);
        List<Matricula> canceladas = matriculaRepository.findAll(specCanceladas);
        assertThat(canceladas).hasSize(1);
        assertThat(canceladas.get(0).getConsecutivo()).isEqualTo("MAT-2026-000003");

        // Filtro 3: por rango de precio final (entre 46000 y 50000)
        Specification<Matricula> specPrecio = MatriculaSpecification.conFiltros(
                null, null, null, null, null, new BigDecimal("46000.00"), new BigDecimal("50000.00"));
        List<Matricula> resultadoPrecio = matriculaRepository.findAll(specPrecio);
        assertThat(resultadoPrecio).hasSize(1);
        assertThat(resultadoPrecio.get(0).getConsecutivo()).isEqualTo("MAT-2026-000002");
    }

    @Test
    @Order(7)
    @DisplayName("Prueba 7: Repositorio Genérico Base JPA (BaseRepository CRUD, paginación, ordenamiento)")
    @Transactional
    void test7_RepositorioGenericoBaseJPA() {
        // Probar inserción con BaseRepository
        Usuario nuevoUsuario = new Usuario(
                "Estudiante Test",
                "9-9999-9999",
                "test.estudiante@lescocr.cr",
                "$2a$10$hashparapruebasunitariasytestcontainers00",
                RolUsuario.ESTUDIANTE
        );
        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        assertThat(guardado.getId()).isNotNull();

        // Probar conteo genérico
        long totalUsuarios = usuarioRepository.count();
        assertThat(totalUsuarios).isGreaterThanOrEqualTo(7);

        // Probar paginación y ordenamiento con BaseRepository
        Page<Usuario> pagina = usuarioRepository.findAll(PageRequest.of(0, 2, Sort.by("nombre").ascending()));
        assertThat(pagina.getContent()).hasSize(2);
        assertThat(pagina.getTotalElements()).isEqualTo(totalUsuarios);

        // Probar borrado genérico
        usuarioRepository.delete(guardado);
        assertThat(usuarioRepository.findById(guardado.getId())).isEmpty();
    }

    @Test
    @Order(8)
    @DisplayName("Prueba 8: Repositorio Genérico Base MongoDB (BaseMongoRepository con subdominio NoSQL)")
    void test8_RepositorioGenericoBaseMongoDB() {
        // Limpiar colecciones para prueba aislada
        senaLescoRepository.deleteAll();
        comentarioRepository.deleteAll();
        recursoMultimediaRepository.deleteAll();

        // 1. Probar SenaLescoRepository (Diccionario LESCO)
        CategoriaSena categoriaSaludos = new CategoriaSena("SALUDOS", "Saludos y Cortesía");
        MultimediaSena videoHola = new MultimediaSena(TipoMultimedia.VIDEO, "https://cdn.lescocr.cr/videos/hola.mp4", true);
        SenaLesco senaHola = new SenaLesco(
                "Hola",
                "hola",
                "Seña tradicional para saludar agitando la mano abierta.",
                categoriaSaludos,
                List.of(videoHola),
                List.of("saludo", "bienvenida", "básico"),
                true
        );
        SenaLesco senaGuardada = senaLescoRepository.save(senaHola);
        assertThat(senaGuardada.getId()).isNotNull();
        assertThat(senaLescoRepository.count()).isEqualTo(1);

        Optional<SenaLesco> encontrada = senaLescoRepository.findByPalabraNormalizada("hola");
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getPalabra()).isEqualTo("Hola");
        assertThat(encontrada.get().getCategoria().getCodigo()).isEqualTo("SALUDOS");

        // 2. Probar ComentarioRepository
        Comentario comentario = new Comentario(
                1L,
                4L,
                "Luis Fernández",
                "Excelente lección sobre el alfabeto dactilológico.",
                5
        );
        Comentario comentarioGuardado = comentarioRepository.save(comentario);
        assertThat(comentarioGuardado.getId()).isNotNull();
        assertThat(comentarioRepository.findByCursoIdOrderByCreadoEnDesc(1L)).hasSize(1);

        // 3. Probar RecursoMultimediaRepository
        RecursoMultimedia recurso = new RecursoMultimedia(
                1L,
                1L,
                TipoMultimedia.VIDEO,
                "https://cdn.lescocr.cr/cursos/leccion1/intro.mp4",
                1,
                Map.of("resolucion", "1080p", "duracion_segundos", 180)
        );
        RecursoMultimedia recursoGuardado = recursoMultimediaRepository.save(recurso);
        assertThat(recursoGuardado.getId()).isNotNull();
        assertThat(recursoMultimediaRepository.findByLeccionIdOrderByOrdenAsc(1L)).hasSize(1);

        // Verificar conteo y eliminación genérica de BaseMongoRepository
        senaLescoRepository.delete(senaGuardada);
        assertThat(senaLescoRepository.count()).isZero();
    }
}
