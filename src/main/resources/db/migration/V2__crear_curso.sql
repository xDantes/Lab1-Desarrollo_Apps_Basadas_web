CREATE TABLE curso (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    nivel VARCHAR(20) NOT NULL,
    cupo_total INTEGER NOT NULL,
    precio NUMERIC(10,2) NOT NULL,
    descuento_porcentaje NUMERIC(5,2) NOT NULL DEFAULT 0,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    publicado BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_curso_codigo_periodo UNIQUE (codigo, fecha_inicio),
    CONSTRAINT ck_curso_nivel CHECK (nivel IN ('BASICO', 'INTERMEDIO', 'AVANZADO')),
    CONSTRAINT ck_curso_cupo_positivo CHECK (cupo_total > 0),
    CONSTRAINT ck_curso_precio_no_negativo CHECK (precio >= 0),
    CONSTRAINT ck_curso_descuento_rango CHECK (descuento_porcentaje BETWEEN 0 AND 100),
    CONSTRAINT ck_curso_fechas CHECK (fecha_fin > fecha_inicio)
);

CREATE INDEX idx_curso_publicado ON curso (publicado) WHERE publicado = TRUE;
