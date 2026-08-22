CREATE TABLE pago (
    id BIGSERIAL PRIMARY KEY,
    matricula_id BIGINT NOT NULL REFERENCES matricula (id) ON DELETE CASCADE,
    monto NUMERIC(10,2) NOT NULL,
    metodo VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Relacion 1:1 con matricula (una matricula tiene un pago asociado); el
    -- UNIQUE ya sirve como indice para buscar el pago de una matricula.
    CONSTRAINT uq_pago_matricula UNIQUE (matricula_id),
    CONSTRAINT ck_pago_monto_no_negativo CHECK (monto >= 0),
    CONSTRAINT ck_pago_estado CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))
);
