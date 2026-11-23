package domain;

// Nigeria - Tornado: remolino a ras de suelo que rebota rapidamente. Efecto: inconsciente.
public class Nigeria extends Personaje {

    private static final long serialVersionUID = 1L;

    public static final double DANO_TIRO = 40;
    public static final double VELOCIDAD_TIRO = 900;
    public static final double REBOTE_TIRO = 380;   // saltitos rapidos contra el suelo

    @Override
    public String getPais() {
        return "Nigeria";
    }

    @Override
    public String getNombrePoder() {
        return "Tornado";
    }

    @Override
    public double getDanoTiro() {
        return DANO_TIRO;
    }

    @Override
    public int getColor() {
        return 0x43A047;
    }

    @Override
    public void lanzarTiro(Balon balon, Jugador lanzador, Partido partido) {
        ponerDelante(balon, lanzador, Balon.RADIO); // a ras de suelo
        balon.setVelocidad(sentido(lanzador) * VELOCIDAD_TIRO, REBOTE_TIRO);
        balon.iniciarTiroDePoder(lanzador.getLado(), false); // con gravedad: va rebotando
    }

    @Override
    public Efecto crearEfecto() {
        return new Inconsciente();
    }
}
