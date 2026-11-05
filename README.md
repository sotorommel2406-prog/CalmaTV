# Calma TV – Launcher para Android TV

Launcher ligero (Android 7.0+, pensado para 1 GB de RAM) con fondo azul relajante
y todas las apps visibles directamente en la pantalla de inicio, sin botón de cajón.

## Estructura
- `app/src/main/java/com/relax/tvlauncher/` – `MainActivity`, `AppAdapter`, `AppEntry`
- `app/src/main/res/` – layouts, degradados azules, iconos vectoriales y textos
- `.github/workflows/build.yml` – compila el APK en GitHub Actions
- `gradlew`, `gradlew.bat`, `gradle/wrapper/` – Gradle Wrapper (Gradle 8.2)

## Compilar con GitHub Actions
1. Sube todo el contenido de esta carpeta a un repositorio de GitHub (rama `main`).
2. En la pestaña **Actions** se ejecuta *Compilar APK*.
3. Descarga `CalmaTV-debug-apk` desde los artefactos del workflow.

## Importante: gradle-wrapper.jar
`gradle-wrapper.jar` es un archivo binario y no viene incluido en este ZIP.
- **GitHub Actions** lo descarga automáticamente si falta (paso "Asegurar gradle-wrapper.jar").
- **Android Studio** lo regenera al sincronizar, o puedes ejecutar `gradle wrapper --gradle-version 8.2`
  y subir el `.jar` al repositorio.

## Compilar en local
```
./gradlew assembleDebug      # Windows: gradlew.bat assembleDebug
```
Requiere JDK 17. El APK queda en `app/build/outputs/apk/debug/`.

## Instalar
```
adb install -r app-debug.apk
```
Al pulsar Inicio, Android pedirá elegir "Calma TV" como pantalla de inicio.

## Uso
- Mantén pulsado OK sobre una app: abrir, información o desinstalar.
- El botón de ajustes (arriba a la derecha) abre los ajustes del sistema.
