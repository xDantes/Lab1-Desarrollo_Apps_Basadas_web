package cr.ac.una.lab1.data.mongo;

public class MultimediaSena {

    private TipoMultimedia tipo;
    private String url;
    private boolean esPrincipal;

    protected MultimediaSena() {
        // requerido por el conversor de Spring Data MongoDB
    }

    public MultimediaSena(TipoMultimedia tipo, String url, boolean esPrincipal) {
        this.tipo = tipo;
        this.url = url;
        this.esPrincipal = esPrincipal;
    }

    public TipoMultimedia getTipo() {
        return tipo;
    }

    public String getUrl() {
        return url;
    }

    public boolean isEsPrincipal() {
        return esPrincipal;
    }
}
