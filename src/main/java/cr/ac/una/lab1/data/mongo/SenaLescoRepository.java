package cr.ac.una.lab1.data.mongo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SenaLescoRepository extends MongoRepository<SenaLesco, String> {

    Optional<SenaLesco> findByPalabraNormalizada(String palabraNormalizada);

    List<SenaLesco> findByCategoria_Codigo(String categoriaCodigo);

    List<SenaLesco> findByActivoTrue();
}
