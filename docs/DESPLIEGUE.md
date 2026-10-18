# Guía de Despliegue en Servicios Gestionados de la Nube (HTTPS)

Esta guía detalla los pasos para publicar la solución completa (**API Spring Boot**, **SPA React**, **PostgreSQL** y **MongoDB**) en servicios gestionados gratuitos con **HTTPS**, cumpliendo con la separación de configuración del código y la **gestión segura de secretos** (sin exponer ninguna credencial en el repositorio de Git).

---

## 1. Arquitectura de Despliegue en la Nube

```
                           ┌──────────────────────────────┐
                           │   Cliente / Navegador Web    │
                           └──────────────┬───────────────┘
                                          │ HTTPS
                                          ▼
                      ┌───────────────────────────────────────┐
                      │    SPA en React (Vite)                │
                      │    Host: Render Static Site / Vercel  │
                      │    URL: https://lesco-spa.onrender.com│
                      └───────────────────┬───────────────────┘
                                          │ HTTPS (Bearer JWT / CORS)
                                          ▼
                      ┌───────────────────────────────────────┐
                      │    API REST Spring Boot 3             │
                      │    Host: Render Web Service (Docker)  │
                      │    URL: https://lesco-api.onrender.com│
                      │    Perfil activo: 'prod'              │
                      └───────────┬───────────────┬───────────┘
                                  │               │
                 TLS / SSL        │               │ TLS / SRV
                                  ▼               ▼
        ┌───────────────────────────────┐   ┌───────────────────────────────┐
        │  PostgreSQL Gestionado        │   │  MongoDB Atlas (M0 Free)      │
        │  Host: Neon.tech o Render DB  │   │  Host: MongoDB Cloud          │
        │  Tablas: usuario, curso, etc. │   │  Colección: recursos, etc.    │
        └───────────────────────────────┘   └───────────────────────────────┘
```

---

## 2. Gestión Segura de Secretos (Cero Secretos en Git)

### Política de Seguridad Aplicada:
1. **`src/main/resources/application-prod.properties`**: Define las variables requeridas (`${SPRING_DATASOURCE_URL}`, `${JWT_SECRET}`, etc.) **sin valores de respaldo (fallbacks)**. Si alguna variable no se define en el panel de la nube, la aplicación se rehúsa a iniciar inmediatamente (*fail-fast*), previniendo arranques con claves inseguras.
2. **`.gitignore`**: Ignora explícitamente todos los archivos `.env`, `.env.*.local`, `spa/.env*` para que nunca se suban por error al control de versiones.
3. **Secretos en la Nube**: Las contraseñas y claves se configuran **únicamente en los paneles de administración de los servicios** (Environment Variables de Render o Railway).

### Generación de Claves Criptográficas:
Para la clave JWT en producción (mínimo 256 bits / 32 bytes), ejecuta en tu terminal:
```bash
openssl rand -base64 32
```
*Guarda este valor para configurarlo como `JWT_SECRET` en Render.*

---

## 3. Paso a Paso: Bases de Datos Gestionadas

