# Atención clínica, seguimiento y cierre de casos

Diseño aprobado el 2026-11-12. Fases 2 y 3 del núcleo de negocio, sobre el
[módulo de casos](modulo-casos.md).

> Las **alertas** son avisos basados en las recomendaciones de la OPS. **No son decisiones**:
> la valoración clínica y las decisiones de tratamiento corresponden siempre al médico.

---

## 1. Base: recomendaciones de la OPS

Fuente: OPS, *Actualización Epidemiológica Oropouche en la Región de las Américas*,
13 de agosto de 2025, apartado "Detección y manejo clínico".
<https://www.paho.org/sites/default/files/2025-08/2025-ago-13-actualizacion-epi-oropouchees.pdf>

- El tratamiento es **sintomático**: aliviar dolor y fiebre, hidratar o rehidratar y controlar el
  vómito. No hay vacunas ni antivirales específicos.
- En la forma **neuroinvasiva** (vértigo, letargia, nistagmo, rigidez de nuca; meningitis o
  encefalitis) es necesario el **ingreso en unidades especializadas** con monitoreo constante.
- Diagnóstico diferencial: **dengue** en la primera semana; **meningitis y encefalitis** en la segunda.
- Puede haber manifestaciones hemorrágicas (petequias, epistaxis, sangrado gingival).
- La infección se resuelve en 2-3 semanas; **hasta el 60 % presenta recaídas** después de la recuperación.

Signos de alarma de dengue: OPS, *Algoritmos para el manejo clínico de los casos de dengue*, 2024.
<https://www.paho.org/sites/default/files/2024-05/2024-cde-algoritmos-manejo-clinico-dengue-es_0.pdf>
La OPS indica internar en hospital o unidades de dengue ante uno o más de:

| Código | Signo de alarma |
|---|---|
| `DOLOR_ABDOMINAL_INTENSO` | Dolor abdominal intenso y sostenido, o dolor a la palpación del abdomen |
| `VOMITOS_PERSISTENTES` | Vómitos persistentes |
| `ACUMULACION_LIQUIDOS` | Acumulación de líquidos |
| `SANGRADO_MUCOSAS` | Sangrado de mucosas |
| `LETARGO_IRRITABILIDAD` | Letargo o irritabilidad |
| `HIPOTENSION_POSTURAL` | Hipotensión postural (lipotimia) |
| `HEPATOMEGALIA` | Hepatomegalia > 2 cm |
| `AUMENTO_PROGRESIVO_HEMATOCRITO` | Aumento progresivo del hematocrito |

---

## 2. Registros clínicos del caso

Todos son **inmutables**: no se editan ni se borran; un error se corrige con un registro nuevo.
La **fecha y el usuario los pone el sistema** al registrarlos.

| Registro | Contenido |
|---|---|
| Evaluación clínica | Temperatura, frecuencia cardíaca y respiratoria, tensión arterial, SatO₂; signos clínicos (`POSTRACION`, `LUMBALGIA`, `VOMITOS_PERSISTENTES`, `PETEQUIAS`, `EPISTAXIS`, `SANGRADO_GINGIVAL`, `VERTIGO`, `LETARGIA`, `NISTAGMO`, `RIGIDEZ_NUCA`); signos de alarma de dengue; observaciones |
| Dictamen | Diagnóstico principal, diagnósticos diferenciales, conclusión |
| Plan de atención | Modalidad (`AMBULATORIO`, `INGRESO_HOSPITALARIO`, `INGRESO_UNIDAD_ESPECIALIZADA`), indicaciones, próximo control |
| Seguimiento | Evolución (`MEJORIA`, `ESTABLE`, `EMPEORAMIENTO`, `RECAIDA`), observaciones, próximo control |

Validaciones de signos vitales (rangos de plausibilidad para detectar errores de tecleo, no
umbrales clínicos): temperatura 30-45 °C, FC 20-250, FR 5-80, TA sistólica 40-300 y diastólica
20-200 (la sistólica debe ser mayor), SatO₂ 50-100 %. Una evaluación debe tener al menos un signo
vital, un signo o una observación.

---

## 3. Alertas

