import React, { useState } from 'react';
import { useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import LoginForm from './components/LoginForm';
import MatriculasList from './components/MatriculasList';
import NuevaMatriculaModal from './components/NuevaMatriculaModal';

export default function App() {
  const { isAuthenticated } = useAuth();
  const [modalOpen, setModalOpen] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  const handleMatriculaCreada = () => {
    // Incrementar refreshKey fuerza a MatriculasList a recargar los datos
    setRefreshKey((prev) => prev + 1);
  };

  return (
    <div className="app-container">
      <Navbar />

      <main className="main-content">
        {!isAuthenticated ? (
          <LoginForm />
        ) : (
          <>
            <MatriculasList
              key={refreshKey}
              onOpenNuevaMatricula={() => setModalOpen(true)}
            />

            <NuevaMatriculaModal
              isOpen={modalOpen}
              onClose={() => setModalOpen(false)}
              onMatriculaCreada={handleMatriculaCreada}
            />
          </>
        )}
      </main>
    </div>
  );
}
