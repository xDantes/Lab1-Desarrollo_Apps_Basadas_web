package cr.ac.una.lab1;

import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.Matricula;
import cr.ac.una.lab1.data.MatriculaRepository;
import cr.ac.una.lab1.presentation.GlobalExceptionHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas de integración para Concurrencia Controlada (Laboratorio 7).
 *
 * <p>Verifica:
 * <ol>
 *   <li>El bloqueo optimista ({@code @Version}) en {@link Matricula} detecta modificaciones
 *       simultáneas y lanza una excepción de conflicto.
 *   <li>{@link GlobalExceptionHandler} traduce la excepción de bloqueo optimista
 *       a un código HTTP 409 (Conflict) con formato Problem Details (RFC 9457).
 * </ol>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConcurrenciaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    @Order(1)
    @DisplayName("Concurrencia: dos transacciones concurrentes sobre la misma matrícula provocan OptimisticLockException")
    void test1_ConflictoConcurrencia_BloqueoOptimista_ProvocaExcepcion() {
        // Obtenemos una matrícula existente de los datos semilla (ej. id 1, creada en V8)
        Matricula semilla = matriculaRepository.findAll().stream()
                .filter(m -> m.getEstado() == EstadoMatricula.PENDIENTE)
                .findFirst()
                .orElseGet(() -> matriculaRepository.findAll().get(0));

        Long matriculaId = semilla.getId();

        // Creamos dos EntityManager independientes que simulan dos hilos/usuarios distintos
        EntityManager em1 = entityManagerFactory.createEntityManager();
        EntityManager em2 = entityManagerFactory.createEntityManager();

        try {
            // Usuario 1 lee la matrícula (versión actual, ej: 0)
            em1.getTransaction().begin();
            Matricula m1 = em1.find(Matricula.class, matriculaId);
            Long versionOriginal = m1.getVersion();
            assertThat(versionOriginal).isNotNull();

            // Usuario 2 lee la misma matrícula antes de que Usuario 1 confirme (misma versión)
            em2.getTransaction().begin();
            Matricula m2 = em2.find(Matricula.class, matriculaId);
            assertThat(m2.getVersion()).isEqualTo(versionOriginal);

            // Usuario 1 modifica y hace commit exitoso -> version se incrementa a versionOriginal + 1
            m1.cancelar();
            em1.getTransaction().commit();

            // Usuario 2 intenta modificar con su snapshot desactualizado (version todavía en versionOriginal)
            m2.activar();

            // Al intentar commit, Hibernate detecta que la fila en BD ya tiene versión incrementada
            assertThatThrownBy(() -> em2.getTransaction().commit())
                    .satisfies(ex -> {
                        assertThat(ex).isInstanceOf(RollbackException.class);
                        assertThat(ex.getCause()).isInstanceOf(OptimisticLockException.class);
                    });

        } finally {
            if (em1.getTransaction().isActive()) em1.getTransaction().rollback();
            if (em2.getTransaction().isActive()) em2.getTransaction().rollback();
            em1.close();
            em2.close();
        }
    }

    @Test
    @Order(2)
    @DisplayName("Concurrencia: GlobalExceptionHandler traduce OptimisticLockingFailureException a HTTP 409 Problem Details")
    void test2_ManejoGlobalConflictoConcurrencia_TraduceA409ProblemDetail() {
        OptimisticLockingFailureException ex =
                new OptimisticLockingFailureException("Fila modificada por otra transacción concurrente");

        ProblemDetail pd = globalExceptionHandler.handleConflictoConcurrencia(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(pd.getTitle()).isEqualTo("Conflicto de concurrencia");
        assertThat(pd.getType().toString()).isEqualTo("https://lescocr.cr/problems/conflicto-concurrencia");
        assertThat(pd.getDetail()).contains("El recurso fue modificado simultáneamente por otro usuario");
    }
}
