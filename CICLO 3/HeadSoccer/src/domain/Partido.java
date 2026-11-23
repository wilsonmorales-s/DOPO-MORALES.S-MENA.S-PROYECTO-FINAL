package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Un partido Jugador vs Jugador: tiene la cancha, el balon, los dos jugadores y el marcador,
// y aplica las reglas (choques, goles, tiempo y fin del partido).
public class Partido implements Serializable {

    public static final double REBOTE_JUGADOR = 0.8;     // el balon conserva el 80% de su rapidez al chocar
    public static final double REBOTE_MINIMO = 250;      // rapidez minima con la que sale despues de chocar
    public static final int PUNTOS_GANADOR = 100;
    public static final int PUNTOS_PERDEDOR = 25;

    private ConfiguracionPartido configuracion;
    private Campo campo;
    private Balon balon;
    private Jugador jugadorIzquierdo;
    private Jugador jugadorDerecho;
    private Marcador marcador;
    private EstadoPartido estado;
    private double tiempoRestante; // en segundos, solo se usa si es POR_TIEMPO
    private boolean muerteSubita;  // true si se acabo el tiempo empatados: el proximo gol gana
    private List<Balon> balonesFalsos;  // los 7 balones falsos de la Ilusion
    private double tiempoFalsos;
    private boolean puntosRegistrados;

    public Partido(ConfiguracionPartido configuracion) {
        this.configuracion = configuracion;
        campo = new Campo();
        marcador = new Marcador();
        jugadorIzquierdo = new Jugador(configuracion.getNombreIzquierdo(), Lado.IZQUIERDA, xInicial(Lado.IZQUIERDA));
        jugadorDerecho = new Jugador(configuracion.getNombreDerecho(), Lado.DERECHA, xInicial(Lado.DERECHA));
        balon = new Balon(campo.getAncho() / 2, campo.getAlto() / 2); // empieza en el centro
        estado = EstadoPartido.EN_JUEGO;
        if (configuracion.getTipo() == TipoPartido.POR_TIEMPO) {
            tiempoRestante = configuracion.getLimite() * 60; // minutos a segundos
        }
        prepararJugadores();
    }

    public ConfiguracionPartido getConfiguracion() {
        return configuracion;
    }

    public Campo getCampo() {
        return campo;
    }

    public Balon getBalon() {
        return balon;
    }

    public Marcador getMarcador() {
        return marcador;
    }

    public EstadoPartido getEstado() {
        return estado;
    }

    public double getTiempoRestante() {
        return tiempoRestante;
    }

    public boolean enMuerteSubita() {
        return muerteSubita;
    }

    public Jugador getJugador(Lado lado) {
        if (lado == Lado.IZQUIERDA) {
            return jugadorIzquierdo;
        } else {
            return jugadorDerecho;
        }
    }

    // El jugador con mas goles. Devuelve null si el partido no ha terminado o si quedo empatado.
    public Jugador getGanador() {
        Jugador ganador = null;
        if (estado == EstadoPartido.FINALIZADO) {
            int golesIzquierda = marcador.getGoles(Lado.IZQUIERDA);
            int golesDerecha = marcador.getGoles(Lado.DERECHA);
            if (golesIzquierda > golesDerecha) {
                ganador = jugadorIzquierdo;
            } else if (golesDerecha > golesIzquierda) {
                ganador = jugadorDerecho;
            }
        }
        return ganador;
    }

    public void pausar() throws PartidoException {
        if (estado == EstadoPartido.EN_JUEGO) {
            estado = EstadoPartido.PAUSADO;
        } else {
            throw new PartidoException(PartidoException.NO_ESTA_EN_JUEGO);
        }
    }

    public void reanudar() throws PartidoException {
        if (estado == EstadoPartido.PAUSADO) {
            estado = EstadoPartido.EN_JUEGO;
        } else {
            throw new PartidoException(PartidoException.NO_ESTA_PAUSADO);
        }
    }

