package domain;

// Lo que el usuario elige antes de jugar: tipo de partido, limite y nombres.
// Si algun dato esta mal, el constructor lanza HeadSoccerException.
public class ConfiguracionPartido {

    public static final int MIN_LIMITE = 1;
    public static final int MAX_LIMITE = 10;

    private TipoPartido tipo;
    private int limite;             // minutos si es POR_TIEMPO, goles si es POR_GOLES
    private String nombreIzquierdo;
    private String nombreDerecho;

    public ConfiguracionPartido(TipoPartido tipo, int limite, String nombreIzquierdo, String nombreDerecho)
            throws HeadSoccerException {
        // 1. Debe haber un tipo de partido
        if (tipo == null) {
            throw new HeadSoccerException(HeadSoccerException.TIPO_NULO);
        }

        // 2. El limite debe estar entre 1 y 10 (el mensaje depende del tipo)
        if (limite < MIN_LIMITE || limite > MAX_LIMITE) {
            if (tipo == TipoPartido.POR_TIEMPO) {
                throw new HeadSoccerException(HeadSoccerException.LIMITE_TIEMPO);
            } else {
                throw new HeadSoccerException(HeadSoccerException.LIMITE_GOLES);
            }
        }

        // 3. Los dos jugadores deben tener nombre (isBlank = vacio o solo espacios)
        if (nombreIzquierdo == null || nombreIzquierdo.isBlank()
                || nombreDerecho == null || nombreDerecho.isBlank()) {
            throw new HeadSoccerException(HeadSoccerException.NOMBRE_VACIO);
        }

        // 4. Los nombres no pueden ser iguales (sin importar mayusculas ni espacios)
        if (nombreIzquierdo.trim().equalsIgnoreCase(nombreDerecho.trim())) {
            throw new HeadSoccerException(HeadSoccerException.NOMBRES_IGUALES);
        }

        // 5. Todo esta bien: guardamos los datos (trim quita espacios de los lados)
        this.tipo = tipo;
        this.limite = limite;
        this.nombreIzquierdo = nombreIzquierdo.trim();
        this.nombreDerecho = nombreDerecho.trim();
    }

    public TipoPartido getTipo() {
        return tipo;
    }

    public int getLimite() {
        return limite;
    }

    public String getNombreIzquierdo() {
        return nombreIzquierdo;
    }

    public String getNombreDerecho() {
        return nombreDerecho;
    }
}
