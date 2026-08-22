package cr.ac.una.lab1.data;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeccionRepository extends JpaRepository<Leccion, Long> {

    List<Leccion> findByCursoIdOrderByOrdenAsc(Long cursoId);
}
