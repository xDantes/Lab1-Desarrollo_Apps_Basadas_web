package cr.ac.una.lab1.data.mongo;

import cr.ac.una.lab1.data.mongo.base.BaseMongoRepository;
import java.util.List;

public interface ComentarioRepository extends BaseMongoRepository<Comentario, String> {

    List<Comentario> findByCursoIdOrderByCreadoEnDesc(Long cursoId);

    List<Comentario> findByUsuarioId(Long usuarioId);
}
