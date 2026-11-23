package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CampoTest {

    @Test
    void tieneLasDimensionesPorDefecto() {
        Campo c = new Campo();
        assertEquals(Campo.ANCHO, c.getAncho());
        assertEquals(Campo.ALTO, c.getAlto());
        assertEquals(Campo.ALTO_PORTERIA, c.getAltoPorteria());
    }

    @Test
    void balonBajoElTravesanoCabeEnLaPorteria() {
        Campo c = new Campo();
        assertTrue(c.cabeEnPorteria(50, Balon.RADIO));
    }

    @Test
    void balonAltoNoCabeEnLaPorteria() {
        Campo c = new Campo();
        assertFalse(c.cabeEnPorteria(c.getAltoPorteria(), Balon.RADIO));
    }
}
