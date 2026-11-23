package domain;

// Estados Unidos - Ilusion: el balon se divide en 8 balones identicos; 7 son falsos. Efecto: confusion.
public class EstadosUnidos extends Personaje {

    private static final long serialVersionUID = 1L;

    public static final double DANO_TIRO = 30;
    public static final double VELOCIDAD_TIRO = 950;
    public static final int BALONES_FALSOS = 7;

    @Override
    public String getPais() {
        return "Estados Unidos";
    }

    @Override
    public String getNombrePoder() {
        return "Ilusion";
    }

    @Override
    public double getDanoTiro() {
        return DANO_TIRO;
    }

    @Override
    public int getColor() {
        return 0xE53935;
    }

    @Override
    public void lanzarTiro(Balon balon, Jugador lanzador, Partido partido) {
        ponerDelante(balon, lanzador, lanzador.getCentroY());
        balon.setVelocidad(sentido(lanzador) * VELOCIDAD_TIRO, 350);
        balon.iniciarTiroDePoder(lanzador.getLado(), false);
        partido.crearBalonesFalsos(BALONES_FALSOS, Confusion.DURACION);
    }

    @Override
    public Efecto crearEfecto() {
        return new Confusion();
    }
}
