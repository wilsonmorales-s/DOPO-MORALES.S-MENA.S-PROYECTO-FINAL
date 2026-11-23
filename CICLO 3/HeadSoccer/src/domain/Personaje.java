package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Un personaje es un pais con su tiro de poder.
// Para un personaje nuevo se hereda de esta clase y se agrega en disponibles().
public abstract class Personaje implements Serializable {

    private static final long serialVersionUID = 1L;

    public abstract String getPais();

    public abstract String getNombrePoder();

    // Dano base del tiro de poder (DanoTiro); el dano real es DanoTiro * multiplicadorKick.
    public abstract double getDanoTiro();

    // Color principal del personaje en formato RGB (0xRRGGBB). La interfaz lo usa para dibujarlo.
    public abstract int getColor();

    // Pone el balon en movimiento segun como sale el tiro de este personaje.
    public abstract void lanzarTiro(Balon balon, Jugador lanzador, Partido partido);

    // Efecto nuevo que recibe el rival si el tiro lo golpea.
    public abstract Efecto crearEfecto();

    // 1 si el lanzador ataca hacia la derecha, -1 si ataca hacia la izquierda.
    protected double sentido(Jugador lanzador) {
        if (lanzador.getLado() == Lado.IZQUIERDA) {
            return 1;
        } else {
            return -1;
        }
    }

    // Saca el balon justo delante de la cabeza del lanzador, para que no lo vuelva a tocar.
    protected void ponerDelante(Balon balon, Jugador lanzador, double altura) {
        double x = lanzador.getCentroX() + sentido(lanzador) * (Jugador.RADIO + Balon.RADIO + 2);
        balon.setPosicion(x, altura);
    }

    // Personajes que se pueden elegir.
    public static List<Personaje> disponibles() {
        List<Personaje> personajes = new ArrayList<Personaje>();
        personajes.add(new CoreaDelSur());
        personajes.add(new Camerun());
        personajes.add(new Nigeria());
        personajes.add(new EstadosUnidos());
        return personajes;
    }

    @Override
    public String toString() {
        return getPais() + " - " + getNombrePoder();
    }
}
