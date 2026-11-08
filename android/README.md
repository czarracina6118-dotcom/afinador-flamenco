# Afinador Flamenco para Android

Esta versión empaqueta el HTML y el icono del repositorio en un APK de prueba.
No necesita conexión para afinar. Conserva el diseño y el cálculo de notas del HTML.
Android pide permiso de micrófono al pulsar «Activar micrófono»; se detiene al salir
de la aplicación y hay que activarlo de nuevo al volver.

## Descargar

1. Abrir **Actions → Generar APK del afinador** en GitHub.
2. Entrar en una ejecución terminada con marca verde.
3. En **Artifacts**, descargar **Afinador-Flamenco-APK** (requiere iniciar sesión).
4. Descomprimir el ZIP e instalar `Afinador-Flamenco-1.1-prueba.apk`.
5. Permitir la instalación desde esa aplicación si Android lo solicita.
6. Abrir el afinador, activar el micrófono y conceder el permiso.

Compatible con Android 8 o posterior y un Android System WebView actualizado.
Probar las seis cuerdas, modo cromático, denegar y volver a conceder el permiso,
y salir de la app para comprobar que desaparece el indicador de micrófono.

## Compilar

Con JDK 17, Android SDK 35 y Gradle 8.11.1:

```sh
cd android
gradle assembleDebug lintDebug
```

Se genera `app/build/outputs/apk/debug/app-debug.apk`. Los archivos web se copian
automáticamente desde la raíz: no hay que mantener dos versiones del afinador.

**Firma de prueba:** cada entorno puede generar una clave debug distinta. Una
compilación posterior podría requerir desinstalar la anterior. Antes de distribuir
versiones definitivas hay que configurar una clave de firma privada permanente;
nunca se debe subir esa clave al repositorio.
