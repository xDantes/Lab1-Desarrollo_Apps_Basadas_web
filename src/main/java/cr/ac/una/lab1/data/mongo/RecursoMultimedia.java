package cr.ac.una.lab1.data.mongo;

import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "recursos_multimedia")
public class RecursoMultimedia {

    @Id
    private String id;

    private Long leccionId;
    private Long cursoId;
    private TipoMultimedia tipo;
    private String url;
    private int orden;

    private Map<String, Object> metadata;

    public RecursoMultimedia() {
        // requerido por el conversor de Spring Data MongoDB
    }

    public RecursoMultimedia(Long leccionId, Long cursoId, TipoMultimedia tipo, String url, int orden, Map<String, Object> metadata) {
        this.leccionId = leccionId;
        this.cursoId = cursoId;
        this.tipo = tipo;
        this.url = url;
        this.orden = orden;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public Long getLeccionId() {
        return leccionId;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public TipoMultimedia getTipo() {
        return tipo;
    }

    public String getUrl() {
        return url;
    }

    public int getOrden() {
        return orden;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
