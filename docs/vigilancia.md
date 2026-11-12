# Estadísticas de vigilancia epidemiológica

Fase 5 del backend (2026-11-12). Acceso: **solo EPIDEMIOLOGO** (ver [roles-y-acceso.md](roles-y-acceso.md)).

## 1. Semana epidemiológica

- Va de **domingo a sábado**.
- La **semana 1** es la que termina en el primer sábado de enero que incluye cuatro o más días de
  enero, aunque empiece en diciembre. Ejemplos: la SE 1 de 2026 va del 4 al 10 de enero; del 1 al
  3 de enero de 2026 es la SE 53 de 2025; la SE 1 de 2025 empezó el 29 de diciembre de 2024.
- Etiqueta: `2026-SE01`.

Fuente consultada: Ministerio de Salud Pública de Uruguay, *Calendarios epidemiológicos*
(<https://www.gub.uy/ministerio-salud-publica/comunicacion/publicaciones/calendarios-epidemiologicos>).
Es la regla habitual en la Región; conviene confirmarla con el calendario oficial del país que use
el sistema.

## 2. Filtro común

Todos los parámetros son opcionales:

| Parámetro | Por defecto | Descripción |
|---|---|---|
| `desde`, `hasta` | Las últimas 12 semanas epidemiológicas hasta hoy | Fechas ISO (`2026-11-12`). Máximo 3 años |
| `areaSaludId` | Todas | Área de salud |
| `criterio` | `INICIO_SINTOMAS` | Fecha que sitúa el caso en el periodo y en su semana: `INICIO_SINTOMAS` (habitual en la curva epidémica) o `NOTIFICACION` |

## 3. Endpoints

| Ruta | Devuelve |
|---|---|
| `GET /api/v1/estadisticas/resumen` | Total de casos; casos por clasificación; % de confirmados (sobre el total); abiertos y cerrados; validados y pendientes de validar; con laboratorio de Oropouche; gestantes; formas neuroinvasivas; recaídas; hospitalizados; cerrados por desenlace |
| `GET /api/v1/estadisticas/semanas` | Curva epidémica: una fila por semana epidemiológica del periodo (incluidas las semanas sin casos), con el total y el desglose por clasificación |
| `GET /api/v1/estadisticas/areas` | Una fila por área de salud (incluidas las que no tienen casos): total, abiertos y desglose por clasificación |
| `GET /api/v1/estadisticas/casos.csv` | Línea de casos para descargar |

Definiciones:
- **Neuroinvasivo:** manifestación neurológica en el caso o signos neurológicos (vértigo, letargia,
  nistagmo, rigidez de nuca) en alguna evaluación.
- **Hospitalizado:** algún plan de atención con ingreso hospitalario o en unidad especializada.
- **Con laboratorio:** al menos un resultado de laboratorio de Oropouche (de cualquier tipo y resultado).

## 4. Exportación CSV

- Separador `;` y UTF-8 con BOM, para que Excel en español lo abra con tildes y columnas correctas.
- **No incluye nombres ni documentos de los pacientes**, solo su identificador interno, para
  minimizar los datos personales que salen del sistema.
- Columnas: `caso_id`, `paciente_id`, `area_codigo`, `fecha_inicio_sintomas`, `fecha_notificacion`,
  `semana_epidemiologica`, `clasificacion`, `clasificacion_validada`, `estado`, `embarazada`,
  `recaida`, `neuroinvasivo`, `hospitalizado`, `manifestaciones`, `desenlace`, `fecha_cierre`.
- Los valores que empiezan por `=`, `+`, `-` o `@` se neutralizan para evitar la inyección de
  fórmulas en hojas de cálculo.

## 5. Arquitectura y límites

- `SemanaEpidemiologica` y los indicadores son modelos de dominio; `VigilanciaService` calcula en
  Java puro y `VigilanciaUseCaseConAcceso` restringe el acceso al epidemiólogo.
- El repositorio filtra por rango de fechas en la base de datos y el resto se calcula en memoria.
  Es suficiente para volúmenes de miles de casos por consulta; con volúmenes mucho mayores
  convendría pasar los conteos a consultas agregadas.
