## Cambios solicitados
- En la pestaña `Other` se listarán hasta las 6 primeras calidades disponibles (no-MP4), ordenadas por altura descendente dentro de 144–1100p.
- Al pulsar una resolución en `Video` u `Other` (ej. 640p) se ejecutará la descarga en esa resolución exacta.

## Estrategia para resolución exacta
1. Metadatos: usar `yt-dlp -J` y mapear cada formato por `format_id`, `ext`, `height`, `vcodec`, `acodec` y `filesize`.
2. `Video (MP4)`: construir combos exactos por altura con selector `-f`:
   - Preferir `bestvideo[ext=mp4][height=H] + bestaudio[ext=m4a]`.
   - Fallback si no hay audio separable: `best[ext=mp4][height=H]`.
   - Guardar el selector exacto por resolución (ej. `-f 247+140` o por filtros).
3. `Other (WEBM/otros)`: seleccionar formatos `ext != mp4` con altura en rango y construir hasta 6 opciones:
   - Preferir `bestvideo[ext=webm][height=H] + bestaudio[ext=webm]`.
   - Fallback: `best[ext=webm][height=H]` o `-f <format_id>` si es contenedor con audio.
4. En el frontend, cada botón de resolución envía al backend el `selector -f` o `format_id` exacto; el backend no recalcula, solo ejecuta esa selección.

## Comandos ejemplo (ejecución exacta)
- MP4 640p: `yt-dlp -f "bestvideo[ext=mp4][height=640]+bestaudio[ext=m4a]/best[ext=mp4][height=640]" --merge-output-format mp4 "URL"`.
- WEBM 640p: `yt-dlp -f "bestvideo[ext=webm][height=640]+bestaudio[ext=webm]/best[ext=webm][height=640]" "URL"`.
- Con `format_id` conocidos: `yt-dlp -f 247+251 "URL"` (garantiza 720p exacto si el 247 es 720p).

## Filtrado y orden
- `Video`: formatos `ext=mp4` y `144 <= height <= 1100`, orden desc y limitar a 6.
- `Other`: formatos `ext != mp4` en el mismo rango, orden desc y limitar a 6.
- `Audio`: MP3 320 y 240 (si 240 no, mostrar 128 como fallback). Ocultar opciones no disponibles.

## Endpoints (sin cambios estructurales)
- `POST /api/metadata` → devuelve listas con resoluciones y su `selector -f`/`format_id`.
- `POST /api/download/session` → recibe `{ url, selector }` y ejecuta `yt-dlp` con `--newline` para progreso.
- `GET /api/download/{id}/progress` (SSE) y `GET /api/download/{id}/file` para descarga.

## UI/UX
- En `Video` y `Other`, cada fila mostrará `altura`, `ext`, tamaño si disponible, y botón `Download` atado al selector exacto.
- Mantener diseño tipo Y2mate (tabs, tablas, barras de progreso). Mostrar “URL no disponible” cuando corresponda.

## Verificación
- Probar que al elegir 640p se descarga exactamente 640p (comprobando `yt-dlp` salida y propiedades del archivo).
- Confirmar top-6 en `Other` y ocultar formatos no válidos.

¿Confirmo estos ajustes para implementar inmediatamente?