package cr.ac.una.lab1.data.mongo.base;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repositorio base genérico para todos los documentos de MongoDB del sistema.
 *
 * <p>Proporciona de forma unificada las operaciones CRUD estándar, paginación,
 * ordenamiento y consultas por ejemplo sobre MongoDB.
 *
 * @param <T>  Tipo del documento MongoDB.
 * @param <ID> Tipo de la clave primaria del documento.
 */
@NoRepositoryBean
public interface BaseMongoRepository<T, ID> extends MongoRepository<T, ID> {
}
