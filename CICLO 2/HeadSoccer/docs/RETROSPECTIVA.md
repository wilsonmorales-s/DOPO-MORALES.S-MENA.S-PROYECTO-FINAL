# Retrospectiva - Versión uno (Head Soccer)

**Integrantes:** Morales S. y Mena S.
**Entrega:** versión uno, a pares el sábado 7 de noviembre y al evaluador preliminar el domingo 15 de noviembre de 2026.

> Completen los espacios marcados con **[ ]** antes de entregar.

## 1. ¿Cuáles fueron los mini-ciclos definidos? Justifíquenlos.

1. **Maqueta y configuración.** Primero la parte visible: menú, pantalla de configuración (tipo de partido, límite y nombres) y la cancha dibujada. Así teníamos desde el comienzo algo que mostrar y contra qué probar.
2. **Dominio básico.** `Campo`, `Jugador`, `Balon` y `Marcador`, con la física (gravedad, salto y rebotes) y sus pruebas de unidad, sin depender de Swing.
3. **Reglas del partido.** `Partido` y `ConfiguracionPartido`: contacto jugador-balón, patada, goles, fin por tiempo o por goles, muerte súbita, pausa y terminar.
4. **Integración y fachada.** `HeadSoccer` como única puerta entre la presentación y el dominio, el bucle de juego con `Timer` y el teclado para los dos jugadores.
5. **Cierre.** Excepciones con constantes (`HeadSoccerException` y `PartidoException`), la fachada siguiendo los diagramas de secuencia, revancha, más pruebas y la documentación.

Los ordenamos así porque cada ciclo deja algo que funciona y se puede probar antes de pasar al siguiente.

## 2. ¿Cuál es el estado actual del proyecto? ¿Por qué?

**Completo para la versión uno.** Se puede configurar y jugar un partido Jugador vs Jugador, por tiempo (1 a 10 minutos, con muerte súbita si hay empate) o por goles (1 a 10). También se puede pausar, terminar, pedir revancha y volver al menú. Hay 57 pruebas de unidad del dominio y todas pasan.

Queda pendiente pasar al modelo de Astah los cambios del diagrama de clases (`docs/diagrama_de_clases.md`).

## 3. ¿Cuál fue el tiempo total invertido por cada uno de ustedes? (horas/hombre)

| Integrante | Horas |
|---|---|
| Morales S. | [ ] |
| Mena S. | [ ] |
| **Total** | [ ] |

## 4. ¿Cuál consideran fue el mayor logro? ¿Por qué?

Mantener el dominio totalmente separado de la presentación. Toda la lógica (física, goles, tiempo y estados del partido) vive en `domain` y se prueba con JUnit sin abrir ninguna ventana. La interfaz solo lee el estado y llama a la fachada. Por eso pudimos probar a fondo las reglas del juego, y las versiones siguientes (persistencia y jugador vs máquina) se pueden montar sobre el mismo dominio.

## 5. ¿Cuál consideran que fue el mayor problema técnico? ¿Qué hicieron para resolverlo?

Lograr que el choque entre la cabeza del jugador y el balón se sintiera natural. Al principio el balón atravesaba al jugador o se quedaba pegado a él. Lo resolvimos pensando la cabeza y el balón como dos círculos:
- Se tocan cuando la distancia entre sus centros (Pitágoras) es menor que la suma de sus radios.
- Cuando se tocan, sacamos el balón hasta el borde de la cabeza y lo hacemos salir en la dirección del choque, con una rapidez mínima, sumándole el movimiento del jugador.

Además usamos el tiempo real entre cuadros (`dt`), para que el juego vaya a la misma velocidad en cualquier computador.

## 6. ¿Qué hicieron bien como equipo? ¿Qué se comprometen a hacer para mejorar los resultados?

**Bien:** [ ]. Por ejemplo, dividir el trabajo por paquetes (uno en presentación y otro en dominio) y acordar primero los métodos de la fachada.

**Compromiso:** [ ]. Por ejemplo, actualizar el modelo en Astah al mismo tiempo que el código y no al final, y escribir las pruebas antes de cada funcionalidad nueva del ciclo dos.

## 7. Referencias

- Documentación de Java SE 21 (Swing, `javax.swing.Timer`, `KeyListener`).
- Guía de usuario de JUnit 5.
- Juego original Head Soccer (D&D Dream), como referencia del diseño.
- Material del curso DOPO.
- [ ] Cualquier otra fuente o herramienta que hayan usado (incluido el apoyo de asistentes de IA, si su curso pide declararlo).
