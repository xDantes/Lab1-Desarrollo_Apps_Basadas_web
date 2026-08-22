INSERT INTO leccion (titulo, orden, curso_id, instructor_id) VALUES
    ('Alfabeto dactilológico', 1,
     (SELECT id FROM curso WHERE codigo = 'LESCO-101'),
     (SELECT id FROM usuario WHERE correo = 'carlos.instructor@lescocr.cr')),

    ('Saludos y presentaciones', 2,
     (SELECT id FROM curso WHERE codigo = 'LESCO-101'),
     (SELECT id FROM usuario WHERE correo = 'carlos.instructor@lescocr.cr')),

    ('Estructura gramatical básica', 1,
     (SELECT id FROM curso WHERE codigo = 'LESCO-102'),
     (SELECT id FROM usuario WHERE correo = 'marcela.instructor@lescocr.cr'));
