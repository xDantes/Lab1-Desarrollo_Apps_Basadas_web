package cr.ac.una.lab1.business;

import cr.ac.una.lab1.business.exception.AccesoNoAutorizadoException;
import cr.ac.una.lab1.business.exception.EntidadNoEncontradaException;
import cr.ac.una.lab1.data.EstadoPago;
import cr.ac.una.lab1.data.Matricula;
import cr.ac.una.lab1.data.MetodoPago;
import cr.ac.una.lab1.data.Pago;
import cr.ac.una.lab1.data.PagoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;

    public PagoService(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    @Transactional(readOnly = true)
    public List<PagoResponseDTO> listar(
            EstadoPago estado,
            MetodoPago metodo,
            Long matriculaId,
            String correoAutenticado,
            boolean esAdmin) {

        List<Pago> pagos;
        if (estado != null) {
            pagos = pagoRepository.findByEstadoConDetalleCompleto(estado);
        } else if (metodo != null) {
            pagos = pagoRepository.findByMetodo(metodo);
        } else if (matriculaId != null) {
            pagos = pagoRepository.findByMatriculaId(matriculaId).map(List::of).orElseGet(List::of);
        } else {
            pagos = pagoRepository.findAll();
        }

        // OWASP API1: Estudiante solo puede ver sus propios pagos
        return pagos.stream()
                .filter(p -> esAdmin || esPropietario(p, correoAutenticado))
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagoResponseDTO obtenerPorId(Long id, String correoAutenticado, boolean esAdmin) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Pago", id));

        // OWASP API1
        if (!esAdmin && !esPropietario(pago, correoAutenticado)) {
            throw new AccesoNoAutorizadoException("No tiene permiso para consultar este pago (OWASP API1).");
        }

        return toDTO(pago);
    }

    private boolean esPropietario(Pago p, String correoAutenticado) {
        return p.getMatricula() != null
                && p.getMatricula().getUsuario() != null
                && correoAutenticado.equals(p.getMatricula().getUsuario().getCorreo());
    }

    private PagoResponseDTO toDTO(Pago p) {
        Matricula m = p.getMatricula();
        return new PagoResponseDTO(
                p.getId(),
                m != null ? m.getId() : null,
                m != null ? m.getConsecutivo() : null,
                (m != null && m.getUsuario() != null) ? m.getUsuario().getId() : null,
                (m != null && m.getUsuario() != null) ? m.getUsuario().getCorreo() : null,
                p.getMonto(),
                p.getMetodo(),
                p.getReferencia(),
                p.getEstado(),
                p.getFecha(),
                p.getActualizadoEn()
        );
    }
}
