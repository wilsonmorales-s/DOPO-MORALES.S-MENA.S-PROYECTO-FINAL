package domain;

// Efecto del Rayo (Camerun): el rival no puede saltar y se mueve al 50 % de su velocidad.
public class Electrocutado extends Efecto {

    private static final long serialVersionUID = 1L;

    public static final double DURACION = 3;
    public static final double FACTOR_VELOCIDAD = 0.5;

    public Electrocutado() {
        super(DURACION);
    }

    @Override
    public String getNombre() {
        return "Electrocutado";
    }

    @Override
    public boolean puedeSaltar() {
        return false;
    }

    @Override
    public double getFactorVelocidad() {
        return FACTOR_VELOCIDAD;
    }
}
