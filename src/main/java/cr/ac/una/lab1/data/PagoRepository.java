package cr.ac.una.lab1.data;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByMatriculaId(Long matriculaId);
}
