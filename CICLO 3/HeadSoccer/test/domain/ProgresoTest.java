package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProgresoTest {

    @Test
    void unJugadorNuevoNoTienePuntosNiMejoras() {
        Progreso p = new Progreso();
        assertFalse(p.existe("Ana"));
        assertEquals(0, p.getPuntos("Ana"));
        assertEquals(1, p.getEstadisticas("Ana").getNivel(Estadistica.KICK));
    }

    @Test
    void sumaPuntosSinImportarMayusculas() {
        Progreso p = new Progreso();
        p.sumarPuntos("Ana", 100);
        p.sumarPuntos(" ana ", 25);
        assertTrue(p.existe("ANA"));
        assertEquals(125, p.getPuntos("Ana"));
        assertEquals(1, p.getNombres().size());
    }

    @Test
    void elPrecioSeDuplicaEmpezandoEnQuinientos() throws ProgresoException {
        Progreso p = new Progreso();
        p.sumarPuntos("Ana", 3500);
        assertEquals(500, p.getPrecioMejora("Ana", Estadistica.SPEED));
        p.mejorar("Ana", Estadistica.SPEED);
        assertEquals(1000, p.getPrecioMejora("Ana", Estadistica.SPEED));
        p.mejorar("Ana", Estadistica.SPEED);
        assertEquals(2000, p.getPrecioMejora("Ana", Estadistica.SPEED));
        p.mejorar("Ana", Estadistica.SPEED);
        assertEquals(0, p.getPuntos("Ana"));
        assertEquals(4, p.getEstadisticas("Ana").getNivel(Estadistica.SPEED));
        // las otras estadisticas tienen su propio precio
        assertEquals(500, p.getPrecioMejora("Ana", Estadistica.KICK));
    }

    @Test
    void sinPuntosSuficientesNoMejora() {
        Progreso p = new Progreso();
        p.sumarPuntos("Ana", 499);
        ProgresoException e = assertThrows(ProgresoException.class, () -> p.mejorar("Ana", Estadistica.KICK));
        assertTrue(e.getMessage().startsWith(ProgresoException.PUNTOS_INSUFICIENTES));
        assertEquals(499, p.getPuntos("Ana"));
        assertEquals(1, p.getEstadisticas("Ana").getNivel(Estadistica.KICK));
    }

    @Test
    void lasEstadisticasQueEntregaSonUnaCopia() throws ProgresoException {
        Progreso p = new Progreso();
        p.sumarPuntos("Ana", 500);
        p.getEstadisticas("Ana").subir(Estadistica.KICK);
        assertEquals(1, p.getEstadisticas("Ana").getNivel(Estadistica.KICK));
    }
}
