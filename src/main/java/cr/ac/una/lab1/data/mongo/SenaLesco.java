package cr.ac.una.lab1.data.mongo;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sena_lesco")
public class SenaLesco {

    @Id
    private String id;
    private String palabra;
    private String palabraNormalizada;
    private String descripcion;
    private CategoriaSena categoria;
    private List<MultimediaSena> multimedia;
    private List<String> tags;
    private boolean activo;

    public SenaLesco() {
        // requerido por el conversor de Spring Data MongoDB
    }

    public SenaLesco(String palabra, String palabraNormalizada, String descripcion, CategoriaSena categoria, List<MultimediaSena> multimedia, List<String> tags, boolean activo) {
        this.palabra = palabra;
        this.palabraNormalizada = palabraNormalizada;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.multimedia = multimedia;
        this.tags = tags;
        this.activo = activo;
    }

    public String getId() {
        return id;
    }

    public String getPalabra() {
        return palabra;
    }

    public String getPalabraNormalizada() {
        return palabraNormalizada;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public CategoriaSena getCategoria() {
        return categoria;
    }

    public List<MultimediaSena> getMultimedia() {
        return multimedia;
    }

    public List<String> getTags() {
        return tags;
    }

    public boolean isActivo() {
        return activo;
    }
}
