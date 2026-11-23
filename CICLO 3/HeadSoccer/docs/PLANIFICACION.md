**Planificación de las versiones - Head Soccer**

Head Soccer · Desarrollo Orientado a Objetos (DOPO)

Integrantes: Morales S. y Mena S.

## Versión uno: Jugador vs Jugador (ya entregada)

Objetivo: que dos personas puedan jugar un partido completo en el mismo teclado.

1. Maqueta y configuración: menú, pantalla de configuración y cancha dibujada.
2. Dominio básico: Campo, Jugador, Balon y Marcador, con la física y sus pruebas.
3. Reglas del partido: goles, fin por tiempo o por goles, muerte súbita, pausa y terminar.
4. Integración y fachada: HeadSoccer, el Timer y el teclado.
5. Cierre: excepciones, revancha, pruebas, diagramas y retrospectiva.

## Versión dos: guardar partidas y jugar contra la máquina

Objetivo: poder guardar y abrir una partida, y jugar contra la computadora.

1. Hacer Serializable las clases del dominio, y agregar guardar y abrir en la fachada.
2. Mensajes de error para los archivos (no existe, no es una partida, no se pudo guardar).
3. Menú Archivo (nuevo, abrir, guardar y salir), botón ABRIR PARTIDA y tecla G.
4. Clase PerfilMaquina con los cuatro perfiles: torpe, ofensiva, defensiva y experta, cada uno en dificultad baja, media o alta.
5. Modalidad y perfil en la pantalla de configuración, con sus pruebas.

## Versión tres: lo que falta del enunciado (esta entrega)

Objetivo: completar los requisitos funcionales.

1. Estadísticas (speed, kick, jump y survival), vida, daño de la patada y aturdimiento.
2. Barra de poder y aura.
3. Los cuatro personajes con su tiro de poder y su efecto (empuje, electrocutado, inconsciente y confusión).
4. Puntos al terminar y pantalla de mejoras. El progreso se guarda en progreso.dat.
5. Las tres resoluciones de cancha y el log de errores.
6. Animación del gol (1 segundo antes del siguiente saque) y dificultad de la máquina: piensa cada cierto tiempo, se equivoca un poco y solo patea fuerte cerca del arco.
7. Pruebas de unidad, pruebas de aceptación, diagramas y retrospectiva.

## Versión cuatro: dejarlo listo para la competencia

Objetivo: mejorar lo que ya hay sin agregar requisitos nuevos.

1. Seguir ajustando la dificultad de la máquina jugando partidos de prueba.
2. Agregar un personaje y una máquina nuevos para mostrar la extensión.
3. Mejorar cómo se ven los tiros de poder.
4. Pasar los diagramas nuevos a Astah y hacer la retrospectiva final.

## Requisitos que no se van a incluir

Ninguno. Estas son las decisiones que tomamos donde el enunciado no era claro:

- El precio de las mejoras se duplica por cada estadística: el primer punto de cada una cuesta 500, el segundo 1000, el tercero 2000.
- Las mejoras se compran en la pantalla MEJORAS y se notan desde el siguiente partido.
- Contra la máquina, la máquina siempre es el jugador 2.
- Si se termina el partido antes de tiempo, gana el que va adelante. Si van empatados, cada uno recibe 25 puntos.
- Los balones falsos de la Ilusión se mueven como el balón, pero no anotan.
- Una partida abierta queda en pausa.