    public void terminar() throws PartidoException {
        if (estado != EstadoPartido.FINALIZADO) {
            estado = EstadoPartido.FINALIZADO;
        } else {
            throw new PartidoException(PartidoException.YA_FINALIZADO);
        }
    }

    // Un "cuadro" del juego: avanza todo dt segundos (la interfaz lo llama unas 60 veces por segundo).
    public void actualizar(double dt) {
        if (estado == EstadoPartido.EN_JUEGO) {
            // 0. Si juega la maquina, decide que hace
            moverMaquinas(dt);
            // 1. Se mueven los jugadores
            jugadorIzquierdo.actualizar(dt, campo);
            jugadorDerecho.actualizar(dt, campo);
            // 2. Los jugadores no se pueden atravesar
            separarJugadores();
            // Tiros de poder: si golpean al rival y si alguno con aura toca el balon
            verificarTiroDePoder();
            usarPoder(jugadorIzquierdo);
            usarPoder(jugadorDerecho);
            // 3. Se revisa si alguno toca o patea el balon
            resolverContacto(jugadorIzquierdo);
            resolverContacto(jugadorDerecho);
            patearRival(jugadorIzquierdo);
            patearRival(jugadorDerecho);
            // 4. Se mueve el balon
            balon.actualizar(dt, campo);
            actualizarBalonesFalsos(dt);
            // 5. Se revisa si hubo gol
            int golesIzquierda = marcador.getGoles(Lado.IZQUIERDA);
            int golesDerecha = marcador.getGoles(Lado.DERECHA);
            verificarGol();
            revisarGolRecibido(golesIzquierda, golesDerecha);
            // 6. Corre el reloj (si el gol no acabo el partido)
            if (estado == EstadoPartido.EN_JUEGO) {
                avanzarTiempo(dt);
            }
        }
    }

    // Descuenta tiempo. Si se acaba: gana el que va adelante, o muerte subita si van empatados.
    private void avanzarTiempo(double dt) {
        if (configuracion.getTipo() == TipoPartido.POR_TIEMPO && !muerteSubita) {
            tiempoRestante = tiempoRestante - dt;
            if (tiempoRestante <= 0) {
                tiempoRestante = 0;
                int golesIzquierda = marcador.getGoles(Lado.IZQUIERDA);
                int golesDerecha = marcador.getGoles(Lado.DERECHA);
                if (golesIzquierda == golesDerecha) {
                    muerteSubita = true;
                } else {
                    estado = EstadoPartido.FINALIZADO;
                }
            }
        }
    }

    // Hay gol cuando el balon se sale de la cancha por un lado (solo puede pasar por la porteria).
    private void verificarGol() {
        double x = balon.getX();
        double ancho = campo.getAncho();
        Lado anotador = null;
        if (x < 0) {
            anotador = Lado.DERECHA;   // entro en la porteria izquierda
        } else if (x > ancho) {
            anotador = Lado.IZQUIERDA; // entro en la porteria derecha
        }

        if (anotador != null) {
            marcador.sumarGol(anotador);
            // Si es por goles, revisa si el que anoto llego a la meta
            boolean llegoALaMeta = false;
            if (configuracion.getTipo() == TipoPartido.POR_GOLES) {
                int goles = marcador.getGoles(anotador);
                int limite = configuracion.getLimite();
                if (goles >= limite) {
                    llegoALaMeta = true;
                }
            }
            if (muerteSubita || llegoALaMeta) {
                estado = EstadoPartido.FINALIZADO;
            } else {
                reiniciarPosiciones(); // saque desde el centro
            }
        }
    }

    // Todos vuelven a su sitio despues de un gol.
    private void reiniciarPosiciones() {
        jugadorIzquierdo.reiniciar(xInicial(Lado.IZQUIERDA));
        jugadorDerecho.reiniciar(xInicial(Lado.DERECHA));
        balon.reiniciar(campo.getAncho() / 2, campo.getAlto() / 2);
    }

