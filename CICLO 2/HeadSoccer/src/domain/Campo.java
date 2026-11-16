package domain;

// La cancha. Guarda sus medidas y las de las porterias.
// El suelo esta en y = 0 y la altura (y) crece hacia arriba.
public class Campo {

    public static final double ANCHO = 1000;
    public static final double ALTO = 500;
    public static final double ALTO_PORTERIA = 170;
    public static final double ANCHO_PORTERIA = 70;

    public double getAncho() {
        return ANCHO;
    }

    public double getAlto() {
        return ALTO;
    }

    public double getAltoPorteria() {
        return ALTO_PORTERIA;
    }

    public double getAnchoPorteria() {
        return ANCHO_PORTERIA;
    }

    // Dice si el balon pasa por debajo del travesano.
    // alturaCentro es la altura del centro del balon y radio su tamano.
    public boolean cabeEnPorteria(double alturaCentro, double radio) {
        double parteDeArribaDelBalon = alturaCentro + radio;
        return parteDeArribaDelBalon <= ALTO_PORTERIA;
    }
}
