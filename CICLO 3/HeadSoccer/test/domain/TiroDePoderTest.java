package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TiroDePoderTest {

    private Partido crear(Personaje izquierdo, Personaje derecho) throws HeadSoccerException {
        return new Partido(new ConfiguracionPartido(Modalidad.JUGADOR_VS_JUGADOR, null, TipoPartido.POR_GOLES,
                5, "Ana", "Luis", izquierdo, derecho, Resolucion.MEDIANA));
    }

    // Llena la barra sin mover el partido y activa el poder.
    private void darAura(Partido p, Jugador j) {
        for (int i = 0; i < 1000; i++) {
            j.actualizar(0.01, p.getCampo());
        }
        j.activarPoder();
    }

    // Pone el balon pegado a la cabeza del jugador, del lado del rival.
    private void balonDelante(Partido p, Jugador j) {
        p.getBalon().setPosicion(j.getCentroX() + Jugador.RADIO + Balon.RADIO - 5, j.getCentroY());
        p.getBalon().setVelocidad(0, 0);
    }

    // Avanza el partido hasta que el rival reciba un efecto (maximo 2 segundos).
    private void hastaQueGolpee(Partido p, Jugador rival) {
        for (int i = 0; i < 200 && rival.getEfecto() == null; i++) {
            p.actualizar(0.01);
        }
    }

    @Test
    void alTocarElBalonConAuraSaleElTiroYElAuraDesaparece() throws HeadSoccerException {
        Partido p = crear(new CoreaDelSur(), new Camerun());
        Jugador ana = p.getJugador(Lado.IZQUIERDA);
        darAura(p, ana);
        balonDelante(p, ana);
        p.actualizar(0.001);
        assertFalse(ana.tieneAura());
        assertTrue(p.getBalon().esTiroDePoder());
        assertEquals(Lado.IZQUIERDA, p.getBalon().getLanzador());
        assertEquals(CoreaDelSur.VELOCIDAD_TIRO, p.getBalon().getVx(), 1e-9);
    }

    @Test
    void auraAzulGolpeaYEmpujaAlRivalHaciaSuArco() throws HeadSoccerException {
        Partido p = crear(new CoreaDelSur(), new Camerun());
        Jugador ana = p.getJugador(Lado.IZQUIERDA);
        Jugador luis = p.getJugador(Lado.DERECHA);
        darAura(p, ana);
        balonDelante(p, ana);
        hastaQueGolpee(p, luis);
        assertTrue(luis.getEfecto() instanceof Empuje);
        assertEquals(100 - 40, luis.getVida(), 1e-9);
        assertFalse(p.getBalon().esTiroDePoder());
        double antes = luis.getX();
        p.actualizar(0.1);
        assertTrue(luis.getX() > antes);   // su arco esta a la derecha
    }

    @Test
    void rayoElectrocuta() throws HeadSoccerException {
        Partido p = crear(new Camerun(), new CoreaDelSur());
        Jugador ana = p.getJugador(Lado.IZQUIERDA);
        Jugador luis = p.getJugador(Lado.DERECHA);
        darAura(p, ana);
        balonDelante(p, ana);
        hastaQueGolpee(p, luis);
        assertTrue(luis.getEfecto() instanceof Electrocutado);
        assertEquals(40, luis.getVida(), 1e-9);
        assertEquals(Jugador.VELOCIDAD * 0.5, luis.getVelocidad(), 1e-9);
        luis.saltar();
        assertEquals(0, luis.getVy(), 1e-9);
    }

    @Test
    void tornadoDejaInconscienteYLoLanzaAlAire() throws HeadSoccerException {
        Partido p = crear(new Nigeria(), new CoreaDelSur());
        Jugador ana = p.getJugador(Lado.IZQUIERDA);
        Jugador luis = p.getJugador(Lado.DERECHA);
        darAura(p, ana);
        balonDelante(p, ana);
        hastaQueGolpee(p, luis);
        assertTrue(luis.getEfecto() instanceof Inconsciente);
        assertTrue(luis.getVy() > 0 || luis.getY() > 0);
        assertFalse(luis.puedeMoverse());
        assertFalse(luis.puedeSaltar());
        assertFalse(luis.puedePatear());
    }

    @Test
    void ilusionDivideElBalonEnOchoYLosFalsosDesaparecen() throws HeadSoccerException {
        Partido p = crear(new EstadosUnidos(), new CoreaDelSur());
        Jugador ana = p.getJugador(Lado.IZQUIERDA);
        darAura(p, ana);
        balonDelante(p, ana);
        p.actualizar(0.001);
        assertEquals(7, p.getBalonesFalsos().size());  // 7 falsos + el real = 8
        for (int i = 0; i < 300; i++) {
            p.actualizar(0.01);
        }
        assertTrue(p.getBalonesFalsos().isEmpty());
    }

    @Test
    void elTiroQueEntraSinTocarAlRivalEsGolSinDano() throws HeadSoccerException {
        Partido p = crear(new CoreaDelSur(), new Camerun());
        Jugador ana = p.getJugador(Lado.IZQUIERDA);
        Jugador luis = p.getJugador(Lado.DERECHA);
        luis.empujar(-1000, p.getCampo());  // el rival se quita del camino (queda detras de ana)
        ana.empujar(400, p.getCampo());
        darAura(p, ana);
        balonDelante(p, ana);
        for (int i = 0; i < 200 && p.getMarcador().getGoles(Lado.IZQUIERDA) == 0; i++) {
            p.actualizar(0.01);
        }
        assertEquals(1, p.getMarcador().getGoles(Lado.IZQUIERDA));
        assertEquals(100, luis.getVida(), 1e-9);
        assertNull(luis.getEfecto());
    }

    @Test
    void losEfectosNoSeAcumulanElNuevoReemplazaAlAnterior() throws HeadSoccerException {
        Partido p = crear(new CoreaDelSur(), new Camerun());
        Jugador luis = p.getJugador(Lado.DERECHA);
        luis.aplicarEfecto(new Electrocutado(), p);
        luis.aplicarEfecto(new Inconsciente(), p);
        assertTrue(luis.getEfecto() instanceof Inconsciente);
        assertEquals(Jugador.VELOCIDAD, luis.getVelocidad(), 1e-9); // ya no esta al 50 %
    }

    @Test
    void elDanoDelTiroUsaElMultiplicadorKick() throws HeadSoccerException {
        Estadisticas fuerte = new Estadisticas();
        for (int i = 0; i < 4; i++) {
            fuerte.subir(Estadistica.KICK);   // kick 5 -> 1.4
        }
        Partido p = new Partido(new ConfiguracionPartido(Modalidad.JUGADOR_VS_JUGADOR, null,
                TipoPartido.POR_GOLES, 5, "Ana", "Luis", new EstadosUnidos(), new Camerun(), Resolucion.MEDIANA));
        Jugador ana = p.getJugador(Lado.IZQUIERDA);
        ana.setEstadisticas(fuerte);
        Jugador luis = p.getJugador(Lado.DERECHA);
        luis.empujar(ana.getX() + 200 - luis.getX(), p.getCampo()); // cerca para que el tiro lo alcance
        darAura(p, ana);
        balonDelante(p, ana);
        hastaQueGolpee(p, luis);
        assertEquals(100 - 30 * 1.4, luis.getVida(), 1e-9);
        assertTrue(luis.getEfecto() instanceof Confusion);
    }

    @Test
    void recibirUnGolSubeLaBarraDelQueLoRecibe() throws HeadSoccerException {
        Partido p = crear(new CoreaDelSur(), new Camerun());
        p.getBalon().setPosicion(p.getCampo().getAncho() + 5, 50);
        p.actualizar(0.001);
        assertEquals(0.25, p.getJugador(Lado.DERECHA).getBarraPoder(), 0.01);
        assertTrue(p.getJugador(Lado.IZQUIERDA).getBarraPoder() < 0.01);
    }
}
