import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';

export default function LoginForm() {
  const { login, loading } = useAuth();
  const [correo, setCorreo] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!correo.trim()) {
      setError('Por favor, ingresa tu correo electrónico.');
      return;
    }
    if (!contrasena) {
      setError('Por favor, ingresa tu contraseña.');
      return;
    }

    try {
      await login(correo.trim(), contrasena);
    } catch (err) {
      setError(err.message || 'Error al iniciar sesión.');
    }
  };

  // Helper para autocompletar credenciales en ambiente de prueba
  const handleQuickFill = (email, pass) => {
    setCorreo(email);
    setContrasena(pass);
    setError(null);
  };

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <div className="login-header">
          <div className="nav-logo-icon" style={{ margin: '0 auto', width: '48px', height: '48px', fontSize: '1.4rem' }}>
            ✋
          </div>
          <h1>Iniciar Sesión</h1>
          <p>Ingresa tus credenciales para acceder a tus cursos y matrículas en LESCO.</p>
        </div>

        {error && (
          <div className="alert alert-danger" role="alert">
            <span>⚠️</span>
            <div>{error}</div>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="correo">
              Correo Electrónico
            </label>
            <input
              id="correo"
              type="email"
              className="form-input"
              placeholder="estudiante@lescocr.cr"
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
              disabled={loading}
              autoComplete="username"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="contrasena">
              Contraseña
            </label>
            <input
              id="contrasena"
              type="password"
              className="form-input"
              placeholder="••••••••"
              value={contrasena}
              onChange={(e) => setContrasena(e.target.value)}
              disabled={loading}
              autoComplete="current-password"
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '0.5rem', padding: '0.75rem' }}
            disabled={loading}
          >
            {loading ? 'Validando...' : 'Entrar al Sistema'}
          </button>
        </form>

        <div className="test-accounts">
          <div className="test-accounts-title">Cuentas de Prueba para Evaluación</div>
          <div className="test-accounts-grid">
            <button
              type="button"
              className="btn-chip"
              onClick={() => handleQuickFill('luis.estudiante@lescocr.cr', 'Student1234!')}
            >
              <strong>Estudiante</strong>
              <span>luis.estudiante@lescocr.cr</span>
            </button>
            <button
              type="button"
              className="btn-chip"
              onClick={() => handleQuickFill('admin@lescocr.cr', 'Admin1234!')}
            >
              <strong>Administrador</strong>
              <span>admin@lescocr.cr</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
