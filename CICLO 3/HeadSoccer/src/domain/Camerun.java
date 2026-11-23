package domain;

// Camerun - Rayo: tiro recto cubierto de electricidad. Efecto: electrocutado.
public class Camerun extends Personaje {

    private static final long serialVersionUID = 1L;

    public static final double DANO_TIRO = 60;
    public static final double VELOCIDAD_TIRO = 1100;

    @Override
    public String getPais() {
        return "Camerun";
    }

    @Override
    public String getNombrePoder() {
        return "Rayo";
    }

    @Override
    public double getDanoTiro() {
        return DANO_TIRO;
    }

    @Override
    public int getColor() {
        return 0xFDD835;
    }

    @Override
    public void lanzarTiro(Balon balon, Jugador lanzador, Partido partido) {
        ponerDelante(balon, lanzador, lanzador.getCentroY());
        balon.setVelocidad(sentido(lanzador) * VELOCIDAD_TIRO, 0);
        balon.iniciarTiroDePoder(lanzador.getLado(), true); // recto: sin gravedad
    }

    @Override
    public Efecto crearEfecto() {
        return new Electrocutado();
    }
}
