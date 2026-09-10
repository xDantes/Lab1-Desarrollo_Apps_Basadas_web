package cr.ac.una.lab1.data.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repositorio base genérico para todas las entidades JPA del sistema.
 *
 * <p>Proporciona de forma unificada las operaciones CRUD estándar, paginación,
 * ordenamiento y ejecución de consultas dinámicas basadas en {@link JpaSpecificationExecutor}.
 *
 * @param <T>  Tipo de la entidad JPA.
 * @param <ID> Tipo de la clave primaria de la entidad.
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}
