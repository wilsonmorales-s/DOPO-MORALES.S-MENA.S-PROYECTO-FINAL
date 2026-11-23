package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EstadisticasTest {

    private Estadisticas conNivel(Estadistica estadistica, int nivel) throws ProgresoException {
        Estadisticas e = new Estadisticas();
        for (int i = 1; i < nivel; i++) {
            e.subir(estadistica);
        }
        return e;
    }

    @Test
    void todasEmpiezanEnUno() {
        Estadisticas e = new Estadisticas();
        for (Estadistica estadistica : Estadistica.values()) {
            assertEquals(1, e.getNivel(estadistica));
            assertEquals(1.0, e.getFactor(estadistica), 1e-9);
        }
        assertEquals(100, e.getVidaMax(), 1e-9);
        assertEquals(1.0, e.getMultiplicadorKick(), 1e-9);
    }

    @Test
    void elMultiplicadorKickSigueLaTablaDelEnunciado() throws ProgresoException {
        double[] esperado = {1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9};
        for (int nivel = 1; nivel <= 10; nivel++) {
            assertEquals(esperado[nivel - 1], conNivel(Estadistica.KICK, nivel).getMultiplicadorKick(), 1e-9);
        }
    }

    @Test
    void laVidaMaximaSigueLaTablaDelEnunciado() throws ProgresoException {
        for (int nivel = 1; nivel <= 10; nivel++) {
            assertEquals(100 + 10 * (nivel - 1), conNivel(Estadistica.SURVIVAL, nivel).getVidaMax(), 1e-9);
        }
    }

    @Test
    void noPasaDeDiez() throws ProgresoException {
        Estadisticas e = conNivel(Estadistica.SPEED, 10);
        ProgresoException ex = assertThrows(ProgresoException.class, () -> e.subir(Estadistica.SPEED));
        assertEquals(ProgresoException.NIVEL_MAXIMO, ex.getMessage());
        assertEquals(10, e.getNivel(Estadistica.SPEED));
    }

    @Test
    void laCopiaEsIndependiente() throws ProgresoException {
        Estadisticas e = conNivel(Estadistica.JUMP, 3);
        Estadisticas copia = e.copia();
        assertNotSame(e, copia);
        assertEquals(3, copia.getNivel(Estadistica.JUMP));
        copia.subir(Estadistica.JUMP);
        assertEquals(3, e.getNivel(Estadistica.JUMP));
    }

    @Test
    void speedYJumpCambianElMovimientoDelJugador() throws ProgresoException {
        Jugador base = new Jugador("A", Lado.IZQUIERDA, 300);
        Jugador rapido = new Jugador("B", Lado.IZQUIERDA, 300);
        rapido.setEstadisticas(conNivel(Estadistica.SPEED, 6));
        assertEquals(base.getVelocidad() * 1.5, rapido.getVelocidad(), 1e-9);

        Jugador saltarin = new Jugador("C", Lado.IZQUIERDA, 300);
        saltarin.setEstadisticas(conNivel(Estadistica.JUMP, 6));
        // La altura maxima del salto es v^2 / (2g): con jump 6 debe ser un 50 % mayor
        double alturaBase = Math.pow(base.getVelocidadSalto(), 2) / (2 * Jugador.GRAVEDAD);
        double alturaMejor = Math.pow(saltarin.getVelocidadSalto(), 2) / (2 * Jugador.GRAVEDAD);
        assertEquals(alturaBase * 1.5, alturaMejor, 1e-6);
    }
}
