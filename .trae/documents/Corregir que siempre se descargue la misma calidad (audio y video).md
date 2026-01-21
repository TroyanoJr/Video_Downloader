## Causa
- `yt-dlp` detecta que el archivo con nombre `%(title)s.%(ext)s` ya existe y devuelve “has already been downloaded”, por eso baja siempre la primera calidad descargada aunque selecciones otra.

## Solución
1. Backend: cambiar la plantilla de salida `-o` para incluir la calidad seleccionada y evitar reutilización:
   - Video/Other: `%(title)s-%(height)s.%(ext)s`.
   - Audio: `%(title)s-%(abr)sK.%(ext)s`.
2. Opcional: añadir `--force-overwrites` si se desea reemplazar el mismo nombre.
3. Verificar que cada botón envía su `selector -f` y que el archivo final se distingue por resolución.

## Verificación
- Descargar dos resoluciones distintas del mismo video: deben generarse archivos con sufijo de altura diferente y ya no aparecerá “has already been downloaded”.

¿Aplico este ajuste ahora?