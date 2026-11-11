package cr.ac.una.lab1.presentation;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Objeto que respalda el formulario {@code GET/POST /mvc/cursos/{id}/publicar}.
 *
 * <p>Aporta validación declarativa con Bean Validation ({@code @NotNull}, {@code @NotBlank},
 * {@code @Size}, {@code @AssertTrue}) y enlace con {@link org.springframework.validation.BindingResult}
 * en el controlador MVC, transformando la confirmación en un formulario de datos validado
 * en la capa de presentación.
 */
public class PublicarCursoFormDTO {

    @NotNull(message = "Debe indicar la fecha de publicación.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaPublicacion = LocalDate.now();

    @NotBlank(message = "Debe ingresar una justificación o notas de publicación.")
    @Size(min = 5, max = 255, message = "La justificación debe tener entre 5 y 255 caracteres.")
    private String justificacion;

    @AssertTrue(message = "Debe confirmar que ha verificado los requisitos del curso (lecciones, instructores y multimedia).")
    private boolean confirmarRequisitos;

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDate fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public boolean isConfirmarRequisitos() {
        return confirmarRequisitos;
    }

    public boolean getConfirmarRequisitos() {
        return confirmarRequisitos;
    }

    public void setConfirmarRequisitos(boolean confirmarRequisitos) {
        this.confirmarRequisitos = confirmarRequisitos;
    }
}
