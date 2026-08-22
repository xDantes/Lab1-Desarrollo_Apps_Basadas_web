CREATE TABLE matricula (
    id BIGSERIAL PRIMARY KEY,
    consecutivo VARCHAR(20) NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuario (id),
    leccion_id BIGINT NOT NULL REFERENCES leccion (id),
    curso_id BIGINT NOT NULL REFERENCES curso (id),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    precio_final NUMERIC(10,2) NOT NULL,
    fecha TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_matricula_consecutivo UNIQUE (consecutivo),
    CONSTRAINT ck_matricula_estado CHECK (estado IN ('PENDIENTE', 'ACTIVA', 'CANCELADA')),
    CONSTRAINT ck_matricula_precio_no_negativo CHECK (precio_final >= 0)
);

-- curso_id es una copia derivada de leccion.curso_id: un UNIQUE/INDEX de
-- Postgres no puede mirar a traves de un JOIN, y la regla "un estudiante no
-- puede matricularse dos veces en el mismo curso" es a nivel de curso aunque
-- la matricula se haga por leccion. Este trigger la mantiene sincronizada
-- (la calcula siempre a partir de leccion_id, nunca confia en lo que mande la
-- aplicacion), asi que no es una redundancia manual sino un valor derivado
-- con integridad garantizada por la base de datos.
CREATE OR REPLACE FUNCTION fn_matricula_set_curso_id() RETURNS TRIGGER AS $$
BEGIN
    SELECT curso_id INTO NEW.curso_id FROM leccion WHERE id = NEW.leccion_id;
    IF NEW.curso_id IS NULL THEN
        RAISE EXCEPTION 'La leccion % no existe', NEW.leccion_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_matricula_set_curso_id
    BEFORE INSERT OR UPDATE OF leccion_id ON matricula
    FOR EACH ROW EXECUTE FUNCTION fn_matricula_set_curso_id();

-- Un estudiante no puede tener dos matriculas vigentes en el mismo curso;
-- CANCELADA libera el cupo para un nuevo intento de matricula.
CREATE UNIQUE INDEX uq_matricula_usuario_curso_vigente
    ON matricula (usuario_id, curso_id)
    WHERE estado <> 'CANCELADA';

-- Calculo de cupos disponibles (cupo_total - matriculas activas), agrupado por curso+estado.
CREATE INDEX idx_matricula_curso_estado ON matricula (curso_id, estado);

-- "Mis cursos" del estudiante.
CREATE INDEX idx_matricula_usuario ON matricula (usuario_id);

-- Roster de estudiantes matriculados en una leccion (vista del instructor).
CREATE INDEX idx_matricula_leccion ON matricula (leccion_id);
