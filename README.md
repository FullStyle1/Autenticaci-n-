# Evidencia 4: API de alumnos con autenticacion JWT

API REST construida con Java 21, Spring Boot 3 y Spring Security. El access
token JWT se valida sin estado en cada solicitud. Los refresh tokens se rotan y
se mantienen en un registro temporal en memoria.

## Requisitos

- JDK 21
- Maven 3.9 o posterior

## Ejecucion local

En PowerShell, desde la raiz del proyecto, genera una clave de 256 bits y
arranca la aplicacion en la misma ventana:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
mvn spring-boot:run
```

No guardes la clave en Git. La aplicacion requiere `JWT_SECRET`; puede
configurarse `JWT_ACCESS_TTL` y `JWT_REFRESH_TTL` en formato ISO-8601 para
ajustar la duracion durante pruebas locales.

Para habilitar el rol `ADMIN` en una ejecucion local, configura tambien
`APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD` en la misma terminal. El registro
publico siempre crea cuentas `USER`; no permite asignar privilegios elevados.

La API queda disponible en `http://localhost:8080`.

## Endpoints de autenticacion

| Metodo | Ruta | Acceso | Resultado |
|---|---|---|---|
| POST | `/api/registro` | Publico | Registra un usuario con rol `USER` |
| POST | `/api/login` | Publico | Valida credenciales y emite access y refresh tokens |
| POST | `/api/refresh` | Publico | Rota el refresh token y emite un nuevo par |

## Endpoints de alumnos

| Metodo | Ruta | Acceso |
|---|---|---|
| GET | `/api/alumnos` | `USER` o `ADMIN` |
| GET | `/api/alumnos/{id}` | `USER` o `ADMIN` |
| POST | `/api/alumnos` | `USER` o `ADMIN` |
| PUT | `/api/alumnos/{id}` | `USER` o `ADMIN` |
| DELETE | `/api/alumnos/{id}` | Solo `ADMIN` |

Las rutas protegidas requieren `Authorization: Bearer <accessToken>`. El access
token dura 15 minutos y el refresh token 7 días por defecto.

## Alcance del almacenamiento

Las cuentas, alumnos y sesiones de refresh se guardan en memoria para esta
práctica; se pierden al detener la aplicación. No es un almacenamiento de
producción. Ejecuta las pruebas automatizadas con `mvn test`.
