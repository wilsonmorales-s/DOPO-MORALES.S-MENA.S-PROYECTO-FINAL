package domain;

import java.io.Serializable;

// El balon. (x, y) es su centro; y se mide desde el suelo hacia arriba.
// vx y vy son su velocidad en cada eje (cuanto avanza por segundo).
public class Balon implements Serializable {

    public static final double RADIO = 18;
    public static final double GRAVEDAD = 1400;        // cuanto lo jala hacia abajo
    public static final double REBOTE = 0.75;          // al rebotar conserva el 75% de la velocidad
    public static final double ROZAMIENTO_SUELO = 1.5; // lo frena cuando rueda por el suelo
    public static final double DURACION_TIRO = 2;      // segundos maximos de un tiro de poder

    private double x;
    private double y;
    private double vx;
    private double vy;
    private boolean tiroDePoder;
    private Lado lanzador;          // quien lanzo el tiro de poder
    private boolean sinGravedad;    // los tiros rectos no caen
    private double tiempoTiro;

    public Balon(double x, double y) {
        setPosicion(x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public void setPosicion(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setVelocidad(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    // Pone el balon quieto en el punto indicado (se usa en el saque).
    public void reiniciar(double x, double y) {
        setPosicion(x, y);
        setVelocidad(0, 0);
    }

    // Mueve el balon lo que le corresponde en dt segundos.
    public void actualizar(double dt, Campo campo) {
        // 0. El tiro de poder dura maximo 2 segundos
        if (tiroDePoder) {
            tiempoTiro = tiempoTiro - dt;
            if (tiempoTiro <= 0) {
                terminarTiroDePoder();
            }
        }

        // 1. La gravedad le quita velocidad hacia arriba (menos en los tiros rectos)
        if (!(tiroDePoder && sinGravedad)) {
            vy = vy - GRAVEDAD * dt;
        }

        // 2. Avanza segun su velocidad
        x = x + vx * dt;
        y = y + vy * dt;

        // 3. Si toca el suelo: no lo atraviesa, rebota y se frena un poco
        if (y - RADIO < 0) {
            y = RADIO;
            if (vy < 0) {
                vy = -vy * REBOTE;
            }
            if (vy < 40) {
                vy = 0; // rebote muy pequeno: lo dejamos quieto en el suelo
            }
            vx = vx * (1 - ROZAMIENTO_SUELO * dt);
        }

        // 4. Si toca el techo: rebota hacia abajo
        if (y + RADIO > campo.getAlto()) {
            y = campo.getAlto() - RADIO;
            if (vy > 0) {
                vy = -vy * REBOTE;
            }
            terminarTiroDePoder();
        }

        // 5. Paredes de los lados. Solo rebota si va por encima de la porteria;
        //    si va por debajo del travesano entra a la porteria (posible gol).
        if (!campo.cabeEnPorteria(y, RADIO)) {
            if (x - RADIO < 0) {
                x = RADIO;
                vx = Math.abs(vx) * REBOTE;   // sale hacia la derecha
                terminarTiroDePoder();
            } else if (x + RADIO > campo.getAncho()) {
                x = campo.getAncho() - RADIO;
                vx = -Math.abs(vx) * REBOTE;  // sale hacia la izquierda
                terminarTiroDePoder();
            }
        }
    }

    // ---------------------------------------------------------------- version tres

    public boolean esTiroDePoder() {
        return tiroDePoder;
    }

    public Lado getLanzador() {
        return lanzador;
    }

    public void iniciarTiroDePoder(Lado lanzador, boolean sinGravedad) {
        this.tiroDePoder = true;
        this.lanzador = lanzador;
        this.sinGravedad = sinGravedad;
        this.tiempoTiro = DURACION_TIRO;
    }

    public void terminarTiroDePoder() {
        tiroDePoder = false;
        lanzador = null;
        sinGravedad = false;
        tiempoTiro = 0;
    }
}
