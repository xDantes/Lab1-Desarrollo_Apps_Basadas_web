package cr.ac.una.lab1.data;

import cr.ac.una.lab1.data.base.BaseRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagoRepository extends BaseRepository<Pago, Long> {

    Optional<Pago> findByMatriculaId(Long matriculaId);

    List<Pago> findByEstado(EstadoPago estado);

    List<Pago> findByMetodo(MetodoPago metodo);

    @Query("SELECT p FROM Pago p JOIN FETCH p.matricula m JOIN FETCH m.usuario u JOIN FETCH m.curso c WHERE p.estado = :estado ORDER BY p.fecha DESC")
    List<Pago> findByEstadoConDetalleCompleto(@Param("estado") EstadoPago estado);
}
