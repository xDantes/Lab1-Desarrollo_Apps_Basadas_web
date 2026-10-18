import React, { useState, useEffect } from 'react';
import { apiGetCursos, apiGetLecciones, apiCrearMatricula } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function NuevaMatriculaModal({ isOpen, onClose, onMatriculaCreada }) {
  const { user } = useAuth();

  const [cursos, setCursos] = useState([]);
  const [lecciones, setLecciones] = useState([]);

  // Form states
  const [cursoId, setCursoId] = useState('');
  const [leccionId, setLeccionId] = useState('');
  const [metodoPago, setMetodoPago] = useState('TARJETA');
  const [referencia, setReferencia] = useState('');

  // UI states
  const [loadingCursos, setLoadingCursos] = useState(false);
  const [loadingLecciones, setLoadingLecciones] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [exito, setExito] = useState(null);

  // Cargar cursos publicados al abrir el modal
  useEffect(() => {
    if (!isOpen) return;

    setError(null);
    setExito(null);
    setCursoId('');
    setLeccionId('');
    setMetodoPago('TARJETA');
    setReferencia('');

    const fetchCursos = async () => {
      setLoadingCursos(true);
      try {
        const data = await apiGetCursos(true);
        const lista = data.content || data || [];
        setCursos(lista);
      } catch (err) {
        setError(`Error al cargar catálogo de cursos: ${err.message}`);
      } finally {
        setLoadingCursos(false);
      }
    };

    fetchCursos();
  }, [isOpen]);

  // Cargar lecciones cuando cambia el curso seleccionado
  useEffect(() => {
    if (!cursoId) {
      setLecciones([]);
      setLeccionId('');
      return;
    }

    const fetchLecciones = async () => {
      setLoadingLecciones(true);
      setError(null);
      try {
        const data = await apiGetLecciones(cursoId);
        setLecciones(data || []);
        if (data && data.length > 0) {
          setLeccionId(data[0].id.toString());
        }
      } catch (err) {
        setError(`Error al cargar lecciones del curso: ${err.message}`);
      } finally {
        setLoadingLecciones(false);
      }
    };

    fetchLecciones();
  }, [cursoId]);

  if (!isOpen) return null;

  const selectedCurso = cursos.find((c) => c.id.toString() === cursoId.toString());

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setExito(null);

    if (!cursoId) {
      setError('Debes seleccionar un curso.');
      return;
    }
    if (!leccionId) {
      setError('Debes seleccionar una lección.');
      return;
    }

    if (metodoPago === 'SINPE_MOVIL') {
      const limpia = referencia.trim();
      if (!/^\d{8}$/.test(limpia)) {
        setError('El número de referencia SINPE Móvil debe contener exactamente 8 dígitos numéricos.');
        return;
      }
    }

    setSubmitting(true);

    try {
      const result = await apiCrearMatricula({
        estudianteId: user.usuarioId,
        cursoId: Number(cursoId),
        leccionId: Number(leccionId),
        metodoPago,
        referencia: metodoPago === 'SINPE_MOVIL' || metodoPago === 'TRANSFERENCIA' ? referencia.trim() : null,
      });

      setExito({
        consecutivo: result.consecutivo,
        mensaje: `¡Matrícula ${result.consecutivo} creada exitosamente en ${result.nombreCurso}!`,
      });

      // Notificar al componente padre para refrescar la lista
      if (onMatriculaCreada) {
        setTimeout(() => {
          onMatriculaCreada();
          onClose();
        }, 1800);
      }
    } catch (err) {
      setError(err.message || 'Error al procesar la matrícula.');
    } finally {
      setSubmitting(false);
    }
  };

  const formatMonto = (monto) => {
    if (!monto) return '₡0';
    return new Intl.NumberFormat('es-CR', {
      style: 'currency',
      currency: 'CRC',
      maximumFractionDigits: 0,
    }).format(monto);
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">Matricular Nuevo Curso LESCO</h2>
          <button className="modal-close" onClick={onClose} aria-label="Cerrar modal">
            &times;
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            {error && (
              <div className="alert alert-danger" role="alert">
                <span>⚠️</span>
                <div>{error}</div>
              </div>
            )}

            {exito && (
              <div className="alert alert-success" role="alert">
                <span>✓</span>
                <div>{exito.mensaje}</div>
              </div>
            )}

            {/* Paso 1: Selección de Curso */}
            <div className="form-group">
              <label className="form-label" htmlFor="cursoSelect">
                1. Selecciona el Curso
              </label>
              {loadingCursos ? (
                <div style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Cargando cursos disponibles...</div>
              ) : (
                <select
                  id="cursoSelect"
                  className="form-select"
                  value={cursoId}
                  onChange={(e) => setCursoId(e.target.value)}
                  disabled={submitting || !!exito}
                  required
                >
                  <option value="">-- Elige un curso --</option>
                  {cursos.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.codigo} — {c.nombre} ({c.nivel}) · Cupos: {c.cuposDisponibles}
                    </option>
                  ))}
                </select>
              )}
            </div>

            {/* Tarjeta de Resumen de Precio */}
            {selectedCurso && (
              <div className="price-card">
                <div>
                  <div className="price-card-label">Precio Final del Curso</div>
                  <div style={{ fontSize: '0.75rem', color: '#3b82f6' }}>
                    Nivel: {selectedCurso.nivel} · Cupos disponibles: {selectedCurso.cuposDisponibles} de {selectedCurso.cupoTotal}
                  </div>
                </div>
                <div className="price-card-amount">
                  {formatMonto(selectedCurso.precioFinal)}
                </div>
              </div>
            )}

            {/* Paso 2: Selección de Lección */}
            {cursoId && (
              <div className="form-group">
                <label className="form-label" htmlFor="leccionSelect">
                  2. Lección Inicial
                </label>
                {loadingLecciones ? (
                  <div style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Cargando lecciones...</div>
                ) : lecciones.length === 0 ? (
                  <div style={{ fontSize: '0.875rem', color: 'var(--warning)' }}>No hay lecciones registradas para este curso.</div>
                ) : (
                  <select
                    id="leccionSelect"
                    className="form-select"
                    value={leccionId}
                    onChange={(e) => setLeccionId(e.target.value)}
                    disabled={submitting || !!exito}
                    required
                  >
                    {lecciones.map((l) => (
                      <option key={l.id} value={l.id}>
                        Lección #{l.orden}: {l.titulo}
                      </option>
                    ))}
                  </select>
                )}
              </div>
            )}

            {/* Paso 3: Método de Pago */}
            <div className="form-group">
              <label className="form-label" htmlFor="metodoPagoSelect">
                3. Método de Pago
              </label>
              <select
                id="metodoPagoSelect"
                className="form-select"
                value={metodoPago}
                onChange={(e) => {
                  setMetodoPago(e.target.value);
                  setReferencia('');
                }}
                disabled={submitting || !!exito}
              >
                <option value="TARJETA">Tarjeta de Débito o Crédito</option>
                <option value="SINPE_MOVIL">SINPE Móvil (Costa Rica)</option>
                <option value="TRANSFERENCIA">Transferencia Bancaria</option>
              </select>
            </div>

            {/* Paso 4: Referencia condicional según método */}
            {metodoPago === 'SINPE_MOVIL' && (
              <div className="form-group">
                <label className="form-label" htmlFor="sinpeInput">
                  Número de Comprobante / Teléfono SINPE Móvil (8 dígitos)
                </label>
                <input
                  id="sinpeInput"
                  type="text"
                  maxLength={8}
                  className="form-input"
                  placeholder="88887777"
                  value={referencia}
                  onChange={(e) => setReferencia(e.target.value.replace(/\D/g, ''))}
                  disabled={submitting || !!exito}
                  required
                />
                <div className="form-help">Ingresa los 8 dígitos numéricos de la transferencia realizada.</div>
              </div>
            )}

            {metodoPago === 'TRANSFERENCIA' && (
              <div className="form-group">
                <label className="form-label" htmlFor="transfInput">
                  Código de Referencia Bancaria
                </label>
                <input
                  id="transfInput"
                  type="text"
                  maxLength={50}
                  className="form-input"
                  placeholder="BNCR-2026-998811"
                  value={referencia}
                  onChange={(e) => setReferencia(e.target.value)}
                  disabled={submitting || !!exito}
                  required
                />
              </div>
            )}

            {metodoPago === 'TARJETA' && (
              <div className="form-help" style={{ marginTop: '0.25rem' }}>
                💳 El pago con tarjeta se procesará directamente al confirmar la matrícula.
              </div>
            )}
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={submitting}>
              Cerrar
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={submitting || !cursoId || !leccionId || !!exito}
            >
              {submitting ? 'Registrando...' : 'Confirmar Matrícula'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
