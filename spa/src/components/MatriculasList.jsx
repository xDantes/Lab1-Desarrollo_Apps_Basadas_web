import React, { useState, useEffect, useCallback } from 'react';
import { apiGetMatriculas, apiCancelarMatricula, apiAprobarMatricula } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function MatriculasList({ onOpenNuevaMatricula }) {
  const { user } = useAuth();

  // Estados de Paginación y Filtro
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [estadoFiltro, setEstadoFiltro] = useState('');

  // Estados requeridos de la vista: Carga, Datos y Error
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [data, setData] = useState({ content: [], totalElements: 0, totalPages: 0 });

  // Estado para acciones en curso (cancelar/aprobar)
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const [feedbackMessage, setFeedbackMessage] = useState(null);

  // Función para cargar los datos paginados
  const fetchMatriculas = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      // Si el usuario es ESTUDIANTE, opcionalmente filtramos por su usuarioId para ver sus registros,
      // o dejamos que el backend aplique los filtros
      const result = await apiGetMatriculas({
        page,
        size,
        estado: estadoFiltro || null,
        usuarioId: user.rol === 'ESTUDIANTE' ? user.usuarioId : null,
      });

      setData(result || { content: [], totalElements: 0, totalPages: 0 });
    } catch (err) {
      setError(err.message || 'No fue posible cargar las matrículas.');
    } finally {
      setLoading(false);
    }
  }, [page, size, estadoFiltro, user]);

  useEffect(() => {
    fetchMatriculas();
  }, [fetchMatriculas]);

  // Manejo de cancelación de matrícula
  const handleCancelar = async (matricula) => {
    const confirmacion = window.confirm(
      `¿Estás seguro de cancelar la matrícula ${matricula.consecutivo} del curso "${matricula.nombreCurso}"?`
    );
    if (!confirmacion) return;

    setActionLoadingId(matricula.matriculaId);
    setFeedbackMessage(null);

    try {
      await apiCancelarMatricula(matricula.matriculaId);
      setFeedbackMessage({
        type: 'success',
        text: `La matrícula ${matricula.consecutivo} ha sido cancelada correctamente.`,
      });
      await fetchMatriculas();
    } catch (err) {
      setFeedbackMessage({
        type: 'danger',
        text: `Error al cancelar matrícula: ${err.message}`,
      });
    } finally {
      setActionLoadingId(null);
    }
  };

  // Manejo de aprobación de matrícula (solo para administradores)
  const handleAprobar = async (matricula) => {
    setActionLoadingId(matricula.matriculaId);
    setFeedbackMessage(null);

    try {
      await apiAprobarMatricula(matricula.matriculaId);
      setFeedbackMessage({
        type: 'success',
        text: `El pago de la matrícula ${matricula.consecutivo} ha sido aprobado. Ahora está ACTIVA.`,
      });
      await fetchMatriculas();
    } catch (err) {
      setFeedbackMessage({
        type: 'danger',
        text: `Error al aprobar pago: ${err.message}`,
      });
    } finally {
      setActionLoadingId(null);
    }
  };

  // Helper para formato de moneda (Costa Rica Colones ₡)
  const formatPrecio = (monto) => {
    if (monto === null || monto === undefined) return '₡0';
    return new Intl.NumberFormat('es-CR', {
      style: 'currency',
      currency: 'CRC',
      maximumFractionDigits: 0,
    }).format(monto);
  };

  // Helper para formato de fecha
  const formatFecha = (fechaStr) => {
    if (!fechaStr) return '-';
    try {
      const fecha = new Date(fechaStr);
      return fecha.toLocaleDateString('es-CR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return fechaStr;
    }
  };

  // Helper para badge de estado
  const renderBadge = (estado) => {
    switch (estado) {
      case 'ACTIVA':
        return <span className="badge badge-activa">✓ Activa</span>;
      case 'PENDIENTE':
        return <span className="badge badge-pendiente">⏳ Pendiente</span>;
      case 'CANCELADA':
        return <span className="badge badge-cancelada">✕ Cancelada</span>;
      default:
        return <span className="badge">{estado}</span>;
    }
  };

  return (
    <div>
      {/* Encabezado y botón para proceso principal */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Gestión de Matrículas</h1>
          <p className="page-description">
            {user.rol === 'ADMINISTRADOR'
              ? 'Panel de control de todas las matrículas registradas en la plataforma LESCO.'
              : 'Tus cursos y lecciones matriculadas en Lengua de Señas Costarricense.'}
          </p>
        </div>

        {user.rol === 'ESTUDIANTE' && (
          <button className="btn btn-primary" onClick={onOpenNuevaMatricula}>
            <span>+</span> Nueva Matrícula
          </button>
        )}
      </div>

      {/* Mensaje de feedback temporal */}
      {feedbackMessage && (
        <div className={`alert alert-${feedbackMessage.type}`} role="alert">
          <span>{feedbackMessage.type === 'success' ? '✓' : '⚠️'}</span>
          <div>{feedbackMessage.text}</div>
        </div>
      )}

      {/* Barra de Filtros y Configuración */}
      <div className="toolbar">
        <div className="filters-group">
          <label className="form-label" style={{ margin: 0 }}>
            Estado:
          </label>
          <select
            className="form-select"
            style={{ width: 'auto' }}
            value={estadoFiltro}
            onChange={(e) => {
              setEstadoFiltro(e.target.value);
              setPage(0); // Reiniciar a la primera página
            }}
          >
            <option value="">Todos los estados</option>
            <option value="ACTIVA">Activas</option>
            <option value="PENDIENTE">Pendientes</option>
            <option value="CANCELADA">Canceladas</option>
          </select>
        </div>

        <div className="filters-group">
          <label className="form-label" style={{ margin: 0 }}>
            Registros por página:
          </label>
          <select
            className="form-select"
            style={{ width: 'auto' }}
            value={size}
            onChange={(e) => {
              setSize(Number(e.target.value));
              setPage(0);
            }}
          >
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
          </select>
        </div>
      </div>

      {/* ───────────────────────────────────────────────────────────────────
          1. ESTADO DE CARGA (LOADING)
         ─────────────────────────────────────────────────────────────────── */}
      {loading && (
        <div className="card state-container">
          <div className="spinner" />
          <div className="state-loading-text">Cargando matrículas desde la API...</div>
        </div>
      )}

      {/* ───────────────────────────────────────────────────────────────────
          2. ESTADO DE ERROR
         ─────────────────────────────────────────────────────────────────── */}
      {!loading && error && (
        <div className="card state-container">
          <div className="state-error-icon">⚠️</div>
          <h2 className="state-error-title">No fue posible obtener los datos</h2>
          <p className="state-error-desc">{error}</p>
          <button className="btn btn-primary" onClick={fetchMatriculas}>
            Reintentar
          </button>
        </div>
      )}

      {/* ───────────────────────────────────────────────────────────────────
          3. ESTADO DE DATOS (DATA)
         ─────────────────────────────────────────────────────────────────── */}
      {!loading && !error && (
        <>
          {data.content.length === 0 ? (
            <div className="card state-container">
              <div style={{ fontSize: '2.5rem', marginBottom: '0.75rem' }}>📋</div>
              <h2 className="state-error-title">No se encontraron matrículas</h2>
              <p className="state-error-desc">
                {estadoFiltro
                  ? 'No hay registros con el estado seleccionado.'
                  : 'Aún no tienes matrículas registradas en el sistema.'}
              </p>
              {user.rol === 'ESTUDIANTE' && (
                <button className="btn btn-primary" onClick={onOpenNuevaMatricula}>
                  Matricular un Curso Ahora
                </button>
              )}
            </div>
          ) : (
            <div className="table-responsive">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Consecutivo</th>
                    {user.rol === 'ADMINISTRADOR' && <th>Estudiante</th>}
                    <th>Curso</th>
                    <th>Lección</th>
                    <th>Precio</th>
                    <th>Método</th>
                    <th>Estado</th>
                    <th>Fecha</th>
                    <th style={{ textAlign: 'right' }}>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((m) => {
                    const isOperating = actionLoadingId === m.matriculaId;
                    const puedeCancelar = m.estadoMatricula === 'PENDIENTE' || m.estadoMatricula === 'ACTIVA';
                    const puedeAprobar = user.rol === 'ADMINISTRADOR' && m.estadoMatricula === 'PENDIENTE';

                    return (
                      <tr key={m.matriculaId}>
                        <td>
                          <strong>{m.consecutivo}</strong>
                        </td>
                        {user.rol === 'ADMINISTRADOR' && <td>{m.nombreEstudiante || `ID: ${m.estudianteId}`}</td>}
                        <td>
                          <div><strong>{m.codigoCurso}</strong></div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{m.nombreCurso}</div>
                        </td>
                        <td>{m.tituloLeccion || `Lección #${m.leccionId}`}</td>
                        <td>
                          <strong>{formatPrecio(m.precioFinal)}</strong>
                        </td>
                        <td>
                          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                            {m.metodoPago === 'SINPE_MOVIL'
                              ? '📱 SINPE Móvil'
                              : m.metodoPago === 'TARJETA'
                              ? '💳 Tarjeta'
                              : '🏦 Transferencia'}
                          </span>
                        </td>
                        <td>{renderBadge(m.estadoMatricula)}</td>
                        <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          {formatFecha(m.fechaMatricula)}
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          <div style={{ display: 'inline-flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                            {puedeAprobar && (
                              <button
                                className="btn btn-primary"
                                style={{ padding: '0.35rem 0.65rem', fontSize: '0.78rem' }}
                                onClick={() => handleAprobar(m)}
                                disabled={isOperating}
                              >
                                {isOperating ? '...' : 'Aprobar Pago'}
                              </button>
                            )}

                            {puedeCancelar && (
                              <button
                                className="btn btn-danger-outline"
                                onClick={() => handleCancelar(m)}
                                disabled={isOperating}
                              >
                                {isOperating ? '...' : 'Cancelar'}
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}

          {/* Controles de Paginación */}
          {data.totalElements > 0 && (
            <div className="pagination">
              <div className="pagination-info">
                Mostrando {data.content.length} de {data.totalElements} registros
                (Página {data.totalPages === 0 ? 0 : page + 1} de {data.totalPages})
              </div>

              <div className="pagination-controls">
                <button
                  className="btn-page"
                  onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                  disabled={page === 0 || loading}
                >
                  ← Anterior
                </button>

                <button
                  className="btn-page active"
                  disabled
                >
                  {page + 1}
                </button>

                <button
                  className="btn-page"
                  onClick={() => setPage((prev) => prev + 1)}
                  disabled={page + 1 >= data.totalPages || loading}
                >
                  Siguiente →
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
