package cr.ac.una.lab1.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Subtipo de {@link Usuario}: un usuario con rol INSTRUCTOR habilitado para
 * que se le asignen lecciones. Mapea la tabla `instructor`
 * (ver V3_1__crear_instructor.sql).
 *
 * <p>Comparte la clave primaria con `usuario` ({@code @MapsId}): el id de un
 * Instructor es siempre el mismo id de su Usuario. Esto es lo que permite que
 * {@code leccion.instructor_id} (V4) referencie `instructor(usuario_id)` en
 * vez de `usuario(id)` sin cambiar el rango de valores válidos.
 */
@Entity
@Table(name = "instructor")
public class Instructor {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(length = 100)
    private String especialidad;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private OffsetDateTime creadoEn;

    protected Instructor() {
        // requerido por JPA
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public OffsetDateTime getCreadoEn() {
        return creadoEn;
    }
}
