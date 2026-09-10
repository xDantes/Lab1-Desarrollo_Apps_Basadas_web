package cr.ac.una.lab1.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "curso")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String nivel;

    @Column(name = "cupo_total", nullable = false)
    private int cupoTotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "descuento_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoPorcentaje;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private boolean publicado;

    @Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @jakarta.persistence.OneToMany(mappedBy = "curso", fetch = jakarta.persistence.FetchType.LAZY)
    private java.util.List<Leccion> lecciones = new java.util.ArrayList<>();

    protected Curso() {
        // requerido por JPA
    }

    public Curso(String codigo, String nombre, String descripcion, String nivel, int cupoTotal,
                 BigDecimal precio, BigDecimal descuentoPorcentaje, LocalDate fechaInicio,
                 LocalDate fechaFin, boolean publicado) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nivel = nivel;
        this.cupoTotal = cupoTotal;
        this.precio = precio;
        this.descuentoPorcentaje = descuentoPorcentaje;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.publicado = publicado;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getNivel() {
        return nivel;
    }

    public int getCupoTotal() {
        return cupoTotal;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public BigDecimal getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public boolean isPublicado() {
        return publicado;
    }

    public OffsetDateTime getCreadoEn() {
        return creadoEn;
    }

    public java.util.List<Leccion> getLecciones() {
        return lecciones;
    }
}

