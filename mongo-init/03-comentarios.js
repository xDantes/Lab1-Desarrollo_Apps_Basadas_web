const dbName = (typeof process !== 'undefined' && process.env.MONGO_INITDB_DATABASE) || 'lescocr';
db = db.getSiblingDB(dbName);

db.createCollection('comentarios', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['cursoId', 'usuarioId', 'autorNombre', 'texto', 'creadoEn'],
            properties: {
                cursoId: {
                    bsonType: ['long', 'int'],
                    description: 'FK logica a curso.id (PostgreSQL)'
                },
                usuarioId: {
                    bsonType: ['long', 'int'],
                    description: 'FK logica a usuario.id (PostgreSQL)'
                },
                autorNombre: {
                    bsonType: 'string',
                    description: 'Snapshot del nombre del usuario al momento de comentar'
                },
                texto: { bsonType: 'string' },
                calificacion: {
                    bsonType: ['long', 'int'],
                    minimum: 1,
                    maximum: 5,
                    description: 'Calificacion opcional de 1 a 5 estrellas'
                },
                creadoEn: { bsonType: 'date' },
                editado: { bsonType: 'bool' },
                respuestas: {
                    bsonType: 'array',
                    description: 'Respuestas al comentario, embebidas (1-a-pocos, acotado)',
                    items: {
                        bsonType: 'object',
                        required: ['autorId', 'autorNombre', 'texto', 'creadoEn'],
                        properties: {
                            autorId: { bsonType: ['long', 'int'] },
                            autorNombre: { bsonType: 'string' },
                            texto: { bsonType: 'string' },
                            creadoEn: { bsonType: 'date' }
                        }
                    }
                }
            }
        }
    },
    validationLevel: 'moderate'
});

db.comentarios.createIndex({ cursoId: 1, creadoEn: -1 }, { name: 'idx_curso_fecha' });
db.comentarios.createIndex({ usuarioId: 1 }, { name: 'idx_usuario' });
