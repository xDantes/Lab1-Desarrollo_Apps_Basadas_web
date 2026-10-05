# Documento Técnico — Patrones de Diseño Aplicados

## 1. Patrón State — Ciclo de vida de `Matricula`

### El problema (señal)

El enum `EstadoMatricula` (PENDIENTE, ACTIVA, CANCELADA) define los estados posibles,
pero no define **qué transiciones son válidas desde cada estado**. Cualquier servicio
podía llamar `matricula.setEstado(ACTIVA)` directamente sobre una matrícula
CANCELADA sin recibir ningún error: la regla de que el estado CANCELADA es terminal
y que ACTIVA no puede volver a PENDIENTE solo existía como convención en comentarios,
no como restricción ejecutable.

### Justificación

El patrón **State** encapsula en un objeto la lógica específica de cada estado,
de modo que la `Matricula` misma rechaza cualquier transición ilegal lanzando
`CambioEstadoInvalidoException`, sin importar desde qué servicio o capa se intente;
así la regla de ciclo de vida vive en un único lugar concreto y no puede ser ignorada.

**Nota posterior:** el riesgo descrito arriba (`matricula.setEstado(ACTIVA)` directo)
existía literalmente mientras `Matricula` expusiera ese setter como público — el
patrón State por sí solo no lo impedía, solo ofrecía el camino correcto en paralelo.
Se corrigió cambiando la firma de `MatriculaEstado`: las implementaciones
(`EstadoPendiente`/`EstadoActiva`/`EstadoCancelada`) ya no reciben la `Matricula` y
la mutan, sino que devuelven el siguiente `EstadoMatricula` (o lanzan la excepción);
`Matricula.activar()`/`cancelar()` son las únicas que asignan `this.estado`. Ya no
existe ningún setter público: el patrón State es el único camino posible, no solo
el recomendado.

---

## 2. Patrón Strategy — Validación del método de pago

### El problema (señal)

El sistema soporta tres métodos de pago (TARJETA, SINPE_MOVIL, TRANSFERENCIA)
con reglas de formato distintas para el campo `referencia`:
SINPE exige exactamente 8 dígitos, TRANSFERENCIA exige 6-30 caracteres alfanuméricos,
TARJETA no exige referencia al matricular. Sin un patrón, `MatriculaService` habría
contenido un bloque `switch/if-else` que mezcla las tres reglas y que crece cada vez
que el negocio agrega un nuevo método de pago.

### Justificación

El patrón **Strategy** aisla cada algoritmo de validación en su propia clase,
de modo que `MatriculaService` solo invoca `validador.validar(dto)` sobre la
implementación correspondiente al método elegido sin conocer sus detalles;
agregar un nuevo método de pago es solo añadir una clase e incluirla en el mapa
de configuración, sin modificar el servicio (Principio Abierto/Cerrado).