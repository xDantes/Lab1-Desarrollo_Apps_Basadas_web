package cr.ac.una.lab1.business;

import cr.ac.una.lab1.data.Curso;
import cr.ac.una.lab1.data.CursoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Reglas de negocio del catálogo de cursos.
 *
 * <p>Ver "Proceso 2: Publicación y habilitación de un curso" en la propuesta de
 * dominio: el catálogo público solo debe mostrar cursos publicados, y los cupos
 * disponibles se calculan (no se almacenan) como cupo total menos matrículas activas.
 */
@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    public List<CursoCatalogoDTO> listarCatalogoPublico() {
        return cursoRepository.findByPublicadoTrue().stream()
                .map(this::aCatalogoDTO)
                .toList();
    }

    private CursoCatalogoDTO aCatalogoDTO(Curso curso) {
        // TODO: una vez exista la tabla/entidad `matricula`, restar aquí las
        // matrículas activas del curso en vez de exponer el cupo total.
        int cuposDisponibles = curso.getCupoTotal();
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
