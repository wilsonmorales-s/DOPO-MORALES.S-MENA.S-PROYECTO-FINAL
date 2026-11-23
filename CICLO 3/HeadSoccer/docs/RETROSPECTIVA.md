**Retrospectiva - Versión tres**

Head Soccer · Desarrollo Orientado a Objetos (DOPO)

Integrantes: Morales S. y Mena S.

## 1. ¿Cuáles fueron los mini-ciclos definidos? Justifíquenlos.

1. Guardar y abrir partidas. Empezamos por aquí porque era lo más rápido: solo había que hacer Serializable las clases.
2. Jugador vs máquina. Hicimos una clase PerfilMaquina y una clase para cada perfil, así la máquina usa el mismo Jugador.
3. Estadísticas y vida. Iban antes de los poderes porque el daño de los poderes depende del kick y de la vida.
4. Poderes, personajes y efectos. Primero la barra y el aura, y después un país a la vez.
5. Puntos, mejoras, resolución y log.
6. Animación del gol y dificultad de la máquina.
7. Pruebas, pruebas de aceptación, diagramas y documentos.

No cambiamos los métodos que ya teníamos de la versión uno: lo nuevo lo pusimos en métodos aparte, para que los diagramas de esa versión sigan sirviendo.

## 2. ¿Cuál es el estado actual del proyecto? ¿Por qué?

Completo para la versión tres. Se puede jugar contra otra persona o contra la máquina (torpe, ofensiva, defensiva o experta), con los cuatro personajes y sus poderes. También funcionan la vida y el aturdimiento, las estadísticas, los puntos y las mejoras, las tres canchas y guardar y abrir partidas. Hay 112 pruebas de unidad y todas pasan.

Cuando hay gol sale una animación y el juego espera 1 segundo antes del saque. La máquina tiene tres dificultades (baja, media y alta) que cambian qué tan rápido reacciona, cuánto se equivoca y qué tanto patea.

Falta pasar a Astah los diagramas de secuencia nuevos.

## 3. ¿Cuál fue el tiempo total invertido por cada uno? (horas/hombre)

| Integrante | Horas |
|---|---|
| Morales S. | |
| Mena S. | |
| Total | |

## 4. ¿Cuál consideran fue el mayor logro? ¿Por qué?

Que los personajes, los efectos y las máquinas son clases hijas de Personaje, Efecto y PerfilMaquina. Para agregar uno nuevo no hay que tocar Partido ni Jugador, solo crear la clase. Además, como el dominio sigue separado de la interfaz, pudimos probar todo con JUnit.

## 5. ¿Cuál consideran que fue el mayor problema técnico? ¿Qué hicieron para resolverlo?

Que el tiro de poder no le pegara al mismo jugador que lo lanzó. Lo resolvimos así: el balón guarda quién lo lanzó, sale un poco delante de la cabeza y solo le hace daño al rival. El tiro se acaba cuando le pega al rival, rebota en una pared o en el techo, entra al arco o pasan 2 segundos.

## 6. ¿Qué hicieron bien como equipo? ¿Qué se comprometen a hacer para mejorar los resultados?

Bien:

Compromiso:

## 7. Referencias

- Enunciado del proyecto DOPOSoccer 2026-2.
- Documentación de Java SE 21 (Swing, Timer, ObjectOutputStream, Logger).
- Guía de usuario de JUnit 5.
- Head Soccer Wiki, para los personajes.
- Material del curso DOPO.
