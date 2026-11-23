package domain;

import java.io.Serializable;

// Un jugador de cabeza grande.
// (x, y) es el punto de abajo en el centro: x es donde esta parado y y su altura sobre el suelo.
public class Jugador implements Serializable {

    public static final double RADIO = 45;             // tamano de la cabeza
    public static final double VELOCIDAD = 320;        // que tan rapido camina
    public static final double VELOCIDAD_SALTO = 650;  // con que fuerza salta
    public static final double GRAVEDAD = 1800;        // que tan rapido cae
    public static final double DURACION_PATADA = 0.25; // segundos que dura una patada
    public static final double ALCANCE_PATADA = 30;    // distancia extra a la que alcanza a patear
    public static final double FUERZA_PATADA = 750;    // velocidad que le da al balon
    public static final double DANO_PATADA = 10;       // dano base de la patada al rival
    public static final double TIEMPO_BARRA = 10;      // segundos que tarda en llenarse la barra de poder
    public static final double DURACION_ATURDIMIENTO = 3;

    private String nombre;
    private Lado lado;
    private double x;
    private double y;
    private double vy;            // velocidad vertical (para el salto)
    private int direccion;        // -1 izquierda, 0 quieto, 1 derecha
    private double tiempoPatada;  // cuanto le queda a la patada actual (0 = no esta pateando)
    private Personaje personaje = new CoreaDelSur();
    private Estadisticas estadisticas = new Estadisticas();
    private PerfilMaquina maquina;          // null si lo controla una persona
    private double vida = Estadisticas.VIDA_BASE;
    private double barraPoder;              // de 0 a 1
    private boolean aura;                   // tiene el poder activo
    private Efecto efecto;                  // efecto del tiro de poder del rival (o null)
    private double tiempoAturdido;

    public Jugador(String nombre, Lado lado, double x) {
        this.nombre = nombre;
        this.lado = lado;
        this.x = x;
    }

    public String getNombre() {
        return nombre;
    }

