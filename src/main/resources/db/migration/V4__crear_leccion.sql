CREATE TABLE leccion (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    orden INTEGER NOT NULL,
    curso_id BIGINT NOT NULL REFERENCES curso (id) ON DELETE CASCADE,
    instructor_id BIGINT NOT NULL REFERENCES usuario (id),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_leccion_curso_orden UNIQUE (curso_id, orden),
    CONSTRAINT ck_leccion_orden_positivo CHECK (orden > 0)
);

CREATE INDEX idx_leccion_instructor ON leccion (instructor_id);
