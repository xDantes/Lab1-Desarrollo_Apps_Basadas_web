const dbName = (typeof process !== 'undefined' && process.env.MONGO_INITDB_DATABASE) || 'lescocr';
db = db.getSiblingDB(dbName);

db.sena_lesco.insertMany([
    {
        palabra: 'Hola',
        palabraNormalizada: 'hola',
        descripcion: 'Saludo informal de uso cotidiano.',
        categoria: { codigo: 'SALUDOS', nombre: 'Saludos' },
        multimedia: [
            { tipo: 'IMAGEN', url: 'https://cdn.lescocr.cr/senas/hola.png', esPrincipal: true },
            { tipo: 'VIDEO', url: 'https://cdn.lescocr.cr/senas/hola.mp4', esPrincipal: false }
        ],
        tags: ['saludo', 'cortesia', 'basico'],
        activo: true
    },
    {
        palabra: 'Buenos días',
        palabraNormalizada: 'buenos dias',
        descripcion: 'Saludo formal usado durante la manana.',
        categoria: { codigo: 'SALUDOS', nombre: 'Saludos' },
        multimedia: [
            { tipo: 'VIDEO', url: 'https://cdn.lescocr.cr/senas/buenos-dias.mp4', esPrincipal: true }
        ],
        tags: ['saludo', 'formal'],
        activo: true
    },
    {
        palabra: 'Adiós',
        palabraNormalizada: 'adios',
        descripcion: 'Despedida de uso general.',
        categoria: { codigo: 'SALUDOS', nombre: 'Saludos' },
        multimedia: [
            { tipo: 'IMAGEN', url: 'https://cdn.lescocr.cr/senas/adios.png', esPrincipal: true }
        ],
        tags: ['despedida'],
        activo: true
    },
    {
        palabra: 'Gracias',
        palabraNormalizada: 'gracias',
        descripcion: 'Expresion de agradecimiento.',
        categoria: { codigo: 'CORTESIA', nombre: 'Cortesía' },
        multimedia: [
            { tipo: 'IMAGEN', url: 'https://cdn.lescocr.cr/senas/gracias.png', esPrincipal: true },
            { tipo: 'VIDEO', url: 'https://cdn.lescocr.cr/senas/gracias.mp4', esPrincipal: false }
        ],
        tags: ['cortesia', 'basico'],
        activo: true
    },
    {
        palabra: 'Por favor',
        palabraNormalizada: 'por favor',
        descripcion: 'Expresion usada para pedir algo con cortesia.',
        categoria: { codigo: 'CORTESIA', nombre: 'Cortesía' },
        multimedia: [
            { tipo: 'VIDEO', url: 'https://cdn.lescocr.cr/senas/por-favor.mp4', esPrincipal: true }
        ],
        tags: ['cortesia'],
        activo: true
    },
    {
        palabra: 'Familia',
        palabraNormalizada: 'familia',
        descripcion: 'Concepto general de familia.',
        categoria: { codigo: 'FAMILIA', nombre: 'Familia' },
        multimedia: [
            { tipo: 'IMAGEN', url: 'https://cdn.lescocr.cr/senas/familia.png', esPrincipal: true }
        ],
        tags: ['familia'],
        activo: true
    },
    {
        palabra: 'Mamá',
        palabraNormalizada: 'mama',
        descripcion: 'Miembro de la familia: madre.',
        categoria: { codigo: 'FAMILIA', nombre: 'Familia' },
        multimedia: [
            { tipo: 'IMAGEN', url: 'https://cdn.lescocr.cr/senas/mama.png', esPrincipal: true },
            { tipo: 'VIDEO', url: 'https://cdn.lescocr.cr/senas/mama.mp4', esPrincipal: false }
        ],
        tags: ['familia'],
        activo: true
    },
    {
        palabra: 'Uno',
        palabraNormalizada: 'uno',
        descripcion: 'Numero cardinal 1.',
        categoria: { codigo: 'NUMEROS', nombre: 'Números' },
        multimedia: [
            { tipo: 'IMAGEN', url: 'https://cdn.lescocr.cr/senas/uno.png', esPrincipal: true }
        ],
        tags: ['numeros', 'basico'],
        activo: true
    },
    {
        palabra: 'Dos',
        palabraNormalizada: 'dos',
        descripcion: 'Numero cardinal 2.',
        categoria: { codigo: 'NUMEROS', nombre: 'Números' },
        multimedia: [
            { tipo: 'IMAGEN', url: 'https://cdn.lescocr.cr/senas/dos.png', esPrincipal: true }
        ],
        tags: ['numeros', 'basico'],
        activo: true
    }
]);