    // Cada jugador empieza a un cuarto de la cancha desde su lado.
    private double xInicial(Lado lado) {
        if (lado == Lado.IZQUIERDA) {
            return campo.getAncho() * 0.25;
        } else {
            return campo.getAncho() * 0.75;
        }
    }

    // Si las cabezas de los dos jugadores se montan una sobre otra, los separa de lado
    // para que no se puedan atravesar. Si uno le cae encima al otro, se resbala hacia un lado.
    private void separarJugadores() {
        // 1. Distancia entre los centros de las dos cabezas (Pitagoras)
        double dx = jugadorDerecho.getCentroX() - jugadorIzquierdo.getCentroX();
        double dy = jugadorDerecho.getCentroY() - jugadorIzquierdo.getCentroY();
        double distancia = Math.sqrt(dx * dx + dy * dy);
        double distanciaMinima = 2 * Jugador.RADIO;

        if (distancia < distanciaMinima) {
            // 2. Cuanto deben quedar separados de lado para estar justo tocandose a esa altura (Pitagoras)
            double separacionNecesaria = Math.sqrt(distanciaMinima * distanciaMinima - dy * dy);

            // 3. Sentido del empujon: normalmente el izquierdo va a la izquierda y el derecho a la derecha;
            //    si se cambiaron de lado (dx negativo), al reves
            double sentido = 1;
            if (dx < 0) {
                sentido = -1;
            }

            // 4. Cada uno se corre la mitad de lo que falta
            double falta = separacionNecesaria - Math.abs(dx);
            jugadorIzquierdo.empujar(-sentido * falta / 2, campo);
            jugadorDerecho.empujar(sentido * falta / 2, campo);

            // 5. Si uno estaba contra la pared no se pudo correr: el otro se corre lo que quedo faltando
            falta = separacionNecesaria - Math.abs(jugadorDerecho.getX() - jugadorIzquierdo.getX());
            if (falta > 0) {
                jugadorIzquierdo.empujar(-sentido * falta, campo);
                jugadorDerecho.empujar(sentido * falta, campo);
            }
        }
    }

    // Revisa si el jugador toca el balon.
    // Se piensa la cabeza y el balon como dos circulos: se tocan si la distancia entre
    // sus centros es menor que la suma de sus radios.
    private void resolverContacto(Jugador jugador) {
        // 1. Distancia entre el centro de la cabeza y el centro del balon (Pitagoras)
        double dx = balon.getX() - jugador.getCentroX();
        double dy = balon.getY() - jugador.getCentroY();
        double distancia = Math.sqrt(dx * dx + dy * dy);
        double distanciaMinima = Jugador.RADIO + Balon.RADIO;

        if (distancia == 0) {
            return; // estan en el mismo punto exacto: no se puede saber hacia donde empujar
        }

        // 2. Patada: si esta pateando y el balon esta cerca, lo manda hacia la porteria rival
        if (jugador.estaPateando() && distancia < distanciaMinima + Jugador.ALCANCE_PATADA) {
            double sentido = 1;  // el de la izquierda patea hacia la derecha
            if (jugador.getLado() == Lado.DERECHA) {
                sentido = -1;    // el de la derecha patea hacia la izquierda
            }
            balon.setVelocidad(sentido * Jugador.FUERZA_PATADA, Jugador.FUERZA_PATADA * 0.6);
            jugador.consumirPatada();
            return;
        }

        // 3. Cabezazo / choque: si se tocan, el balon sale rebotado hacia afuera de la cabeza
        if (distancia < distanciaMinima) {
            // Direccion del choque: de la cabeza hacia el balon (dividir entre la distancia la deja de tamano 1)
            double direccionX = dx / distancia;
            double direccionY = dy / distancia;

            // Sacamos el balon para que no quede metido dentro de la cabeza
            double centroX = jugador.getCentroX();
            double centroY = jugador.getCentroY();
            balon.setPosicion(centroX + direccionX * distanciaMinima, centroY + direccionY * distanciaMinima);

            // Rapidez con la que sale: la que traia (con algo de perdida), pero nunca menos del minimo
            double vxBalon = balon.getVx();
            double vyBalon = balon.getVy();
            double rapidezBalon = Math.sqrt(vxBalon * vxBalon + vyBalon * vyBalon);
            double rapidez = rapidezBalon * REBOTE_JUGADOR;
            if (rapidez < REBOTE_MINIMO) {
                rapidez = REBOTE_MINIMO;
            }

            // Sale en la direccion del choque, y ademas el jugador le suma su propio movimiento
            double vxJugador = jugador.getVx();
            double vyJugador = jugador.getVy();
            balon.setVelocidad(direccionX * rapidez + vxJugador, direccionY * rapidez + vyJugador);
        }
    }

