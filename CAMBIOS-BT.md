# Wii TV — versión BT

Versión personalizada del launcher para una TV Motorola (KTC, Android TV 11, 2 GB de RAM).

## Créditos

- Proyecto original: [MauroProto/wii-tv](https://github.com/MauroProto/wii-tv) (licencia MIT, ver `LICENSE`).
- Basado a su vez en [cleanyourtv](https://github.com/Gathin23/cleanyourtv) de Gathin (licencia MIT).
- Proyecto de fans, sin relación con Nintendo.

## Cambios de esta versión

**Más liviano** (~98 MB de memoria frente a ~165 MB de FLauncher en la misma TV):
- Sin música de fondo, sin canal de Miis, sin canal de juegos, sin fondo animado ni tarjetas de módulo.
- Sin canal "Wiimote" y **sin permiso de internet**: el launcher funciona 100 % sin conexión.
- Movimiento reducido y sin pantalla blanca de aviso al encender.

**Diseño:**
- Modo oscuro (grafito con tinte azul) calculado una sola vez en el fondo (`WiiBase.DARK`).
- Cuadrícula de 4×2 (8 apps por página) y solo las páginas necesarias.
- Apps más grandes, esquinas redondeadas, borde fino y sombra suave; espacios vacíos casi invisibles.
- Botón izquierdo con las iniciales **BT** (abre Ajustes) y botón derecho con ícono de TV (abre las entradas).
- Sin botón SD.
- Imágenes uniformes para cada app (`ThemedBanner.java`): fondo oscuro con el color de la app, ícono de línea fina, nombre y categoría. Se activan con `themed_banners` en `config.xml`.

## Compilar e instalar (Windows)

Requiere Java 17 y el SDK de Android (plataforma 34).

```
cd launcher
gradlew.bat assembleRelease
adb connect <IP-de-la-TV>:5555
adb install -r app\build\outputs\apk\release\app-release.apk
adb shell cmd package set-home-activity com.gathin.tvhome/.WiiMenuActivity
```

Después, apagar la Depuración USB en la TV.
