package domain;

// Quien juega en cada lado:
// JUGADOR_VS_JUGADOR -> dos personas en el mismo teclado
// JUGADOR_VS_MAQUINA -> el jugador 2 (derecha) lo controla la maquina
public enum Modalidad {
    JUGADOR_VS_JUGADOR, JUGADOR_VS_MAQUINA
}
