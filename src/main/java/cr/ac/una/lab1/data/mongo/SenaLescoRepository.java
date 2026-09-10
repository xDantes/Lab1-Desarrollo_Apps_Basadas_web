package cr.ac.una.lab1.data.mongo;

import cr.ac.una.lab1.data.mongo.base.BaseMongoRepository;
import java.util.List;
import java.util.Optional;

public interface SenaLescoRepository extends BaseMongoRepository<SenaLesco, String> {

    Optional<SenaLesco> findByPalabraNormalizada(String palabraNormalizada);

    List<SenaLesco> findByCategoria_Codigo(String categoriaCodigo);

    List<SenaLesco> findByActivoTrue();
}
