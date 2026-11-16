# Head Soccer - Versión uno (Presentación + Dominio)

Jugador vs Jugador, nivel básico. Java 21, Swing y JUnit 5.

- `src/domain`: lógica del juego, sin dependencias de Swing. Punto de entrada: `HeadSoccer`.
- `src/presentation`: interfaz. Clase principal: `HeadSoccerGUI`.
- `test/domain`: pruebas de unidad (57).
- `docs/diagramas/`: los 15 diagramas de secuencia en PlantUML (`.puml`) con su imagen (`.png`).
- `docs/RETROSPECTIVA.md`: retrospectiva de la versión uno.
- `docs/capturas/`: capturas del juego funcionando.

Errores: `HeadSoccerException` (errores del sistema, con constantes) y `PartidoException` (errores del estado del partido).

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

Pausa: ESC o P. Terminar: T. Menú: M. Revancha al terminar: R o Enter.
