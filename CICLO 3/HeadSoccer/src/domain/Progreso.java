package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Puntos y estadisticas de cada jugador. Se busca por el nombre (sin importar mayusculas).
public class Progreso implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int PRECIO_INICIAL = 500;   // la primera mejora cuesta 500; luego 1000, 2000...

    private HashMap<String, String> nombres;            // clave -> nombre como lo escribio
    private HashMap<String, Integer> puntos;
    private HashMap<String, Estadisticas> estadisticas;

    public Progreso() {
        nombres = new HashMap<String, String>();
        puntos = new HashMap<String, Integer>();
        estadisticas = new HashMap<String, Estadisticas>();
    }

    private String clave(String nombre) {
        return nombre.trim().toLowerCase();
    }

    // Si el jugador no existia, lo crea con 0 puntos y todas las estadisticas en 1.
    private void registrar(String nombre) {
        String clave = clave(nombre);
        if (!nombres.containsKey(clave)) {
            nombres.put(clave, nombre.trim());
            puntos.put(clave, 0);
            estadisticas.put(clave, new Estadisticas());
        }
    }

    public boolean existe(String nombre) {
        return nombres.containsKey(clave(nombre));
    }

    // Nombres de los jugadores que tienen progreso guardado.
    public List<String> getNombres() {
        List<String> lista = new ArrayList<String>();
        for (String nombre : nombres.values()) {
            if (!lista.contains(nombre)) {
                lista.add(nombre);
            }
        }
        lista.sort(String.CASE_INSENSITIVE_ORDER);
        return lista;
    }

    public int getPuntos(String nombre) {
        int total = 0;
        if (existe(nombre)) {
            total = puntos.get(clave(nombre));
        }
        return total;
    }

    // Copia de las estadisticas del jugador (si no existe, las de un jugador nuevo).
    public Estadisticas getEstadisticas(String nombre) {
        Estadisticas copia = new Estadisticas();
        if (existe(nombre)) {
            copia = estadisticas.get(clave(nombre)).copia();
        }
        return copia;
    }

    public void sumarPuntos(String nombre, int cantidad) {
        registrar(nombre);
        String clave = clave(nombre);
        puntos.put(clave, puntos.get(clave) + cantidad);
    }

    // Precio del siguiente punto de la estadistica: 500 * 2^(nivel - 1) -> 500, 1000, 2000, ...
    public int getPrecioMejora(String nombre, Estadistica estadistica) {
        int nivel = getEstadisticas(nombre).getNivel(estadistica);
        int precio = PRECIO_INICIAL;
        for (int i = Estadisticas.NIVEL_MINIMO; i < nivel; i++) {
            precio = precio * 2;
        }
        return precio;
    }

    // Compra un punto de la estadistica con los puntos del jugador.
    public void mejorar(String nombre, Estadistica estadistica) throws ProgresoException {
        registrar(nombre);
        String clave = clave(nombre);
        Estadisticas suyas = estadisticas.get(clave);
        if (suyas.getNivel(estadistica) < Estadisticas.NIVEL_MAXIMO) {
            int precio = getPrecioMejora(nombre, estadistica);
            int disponibles = puntos.get(clave);
            if (disponibles >= precio) {
                suyas.subir(estadistica);
                puntos.put(clave, disponibles - precio);
            } else {
                throw new ProgresoException(ProgresoException.PUNTOS_INSUFICIENTES
                        + " (cuesta " + precio + " y tiene " + disponibles + ")");
            }
        } else {
            throw new ProgresoException(ProgresoException.NIVEL_MAXIMO);
        }
    }
}
