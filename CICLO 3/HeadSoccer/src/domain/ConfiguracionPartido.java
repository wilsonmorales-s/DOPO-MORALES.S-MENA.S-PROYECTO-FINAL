package domain;

import java.io.Serializable;

// Lo que el usuario elige antes de jugar: tipo de partido, limite y nombres.
// Si algun dato esta mal, el constructor lanza HeadSoccerException.
public class ConfiguracionPartido implements Serializable {

    public static final int MIN_LIMITE = 1;
    public static final int MAX_LIMITE = 10;
    public static final String PREFIJO_MAQUINA = "Maquina ";

    private TipoPartido tipo;
    private int limite;             // minutos si es POR_TIEMPO, goles si es POR_GOLES
    private String nombreIzquierdo;
    private String nombreDerecho;
    private Modalidad modalidad = Modalidad.JUGADOR_VS_JUGADOR;
    private PerfilMaquina perfilMaquina;            // solo si es contra la maquina
    private Personaje personajeIzquierdo = new CoreaDelSur();
    private Personaje personajeDerecho = new Camerun();
    private Resolucion resolucion = Resolucion.MEDIANA;

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

    // Version tres: ademas se elige modalidad, perfil de la maquina, personajes y resolucion.
    // Contra la maquina, el jugador 2 se llama como el perfil (por ejemplo "Maquina Experta").
    public ConfiguracionPartido(Modalidad modalidad, PerfilMaquina perfilMaquina, TipoPartido tipo, int limite,
            String nombreIzquierdo, String nombreDerecho, Personaje personajeIzquierdo,
            Personaje personajeDerecho, Resolucion resolucion) throws HeadSoccerException {
        this(tipo, limite, nombreIzquierdo, nombreDeLaMaquina(modalidad, perfilMaquina, nombreDerecho));
        if (modalidad == null) {
            throw new HeadSoccerException(HeadSoccerException.MODALIDAD_NULA);
        }
        if (modalidad == Modalidad.JUGADOR_VS_MAQUINA && perfilMaquina == null) {
            throw new HeadSoccerException(HeadSoccerException.PERFIL_NULO);
        }
        if (personajeIzquierdo == null || personajeDerecho == null) {
            throw new HeadSoccerException(HeadSoccerException.PERSONAJE_NULO);
        }
        if (resolucion == null) {
            throw new HeadSoccerException(HeadSoccerException.RESOLUCION_NULA);
        }
        this.modalidad = modalidad;
        if (modalidad == Modalidad.JUGADOR_VS_MAQUINA) {
            this.perfilMaquina = perfilMaquina;
        }
        this.personajeIzquierdo = personajeIzquierdo;
        this.personajeDerecho = personajeDerecho;
        this.resolucion = resolucion;
    }

    private static String nombreDeLaMaquina(Modalidad modalidad, PerfilMaquina perfil, String nombreDerecho) {
        String nombre = nombreDerecho;
        if (modalidad == Modalidad.JUGADOR_VS_MAQUINA && perfil != null) {
            nombre = PREFIJO_MAQUINA + perfil.getNombre();
        }
        return nombre;
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

    public Modalidad getModalidad() {
        return modalidad;
    }

    public PerfilMaquina getPerfilMaquina() {
        return perfilMaquina;
    }

    public Personaje getPersonajeIzquierdo() {
        return personajeIzquierdo;
    }

    public Personaje getPersonajeDerecho() {
        return personajeDerecho;
    }

    public Resolucion getResolucion() {
        return resolucion;
    }
}
