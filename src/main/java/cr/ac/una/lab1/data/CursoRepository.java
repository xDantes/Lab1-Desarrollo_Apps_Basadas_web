package cr.ac.una.lab1.data;

import cr.ac.una.lab1.data.base.BaseRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

public interface CursoRepository extends BaseRepository<Curso, Long> {

    Optional<Curso> findByCodigo(String codigo);

    List<Curso> findByPublicadoTrue();

    /**
     * Consulta de Negocio 1 (JPQL):
     * Cursos publicados con sus lecciones cargadas mediante JOIN FETCH en una única consulta SQL.
     * Soluciona el problema de N+1 consultas al evitar subconsultas perezosas (LAZY) por cada curso.
     */
    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.lecciones l WHERE c.publicado = true ORDER BY c.fechaInicio ASC")
    List<Curso> findCursosPublicadosConLecciones();

    /**
     * Alternativa con @EntityGraph para cargar lecciones en una sola consulta.
     */
    @EntityGraph(attributePaths = {"lecciones"})
    @Query("SELECT c FROM Curso c WHERE c.publicado = true")
    List<Curso> findCursosPublicadosConEntityGraph();
}
