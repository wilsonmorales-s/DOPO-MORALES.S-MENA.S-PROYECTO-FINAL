package domain;

// Tamanos de cancha que se pueden elegir al iniciar el juego.
// Ancho y alto estan en unidades del juego (la interfaz las convierte a pixeles).
public enum Resolucion {
    PEQUENA("Pequena (800 x 400)", 800, 400),
    MEDIANA("Mediana (1000 x 500)", 1000, 500),
    GRANDE("Grande (1200 x 600)", 1200, 600);

    private final String nombre;
    private final double ancho;
    private final double alto;

    Resolucion(String nombre, double ancho, double alto) {
        this.nombre = nombre;
        this.ancho = ancho;
        this.alto = alto;
    }

    public String getNombre() {
        return nombre;
    }

    public double getAncho() {
        return ancho;
    }

    public double getAlto() {
        return alto;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
