package cr.ac.una.lab1.business;

import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.data.Leccion;
import cr.ac.una.lab1.data.LeccionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeccionService {

    private final LeccionRepository leccionRepository;

    public LeccionService(LeccionRepository leccionRepository) {
        this.leccionRepository = leccionRepository;
    }

    @Transactional(readOnly = true)
    public List<LeccionDetalleResponseDTO> listar(Long cursoId) {
        List<Leccion> lecciones = (cursoId != null)
                ? leccionRepository.findByCursoIdConDetalle(cursoId)
                : leccionRepository.findAll();
        return lecciones.stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeccionDetalleResponseDTO obtenerPorId(Long id) {
        Leccion leccion = leccionRepository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Leccion", id));
        return toDTO(leccion);
    }

    private LeccionDetalleResponseDTO toDTO(Leccion l) {
        return new LeccionDetalleResponseDTO(
                l.getId(),
                l.getOrden(),
                l.getTitulo(),
                l.getCurso() != null ? l.getCurso().getId() : null,
                l.getCurso() != null ? l.getCurso().getCodigo() : null,
                l.getCurso() != null ? l.getCurso().getNombre() : null,
                l.getInstructor() != null ? l.getInstructor().getUsuarioId() : null,
                (l.getInstructor() != null && l.getInstructor().getUsuario() != null)
                        ? l.getInstructor().getUsuario().getNombre()
                        : null
        );
    }
}
