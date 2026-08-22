package cr.ac.una.lab1.data.mongo;

public class CategoriaSena {

    private String codigo;
    private String nombre;

    protected CategoriaSena() {
        // requerido por el conversor de Spring Data MongoDB
    }

    public CategoriaSena(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }
}
