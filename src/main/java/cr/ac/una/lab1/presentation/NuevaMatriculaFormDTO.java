package cr.ac.una.lab1.presentation;

import cr.ac.una.lab1.data.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Objeto que respalda el formulario {@code GET/POST /mvc/matriculas/nueva}.
 *
 * <p>A propósito tiene menos campos que {@link cr.ac.una.lab1.business.MatriculaRequestDTO}:
 * el estudiante no escribe su propio {@code estudianteId} (se toma de la sesión)
 * ni el {@code cursoId} (se resuelve en el servidor a partir de la lección elegida,
 * para no duplicar la selección en el formulario). Es un objeto de la capa de
 * presentación, no una entidad ni el DTO de negocio — por eso vive aquí y no en
 * {@code business}.
 */
public class NuevaMatriculaFormDTO {

    @NotNull(message = "Debe elegir una lección.")
    private Long leccionId;

    @NotNull(message = "Debe elegir un método de pago.")
    private MetodoPago metodoPago;

    @Size(max = 50, message = "La referencia no puede superar 50 caracteres.")
    private String referencia;

    public Long getLeccionId() {
        return leccionId;
    }

    public void setLeccionId(Long leccionId) {
        this.leccionId = leccionId;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
