package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VidaTest {

    private final Campo campo = new Campo();

    private Estadisticas con(Estadistica estadistica, int nivel) throws ProgresoException {
        Estadisticas e = new Estadisticas();
        for (int i = 1; i < nivel; i++) {
            e.subir(estadistica);
        }
        return e;
    }

    @Test
    void empiezaConLaVidaAlMaximo() throws ProgresoException {
        Jugador j = new Jugador("Ana", Lado.IZQUIERDA, 250);
        j.setEstadisticas(con(Estadistica.SURVIVAL, 3));
        assertEquals(120, j.getVida(), 1e-9);
        assertEquals(120, j.getVidaMax(), 1e-9);
    }

    @Test
    void laVidaNuncaBajaDeCeroYQuedaAturdido() {
        Jugador j = new Jugador("Ana", Lado.IZQUIERDA, 250);
        j.recibirDano(250);
        assertEquals(0, j.getVida(), 1e-9);
        assertTrue(j.estaAturdido());
    }

    @Test
    void aturdidoNoSeMueveNiSaltaNiPateaDuranteTresSegundos() {
        Jugador j = new Jugador("Ana", Lado.IZQUIERDA, 250);
        j.recibirDano(100);
        j.mover(1);
        j.saltar();
        j.patear();
        for (int i = 0; i < 290; i++) {
            j.actualizar(0.01, campo);
        }
        assertEquals(250, j.getX(), 1e-9);
        assertEquals(0, j.getY(), 1e-9);
        assertFalse(j.estaPateando());
        assertTrue(j.estaAturdido());
        for (int i = 0; i < 20; i++) {
            j.actualizar(0.01, campo);
        }
        assertFalse(j.estaAturdido());
        assertEquals(j.getVidaMax(), j.getVida(), 1e-9); // la vida se reinicia
    }

    // Ejemplo del enunciado: Jugador 1 con Camerun y Kick 5; Jugador 2 con Survival 3.
    @Test
    void ejemploDelEnunciado() throws HeadSoccerException {
        Partido p = new Partido(new ConfiguracionPartido(Modalidad.JUGADOR_VS_JUGADOR, null,
                TipoPartido.POR_GOLES, 5, "Uno", "Dos", new Camerun(), new Nigeria(), Resolucion.MEDIANA));
        Jugador uno = p.getJugador(Lado.IZQUIERDA);
        Jugador dos = p.getJugador(Lado.DERECHA);
        uno.setEstadisticas(con(Estadistica.KICK, 5));
        dos.setEstadisticas(con(Estadistica.SURVIVAL, 3));
        assertEquals(120, dos.getVida(), 1e-9);
        assertEquals(1.4, uno.getMultiplicadorKick(), 1e-9);

        // Patada: 10 * 1.4 = 14 -> quedan 106
        dos.recibirDano(Jugador.DANO_PATADA * uno.getMultiplicadorKick());
        assertEquals(106, dos.getVida(), 1e-9);

        // Tiro de poder: 60 * 1.4 = 84 -> quedan 22 (sobre los 106) y queda electrocutado
        dos.recibirDano(uno.getPersonaje().getDanoTiro() * uno.getMultiplicadorKick());
        dos.aplicarEfecto(uno.getPersonaje().crearEfecto(), p);
        assertEquals(22, dos.getVida(), 1e-9);
        assertTrue(dos.getEfecto() instanceof Electrocutado);
        assertEquals(3, dos.getEfecto().getDuracion(), 1e-9);
    }

    @Test
    void laPatadaAlRivalLeQuitaVida() throws HeadSoccerException {
        Partido p = new Partido(new ConfiguracionPartido(TipoPartido.POR_GOLES, 5, "Ana", "Luis"));
        Jugador ana = p.getJugador(Lado.IZQUIERDA);
        Jugador luis = p.getJugador(Lado.DERECHA);
        p.getBalon().setPosicion(100, 400);       // el balon lejos, arriba
        luis.empujar(ana.getX() + 2 * Jugador.RADIO + 10 - luis.getX(), p.getCampo());
        ana.patear();
        p.actualizar(0.001);
        assertEquals(90, luis.getVida(), 1e-9);
        assertFalse(ana.estaPateando());          // la patada se gasto
    }

    @Test
    void laPatadaQueTocaElBalonNoLeHaceDanoAlRival() throws HeadSoccerException {
        Partido p = new Partido(new ConfiguracionPartido(TipoPartido.POR_GOLES, 5, "Ana", "Luis"));
        Jugador ana = p.getJugador(Lado.IZQUIERDA);
        Jugador luis = p.getJugador(Lado.DERECHA);
        luis.empujar(ana.getX() + 2 * Jugador.RADIO + 10 - luis.getX(), p.getCampo());
        // El balon detras de ana (lejos de luis) pero al alcance de su pie
        p.getBalon().setPosicion(ana.getCentroX() - Jugador.RADIO - 5, ana.getCentroY());
        p.getBalon().setVelocidad(0, 0);
        ana.patear();
        p.actualizar(0.001);
        assertEquals(100, luis.getVida(), 1e-9);
        assertTrue(p.getBalon().getVx() > 0);   // la patada lo mando hacia el arco rival
    }
}
