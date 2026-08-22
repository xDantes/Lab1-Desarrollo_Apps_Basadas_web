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

## Arquitectura del código

El proyecto sigue una separación por capas bajo `cr.ac.una.lab1`:

- `presentation` — controladores REST (ej. `CursoController`).
- `business` — reglas de negocio y DTOs de salida (ej. `CursoService`, `CursoCatalogoDTO`).
- `data` — entidades JPA y repositorios (ej. `Curso`, `CursoRepository`).
- `config` — configuración transversal de la aplicación.

## Persistencia

- PostgreSQL: usuarios, cursos, lecciones, matrículas y pagos. Esquema versionado
  con [Flyway](src/main/resources/db/migration) (3FN, con CHECK/UNIQUE/FK e índices
  justificados en cada migración).
- MongoDB: diccionario de señas LESCO, recursos multimedia de lecciones y
  comentarios de cursos — en progreso (ver `mongo-init/`).

## Notas adicionales

- Los comentarios no eran parte de la propuesta inicial, se implemento para hacer mas amplio el modelo de base de datos y dividir mejor el uso de base de datos para equilibrar su uso y que sea mas amplio su uso y aprendizaje. 

- A la hora de crear la propuesta inicial, cometimos algunos errores en la redaccion con respecto a la relacion del profesor con el curso o leccion, fuimos algo ambiguos, dado que, el plan real es que el profesor esta asociado a la leccion, y de la misma forma el profesor adjunte el material a la leccion y no directamente al curso.

- Tras un analisis, decidimos hacer algunos cambios con el modelado de las tablas, para re distribuir tal como ya se agrego a este documento.

