package cr.ac.una.lab1.business;

import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.data.Instructor;
import cr.ac.una.lab1.data.InstructorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;

    public InstructorService(InstructorRepository instructorRepository) {
        this.instructorRepository = instructorRepository;
    }

    @Transactional(readOnly = true)
    public List<InstructorResponseDTO> listar() {
        return instructorRepository.findAllWithUsuario().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public InstructorResponseDTO obtenerPorId(Long id) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Instructor", id));
        return toDTO(instructor);
    }

    private InstructorResponseDTO toDTO(Instructor i) {
        return new InstructorResponseDTO(
                i.getUsuarioId(),
                i.getUsuario() != null ? i.getUsuario().getNombre() : null,
                i.getUsuario() != null ? i.getUsuario().getIdentificacion() : null,
                i.getUsuario() != null ? i.getUsuario().getCorreo() : null,
                i.getEspecialidad()
        );
    }
}
