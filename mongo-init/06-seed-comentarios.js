const dbName = (typeof process !== 'undefined' && process.env.MONGO_INITDB_DATABASE) || 'lescocr';
db = db.getSiblingDB(dbName);

db.comentarios.insertMany([
    {
        cursoId: NumberLong(1),
        usuarioId: NumberLong(4),
        autorNombre: 'Luis Fernández Rojas',
        texto: 'Excelente curso para empezar, las explicaciones del alfabeto son muy claras.',
        calificacion: NumberLong(5),
        creadoEn: new Date('2026-09-10T15:20:00Z'),
        editado: false,
        respuestas: [
            {
                autorId: NumberLong(2),
                autorNombre: 'Carlos Jiménez Vargas',
                texto: '¡Gracias Luis! Me alegra que te esté sirviendo.',
                creadoEn: new Date('2026-09-10T18:05:00Z')
            }
        ]
    },
    {
        cursoId: NumberLong(1),
        usuarioId: NumberLong(6),
        autorNombre: 'Diego Chacón Ureña',
        texto: 'Tuve que cancelar por tiempo, pero el contenido se ve muy bien organizado.',
        calificacion: NumberLong(4),
        creadoEn: new Date('2026-09-12T09:40:00Z'),
        editado: false,
        respuestas: []
    },
    {
        cursoId: NumberLong(2),
        usuarioId: NumberLong(5),
        autorNombre: 'Paola Méndez Castro',
        texto: '¿La lección de estructura gramatical tiene subtítulos en español?',
        creadoEn: new Date('2026-09-14T11:00:00Z'),
        editado: false,
        respuestas: [
            {
                autorId: NumberLong(3),
                autorNombre: 'Marcela Solano Pérez',
                texto: 'Sí, todos los videos incluyen subtítulos.',
                creadoEn: new Date('2026-09-14T13:30:00Z')
            }
        ]
    }
]);