    public Lado getLado() {
        return lado;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // Velocidad horizontal: depende de hacia donde se esta moviendo.
    public double getVx() {
        double vx = 0;
        if (efecto != null && efecto.tieneVelocidadForzada()) {
            vx = efecto.getVelocidadForzada(lado);
        } else if (puedeMoverse()) {
            vx = direccion * getVelocidad();
        }
        return vx;
    }

    public double getVy() {
        return vy;
    }

    // Centro de la cabeza (sirve para saber si toca el balon).
    public double getCentroX() {
        return x;
    }

    public double getCentroY() {
        return y + RADIO;
    }

    public boolean estaEnSuelo() {
        return y <= 0;
    }

    public boolean estaPateando() {
        return tiempoPatada > 0;
    }

    // Cambia hacia donde camina: negativo izquierda, 0 quieto, positivo derecha.
    public void mover(int direccion) {
        if (direccion < 0) {
            this.direccion = -1;
        } else if (direccion > 0) {
            this.direccion = 1;
        } else {
            this.direccion = 0;
        }
    }

    // Solo puede saltar si esta en el suelo (no hay doble salto).
    public void saltar() {
        if (estaEnSuelo() && puedeSaltar()) {
            vy = getVelocidadSalto();
        }
    }

    // Empieza una patada si no hay una en curso.
    public void patear() {
        if (!estaPateando() && puedePatear()) {
            tiempoPatada = DURACION_PATADA;
        }
    }

    // La patada ya golpeo el balon: se acaba para no golpearlo dos veces.
    public void consumirPatada() {
        tiempoPatada = 0;
    }

    // Lo devuelve a su posicion inicial, quieto y en el suelo (despues de un gol).
    public void reiniciar(double x) {
        this.x = x;
        this.y = 0;
        this.vy = 0;
        this.direccion = 0;
        this.tiempoPatada = 0;
    }

    // Corre al jugador de lado (positivo a la derecha, negativo a la izquierda)
    // sin dejarlo salir de la cancha. Se usa cuando choca con el otro jugador.
    public void empujar(double cantidad, Campo campo) {
        x = x + cantidad;
        if (x < RADIO) {
            x = RADIO;
        }
        if (x > campo.getAncho() - RADIO) {
            x = campo.getAncho() - RADIO;
        }
    }

    // Mueve al jugador lo que le corresponde en dt segundos.
    public void actualizar(double dt, Campo campo) {
        // 1. Camina hacia donde va
        x = x + getVx() * dt;

        // 2. No se puede salir de la cancha
        if (x < RADIO) {
            x = RADIO;
        }
        if (x > campo.getAncho() - RADIO) {
            x = campo.getAncho() - RADIO;
        }

        // 3. Salto y gravedad
        vy = vy - GRAVEDAD * dt;
        y = y + vy * dt;
        if (y <= 0) {
            y = 0;   // llego al suelo
            vy = 0;
        }

        // 4. Se va gastando el tiempo de la patada
        if (tiempoPatada > 0) {
            tiempoPatada = tiempoPatada - dt;
            if (tiempoPatada < 0) {
                tiempoPatada = 0;
            }
        }

        // 5. Barra de poder, efecto y aturdimiento
        actualizarPoderYEfectos(dt);
    }

    // ---------------------------------------------------------------- version tres

    public Personaje getPersonaje() {
        return personaje;
    }

    public void setPersonaje(Personaje personaje) {
        this.personaje = personaje;
    }

    public Estadisticas getEstadisticas() {
        return estadisticas;
    }

    // Al cambiar las estadisticas la vida queda en el nuevo maximo.
    public void setEstadisticas(Estadisticas estadisticas) {
        this.estadisticas = estadisticas;
        this.vida = estadisticas.getVidaMax();
    }

    public PerfilMaquina getMaquina() {
        return maquina;
    }

    public void setMaquina(PerfilMaquina maquina) {
        this.maquina = maquina;
    }

    public boolean esMaquina() {
        return maquina != null;
    }

    // Speed: cada punto suma 10 %. Si esta electrocutado va mas lento.
    public double getVelocidad() {
        double velocidad = VELOCIDAD * estadisticas.getFactor(Estadistica.SPEED);
        if (efecto != null) {
            velocidad = velocidad * efecto.getFactorVelocidad();
        }
        return velocidad;
    }

    // Jump: la altura del salto es v^2 / 2g, por eso para subir la altura un 10 % se usa la raiz.
    public double getVelocidadSalto() {
        return VELOCIDAD_SALTO * Math.sqrt(estadisticas.getFactor(Estadistica.JUMP));
    }

    public double getVida() {
        return vida;
    }

    public double getVidaMax() {
        return estadisticas.getVidaMax();
    }

    public double getMultiplicadorKick() {
        return estadisticas.getMultiplicadorKick();
    }

    public boolean estaAturdido() {
        return tiempoAturdido > 0;
    }

    // La vida nunca baja de 0. Si llega a 0 queda aturdido 3 segundos.
    public void recibirDano(double dano) {
        if (!estaAturdido()) {
            vida = vida - dano;
            if (vida <= 0) {
                vida = 0;
                tiempoAturdido = DURACION_ATURDIMIENTO;
                direccion = 0;
                tiempoPatada = 0;
            }
        }
    }

    public double getBarraPoder() {
        return barraPoder;
    }

    public boolean barraLlena() {
        return barraPoder >= 1;
    }

    public boolean tieneAura() {
        return aura;
    }

    // Con la barra llena queda con aura y la barra vuelve a 0. Si ya tiene aura no pasa nada.
    public void activarPoder() {
        if (barraLlena() && !aura && !estaAturdido()) {
            barraPoder = 0;
            aura = true;
        }
    }

    public void consumirAura() {
        aura = false;
    }

    // Al recibir un gol la barra sube un cuarto.
    public void recibirGol() {
        barraPoder = Math.min(1, barraPoder + 0.25);
    }

    public Efecto getEfecto() {
        return efecto;
    }

    // Los efectos no se acumulan: el nuevo reemplaza al que tenia.
    public void aplicarEfecto(Efecto nuevo, Partido partido) {
        efecto = nuevo;
        if (nuevo != null) {
            nuevo.alAplicar(this, partido);
        }
    }

    public void quitarEfecto() {
        efecto = null;
    }

    public void lanzarAlAire(double velocidad) {
        vy = velocidad;
    }

    public boolean puedeMoverse() {
        return !estaAturdido() && (efecto == null || efecto.puedeMoverse());
    }

    public boolean puedeSaltar() {
        return !estaAturdido() && (efecto == null || efecto.puedeSaltar());
    }

    public boolean puedePatear() {
        return !estaAturdido() && (efecto == null || efecto.puedePatear());
    }

    private void actualizarPoderYEfectos(double dt) {
        // La barra se llena en 10 segundos (tambien mientras tiene el aura)
        barraPoder = Math.min(1, barraPoder + dt / TIEMPO_BARRA);

        if (efecto != null) {
            efecto.avanzar(dt);
            if (efecto.terminado()) {
                efecto = null;
            }
        }

        // Cuando se le pasa el aturdimiento vuelve a tener toda la vida
        if (tiempoAturdido > 0) {
            tiempoAturdido = tiempoAturdido - dt;
            if (tiempoAturdido <= 0) {
                tiempoAturdido = 0;
                vida = getVidaMax();
            }
        }
    }
}
