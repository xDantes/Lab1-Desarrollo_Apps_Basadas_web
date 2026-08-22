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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula_id", nullable = false, unique = true)
    private Matricula matricula;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 30)
    private String metodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fecha;

    protected Pago() {
        // requerido por JPA
    }

    public Long getId() {
        return id;
    }

    public Matricula getMatricula() {
        return matricula;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMetodo() {
        return metodo;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }
}
