package domain;

// Efecto de la Ilusion (Estados Unidos): el rival no sabe cual es el balon real.
// Los 8 balones (1 real y 7 falsos) siguen en la cancha mientras dura el efecto.
public class Confusion extends Efecto {

    private static final long serialVersionUID = 1L;

    public static final double DURACION = 2.5;

    public Confusion() {
        super(DURACION);
    }

    @Override
    public String getNombre() {
        return "Confusion";
    }

    @Override
    public void alAplicar(Jugador rival, Partido partido) {
        partido.mantenerBalonesFalsos(DURACION);
    }
}
