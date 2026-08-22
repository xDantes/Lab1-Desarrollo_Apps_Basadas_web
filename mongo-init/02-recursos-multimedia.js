const dbName = (typeof process !== 'undefined' && process.env.MONGO_INITDB_DATABASE) || 'lescocr';
db = db.getSiblingDB(dbName);

db.createCollection('recursos_multimedia', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['leccionId', 'cursoId', 'tipo', 'url', 'orden'],
            properties: {
                leccionId: {
                    bsonType: ['long', 'int'],
                    description: 'FK logica a leccion.id (PostgreSQL)'
                },
                cursoId: {
                    bsonType: ['long', 'int'],
                    description: 'FK logica a curso.id (PostgreSQL), denormalizada desde leccion.curso_id'
                },
                tipo: { enum: ['VIDEO', 'IMAGEN'] },
                url: { bsonType: 'string' },
                orden: {
                    bsonType: ['long', 'int'],
                    minimum: 1
                },
                metadata: {
                    bsonType: 'object',
                    description: 'Campos variables segun tipo (duracionSegundos para VIDEO; anchoPx/altoPx para IMAGEN, etc.)'
                }
            }
        }
    },
    validationLevel: 'moderate'
});

db.recursos_multimedia.createIndex({ leccionId: 1, orden: 1 }, { name: 'idx_leccion_orden' });
db.recursos_multimedia.createIndex({ cursoId: 1 }, { name: 'idx_curso' });
