package cr.ac.una.lab1.data;

import cr.ac.una.lab1.data.base.BaseRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatriculaRepository extends BaseRepository<Matricula, Long> {

    Optional<Matricula> findByConsecutivo(String consecutivo);

    List<Matricula> findByUsuarioId(Long usuarioId);

    List<Matricula> findByCursoIdAndEstado(Long cursoId, EstadoMatricula estado);

    /**
     * Consulta de Negocio 2 (JPQL):
     * Matrículas de un estudiante con carga eagerly mediante JOIN FETCH de usuario, lección, curso
     * y pago (LEFT JOIN FETCH porque el pago podría ser opcional o estar pendiente).
     * Evita consultas N+1 al consultar el historial de matrículas de un alumno.
     */
    @Query("SELECT m FROM Matricula m " +
           "JOIN FETCH m.usuario u " +
           "JOIN FETCH m.leccion l " +
           "JOIN FETCH m.curso c " +
           "LEFT JOIN FETCH m.pago p " +
           "WHERE u.id = :usuarioId " +
           "ORDER BY m.fecha DESC")
    List<Matricula> findMatriculasPorUsuarioConDetalleCompleto(@Param("usuarioId") Long usuarioId);

    @Query("SELECT m FROM Matricula m " +
           "JOIN FETCH m.usuario u " +
           "JOIN FETCH m.leccion l " +
           "JOIN FETCH m.curso c " +
           "LEFT JOIN FETCH m.pago p " +
           "WHERE c.id = :cursoId AND m.estado = :estado " +
           "ORDER BY m.fecha ASC")
    List<Matricula> findMatriculasPorCursoYEstadoConDetalle(@Param("cursoId") Long cursoId, @Param("estado") EstadoMatricula estado);
}
