package domain;

// Efecto del Tornado (Nigeria): el rival es lanzado al aire y no puede moverse, saltar ni patear.
public class Inconsciente extends Efecto {

    private static final long serialVersionUID = 1L;

    public static final double DURACION = 3;
    public static final double ALTURA = 800;   // velocidad hacia arriba con la que sale volando

    public Inconsciente() {
        super(DURACION);
    }

    @Override
    public String getNombre() {
        return "Inconsciente";
    }

    @Override
    public void alAplicar(Jugador rival, Partido partido) {
        rival.lanzarAlAire(ALTURA);
    }

    @Override
    public boolean puedeMoverse() {
        return false;
    }

    @Override
    public boolean puedeSaltar() {
        return false;
    }

    @Override
    public boolean puedePatear() {
        return false;
    }
}
