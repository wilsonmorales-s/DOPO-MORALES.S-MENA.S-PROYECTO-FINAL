package domain;

// Errores del sistema: datos de configuracion invalidos o acciones sin partido.
// Cada constante es el mensaje que ve el usuario.
public class HeadSoccerException extends Exception {

    public static final String LIMITE_TIEMPO = "El tiempo debe estar entre 1 y 10 minutos";
    public static final String LIMITE_GOLES = "Los goles deben estar entre 1 y 10";
    public static final String NOMBRE_VACIO = "Cada jugador debe tener un nombre";
    public static final String NOMBRES_IGUALES = "Los jugadores deben tener nombres diferentes";
    public static final String TIPO_NULO = "Debe elegir el tipo de partido";
    public static final String CONFIGURACION_NULA = "Debe configurar el partido antes de iniciarlo";
    public static final String SIN_PARTIDO = "No hay un partido iniciado";
    public static final String LADO_NULO = "Debe indicar el lado del jugador";

    public HeadSoccerException(String mensaje) {
        super(mensaje);
    }
}
