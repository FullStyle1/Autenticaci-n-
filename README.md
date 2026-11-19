# Evidencia 4: Core de alumnos

API REST de ejemplo construida con Java 21 y Spring Boot 3. Este Core no incluye
autenticacion ni autorizacion; sus endpoints son publicos intencionalmente para
servir como linea base antes de incorporar seguridad en una fase posterior.

## Requisitos

- JDK 21
- Maven 3.9 o posterior

## Ejecucion

Desde la raiz del proyecto:

```powershell
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Endpoints

| Metodo | Ruta | Resultado |
|---|---|---|
| GET | `/api/alumnos` | Lista alumnos |
| GET | `/api/alumnos/{id}` | Consulta un alumno |
| POST | `/api/alumnos` | Crea un alumno |
| PUT | `/api/alumnos/{id}` | Actualiza un alumno |
| DELETE | `/api/alumnos/{id}` | Elimina un alumno |

El almacenamiento es en memoria: los registros se pierden al detener la
aplicacion. Ejecuta las pruebas con `mvn test`.
