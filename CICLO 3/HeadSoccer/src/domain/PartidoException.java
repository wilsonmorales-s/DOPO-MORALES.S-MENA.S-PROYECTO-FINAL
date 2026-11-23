package domain;

// Errores del partido: pausar, reanudar o terminar cuando no se puede.
public class PartidoException extends HeadSoccerException {

    public static final String YA_FINALIZADO = "El partido ya termino";
    public static final String NO_ESTA_EN_JUEGO = "Solo se puede pausar un partido en juego";
    public static final String NO_ESTA_PAUSADO = "Solo se puede reanudar un partido en pausa";

    public PartidoException(String mensaje) {
        super(mensaje);
    }
}
