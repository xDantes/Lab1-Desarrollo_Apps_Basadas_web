INSERT INTO usuario (nombre, identificacion, correo, contrasena_hash, rol) VALUES
    ('Ana Rodríguez Mora',    '1-1111-1111', 'admin@lescocr.cr',              '$2a$10$examplehashplaceholder0000000000000000000000', 'ADMINISTRADOR'),
    ('Carlos Jiménez Vargas', '2-2222-2222', 'carlos.instructor@lescocr.cr',  '$2a$10$examplehashplaceholder1111111111111111111111', 'INSTRUCTOR'),
    ('Marcela Solano Pérez',  '3-3333-3333', 'marcela.instructor@lescocr.cr', '$2a$10$examplehashplaceholder2222222222222222222222', 'INSTRUCTOR');

INSERT INTO curso (codigo, nombre, descripcion, nivel, cupo_total, precio, descuento_porcentaje, fecha_inicio, fecha_fin, publicado) VALUES
    ('LESCO-101', 'LESCO Básico I',
     'Introducción a la Lengua de Señas Costarricense: alfabeto dactilológico, saludos y vocabulario cotidiano.',
     'BASICO', 20, 45000.00, 0, '2026-09-07', '2026-11-13', TRUE),

    ('LESCO-102', 'LESCO Intermedio',
     'Estructura gramatical de LESCO y vocabulario temático ampliado.',
     'INTERMEDIO', 15, 55000.00, 10, '2026-09-07', '2026-12-04', TRUE),

    ('LESCO-201', 'LESCO para el Ámbito Laboral',
     'Vocabulario y frases para atención al público y entornos laborales inclusivos.',
     'AVANZADO', 12, 60000.00, 15, '2026-10-05', '2027-01-11', FALSE);
