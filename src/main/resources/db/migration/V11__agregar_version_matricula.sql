-- =============================================================================
-- V11: Bloqueo optimista (@Version) para control de concurrencia en Matricula
--      (Laboratorio 7: Concurrencia controlada).
--
-- Justificación de contención:
-- La transición de estado de una matrícula (aprobación de pago por parte del
-- administrador vs. cancelación por parte del estudiante, o doble aprobación
-- simultánea por dos administradores) presenta contención real en el dominio.
-- La columna `version` permite a Hibernate verificar que la fila no fue modificada
-- concurrentemente entre la lectura y la actualización (evita lost updates).
-- =============================================================================

ALTER TABLE matricula
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
