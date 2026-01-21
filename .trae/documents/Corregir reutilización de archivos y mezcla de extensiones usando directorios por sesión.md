## Diagnóstico
- El backend guarda el archivo descargado buscando el “último archivo” en la carpeta compartida `downloads/`. Si descargas otra calidad/otro formato después, el endpoint `/file` de una sesión anterior devuelve ese último archivo, dando la impresión de que siempre se baja la misma resolución/extension.

## Solución
1. Asignar un subdirectorio por sesión (UUID) y escribir el archivo ahí.
2. Actualizar la plantilla `-o` para usar el subdirectorio de la sesión.
3. Al finalizar, buscar el archivo dentro del subdirectorio de la sesión y devolverlo.

## Cambios concretos
- `DownloadSession`: añadir campo `Path dir` para recordar el directorio de la sesión.
- `YtDlpService.startDownload`: crear `downloads/<sessionId>/` y usarlo en `-o`.
- `YtDlpService.trackProgress`: usar `findLatestFile(session.dir)`.

## Verificación
- Descargar distintas resoluciones/extensiones en varias sesiones; cada `/file` devolverá el archivo correspondiente y no el último global.

¿Aplico estos cambios ahora?