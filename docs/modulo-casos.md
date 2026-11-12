# Módulo de casos de Oropouche

Diseño aprobado el 2026-11-12. Fase 1 del núcleo de negocio: registro de casos,
resultados de laboratorio y clasificación según las definiciones de la OPS.

> El sistema **sugiere** la clasificación de cada caso a partir de los criterios de la OPS
> y muestra qué criterios se cumplen. **La clasificación la decide siempre el profesional
> sanitario**: Oropouche es una herramienta de apoyo y no sustituye el criterio clínico.

---

## 1. Definiciones de caso (OPS)

Fuente: Organización Panamericana de la Salud, *Actualización Epidemiológica Oropouche en la
Región de las Américas*, 13 de agosto de 2025, págs. 7-8.
<https://www.paho.org/sites/default/files/2025-08/2025-ago-13-actualizacion-epi-oropouchees.pdf>

### Caso sospechoso
Persona con fiebre de inicio agudo (o historia de fiebre) de **hasta 5 días** de evolución,
asociada a **cefalea intensa**, y **dos o más** de las siguientes manifestaciones:

| Código en el sistema | Manifestación |
|---|---|
| `MIALGIA_ARTRALGIA` | Mialgia o artralgia |
| `ESCALOFRIOS` | Escalofríos |
| `FOTOFOBIA` | Fotofobia |
| `MAREOS` | Mareos |
| `DOLOR_RETROOCULAR` | Dolor retroocular |
| `GASTROINTESTINAL` | Náusea, vómitos o diarrea (3 o más episodios en 24 h con cambio de consistencia) |
| `NEUROLOGICA` | Cualquier manifestación del sistema nervioso (diplopía, parestesia, meningitis, encefalitis, meningoencefalitis) |

Recaída: los casos pueden presentar recaída de síntomas de 1 a 10 días después de la mejoría;
si no se sospechó Oropouche inicialmente, se consideran caso sospechoso.

### Caso sospechoso en embarazada
Toda embarazada con fiebre de inicio agudo (o historia de fiebre de hasta 5 días) asociada a
cefalea intensa. No exige manifestaciones adicionales.

### Caso probable
Caso sospechoso que además cumple al menos uno de:
- Vive o ha viajado a un área de transmisión confirmada de Oropouche.
- Tiene nexo epidemiológico con un caso confirmado.
- Tiene una prueba ELISA IgM positiva para Oropouche.

### Caso confirmado
Caso sospechoso que además cumple al menos uno de:
- Resultado positivo de detección de OROV: ARN viral (RT-PCR) o antígenos virales.
- Seroconversión o aumento de al menos 4 veces del título de anticuerpos en muestras pareadas.
- Determinación post mortem de ARN viral (RT-PCR) o antígenos (inmunohistoquímica u otras).
- En sospecha de encefalitis u otra afección neurológica: IgM positiva en líquido cefalorraquídeo.

### Diagnóstico diferencial
La OPS recomienda el diagnóstico diferencial con dengue y realizar pruebas de Oropouche en los
casos negativos para dengue. Por eso los resultados de laboratorio registran el agente
(`OROPOUCHE` o `DENGUE`).

---

## 2. Cómo aplica el sistema las definiciones (`ClasificadorOPS`)

Regla de dominio en Java puro, sin dependencias de framework. Evalúa un caso y devuelve la
clasificación sugerida y la lista de criterios cumplidos.

1. **Fiebre de hasta 5 días:** `fiebre = true` y como máximo 5 días entre el inicio de síntomas
   y la notificación. Si el caso es una **recaída**, no se aplica el límite de 5 días.
2. **Sospechoso:** fiebre válida + cefalea intensa + (embarazada **o** 2 o más manifestaciones).
3. **Confirmado:** sospechoso + alguna prueba de **Oropouche positiva** de tipo `RT_PCR`,
   `ANTIGENO`, `SEROCONVERSION_PAREADA` o `POST_MORTEM`; o `IGM_LCR` positiva si el caso tiene
   manifestación `NEUROLOGICA`.
4. **Probable:** sospechoso + (área de transmisión o viaje **o** nexo con confirmado **o**
   `ELISA_IGM` de Oropouche positiva).
5. Si no cumple la definición de sospechoso, no hay clasificación sugerida.

