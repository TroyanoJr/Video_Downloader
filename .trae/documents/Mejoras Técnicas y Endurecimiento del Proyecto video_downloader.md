## Arquitectura del Código
- Separar responsabilidades en dos servicios: MetadataService (obtener/filtrar formatos) y DownloadService (gestionar descargas y progreso).
- Encapsular DownloadSession (campos privados) y exponer un DTO de respuesta (DownloadSessionDto) sin detalles internos (dir, process).
- Introducir un mapper (MapStruct o manual) para transformar entidades internas → DTOs.
- Definir contratos de API estables (OpenAPI/Swagger) y mantenerlos versionados.

## Rendimiento y Escalabilidad
- Sustituir hilos ad-hoc por un ThreadPool (ExecutorService) con límites y colas; controlar concurrencia máxima.
- Añadir cache para metadatos (URL → formatos) con TTL (Caffeine/Redis) para reducir llamadas a yt-dlp.
- Implementar límites por sesión (tiempo máximo, tamaño estimado) y cancelación de procesos.
- Preparar escalado horizontal: mover sesiones a Redis y usar IDs estables; considerar una cola de trabajos (RabbitMQ/Redis Streams).

## Seguridad
- Añadir autenticación básica (token/api-key) y rate limiting (Bucket4j) en endpoints.
- Validar y sanear entradas: URL, selector, ext, audioQuality.
- Activar CSRF si se sirve desde el mismo dominio; endurecer cabeceras (CSP, X-Frame-Options, HSTS) vía Spring Security.
- Auditoría de dependencias (OWASP Dependency-Check/Snyk) y actualización de versiones gestionadas.

## Observabilidad y Monitorización
- Incluir Spring Boot Actuator + Micrometer: métricas de descargas, latencias, uso de CPU/memoria, cola de trabajos.
- Exponer health checks y readiness/liveness endpoints.
- Configurar logs estructurados (SLF4J + Logback) con niveles y correlación por session-id.

## Mantenibilidad y Calidad
- Añadir tests unitarios (parseFormats, buildSelectorsByHeight, parsePercent) e integración para la API.
- Establecer estándares: Checkstyle/SpotBugs, formateo con Maven plugins.
- Documentar API con OpenAPI y brief ADRs para decisiones clave.

## Resiliencia
- Integrar Resilience4j: circuit breaker para llamadas a procesos externos, time limiter y retry con backoff.
- Manejar cancelación y reintentos controlados de descargas.

## CI/CD
- Pipeline Maven: build, tests, análisis estático, escaneo de dependencias, empaquetado y despliegue.
- Artefactos versionados y configuración por entorno (vars de entorno para yt-dlp/ffmpeg/dir de descarga).

## Frontend
- Bloquear acciones durante operaciones; mejorar manejo de errores de red.
- Mostrar tamaño estimado y detalles de formato; añadir cola de descargas visibles.

## Gestión de recursos
- Limpieza programada de sesiones/carpetas antiguas; eliminación segura del directorio al finalizar.
- Límites de espacio y alertas cuando se superen umbrales.

## Dependencias
- Alinear con versiones estables (Spring Boot BOM); fijar jackson-databind actualizado.
- Añadir perfiles Maven para dev/prod y centralizar config.

Confirma si deseas que implemente estas mejoras por fases (por ejemplo: 1) Observabilidad y limpieza de sesiones, 2) Encapsulación y DTOs, 3) Seguridad y resiliencia, 4) Concurrencia y cache, 5) CI/CD).