package cr.ac.una.lab1.data;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    List<Matricula> findByUsuarioId(Long usuarioId);

    List<Matricula> findByCursoIdAndEstado(Long cursoId, EstadoMatricula estado);
}
