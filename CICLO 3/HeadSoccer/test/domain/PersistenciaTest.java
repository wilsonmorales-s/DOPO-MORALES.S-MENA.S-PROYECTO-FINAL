package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PersistenciaTest {

    @TempDir
    Path carpeta;

    private ConfiguracionPartido configuracion() throws HeadSoccerException {
        return new ConfiguracionPartido(Modalidad.JUGADOR_VS_MAQUINA, new MaquinaExperta(),
                TipoPartido.POR_TIEMPO, 2, "Ana", "", new Nigeria(), new EstadosUnidos(), Resolucion.GRANDE);
    }

    @Test
    void guardaYAbreElEstadoDelPartido() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        juego.iniciarPartido(configuracion());
        Partido p = juego.getPartido();
        p.getMarcador().sumarGol(Lado.IZQUIERDA);
        p.getJugador(Lado.IZQUIERDA).recibirDano(30);
        for (int i = 0; i < 60; i++) {
            juego.actualizar(1.0 / 60);
        }
        double tiempo = p.getTiempoRestante();
        double xBalon = p.getBalon().getX();
        File archivo = carpeta.resolve("partida.hsp").toFile();
        juego.guardar(archivo);

        HeadSoccer otro = new HeadSoccer();
        otro.abrir(archivo);
        Partido abierto = otro.getPartido();
        assertEquals(1, abierto.getMarcador().getGoles(Lado.IZQUIERDA));
        assertEquals(70, abierto.getJugador(Lado.IZQUIERDA).getVida(), 1e-9);
        assertEquals(tiempo, abierto.getTiempoRestante(), 1e-9);
        assertEquals(xBalon, abierto.getBalon().getX(), 1e-9);
        assertEquals(Resolucion.GRANDE, abierto.getCampo().getResolucion());
        assertTrue(abierto.getJugador(Lado.DERECHA).getMaquina() instanceof MaquinaExperta);
        assertTrue(abierto.getJugador(Lado.IZQUIERDA).getPersonaje() instanceof Nigeria);
    }

    @Test
    void laPartidaAbiertaQuedaEnPausaYSePuedeSeguir() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        juego.iniciarPartido(configuracion());
        File archivo = carpeta.resolve("partida.hsp").toFile();
        juego.guardar(archivo);
        HeadSoccer otro = new HeadSoccer();
        otro.abrir(archivo);
        assertEquals(EstadoPartido.PAUSADO, otro.getPartido().getEstado());
        otro.reanudar();
        otro.actualizar(0.5);
        assertEquals(119.5, otro.getPartido().getTiempoRestante(), 1e-6);
    }

    @Test
    void erroresAlGuardar() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        HeadSoccerException e;
        e = assertThrows(HeadSoccerException.class, () -> juego.guardar(carpeta.resolve("x.hsp").toFile()));
        assertEquals(HeadSoccerException.SIN_PARTIDO, e.getMessage());
        juego.iniciarPartido(configuracion());
        e = assertThrows(HeadSoccerException.class, () -> juego.guardar(null));
        assertEquals(HeadSoccerException.ARCHIVO_NULO, e.getMessage());
        File enCarpetaQueNoExiste = carpeta.resolve("no/existe/x.hsp").toFile();
        e = assertThrows(HeadSoccerException.class, () -> juego.guardar(enCarpetaQueNoExiste));
        assertTrue(e.getMessage().startsWith(HeadSoccerException.ERROR_GUARDAR));
    }

    @Test
    void erroresAlAbrir() throws IOException {
        HeadSoccer juego = new HeadSoccer();
        HeadSoccerException e;
        e = assertThrows(HeadSoccerException.class, () -> juego.abrir(null));
        assertEquals(HeadSoccerException.ARCHIVO_NULO, e.getMessage());
        e = assertThrows(HeadSoccerException.class, () -> juego.abrir(carpeta.resolve("nada.hsp").toFile()));
        assertTrue(e.getMessage().startsWith(HeadSoccerException.ARCHIVO_NO_EXISTE));

        File texto = carpeta.resolve("texto.hsp").toFile();
        try (FileWriter w = new FileWriter(texto)) {
            w.write("esto no es una partida");
        }
        e = assertThrows(HeadSoccerException.class, () -> juego.abrir(texto));
        assertTrue(e.getMessage().startsWith(HeadSoccerException.ERROR_ABRIR));
        assertTrue(!juego.hayPartido());
    }

    @Test
    void alTerminarSeRegistranLosPuntosUnaSolaVez() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        juego.iniciarPartido(new ConfiguracionPartido(TipoPartido.POR_GOLES, 1, "Ana", "Luis"));
        Partido p = juego.getPartido();
        p.getBalon().setPosicion(p.getCampo().getAncho() + 5, 50);
        juego.actualizar(0.001);
        juego.registrarResultado();
        assertEquals(EstadoPartido.FINALIZADO, p.getEstado());
        assertEquals(100, p.getPuntosGanados(Lado.IZQUIERDA));
        assertEquals(25, p.getPuntosGanados(Lado.DERECHA));
        juego.registrarResultado();
        juego.registrarResultado();
        assertEquals(100, juego.getPuntos("Ana"));
        assertEquals(25, juego.getPuntos("Luis"));
    }

    @Test
    void terminarEmpatadosDaVeinticincoACadaUno() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        juego.iniciarPartido(new ConfiguracionPartido(TipoPartido.POR_GOLES, 3, "Ana", "Luis"));
        juego.terminar();
        juego.registrarResultado();
        assertEquals(25, juego.getPuntos("Ana"));
        assertEquals(25, juego.getPuntos("Luis"));
    }

    @Test
    void elProgresoSeGuardaEntreSesionesYLasMejorasAplicanAlSiguientePartido() throws HeadSoccerException {
        File progreso = carpeta.resolve("progreso.dat").toFile();
        HeadSoccer juego = new HeadSoccer(progreso);
        for (int i = 0; i < 5; i++) {     // 5 victorias = 500 puntos
            juego.iniciarPartido(new ConfiguracionPartido(TipoPartido.POR_GOLES, 1, "Ana", "Luis"));
            Partido p = juego.getPartido();
            p.getBalon().setPosicion(p.getCampo().getAncho() + 5, 50);
            juego.actualizar(0.001);
            juego.registrarResultado();
        }
        assertEquals(500, juego.getPuntos("Ana"));
        assertTrue(progreso.exists());

        HeadSoccer otraSesion = new HeadSoccer(progreso);
        assertEquals(500, otraSesion.getPuntos("Ana"));
        assertEquals(125, otraSesion.getPuntos("Luis"));
        otraSesion.mejorarEstadistica("Ana", Estadistica.SURVIVAL);
        assertEquals(0, otraSesion.getPuntos("Ana"));
        assertEquals(1000, otraSesion.getPrecioMejora("Ana", Estadistica.SURVIVAL));

        HeadSoccer tercera = new HeadSoccer(progreso);
        assertEquals(2, tercera.getEstadisticas("Ana").getNivel(Estadistica.SURVIVAL));
        tercera.iniciarPartido(new ConfiguracionPartido(TipoPartido.POR_GOLES, 3, "Ana", "Luis"));
        assertEquals(110, tercera.getPartido().getJugador(Lado.IZQUIERDA).getVida(), 1e-9);
    }

    @Test
    void erroresAlMejorar() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        HeadSoccerException e;
        e = assertThrows(HeadSoccerException.class, () -> juego.mejorarEstadistica(" ", Estadistica.KICK));
        assertEquals(HeadSoccerException.NOMBRE_VACIO, e.getMessage());
        e = assertThrows(HeadSoccerException.class, () -> juego.mejorarEstadistica("Ana", null));
        assertEquals(HeadSoccerException.ESTADISTICA_NULA, e.getMessage());
        e = assertThrows(ProgresoException.class, () -> juego.mejorarEstadistica("Ana", Estadistica.KICK));
        assertTrue(e.getMessage().startsWith(ProgresoException.PUNTOS_INSUFICIENTES));
    }

    @Test
    void unArchivoDeProgresoDanadoNoImpideJugar() throws IOException, HeadSoccerException {
        File progreso = carpeta.resolve("progreso.dat").toFile();
        try (FileWriter w = new FileWriter(progreso)) {
            w.write("danado");
        }
        HeadSoccer juego = new HeadSoccer(progreso);
        assertEquals(0, juego.getPuntos("Ana"));
    }
}
