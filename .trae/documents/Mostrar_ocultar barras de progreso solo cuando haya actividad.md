## Cambios propuestos
- Ocultar ambas barras por defecto y mostrarlas solo durante su actividad.
- Barra de metadatos: se muestra al pulsar `start`, avanza durante la petición y se oculta al terminar (éxito o error).
- Barra de descarga: se muestra al crear la sesión, avanza durante el proceso y se oculta al finalizar (DONE/FAILED).
- Resetear a 0% y limpiar acciones entre intentos.

## Archivos a modificar
- `src/main/resources/static/index.html`: añadir contenedores con `d-none` y lógica JS para alternar visibilidad (`classList.add/remove('d-none')`).

## Validación
- Cargar la página, iniciar metadatos: la barra aparece y se oculta al finalizar.
- Iniciar una descarga: la barra aparece, se actualiza y se oculta cuando termina o falla.

¿Aplico estos cambios ahora?