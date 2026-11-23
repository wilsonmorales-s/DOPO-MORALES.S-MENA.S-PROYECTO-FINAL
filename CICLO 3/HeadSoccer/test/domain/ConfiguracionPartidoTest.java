package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ConfiguracionPartidoTest {

    @Test
    void creaConfiguracionValida() throws HeadSoccerException {
        ConfiguracionPartido c = new ConfiguracionPartido(TipoPartido.POR_GOLES, 5, " Ana ", "Luis");
        assertEquals(TipoPartido.POR_GOLES, c.getTipo());
        assertEquals(5, c.getLimite());
        assertEquals("Ana", c.getNombreIzquierdo());
    }

    @Test
    void rechazaTiempoFueraDeRango() {
        HeadSoccerException e = assertThrows(HeadSoccerException.class,
                () -> new ConfiguracionPartido(TipoPartido.POR_TIEMPO, 11, "A", "B"));
        assertEquals(HeadSoccerException.LIMITE_TIEMPO, e.getMessage());
    }

    @Test
    void rechazaGolesFueraDeRango() {
        HeadSoccerException e = assertThrows(HeadSoccerException.class,
                () -> new ConfiguracionPartido(TipoPartido.POR_GOLES, 0, "A", "B"));
        assertEquals(HeadSoccerException.LIMITE_GOLES, e.getMessage());
    }

    @Test
    void rechazaNombresVacios() {
        assertThrows(HeadSoccerException.class,
                () -> new ConfiguracionPartido(TipoPartido.POR_GOLES, 3, "  ", "B"));
        assertThrows(HeadSoccerException.class,
                () -> new ConfiguracionPartido(TipoPartido.POR_GOLES, 3, "A", null));
    }

    @Test
    void rechazaTipoNulo() {
        assertThrows(HeadSoccerException.class, () -> new ConfiguracionPartido(null, 3, "A", "B"));
    }

    @Test
    void rechazaNombresIguales() {
        HeadSoccerException e = assertThrows(HeadSoccerException.class,
                () -> new ConfiguracionPartido(TipoPartido.POR_GOLES, 3, "Ana", " ana "));
        assertEquals(HeadSoccerException.NOMBRES_IGUALES, e.getMessage());
    }

    @Test
    void aceptaLosLimitesExtremos() throws HeadSoccerException {
        assertEquals(1, new ConfiguracionPartido(TipoPartido.POR_TIEMPO, 1, "A", "B").getLimite());
        assertEquals(10, new ConfiguracionPartido(TipoPartido.POR_GOLES, 10, "A", "B").getLimite());
    }
}
