CREATE TABLE pago (
    id BIGSERIAL PRIMARY KEY,
    matricula_id BIGINT NOT NULL REFERENCES matricula (id) ON DELETE CASCADE,
    monto NUMERIC(10,2) NOT NULL,
    metodo VARCHAR(30) NOT NULL,
    -- Numero de comprobante del pago simulado (sin pasarela real, ver alcance
    -- de la propuesta de dominio). Nulo mientras el pago esta PENDIENTE.
    referencia VARCHAR(50),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Ultimo cambio de estado (creado_en != actualizado_en cuando el pago pasa
    -- de PENDIENTE a APROBADO/RECHAZADO). Lo mantiene el trigger de abajo.
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Relacion 1:1 con matricula (una matricula tiene un pago asociado); el
    -- UNIQUE ya sirve como indice para buscar el pago de una matricula.
    CONSTRAINT uq_pago_matricula UNIQUE (matricula_id),
    CONSTRAINT ck_pago_monto_no_negativo CHECK (monto >= 0),
    -- Metodos de pago simulados soportados por el sistema (sin pasarela real).
    CONSTRAINT ck_pago_metodo CHECK (metodo IN ('TARJETA', 'SINPE_MOVIL', 'TRANSFERENCIA')),
    CONSTRAINT ck_pago_estado CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))
);

CREATE OR REPLACE FUNCTION fn_set_actualizado_en() RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pago_actualizado_en
    BEFORE UPDATE ON pago
    FOR EACH ROW EXECUTE FUNCTION fn_set_actualizado_en();
