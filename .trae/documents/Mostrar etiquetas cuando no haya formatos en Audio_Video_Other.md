## Cambios propuestos
- En la UI, si una sección no tiene formatos (lista vacía), mostrar una fila con un texto informativo en su tabla.
- Mensajes:
  - Audio: “No hay formatos de audio disponibles”.
  - Video: “No hay formatos de video disponibles”.
  - Other: “No hay formatos disponibles en Other”.
- Se actualizan dinámicamente en cada búsqueda.

## Archivos
- `src/main/resources/static/index.html`: lógica JS tras poblar cada tabla para insertar la etiqueta cuando las listas estén vacías.

## Verificación
- Probar con URLs que no tengan audio, video MP4 o WEBM en el rango; ver que aparece el mensaje en la sección correspondiente.

¿Aplico estos cambios ahora?