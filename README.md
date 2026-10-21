# LescoCR

Plataforma de apoyo a la enseñanza de LESCO (Lengua de Señas Costarricense), dirigida
a la comunidad LESCO y a cualquier persona interesada en aprenderla gratis. ofrece un
diccionario de señas con búsqueda por imágenes/video, y cursos estructurados en
lecciones con matrícula y pago (simulado).

Proyecto de EIF509 Desarrollo de Aplicaciones Basadas en Web.

Desarrollado por: Derrek Adrián Ureña Solís y José Arrieta Sancho.

## Documentación del proyecto

- [Propuesta de dominio](docs/Propuesta_de_Dominio.pdf) — entidades de negocio, procesos y alcance.
- [Diagrama de arquitectura previsto](docs/diagrama.md)
- [ADR-001 · Elección de stack](docs/adr/ADR-001-EleccionStack.md)
- [Modelo de datos](docs/modelo-datos.md) — esquema PostgreSQL, colecciones MongoDB, justificación embeber/referenciar.
- [Patrones de diseño](docs/patrones.md) — State y Strategy, con la señal que los justificó.
- [Guía de despliegue](docs/DESPLIEGUE.md) — servicios gestionados, variables de entorno y diagrama de despliegue.
- [Módulo MVC con Thymeleaf](#módulo-mvc-con-thymeleaf-laboratorio-6) — vistas con sesión, CSRF y PRG, aparte de la API REST stateless.

## Requisitos previos

- **Java 21** (JDK)
- **Docker** y **Docker Compose** — para levantar PostgreSQL y MongoDB. 
  Si no los tenés instalados en Windows: instalar WSL2 (`wsl --install` desde una PowerShell
  como Administrador, reiniciar) y luego [Docker Desktop](https://www.docker.com/products/docker-desktop/).

No hace falta instalar Gradle: el proyecto trae `gradlew` / `gradlew.bat`.

## Cómo clonarlo y levantarlo

```bash
git clone https://github.com/xDantes/Lab1-Desarrollo_Apps_Basadas_web.git
cd Lab1-Desarrollo_Apps_Basadas_web
```

1. Levantar las bases de datos (PostgreSQL + MongoDB):

```bash
docker compose up -d
```

2. Compilar y correr las pruebas (la app necesita Postgres arriba para este paso,
porque las pruebas levantan el contexto de Spring con JPA + Flyway ya conectados):

```bash
./gradlew build
```

3. Definir `JWT_SECRET` — **obligatoria**, la app no arranca sin ella (a propósito:
   antes tenía un valor por defecto committeado al repo; ver más abajo
   [«Por qué no hay un `JWT_SECRET` por defecto»](#por-qué-no-hay-un-jwt_secret-por-defecto)).
   No hace falta para `./gradlew build`/`test` (las pruebas usan su propio secreto
   de prueba), solo para correr la app de verdad:

```bash
# PowerShell
$env:JWT_SECRET = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))

# Bash / macOS / Linux
export JWT_SECRET=$(openssl rand -base64 32)
```

4. Levantarla:

```bash
./gradlew bootRun
```

Queda en **http://localhost:8080**.

## Comprobar que funciona

```bash
curl http://localhost:8080/actuator/health
```
```json
{"status":"UP"}
```

```bash
curl "http://localhost:8080/api/v1/cursos?publicado=true"
```
```json
{
  "content": [
    {"id":1,"codigo":"LESCO-101","nombre":"LESCO Básico I","nivel":"BASICO","precioFinal":45000.00,"cupoTotal":20,"cuposDisponibles":19,"fechaInicio":"2026-09-07","fechaFin":"2026-11-13"},
    {"id":2,"codigo":"LESCO-102","nombre":"LESCO Intermedio","nivel":"INTERMEDIO","precioFinal":49500.00,"cupoTotal":15,"cuposDisponibles":15,"fechaInicio":"2026-09-07","fechaFin":"2026-12-04"}
  ],
  "totalElements": 2,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```
(`Page` de Spring Data trae más metadatos de los que se muestran arriba — `pageable`, `sort`, `first`, `last`, etc. — se omiten aquí por brevedad.)

Ese resultado viene de los datos semilla de Flyway (`src/main/resources/db/migration`).
`?publicado=true` es uno de los filtros de negocio del endpoint (ver
[Colecciones: paginación, orden y filtros](#colecciones-paginación-orden-y-filtros-laboratorio-5)
más abajo) — sin él, `GET /api/v1/cursos` devuelve los 3 cursos semilla, incluido
`LESCO-201` que todavía no está publicado. `cuposDisponibles` de `LESCO-101` da 19
(no 20) porque la matrícula semilla `MAT-2026-000001` ya está `ACTIVA` en ese curso
(ver [Frontera DTO](#frontera-dto--capa-de-negocio-laboratorio-4) más abajo).

## Cómo Ejecutar las Pruebas de Integración con Testcontainers

Las pruebas de integración levantan contenedores reales de **PostgreSQL 16** y **MongoDB 7.0** usando **Testcontainers**, aplican automáticamente todas las migraciones de Flyway (`V1` a `V8`), validan el mapeo JPA con `spring.jpa.hibernate.ddl-auto=validate`, prueban consultas de negocio, repositorios genéricos, la corrección del problema N+1, y el rollback transaccional del proceso de matriculación.

**Necesitan Docker corriendo, sin excepción.** Antes estas pruebas tenían un
`@EnabledIf(isDockerAvailable)`: si Docker faltaba, se marcaban "skipped" en
silencio y `./gradlew build` igual terminaba en verde, sin haber corrido nada
— un CI o un desarrollador podían creer que todo pasó sin que se ejecutara ni
una sola prueba. Se quitó esa condición: si Docker no está disponible, ahora
la prueba **falla** con el error real de Testcontainers, no se salta.

### Comando para correr las pruebas:

```bash
# En Windows (PowerShell / CMD)
.\gradlew.bat test

.\gradlew.bat test --rerun

# En Linux / macOS
./gradlew test
```

### Qué se espera ver como resultado:

Al ejecutar las pruebas con Docker iniciado (localmente o en el CI del repositorio):
1. Testcontainers descarga e inicia los contenedores `postgres:16-alpine` y `mongo:7.0`.
2. Flyway ejecuta exitosamente las migraciones `V1__crear_usuario.sql` hasta `V10__actualizar_contrasenas_semilla.sql`.
3. Hibernate valida la coherencia entre las entidades JPA y la BD (`Schema-validation: [SUCCESS]`).
4. Se ejecutan en verde (PASSED) **19 pruebas de integración**, en 4 clases:

   [`Lab1ApplicationTests`](src/test/java/cr/ac/una/lab1/Lab1ApplicationTests.java):
   - `contextLoads`: el contexto de Spring arranca contra Postgres/Mongo reales.

   [`PersistenciaIntegrationTest`](src/test/java/cr/ac/una/lab1/PersistenciaIntegrationTest.java) (Laboratorio 3):
   - `test1_ValidarEsquemaFlywayYEntidades`: Verifica el esquema Flyway y las entidades JPA.
   - `test2_DemostracionProblemaNMasUnoYCorreccionFetch`: Demuestra y resuelve el problema N+1.
   - `test3_ConsultaJPQL1_CursosPublicadosConLecciones`: Prueba JPQL de cursos publicados con lecciones.
   - `test4_ConsultaJPQL2_MatriculasPorEstudianteConDetalle`: Prueba JPQL de matrículas con detalle completo.
   - `test5_ConsultaCriteriaSpecification_CursosFiltrosDinamicos`: Prueba Specification dinámica de cursos.
   - `test6_ConsultaCriteriaSpecification_MatriculasFiltrosDinamicos`: Prueba Specification dinámica de matrículas.
   - `test7_RepositorioGenericoBaseJPA`: Prueba operaciones genéricas de `BaseRepository` (CRUD, paginación, sort).
   - `test8_RepositorioGenericoBaseMongoDB`: Prueba operaciones genéricas de `BaseMongoRepository` sobre MongoDB.

   [`TransaccionIntegrationTest`](src/test/java/cr/ac/una/lab1/TransaccionIntegrationTest.java) (Laboratorio 4):
   - `test9_RollbackVerificado_FalloEnPagoRevierteMatricula`: un `@SpyBean` hace fallar `PagoRepository.save()` a mitad del proceso de matriculación y comprueba que la `Matricula` ya persistida también se revierte (transacción verificada).
   - `test10_ReglasNegocioYPatronState`: reglas de `MatriculaService` y las transiciones del patrón State, contra datos reales.

   [`ApiSecurityIntegrationTest`](src/test/java/cr/ac/una/lab1/ApiSecurityIntegrationTest.java) (Laboratorio 5) — la API completa, de punta a punta, vía JWT real emitido por `POST /auth/login`:
   - `test_201_matricularEstudiante`: `POST /api/v1/matriculas` — un ESTUDIANTE crea su propia matrícula.
   - `test_400_loginSinCorreo`: `POST /auth/login` sin correo — rechazado por Bean Validation.
   - `test_401_sinToken`: `GET /api/v1/matriculas` sin header `Authorization`.
   - `test_403_estudianteIntentaPublicar`: un ESTUDIANTE intenta `POST /api/v1/cursos/{id}/publicar` (requiere ADMINISTRADOR).
   - `test_403_owaspApi1_cancelarMatriculaAjena`: un ESTUDIANTE intenta cancelar la matrícula de otro (OWASP API1).
   - `test_404_matriculaNoExiste`: `GET /api/v1/matriculas/99999`.
   - `test_422_reglaNegocioSinpeInvalido`: referencia SINPE con formato inválido.
   - `test_401_tokenInvalido`: un JWT malformado es rechazado.

El reporte HTML completo se genera en: `build/reports/tests/test/index.html`.

---

## Arquitectura del código

El proyecto sigue una separación por capas bajo `cr.ac.una.lab1`:

- `presentation` — controladores REST (`AuthController`, `CursoController`,
  `MatriculaController`, `PagoController`, `InstructorController`,
  `LeccionController`, `UsuarioController`) y `GlobalExceptionHandler`.
- `business` — reglas de negocio, DTOs de entrada/salida y las excepciones propias del
  dominio (`business.exception`), más los patrones State (`business.state`) y
  Strategy (`business.strategy`).
- `data` — entidades JPA y repositorios de PostgreSQL (`Usuario`, `Instructor`, `Curso`,
  `Leccion`, `Matricula`, `Pago`), con filtrado dinámico en `data.specification`
  (`CursoSpecification`, `MatriculaSpecification`); y en `data.mongo` los
  documentos/repositorios de MongoDB (`SenaLesco`, `RecursoMultimedia`, `Comentario`).
- `config` — seguridad stateless con JWT (`SecurityConfig`, `JwtTokenProvider`,
  `JwtAuthenticationFilter`, `UsuarioDetailsServiceImpl`) y documentación OpenAPI
  (`OpenApiConfig`).

## Endpoints de la API

Contrato versionado bajo `/api/v1` (Laboratorio 5). Todas las respuestas de
error usan Problem Details — ver la tabla completa en
[Frontera DTO](#frontera-dto--capa-de-negocio-laboratorio-4-y-5).

| Recurso | Método | Ruta | Quién puede | Códigos típicos |
|---|---|---|---|---|
| Autenticación | `POST` | `/auth/login` | público | 200, 400, 401 |
| Cursos | `GET` | `/api/v1/cursos` | público | 200 |
| Cursos | `GET` | `/api/v1/cursos/{id}` | público | 200, 404 |
| Cursos | `GET` | `/api/v1/cursos/{id}/lecciones` | público | 200, 404 |
| Cursos | `POST` | `/api/v1/cursos/{id}/publicar` | ADMINISTRADOR | 200, 400, 404, 409, 422 |
| Instructores | `GET` | `/api/v1/instructores`, `/{id}` | público | 200, 404 |
| Lecciones | `GET` | `/api/v1/lecciones`, `/{id}` | público | 200, 404 |
| Matrículas | `POST` | `/api/v1/matriculas` | ESTUDIANTE | 201, 400, 401, 409, 422 |
| Matrículas | `GET` | `/api/v1/matriculas` | autenticado (ve lo suyo, salvo ADMINISTRADOR) | 200 |
| Matrículas | `GET` | `/api/v1/matriculas/{id}` | propietario o ADMINISTRADOR | 200, 403, 404 |
| Matrículas | `POST` | `/api/v1/matriculas/{id}/aprobar` | ADMINISTRADOR | 204, 404, 409 |
| Matrículas | `POST` | `/api/v1/matriculas/{id}/cancelar` | propietario (ESTUDIANTE) o ADMINISTRADOR | 204, 403, 404, 409 |
| Pagos | `GET` | `/api/v1/pagos` | autenticado (ve lo suyo, salvo ADMINISTRADOR) | 200 |
| Pagos | `GET` | `/api/v1/pagos/{id}` | propietario o ADMINISTRADOR | 200, 403, 404 |
| Usuarios | `GET` | `/api/v1/usuarios`, `/{id}` | ADMINISTRADOR | 200, 404 |
| Documentación | `GET` | `/v3/api-docs`, `/swagger-ui.html` | público | 200 |

Dos formas de ejercitar esto sin escribir código:

- **Swagger UI**: con la app corriendo, abrir `http://localhost:8080/swagger-ui.html`
  — documentación interactiva generada por [`OpenApiConfig`](src/main/java/cr/ac/una/lab1/config/OpenApiConfig.java)
  (springdoc), con botón **Authorize** para pegar el JWT y probar los endpoints protegidos
  directamente desde el navegador.
- **Colección de peticiones**: [`requests/lesco-api.http`](requests/lesco-api.http)
  — cubre los endpoints de la tabla de arriba (login de los 2 roles, cada recurso, y los
  caminos de error 400/401/403/404/409/422), compatible con el HTTP Client de
  IntelliJ/VS Code (extensión REST Client) sin instalar Postman.

## Persistencia

- PostgreSQL: usuarios, cursos, lecciones, matrículas y pagos. Esquema versionado
  con [Flyway](src/main/resources/db/migration) (3FN, con CHECK/UNIQUE/FK e índices
  justificados en cada migración).
- MongoDB: 3 colecciones creadas por [mongo-init](mongo-init/) al primer arranque
  (validador `$jsonSchema` + índices + datos semilla en cada una):
  - `sena_lesco` — diccionario de señas, con `categoria` y `multimedia` **embebidas**.
  - `recursos_multimedia` — videos/imágenes de lecciones, **referenciando** `leccionId`/`cursoId` de PostgreSQL.
  - `comentarios` — comentarios de cursos, **referenciando** `cursoId`/`usuarioId`, con `respuestas` **embebidas**.

## Notas adicionales

- Los comentarios no eran parte de la propuesta inicial, se implemento para hacer mas amplio el modelo de base de datos y dividir mejor el uso de base de datos para equilibrar su uso y que sea mas amplio su uso y aprendizaje. 

- A la hora de crear la propuesta inicial, cometimos algunos errores en la redaccion con respecto a la relacion del profesor con el curso o leccion, fuimos algo ambiguos, dado que, el plan real es que el profesor esta asociado a la leccion, y de la misma forma el profesor adjunte el material a la leccion y no directamente al curso.

- Tras un analisis, decidimos hacer algunos cambios con el modelado de las tablas, para re distribuir tal como ya se agrego a este documento.

## Arquitectura de Persistencia y Repositorios

El proyecto implementa el patrón **Repository con Generalización** para JPA y MongoDB:

### 1. Jerarquía de Repositorios JPA (PostgreSQL)
- **`BaseRepository<T, ID>`** (`cr.ac.una.lab1.data.base`):
  Interfaz genérica anotada con `@NoRepositoryBean` que combina `JpaRepository<T, ID>` y `JpaSpecificationExecutor<T>`, permitiendo a todos los repositorios contar con operaciones CRUD estándar, paginación, ordenamiento y ejecución de Criteria API dinámica.
- **Repositorios específicos**:
  - `UsuarioRepository extends BaseRepository<Usuario, Long>`
  - `InstructorRepository extends BaseRepository<Instructor, Long>`
  - `CursoRepository extends BaseRepository<Curso, Long>`
  - `LeccionRepository extends BaseRepository<Leccion, Long>`
  - `MatriculaRepository extends BaseRepository<Matricula, Long>`
  - `PagoRepository extends BaseRepository<Pago, Long>`

### 2. Jerarquía de Repositorios MongoDB
- **`BaseMongoRepository<T, ID>`** (`cr.ac.una.lab1.data.mongo.base`):
  Interfaz genérica anotada con `@NoRepositoryBean` que extiende `MongoRepository<T, ID>`.
- **Repositorios específicos del subdominio NoSQL**:
  - `SenaLescoRepository extends BaseMongoRepository<SenaLesco, String>` (Diccionario LESCO).
  - `RecursoMultimediaRepository extends BaseMongoRepository<RecursoMultimedia, String>` (Multimedia de lecciones).
  - `ComentarioRepository extends BaseMongoRepository<Comentario, String>` (Comentarios y retroalimentación de cursos).

### 3. Entidades JPA y Criterio de Carga LAZY
Todas las entidades del dominio (`Usuario`, `Instructor`, `Curso`, `Leccion`, `Matricula`, `Pago`) aplican estrictamente:
- **`fetch = FetchType.LAZY`** en todas las relaciones (`@OneToMany`, `@ManyToOne`, `@OneToOne`) para evitar cargas ansiosas innecesarias en cascada.
- **`spring.jpa.hibernate.ddl-auto=validate`**: Hibernate no genera DDL en tiempo de ejecución; valida estrictamente que la definición de las entidades coincida con las tablas, tipos de columnas, claves primarias y restricciones del esquema Flyway.

---

## Evidencia y Resolución del Problema N+1

### 1. Caso Real en el Dominio: Cursos y sus Lecciones
Al consultar los cursos publicados para mostrar el catálogo con su temario o cantidad de lecciones:
- Cada `Curso` tiene una colección `@OneToMany(fetch = FetchType.LAZY) List<Leccion> lecciones`.
- Si se realiza una consulta simple como `cursoRepository.findByPublicadoTrue()` y luego se recorren los cursos accediendo a `.getLecciones()`, Hibernate realiza **1 consulta** para traer los cursos y luego **N consultas adicionales** (una por cada curso obtenido) para cargar sus lecciones.

### 2. SQL Generado ANTES (N+1 Consultas):
```sql
-- 1ra Consulta: Obtiene los N cursos publicados (ej. 2 cursos)
SELECT c.id, c.codigo, c.nombre, c.descripcion, c.nivel, c.cupo_total, c.precio, c.descuento_porcentaje, c.fecha_inicio, c.fecha_fin, c.publicado, c.creado_en
FROM curso c
WHERE c.publicado = true;

-- 2da Consulta (para curso 1):
SELECT l.id, l.titulo, l.orden, l.curso_id, l.instructor_id, l.creado_en
FROM leccion l
WHERE l.curso_id = 1;

-- 3ra Consulta (para curso 2):
SELECT l.id, l.titulo, l.orden, l.curso_id, l.instructor_id, l.creado_en
FROM leccion l
WHERE l.curso_id = 2;

-- Total de consultas: 1 + N = 3 consultas SQL
```

### 3. SQL Generado DESPUÉS de Corregir con `JOIN FETCH` (1 Única Consulta):
En `CursoRepository`:
```java
@Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.lecciones l WHERE c.publicado = true ORDER BY c.fechaInicio ASC")
List<Curso> findCursosPublicadosConLecciones();
```

**SQL emitido por Hibernate:**
```sql
SELECT DISTINCT c.id, c.codigo, c.nombre, c.descripcion, c.nivel, c.cupo_total, c.precio, c.descuento_porcentaje, c.fecha_inicio, c.fecha_fin, c.publicado, c.creado_en,
       l.id, l.titulo, l.orden, l.curso_id, l.instructor_id, l.creado_en
FROM curso c
LEFT OUTER JOIN leccion l ON c.id = l.curso_id
WHERE c.publicado = true
ORDER BY c.fecha_inicio ASC;

-- Total de consultas: 1 sola consulta SQL
```

---

## 4 Consultas de Negocio y SQL Generado

### Consulta 1 (JPQL): Cursos publicados con lecciones (JOIN FETCH)
- **Ubicación:** `cr.ac.una.lab1.data.CursoRepository.findCursosPublicadosConLecciones()`
- **Código JPQL:**
  ```java
  @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.lecciones l WHERE c.publicado = true ORDER BY c.fechaInicio ASC")
  List<Curso> findCursosPublicadosConLecciones();
  ```
- **SQL Generado:**
  ```sql
  SELECT DISTINCT c1_0.id, c1_0.codigo, c1_0.creado_en, c1_0.descripcion, c1_0.descuento_porcentaje,
                  c1_0.fecha_fin, c1_0.fecha_inicio, c1_0.nivel, c1_0.nombre, c1_0.precio, c1_0.publicado,
                  l1_0.curso_id, l1_0.id, l1_0.creado_en, l1_0.instructor_id, l1_0.orden, l1_0.titulo
  FROM curso c1_0
  LEFT JOIN leccion l1_0 ON c1_0.id = l1_0.curso_id
  WHERE c1_0.publicado = true
  ORDER BY c1_0.fecha_inicio ASC;
  ```

---

### Consulta 2 (JPQL): Matrículas de un estudiante con grafo completo (JOIN FETCH)
- **Ubicación:** `cr.ac.una.lab1.data.MatriculaRepository.findMatriculasPorUsuarioConDetalleCompleto(Long usuarioId)`
- **Código JPQL:**
  ```java
  @Query("SELECT m FROM Matricula m " +
         "JOIN FETCH m.usuario u " +
         "JOIN FETCH m.leccion l " +
         "JOIN FETCH m.curso c " +
         "LEFT JOIN FETCH m.pago p " +
         "WHERE u.id = :usuarioId " +
         "ORDER BY m.fecha DESC")
  List<Matricula> findMatriculasPorUsuarioConDetalleCompleto(@Param("usuarioId") Long usuarioId);
  ```
- **SQL Generado:**
  ```sql
  SELECT m1_0.id, m1_0.consecutivo, m1_0.estado, m1_0.fecha, m1_0.precio_final,
         u1_0.id, u1_0.contrasena_hash, u1_0.correo, u1_0.creado_en, u1_0.identificacion, u1_0.nombre, u1_0.rol,
         l1_0.id, l1_0.creado_en, l1_0.curso_id, l1_0.instructor_id, l1_0.orden, l1_0.titulo,
         c1_0.id, c1_0.codigo, c1_0.creado_en, c1_0.descripcion, c1_0.descuento_porcentaje, c1_0.fecha_fin, c1_0.fecha_inicio, c1_0.nivel, c1_0.nombre, c1_0.precio, c1_0.publicado,
         p1_0.id, p1_0.actualizado_en, p1_0.estado, p1_0.fecha, p1_0.metodo, p1_0.monto, p1_0.referencia
  FROM matricula m1_0
  JOIN usuario u1_0 ON u1_0.id = m1_0.usuario_id
  JOIN leccion l1_0 ON l1_0.id = m1_0.leccion_id
  JOIN curso c1_0 ON c1_0.id = m1_0.curso_id
  LEFT JOIN pago p1_0 ON m1_0.id = p1_0.matricula_id
  WHERE u1_0.id = ?
  ORDER BY m1_0.fecha DESC;
  ```

---

### Consulta 3 (Criteria / Specification Dinámica): Filtrado multi-criterio de Cursos
- **Ubicación:** `cr.ac.una.lab1.data.specification.CursoSpecification.conFiltros(...)`
- **Código Criteria / Specification:**
  ```java
  public static Specification<Curso> conFiltros(
          String texto, String nivel, BigDecimal precioMax, Boolean publicado, LocalDate fechaInicioDespuesDe) {
      return (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (texto != null && !texto.isBlank()) {
              String pattern = "%" + texto.trim().toLowerCase() + "%";
              predicates.add(cb.or(
                  cb.like(cb.lower(root.get("nombre")), pattern),
                  cb.like(cb.lower(root.get("codigo")), pattern)
              ));
          }
          if (nivel != null && !nivel.isBlank()) predicates.add(cb.equal(root.get("nivel"), nivel.trim()));
          if (precioMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
          if (publicado != null) predicates.add(cb.equal(root.get("publicado"), publicado));
          if (fechaInicioDespuesDe != null) predicates.add(cb.greaterThanOrEqualTo(root.get("fechaInicio"), fechaInicioDespuesDe));
          return cb.and(predicates.toArray(new Predicate[0]));
      };
  }
  ```
- **SQL Generado (con filtros activos):**
  ```sql
  SELECT c1_0.id, c1_0.codigo, c1_0.creado_en, c1_0.descripcion, c1_0.descuento_porcentaje,
         c1_0.fecha_fin, c1_0.fecha_inicio, c1_0.nivel, c1_0.nombre, c1_0.precio, c1_0.publicado
  FROM curso c1_0
  WHERE (lower(c1_0.nombre) LIKE ? OR lower(c1_0.codigo) LIKE ?)
    AND c1_0.nivel = ?
    AND c1_0.precio <= ?
    AND c1_0.publicado = ?
    AND c1_0.fecha_inicio >= ?;
  ```

---

### Consulta 4 (Criteria / Specification Dinámica): Filtrado multi-criterio de Matrículas
- **Ubicación:** `cr.ac.una.lab1.data.specification.MatriculaSpecification.conFiltros(...)`
- **Código Criteria / Specification:**
  ```java
  public static Specification<Matricula> conFiltros(
          Long usuarioId, Long cursoId, EstadoMatricula estado,
          OffsetDateTime fechaDesde, OffsetDateTime fechaHasta,
          BigDecimal precioFinalMin, BigDecimal precioFinalMax) {
      return (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (usuarioId != null) predicates.add(cb.equal(root.get("usuario").get("id"), usuarioId));
          if (cursoId != null) predicates.add(cb.equal(root.get("curso").get("id"), cursoId));
          if (estado != null) predicates.add(cb.equal(root.get("estado"), estado));
          if (fechaDesde != null) predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), fechaDesde));
          if (fechaHasta != null) predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), fechaHasta));
          if (precioFinalMin != null) predicates.add(cb.greaterThanOrEqualTo(root.get("precioFinal"), precioFinalMin));
          if (precioFinalMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("precioFinal"), precioFinalMax));
          return cb.and(predicates.toArray(new Predicate[0]));
      };
  }
  ```
- **SQL Generado (con filtros activos):**
  ```sql
  SELECT m1_0.id, m1_0.consecutivo, m1_0.curso_id, m1_0.estado, m1_0.fecha, m1_0.leccion_id, m1_0.precio_final, m1_0.usuario_id
  FROM matricula m1_0
  WHERE m1_0.usuario_id = ?
    AND m1_0.curso_id = ?
    AND m1_0.estado = ?
    AND m1_0.fecha >= ?
    AND m1_0.fecha <= ?
    AND m1_0.precio_final >= ?
    AND m1_0.precio_final <= ?;
  ```

---

## Frontera DTO — Capa de Negocio (Laboratorio 4 y 5)

Los dos procesos de negocio (matriculación y publicación de curso) reciben y
devuelven exclusivamente DTOs (`record`); ninguna entidad JPA (`Curso`,
`Matricula`, `Usuario`, `Leccion`, `Pago`...) sale del paquete `business` hacia
`presentation`. El mapeo es manual (sin MapStruct), en un método privado de
cada servicio. Patrones de diseño usados dentro de estos servicios (State,
Strategy): ver [docs/patrones.md](docs/patrones.md). Todas las rutas viven bajo
`/api/v1` (Laboratorio 5: contrato versionado).

### Proceso 1 — Matriculación de un estudiante

- **Entrada:** [`MatriculaRequestDTO`](src/main/java/cr/ac/una/lab1/business/MatriculaRequestDTO.java)
  — `record` con Bean Validation (`@NotNull` en `estudianteId`/`cursoId`/`leccionId`/`metodoPago`,
  `@Size(max = 50)` en `referencia`). El formato de `referencia` (8 dígitos para
  SINPE, 6-30 caracteres para transferencia, ninguna para tarjeta) **no** se valida
  con Bean Validation porque depende de otro campo (`metodoPago`): esa validación
  condicional la hace el patrón Strategy dentro del servicio, no el DTO.
- **Salida:** [`MatriculaResponseDTO`](src/main/java/cr/ac/una/lab1/business/MatriculaResponseDTO.java)
  — combina datos de `Matricula`, `Curso`, `Leccion` y `Pago` en un solo `record` plano.
- **Mapeo:** `MatriculaService.toResponseDTO(Matricula, Curso, Pago)` (privado, manual).

Ejemplo real contra los datos semilla (Diego, `usuarioId=6`, tenía una matrícula
`CANCELADA` en `LESCO-101` — puede volver a matricularse en ese mismo curso).
`POST` devuelve **201 Created** con el header **`Location`** apuntando al recurso
que se acaba de crear (`GET /api/v1/matriculas/{id}`, ver más abajo):

```bash
curl -i -X POST http://localhost:8080/api/v1/matriculas \
  -H "Content-Type: application/json" \
  -d '{"estudianteId":6,"cursoId":1,"leccionId":1,"metodoPago":"TARJETA","referencia":null}'
```
```
HTTP/1.1 201
Location: http://localhost:8080/api/v1/matriculas/4
```
```json
{
  "matriculaId": 4,
  "consecutivo": "MAT-2026-000004",
  "estudianteId": 6,
  "nombreEstudiante": "Diego Chacón Ureña",
  "cursoId": 1,
  "codigoCurso": "LESCO-101",
  "nombreCurso": "LESCO Básico I",
  "leccionId": 1,
  "tituloLeccion": "Alfabeto dactilológico",
  "estadoMatricula": "PENDIENTE",
  "precioFinal": 45000.00,
  "fechaMatricula": "2026-09-24T10:15:00Z",
  "pagoId": 4,
  "estadoPago": "PENDIENTE",
  "metodoPago": "TARJETA"
}
```

(`fechaMatricula` la pone la base de datos al insertar — va a mostrar la hora real
en la que corriste el comando, no la del ejemplo.)

`GET /api/v1/matriculas/{id}` devuelve el mismo `MatriculaResponseDTO`. Las
acciones de transición de estado (`POST .../aprobar`, `POST .../cancelar`)
devuelven **204 No Content** — sin cuerpo: el cliente ya sabe a qué estado
transicionó y puede releer el recurso con el `GET` anterior si necesita el
detalle completo.

```bash
curl -i -X POST http://localhost:8080/api/v1/matriculas/4/aprobar
```
```
HTTP/1.1 204
```

### Proceso 2 — Publicación y habilitación de un curso

- **Entrada:** [`PublicarCursoRequestDTO`](src/main/java/cr/ac/una/lab1/business/PublicarCursoRequestDTO.java)
  — `record` con un único campo, `administradorId`, validado `@NotNull`.
- **Salida:** [`CursoCatalogoDTO`](src/main/java/cr/ac/una/lab1/business/CursoCatalogoDTO.java)
  — el mismo `record` que ya usa `GET /api/v1/cursos` para el catálogo
  (un solo DTO de salida reutilizado en varios endpoints, sin duplicarlo).
- **Mapeo:** `CursoService.aCatalogoDTO(Curso)` (privado, manual).

Con los datos semilla actuales no hay ningún curso en estado "publicable"
(`LESCO-101`/`LESCO-102` ya están publicados, `LESCO-201` no tiene lecciones),
así que en vez de fabricar una publicación exitosa artificial, estos son los
dos caminos de regla que sí se pueden reproducir tal cual contra la base semilla
— y que muestran cómo `GlobalExceptionHandler` traduce una `ReglaNegocioException`
a **Problem Details (RFC 9457)**, nunca a un stack trace ni a la entidad cruda:

```bash
curl -i -X POST http://localhost:8080/api/v1/cursos/1/publicar \
  -H "Content-Type: application/json" -d '{"administradorId":1}'   # LESCO-101, ya publicado
```
```json
{
  "type": "https://lescocr.cr/problems/regla-negocio",
  "title": "Regla de negocio violada",
  "status": 422,
  "detail": "El curso 'LESCO-101' ya está publicado."
}
```

```bash
curl -i -X POST http://localhost:8080/api/v1/cursos/3/publicar \
  -H "Content-Type: application/json" -d '{"administradorId":1}'   # LESCO-201, sin lecciones
```
```json
{
  "type": "https://lescocr.cr/problems/regla-negocio",
  "title": "Regla de negocio violada",
  "status": 422,
  "detail": "No se puede publicar el curso 'LESCO-201' porque no tiene ninguna lección asignada."
}
```

Otras excepciones de negocio tienen su propio `type`/`status` en vez de caer en
el genérico 422 — por ejemplo `SinCupoDisponibleException`,
`MatriculaDuplicadaException` y `CambioEstadoInvalidoException` responden con
**409 Conflict** (son conflictos con el estado actual del recurso, no errores
de validación). Mapeo completo en
[`GlobalExceptionHandler`](src/main/java/cr/ac/una/lab1/presentation/GlobalExceptionHandler.java):

| Excepción / causa | Status | `type` |
|---|---|---|
| `CredencialesInvalidasException` (`POST /auth/login` falla) | 401 | `.../problems/credenciales-invalidas` |
| `EntidadNoEncontradaException` | 404 | `.../problems/entidad-no-encontrada` |
| `AccesoNoAutorizadoException` (OWASP API1 — recurso ajeno) | 403 | `.../problems/acceso-no-autorizado` |
| `CambioEstadoInvalidoException` | 409 | `.../problems/cambio-estado-invalido` |
| `MatriculaDuplicadaException` | 409 | `.../problems/matricula-duplicada` |
| `SinCupoDisponibleException` | 409 | `.../problems/sin-cupo-disponible` |
| `ReglaNegocioException` (resto de reglas) | 422 | `.../problems/regla-negocio` |
| `@Valid` rechaza el DTO (`MethodArgumentNotValidException`) | 400 | `.../problems/validacion` |
| Parámetro con tipo inválido, p. ej. `GET /api/v1/cursos/abc` | 400 | `.../problems/parametro-invalido` |
| Cuerpo no es JSON válido (`HttpMessageNotReadableException`) | 400 | `.../problems/cuerpo-invalido` |
| Ruta inexistente (`NoResourceFoundException`/`NoHandlerFoundException`) | 404 | `.../problems/ruta-no-encontrada` |
| Cualquier otra excepción no prevista | 500 | `.../problems/error-interno` |

Las últimas dos filas existen porque el `@ExceptionHandler(Exception.class)`
genérico, al capturar *todo*, también interceptaba excepciones que Spring ya
sabía traducir correctamente por su cuenta — un JSON malformado y una ruta
inexistente respondían 500 en vez de 400/404. Como Spring resuelve siempre por
el handler más específico, bastó con declarar uno para cada una.

El último caso es la red de seguridad: nunca devuelve `ex.getMessage()` ni la
traza real, solo confirma que algo falló — así se cumple "ninguna traza
expuesta" incluso para errores que ningún handler específico contempló.

### Verificación de que ninguna entidad cruza la frontera

Los 7 controladores (`AuthController`, `CursoController`, `MatriculaController`,
`PagoController`, `InstructorController`, `LeccionController`, `UsuarioController`)
solo reciben y devuelven tipos de `cr.ac.una.lab1.business` (los DTOs) y de
Spring; ningún método público recibe ni devuelve `Curso`, `Matricula`,
`Usuario`, `Leccion`, `Pago` ni `Instructor`. Comando usado para confirmarlo
(sin resultados = sin fugas; el patrón exige que la palabra no venga pegada a
otra, para no contar falsos positivos como `EstadoMatricula`/`RolUsuario`,
que son enums, no entidades):

```bash
grep -rnE "(^|[^A-Za-z])(Curso|Matricula|Usuario|Leccion|Pago|Instructor) " src/main/java/cr/ac/una/lab1/presentation/
```

---

## Colecciones: paginación, orden y filtros (Laboratorio 5)

`GET /api/v1/cursos` y `GET /api/v1/matriculas` son colecciones paginadas, no
listas completas: reciben un `Pageable` (`?page=&size=&sort=`) que Spring
resuelve automáticamente, y delegan el filtrado a las Specifications que ya
existían desde el Laboratorio 3
([`CursoSpecification`](src/main/java/cr/ac/una/lab1/data/specification/CursoSpecification.java),
[`MatriculaSpecification`](src/main/java/cr/ac/una/lab1/data/specification/MatriculaSpecification.java))
— `CursoService.buscar(...)`/`MatriculaService.buscar(...)` arman la
`Specification` a partir de los parámetros no nulos y le piden al repositorio
`findAll(spec, pageable)` (ya disponible porque `BaseRepository` extiende
`JpaSpecificationExecutor`), y mapean la `Page<Entidad>` resultante a
`Page<DTO>` con `.map(...)` sin perder los metadatos de paginación.

**Cursos** — filtros: `texto` (nombre o código), `nivel`, `precioMax`,
`publicado`, `fechaInicioDespuesDe`.

```bash
curl "http://localhost:8080/api/v1/cursos?nivel=BASICO&precioMax=50000&sort=precio,asc&page=0&size=5"
```

**Matrículas** — filtros: `usuarioId`, `cursoId`, `estado`, `fechaDesde`,
`fechaHasta`, `precioFinalMin`, `precioFinalMax`.

```bash
curl "http://localhost:8080/api/v1/matriculas?cursoId=1&estado=ACTIVA&sort=fecha,desc"
```
```json
{
  "content": [
    {"matriculaId":1,"consecutivo":"MAT-2026-000001","estudianteId":4,"nombreEstudiante":"Luis Fernández Rojas","cursoId":1,"codigoCurso":"LESCO-101","estadoMatricula":"ACTIVA", "...": "..."}
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

Todos los filtros son opcionales e independientes entre sí (combinados con
`AND` dentro de la Specification), así que se pueden mezclar libremente, por
ejemplo `?cursoId=1&estado=ACTIVA&precioFinalMin=40000`.

---

## Seguridad JWT

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"luis.estudiante@lescocr.cr","contrasena":"Student1234!"}'
```

Devuelve `{ "token": "...", "tipo": "Bearer", ... }` (ver credenciales semilla
en [V10](src/main/resources/db/migration/V10__actualizar_contrasenas_semilla.sql)).
Ese token va en cada petición protegida: `Authorization: Bearer <token>`.

### Por qué no hay un `JWT_SECRET` por defecto

`application.properties` tenía `app.jwt.secret=${JWT_SECRET:lescocr-jwt-dev-secret-key-...}`
— un valor de respaldo **committeado al repositorio**. Cualquiera con acceso al
código (público desde el primer commit que lo agregó) podía leer esa clave y
firmar sus propios tokens válidos, para cualquier rol, sin saber ninguna
contraseña — el secreto de firma es lo único que protege un JWT, así que un
secreto público equivale a que no haya autenticación.

Se quitó el valor de respaldo (`app.jwt.secret=${JWT_SECRET}`, sin default):
si `JWT_SECRET` no está definida, la aplicación **no arranca**, en vez de
arrancar silenciosamente con una clave insegura. Fallar al iniciar es
molesto; emitir tokens firmados con una clave que cualquiera puede leer en
GitHub es mucho peor.

- **Al correr la app de verdad** (`./gradlew bootRun`), hay que exportar
  `JWT_SECRET` a mano (ver [«Cómo clonarlo y levantarlo»](#cómo-clonarlo-y-levantarlo)) —
  Spring Boot no lee `.env` automáticamente, solo Docker Compose lo hace para
  los contenedores de las bases de datos.
- **Las pruebas no necesitan nada de esto**: [`AbstractIntegrationTest`](src/test/java/cr/ac/una/lab1/AbstractIntegrationTest.java)
  inyecta un secreto propio, exclusivo de pruebas, vía `@DynamicPropertySource`
  — el mismo mecanismo que ya usaba para apuntar Postgres/Mongo a los
  contenedores de Testcontainers en vez de a `localhost`.
- El secreto que quedó en el historial de Git ya está comprometido (es
  público) — si este proyecto se llega a desplegar de verdad, generar uno
  nuevo (`openssl rand -base64 32`) es obligatorio, no opcional.

---

## Módulo MVC con Thymeleaf (Laboratorio 6)

Además de la API REST stateless (JWT), la aplicación expone un segundo
módulo bajo `/mvc/**`: vistas renderizadas en el servidor con
[Thymeleaf](https://www.thymeleaf.org/), pensado para un administrador o
estudiante que navega desde el propio navegador en vez de consumir la API.
Reutiliza las mismas clases de `business` que la API — ninguna regla de
negocio se duplicó, los controladores MVC solo traducen entre formularios
HTML y los DTOs existentes.

### Por qué una segunda cadena de seguridad

La API REST es **stateless** (cada petición lleva su propio JWT) y por eso
tiene CSRF deshabilitado — no hay nada que un ataque CSRF pueda explotar
cuando el navegador no adjunta credenciales automáticamente. El módulo MVC
es justo lo contrario: usa **sesión** (login por formulario, cookie de
sesión) donde CSRF sí es necesario. En vez de forzar un único modelo de
seguridad para los dos casos, [`SecurityConfig`](src/main/java/cr/ac/una/lab1/config/SecurityConfig.java)
define **dos `SecurityFilterChain` independientes** con `@Order` y
`.securityMatcher(...)`: una para `/mvc/**` (sesión + CSRF + login por
formulario) y otra para el resto (JWT + stateless, la que ya existía). Ver
el Javadoc de la clase para la matriz de autorización completa de cada una.

### Casos de uso

| Caso de uso | Rutas | Quién |
|---|---|---|
| Publicar un curso | `GET/POST /mvc/cursos/{id}/publicar` | ADMINISTRADOR |
| Matricularse en una lección | `GET/POST /mvc/matriculas/nueva` | ESTUDIANTE |
| Catálogo de cursos (listado paginado) | `GET /mvc/cursos` | público |
| Mis matrículas (listado paginado, filtro por estado) | `GET /mvc/matriculas` | autenticado |
| Ingresar / salir | `GET /mvc/login`, `POST /mvc/logout` | público |

Controladores: [`CursoMvcController`](src/main/java/cr/ac/una/lab1/presentation/CursoMvcController.java),
[`MatriculaMvcController`](src/main/java/cr/ac/una/lab1/presentation/MatriculaMvcController.java),
[`MvcLoginController`](src/main/java/cr/ac/una/lab1/presentation/MvcLoginController.java).
Plantillas en [`src/main/resources/templates`](src/main/resources/templates).

### Post/Redirect/Get y CSRF

Los dos `POST` (publicar curso, crear matrícula) siguen el patrón **PRG**:
si la operación tiene éxito, el controlador responde con un `redirect:` al
listado correspondiente (nunca renderiza una vista directo sobre un `POST`)
— así un refresh del navegador repite el `GET` de siempre y nunca reenvía
el formulario. El mensaje de éxito se pasa con `RedirectAttributes` (flash
attribute), visible una sola vez tras la redirección.

El token **CSRF** no se maneja a mano: cualquier `<form th:action="@{...}">`
en una plantilla de este módulo lo recibe automáticamente como campo oculto
(`CsrfRequestDataValueProcessor`, autoconfigurado por Spring Boot al tener
Thymeleaf + Spring Security en el classpath). Si se omite `th:action` por
un `action="..."` plano, el campo no se inyecta y el `POST` responde 403 —
por eso todos los formularios del módulo usan la sintaxis de Thymeleaf.

Formulario validado: `POST /mvc/matriculas/nueva` usa Bean Validation sobre
[`NuevaMatriculaFormDTO`](src/main/java/cr/ac/una/lab1/presentation/NuevaMatriculaFormDTO.java)
(`@NotNull` en lección y método de pago); los errores de campo se muestran
inline con `th:errors`, y las reglas de negocio que fallan en el servicio
(p. ej. cupo lleno, matrícula duplicada) se muestran igual sin perder lo
que el usuario ya llenó en el formulario.

### Probarlo localmente

Con la app corriendo (`./gradlew bootRun`, ver [«Cómo clonarlo y
levantarlo»](#cómo-clonarlo-y-levantarlo)), abrir `http://localhost:8080/mvc/cursos`
en el navegador. Ingresar con las mismas credenciales semilla de la API
(ver [Seguridad JWT](#seguridad-jwt)) — un correo `ADMINISTRADOR` para
publicar cursos, uno `ESTUDIANTE` para matricularse.

## Configuración y perfiles (dev / prod)

Dos perfiles de Spring, uno por entorno — nunca un solo archivo con `if` por entorno:

- [`application.properties`](src/main/resources/application.properties) — perfil
  **`dev`** (activo por defecto, `spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}`).
  Todo tiene un valor de respaldo que coincide con `docker-compose.yml`, así que
  corre local sin crear ningún `.env` (excepto `JWT_SECRET`, ver arriba).
- [`application-prod.properties`](src/main/resources/application-prod.properties) — perfil
  **`prod`**. Ninguna variable tiene valor de respaldo: si falta una en el panel
  del proveedor, la app no arranca (mismo criterio *fail-fast* que `JWT_SECRET`).
  Se activa con `SPRING_PROFILES_ACTIVE=prod`.

### Variables de entorno

Solo los **nombres** — los valores reales se configuran únicamente en el panel
del proveedor (Render/Railway/Neon/Atlas) o en un `.env` local que nunca se
commitea (ver [`.env.example`](.env.example) y [`spa/.env.example`](spa/.env.example)
para el formato esperado de cada una, sin valores reales).

| Variable | Dónde | Para qué |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | API (dev y prod) | Selecciona el perfil (`dev` local, `prod` en el proveedor). |
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT` | API (dev) + `docker-compose.yml` | Conexión a Postgres local. |
| `MONGO_ROOT_USER`, `MONGO_ROOT_PASSWORD`, `MONGO_DB`, `MONGO_PORT` | API (dev) + `docker-compose.yml` | Conexión a Mongo local. |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | API (prod) | Conexión a Postgres gestionado (Neon/Render, con SSL). |
| `SPRING_DATA_MONGODB_URI` | API (prod) | Conexión a MongoDB Atlas. |
| `JWT_SECRET` | API (dev y prod) | Clave de firma HS256. **Obligatoria**, sin valor de respaldo (ver arriba). |
| `JWT_EXPIRATION_MS` | API (dev y prod, opcional) | Vigencia del token en milisegundos (default 24h). |
| `CORS_ALLOWED_ORIGINS` | API (dev y prod) | Orígenes permitidos; en prod debe ser el dominio real donde quede publicada la SPA. |
| `PORT` | API (prod) | Puerto que inyecta el proveedor gestionado. |
| `VITE_API_URL` | SPA (`spa/.env`, build-time) | URL pública de la API que consume la SPA. |

Guía paso a paso de dónde configurar cada una en el proveedor, diagrama de
despliegue y checklist de verificación: [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md).

---

## Pruebas Unitarias con Mockito y Cobertura JaCoCo (Laboratorio 4)

A diferencia de las pruebas de [Testcontainers](#cómo-ejecutar-las-pruebas-de-integración-con-testcontainers)
(que levantan Postgres/Mongo reales), estas son **pruebas unitarias puras**:
`MatriculaService` y `CursoService` se instancian a mano con sus repositorios
simulados con Mockito (`@Mock`), sin contexto de Spring, sin Docker y sin base
de datos — por eso corren en milisegundos.

### Comando para correr solo las pruebas unitarias

```bash
./gradlew test --tests "cr.ac.una.lab1.business.*"
```

(`./gradlew test` a secas corre además las de Testcontainers, que si necesitan Docker arriba.)

### 74 pruebas unitarias, organizadas por clase

| Clase | Qué cubre | # |
|---|---|---|
| [`MatriculaServiceTest`](src/test/java/cr/ac/una/lab1/business/MatriculaServiceTest.java) | `matricular()`: camino feliz + caminos de regla (estudiante/curso/lección inexistentes, curso no publicado, lección de otro curso, sin cupo, ya matriculado, método de pago no soportado, Strategy que rechaza la referencia). `aprobarPago()`/`cancelarMatricula()`: las 6 transiciones del patrón State, re-chequeo de cupo al aprobar, y que el `Pago` quede `APROBADO`. `obtenerPorId()`/`buscar()` (Lab 5): lectura paginada+filtrada vía `MatriculaSpecification`, y su verificación de propiedad OWASP API1 (un estudiante no ve matrículas ajenas, ni pasando el `usuarioId` de otro). | 27 |
| [`CursoServiceTest`](src/test/java/cr/ac/una/lab1/business/CursoServiceTest.java) | `listarCatalogoPublico()`: cálculo de `cuposDisponibles`/`precioFinal`. `publicarCurso()`: camino feliz + las 6 reglas de publicación (curso inexistente, ya publicado, fechas incoherentes, código duplicado, sin lecciones, lección sin instructor, sin recurso multimedia). `obtenerPorId()`/`buscar()`: lectura por id y colección paginada+filtrada vía `CursoSpecification`. | 12 |
| [`PagoServiceTest`](src/test/java/cr/ac/una/lab1/business/PagoServiceTest.java) | `listar()`: los 4 caminos de filtrado (`estado`/`metodo`/`matriculaId`/sin filtro) y la verificación de propiedad OWASP API1 (estudiante solo ve sus pagos, admin los ve todos). `obtenerPorId()`: propietario, admin, ajeno (403) e inexistente (404). | 11 |
| [`AuthServiceTest`](src/test/java/cr/ac/una/lab1/business/AuthServiceTest.java) | `login()`: camino feliz (token + datos del usuario), credenciales incorrectas (401 vía `CredencialesInvalidasException`) y usuario autenticado pero inexistente en la BD. | 3 |
| [`LeccionServiceTest`](src/test/java/cr/ac/una/lab1/business/LeccionServiceTest.java) | `listar()` con y sin filtro de curso; `obtenerPorId()` con y sin instructor asignado (campos nulos en el DTO), e inexistente. | 5 |
| [`InstructorServiceTest`](src/test/java/cr/ac/una/lab1/business/InstructorServiceTest.java) | `listar()`/`obtenerPorId()`, incluyendo el caso con el `Usuario` sin cargar (campos nulos en el DTO), e inexistente. | 4 |
| [`UsuarioServiceTest`](src/test/java/cr/ac/una/lab1/business/UsuarioServiceTest.java) | `listar()` con y sin filtro de rol; `obtenerPorId()` existente e inexistente. | 4 |
| [`ValidadorSinpeMovilTest`](src/test/java/cr/ac/una/lab1/business/strategy/ValidadorSinpeMovilTest.java) | Formato de referencia SINPE (8 dígitos, nula, inválida). | 3 |
| [`ValidadorTransferenciaTest`](src/test/java/cr/ac/una/lab1/business/strategy/ValidadorTransferenciaTest.java) | Formato de referencia de transferencia (alfanumérica 6-30, vacía, con espacios). | 3 |
| [`ValidadorTarjetaTest`](src/test/java/cr/ac/una/lab1/business/strategy/ValidadorTarjetaTest.java) | TARJETA nunca exige referencia al matricular. | 2 |

`AuthServiceTest`, `InstructorServiceTest`, `LeccionServiceTest`, `PagoServiceTest` y
`UsuarioServiceTest` cubren los servicios que llegaron con la seguridad JWT y el
resto de la API (login, instructores, lecciones, pagos, usuarios) — antes del
Laboratorio 5 no tenían ninguna prueba propia.

Los validadores Strategy se prueban dos veces con propósitos distintos: en
`MatriculaServiceTest` están **mockeados** (se verifica que `MatriculaService`
los invoque y propague lo que lancen); en sus propios `*Test` se instancian
**reales**, sin mocks, para ejercitar la lógica de formato de cada uno.

### Cobertura JaCoCo (mínimo 70% exigido, restringido al paquete de negocio)

`build.gradle` agrega el plugin `jacoco` y filtra tanto el reporte como la
verificación a `cr/ac/una/lab1/business/**` (no cuenta cobertura de entidades
JPA, repositorios ni controladores — esos ya se prueban con Testcontainers).
`check` (y por lo tanto `build`) dependen de `jacocoTestCoverageVerification`,
así que **el build falla si la cobertura del paquete de negocio baja de 70%**:

```bash
./gradlew jacocoTestCoverageVerification --tests "cr.ac.una.lab1.business.*"
```

Resultado actual: **326 de 352 líneas cubiertas (92.6%)** — el reporte HTML
completo queda en `build/reports/jacoco/test/html/index.html` después de correr
`./gradlew test`.