package cr.ac.una.lab1.business;

import cr.ac.una.lab1.business.exception.CambioEstadoInvalidoException;
import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.business.exception.ReglaNegocioException;
import cr.ac.una.lab1.business.strategy.ValidadorMetodoPago;
import cr.ac.una.lab1.data.Curso;
import cr.ac.una.lab1.data.EstadoMatricula;
import cr.ac.una.lab1.data.EstadoPago;
import cr.ac.una.lab1.data.Leccion;
import cr.ac.una.lab1.data.Matricula;
import cr.ac.una.lab1.data.MatriculaRepository;
import cr.ac.una.lab1.data.MetodoPago;
import cr.ac.una.lab1.data.Pago;
import cr.ac.una.lab1.data.PagoRepository;
import cr.ac.una.lab1.data.CursoRepository;
import cr.ac.una.lab1.data.LeccionRepository;
import cr.ac.una.lab1.data.RolUsuario;
import cr.ac.una.lab1.data.Usuario;
import cr.ac.una.lab1.data.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio del <strong>Proceso 2 — Matriculación de un estudiante</strong>.
 *
 * <h3>Proceso multi-paso bajo {@code @Transactional}</h3>
 * <ol>
 *   <li>Validar estudiante (existe y tiene rol ESTUDIANTE).
 *   <li>Validar curso (existe y está publicado).
 *   <li>Validar lección (existe y pertenece al curso).
 *   <li>Calcular cupos disponibles → sin cupo lanza {@link ReglaNegocioException}.
 *   <li>Verificar que el estudiante no tenga matrícula vigente en el mismo curso.
 *   <li>Validar referencia de pago según método (patrón Strategy).
 *   <li>Calcular precio final con descuento.
 *   <li>Generar consecutivo único.
 *   <li>Persistir {@link Matricula} (PENDIENTE).
 *   <li>Persistir {@link Pago} (PENDIENTE).
 *       ← Si falla aquí, el paso 9 hace rollback automático.
 * </ol>
 *
 * <p>Los cambios de estado posteriores (activar/cancelar) delegan al patrón
 * {@link cr.ac.una.lab1.business.state.MatriculaEstado} para que las
 * transiciones inválidas sean rechazadas por la matrícula misma.
 */
