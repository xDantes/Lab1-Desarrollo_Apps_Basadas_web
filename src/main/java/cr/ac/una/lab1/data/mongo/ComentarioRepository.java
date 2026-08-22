package cr.ac.una.lab1.data.mongo;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComentarioRepository extends MongoRepository<Comentario, String> {

    List<Comentario> findByCursoIdOrderByCreadoEnDesc(Long cursoId);

    List<Comentario> findByUsuarioId(Long usuarioId);
}
