package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PoderTest {

    private final Campo campo = new Campo();

    private Jugador nuevo() {
        return new Jugador("Ana", Lado.IZQUIERDA, 250);
    }

    private void esperar(Jugador j, double segundos) {
        for (int i = 0; i < Math.round(segundos * 100); i++) {
            j.actualizar(0.01, campo);
        }
    }

    @Test
    void laBarraEmpiezaVaciaYSeLlenaEnDiezSegundos() {
        Jugador j = nuevo();
        assertEquals(0, j.getBarraPoder(), 1e-9);
        esperar(j, 5);
        assertEquals(0.5, j.getBarraPoder(), 1e-6);
        assertFalse(j.barraLlena());
        esperar(j, 5);
        assertTrue(j.barraLlena());
        esperar(j, 5);
        assertEquals(1, j.getBarraPoder(), 1e-9);
    }

    @Test
    void alRecibirUnGolLaBarraSubeUnCuarto() {
        Jugador j = nuevo();
        j.recibirGol();
        assertEquals(0.25, j.getBarraPoder(), 1e-9);
    }

    @Test
    void sinBarraLlenaNoSeActiva() {
        Jugador j = nuevo();
        esperar(j, 9);
        j.activarPoder();
        assertFalse(j.tieneAura());
    }

    @Test
    void conBarraLlenaQuedaConAuraYLaBarraVuelveACargarse() {
        Jugador j = nuevo();
        esperar(j, 10);
        j.activarPoder();
        assertTrue(j.tieneAura());
        assertEquals(0, j.getBarraPoder(), 1e-9);
        esperar(j, 2);
        assertEquals(0.2, j.getBarraPoder(), 1e-6);
    }

    @Test
    void elAuraNoTieneLimiteDeTiempoYNoSePuedeActivarOtroPoder() {
        Jugador j = nuevo();
        esperar(j, 10);
        j.activarPoder();
        esperar(j, 12);                // la barra se vuelve a llenar
        assertTrue(j.barraLlena());
        assertTrue(j.tieneAura());     // el aura sigue
        j.activarPoder();              // no pasa nada: ya tiene un poder
        assertTrue(j.barraLlena());
        j.consumirAura();
        assertFalse(j.tieneAura());
    }
}