### A. PostgreSQL Gestionado (Neon.tech o Render PostgreSQL)
1. **Registrarse en [Neon.tech](https://neon.tech/)** o en **[Render.com](https://render.com/)**.
2. Crear un nuevo proyecto o base de datos PostgreSQL:
   - **Nombre de base de datos**: `lescocr`
   - **Región**: `US East (Ohio / N. Virginia)` (la misma donde desplegarás el backend para menor latencia).
3. Obtener los datos de conexión con **SSL requerido**:
   - **Host**: `ep-xyz.us-east-2.aws.neon.tech`
   - **Puerto**: `5432`
   - **Base de datos**: `lescocr`
   - **Usuario**: `neondb_owner` (o tu usuario)
   - **Contraseña**: `<tu_password_segura>`
4. La URL JDBC resultante tendrá este formato:
   ```
   jdbc:postgresql://<host>:5432/lescocr?sslmode=require
   ```

### B. MongoDB Gestionado (MongoDB Atlas)
1. Iniciar sesión en **[MongoDB Atlas](https://www.mongodb.com/cloud/atlas)**.
2. Crear un clúster gratuito **M0 Free** (Shared).
3. En **Security > Database Access**:
   - Crear un usuario (ej. `lesco_admin`) con una contraseña segura. Asignar rol *Read and write to any database*.
4. En **Security > Network Access**:
   - Añadir una regla de acceso IP: `0.0.0.0/0` (*Allow Access from Anywhere*) para permitir conexiones desde la IP dinámica del backend en Render.
5. En **Deployments > Database > Connect > Drivers (Java)**:
   - Copiar la cadena de conexión URI. Tendrá el formato:
   ```
   mongodb+srv://lesco_admin:<password>@cluster0.xxxxx.mongodb.net/lescocr?retryWrites=true&w=majority
   ```

---

## 4. Despliegue de la API Backend (Spring Boot) en Render

1. En [Render Dashboard](https://dashboard.render.com/), haz clic en **New + > Web Service**.
2. Conecta tu repositorio de GitHub / GitLab.
3. Configuración del servicio:
   - **Name**: `lescocr-api`
   - **Region**: Igual a la base de datos (ej. *Oregon* u *Ohio*).
   - **Branch**: `main` (o tu rama de entrega).
   - **Runtime**: **Docker** (Render detectará automáticamente el archivo `Dockerfile` multi-stage en la raíz).
   - **Instance Type**: Free.
4. **Environment Variables** (Configurar en la pestaña *Environment*):

| Variable | Valor de Ejemplo / Descripción |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host>:5432/lescocr?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | `<usuario_postgres>` |
| `SPRING_DATASOURCE_PASSWORD` | `<password_postgres>` |
| `SPRING_DATA_MONGODB_URI` | `mongodb+srv://<usuario>:<pass>@cluster0.../lescocr` |
| `JWT_SECRET` | *(Clave de 32+ bytes generada con openssl)* |
| `JWT_EXPIRATION_MS` | `86400000` (24 horas) |
| `CORS_ALLOWED_ORIGINS` | `https://lescocr-spa.onrender.com,http://localhost:5173` |

5. Haz clic en **Create Web Service**.
   - Render compilará el JAR con Docker, aplicará las migraciones de Flyway y levantará la API en una URL pública con HTTPS (ej. `https://lescocr-api.onrender.com`).
   - Verifica Swagger UI accediendo a: `https://lescocr-api.onrender.com/swagger-ui.html`.

---

## 5. Despliegue de la SPA en React en Render (o Vercel)

### Opción A: Render Static Site (Recomendada)
1. En Render Dashboard, haz clic en **New + > Static Site**.
2. Selecciona el mismo repositorio.
3. Configuración:
   - **Name**: `lescocr-spa`
   - **Branch**: `main`
   - **Root Directory**: `spa`
   - **Build Command**: `npm run build`
   - **Publish Directory**: `dist`
4. **Environment Variables**:
   - `VITE_API_URL`: `https://lescocr-api.onrender.com` (la URL pública HTTPS de tu API en Render).
5. **Redirects / Rewrites** (Muy importante para Single Page Apps):
   - En la pestaña **Redirects/Rewrites**:
     - **Source**: `/*`
     - **Destination**: `/index.html`
     - **Action**: `Rewrite`
6. Haz clic en **Create Static Site**.
   - Render generará tu URL pública HTTPS (ej. `https://lescocr-spa.onrender.com`).

### Opción B: Vercel
1. Instala el CLI o importa el repositorio en [Vercel](https://vercel.com).
2. Especifica **Root Directory**: `spa`.
3. Añade la variable de entorno `VITE_API_URL=https://lescocr-api.onrender.com`.
4. Despliega con un clic.

---

## 6. Verificación Final de Entrega

Una vez completado el despliegue, realiza la siguiente lista de verificación:

- [ ] **Acceso HTTPS**: Ambos dominios cargan con certificado SSL/TLS válido (candado verde en el navegador).
- [ ] **Documentación OpenAPI**: `https://<api-url>/swagger-ui.html` carga y lista los endpoints de Cursos, Matrículas y Autenticación.
- [ ] **Login JWT**: Iniciar sesión en la SPA con credenciales de prueba. El token JWT se almacena de forma segura y se incluye en las cabeceras `Authorization: Bearer <token>`.
- [ ] **Listado Paginado**:
  - Estado de **Carga**: Se visualiza el spinner animado mientras se consultan los datos.
  - Estado de **Datos**: Se muestran las matrículas en tabla con su número consecutivo, curso, precio en ₡ y badge de estado.
  - Controles de paginación (Anterior, Siguiente, selector de tamaño) y filtro por estado operativos.
- [ ] **Proceso Principal (Creación de Matrícula)**:
  - Clic en "+ Nueva Matrícula".
  - Selección de curso y lección cargados dinámicamente desde la API.
  - Selección de método de pago (con validación de 8 dígitos para SINPE Móvil).
  - Creación exitosa (código 201) y actualización inmediata de la tabla.
- [ ] **Cancelación de Matrícula**: Clic en botón "Cancelar" para cancelar una matrícula activa o pendiente.
- [ ] **Cero Secretos en Git**: Ejecutar `git status` y verificar que ningún archivo `.env` o credencial privada fue versionada.