Se calculan con la **evaluación más reciente** y el estado actual del caso, solo en casos abiertos.

| Alerta | Cuándo |
|---|---|
| `NEUROINVASIVA` | Vértigo, letargia, nistagmo o rigidez de nuca, o manifestación neurológica en el caso |
| `SIGNOS_ALARMA_DENGUE` | Algún signo de alarma de dengue |
| `HEMORRAGIA` | Petequias, epistaxis o sangrado gingival |
| `GESTANTE` | Caso en embarazada (seguimiento y vigilancia de posible transmisión vertical) |
| `CONTROL_VENCIDO` | El próximo control programado ya pasó |

El **próximo control** es el que indica el plan o seguimiento **más reciente**. Si el registro
más reciente no programa control, no hay control pendiente.

---

## 4. Cierre y reapertura

- Se cierra con un **desenlace del catálogo** (activo), fecha (por defecto hoy; no futura ni
  anterior a la notificación) y observaciones. No hace falta laboratorio: la OPS recomienda
  analizar solo un 10-30 % de los casos.
- Un caso cerrado no admite registros nuevos.
- Se puede **reabrir con motivo** (por ejemplo, recaída tras el alta), si el paciente no tiene
  otro caso abierto.
- Cierres y reaperturas quedan en los **eventos** del caso, con el cierre anterior incluido.

### Catálogo de desenlaces (editable)

Valores iniciales (se cargan al arrancar si el catálogo está vacío): `RECUPERADO`,
`RECUPERADO_CON_SECUELAS`, `FALLECIDO`, `TRASLADADO`, `PERDIDO_SEGUIMIENTO`.

Se pueden **añadir** desenlaces nuevos, **renombrarlos** y **activarlos o desactivarlos**. No se
borran ni se cambia su código, porque los casos cerrados los referencian. El código se normaliza
a mayúsculas con guiones bajos (`alta voluntaria` → `ALTA_VOLUNTARIA`).

---

## 5. API

Todas las rutas requieren JWT. Las respuestas de casos incluyen los registros, el próximo
control, los eventos y las **alertas**.

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/v1/casos/{id}/evaluaciones` | Evaluación clínica |
| `POST` | `/api/v1/casos/{id}/dictamenes` | Dictamen médico |
| `POST` | `/api/v1/casos/{id}/planes` | Plan de atención |
| `POST` | `/api/v1/casos/{id}/seguimientos` | Seguimiento (`RECAIDA` marca el caso como recaída) |
| `PATCH` | `/api/v1/casos/{id}/cierre` | Cerrar: `{desenlace, fechaCierre, observaciones}` |
| `PATCH` | `/api/v1/casos/{id}/reapertura` | Reabrir: `{motivo}` |
| `GET` | `/api/v1/casos/controles-pendientes?areaSaludId=&hasta=` | Agenda de controles (incluye vencidos); `hasta` por defecto hoy |
| `GET` | `/api/v1/desenlaces?soloActivos=` | Catálogo de desenlaces |
| `POST` | `/api/v1/desenlaces` | Añadir desenlace `{codigo, nombre}` |
| `PUT` | `/api/v1/desenlaces/{id}` | Renombrar `{nombre}` |
| `PATCH` | `/api/v1/desenlaces/{id}/activar` · `/desactivar` | Activar o desactivar |

La gestión del catálogo está reservada al ADMIN y los registros clínicos al MEDICO de cada área (ver [roles-y-acceso.md](roles-y-acceso.md)).

---

## 6. Arquitectura

```
domain/models/caso/      EvaluacionClinica, Dictamen, PlanAtencion, Seguimiento, EventoCaso,
                         Desenlace, AlertasClinicas, Alerta, ControlPendiente y enums
domain/ports/in/         AtencionClinicaUseCase, DesenlaceUseCase
domain/ports/out/        DesenlaceRepositoryPort
application/services/    AtencionClinicaService, DesenlaceService (Java puro), CasoSoporte
infrastructure/          Entidades en cascada desde CasoEntity, DesenlaceEntity y su adaptador,
                         AtencionClinicaController, DesenlaceController, carga inicial del catálogo
```

Todo vive dentro del agregado `Caso` y se guarda en una sola transacción.
