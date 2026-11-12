# Estado del proyecto

Última actualización: 2026-11-12.

## 1. Resumen

El **backend está completo** en sus cinco fases, con arquitectura hexagonal en todos los módulos
(dominio y aplicación en Java puro, sin dependencias de Spring ni JPA) y **164 pruebas** en verde.
El siguiente paso es el **frontend**.

| Fase | Contenido | Documento |
|---|---|---|
| Base | Arquitectura hexagonal, login JWT, usuarios, áreas de salud, pacientes, seguridad | [README](../README.md) |
| 1 | Casos de Oropouche, laboratorio y clasificación según la OPS | [modulo-casos.md](modulo-casos.md) |
| 2-3 | Atención clínica, alertas, seguimiento, cierre y catálogo de desenlaces | [modulo-clinico.md](modulo-clinico.md) |
| 4 | Roles ADMIN, MEDICO y EPIDEMIOLOGO con acceso por área de salud | [roles-y-acceso.md](roles-y-acceso.md) |
| 5 | Estadísticas de vigilancia epidemiológica y exportación CSV | [vigilancia.md](vigilancia.md) |

## 2. Cómo arrancarlo en local

Requisitos: Java 17, Docker y MySQL 8 (en local se usa un contenedor).

```bash
# 1. MySQL (el contenedor ya existe en la PC de desarrollo; los datos están en un volumen)
docker start oropouche-mysql
#    Si no existe:
#    docker run -d --name oropouche-mysql --restart unless-stopped -p 127.0.0.1:3306:3306 --memory 512m \
#      -e MYSQL_ROOT_PASSWORD=<contraseña> -e MYSQL_DATABASE=oropouchedb -v oropouche_mysqldata:/var/lib/mysql \
#      mysql:8.0 --performance-schema=OFF --innodb-buffer-pool-size=64M --max-connections=30

# 2. Configuración local (no se sube a Git)
cp application-local.properties.example application-local.properties   # y rellenar valores

# 3. Arrancar con memoria limitada (la PC tiene 8 GB compartidos)
MAVEN_OPTS="-Xmx384m" ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx512m"

# 4. Pruebas
MAVEN_OPTS="-Xmx512m" ./mvnw clean test -DargLine="-Xmx768m"
```

- La aplicación escucha en `http://localhost:8080`.
- Al arrancar se crea el **primer administrador** con `ADMIN_USERNAME` / `ADMIN_PASSWORD` si no
  existe ninguno, y se carga el catálogo de desenlaces si está vacío.
- Después de un `git pull`, usar `./mvnw clean ...` para que no queden clases antiguas en `target/`.
- Al terminar, parar la aplicación y `docker stop oropouche-mysql` para liberar memoria.

## 3. Para empezar el frontend

Proyecto: `oropoucheApp-main` (Ionic 8 + Angular 18 + Capacitor), fuera de este repositorio.
Hoy solo tiene login y datos personales.

Puntos de partida en la API:
- `POST /auth/login` devuelve `{token}`. Ya **no existe** `/auth/register`.
- `GET /api/v1/user/me` devuelve id, nombre, **rol** y **área**: con eso el frontend decide qué
  pantallas mostrar. El frontend actual usa un `userId` fijo en `environment.ts`; debe pasar a usar `/me`.
- Todas las peticiones llevan `Authorization: Bearer <token>`. `401` significa volver al login;
  `403` significa sin permiso (el cuerpo trae un mensaje explicativo en español).
- CORS permitido para `http://localhost:4200`.

Pantallas que se desprenden de los roles:

| Rol | Pantallas |
|---|---|
| ADMIN | Usuarios (alta, rol, área, activar o desactivar, contraseña), áreas de salud, catálogo de desenlaces |
| MEDICO | Pacientes de su área, casos (registro con sugerencia OPS, laboratorio, clasificación), atención clínica (evaluaciones con alertas, dictámenes, planes, seguimientos, cierre), agenda de controles |
| EPIDEMIOLOGO | Casos de todas las áreas, validación de clasificaciones, panel de vigilancia (indicadores, curva epidémica, áreas, descarga CSV) |

## 4. Pendiente y mejoras recomendadas

Funcional:
- **Frontend** (siguiente fase).
- **Transmisión vertical** (feto y recién nacido), según las directrices específicas de la OPS.
- Validar con profesionales sanitarios las interpretaciones señaladas en
  [modulo-casos.md](modulo-casos.md) (recaída y límite de 5 días) y la regla de semana
  epidemiológica del país.

Técnico:
- **Migraciones con Flyway** en lugar de `ddl-auto=update` antes de trabajar con datos reales
  (Hibernate no amplía ni modifica columnas existentes; ver la nota de
  [roles-y-acceso.md](roles-y-acceso.md)).
- **Spring Boot 3**: la versión actual (2.6.1) ya no recibe parches de seguridad. Requiere pasar
  de `javax` a `jakarta`.
- Auditoría general (quién y cuándo) también para pacientes y áreas; los casos ya la tienen.
- Paginación en los listados y conteos de vigilancia agregados en base de datos si el volumen crece.
- Renombrar el paquete `com.oropouche.tasks`, heredado de la plantilla inicial, en un commit aparte.
- Documentación OpenAPI/Swagger para el frontend, Docker Compose y CI con GitHub Actions.
