const dbName = (typeof process !== 'undefined' && process.env.MONGO_INITDB_DATABASE) || 'lescocr';
db = db.getSiblingDB(dbName);

db.createCollection('sena_lesco', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['palabra', 'palabraNormalizada', 'categoria', 'multimedia', 'activo'],
            properties: {
                palabra: {
                    bsonType: 'string',
                    description: 'Palabra o frase que representa la sena, tal como se muestra al usuario'
                },
                palabraNormalizada: {
                    bsonType: 'string',
                    description: 'palabra en minusculas y sin tildes, usada para busqueda y como clave unica'
                },
                descripcion: {
                    bsonType: 'string',
                    description: 'Explicacion de uso/contexto de la sena'
                },
                categoria: {
                    bsonType: 'object',
                    required: ['codigo', 'nombre'],
                    description: 'Clasificacion de la sena, embebida (ver justificacion arriba)',
                    properties: {
                        codigo: { bsonType: 'string' },
                        nombre: { bsonType: 'string' }
                    }
                },
                multimedia: {
                    bsonType: 'array',
                    minItems: 1,
                    description: 'Imagenes/videos de referencia de la sena, embebidos',
                    items: {
                        bsonType: 'object',
                        required: ['tipo', 'url'],
                        properties: {
                            tipo: { enum: ['IMAGEN', 'VIDEO'] },
                            url: { bsonType: 'string' },
                            esPrincipal: { bsonType: 'bool' }
                        }
                    }
                },
                tags: {
                    bsonType: 'array',
                    items: { bsonType: 'string' }
                },
                activo: { bsonType: 'bool' }
            }
        }
    },
    validationLevel: 'moderate'
});

db.sena_lesco.createIndex({ palabraNormalizada: 1 }, { unique: true, name: 'uq_palabra_normalizada' });
db.sena_lesco.createIndex({ 'categoria.codigo': 1 }, { name: 'idx_categoria_codigo' });
db.sena_lesco.createIndex(
    { palabra: 'text', descripcion: 'text', tags: 'text' },
    { name: 'ix_texto_busqueda' }
);
