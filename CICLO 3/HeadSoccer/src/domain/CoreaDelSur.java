package domain;

// Corea del Sur - Aura Azul: tiro recto y horizontal a alta velocidad. Efecto: empuje.
public class CoreaDelSur extends Personaje {

    private static final long serialVersionUID = 1L;

    public static final double DANO_TIRO = 40;
    public static final double VELOCIDAD_TIRO = 1300;

    @Override
    public String getPais() {
        return "Corea del Sur";
    }

    @Override
    public String getNombrePoder() {
        return "Aura Azul";
    }

    @Override
    public double getDanoTiro() {
        return DANO_TIRO;
    }

    @Override
    public int getColor() {
        return 0x1E88E5;
    }

    @Override
    public void lanzarTiro(Balon balon, Jugador lanzador, Partido partido) {
        ponerDelante(balon, lanzador, lanzador.getCentroY());
        balon.setVelocidad(sentido(lanzador) * VELOCIDAD_TIRO, 0);
        balon.iniciarTiroDePoder(lanzador.getLado(), true); // recto: sin gravedad
    }

    @Override
    public Efecto crearEfecto() {
        return new Empuje();
    }
}
