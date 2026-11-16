package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MarcadorTest {

    @Test
    void iniciaEnCero() {
        Marcador m = new Marcador();
        assertEquals(0, m.getGoles(Lado.IZQUIERDA));
        assertEquals(0, m.getGoles(Lado.DERECHA));
    }

    @Test
    void sumaGolesPorLado() {
        Marcador m = new Marcador();
        m.sumarGol(Lado.IZQUIERDA);
        m.sumarGol(Lado.IZQUIERDA);
        m.sumarGol(Lado.DERECHA);
        assertEquals(2, m.getGoles(Lado.IZQUIERDA));
        assertEquals(1, m.getGoles(Lado.DERECHA));
    }

    @Test
    void reiniciaLosGoles() {
        Marcador m = new Marcador();
        m.sumarGol(Lado.DERECHA);
        m.reiniciar();
        assertEquals(0, m.getGoles(Lado.DERECHA));
    }
}
