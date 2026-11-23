package domain;

import java.io.Serializable;

// Lo que le pasa al rival cuando le pega un tiro de poder. Dura un tiempo y puede
// no dejarlo moverse, saltar o patear. Cada efecto es una clase hija.
public abstract class Efecto implements Serializable {

    private static final long serialVersionUID = 1L;

    private double duracion;
    private double tiempoRestante;

    protected Efecto(double duracion) {
        this.duracion = duracion;
        this.tiempoRestante = duracion;
    }

    public abstract String getNombre();

    public double getDuracion() {
        return duracion;
    }

    public double getTiempoRestante() {
        return tiempoRestante;
    }

    public boolean terminado() {
        return tiempoRestante <= 0;
    }

    // Descuenta tiempo al efecto.
    public void avanzar(double dt) {
        tiempoRestante = tiempoRestante - dt;
        if (tiempoRestante < 0) {
            tiempoRestante = 0;
        }
    }

    // Lo que pasa en el instante en que el efecto le cae al rival (por defecto nada).
    public void alAplicar(Jugador rival, Partido partido) {
    }

    public boolean puedeMoverse() {
        return true;
    }

    public boolean puedeSaltar() {
        return true;
    }

    public boolean puedePatear() {
        return true;
    }

    // Cuanto se multiplica la velocidad del rival (1 = normal).
    public double getFactorVelocidad() {
        return 1;
    }

    // Algunos efectos mueven al rival a la fuerza (por ejemplo, el empuje).
    public boolean tieneVelocidadForzada() {
        return false;
    }

    public double getVelocidadForzada(Lado lado) {
        return 0;
    }
}
