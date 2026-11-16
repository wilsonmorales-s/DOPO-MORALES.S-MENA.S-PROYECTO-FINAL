package domain;

// Un jugador de cabeza grande.
// (x, y) es el punto de abajo en el centro: x es donde esta parado y y su altura sobre el suelo.
public class Jugador {

    public static final double RADIO = 45;             // tamano de la cabeza
    public static final double VELOCIDAD = 320;        // que tan rapido camina
    public static final double VELOCIDAD_SALTO = 650;  // con que fuerza salta
    public static final double GRAVEDAD = 1800;        // que tan rapido cae
    public static final double DURACION_PATADA = 0.25; // segundos que dura una patada
    public static final double ALCANCE_PATADA = 30;    // distancia extra a la que alcanza a patear
    public static final double FUERZA_PATADA = 750;    // velocidad que le da al balon

    private String nombre;
    private Lado lado;
    private double x;
    private double y;
    private double vy;            // velocidad vertical (para el salto)
    private int direccion;        // -1 izquierda, 0 quieto, 1 derecha
    private double tiempoPatada;  // cuanto le queda a la patada actual (0 = no esta pateando)

    public Jugador(String nombre, Lado lado, double x) {
        this.nombre = nombre;
        this.lado = lado;
        this.x = x;
    }

    public String getNombre() {
        return nombre;
    }

    public Lado getLado() {
        return lado;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // Velocidad horizontal: depende de hacia donde se esta moviendo.
    public double getVx() {
        return direccion * VELOCIDAD;
    }

    public double getVy() {
        return vy;
    }

    // Centro de la cabeza (sirve para saber si toca el balon).
    public double getCentroX() {
        return x;
    }

    public double getCentroY() {
        return y + RADIO;
    }

    public boolean estaEnSuelo() {
        return y <= 0;
    }

    public boolean estaPateando() {
        return tiempoPatada > 0;
    }

    // Cambia hacia donde camina: negativo izquierda, 0 quieto, positivo derecha.
    public void mover(int direccion) {
        if (direccion < 0) {
            this.direccion = -1;
        } else if (direccion > 0) {
            this.direccion = 1;
        } else {
            this.direccion = 0;
        }
    }

    // Solo puede saltar si esta en el suelo (no hay doble salto).
    public void saltar() {
        if (estaEnSuelo()) {
            vy = VELOCIDAD_SALTO;
        }
    }

    // Empieza una patada si no hay una en curso.
    public void patear() {
        if (!estaPateando()) {
            tiempoPatada = DURACION_PATADA;
        }
    }

    // La patada ya golpeo el balon: se acaba para no golpearlo dos veces.
    public void consumirPatada() {
        tiempoPatada = 0;
    }

    // Lo devuelve a su posicion inicial, quieto y en el suelo (despues de un gol).
    public void reiniciar(double x) {
        this.x = x;
        this.y = 0;
        this.vy = 0;
        this.direccion = 0;
        this.tiempoPatada = 0;
    }

    // Corre al jugador de lado (positivo a la derecha, negativo a la izquierda)
    // sin dejarlo salir de la cancha. Se usa cuando choca con el otro jugador.
    public void empujar(double cantidad, Campo campo) {
        x = x + cantidad;
        if (x < RADIO) {
            x = RADIO;
        }
        if (x > campo.getAncho() - RADIO) {
            x = campo.getAncho() - RADIO;
        }
    }

    // Mueve al jugador lo que le corresponde en dt segundos.
    public void actualizar(double dt, Campo campo) {
        // 1. Camina hacia donde va
        x = x + direccion * VELOCIDAD * dt;

        // 2. No se puede salir de la cancha
        if (x < RADIO) {
            x = RADIO;
        }
        if (x > campo.getAncho() - RADIO) {
            x = campo.getAncho() - RADIO;
        }

        // 3. Salto y gravedad
        vy = vy - GRAVEDAD * dt;
        y = y + vy * dt;
        if (y <= 0) {
            y = 0;   // llego al suelo
            vy = 0;
        }

        // 4. Se va gastando el tiempo de la patada
        if (tiempoPatada > 0) {
            tiempoPatada = tiempoPatada - dt;
            if (tiempoPatada < 0) {
                tiempoPatada = 0;
            }
        }
    }
}
