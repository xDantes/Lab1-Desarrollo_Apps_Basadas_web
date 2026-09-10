package cr.ac.una.lab1.data;

import cr.ac.una.lab1.data.base.BaseRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface InstructorRepository extends BaseRepository<Instructor, Long> {

    @Query("SELECT i FROM Instructor i JOIN FETCH i.usuario u")
    List<Instructor> findAllWithUsuario();
}
