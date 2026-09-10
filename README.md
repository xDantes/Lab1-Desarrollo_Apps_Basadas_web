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
- [Diagrama de arquitectura actual](docs/diagrama_actual.md) 
- [ADR-001 · Elección de stack](docs/adr/ADR-001-EleccionStack.md)

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

3. Levantarla:

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
curl http://localhost:8080/api/cursos
```
```json
[
  {"id":1,"codigo":"LESCO-101","nombre":"LESCO Básico I","nivel":"BASICO","precioFinal":45000.00,"cupoTotal":20,"cuposDisponibles":20,"fechaInicio":"2026-09-07","fechaFin":"2026-11-13"},
  {"id":2,"codigo":"LESCO-102","nombre":"LESCO Intermedio","nivel":"INTERMEDIO","precioFinal":49500.00,"cupoTotal":15,"cuposDisponibles":15,"fechaInicio":"2026-09-07","fechaFin":"2026-12-04"}
]
```

Ese resultado viene de los datos semilla de Flyway (`src/main/resources/db/migration`) y
solo muestra los cursos **publicados** — hay un tercer curso de ejemplo (`LESCO-201`) que
no aparece a propósito, porque todavía no está publicado.

## Cómo Ejecutar las Pruebas de Integración con Testcontainers

Las pruebas de integración levantan contenedores reales de **PostgreSQL 16** y **MongoDB 7.0** usando **Testcontainers**, aplican automáticamente todas las migraciones de Flyway (`V1` a `V8`), validan el mapeo JPA con `spring.jpa.hibernate.ddl-auto=validate` y prueban consultas de negocio, repositorios genéricos y la corrección del problema N+1.

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
2. Flyway ejecuta exitosamente las migraciones `V1__crear_usuario.sql` hasta `V8__datos_semilla_matricula_pago.sql`.
3. Hibernate valida la coherencia entre las entidades JPA y la BD (`Schema-validation: [SUCCESS]`).
4. Se ejecutan en verde (PASSED) **8 pruebas de integración**:
   - `test1_ValidarEsquemaFlywayYEntidades`: Verifica el esquema Flyway y las entidades JPA.
   - `test2_DemostracionProblemaNMasUnoYCorreccionFetch`: Demuestra y resuelve el problema N+1.
   - `test3_ConsultaJPQL1_CursosPublicadosConLecciones`: Prueba JPQL de cursos publicados con lecciones.
   - `test4_ConsultaJPQL2_MatriculasPorEstudianteConDetalle`: Prueba JPQL de matrículas con detalle completo.
   - `test5_ConsultaCriteriaSpecification_CursosFiltrosDinamicos`: Prueba Specification dinámica de cursos.
   - `test6_ConsultaCriteriaSpecification_MatriculasFiltrosDinamicos`: Prueba Specification dinámica de matrículas.
   - `test7_RepositorioGenericoBaseJPA`: Prueba operaciones genéricas de `BaseRepository` (CRUD, paginación, sort).
   - `test8_RepositorioGenericoBaseMongoDB`: Prueba operaciones genéricas de `BaseMongoRepository` sobre MongoDB.

El reporte HTML completo se genera en: `build/reports/tests/test/index.html`.

---

## Arquitectura del código

El proyecto sigue una separación por capas bajo `cr.ac.una.lab1`:

- `presentation` — controladores REST (ej. `CursoController`).
- `business` — reglas de negocio y DTOs de salida (ej. `CursoService`, `CursoCatalogoDTO`).
- `data` — entidades JPA y repositorios de PostgreSQL (`Usuario`, `Curso`, `Leccion`, `Matricula`, `Pago`), y en    `data.mongo` los documentos/repositorios de MongoDB (`SenaLesco`, `RecursoMultimedia`, `Comentario`).
- `config` — configuración transversal de la aplicación.

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