## Qué haré
- Verificar que Maven usa JDK (no JRE) y que `javac` está en PATH.
- Si es necesario, ajustar `JAVA_HOME` y `PATH`.
- Compilar y arrancar el servidor Spring Boot.
- Probar flujo completo: metadatos → top-6 en Video y Other → descarga exacta (ej. 640p) y botón `Download`.

## Comandos de verificación y arranque
1. Comprobar versiones:
   - `java -version`
   - `javac -version`
   - `./mvnw.cmd -v`
2. Si Maven sigue usando JRE, configurar variables de entorno (ejemplos Windows):
   - `setx JAVA_HOME "C:\\Program Files\\Java\\jdk-17"`
   - `setx PATH "%JAVA_HOME%\\bin;%PATH%"`
   - Abrir nueva terminal y repetir verificación.
3. Construir y ejecutar:
   - `./mvnw.cmd clean package -DskipTests`
   - `./mvnw.cmd spring-boot:run -DskipTests`
4. Navegar a `http://localhost:8080/` y probar con una URL pública.

## Validación funcional
- Confirmar que `Video` muestra hasta 6 MP4 entre 144–1100p y `Other` hasta 6 no-MP4 en el mismo rango.
- Pulsar una resolución (p.ej. 640p) y observar progreso real; al finalizar, usar `Download`.
- Revisar que el selector `-f` se corresponde a la altura exacta (construcción en `src/main/java/com/video_downloader/service/YtDlpService.java:106-131` y ejecución en `src/main/java/com/video_downloader/service/YtDlpService.java:152-171`).

¿Procedo a ejecutar estos pasos ahora?