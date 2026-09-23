INSERT INTO usuario (nombre, identificacion, correo, contrasena_hash, rol) VALUES
    ('Luis Fernández Rojas', '4-4444-4444', 'luis.estudiante@lescocr.cr',  '$2a$10$examplehashplaceholder3333333333333333333333', 'ESTUDIANTE'),
    ('Paola Méndez Castro',  '5-5555-5555', 'paola.estudiante@lescocr.cr', '$2a$10$examplehashplaceholder4444444444444444444444', 'ESTUDIANTE'),
    ('Diego Chacón Ureña',   '6-6666-6666', 'diego.estudiante@lescocr.cr', '$2a$10$examplehashplaceholder5555555555555555555555', 'ESTUDIANTE');

-- curso_id lo completa el trigger trg_matricula_set_curso_id (V6); no se envía aquí a propósito.
INSERT INTO matricula (consecutivo, usuario_id, leccion_id, estado, precio_final) VALUES
    ('MAT-2026-000001',
     (SELECT id FROM usuario WHERE correo = 'luis.estudiante@lescocr.cr'),
     (SELECT id FROM leccion WHERE curso_id = (SELECT id FROM curso WHERE codigo = 'LESCO-101') AND orden = 1),
     'ACTIVA', 45000.00),

    ('MAT-2026-000002',
     (SELECT id FROM usuario WHERE correo = 'paola.estudiante@lescocr.cr'),
     (SELECT id FROM leccion WHERE curso_id = (SELECT id FROM curso WHERE codigo = 'LESCO-102') AND orden = 1),
     'PENDIENTE', 49500.00),

    -- Matrícula cancelada: demuestra que uq_matricula_usuario_curso_vigente
    -- (V6) libera el cupo para un nuevo intento en el mismo curso.
    ('MAT-2026-000003',
     (SELECT id FROM usuario WHERE correo = 'diego.estudiante@lescocr.cr'),
     (SELECT id FROM leccion WHERE curso_id = (SELECT id FROM curso WHERE codigo = 'LESCO-101') AND orden = 2),
     'CANCELADA', 45000.00);

INSERT INTO pago (matricula_id, monto, metodo, estado) VALUES
    ((SELECT id FROM matricula WHERE consecutivo = 'MAT-2026-000001'), 45000.00, 'SINPE_MOVIL', 'APROBADO'),
    ((SELECT id FROM matricula WHERE consecutivo = 'MAT-2026-000002'), 49500.00, 'TARJETA', 'PENDIENTE'),
    ((SELECT id FROM matricula WHERE consecutivo = 'MAT-2026-000003'), 45000.00, 'TARJETA', 'RECHAZADO');
