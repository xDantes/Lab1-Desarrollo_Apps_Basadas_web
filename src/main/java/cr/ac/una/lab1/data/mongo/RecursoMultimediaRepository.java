package cr.ac.una.lab1.data.mongo;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecursoMultimediaRepository extends MongoRepository<RecursoMultimedia, String> {

    List<RecursoMultimedia> findByLeccionIdOrderByOrdenAsc(Long leccionId);

    List<RecursoMultimedia> findByCursoId(Long cursoId);
}
