package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConfiguracionVersionTresTest {

    private void falla(String mensaje, Modalidad modalidad, PerfilMaquina perfil, Personaje p1, Personaje p2,
            Resolucion resolucion) {
        HeadSoccerException e = assertThrows(HeadSoccerException.class, () -> new ConfiguracionPartido(
                modalidad, perfil, TipoPartido.POR_GOLES, 3, "Ana", "Luis", p1, p2, resolucion));
        assertEquals(mensaje, e.getMessage());
    }

    @Test
    void validaModalidadPerfilPersonajesYResolucion() {
        falla(HeadSoccerException.MODALIDAD_NULA, null, null, new Nigeria(), new Camerun(), Resolucion.MEDIANA);
        falla(HeadSoccerException.PERFIL_NULO, Modalidad.JUGADOR_VS_MAQUINA, null, new Nigeria(), new Camerun(),
                Resolucion.MEDIANA);
        falla(HeadSoccerException.PERSONAJE_NULO, Modalidad.JUGADOR_VS_JUGADOR, null, null, new Camerun(),
                Resolucion.MEDIANA);
        falla(HeadSoccerException.PERSONAJE_NULO, Modalidad.JUGADOR_VS_JUGADOR, null, new Nigeria(), null,
                Resolucion.MEDIANA);
        falla(HeadSoccerException.RESOLUCION_NULA, Modalidad.JUGADOR_VS_JUGADOR, null, new Nigeria(),
                new Camerun(), null);
    }

    @Test
    void laConfiguracionDeLaVersionUnoSigueFuncionando() throws HeadSoccerException {
        ConfiguracionPartido c = new ConfiguracionPartido(TipoPartido.POR_GOLES, 3, "Ana", "Luis");
        assertEquals(Modalidad.JUGADOR_VS_JUGADOR, c.getModalidad());
        assertEquals(Resolucion.MEDIANA, c.getResolucion());
        assertTrue(c.getPersonajeIzquierdo() instanceof CoreaDelSur);
    }

    @Test
    void laCanchaTieneElTamanoDeLaResolucion() throws HeadSoccerException {
        for (Resolucion r : Resolucion.values()) {
            Partido p = new Partido(new ConfiguracionPartido(Modalidad.JUGADOR_VS_JUGADOR, null,
                    TipoPartido.POR_GOLES, 3, "Ana", "Luis", new Nigeria(), new Camerun(), r));
            assertEquals(r.getAncho(), p.getCampo().getAncho(), 1e-9);
            assertEquals(r.getAlto(), p.getCampo().getAlto(), 1e-9);
            assertEquals(r.getAncho() * 0.75, p.getJugador(Lado.DERECHA).getX(), 1e-9);
            assertEquals(r.getAncho() / 2, p.getBalon().getX(), 1e-9);
        }
    }

    @Test
    void hayCuatroPersonajesConSusDatosDelAnexo() {
        assertEquals(4, Personaje.disponibles().size());
        assertEquals(40, new CoreaDelSur().getDanoTiro(), 1e-9);
        assertEquals(60, new Camerun().getDanoTiro(), 1e-9);
        assertEquals(40, new Nigeria().getDanoTiro(), 1e-9);
        assertEquals(30, new EstadosUnidos().getDanoTiro(), 1e-9);
        assertEquals(1.5, new CoreaDelSur().crearEfecto().getDuracion(), 1e-9);
        assertEquals(3, new Camerun().crearEfecto().getDuracion(), 1e-9);
        assertEquals(3, new Nigeria().crearEfecto().getDuracion(), 1e-9);
        assertEquals(2.5, new EstadosUnidos().crearEfecto().getDuracion(), 1e-9);
    }
}