Interpretaciones que deben validar profesionales sanitarios:
- Que la recaída exime del límite de 5 días pero sigue exigiendo el cuadro clínico.
- Que los 5 días se cuentan hasta la fecha de notificación del caso.

El clasificador está detrás de una interfaz (`DefinicionCaso`) para poder añadir más adelante
una definición nacional (por ejemplo, la del MINSAP) sin rehacer el módulo.

---

## 3. Decisiones aprobadas

| # | Decisión |
|---|---|
| 1 | `CONFIRMADO` solo se puede asignar si el caso tiene una prueba confirmatoria positiva, es decir, si la sugerencia de la OPS es `CONFIRMADO`. |
| 2 | Cualquier cambio de clasificación exige un **motivo**. `DESCARTADO` (no definido por la OPS) se asigna con motivo, por ejemplo "RT-PCR de Oropouche negativa y dengue confirmado". |
| 3 | Las demás clasificaciones pueden diferir de la sugerencia, siempre con motivo. |
| 4 | Se usa la definición de la OPS; el diseño permite añadir una definición nacional. |
| 5 | Se registra el embarazo y las semanas de gestación. Los casos de transmisión vertical (feto, recién nacido) quedan para una fase posterior, según las *Directrices para la detección y vigilancia de Oropouche en posibles casos de infección vertical* de la OPS. |

---

## 4. Reglas de negocio

- Un caso se abre para un **paciente existente y activo**, en su área de salud.
- La fecha de inicio de síntomas no puede ser posterior a la de notificación; ninguna puede ser futura.
- Un paciente **no puede tener dos casos abiertos**. Una recaída se registra en el mismo caso.
- Al registrar un caso, su clasificación inicial es la sugerida. Si no cumple la definición de
  sospechoso, hay que indicar un motivo para registrarlo igualmente (queda como `SOSPECHOSO`).
- Añadir un resultado de laboratorio o actualizar los datos **recalcula la sugerencia**, pero
  **no cambia la clasificación**: el cambio lo hace el profesional.
- Cada cambio de clasificación queda en el **historial** con fecha, clasificación anterior,
  nueva, motivo y usuario.
- Un caso `CERRADO` no admite cambios (el cierre llega en la fase de seguimiento).
- Las fechas de resultado no pueden ser anteriores a la toma de la muestra ni futuras.

---

## 5. API

Todas las rutas requieren JWT.

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/v1/casos` | Registrar un caso. Devuelve el caso con su sugerencia. |
| `GET` | `/api/v1/casos?areaSaludId=&clasificacion=&estado=` | Listar con filtros opcionales. |
| `GET` | `/api/v1/casos/{id}` | Detalle: datos, resultados, historial y sugerencia. |
| `GET` | `/api/v1/pacientes/{id}/casos` | Casos de un paciente. |
| `PUT` | `/api/v1/casos/{id}` | Actualizar datos clínicos y epidemiológicos. |
| `POST` | `/api/v1/casos/{id}/resultados` | Añadir un resultado de laboratorio. |
| `PATCH` | `/api/v1/casos/{id}/clasificacion` | Cambiar la clasificación (motivo obligatorio). |

Códigos: `400` datos inválidos o regla incumplida, `401` sin autenticar, `404` no existe,
`409` ya hay un caso abierto para el paciente.

---

## 6. Arquitectura

```
domain/models/caso/      Caso, ResultadoLaboratorio, CambioClasificacion, enums,
                         ResultadoClasificacion, DefinicionCaso, ClasificadorOPS
domain/ports/in/         CasoUseCase
domain/ports/out/        CasoRepositoryPort, UsuarioActualPort
application/services/    CasoService (Java puro)
infrastructure/          CasoEntity (+ resultados e historial en cascada), JpaCasoRepository,
                         JpaCasoRepositoryAdapter, SecurityUsuarioActualAdapter,
                         CasoController + DTOs
```

## 7. Fases siguientes

- Atención clínica, seguimiento y cierre: implementado, ver [modulo-clinico.md](modulo-clinico.md).
- Pendiente: roles (ADMIN, MEDICO, EPIDEMIOLOGO) y visibilidad por área.
- Pendiente: transmisión vertical.
- Pendiente: estadísticas de vigilancia.
