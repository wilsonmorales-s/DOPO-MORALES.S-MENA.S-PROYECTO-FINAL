package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BalonTest {

    private final Campo campo = new Campo();

    @Test
    void caePorLaGravedad() {
        Balon b = new Balon(500, 300);
        b.actualizar(0.1, campo);
        assertTrue(b.getY() < 300);
        assertTrue(b.getVy() < 0);
    }

    @Test
    void noAtraviesaElSuelo() {
        Balon b = new Balon(500, 100);
        for (int i = 0; i < 400; i++) {
            b.actualizar(0.016, campo);
            assertTrue(b.getY() >= Balon.RADIO);
        }
    }

    @Test
    void rebotaEnElSuelo() {
        Balon b = new Balon(500, Balon.RADIO + 1);
        b.setVelocidad(0, -500);
        b.actualizar(0.016, campo);
        assertTrue(b.getVy() > 0);
    }

    @Test
    void rebotaEnLaParedPorEncimaDeLaPorteria() {
        Balon b = new Balon(10, 300);
        b.setVelocidad(-400, 0);
        b.actualizar(0.016, campo);
        assertTrue(b.getVx() > 0);
        assertEquals(Balon.RADIO, b.getX());
    }

    @Test
    void pasaPorLaBocaDeLaPorteria() {
        Balon b = new Balon(10, 50);
        b.setVelocidad(-400, 0);
        b.actualizar(0.1, campo);
        assertTrue(b.getX() < 0);
    }

    @Test
    void rebotaEnElTecho() {
        Balon b = new Balon(500, campo.getAlto() - Balon.RADIO - 1);
        b.setVelocidad(0, 600);
        b.actualizar(0.016, campo);
        assertTrue(b.getVy() < 0);
    }

    @Test
    void reiniciarLoDejaQuieto() {
        Balon b = new Balon(500, 300);
        b.setVelocidad(100, 100);
        b.reiniciar(200, 200);
        assertEquals(200, b.getX());
        assertEquals(0, b.getVx());
        assertEquals(0, b.getVy());
    }
}
