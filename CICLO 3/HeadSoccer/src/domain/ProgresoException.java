package domain;

// Errores al mejorar las estadisticas de un jugador.
public class ProgresoException extends HeadSoccerException {

    public static final String PUNTOS_INSUFICIENTES = "No tiene puntos suficientes para esta mejora";
    public static final String NIVEL_MAXIMO = "La estadistica ya esta en el nivel maximo (10)";

    public ProgresoException(String mensaje) {
        super(mensaje);
    }
}
