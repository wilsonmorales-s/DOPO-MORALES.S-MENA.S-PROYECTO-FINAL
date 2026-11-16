package domain;

// Lleva la cuenta de los goles de cada lado.
public class Marcador {

    private int golesIzquierda;
    private int golesDerecha;

    // Suma un gol al lado que anoto.
    public void sumarGol(Lado lado) {
        if (lado == Lado.IZQUIERDA) {
            golesIzquierda = golesIzquierda + 1;
        } else {
            golesDerecha = golesDerecha + 1;
        }
    }

    public int getGoles(Lado lado) {
        if (lado == Lado.IZQUIERDA) {
            return golesIzquierda;
        } else {
            return golesDerecha;
        }
    }

    // Deja el marcador en 0 - 0.
    public void reiniciar() {
        golesIzquierda = 0;
        golesDerecha = 0;
    }
}
