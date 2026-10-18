-- ===================================================================
-- V10: Actualizar contraseñas de los usuarios semilla para desarrollo
-- ===================================================================
-- Reemplaza los placeholders '$2a$10$examplehashplaceholder...' con hashes
-- BCrypt reales para permitir la autenticación con JWT.
--
-- Credenciales válidas:
--   - Administradores / Instructores: Admin1234!
--   - Estudiantes:                   Student1234!

UPDATE usuario
SET contrasena_hash = '$2a$10$PrtPp0n.nxWbKrnApHvhLeRgvONAqFktanqIMW2QZ95aN6dIEZG0.'
WHERE rol IN ('ADMINISTRADOR', 'INSTRUCTOR');

UPDATE usuario
SET contrasena_hash = '$2a$10$qDAn6UhVQ/MOcyMcM.Akoehb3OUB2W35TlY4EXzg2UFCOonRgqFVa'
WHERE rol = 'ESTUDIANTE';
