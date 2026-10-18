// ===================================================================
// Servicio centralizado de llamadas a la API REST (LESCO CR)
// ===================================================================

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

/**
 * Obtiene el token JWT actual almacenado en localStorage.
 */
export function getToken() {
  return localStorage.getItem('lesco_token');
}

/**
 * Helper para peticiones HTTP con encabezados y manejo de errores RFC 9457 (Problem Detail).
 */
async function request(endpoint, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const config = {
    ...options,
    headers,
  };

  const url = `${BASE_URL}${endpoint}`;
  let response;

  try {
    response = await fetch(url, config);
  } catch (netErr) {
    throw new Error('No se pudo conectar con el servidor. Verifica tu conexión de red o que el backend esté en ejecución.');
  }

  // Manejo de 204 No Content
  if (response.status === 204) {
    return null;
  }

  // Intentar parsear JSON
  let data = null;
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('json')) {
    try {
      data = await response.json();
    } catch {
      data = null;
    }
  }

  // Si no es OK (2xx), extraer mensaje amigable
  if (!response.ok) {
    if (response.status === 401) {
      // Token expirado o inválido
      localStorage.removeItem('lesco_token');
      localStorage.removeItem('lesco_user');
      window.dispatchEvent(new Event('auth:unauthorized'));
      throw new Error(data?.detail || 'Tu sesión ha expirado. Por favor, inicia sesión de nuevo.');
    }

    if (data && data.detail) {
      throw new Error(data.detail);
    } else if (data && data.title) {
      throw new Error(data.title);
    } else if (data && data.message) {
      throw new Error(data.message);
    } else {
      throw new Error(`Error en la solicitud (Código HTTP ${response.status}).`);
    }
  }

  return data;
}

// -------------------------------------------------------------------
// 1. Autenticación
// -------------------------------------------------------------------

export async function apiLogin(correo, contrasena) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ correo, contrasena }),
  });
}

// -------------------------------------------------------------------
// 2. Cursos y Lecciones
// -------------------------------------------------------------------

export async function apiGetCursos(publicado = true) {
  const query = publicado ? '?publicado=true&size=50' : '?size=50';
  return request(`/api/v1/cursos${query}`);
}

export async function apiGetLecciones(cursoId) {
  return request(`/api/v1/cursos/${cursoId}/lecciones`);
}

// -------------------------------------------------------------------
// 3. Matrículas (Listado Paginado, Creación, Cancelación)
// -------------------------------------------------------------------

export async function apiGetMatriculas({ page = 0, size = 10, estado = '', usuarioId = null } = {}) {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
    sort: 'fecha,desc',
  });

  if (estado) {
    params.append('estado', estado);
  }
  if (usuarioId) {
    params.append('usuarioId', usuarioId.toString());
  }

  return request(`/api/v1/matriculas?${params.toString()}`);
}

export async function apiCrearMatricula({ estudianteId, cursoId, leccionId, metodoPago, referencia }) {
  return request('/api/v1/matriculas', {
    method: 'POST',
    body: JSON.stringify({
      estudianteId,
      cursoId,
      leccionId,
      metodoPago,
      referencia: referencia || null,
    }),
  });
}

export async function apiCancelarMatricula(matriculaId) {
  return request(`/api/v1/matriculas/${matriculaId}/cancelar`, {
    method: 'POST',
  });
}

export async function apiAprobarMatricula(matriculaId) {
  return request(`/api/v1/matriculas/${matriculaId}/aprobar`, {
    method: 'POST',
  });
}
