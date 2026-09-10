package cr.ac.una.lab1.data.specification;

import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.Matricula;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Consulta de Negocio 4 (Dynamic Criteria / Specification):
 * Permite filtrar dinámicamente el registro de matrículas por estudiante (usuarioId),
 * curso (cursoId), estado de matrícula, rango de fechas y monto de precio final.
 */
public final class MatriculaSpecification {

    private MatriculaSpecification() {
    }

    public static Specification<Matricula> conFiltros(
            Long usuarioId,
            Long cursoId,
            EstadoMatricula estado,
            OffsetDateTime fechaDesde,
            OffsetDateTime fechaHasta,
            BigDecimal precioFinalMin,
            BigDecimal precioFinalMax
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("usuario").get("id"), usuarioId));
            }

            if (cursoId != null) {
                predicates.add(cb.equal(root.get("curso").get("id"), cursoId));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), fechaDesde));
            }

            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), fechaHasta));
            }

            if (precioFinalMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("precioFinal"), precioFinalMin));
            }

            if (precioFinalMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("precioFinal"), precioFinalMax));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