@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final PagoRepository pagoRepository;
    private final CursoRepository cursoRepository;
    private final LeccionRepository leccionRepository;
    private final UsuarioRepository usuarioRepository;

    /** Patrón Strategy: un validador por cada método de pago, inyectado por Spring. */
    private final Map<MetodoPago, ValidadorMetodoPago> validadores;

    public MatriculaService(
            MatriculaRepository matriculaRepository,
            PagoRepository pagoRepository,
            CursoRepository cursoRepository,
            LeccionRepository leccionRepository,
            UsuarioRepository usuarioRepository,
            Map<MetodoPago, ValidadorMetodoPago> validadores) {
        this.matriculaRepository = matriculaRepository;
        this.pagoRepository = pagoRepository;
        this.cursoRepository = cursoRepository;
        this.leccionRepository = leccionRepository;
        this.usuarioRepository = usuarioRepository;
        this.validadores = validadores;
    }

    // -----------------------------------------------------------------------
    // Proceso 2: Matriculación
    // -----------------------------------------------------------------------

    /**
     * Matricula un estudiante en una lección de un curso publicado.
     *
     * <p>Toda la operación ocurre bajo una única transacción. Si cualquiera de
     * los pasos falla, Spring revierte todos los cambios persistidos hasta ese
     * momento (incluida la Matricula creada antes del Pago).
     *
     * @param dto datos validados con Bean Validation por el controlador
     * @return datos de la matrícula y pago creados
     */
    @Transactional
    public MatriculaResponseDTO matricular(MatriculaRequestDTO dto) {

        // Paso 1: validar estudiante
        Usuario estudiante = usuarioRepository.findById(dto.estudianteId())
                .orElseThrow(() -> new EntidadNoEncontradaException("Usuario", dto.estudianteId()));
        if (estudiante.getRol() != RolUsuario.ESTUDIANTE) {
            throw new ReglaNegocioException(
                    "El usuario con id " + dto.estudianteId() + " no tiene rol ESTUDIANTE.");
        }

        // Paso 2: validar curso publicado
        Curso curso = cursoRepository.findById(dto.cursoId())
                .orElseThrow(() -> new EntidadNoEncontradaException("Curso", dto.cursoId()));
        if (!curso.isPublicado()) {
            throw new ReglaNegocioException(
                    "El curso '" + curso.getCodigo() + "' no está publicado.");
        }

        // Paso 3: validar lección perteneciente al curso
        Leccion leccion = leccionRepository.findById(dto.leccionId())
                .orElseThrow(() -> new EntidadNoEncontradaException("Leccion", dto.leccionId()));
        if (!leccion.getCurso().getId().equals(curso.getId())) {
            throw new ReglaNegocioException(
                    "La lección " + dto.leccionId() + " no pertenece al curso " + dto.cursoId() + ".");
        }

        // Paso 4: verificar cupo disponible
        int matriculasActivas = matriculaRepository
                .findByCursoIdAndEstado(curso.getId(), EstadoMatricula.ACTIVA).size();
        int cuposDisponibles = curso.getCupoTotal() - matriculasActivas;
        if (cuposDisponibles <= 0) {
            throw new ReglaNegocioException(
                    "El curso '" + curso.getCodigo() + "' no tiene cupo disponible.");
        }

        // Paso 5: verificar que no haya matrícula vigente del mismo estudiante en el mismo curso
        boolean yaMatriculado = matriculaRepository
                .findByCursoIdAndEstado(curso.getId(), EstadoMatricula.ACTIVA)
                .stream()
                .anyMatch(m -> m.getUsuario().getId().equals(estudiante.getId()));
        if (!yaMatriculado) {
            // También verificar PENDIENTE (podría haber una en proceso)
            yaMatriculado = matriculaRepository
                    .findByCursoIdAndEstado(curso.getId(), EstadoMatricula.PENDIENTE)
                    .stream()
                    .anyMatch(m -> m.getUsuario().getId().equals(estudiante.getId()));
        }
        if (yaMatriculado) {
            throw new ReglaNegocioException(
                    "El estudiante ya tiene una matrícula vigente en el curso '" + curso.getCodigo() + "'.");
        }

        // Paso 6: validar referencia de pago (patrón Strategy)
        ValidadorMetodoPago validador = validadores.get(dto.metodoPago());
        if (validador == null) {
            throw new ReglaNegocioException("Método de pago no soportado: " + dto.metodoPago());
        }
        validador.validar(dto);

        // Paso 7: calcular precio final
        BigDecimal precioFinal = calcularPrecioConDescuento(curso.getPrecio(), curso.getDescuentoPorcentaje());

        // Paso 8: generar consecutivo
        String consecutivo = generarConsecutivo();

        // Paso 9: persistir Matricula (PENDIENTE)
        Matricula matricula = new Matricula(consecutivo, estudiante, leccion, EstadoMatricula.PENDIENTE, precioFinal);
        matricula = matriculaRepository.save(matricula);

        // Paso 10: persistir Pago (PENDIENTE)
        // Si falla aquí (p. ej. por un spy en pruebas), Spring revierte el paso 9.
        Pago pago = new Pago(matricula, precioFinal, dto.metodoPago(), dto.referencia(), EstadoPago.PENDIENTE);
        pago = pagoRepository.save(pago);

        return toResponseDTO(matricula, curso, pago);
    }

    // -----------------------------------------------------------------------
    // Gestión de estado (patrón State en Matricula)
    // -----------------------------------------------------------------------

    /**
     * Aprueba el pago: transición PENDIENTE → ACTIVA.
     * Delega a {@link Matricula#activar()}; lanza {@link CambioEstadoInvalidoException}
     * si la transición no es válida.
     */
    @Transactional
    public MatriculaResponseDTO aprobarPago(Long matriculaId) {
        Matricula matricula = obtenerMatricula(matriculaId);
        matricula.activar(); // patrón State valida la transición
        Pago pago = pagoRepository.findByMatriculaId(matriculaId).orElse(null);
        // Dentro de @Transactional el proxy LAZY de leccion.getCurso() es inicializable.
        Curso curso = matricula.getLeccion().getCurso();
        return toResponseDTO(matriculaRepository.save(matricula), curso, pago);
    }

    /**
     * Cancela una matrícula: transición PENDIENTE/ACTIVA → CANCELADA.
     * Delega a {@link Matricula#cancelar()}; lanza {@link CambioEstadoInvalidoException}
     * si la transición no es válida (p. ej. ya estaba CANCELADA).
     */
    @Transactional
    public MatriculaResponseDTO cancelarMatricula(Long matriculaId) {
        Matricula matricula = obtenerMatricula(matriculaId);
        matricula.cancelar(); // patrón State valida la transición
        Pago pago = pagoRepository.findByMatriculaId(matriculaId).orElse(null);
        Curso curso = matricula.getLeccion().getCurso();
        return toResponseDTO(matriculaRepository.save(matricula), curso, pago);
    }

    // -----------------------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------------------

    private Matricula obtenerMatricula(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Matricula", id));
    }

    private BigDecimal calcularPrecioConDescuento(BigDecimal precio, BigDecimal descuento) {
        BigDecimal factor = BigDecimal.ONE.subtract(
                descuento.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return precio.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private String generarConsecutivo() {
        int anio = LocalDate.now().getYear();
        long siguiente = matriculaRepository.count() + 1;
        return String.format("MAT-%d-%06d", anio, siguiente);
    }

    private MatriculaResponseDTO toResponseDTO(Matricula m, Curso curso, Pago p) {
        return new MatriculaResponseDTO(
                m.getId(),
                m.getConsecutivo(),
                m.getUsuario().getId(),
                m.getUsuario().getNombre(),
                curso.getId(),
                curso.getCodigo(),
                curso.getNombre(),
                m.getLeccion().getId(),
                m.getLeccion().getTitulo(),
                m.getEstado(),
                m.getPrecioFinal(),
                m.getFecha(),
                p != null ? p.getId() : null,
                p != null ? p.getEstado() : null,
                p != null ? p.getMetodo() : null
        );
    }
}
