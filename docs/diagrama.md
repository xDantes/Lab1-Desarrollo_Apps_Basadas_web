# Diagrama de Arquitectura del Sistema

## 1. Imagen del Diagrama (Versionada)

![Diagrama de arquitectura](arquitectura.png)

---

## 2. Diagrama de Arquitectura en Mermaid (Diffeable y Alineado al Estado Actual)

> **Convención de Estados:**
> - 🟩 **Verde / Borde Sólido (Entregado):** Componentes y flujos que ya se encuentran implementados en el código fuente.
> - ⬜ **Gris / Borde Punteado (Pendiente):** Componentes y flujos que forman parte de la visión de destino para iteraciones futuras.

```mermaid
graph TD
    %% Estilos de Nodos
    classDef entregado fill:#d4edda,stroke:#28a745,stroke-width:2px,color:#155724;
    classDef pendiente fill:#f8f9fa,stroke:#6c757d,stroke-width:1px,stroke-dasharray: 5 5,color:#6c757d;
    classDef db fill:#e2e3e5,stroke:#383d41,stroke-width:2px,color:#383d41;

    %% CAPA DE PRESENTACIÓN
    subgraph Presentacion["CAPA DE PRESENTACIÓN"]
        CC["CursoController<br/><i>(Oferta de cursos, Visualización)</i>"]:::entregado
        AC["AuthController <i>[Pendiente]</i><br/><i>(Login, Registro)</i>"]:::pendiente
        MC["MatriculaController <i>[Pendiente]</i><br/><i>(Selección de curso, Checkout)</i>"]:::pendiente
        DC["DiccionarioController <i>[Pendiente]</i><br/><i>(Búsqueda y traducción LESCO)</i>"]:::pendiente
        ADC["AdminController <i>[Pendiente]</i><br/><i>(Publicación de cursos, Gestión cupos)</i>"]:::pendiente
    end

    %% CAPA DE NEGOCIO
    subgraph Negocio["CAPA DE NEGOCIO"]
        CS["CursoService<br/><i>(Validación publicación, Cálculo cupos)</i>"]:::entregado
        AS["AuthService <i>[Pendiente]</i><br/><i>(Autenticación, Roles)</i>"]:::pendiente
        MS["MatriculaService @Transactional <i>[Pendiente]</i><br/><i>(Validación cupo, Descuentos)</i>"]:::pendiente
        PS["PaymentService <i>[Pendiente]</i><br/><i>(Simulación de pago)</i>"]:::pendiente
        RMS["RecursoMultimediaService <i>[Pendiente]</i><br/><i>(Carga y almac. archivos)</i>"]:::pendiente
        DS["DiccionarioService <i>[Pendiente]</i><br/><i>(Búsqueda de señas y categorías)</i>"]:::pendiente
    end

    %% CAPA DE PERSISTENCIA
    subgraph Persistencia["CAPA DE PERSISTENCIA"]
        subgraph JPA["Spring Data JPA (Generalización BaseRepository)"]
            BR["BaseRepository&lt;T, ID&gt;<br/><i>(JpaRepository + JpaSpecificationExecutor)</i>"]:::entregado
            UR["UsuarioRepository"]:::entregado
            IR["InstructorRepository"]:::entregado
            CR["CursoRepository"]:::entregado
            LR["LeccionRepository"]:::entregado
            MR["MatriculaRepository"]:::entregado
            PR["PagoRepository"]:::entregado
            CSPEC["CursoSpecification & MatriculaSpecification<br/><i>(Criteria API Dinámica)</i>"]:::entregado

            UR --> BR
            IR --> BR
            CR --> BR
            LR --> BR
            MR --> BR
            PR --> BR
        end

        subgraph Mongo["MongoDB Repositories (Generalización BaseMongoRepository)"]
            BMR["BaseMongoRepository&lt;T, ID&gt;<br/><i>(MongoRepository)</i>"]:::entregado
            SLR["SenaLescoRepository"]:::entregado
            CMR["ComentarioRepository"]:::entregado
            RMR["RecursoMultimediaRepository"]:::entregado

            SLR --> BMR
            CMR --> BMR
            RMR --> BMR
        end
    end

    %% CAPA DE ALMACENAMIENTO
    subgraph Almacenamiento["CAPA DE ALMACENAMIENTO"]
        BDR[("PostgreSQL 16<br/>(Flyway V1..V8 / ddl-auto=validate)")]:::db
        BDM[("MongoDB 7.0<br/>(Diccionario, Multimedia, Comentarios)")]:::db
    end

    %% RELACIONES ENTREGADAS
    CC --> CS
    CS --> CR
    CS --> MR
    CR --> BDR
    LR --> BDR
    MR --> BDR
    PR --> BDR
    UR --> BDR
    IR --> BDR
    SLR --> BDM
    CMR --> BDM
    RMR --> BDM

    %% RELACIONES PENDIENTES
    AC -.- AS
    MC -.- MS
    MC -.- PS
    DC -.- DS
    ADC -.- CS
    AS -.- UR
    MS -.- MR
    PS -.- PR
    RMS -.- RMR
    DS -.- SLR
```

---