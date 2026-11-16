package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JugadorTest {

    private final Campo campo = new Campo();

    private Jugador nuevo() {
        return new Jugador("Ana", Lado.IZQUIERDA, 250);
    }

    @Test
    void seMueveEnLaDireccionIndicada() {
        Jugador j = nuevo();
        j.mover(1);
        j.actualizar(0.1, campo);
        assertTrue(j.getX() > 250);
        j.mover(-1);
        j.actualizar(0.3, campo);
        assertTrue(j.getX() < 250);
    }

    @Test
    void sinDireccionSeQuedaQuieto() {
        Jugador j = nuevo();
        j.mover(0);
        j.actualizar(0.5, campo);
        assertEquals(250, j.getX());
    }

    @Test
    void noSaleDeLaCancha() {
        Jugador j = nuevo();
        j.mover(-1);
        j.actualizar(10, campo);
        assertEquals(Jugador.RADIO, j.getX());
        j.mover(1);
        j.actualizar(10, campo);
        assertEquals(campo.getAncho() - Jugador.RADIO, j.getX());
    }

    @Test
    void saltaSoloDesdeElSuelo() {
        Jugador j = nuevo();
        j.saltar();
        j.actualizar(0.05, campo);
        assertFalse(j.estaEnSuelo());
        double vyAntes = j.getVy();
        j.saltar();
        assertEquals(vyAntes, j.getVy());
    }

    @Test
    void despuesDeSaltarVuelveAlSuelo() {
        Jugador j = nuevo();
        j.saltar();
        for (int i = 0; i < 100; i++) {
            j.actualizar(0.016, campo);
        }
        assertTrue(j.estaEnSuelo());
        assertEquals(0, j.getY());
    }

    @Test
    void laPatadaDuraUnTiempoCorto() {
        Jugador j = nuevo();
        j.patear();
        assertTrue(j.estaPateando());
        j.actualizar(Jugador.DURACION_PATADA + 0.01, campo);
        assertFalse(j.estaPateando());
    }

    @Test
    void consumirPatadaLaTermina() {
        Jugador j = nuevo();
        j.patear();
        j.consumirPatada();
        assertFalse(j.estaPateando());
    }

    @Test
    void reiniciarLoDevuelveAlSuelo() {
        Jugador j = nuevo();
        j.saltar();
        j.actualizar(0.1, campo);
        j.reiniciar(300);
        assertEquals(300, j.getX());
        assertTrue(j.estaEnSuelo());
    }
}
