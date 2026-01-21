## Objetivos
- Fortalecer arquitectura y encapsulación de modelos
- Mejorar rendimiento y robustez del seguimiento de progreso
- Incorporar seguridad básica, límites y resiliencia
- Añadir observabilidad (métricas, logs), pruebas y CI/CD

## Fase 1: Arquitectura y limpieza
1. Separar DTOs públicos de estado interno (DownloadSession → DownloadSessionDto)
2. Encapsular campos y exponer solo lo necesario en controladores
3. Implementar cleanup de directorios por sesión y job de housekeeping

## Fase 2: Progreso y procesos
1. Estandarizar parsing de progreso (progress-template/JSON cuando sea posible)
2. Añadir timeout/cancelación para procesos yt-dlp
3. Controlar concurrencia (límite de descargas simultáneas)

## Fase 3: Seguridad
1. Validación de URL y selector, sanitización
2. Rate limiting básico y tamaño/tiempo máximos de descarga
3. Preparar capa de auth si se expone públicamente

## Fase 4: Observabilidad y calidad
1. Micrometer + endpoint /actuator para métricas
2. Logging estructurado (SLF4J) y niveles
3. Tests unitarios (parseFormats, buildSelectors, parsePercent) e integración para endpoints
4. Checkstyle/SpotBugs y reglas de calidad

## Fase 5: Experiencia de usuario
1. Mejoras en UI: deshabilitar botones durante operaciones, mostrar tamaño estimado
2. Manejo de errores de red y mensajes más detallados

## Fase 6: CI/CD
1. Pipeline de build y tests (GitHub Actions)
2. Artefactos JAR, variables de entorno para rutas yt-dlp/ffmpeg

¿Confirmas este plan para proceder a implementar por fases sin cambiar aún nada?