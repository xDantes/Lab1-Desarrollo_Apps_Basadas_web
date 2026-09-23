package cr.ac.una.lab1.business;

import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import cr.ac.una.lab1.data.Curso;
import cr.ac.una.lab1.data.CursoRepository;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.LeccionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de negocio del <strong>Proceso 1 — Catálogo y publicación de cursos</strong>.
 *
 * <ul>
 *   <li>{@link #listarCatalogoPublico()} — devuelve cursos publicados con precio final y cupos.
 *   <li>{@link #publicarCurso(Long)} — valida pre-condiciones y activa un curso para el catálogo.
 * </ul>
 *
 * <p>Ver "Proceso 2: Publicación y habilitación de un curso" en la propuesta de dominio.
 */
@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final cr.ac.una.lab1.data.MatriculaRepository matriculaRepository;
    private final LeccionRepository leccionRepository;

    public CursoService(
            CursoRepository cursoRepository,
            cr.ac.una.lab1.data.MatriculaRepository matriculaRepository,
            LeccionRepository leccionRepository) {
        this.cursoRepository = cursoRepository;
        this.matriculaRepository = matriculaRepository;
        this.leccionRepository = leccionRepository;
    }

    // -----------------------------------------------------------------------
    // Catálogo público
    // -----------------------------------------------------------------------

    public List<CursoCatalogoDTO> listarCatalogoPublico() {
        return cursoRepository.findByPublicadoTrue().stream()
                .map(this::aCatalogoDTO)
                .toList();
    }

    // -----------------------------------------------------------------------
    // Proceso 1: publicar un curso
    // -----------------------------------------------------------------------

    /**
     * Publica un curso para que aparezca en el catálogo público.
     *
     * <p>Reglas de negocio verificadas antes de publicar:
     * <ol>
     *   <li>El curso debe existir.
     *   <li>Si ya está publicado, lanza {@link ReglaNegocioException} (idempotencia explícita).
     *   <li>El curso debe tener al menos una lección asignada.
     * </ol>
     *
     * @param cursoId identificador del curso a publicar
     * @return DTO del catálogo con el precio y cupos actualizados
     */
    @Transactional
    public CursoCatalogoDTO publicarCurso(Long cursoId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Curso", cursoId));

        if (curso.isPublicado()) {
            throw new ReglaNegocioException(
                    "El curso '" + curso.getCodigo() + "' ya está publicado.");
        }

        boolean tieneLecciones = !leccionRepository.findByCursoIdOrderByOrdenAsc(cursoId).isEmpty();
        if (!tieneLecciones) {
            throw new ReglaNegocioException(
                    "No se puede publicar el curso '" + curso.getCodigo() +
                    "' porque no tiene ninguna lección asignada.");
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
