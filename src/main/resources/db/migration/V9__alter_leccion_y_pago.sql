-- =============================================================================
-- V9: Cambios estructurales movidos aqui desde V4 y V7 (que habian sido
--     modificados despues de aplicarse, lo cual invalida su checksum).
--     Regla de oro de Flyway: una migracion aplicada nunca se edita;
--     los cambios van siempre en una nueva version.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. leccion: redirigir instructor_id a instructor(usuario_id)
--    V4 original apunto a usuario(id). Ahora queremos que apunte al subtipo
--    instructor(usuario_id) para que la base de datos garantice, por FK,
--    que solo un usuario con rol INSTRUCTOR puede asignarse a una leccion.
-- ---------------------------------------------------------------------------
ALTER TABLE leccion
    DROP CONSTRAINT leccion_instructor_id_fkey,
    ADD CONSTRAINT leccion_instructor_id_fkey
        FOREIGN KEY (instructor_id) REFERENCES instructor (usuario_id);

-- ---------------------------------------------------------------------------
-- 2. pago: columnas nuevas, constraint de metodo, trigger de auditoria
--    V7 original no tenia referencia ni actualizado_en.
-- ---------------------------------------------------------------------------
ALTER TABLE pago
    ADD COLUMN referencia     VARCHAR(50),
    ADD COLUMN actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now();

-- Metodos de pago simulados soportados por el sistema (sin pasarela real).
ALTER TABLE pago
    ADD CONSTRAINT ck_pago_metodo
        CHECK (metodo IN ('TARJETA', 'SINPE_MOVIL', 'TRANSFERENCIA'));

-- Funcion y trigger: mantienen actualizado_en sincronizado con cada UPDATE.
CREATE OR REPLACE FUNCTION fn_set_actualizado_en() RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pago_actualizado_en
    BEFORE UPDATE ON pago
    FOR EACH ROW EXECUTE FUNCTION fn_set_actualizado_en();

-- ---------------------------------------------------------------------------
-- 3. Rellenar referencia en los registros semilla ya insertados por V8
--    (MAT-2026-000002 queda en NULL: pago PENDIENTE, aun sin referencia).
-- ---------------------------------------------------------------------------
UPDATE pago
   SET referencia = 'SIM-000001'
 WHERE matricula_id = (SELECT id FROM matricula WHERE consecutivo = 'MAT-2026-000001');

UPDATE pago
   SET referencia = 'SIM-000003'
 WHERE matricula_id = (SELECT id FROM matricula WHERE consecutivo = 'MAT-2026-000003');
