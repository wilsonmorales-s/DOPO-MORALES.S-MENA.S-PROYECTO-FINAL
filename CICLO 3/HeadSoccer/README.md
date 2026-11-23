# Head Soccer - Versión tres

Jugador vs Jugador y Jugador vs Máquina, con personajes y poderes, vida, estadísticas, puntos y mejoras, tres tamaños de cancha y guardar/abrir partidas. Java 21, Swing y JUnit 5.

- `src/domain`: lógica del juego, sin dependencias de Swing. Punto de entrada: `HeadSoccer`.
- `src/presentation`: interfaz. Clase principal: `HeadSoccerGUI`.
- `test/domain`: pruebas de unidad (116).
- `docs/diagramas/`: diagramas de secuencia en PlantUML. Del 01 al 15 son los de la versión uno y del 16 al 23 los nuevos. En `CAMBIOS_EN_DIAGRAMAS_VERSION_UNO.txt` están las líneas que se agregaron a los de la versión uno.
- `docs/PLANIFICACION.pdf`, `docs/PRUEBAS_ACEPTACION.pdf` y `docs/RETROSPECTIVA.pdf`.
- `docs/capturas/`: capturas del juego funcionando.

Errores: `HeadSoccerException` (errores del sistema, con constantes), `PartidoException` (errores del estado del partido) y `ProgresoException` (errores al mejorar). Los errores inesperados se guardan en `HeadSoccer.log`.

El juego crea `progreso.dat` (puntos y estadísticas) y las partidas se guardan como `.hsp`.

## Forma rapida (Windows)

- `jugar.bat`: compila y abre el juego.
- `correrPruebas.bat`: compila y corre todas las pruebas.

En VS Code o Eclipse el proyecto ya trae `.project` y `.classpath` (con JUnit de la carpeta `lib`), asi que las pruebas se pueden correr desde el panel de Testing.

## Compilar y ejecutar (Windows, desde la carpeta HeadSoccer)

```
javac -encoding UTF-8 -d out src\domain\*.java src\presentation\*.java
java -cp out presentation.HeadSoccerGUI
```

En Eclipse o VS Code basta con ejecutar `presentation.HeadSoccerGUI`.

## Pruebas

```
javac -encoding UTF-8 -cp lib\junit-platform-console-standalone-1.14.4.jar -d out src\domain\*.java src\presentation\*.java test\domain\*.java
java -jar lib\junit-platform-console-standalone-1.14.4.jar execute --class-path out --scan-class-path
```

## Controles

| Acción | Jugador 1 (izquierda) | Jugador 2 (derecha) |
|---|---|---|
| Mover | A / D | Flechas izquierda / derecha |
| Saltar | W | Flecha arriba |
| Patear | S | Flecha abajo |
| Poder | E | L o Ctrl derecho |

Pausa: ESC o P. Guardar: G. Terminar: T. Menú: M. Revancha al terminar: R o Enter.