    // ---------------------------------------------------------------- version tres

    // Pone la cancha del tamano elegido y le da a cada jugador su personaje (y la maquina si hay).
    private void prepararJugadores() {
        campo = new Campo(configuracion.getResolucion());
        jugadorIzquierdo.setPersonaje(configuracion.getPersonajeIzquierdo());
        jugadorDerecho.setPersonaje(configuracion.getPersonajeDerecho());
        jugadorDerecho.setMaquina(configuracion.getPerfilMaquina());
        balonesFalsos = new ArrayList<Balon>();
        reiniciarPosiciones();
    }

    public Jugador getRival(Jugador jugador) {
        if (jugador == jugadorIzquierdo) {
            return jugadorDerecho;
        } else {
            return jugadorIzquierdo;
        }
    }

    public List<Balon> getBalonesFalsos() {
        return balonesFalsos;
    }

    // 100 puntos el ganador y 25 el perdedor (25 cada uno si empatan). 0 si no ha terminado.
    public int getPuntosGanados(Lado lado) {
        int puntos = 0;
        if (estado == EstadoPartido.FINALIZADO) {
            Jugador ganador = getGanador();
            if (ganador != null && ganador.getLado() == lado) {
                puntos = PUNTOS_GANADOR;
            } else {
                puntos = PUNTOS_PERDEDOR;
            }
        }
        return puntos;
    }

    public boolean puntosRegistrados() {
        return puntosRegistrados;
    }

    public void marcarPuntosRegistrados() {
        puntosRegistrados = true;
    }

    private void moverMaquinas(double dt) {
        if (jugadorIzquierdo.esMaquina()) {
            jugadorIzquierdo.getMaquina().decidir(this, jugadorIzquierdo, dt);
        }
        if (jugadorDerecho.esMaquina()) {
            jugadorDerecho.getMaquina().decidir(this, jugadorDerecho, dt);
        }
    }

    // Si el jugador tiene aura y toca el balon (con la cabeza o el pie) sale el tiro de su personaje.
    private void usarPoder(Jugador jugador) {
        if (jugador.tieneAura()) {
            double dx = balon.getX() - jugador.getCentroX();
            double dy = balon.getY() - jugador.getCentroY();
            double distancia = Math.sqrt(dx * dx + dy * dy);
            double alcance = Jugador.RADIO + Balon.RADIO;
            if (jugador.estaPateando()) {
                alcance = alcance + Jugador.ALCANCE_PATADA;
            }
            if (distancia < alcance) {
                jugador.consumirAura();
                jugador.consumirPatada();
                jugador.getPersonaje().lanzarTiro(balon, jugador, this);
            }
        }
    }

