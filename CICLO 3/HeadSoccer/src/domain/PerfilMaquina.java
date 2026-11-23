package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Forma de jugar de la maquina. En cada cuadro decide que hace su jugador.
// Para un perfil nuevo se hereda de esta clase y se agrega en disponibles().
public abstract class PerfilMaquina implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final double MARGEN = 12;   // distancia a la que ya se considera "llegue"

    // Para que no sea imposible ganarle: la maquina piensa cada cierto tiempo (no en cada cuadro),
    // se equivoca un poco al ubicarse y solo patea fuerte cerca del arco rival.
    private Random aleatorio = new Random();
    private double esperaDecision;
    private double error;               // cuanto se equivoca al ubicarse en esta decision
    private Dificultad dificultad = Dificultad.MEDIA;

    public abstract String getNombre();

    public Dificultad getDificultad() {
        return dificultad;
    }

    public void setDificultad(Dificultad dificultad) throws HeadSoccerException {
        if (dificultad != null) {
            this.dificultad = dificultad;
        } else {
            throw new HeadSoccerException(HeadSoccerException.DIFICULTAD_NULA);
        }
    }

    // Segundos entre una decision y otra (mas alto = mas lenta).
    protected double getTiempoReaccion() {
        return dificultad.getTiempoReaccion();
    }

    // Cuantas unidades se puede equivocar al ubicarse.
    protected double getImprecision() {
        return dificultad.getImprecision();
    }

    // Probabilidad de patear cuando tiene el balon cerca del arco rival.
    protected double getProbabilidadPatada() {
        return dificultad.getProbabilidadPatada();
    }

    // True cuando ya le toca pensar otra vez. Mientras tanto sigue haciendo lo que estaba haciendo.
    protected boolean puedeDecidir(double dt) {
        esperaDecision = esperaDecision - dt;
        if (esperaDecision <= 0) {
            esperaDecision = getTiempoReaccion();
            error = (aleatorio.nextDouble() * 2 - 1) * getImprecision();
            return true;
        }
        return false;
    }

    // A veces (segun la dificultad) se queda quieta y no hace nada en esta decision.
    protected boolean distraida(Jugador yo) {
        if (aleatorio.nextDouble() < dificultad.getDistraccion()) {
            yo.mover(0);
            return true;
        }
        return false;
    }

    // Solo patea si el balon esta en el ultimo tercio de la cancha (cerca del arco rival) y le sale la probabilidad.
    protected boolean quierePatear(Partido partido, Jugador yo) {
        double ancho = partido.getCampo().getAncho();
        double x = partido.getBalon().getX();
        boolean cercaDelArco;
        if (yo.getLado() == Lado.IZQUIERDA) {
            cercaDelArco = x > ancho * 0.66;
        } else {
            cercaDelArco = x < ancho * 0.34;
        }
        return cercaDelArco && aleatorio.nextDouble() < getProbabilidadPatada();
    }

    // Decide la jugada de este cuadro para el jugador yo.
    public abstract void decidir(Partido partido, Jugador yo, double dt);

    // 1 si ataca hacia la derecha, -1 si ataca hacia la izquierda.
    protected double sentidoAtaque(Jugador yo) {
        if (yo.getLado() == Lado.IZQUIERDA) {
            return 1;
        } else {
            return -1;
        }
    }

    // x del arco que defiende.
    protected double arcoPropio(Partido partido, Jugador yo) {
        if (yo.getLado() == Lado.IZQUIERDA) {
            return 0;
        } else {
            return partido.getCampo().getAncho();
        }
    }

    protected Jugador rival(Partido partido, Jugador yo) {
        if (yo.getLado() == Lado.IZQUIERDA) {
            return partido.getJugador(Lado.DERECHA);
        } else {
            return partido.getJugador(Lado.IZQUIERDA);
        }
    }

    // Camina hacia la x indicada (o se queda quieto si ya llego).
    protected void moverHacia(Jugador yo, double xObjetivo) {
        double diferencia = xObjetivo - yo.getX();
        if (diferencia > MARGEN) {
            yo.mover(1);
        } else if (diferencia < -MARGEN) {
            yo.mover(-1);
        } else {
            yo.mover(0);
        }
    }

    // Distancia entre el centro de la cabeza y el balon.
    protected double distanciaAlBalon(Partido partido, Jugador jugador) {
        double dx = partido.getBalon().getX() - jugador.getCentroX();
        double dy = partido.getBalon().getY() - jugador.getCentroY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // True si el balon esta en la mitad de la cancha que defiende.
    protected boolean balonEnMiMitad(Partido partido, Jugador yo) {
        double mitad = partido.getCampo().getAncho() / 2;
        double x = partido.getBalon().getX();
        if (yo.getLado() == Lado.IZQUIERDA) {
            return x < mitad;
        } else {
            return x > mitad;
        }
    }

    // Se pone detras del balon (del lado de su arco) para empujarlo hacia el arco rival,
    // salta si el balon va alto y cerca, y patea si lo tiene al alcance.
    protected void atacar(Partido partido, Jugador yo) {
        Balon balon = partido.getBalon();
        double detras = balon.getX() - sentidoAtaque(yo) * (Jugador.RADIO * 0.6) + error;
        moverHacia(yo, detras);
        double distancia = distanciaAlBalon(partido, yo);
        if (Math.abs(balon.getX() - yo.getX()) < 110 && balon.getY() > Jugador.RADIO * 2.2) {
            yo.saltar();
        }
        if (distancia < Jugador.RADIO + Balon.RADIO + Jugador.ALCANCE_PATADA && quierePatear(partido, yo)) {
            yo.patear();
        }
    }

    // Se queda entre el balon y su arco. Si el balon llega a su mitad, sale a despejarlo.
    protected void defender(Partido partido, Jugador yo) {
        Balon balon = partido.getBalon();
        double puesto = arcoPropio(partido, yo) + sentidoAtaque(yo) * 140;
        if (balonEnMiMitad(partido, yo)) {
            double delante = balon.getX() - sentidoAtaque(yo) * (Jugador.RADIO + Balon.RADIO) + error;
            moverHacia(yo, delante);
        } else {
            moverHacia(yo, puesto);
        }
        double distancia = distanciaAlBalon(partido, yo);
        if (distancia < Jugador.RADIO * 3 && balon.getY() > Jugador.RADIO * 2) {
            yo.saltar();
        }
        if (distancia < Jugador.RADIO + Balon.RADIO + Jugador.ALCANCE_PATADA) {
            yo.patear();
        }
    }

    // Perfiles que se pueden elegir.
    public static List<PerfilMaquina> disponibles() {
        List<PerfilMaquina> perfiles = new ArrayList<PerfilMaquina>();
        perfiles.add(new MaquinaTorpe());
        perfiles.add(new MaquinaOfensiva());
        perfiles.add(new MaquinaDefensiva());
        perfiles.add(new MaquinaExperta());
        return perfiles;
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
