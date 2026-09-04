-- Unidad matriculable. cupo_disponible NO se almacena aqui: se calcula
-- (cupo_total menos matriculas activas, ver V6) para no guardar un valor
-- derivado que se desincronice del real.
--
-- El instructor NO se asigna aqui: se asigna por leccion (ver V4), porque un
-- mismo curso puede tener lecciones a cargo de distintos instructores.
CREATE TABLE curso (
    id                   BIGSERIAL PRIMARY KEY,
    codigo               VARCHAR(20)   NOT NULL,
    nombre               VARCHAR(150)  NOT NULL,
    descripcion          TEXT,
    nivel                VARCHAR(20)   NOT NULL,
    cupo_total           INTEGER       NOT NULL,
    precio               NUMERIC(10,2) NOT NULL,
    descuento_porcentaje NUMERIC(5,2)  NOT NULL DEFAULT 0,
    fecha_inicio         DATE          NOT NULL,
    fecha_fin            DATE          NOT NULL,
    publicado            BOOLEAN       NOT NULL DEFAULT FALSE,
    creado_en            TIMESTAMPTZ   NOT NULL DEFAULT now(),

    -- No se permite publicar un curso duplicado en el mismo periodo (regla del
    -- Proceso 2 de la propuesta de dominio).
    CONSTRAINT uq_curso_codigo_periodo UNIQUE (codigo, fecha_inicio),
    -- Solo se admiten los 3 niveles definidos en la propuesta de dominio.
    CONSTRAINT ck_curso_nivel CHECK (nivel IN ('BASICO', 'INTERMEDIO', 'AVANZADO')),
    -- Un curso sin cupo no tiene sentido (y evita division por 0 / cupos negativos
    -- al calcular cupo_total - matriculas_activas).
    CONSTRAINT ck_curso_cupo_positivo CHECK (cupo_total > 0),
    -- El precio es la base sobre la que se calcula el precio final de matricula.
    CONSTRAINT ck_curso_precio_no_negativo CHECK (precio >= 0),
    -- El descuento es un porcentaje: fuera de 0-100 no tiene significado valido.
    CONSTRAINT ck_curso_descuento_rango CHECK (descuento_porcentaje BETWEEN 0 AND 100),
    -- Validacion explicita del Proceso 2: la fecha de inicio debe ser anterior a la de fin.
    CONSTRAINT ck_curso_fechas CHECK (fecha_fin > fecha_inicio)
);

-- El catalogo publico solo lee cursos publicados; indice parcial porque a esa
-- consulta nunca le importan los no publicados (y son la minoria mientras se arman).
CREATE INDEX idx_curso_publicado ON curso (publicado) WHERE publicado = TRUE;
