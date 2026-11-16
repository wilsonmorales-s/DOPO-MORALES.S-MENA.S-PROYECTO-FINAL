package domain;

// Fachada del dominio: es la unica clase que usa la interfaz (presentation).
// Cada metodo sigue su diagrama de secuencia en Astah.
// Si no hay partido o falta un dato, lanza HeadSoccerException.
public class HeadSoccer {

    private Partido partido;

    // Crea un partido nuevo con la configuracion elegida.
    public void iniciarPartido(ConfiguracionPartido configuracion) throws HeadSoccerException {
        if (configuracion != null) {
            partido = new Partido(configuracion);
        } else {
            throw new HeadSoccerException(HeadSoccerException.CONFIGURACION_NULA);
        }
    }

    public boolean hayPartido() {
        return partido != null;
    }

    // La interfaz lo usa para leer el estado y dibujarlo (marcador, posiciones, tiempo).
    public Partido getPartido() {
        return partido;
    }

    // Avanza el juego dt segundos (un cuadro).
    public void actualizar(double dt) throws HeadSoccerException {
        if (partido != null) {
            partido.actualizar(dt);
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }

    // direccion: -1 izquierda, 0 quieto, 1 derecha.
    public void moverJugador(Lado lado, int direccion) throws HeadSoccerException {
        Jugador jugador = buscarJugador(lado);
        jugador.mover(direccion);
    }

    public void saltar(Lado lado) throws HeadSoccerException {
        Jugador jugador = buscarJugador(lado);
        jugador.saltar();
    }

    public void patear(Lado lado) throws HeadSoccerException {
        Jugador jugador = buscarJugador(lado);
        jugador.patear();
    }

    public void pausar() throws HeadSoccerException {
        if (partido != null) {
            partido.pausar();
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }

    public void reanudar() throws HeadSoccerException {
        if (partido != null) {
            partido.reanudar();
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }

    public void terminar() throws HeadSoccerException {
        if (partido != null) {
            partido.terminar();
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }

    // Vuelve a jugar con la misma configuracion (mismo tipo, limite y nombres), desde 0 - 0.
    public void revancha() throws HeadSoccerException {
        if (partido != null) {
            ConfiguracionPartido configuracion = partido.getConfiguracion();
            iniciarPartido(configuracion);
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }

    // Busca el jugador del lado indicado. Lo usan moverJugador, saltar y patear.
    private Jugador buscarJugador(Lado lado) throws HeadSoccerException {
        if (partido != null) {
            if (lado != null) {
                return partido.getJugador(lado);
            } else {
                throw new HeadSoccerException(HeadSoccerException.LADO_NULO);
            }
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }
}
