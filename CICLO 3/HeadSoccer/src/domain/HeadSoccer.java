package domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

// Fachada del dominio: es la unica clase que usa la interfaz (presentation).
// Cada metodo sigue su diagrama de secuencia en Astah.
// Si no hay partido o falta un dato, lanza HeadSoccerException.
public class HeadSoccer {

    public static final String ARCHIVO_PROGRESO = "progreso.dat";

    private Partido partido;
    private Progreso progreso;       // puntos y estadisticas de cada jugador
    private File archivoProgreso;    // si es null el progreso no se guarda en disco (pruebas)

    public HeadSoccer() {
        progreso = new Progreso();
    }

    // Carga el progreso guardado. Si el archivo esta danado se empieza de cero y se anota en el log.
    public HeadSoccer(File archivoProgreso) {
        this.archivoProgreso = archivoProgreso;
        progreso = new Progreso();
        if (archivoProgreso != null && archivoProgreso.exists()) {
            try {
                progreso = (Progreso) leer(archivoProgreso);
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                Log.registre(e);
            }
        }
    }

    // Crea un partido nuevo con la configuracion elegida.
    public void iniciarPartido(ConfiguracionPartido configuracion) throws HeadSoccerException {
        if (configuracion != null) {
            partido = new Partido(configuracion);
            aplicarEstadisticas();
        } else {
            throw new HeadSoccerException(HeadSoccerException.CONFIGURACION_NULA);
        }
    }

    public boolean hayPartido() {
        return partido != null;
    }

    // La interfaz lo usa para leer el estado y dibujarlo (marcador, posiciones, tiempo).
    public Partido getPartido() {
        return partido;
    }

