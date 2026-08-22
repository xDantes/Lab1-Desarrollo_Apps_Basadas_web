const dbName = (typeof process !== 'undefined' && process.env.MONGO_INITDB_DATABASE) || 'lescocr';
db = db.getSiblingDB(dbName);

db.recursos_multimedia.insertMany([
    {
        leccionId: NumberLong(1),
        cursoId: NumberLong(1),
        tipo: 'IMAGEN',
        url: 'https://cdn.lescocr.cr/lecciones/leccion-1/alfabeto-completo.png',
        orden: NumberLong(1),
        metadata: { anchoPx: 1200, altoPx: 800, tamanoBytes: 245000 }
    },
    {
        leccionId: NumberLong(1),
        cursoId: NumberLong(1),
        tipo: 'VIDEO',
        url: 'https://cdn.lescocr.cr/lecciones/leccion-1/alfabeto-demo.mp4',
        orden: NumberLong(2),
        metadata: { duracionSegundos: 180, tamanoBytes: 15400000 }
    },
    {
        leccionId: NumberLong(2),
        cursoId: NumberLong(1),
        tipo: 'VIDEO',
        url: 'https://cdn.lescocr.cr/lecciones/leccion-2/saludos-presentaciones.mp4',
        orden: NumberLong(1),
        metadata: { duracionSegundos: 240, tamanoBytes: 20100000 }
    },
    {
        leccionId: NumberLong(3),
        cursoId: NumberLong(2),
        tipo: 'IMAGEN',
        url: 'https://cdn.lescocr.cr/lecciones/leccion-3/estructura-oraciones.png',
        orden: NumberLong(1),
        metadata: { anchoPx: 1400, altoPx: 900, tamanoBytes: 310000 }
    },
    {
        leccionId: NumberLong(3),
        cursoId: NumberLong(2),
        tipo: 'VIDEO',
        url: 'https://cdn.lescocr.cr/lecciones/leccion-3/estructura-oraciones-demo.mp4',
        orden: NumberLong(2),
        metadata: { duracionSegundos: 300, tamanoBytes: 24500000 }
    }
]);
