package cr.ac.una.lab1.data;

import cr.ac.una.lab1.data.base.BaseRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeccionRepository extends BaseRepository<Leccion, Long> {

    List<Leccion> findByCursoIdOrderByOrdenAsc(Long cursoId);

    @Query("SELECT l FROM Leccion l JOIN FETCH l.curso c JOIN FETCH l.instructor i JOIN FETCH i.usuario u WHERE c.id = :cursoId ORDER BY l.orden ASC")
    List<Leccion> findByCursoIdConDetalle(@Param("cursoId") Long cursoId);
}
