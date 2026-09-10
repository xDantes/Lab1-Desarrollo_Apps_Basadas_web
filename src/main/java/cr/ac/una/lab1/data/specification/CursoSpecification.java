package cr.ac.una.lab1.data.specification;

import cr.ac.una.lab1.data.Curso;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Consulta de Negocio 3 (Dynamic Criteria / Specification):
 * Permite filtrar dinámicamente el catálogo de cursos por texto (código o nombre),
 * nivel, precio máximo, estado de publicación y fecha de inicio.
 */
public final class CursoSpecification {

    private CursoSpecification() {
    }

    public static Specification<Curso> conFiltros(
            String texto,
            String nivel,
            BigDecimal precioMax,
            Boolean publicado,
            LocalDate fechaInicioDespuesDe
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (texto != null && !texto.isBlank()) {
                String pattern = "%" + texto.trim().toLowerCase() + "%";
                Predicate nombreMatch = cb.like(cb.lower(root.get("nombre")), pattern);
                Predicate codigoMatch = cb.like(cb.lower(root.get("codigo")), pattern);
                predicates.add(cb.or(nombreMatch, codigoMatch));
            }

            if (nivel != null && !nivel.isBlank()) {
                predicates.add(cb.equal(root.get("nivel"), nivel.trim()));
            }

            if (precioMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
            }

            if (publicado != null) {
                predicates.add(cb.equal(root.get("publicado"), publicado));
            }

            if (fechaInicioDespuesDe != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaInicio"), fechaInicioDespuesDe));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
