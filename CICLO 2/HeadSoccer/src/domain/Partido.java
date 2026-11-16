package domain;

// Un partido Jugador vs Jugador: tiene la cancha, el balon, los dos jugadores y el marcador,
// y aplica las reglas (choques, goles, tiempo y fin del partido).
public class Partido {

    public static final double REBOTE_JUGADOR = 0.8;     // el balon conserva el 80% de su rapidez al chocar
    public static final double REBOTE_MINIMO = 250;      // rapidez minima con la que sale despues de chocar

    private ConfiguracionPartido configuracion;
    private Campo campo;
    private Balon balon;
    private Jugador jugadorIzquierdo;
    private Jugador jugadorDerecho;
    private Marcador marcador;
    private EstadoPartido estado;
    private double tiempoRestante; // en segundos, solo se usa si es POR_TIEMPO
    private boolean muerteSubita;  // true si se acabo el tiempo empatados: el proximo gol gana

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
            // 1. Se mueven los jugadores
            jugadorIzquierdo.actualizar(dt, campo);
            jugadorDerecho.actualizar(dt, campo);
            // 2. Los jugadores no se pueden atravesar
            separarJugadores();
            // 3. Se revisa si alguno toca o patea el balon
            resolverContacto(jugadorIzquierdo);
            resolverContacto(jugadorDerecho);
            // 4. Se mueve el balon
            balon.actualizar(dt, campo);
            // 5. Se revisa si hubo gol
            verificarGol();
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
            return Campo.ANCHO * 0.25;
        } else {
            return Campo.ANCHO * 0.75;
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
}
