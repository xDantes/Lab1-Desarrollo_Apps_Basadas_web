-- Contenido dentro de un curso. instructor_id referencia instructor(usuario_id)
-- (V3_1), no usuario(id) directamente: asi la base de datos garantiza que solo
-- un usuario con rol INSTRUCTOR puede quedar asignado a una leccion.
CREATE TABLE leccion (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    orden INTEGER NOT NULL,
    curso_id BIGINT NOT NULL REFERENCES curso (id) ON DELETE CASCADE,
    instructor_id BIGINT NOT NULL REFERENCES instructor (usuario_id),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Evita dos lecciones con el mismo numero de orden en un mismo curso; de
    -- paso sirve de indice para listar las lecciones de un curso en orden.
    CONSTRAINT uq_leccion_curso_orden UNIQUE (curso_id, orden),
    -- El orden es una posicion (1, 2, 3...), no tiene sentido en 0 o negativo.
    CONSTRAINT ck_leccion_orden_positivo CHECK (orden > 0)
);

-- Panel del instructor: listar "mis lecciones" sin recorrer todos los cursos.
CREATE INDEX idx_leccion_instructor ON leccion (instructor_id);
