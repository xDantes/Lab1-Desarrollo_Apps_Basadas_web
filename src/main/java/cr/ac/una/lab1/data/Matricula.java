package cr.ac.una.lab1.data;

import cr.ac.una.lab1.business.state.EstadoActiva;
import cr.ac.una.lab1.business.state.EstadoCancelada;
import cr.ac.una.lab1.business.state.EstadoPendiente;
import cr.ac.una.lab1.business.state.MatriculaEstado;
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
import jakarta.persistence.OneToOne;
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

    // curso_id lo rellena el trigger trg_matricula_set_curso_id a partir de leccion_id.
    // insertable=false, updatable=false: Hibernate nunca lo incluye en INSERT/UPDATE.
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

    @OneToOne(mappedBy = "matricula", fetch = FetchType.LAZY)
    private Pago pago;

    protected Matricula() {
        // requerido por JPA
    }

    public Matricula(String consecutivo, Usuario usuario, Leccion leccion,
                     EstadoMatricula estado, BigDecimal precioFinal) {
        this.consecutivo = consecutivo;
        this.usuario     = usuario;
        this.leccion     = leccion;
        // NO se asigna this.curso: el trigger de BD lo calcula a partir de leccion_id.
        // Asignarlo aquí obligaría a inicializar el proxy LAZY de leccion.getCurso()
        // justo antes del INSERT IDENTITY, causando un flush prematuro.
        this.estado      = estado;
        this.precioFinal = precioFinal;
    }

    // -----------------------------------------------------------------------
    // Patrón State — ciclo de vida de la matrícula
    //
    // Se resuelve el estado actual de forma INLINE (sin campo @Transient) para
    // evitar que Hibernate interprete un campo no persistido durante la
    // instrumentación de bytecode.
    // -----------------------------------------------------------------------

    /**
     * Transición a ACTIVA (aprobación de pago).
     * Solo válida desde PENDIENTE; lanza {@code CambioEstadoInvalidoException} en otro caso.
     */
    public void activar() {
        estadoActual().activar(this);
    }

    /**
     * Transición a CANCELADA.
     * Válida desde PENDIENTE o ACTIVA; lanza {@code CambioEstadoInvalidoException} desde CANCELADA.
     */
    public void cancelar() {
        estadoActual().cancelar(this);
    }

    /**
     * Usado por las implementaciones de {@link MatriculaEstado} para cambiar el
     * valor persistido. No forma parte de la API pública del dominio.
     */
    public void setEstado(EstadoMatricula nuevoEstado) {
        this.estado = nuevoEstado;
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public Long getId()                   { return id; }
    public String getConsecutivo()        { return consecutivo; }
    public Usuario getUsuario()           { return usuario; }
    public Leccion getLeccion()           { return leccion; }
    public Curso getCurso()               { return curso; }
    public EstadoMatricula getEstado()    { return estado; }
    public BigDecimal getPrecioFinal()    { return precioFinal; }
    public OffsetDateTime getFecha()      { return fecha; }
    public Pago getPago()                 { return pago; }

    // -----------------------------------------------------------------------
    // Helper privado
    // -----------------------------------------------------------------------

    private MatriculaEstado estadoActual() {
        return switch (estado) {
            case PENDIENTE -> EstadoPendiente.INSTANCIA;
            case ACTIVA    -> EstadoActiva.INSTANCIA;
            case CANCELADA -> EstadoCancelada.INSTANCIA;
        };
    }
}
