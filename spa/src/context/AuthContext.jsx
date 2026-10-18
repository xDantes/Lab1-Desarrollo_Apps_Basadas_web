import React, { createContext, useContext, useState, useEffect } from 'react';
import { apiLogin } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem('lesco_user');
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Escuchar expiración de token reportada por el api service
    const handleUnauthorized = () => {
      setUser(null);
    };
    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, []);

  const login = async (correo, contrasena) => {
    setLoading(true);
    try {
      const data = await apiLogin(correo, contrasena);
      const userData = {
        token: data.token,
        correo: data.correo,
        rol: data.rol,
        usuarioId: data.usuarioId,
        nombre: data.nombre || data.correo,
      };
      localStorage.setItem('lesco_token', data.token);
      localStorage.setItem('lesco_user', JSON.stringify(userData));
      setUser(userData);
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('lesco_token');
    localStorage.removeItem('lesco_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        login,
        logout,
        loading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe ser utilizado dentro de un AuthProvider');
  }
  return context;
}
