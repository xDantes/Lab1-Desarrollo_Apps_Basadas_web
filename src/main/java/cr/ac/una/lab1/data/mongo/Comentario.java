package cr.ac.una.lab1.data.mongo;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "comentarios")
public class Comentario {

    @Id
    private String id;

    private Long cursoId;
    private Long usuarioId;
    private String autorNombre;
    private String texto;
    private Integer calificacion;
    private Instant creadoEn;
    private boolean editado;
    private List<RespuestaComentario> respuestas;

    protected Comentario() {
        // requerido por el conversor de Spring Data MongoDB
    }

    public String getId() {
        return id;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getAutorNombre() {
        return autorNombre;
    }

    public String getTexto() {
        return texto;
    }

    public Integer getCalificacion() {
        return calificacion;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public boolean isEditado() {
        return editado;
    }

    public List<RespuestaComentario> getRespuestas() {
        return respuestas;
    }
}
