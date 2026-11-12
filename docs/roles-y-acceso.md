# Roles y control de acceso

Roles aprobados el 2026-11-12. Fase 4 del backend.

## 1. Roles

| Rol | Puede | No puede |
|---|---|---|
| `ADMIN` | Crear usuarios, asignar roles y áreas, activar o desactivar usuarios, restablecer contraseñas, gestionar áreas de salud y el catálogo de desenlaces | Ver o modificar datos clínicos (pacientes, casos, atención) |
| `MEDICO` | Registrar pacientes y casos, resultados de laboratorio, evaluaciones clínicas y signos vitales, dictámenes, planes de atención, seguimientos, cierre y reapertura; proponer la clasificación | Acceder a otras áreas de salud; validar clasificaciones; administrar |
| `EPIDEMIOLOGO` | Ver pacientes y casos de **todas** las áreas, reclasificar y **validar** clasificaciones (y, en la siguiente fase, estadísticas) | Registrar atención clínica o pacientes; administrar |

Cualquier usuario autenticado puede leer las áreas de salud y el catálogo de desenlaces, y
consultar o modificar **sus propios** datos personales (`/api/v1/user/me`, `/api/v1/user/{suId}`).

## 2. Área de salud

- Todo `MEDICO` tiene un área de salud asignada (obligatoria, existente y activa).
- El médico solo lee y escribe pacientes, casos y atención de **su** área. En los listados, el
  sistema filtra por su área aunque no la indique; si pide otra, recibe `403`.
- No puede crear pacientes en otra área ni mover un paciente a otra área.
- El epidemiólogo puede filtrar por cualquier área o ver todas.

## 3. Clasificación: el médico propone, el epidemiólogo valida

- Cuando un **médico** registra un caso o cambia su clasificación, esta queda **pendiente de
  validar** (`clasificacionValidada = false`).
- Cuando la cambia un **epidemiólogo**, queda validada.
- El epidemiólogo puede **validar** la clasificación actual
  (`PATCH /api/v1/casos/{id}/clasificacion/validacion`, observaciones opcionales). La validación
  queda en los eventos del caso y se permite también en casos cerrados.
- Si un médico vuelve a cambiar una clasificación validada, vuelve a quedar pendiente.

## 4. Usuarios

- **No hay registro público.** Los usuarios los crea el ADMIN en `/api/v1/admin/usuarios`.
- **Primer administrador:** al arrancar, si no hay ningún ADMIN activo, se crea con
  `ADMIN_USERNAME` y `ADMIN_PASSWORD` (variables de entorno o `application-local.properties`).
  Si no están definidas, la aplicación avisa en el log.
- Nombre de usuario: 3-50 caracteres (letras, números, `.`, `-`, `_`). Contraseña: mínimo 8.
- Un usuario **desactivado** no puede iniciar sesión y su token deja de valer al instante.
- Nadie puede desactivarse a sí mismo, y siempre debe quedar **al menos un ADMIN activo**.

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/v1/admin/usuarios` | Listar usuarios (sin contraseñas) |
| `POST` | `/api/v1/admin/usuarios` | Crear `{username, password, firstname, lastname, country, role, areaSaludId}` |
| `PUT` | `/api/v1/admin/usuarios/{id}` | Cambiar `{role, areaSaludId, activo}` |
| `PATCH` | `/api/v1/admin/usuarios/{id}/password` | Restablecer contraseña `{password}` |
| `GET` | `/api/v1/user/me` | Perfil del usuario autenticado (id, nombre, rol, área), para el frontend |

## 5. Respuestas

- `401`: sin token, token inválido o caducado, o usuario desactivado.
- `403`: autenticado pero sin permiso por rol o por área (mensaje explicativo).

## 6. Arquitectura

El control de acceso tiene dos niveles:

1. **Por rol y URL** en `SecurityConfig` (infraestructura). Cualquier ruta no prevista se deniega.
2. **Por área y propiedad** en la capa de aplicación, en Java puro: `PoliticaAcceso` y
   decoradores que envuelven los casos de uso sin cambiar su lógica (`PacienteUseCaseConAcceso`,
   `CasoUseCaseConAcceso`, `AtencionClinicaUseCaseConAcceso`, `UsuarioPropioConAcceso`).

El usuario actual llega por el puerto `UsuarioActualPort` (`UsuarioActual`: id, usuario, rol, área).

## 7. Nota para bases de datos existentes

Hibernate (`ddl-auto=update`) añade columnas nuevas pero **no amplía las existentes**. Si una base
se creó con una versión anterior, aplicar:

```sql
ALTER TABLE caso_evento MODIFY tipo VARCHAR(40) NOT NULL;
```

Los usuarios con el antiguo rol `USER` deben pasar a uno de los nuevos roles antes de arrancar:

```sql
UPDATE user SET role = 'MEDICO' WHERE role = 'USER';  -- y asignarles area_salud_id
```
