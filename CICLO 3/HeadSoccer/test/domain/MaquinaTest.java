package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class MaquinaTest {

    private ConfiguracionPartido contra(PerfilMaquina perfil) throws HeadSoccerException {
        return new ConfiguracionPartido(Modalidad.JUGADOR_VS_MAQUINA, perfil, TipoPartido.POR_GOLES, 3,
                "Ana", "", new CoreaDelSur(), new Nigeria(), Resolucion.MEDIANA);
    }

    // Juega hasta "segundos" con el jugador humano quieto y devuelve los goles de la maquina.
    private int golesContraJugadorQuieto(PerfilMaquina perfil, double segundos) throws HeadSoccerException {
        Partido p = new Partido(contra(perfil));
        for (int i = 0; i < segundos * 60 && p.getEstado() == EstadoPartido.EN_JUEGO; i++) {
            p.actualizar(1.0 / 60);
        }
        return p.getMarcador().getGoles(Lado.DERECHA);
    }

    @Test
    void contraLaMaquinaElJugadorDosEsLaMaquina() throws HeadSoccerException {
        PerfilMaquina experta = new MaquinaExperta();
        ConfiguracionPartido c = contra(experta);
        assertSame(experta, c.getPerfilMaquina());
        assertEquals("Maquina Experta", c.getNombreDerecho());
        Partido p = new Partido(c);
        assertTrue(p.getJugador(Lado.DERECHA).esMaquina());
        assertFalse(p.getJugador(Lado.IZQUIERDA).esMaquina());
    }

    @Test
    void jugadorContraJugadorNoTieneMaquina() throws HeadSoccerException {
        ConfiguracionPartido c = new ConfiguracionPartido(Modalidad.JUGADOR_VS_JUGADOR, new MaquinaTorpe(),
                TipoPartido.POR_GOLES, 3, "Ana", "Luis", new CoreaDelSur(), new Nigeria(), Resolucion.MEDIANA);
        assertNull(c.getPerfilMaquina());
        assertFalse(new Partido(c).getJugador(Lado.DERECHA).esMaquina());
    }

    @Test
    void laDificultadCambiaComoJuegaLaMaquina() throws HeadSoccerException {
        MaquinaOfensiva maquina = new MaquinaOfensiva();
        assertEquals(Dificultad.MEDIA, maquina.getDificultad());
        maquina.setDificultad(Dificultad.BAJA);
        double reaccionBaja = maquina.getTiempoReaccion();
        double errorBajo = maquina.getImprecision();
        double patadaBaja = maquina.getProbabilidadPatada();
        maquina.setDificultad(Dificultad.ALTA);
        assertTrue(maquina.getTiempoReaccion() < reaccionBaja);    // piensa mas rapido
        assertTrue(maquina.getImprecision() < errorBajo);          // se equivoca menos
        assertTrue(maquina.getProbabilidadPatada() > patadaBaja);  // patea mas
    }

    @Test
    void conLaMismaDificultadLaExpertaEsMejorQueLaOfensiva() throws HeadSoccerException {
        MaquinaOfensiva ofensiva = new MaquinaOfensiva();
        MaquinaExperta experta = new MaquinaExperta();
        ofensiva.setDificultad(Dificultad.ALTA);
        experta.setDificultad(Dificultad.ALTA);
        assertTrue(experta.getTiempoReaccion() < ofensiva.getTiempoReaccion());
        assertTrue(experta.getImprecision() < ofensiva.getImprecision());
    }

    @Test
    void noAceptaDificultadNula() {
        HeadSoccerException e = assertThrows(HeadSoccerException.class,
                () -> new MaquinaExperta().setDificultad(null));
        assertEquals(HeadSoccerException.DIFICULTAD_NULA, e.getMessage());
    }

    @Test
    void laDificultadSeGuardaConLaPartida() throws HeadSoccerException {
        MaquinaExperta experta = new MaquinaExperta();
        experta.setDificultad(Dificultad.BAJA);
        Partido p = new Partido(contra(experta));
        assertEquals(Dificultad.BAJA, p.getJugador(Lado.DERECHA).getMaquina().getDificultad());
    }

    @Test
    void hayCuatroPerfiles() {
        assertEquals(4, PerfilMaquina.disponibles().size());
    }

    @Test
    void laTorpeSeMueveAlAzar() throws HeadSoccerException {
        Partido p = new Partido(contra(new MaquinaTorpe(new Random(7))));
        Jugador maquina = p.getJugador(Lado.DERECHA);
        double inicio = maquina.getX();
        boolean semovio = false;
        for (int i = 0; i < 300; i++) {
            p.actualizar(1.0 / 60);
            if (Math.abs(maquina.getX() - inicio) > 1) {
                semovio = true;
            }
        }
        assertTrue(semovio);
    }

    @Test
    void laOfensivaVaPorElBalon() throws HeadSoccerException {
        MaquinaOfensiva ofensiva = new MaquinaOfensiva();
        ofensiva.setDificultad(Dificultad.ALTA);
        Partido p = new Partido(contra(ofensiva));
        Jugador maquina = p.getJugador(Lado.DERECHA);
        double antes = Math.abs(maquina.getX() - p.getBalon().getX());
        for (int i = 0; i < 30; i++) {
            p.actualizar(1.0 / 60);
        }
        assertTrue(Math.abs(maquina.getX() - p.getBalon().getX()) < antes);
    }

    @Test
    void laDefensivaSeQuedaCercaDeSuArcoSiElBalonEstaLejos() throws HeadSoccerException {
        MaquinaDefensiva defensiva = new MaquinaDefensiva();
        defensiva.setDificultad(Dificultad.ALTA);   // sin distracciones
        Partido p = new Partido(contra(defensiva));
        Jugador maquina = p.getJugador(Lado.DERECHA);
        p.getBalon().setPosicion(100, Balon.RADIO);   // balon en la mitad del rival
        for (int i = 0; i < 120; i++) {
            p.getBalon().setPosicion(100, Balon.RADIO);
            p.getBalon().setVelocidad(0, 0);
            p.actualizar(1.0 / 60);
        }
        assertEquals(p.getCampo().getAncho() - 140, maquina.getX(), PerfilMaquina.MARGEN + 10);
    }

    @Test
    void enDificultadAltaLaOfensivaYLaExpertaLeAnotanAUnJugadorQuieto() throws HeadSoccerException {
        MaquinaOfensiva ofensiva = new MaquinaOfensiva();
        ofensiva.setDificultad(Dificultad.ALTA);
        MaquinaExperta experta = new MaquinaExperta();
        experta.setDificultad(Dificultad.ALTA);
        assertTrue(golesContraJugadorQuieto(ofensiva, 120) >= 1);
        assertTrue(golesContraJugadorQuieto(experta, 120) >= 1);
    }

    @Test
    void laMaquinaUsaSuPoder() throws HeadSoccerException {
        // Por tiempo, para que el partido no se acabe por goles antes de llenar la barra
        MaquinaOfensiva ofensiva = new MaquinaOfensiva();
        ofensiva.setDificultad(Dificultad.ALTA);
        Partido p = new Partido(new ConfiguracionPartido(Modalidad.JUGADOR_VS_MAQUINA, ofensiva,
                TipoPartido.POR_TIEMPO, 3, "Ana", "", new CoreaDelSur(), new Nigeria(), Resolucion.MEDIANA));
        Jugador maquina = p.getJugador(Lado.DERECHA);
        boolean uso = false;
        for (int i = 0; i < 60 * 15 && !uso; i++) {
            p.actualizar(1.0 / 60);
            // puede activar el aura y lanzar el tiro en el mismo cuadro
            boolean tiroSuyo = p.getBalon().esTiroDePoder() && p.getBalon().getLanzador() == Lado.DERECHA;
            if (maquina.tieneAura() || tiroSuyo) {
                uso = true;
            }
        }
        assertTrue(uso);
    }

    @Test
    void laExpertaDefiendeCuandoElBalonEstaEnSuMitad() throws HeadSoccerException {
        MaquinaExperta experta = new MaquinaExperta();
        experta.setDificultad(Dificultad.ALTA);
        Partido p = new Partido(contra(experta));
        Jugador maquina = p.getJugador(Lado.DERECHA);
        double x = p.getCampo().getAncho() - 200;
        for (int i = 0; i < 90; i++) {
            p.getBalon().setPosicion(x, Balon.RADIO);
            p.getBalon().setVelocidad(0, 0);
            p.actualizar(1.0 / 60);
        }
        // Se pone entre el balon y su arco (a la derecha del balon)
        assertTrue(maquina.getX() > x);
        assertNotEquals(p.getCampo().getAncho() * 0.75, maquina.getX(), 1);
    }
}
