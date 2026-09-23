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