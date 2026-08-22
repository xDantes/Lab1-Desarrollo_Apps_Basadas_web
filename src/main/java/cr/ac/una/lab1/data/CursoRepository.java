package cr.ac.una.lab1.data;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    List<Curso> findByPublicadoTrue();
}
