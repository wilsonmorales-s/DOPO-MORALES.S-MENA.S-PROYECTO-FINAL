package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HeadSoccerTest {

    private HeadSoccer iniciado() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        juego.iniciarPartido(new ConfiguracionPartido(TipoPartido.POR_GOLES, 3, "Ana", "Luis"));
        return juego;
    }

    @Test
    void alInicioNoHayPartido() {
        assertFalse(new HeadSoccer().hayPartido());
    }

    @Test
    void sinPartidoLasAccionesLanzanSinPartido() {
        HeadSoccer juego = new HeadSoccer();
        HeadSoccerException e;
        e = assertThrows(HeadSoccerException.class, () -> juego.actualizar(0.1));
        assertEquals(HeadSoccerException.SIN_PARTIDO, e.getMessage());
        e = assertThrows(HeadSoccerException.class, () -> juego.moverJugador(Lado.IZQUIERDA, 1));
        assertEquals(HeadSoccerException.SIN_PARTIDO, e.getMessage());
        e = assertThrows(HeadSoccerException.class, () -> juego.saltar(Lado.DERECHA));
        assertEquals(HeadSoccerException.SIN_PARTIDO, e.getMessage());
        e = assertThrows(HeadSoccerException.class, () -> juego.patear(Lado.DERECHA));
        assertEquals(HeadSoccerException.SIN_PARTIDO, e.getMessage());
        e = assertThrows(HeadSoccerException.class, juego::pausar);
        assertEquals(HeadSoccerException.SIN_PARTIDO, e.getMessage());
        e = assertThrows(HeadSoccerException.class, juego::reanudar);
        assertEquals(HeadSoccerException.SIN_PARTIDO, e.getMessage());
        e = assertThrows(HeadSoccerException.class, juego::terminar);
        assertEquals(HeadSoccerException.SIN_PARTIDO, e.getMessage());
        e = assertThrows(HeadSoccerException.class, juego::revancha);
        assertEquals(HeadSoccerException.SIN_PARTIDO, e.getMessage());
        assertFalse(juego.hayPartido());
    }

    @Test
    void noIniciaConConfiguracionNula() {
        HeadSoccerException e = assertThrows(HeadSoccerException.class,
                () -> new HeadSoccer().iniciarPartido(null));
        assertEquals(HeadSoccerException.CONFIGURACION_NULA, e.getMessage());
    }

    @Test
    void noAceptaAccionesSinLado() throws HeadSoccerException {
        HeadSoccer juego = iniciado();
        HeadSoccerException e;
        e = assertThrows(HeadSoccerException.class, () -> juego.moverJugador(null, 1));
        assertEquals(HeadSoccerException.LADO_NULO, e.getMessage());
        e = assertThrows(HeadSoccerException.class, () -> juego.saltar(null));
        assertEquals(HeadSoccerException.LADO_NULO, e.getMessage());
        e = assertThrows(HeadSoccerException.class, () -> juego.patear(null));
        assertEquals(HeadSoccerException.LADO_NULO, e.getMessage());
    }

    @Test
    void noPausaUnPartidoTerminado() throws HeadSoccerException {
        HeadSoccer juego = iniciado();
        juego.terminar();
        PartidoException e = assertThrows(PartidoException.class, juego::pausar);
        assertEquals(PartidoException.NO_ESTA_EN_JUEGO, e.getMessage());
    }

    @Test
    void revanchaEmpiezaDeCeroConLaMismaConfiguracion() throws HeadSoccerException {
        HeadSoccer juego = iniciado();
        Partido anterior = juego.getPartido();
        anterior.getMarcador().sumarGol(Lado.IZQUIERDA);
        juego.terminar();
        juego.revancha();
        Partido nuevo = juego.getPartido();
        assertNotSame(anterior, nuevo);
        assertSame(anterior.getConfiguracion(), nuevo.getConfiguracion());
        assertEquals(EstadoPartido.EN_JUEGO, nuevo.getEstado());
        assertEquals(0, nuevo.getMarcador().getGoles(Lado.IZQUIERDA));
    }

    @Test
    void iniciarCreaElPartido() throws HeadSoccerException {
        assertTrue(iniciado().hayPartido());
    }

    @Test
    void moverJugadorCambiaSuPosicion() throws HeadSoccerException {
        HeadSoccer juego = iniciado();
        double x = juego.getPartido().getJugador(Lado.IZQUIERDA).getX();
        juego.moverJugador(Lado.IZQUIERDA, 1);
        juego.actualizar(0.1);
        assertTrue(juego.getPartido().getJugador(Lado.IZQUIERDA).getX() > x);
    }

    @Test
    void saltarYPatearActuanSobreElJugadorIndicado() throws HeadSoccerException {
        HeadSoccer juego = iniciado();
        juego.saltar(Lado.DERECHA);
        juego.patear(Lado.DERECHA);
        juego.actualizar(0.016);
        assertFalse(juego.getPartido().getJugador(Lado.DERECHA).estaEnSuelo());
        assertTrue(juego.getPartido().getJugador(Lado.DERECHA).estaPateando());
        assertTrue(juego.getPartido().getJugador(Lado.IZQUIERDA).estaEnSuelo());
    }

    @Test
    void pausarReanudarYTerminar() throws HeadSoccerException {
        HeadSoccer juego = iniciado();
        juego.pausar();
        assertEquals(EstadoPartido.PAUSADO, juego.getPartido().getEstado());
        juego.reanudar();
        assertEquals(EstadoPartido.EN_JUEGO, juego.getPartido().getEstado());
        juego.terminar();
        assertEquals(EstadoPartido.FINALIZADO, juego.getPartido().getEstado());
    }
}