    // Avanza el juego dt segundos (un cuadro).
    public void actualizar(double dt) throws HeadSoccerException {
        if (partido != null) {
            partido.actualizar(dt);
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }

    // direccion: -1 izquierda, 0 quieto, 1 derecha.
    public void moverJugador(Lado lado, int direccion) throws HeadSoccerException {
        Jugador jugador = buscarJugador(lado);
        jugador.mover(direccion);
    }

    public void saltar(Lado lado) throws HeadSoccerException {
        Jugador jugador = buscarJugador(lado);
        jugador.saltar();
    }

    public void patear(Lado lado) throws HeadSoccerException {
        Jugador jugador = buscarJugador(lado);
        jugador.patear();
    }

    public void pausar() throws HeadSoccerException {
        if (partido != null) {
            partido.pausar();
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }

    public void reanudar() throws HeadSoccerException {
        if (partido != null) {
            partido.reanudar();
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }

    public void terminar() throws HeadSoccerException {
        if (partido != null) {
            partido.terminar();
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }

    // Vuelve a jugar con la misma configuracion (mismo tipo, limite y nombres), desde 0 - 0.
    public void revancha() throws HeadSoccerException {
        if (partido != null) {
            ConfiguracionPartido configuracion = partido.getConfiguracion();
            iniciarPartido(configuracion);
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }

    // Busca el jugador del lado indicado. Lo usan moverJugador, saltar y patear.
    private Jugador buscarJugador(Lado lado) throws HeadSoccerException {
        if (partido != null) {
            if (lado != null) {
                return partido.getJugador(lado);
            } else {
                throw new HeadSoccerException(HeadSoccerException.LADO_NULO);
            }
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }

    // ---------------------------------------------------------------- version tres

    // Cada jugador juega con las estadisticas que tiene guardadas.
    private void aplicarEstadisticas() {
        Jugador izquierdo = partido.getJugador(Lado.IZQUIERDA);
        Jugador derecho = partido.getJugador(Lado.DERECHA);
        izquierdo.setEstadisticas(progreso.getEstadisticas(izquierdo.getNombre()));
        derecho.setEstadisticas(progreso.getEstadisticas(derecho.getNombre()));
    }

    public void activarPoder(Lado lado) throws HeadSoccerException {
        Jugador jugador = buscarJugador(lado);
        jugador.activarPoder();
    }

    // Si el partido ya termino, le suma los puntos a cada jugador (solo una vez).
    public void registrarResultado() throws HeadSoccerException {
        if (partido != null) {
            if (partido.getEstado() == EstadoPartido.FINALIZADO && !partido.puntosRegistrados()) {
                Jugador izquierdo = partido.getJugador(Lado.IZQUIERDA);
                Jugador derecho = partido.getJugador(Lado.DERECHA);
                progreso.sumarPuntos(izquierdo.getNombre(), partido.getPuntosGanados(Lado.IZQUIERDA));
                progreso.sumarPuntos(derecho.getNombre(), partido.getPuntosGanados(Lado.DERECHA));
                partido.marcarPuntosRegistrados();
                guardarProgreso();
            }
        } else {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
        }
    }

    public void guardar(File archivo) throws HeadSoccerException {
        if (archivo != null) {
            if (partido != null) {
                try {
                    escribir(archivo, partido);
                } catch (IOException e) {
                    Log.registre(e);
                    throw new HeadSoccerException(HeadSoccerException.ERROR_GUARDAR + ": " + archivo.getName());
                }
            } else {
                throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO);
            }
        } else {
            throw new HeadSoccerException(HeadSoccerException.ARCHIVO_NULO);
        }
    }

    // La partida abierta queda en pausa para que los jugadores decidan cuando seguir.
    public void abrir(File archivo) throws HeadSoccerException {
        if (archivo != null) {
            if (archivo.exists()) {
                Object leido;
                try {
                    leido = leer(archivo);
                } catch (IOException | ClassNotFoundException e) {
                    Log.registre(e);
                    throw new HeadSoccerException(HeadSoccerException.ERROR_ABRIR + ": " + archivo.getName());
                }
                if (leido instanceof Partido) {
                    partido = (Partido) leido;
                    if (partido.getEstado() == EstadoPartido.EN_JUEGO) {
                        partido.pausar();
                    }
                } else {
                    throw new HeadSoccerException(HeadSoccerException.ARCHIVO_INVALIDO + ": " + archivo.getName());
                }
            } else {
                throw new HeadSoccerException(HeadSoccerException.ARCHIVO_NO_EXISTE + ": " + archivo.getName());
            }
        } else {
            throw new HeadSoccerException(HeadSoccerException.ARCHIVO_NULO);
        }
    }

    public List<String> getJugadoresRegistrados() {
        return progreso.getNombres();
    }

    public int getPuntos(String nombre) throws HeadSoccerException {
        validarNombre(nombre);
        return progreso.getPuntos(nombre);
    }

    public Estadisticas getEstadisticas(String nombre) throws HeadSoccerException {
        validarNombre(nombre);
        return progreso.getEstadisticas(nombre);
    }

    public int getPrecioMejora(String nombre, Estadistica estadistica) throws HeadSoccerException {
        validarNombre(nombre);
        if (estadistica != null) {
            return progreso.getPrecioMejora(nombre, estadistica);
        } else {
            throw new HeadSoccerException(HeadSoccerException.ESTADISTICA_NULA);
        }
    }

    // Compra un punto de la estadistica. Se nota desde el siguiente partido.
    public void mejorarEstadistica(String nombre, Estadistica estadistica) throws HeadSoccerException {
        validarNombre(nombre);
        if (estadistica != null) {
            progreso.mejorar(nombre, estadistica);
            guardarProgreso();
        } else {
            throw new HeadSoccerException(HeadSoccerException.ESTADISTICA_NULA);
        }
    }

    private void guardarProgreso() throws HeadSoccerException {
        if (archivoProgreso != null) {
            try {
                escribir(archivoProgreso, progreso);
            } catch (IOException e) {
                Log.registre(e);
                throw new HeadSoccerException(HeadSoccerException.ERROR_GUARDAR + ": " + archivoProgreso.getName());
            }
        }
    }

    private void validarNombre(String nombre) throws HeadSoccerException {
        if (nombre == null || nombre.isBlank()) {
            throw new HeadSoccerException(HeadSoccerException.NOMBRE_VACIO);
        }
    }

    // El archivo se abre aparte para que siempre se cierre, aunque falle (en Windows un archivo abierto no se puede borrar).
    private static void escribir(File archivo, Object objeto) throws IOException {
        try (FileOutputStream archivoSalida = new FileOutputStream(archivo);
                ObjectOutputStream salida = new ObjectOutputStream(archivoSalida)) {
            salida.writeObject(objeto);
        }
    }

    private static Object leer(File archivo) throws IOException, ClassNotFoundException {
        try (FileInputStream archivoEntrada = new FileInputStream(archivo);
                ObjectInputStream entrada = new ObjectInputStream(archivoEntrada)) {
            return entrada.readObject();
        }
    }
}
