package cr.ac.una.lab1.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "matricula")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String consecutivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leccion_id", nullable = false)
    private Leccion leccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false, insertable = false, updatable = false)
    private Curso curso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMatricula estado;

    @Column(name = "precio_final", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioFinal;

    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fecha;

    protected Matricula() {
        // requerido por JPA
    }

    public Long getId() {
        return id;
    }

    public String getConsecutivo() {
        return consecutivo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Leccion getLeccion() {
        return leccion;
    }

    public Curso getCurso() {
        return curso;
    }

    public EstadoMatricula getEstado() {
        return estado;
    }

    public BigDecimal getPrecioFinal() {
        return precioFinal;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }
}
