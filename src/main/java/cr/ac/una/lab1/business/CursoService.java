package cr.ac.una.lab1.business;

import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import cr.ac.una.lab1.data.Curso;
import cr.ac.una.lab1.data.CursoRepository;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.Leccion;
import cr.ac.una.lab1.data.LeccionRepository;
import cr.ac.una.lab1.data.mongo.RecursoMultimediaRepository;
import cr.ac.una.lab1.data.specification.CursoSpecification;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de negocio del <strong>Proceso 1 — Catálogo y publicación de cursos</strong>.
 *
 * <ul>
 *   <li>{@link #listarCatalogoPublico()} — devuelve cursos publicados con precio final y cupos.
 *   <li>{@link #publicarCurso(Long, PublicarCursoRequestDTO)} — valida pre-condiciones y activa un
 *       curso para el catálogo.
 * </ul>
 *
 * <p>Ver "Proceso 1: Publicación y habilitación de un curso" en la propuesta de dominio.
 */
@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final cr.ac.una.lab1.data.MatriculaRepository matriculaRepository;
    private final LeccionRepository leccionRepository;
    private final RecursoMultimediaRepository recursoMultimediaRepository;

    public CursoService(
            CursoRepository cursoRepository,
            cr.ac.una.lab1.data.MatriculaRepository matriculaRepository,
            LeccionRepository leccionRepository,
            RecursoMultimediaRepository recursoMultimediaRepository) {
        this.cursoRepository = cursoRepository;
        this.matriculaRepository = matriculaRepository;
        this.leccionRepository = leccionRepository;
        this.recursoMultimediaRepository = recursoMultimediaRepository;
    }

    // -----------------------------------------------------------------------
    // Catálogo público
    // -----------------------------------------------------------------------

    public List<CursoCatalogoDTO> listarCatalogoPublico() {
        return cursoRepository.findByPublicadoTrue().stream()
                .map(this::aCatalogoDTO)
                .toList();
    }

    /** Un curso por id. Lanza {@link EntidadNoEncontradaException} (404) si no existe. */
    @Transactional(readOnly = true)
    public CursoCatalogoDTO obtenerPorId(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Curso", id));
        return aCatalogoDTO(curso);
    }

    /**
     * Colección paginada y filtrable de cursos (patrón Specification, ver
     * {@link CursoSpecification}). Todos los filtros son opcionales; el orden
     * y el tamaño de página los resuelve Spring a partir de {@code pageable}
     * ({@code ?page=&size=&sort=}).
     */
    @Transactional(readOnly = true)
    public Page<CursoCatalogoDTO> buscar(
            String texto, String nivel, BigDecimal precioMax, Boolean publicado,
            LocalDate fechaInicioDespuesDe, Pageable pageable) {
        Specification<Curso> filtros =
                CursoSpecification.conFiltros(texto, nivel, precioMax, publicado, fechaInicioDespuesDe);
        return cursoRepository.findAll(filtros, pageable).map(this::aCatalogoDTO);
    }

    // -----------------------------------------------------------------------
    // Proceso 1: publicar un curso
    // -----------------------------------------------------------------------

    /**
     * Publica un curso para que aparezca en el catálogo público.
     *
     * <p>Reglas de negocio verificadas (en orden) antes de publicar:
     * <ol>
     *   <li>El curso debe existir ({@link EntidadNoEncontradaException} si no).
     *   <li>No debe estar ya publicado ({@link ReglaNegocioException}).
     *   <li>Las fechas deben ser coherentes: {@code fechaInicio} anterior a {@code fechaFin}
     *       ({@link ReglaNegocioException}).
     *   <li>No debe existir otro curso publicado con el mismo código en el catálogo
     *       ({@link ReglaNegocioException} — duplicado en mismo periodo).
     *   <li>Debe tener al menos una lección asignada ({@link ReglaNegocioException}).
     *   <li>Todas las lecciones deben tener un instructor asignado ({@link ReglaNegocioException}).
     *   <li>Al menos una lección debe tener un recurso multimedia registrado en MongoDB
     *       ({@link ReglaNegocioException}).
     * </ol>
     *
     * @param cursoId identificador del curso a publicar
     * @param request solicitud con el ID del administrador que autoriza la publicación
     * @return DTO del catálogo con el precio y cupos actualizados
     */
    @Transactional
    public CursoCatalogoDTO publicarCurso(Long cursoId, PublicarCursoRequestDTO request) {

        // Regla 1: el curso debe existir
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Curso", cursoId));

        // Regla 2: no publicar dos veces
        if (curso.isPublicado()) {
            throw new ReglaNegocioException(
                    "El curso '" + curso.getCodigo() + "' ya está publicado.");
        }

        // Regla 3: coherencia de fechas
        if (!curso.getFechaInicio().isBefore(curso.getFechaFin())) {
            throw new ReglaNegocioException(
                    "La fecha de inicio (" + curso.getFechaInicio() +
                    ") debe ser anterior a la fecha de fin (" + curso.getFechaFin() +
                    ") del curso '" + curso.getCodigo() + "'.");
        }

        // Regla 4: sin duplicado publicado con el mismo código en el catálogo
        if (cursoRepository.existsByCodigoAndPublicadoTrueAndIdNot(curso.getCodigo(), cursoId)) {
            throw new ReglaNegocioException(
                    "Ya existe un curso publicado con el código '" + curso.getCodigo() +
                    "'. No se puede tener dos versiones activas del mismo curso en el catálogo.");
        }

        // Regla 5: al menos una lección asignada
        List<Leccion> lecciones = leccionRepository.findByCursoIdOrderByOrdenAsc(cursoId);
        if (lecciones.isEmpty()) {
            throw new ReglaNegocioException(
                    "No se puede publicar el curso '" + curso.getCodigo() +
                    "' porque no tiene ninguna lección asignada.");
        }

        // Regla 6: todas las lecciones deben tener instructor asignado
        boolean algulnasSinInstructor = lecciones.stream()
                .anyMatch(l -> l.getInstructor() == null);
        if (algulnasSinInstructor) {
            throw new ReglaNegocioException(
                    "El curso '" + curso.getCodigo() +
                    "' tiene lecciones sin instructor asignado. " +
                    "Todas las lecciones deben tener un instructor antes de publicar.");
        }

        // Regla 7: al menos una lección con recurso multimedia registrado en MongoDB
        boolean tieneRecurso = lecciones.stream()
                .anyMatch(l -> !recursoMultimediaRepository.findByLeccionIdOrderByOrdenAsc(l.getId()).isEmpty());
        if (!tieneRecurso) {
            throw new ReglaNegocioException(
                    "El curso '" + curso.getCodigo() +
                    "' no tiene recursos multimedia registrados en ninguna de sus lecciones. " +
                    "Se requiere al menos un recurso antes de publicar.");
        }

        curso.publicar();
        cursoRepository.save(curso);
        return aCatalogoDTO(curso);
    }

    // -----------------------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------------------

    private CursoCatalogoDTO aCatalogoDTO(Curso curso) {
        int matriculasActivas = matriculaRepository.findByCursoIdAndEstado(
                curso.getId(),
                EstadoMatricula.ACTIVA
        ).size();
        int cuposDisponibles = Math.max(0, curso.getCupoTotal() - matriculasActivas);
        BigDecimal precioFinal = precioConDescuento(curso.getPrecio(), curso.getDescuentoPorcentaje());
        return new CursoCatalogoDTO(
                curso.getId(),
                curso.getCodigo(),
                curso.getNombre(),
                curso.getNivel(),
                precioFinal,
                curso.getCupoTotal(),
                cuposDisponibles,
                curso.getFechaInicio(),
                curso.getFechaFin()
        );
    }

    private BigDecimal precioConDescuento(BigDecimal precio, BigDecimal descuentoPorcentaje) {
        BigDecimal factor = BigDecimal.ONE.subtract(
                descuentoPorcentaje.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return precio.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
