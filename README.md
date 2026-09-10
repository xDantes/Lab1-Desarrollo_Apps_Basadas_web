# LescoCR

Plataforma de apoyo a la enseñanza de LESCO (Lengua de Señas Costarricense), dirigida a la comunidad LESCO y a cualquier persona interesada en aprenderla gratis. Ofrece un diccionario de señas con búsqueda por imágenes/video, y cursos estructurados en lecciones con matrícula y pago (simulado).

Proyecto de EIF509 Desarrollo de Aplicaciones Basadas en Web.

Desarrollado por: Derrek Adrián Ureña Solís y José Arrieta Sancho.

---

## Documentación del proyecto

- [Propuesta de dominio](docs/Propuesta_de_Dominio.pdf) — entidades de negocio, procesos y alcance.
- [Diagrama de arquitectura](docs/diagrama.md) — diagrama Mermaid actualizado con generalización de repositorios y specifications.
- [ADR-001 · Elección de stack](docs/adr/ADR-001-EleccionStack.md)
- [Modelo de datos](docs/modelo-datos.md) — tablas, restricciones e índices justificados; colecciones Mongo con su justificación de embeber/referenciar.

---

## Requisitos previos

- **Java 21** (JDK)
- **Docker** y **Docker Compose** — para levantar PostgreSQL y MongoDB (o para ejecución de Testcontainers).
  - En Windows: WSL2 (`wsl --install` desde PowerShell como Administrador, reiniciar) y [Docker Desktop](https://www.docker.com/products/docker-desktop/).
- El proyecto incluye el wrapper de Gradle (`gradlew` / `gradlew.bat`), por lo que no es necesario instalar Gradle por separado.

---

## Cómo Ejecutar las Pruebas de Integración con Testcontainers

Las pruebas de integración levantan contenedores reales de **PostgreSQL 16** y **MongoDB 7.0** usando **Testcontainers**, aplican automáticamente todas las migraciones de Flyway (`V1` a `V8`), validan el mapeo JPA con `spring.jpa.hibernate.ddl-auto=validate` y prueban consultas de negocio, repositorios genéricos y la corrección del problema N+1.

### Comando para correr las pruebas:

```bash
# En Windows (PowerShell / CMD)
.\gradlew.bat test

# En Linux / macOS
./gradlew test
```

Para correr una clase de prueba específica o ver el log detallado:
```bash
.\gradlew.bat test --tests "cr.ac.una.lab1.PersistenciaIntegrationTest" --info
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

Salida esperada en consola:
```text
> Task :test
PersistenciaIntegrationTest > Prueba 1: Validar esquema Flyway y persistencia de entidades JPA (ddl-auto=validate) PASSED
PersistenciaIntegrationTest > Prueba 2: Demostración del problema N+1 y corrección con JOIN FETCH y @EntityGraph PASSED
PersistenciaIntegrationTest > Prueba 3: Consulta de Negocio 1 (JPQL) - Cursos publicados con lecciones PASSED
PersistenciaIntegrationTest > Prueba 4: Consulta de Negocio 2 (JPQL) - Matrículas por estudiante con grafo completo PASSED
PersistenciaIntegrationTest > Prueba 5: Consulta de Negocio 3 (Criteria / Specification) - Filtrado dinámico de Cursos PASSED
PersistenciaIntegrationTest > Prueba 6: Consulta de Negocio 4 (Criteria / Specification) - Filtrado dinámico de Matrículas PASSED
PersistenciaIntegrationTest > Prueba 7: Repositorio Genérico Base JPA (BaseRepository CRUD, paginación, ordenamiento) PASSED
PersistenciaIntegrationTest > Prueba 8: Repositorio Genérico Base MongoDB (BaseMongoRepository con subdominio NoSQL) PASSED

BUILD SUCCESSFUL in 15s
```

El reporte HTML completo se genera en: `build/reports/tests/test/index.html`.

---

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

## Cómo Levantar la Aplicación en Desarrollo

1. Levantar las bases de datos locales:
   ```bash
   docker compose up -d
   ```

2. Ejecutar la aplicación:
   ```bash
   ./gradlew bootRun
   ```
   La aplicación estará disponible en **http://localhost:8080**.

3. Probar endpoints:
   ```bash
   curl http://localhost:8080/actuator/health
   curl http://localhost:8080/api/cursos
   ```
