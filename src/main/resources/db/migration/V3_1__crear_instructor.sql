-- Tabla de subtipo: no todo usuario puede ser instructor de una leccion, solo
-- los que tienen rol = 'INSTRUCTOR'. Antes esa regla dependia por completo de
-- que la aplicacion respetara usuario.rol; con esta tabla, leccion.instructor_id
-- (V4) referencia instructor(usuario_id) en vez de usuario(id) directamente, asi
-- que un usuario con rol ESTUDIANTE o ADMINISTRADOR no puede terminar asignado
-- a una leccion: la base de datos lo rechaza con un error de FK, no solo por
-- convencion de la aplicacion.
CREATE TABLE instructor (
    usuario_id BIGINT PRIMARY KEY REFERENCES usuario (id),
    especialidad VARCHAR(100),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Redundancia de seguridad: el FK de arriba impide asignar como instructor a
-- alguien que ni siquiera esta en esta tabla; este trigger impide que entren a
-- la tabla, en primer lugar, usuarios que no tienen rol INSTRUCTOR (por
-- ejemplo, por un bug de la aplicacion).
CREATE OR REPLACE FUNCTION fn_instructor_validar_rol() RETURNS TRIGGER AS $$
DECLARE
    rol_usuario VARCHAR(20);
BEGIN
    SELECT rol INTO rol_usuario FROM usuario WHERE id = NEW.usuario_id;
    IF rol_usuario IS DISTINCT FROM 'INSTRUCTOR' THEN
        RAISE EXCEPTION 'El usuario % no tiene rol INSTRUCTOR', NEW.usuario_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_instructor_validar_rol
    BEFORE INSERT OR UPDATE ON instructor
    FOR EACH ROW EXECUTE FUNCTION fn_instructor_validar_rol();

-- Los mismos dos instructores ya sembrados en V3.
INSERT INTO instructor (usuario_id, especialidad) VALUES
    ((SELECT id FROM usuario WHERE correo = 'carlos.instructor@lescocr.cr'), 'Lengua de señas para principiantes'),
    ((SELECT id FROM usuario WHERE correo = 'marcela.instructor@lescocr.cr'), 'Gramática de LESCO');
