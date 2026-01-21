## Diagnóstico
- Tomcat elimina el header `Content-Disposition` por inválido al intentar enviar nombres con acentos/símbolos (ej. “SEGUIRÁS”).
- Causa: los headers HTTP deben codificarse en ASCII/ISO‑8859‑1; Spring/Tomcat no pueden mapear esos caracteres y lanzan `UnmappableCharacterException`.

## Solución
1. Construir el header con `ContentDisposition` de Spring usando `filename(..., StandardCharsets.UTF_8)` que añade `filename*=` (RFC 5987) y escapa correctamente.
2. Mantener también `Content-Type` y `Content-Length`.
3. Fallback: si el cliente no soporta `filename*`, proveer versión saneada ASCII para `filename` (opcional).

## Cambios concretos
- Editar `src/main/java/com/video_downloader/controller/DownloadController.java` en el método `file(...)`:
  - Reemplazar el `header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + f.getName())` por:
    - `ContentDisposition cd = ContentDisposition.attachment().filename(f.getName(), StandardCharsets.UTF_8).build();`
    - `headers.setContentDisposition(cd);`
    - Opcional: `headers.add(HttpHeaders.CONTENT_DISPOSITION, cd.toString());` si construimos manualmente el `ResponseEntity`.
  - Conservar `MediaType.APPLICATION_OCTET_STREAM` y `contentLength`.

## Verificación
- Descargar un archivo cuyo título tenga acentos/espacios/`#`.
- Confirmar que el navegador presenta el nombre correcto y desaparece el warning de Tomcat.

¿Aplico estos cambios ahora?