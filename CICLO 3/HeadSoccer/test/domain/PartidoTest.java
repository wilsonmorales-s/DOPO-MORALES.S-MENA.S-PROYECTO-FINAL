package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PartidoTest {

    private Partido crear(TipoPartido tipo, int limite) throws HeadSoccerException {
        return new Partido(new ConfiguracionPartido(tipo, limite, "Ana", "Luis"));
    }

    /** Pone el balon dentro de la porteria derecha: anota el lado izquierdo. */
    private void golIzquierda(Partido p) {
        p.getBalon().setPosicion(p.getCampo().getAncho() + 5, 50);
        p.getBalon().setVelocidad(0, 0);
        p.actualizar(0.001);
    }

    private void golDerecha(Partido p) {
        p.getBalon().setPosicion(-5, 50);
        p.getBalon().setVelocidad(0, 0);
        p.actualizar(0.001);
    }

    @Test
    void iniciaEnJuegoConElMarcadorEnCero() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        assertEquals(EstadoPartido.EN_JUEGO, p.getEstado());
        assertEquals(0, p.getMarcador().getGoles(Lado.IZQUIERDA));
        assertEquals("Ana", p.getJugador(Lado.IZQUIERDA).getNombre());
        assertEquals("Luis", p.getJugador(Lado.DERECHA).getNombre());
    }

    @Test
    void porTiempoArrancaConElTiempoCompleto() throws HeadSoccerException {
        assertEquals(120, crear(TipoPartido.POR_TIEMPO, 2).getTiempoRestante());
    }

    @Test
    void golEnLaPorteriaDerechaLoAnotaElIzquierdo() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        golIzquierda(p);
        assertEquals(1, p.getMarcador().getGoles(Lado.IZQUIERDA));
        assertEquals(0, p.getMarcador().getGoles(Lado.DERECHA));
    }

    @Test
    void golEnLaPorteriaIzquierdaLoAnotaElDerecho() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        golDerecha(p);
        assertEquals(1, p.getMarcador().getGoles(Lado.DERECHA));
    }

    @Test
    void despuesDeUnGolSeReiniciaElSaque() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        p.getJugador(Lado.IZQUIERDA).mover(1);
        p.actualizar(0.2);
        golIzquierda(p);
        assertEquals(p.getCampo().getAncho() / 2, p.getBalon().getX());
        assertEquals(Campo.ANCHO * 0.25, p.getJugador(Lado.IZQUIERDA).getX());
        assertEquals(EstadoPartido.EN_JUEGO, p.getEstado());
    }

    @Test
    void porGolesTerminaAlLlegarALaMeta() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 2);
        golDerecha(p);
        assertEquals(EstadoPartido.EN_JUEGO, p.getEstado());
        golDerecha(p);
        assertEquals(EstadoPartido.FINALIZADO, p.getEstado());
        assertSame(p.getJugador(Lado.DERECHA), p.getGanador());
    }

    @Test
    void porTiempoTerminaConElMarcadorADesempate() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_TIEMPO, 1);
        golIzquierda(p);
        for (int i = 0; i < 1300; i++) {
            p.actualizar(0.05);
        }
        assertEquals(EstadoPartido.FINALIZADO, p.getEstado());
        assertSame(p.getJugador(Lado.IZQUIERDA), p.getGanador());
    }

    @Test
    void siEmpatanPorTiempoHayMuerteSubita() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_TIEMPO, 1);
        for (int i = 0; i < 1300; i++) {
            p.actualizar(0.05);
        }
        assertEquals(EstadoPartido.EN_JUEGO, p.getEstado());
        assertTrue(p.enMuerteSubita());
        golDerecha(p);
        assertEquals(EstadoPartido.FINALIZADO, p.getEstado());
        assertSame(p.getJugador(Lado.DERECHA), p.getGanador());
    }

    @Test
    void enPausaNadaAvanza() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_TIEMPO, 1);
        p.pausar();
        double y = p.getBalon().getY();
        p.actualizar(1);
        assertEquals(EstadoPartido.PAUSADO, p.getEstado());
        assertEquals(60, p.getTiempoRestante());
        assertEquals(y, p.getBalon().getY());
        p.reanudar();
        assertEquals(EstadoPartido.EN_JUEGO, p.getEstado());
    }

    @Test
    void terminarManualSinGolesDejaEmpate() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        p.terminar();
        assertEquals(EstadoPartido.FINALIZADO, p.getEstado());
        assertNull(p.getGanador());
    }

    @Test
    void ganadorEsNuloMientrasSeJuega() throws HeadSoccerException {
        assertNull(crear(TipoPartido.POR_GOLES, 3).getGanador());
    }

    @Test
    void lasPateadasMandanElBalonHaciaElArcoRival() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        Jugador izq = p.getJugador(Lado.IZQUIERDA);
        double cerca = Jugador.RADIO + Balon.RADIO + 5;
        p.getBalon().setPosicion(izq.getCentroX() + cerca, izq.getCentroY());
        p.getBalon().setVelocidad(0, 0);
        izq.patear();
        p.actualizar(0.016);
        assertTrue(p.getBalon().getVx() > 0);
        assertTrue(p.getBalon().getVy() > 0);
        assertTrue(!izq.estaPateando());
    }

    @Test
    void elJugadorDerechoPateaHaciaLaIzquierda() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        Jugador der = p.getJugador(Lado.DERECHA);
        double cerca = Jugador.RADIO + Balon.RADIO + 5;
        p.getBalon().setPosicion(der.getCentroX() - cerca, der.getCentroY());
        p.getBalon().setVelocidad(0, 0);
        der.patear();
        p.actualizar(0.016);
        assertTrue(p.getBalon().getVx() < 0);
    }

    @Test
    void elJugadorEmpujaElBalonAlChocar() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        Jugador izq = p.getJugador(Lado.IZQUIERDA);
        p.getBalon().setPosicion(izq.getCentroX() + Jugador.RADIO + Balon.RADIO - 5, izq.getCentroY());
        p.getBalon().setVelocidad(-200, 0);
        p.actualizar(0.001);
        assertTrue(p.getBalon().getVx() > 0);
    }

    @Test
    void noSePuedeReanudarSiNoEstaEnPausa() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        PartidoException e = assertThrows(PartidoException.class, p::reanudar);
        assertEquals(PartidoException.NO_ESTA_PAUSADO, e.getMessage());
        assertEquals(EstadoPartido.EN_JUEGO, p.getEstado());
    }

    @Test
    void noSePuedePausarDosVeces() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        p.pausar();
        PartidoException e = assertThrows(PartidoException.class, p::pausar);
        assertEquals(PartidoException.NO_ESTA_EN_JUEGO, e.getMessage());
    }

    @Test
    void noSePuedeTerminarDosVeces() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        p.terminar();
        PartidoException e = assertThrows(PartidoException.class, p::terminar);
        assertEquals(PartidoException.YA_FINALIZADO, e.getMessage());
    }

    @Test
    void sePuedeTerminarDesdeLaPausa() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        p.pausar();
        p.terminar();
        assertEquals(EstadoPartido.FINALIZADO, p.getEstado());
    }

    @Test
    void terminadoNoSigueJugando() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_TIEMPO, 1);
        p.terminar();
        p.actualizar(5);
        assertEquals(60, p.getTiempoRestante());
    }

    @Test
    void losJugadoresNoSeAtraviesan() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        Jugador izq = p.getJugador(Lado.IZQUIERDA);
        Jugador der = p.getJugador(Lado.DERECHA);
        izq.mover(1);
        der.mover(-1);
        for (int i = 0; i < 300; i++) {
            p.actualizar(0.016);
        }
        assertTrue(der.getX() - izq.getX() >= 2 * Jugador.RADIO - 0.001);
        assertTrue(izq.getX() < der.getX());
    }

    @Test
    void contraLaParedElOtroNoLaAtraviesa() throws HeadSoccerException {
        Partido p = crear(TipoPartido.POR_GOLES, 3);
        Jugador izq = p.getJugador(Lado.IZQUIERDA);
        Jugador der = p.getJugador(Lado.DERECHA);
        der.reiniciar(Jugador.RADIO + 10);   // el derecho pegado a la pared izquierda
        izq.reiniciar(300);
        izq.mover(-1);                       // el izquierdo lo empuja contra la pared
        for (int i = 0; i < 300; i++) {
            p.actualizar(0.016);
        }
        assertEquals(Jugador.RADIO, der.getX(), 0.001);
        assertTrue(izq.getX() - der.getX() >= 2 * Jugador.RADIO - 0.001);
    }
}
