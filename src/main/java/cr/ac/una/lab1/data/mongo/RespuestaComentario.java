package cr.ac.una.lab1.data.mongo;
import java.time.Instant;

public class RespuestaComentario {

    private Long autorId;
    private String autorNombre;
    private String texto;
    private Instant creadoEn;

    protected RespuestaComentario() {
        // requerido por el conversor de Spring Data MongoDB
    }

    public RespuestaComentario(Long autorId, String autorNombre, String texto, Instant creadoEn) {
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.texto = texto;
        this.creadoEn = creadoEn;
    }

    public Long getAutorId() {
        return autorId;
    }

    public String getAutorNombre() {
        return autorNombre;
    }

    public String getTexto() {
        return texto;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
