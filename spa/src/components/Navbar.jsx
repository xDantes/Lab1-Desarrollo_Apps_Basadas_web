import React from 'react';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();

  return (
    <header className="navbar">
      <div className="nav-brand">
        <div className="nav-logo-icon">✋</div>
        <div>
          <div className="nav-title">LESCO Costa Rica</div>
          <div className="nav-subtitle">Portal de Estudiantes y Matrículas</div>
        </div>
      </div>

      {user && (
        <div className="nav-user">
          <div className="user-badge">
            <span className="user-name">{user.nombre || user.correo}</span>
            <span className="user-role">{user.rol}</span>
          </div>
          <button className="btn btn-secondary" onClick={logout} title="Cerrar sesión">
            Cerrar Sesión
          </button>
        </div>
      )}
    </header>
  );
}
