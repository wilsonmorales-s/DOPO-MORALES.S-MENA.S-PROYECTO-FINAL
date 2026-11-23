package domain;

// Estados por los que pasa un partido:
// EN_JUEGO -> se esta jugando
// PAUSADO -> esta detenido, nada se mueve
// FINALIZADO -> ya termino, se puede ver el ganador
public enum EstadoPartido {
    EN_JUEGO, PAUSADO, FINALIZADO
}