    // Si el tiro de poder le pega en la cabeza al rival: dano (DanoTiro * multiplicadorKick) y efecto.
    // Si el rival lo alcanza a patear, lo despeja sin dano.
    private void verificarTiroDePoder() {
        if (balon.esTiroDePoder()) {
            Jugador lanzador = getJugador(balon.getLanzador());
            Jugador rival = getRival(lanzador);
            double dx = balon.getX() - rival.getCentroX();
            double dy = balon.getY() - rival.getCentroY();
            double distancia = Math.sqrt(dx * dx + dy * dy);
            if (distancia < Jugador.RADIO + Balon.RADIO) {
                Personaje personaje = lanzador.getPersonaje();
                rival.recibirDano(personaje.getDanoTiro() * lanzador.getMultiplicadorKick());
                rival.aplicarEfecto(personaje.crearEfecto(), this);
                balon.terminarTiroDePoder();
                balon.setVelocidad(-balon.getVx() * 0.4, Math.abs(balon.getVx()) * 0.3); // rebota en el rival
            } else if (rival.estaPateando() && distancia < Jugador.RADIO + Balon.RADIO + Jugador.ALCANCE_PATADA) {
                balon.terminarTiroDePoder();
            }
        }
    }

    // Si la patada no toco el balon pero el rival esta al alcance del pie, le quita vida.
    private void patearRival(Jugador jugador) {
        if (jugador.estaPateando()) {
            Jugador rival = getRival(jugador);
            double dx = rival.getCentroX() - jugador.getCentroX();
            double dy = rival.getCentroY() - jugador.getCentroY();
            double distancia = Math.sqrt(dx * dx + dy * dy);
            if (distancia < 2 * Jugador.RADIO + Jugador.ALCANCE_PATADA) {
                rival.recibirDano(Jugador.DANO_PATADA * jugador.getMultiplicadorKick());
                jugador.consumirPatada();
            }
        }
    }

    // Si hubo gol: al que lo recibio le sube la barra y se quitan efectos, tiro de poder y balones falsos.
    private void revisarGolRecibido(int golesIzquierdaAntes, int golesDerechaAntes) {
        boolean huboGol = false;
        if (marcador.getGoles(Lado.IZQUIERDA) > golesIzquierdaAntes) {
            jugadorDerecho.recibirGol();
            huboGol = true;
        }
        if (marcador.getGoles(Lado.DERECHA) > golesDerechaAntes) {
            jugadorIzquierdo.recibirGol();
            huboGol = true;
        }
        if (huboGol) {
            jugadorIzquierdo.quitarEfecto();
            jugadorDerecho.quitarEfecto();
            balon.terminarTiroDePoder();
            balonesFalsos.clear();
            tiempoFalsos = 0;
        }
    }

    // Ilusion: el balon se divide y salen balones iguales en direcciones parecidas.
    public void crearBalonesFalsos(int cantidad, double duracion) {
        balonesFalsos.clear();
        for (int i = 0; i < cantidad; i++) {
            Balon falso = new Balon(balon.getX(), balon.getY());
            falso.setVelocidad(balon.getVx() * (0.55 + 0.1 * i), balon.getVy() + (i - cantidad / 2) * 130);
            balonesFalsos.add(falso);
        }
        tiempoFalsos = duracion;
    }

    public void mantenerBalonesFalsos(double duracion) {
        if (!balonesFalsos.isEmpty() && tiempoFalsos < duracion) {
            tiempoFalsos = duracion;
        }
    }

    // Los falsos se mueven como el balon; si entran al arco desaparecen y no cuentan.
    private void actualizarBalonesFalsos(double dt) {
        if (!balonesFalsos.isEmpty()) {
            List<Balon> quedan = new ArrayList<Balon>();
            for (Balon falso : balonesFalsos) {
                falso.actualizar(dt, campo);
                if (falso.getX() >= 0 && falso.getX() <= campo.getAncho()) {
                    quedan.add(falso);
                }
            }
            balonesFalsos = quedan;
            tiempoFalsos = tiempoFalsos - dt;
            if (tiempoFalsos <= 0) {
                balonesFalsos.clear();
                tiempoFalsos = 0;
            }
        }
    }
}
