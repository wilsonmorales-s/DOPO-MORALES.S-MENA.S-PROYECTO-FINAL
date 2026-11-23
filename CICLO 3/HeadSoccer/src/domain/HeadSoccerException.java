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
    public static final String MODALIDAD_NULA = "Debe elegir la modalidad del juego";
    public static final String PERFIL_NULO = "Debe elegir el perfil de la maquina";
    public static final String DIFICULTAD_NULA = "Debe elegir la dificultad de la maquina";
    public static final String PERSONAJE_NULO = "Cada jugador debe elegir un personaje";
    public static final String RESOLUCION_NULA = "Debe elegir la resolucion de la cancha";
    public static final String ESTADISTICA_NULA = "Debe elegir la estadistica que quiere mejorar";
    public static final String ARCHIVO_NULO = "Debe elegir un archivo";
    public static final String ARCHIVO_NO_EXISTE = "El archivo no existe";
    public static final String ARCHIVO_INVALIDO = "El archivo no es una partida de Head Soccer";
    public static final String ERROR_GUARDAR = "No se pudo guardar el archivo";
    public static final String ERROR_ABRIR = "No se pudo abrir el archivo";

    public HeadSoccerException(String mensaje) {
        super(mensaje);
    }
}
