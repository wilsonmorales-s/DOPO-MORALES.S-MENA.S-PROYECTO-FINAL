package domain;

// Efecto del Aura Azul (Corea del Sur): el rival es lanzado hacia su propio arco.
// Mientras dura no controla su movimiento: no puede moverse, saltar ni patear.
public class Empuje extends Efecto {

    private static final long serialVersionUID = 1L;

    public static final double DURACION = 1.5;
    public static final double VELOCIDAD = 650;   // rapidez con la que lo arrastra hacia su arco
    public static final double ALTURA = 350;      // pequeno salto al recibir el golpe

    public Empuje() {
        super(DURACION);
    }

    @Override
    public String getNombre() {
        return "Empuje";
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

    @Override
    public boolean tieneVelocidadForzada() {
        return true;
    }

    // Su propio arco: el de la izquierda esta en x = 0 y el de la derecha al final de la cancha.
    @Override
    public double getVelocidadForzada(Lado lado) {
        if (lado == Lado.IZQUIERDA) {
            return -VELOCIDAD;
        } else {
            return VELOCIDAD;
        }
    }
}
