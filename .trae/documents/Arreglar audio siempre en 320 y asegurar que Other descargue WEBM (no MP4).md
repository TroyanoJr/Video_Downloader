## Diagnóstico
- Audio: el frontend fija siempre `audioQuality=320` en `startDownload`, ignorando la opción seleccionada; por eso todas las descargas salen con 320 kbps.
- Other: el backend fuerza `--merge-output-format mp4` para cualquier descarga que no sea audio; esto convierte también WEBM a MP4.

## Cambios propuestos
1. Frontend (`static/index.html`):
   - En cada fila de Audio, pasar el bitrate seleccionado: `startDownload(selector, 'audio', f.abr)`.
   - Modificar `startDownload` para enviar `audioQuality` dinámico, no fijo.
   - En Video y Other, enviar también el `ext` de la opción elegida en el body.
2. Backend:
   - `DownloadRequest` añadir campo `ext`.
   - `YtDlpService.startDownload(...)`:
     - Solo usar `--merge-output-format mp4` si `ext=='mp4'`.
     - Si `ext=='webm'`, usar `--merge-output-format webm` para garantizar contenedor WEBM.
     - Mantener extracción MP3 con `--audio-quality` usando el valor recibido.
3. Verificación:
   - Probar Audio 240 kbps y confirmar que baja a 240.
   - Probar Other en una resolución; verificar que el archivo sea `.webm` y no `.mp4`.
   - Probar Video y Other con varias resoluciones; cada una debe generar un archivo distinto por sufijo de altura.

¿Aplico estos cambios ahora?