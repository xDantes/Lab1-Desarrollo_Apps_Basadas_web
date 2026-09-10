package cr.ac.una.lab1.data.mongo;

import cr.ac.una.lab1.data.mongo.base.BaseMongoRepository;
import java.util.List;

public interface RecursoMultimediaRepository extends BaseMongoRepository<RecursoMultimedia, String> {

    List<RecursoMultimedia> findByLeccionIdOrderByOrdenAsc(Long leccionId);

    List<RecursoMultimedia> findByCursoId(Long cursoId);
}
