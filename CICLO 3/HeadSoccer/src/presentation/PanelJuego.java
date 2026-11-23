package presentation;

import domain.Balon;
import domain.Campo;
import domain.Efecto;
import domain.EstadoPartido;
import domain.HeadSoccer;
import domain.HeadSoccerException;
import domain.Jugador;
import domain.Lado;
import domain.Partido;
import domain.TipoPartido;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import javax.swing.JPanel;
import javax.swing.Timer;

// Pantalla del partido: dibuja la cancha y lee el teclado.
// No decide nada del juego: todo se lo pide a la fachada HeadSoccer.
public class PanelJuego extends JPanel implements KeyListener {

    private static final int ALTO_MARCADOR = 118; // franja de arriba con nombres, goles, vida y poder
    private static final int ALTO_PASTO = 40;     // franja de pasto debajo de la cancha
    private static final int ANCHO_BARRA = 220;
    private static final double TIEMPO_GOL = 1.0;  // segundos que se muestra el gol antes del siguiente saque

    private HeadSoccerGUI ventana;
    private HeadSoccer juego;
    private Timer reloj;                // llama a paso() cada 16 ms (unas 60 veces por segundo)
    private HashSet<Integer> teclas;    // teclas que estan presionadas en este momento
    private long ultimo;                // momento del ultimo cuadro, para medir cuanto tiempo paso
    private double animacion;           // segundos que lleva el partido en pantalla (para animar el aura)
    private double celebracion;         // lo que le queda a la animacion del gol (0 = no hay gol)
    private int golesIzquierdaVistos;   // para saber cuando hubo un gol nuevo
    private int golesDerechaVistos;
    private Lado ladoAnotador;

    // Para pasar de medidas del juego a pixeles de la pantalla
    private double escala;
    private double origenX;
    private double suelo;

    public PanelJuego(HeadSoccerGUI ventana, HeadSoccer juego) {
        this.ventana = ventana;
        this.juego = juego;
        prepareElementos();
        prepareAcciones();
    }

    private void prepareElementos() {
        teclas = new HashSet<Integer>();
        setFocusable(true); // para poder recibir las teclas
    }

    private void prepareAcciones() {
        reloj = new Timer(16, e -> paso());
        addKeyListener(this);
    }

    public void iniciar() {
        teclas.clear();
        celebracion = 0;
        Partido partido = juego.getPartido();
        if (partido != null) {
            golesIzquierdaVistos = partido.getMarcador().getGoles(Lado.IZQUIERDA);
            golesDerechaVistos = partido.getMarcador().getGoles(Lado.DERECHA);
        }
        ultimo = System.nanoTime();
        reloj.start();
        requestFocusInWindow();
    }

    public void detener() {
        reloj.stop();
    }

    // Un cuadro del juego: mueve a los jugadores segun las teclas, avanza el partido y redibuja.
    private void paso() {
        // Tiempo que paso desde el cuadro anterior, en segundos (nanoTime esta en nanosegundos)
        long ahora = System.nanoTime();
        double dt = (ahora - ultimo) / 1000000000.0;
        ultimo = ahora;
        if (dt > 0.05) {
            dt = 0.05; // si el computador se trabo, no dejamos que el juego de un salto grande
        }
        animacion = animacion + dt;

        // Mientras se muestra el gol nadie se mueve; al terminar sigue el saque
        if (celebracion > 0) {
            celebracion = celebracion - dt;
            repaint();
            return;
        }

        try {
            juego.moverJugador(Lado.IZQUIERDA, direccion(KeyEvent.VK_A, KeyEvent.VK_D));
            if (!derechaEsMaquina()) {
                juego.moverJugador(Lado.DERECHA, direccion(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT));
            }
            juego.actualizar(dt);
            juego.registrarResultado(); // si termino, suma los puntos
            revisarGol();
        } catch (HeadSoccerException e) {
            detener();
            ventana.mostrarError(e);
        } catch (RuntimeException e) {
            detener();
            ventana.mostrarErrorInesperado(e);
        }
        repaint(); // vuelve a dibujar (llama a paintComponent)
    }

    // Si alguien anoto (y el partido sigue), empieza la animacion del gol.
    private void revisarGol() {
        Partido partido = juego.getPartido();
        int golesIzquierda = partido.getMarcador().getGoles(Lado.IZQUIERDA);
        int golesDerecha = partido.getMarcador().getGoles(Lado.DERECHA);
        if (golesIzquierda > golesIzquierdaVistos || golesDerecha > golesDerechaVistos) {
            if (golesIzquierda > golesIzquierdaVistos) {
                ladoAnotador = Lado.IZQUIERDA;
            } else {
                ladoAnotador = Lado.DERECHA;
            }
            if (partido.getEstado() == EstadoPartido.EN_JUEGO) {
                celebracion = TIEMPO_GOL;
                teclas.clear();
            }
        }
        golesIzquierdaVistos = golesIzquierda;
        golesDerechaVistos = golesDerecha;
    }

    // -1 si esta presionada la tecla de ir a la izquierda, 1 si la de la derecha, 0 si ninguna o ambas.
    private int direccion(int teclaIzquierda, int teclaDerecha) {
        int d = 0;
        if (teclas.contains(teclaIzquierda)) {
            d = d - 1;
        }
        if (teclas.contains(teclaDerecha)) {
            d = d + 1;
        }
        return d;
    }

    // ---------------------------------------------------------------- teclado

    @Override
    public void keyPressed(KeyEvent evento) {
        int codigo = evento.getKeyCode();
        if (teclas.contains(codigo)) {
            return; // ya estaba presionada: no repetimos el salto ni la patada
        }
        teclas.add(codigo);
        if (celebracion > 0 && esTeclaDeJugador(codigo)) {
            return; // durante el gol no se puede saltar, patear ni usar el poder
        }
        try {
            switch (codigo) {
                case KeyEvent.VK_W:
                    juego.saltar(Lado.IZQUIERDA);
                    break;
                case KeyEvent.VK_S:
                    juego.patear(Lado.IZQUIERDA);
                    break;
                case KeyEvent.VK_E:
                    juego.activarPoder(Lado.IZQUIERDA);
                    break;
                case KeyEvent.VK_UP:
                    if (!derechaEsMaquina()) {
                        juego.saltar(Lado.DERECHA);
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (!derechaEsMaquina()) {
                        juego.patear(Lado.DERECHA);
                    }
                    break;
                case KeyEvent.VK_L:
                    if (!derechaEsMaquina()) {
                        juego.activarPoder(Lado.DERECHA);
                    }
                    break;
                case KeyEvent.VK_CONTROL:
                    if (evento.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT && !derechaEsMaquina()) {
                        juego.activarPoder(Lado.DERECHA);
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                case KeyEvent.VK_P:
                    alternarPausa();
                    break;
                case KeyEvent.VK_G:
                    teclas.clear();
                    ventana.guardar();
                    break;
                case KeyEvent.VK_T:
                    juego.terminar();
                    break;
                case KeyEvent.VK_M:
                    ventana.mostrarMenu();
                    break;
                case KeyEvent.VK_R:
                case KeyEvent.VK_ENTER:
                    revancha();
                    break;
                default:
                    break;
            }
        } catch (HeadSoccerException e) {
            System.out.println(e.getMessage()); // por ejemplo, pausar un partido que ya termino
        }
    }

    private boolean esTeclaDeJugador(int codigo) {
        return codigo == KeyEvent.VK_W || codigo == KeyEvent.VK_S || codigo == KeyEvent.VK_E
                || codigo == KeyEvent.VK_UP || codigo == KeyEvent.VK_DOWN || codigo == KeyEvent.VK_L
                || codigo == KeyEvent.VK_CONTROL;
    }

    // Contra la maquina el teclado del jugador 2 no hace nada.
    private boolean derechaEsMaquina() {
        Partido partido = juego.getPartido();
        return partido != null && partido.getJugador(Lado.DERECHA).esMaquina();
    }

    // ESC o P: si esta en pausa la quita, si no la pone.
    private void alternarPausa() throws HeadSoccerException {
        Partido partido = juego.getPartido();
        if (partido != null && partido.getEstado() == EstadoPartido.PAUSADO) {
            juego.reanudar();
        } else {
            juego.pausar();
        }
    }

    // La revancha solo funciona cuando el partido ya termino.
    private void revancha() throws HeadSoccerException {
        Partido partido = juego.getPartido();
        if (partido != null && partido.getEstado() == EstadoPartido.FINALIZADO) {
            ventana.revancha();
        }
    }

    @Override
    public void keyReleased(KeyEvent evento) {
        teclas.remove(evento.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent evento) {
    }

    // ---------------------------------------------------------------- dibujo

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Partido partido = juego.getPartido();
        if (partido == null) {
            return;
        }
        Graphics2D g = (Graphics2D) g0;
        // Suaviza los bordes de los circulos para que no se vean pixelados
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        calcularEscala(partido.getCampo());
        dibujarFondo(g);
        dibujarLineas(g, partido.getCampo());
        dibujarPorteria(g, partido.getCampo(), true);
        dibujarPorteria(g, partido.getCampo(), false);
        dibujarJugador(g, partido.getJugador(Lado.IZQUIERDA));
        dibujarJugador(g, partido.getJugador(Lado.DERECHA));
        dibujarBalones(g, partido);
        dibujarMarcador(g, partido);
        dibujarMensaje(g, partido);
        dibujarGol(g, partido);
    }

    // Animacion del gol: la red donde entro parpadea y aparece "GOL!" creciendo con el color del que anoto.
    private void dibujarGol(Graphics2D g, Partido partido) {
        if (celebracion <= 0 || ladoAnotador == null) {
            return;
        }
        double avance = 1 - celebracion / TIEMPO_GOL;   // va de 0 a 1
        Jugador anoto = partido.getJugador(ladoAnotador);
        Color color = Tema.colorDe(anoto.getPersonaje());
        Campo campo = partido.getCampo();

        // Red parpadeando (si anoto el de la izquierda, el gol fue en la porteria derecha)
        if ((int) (animacion * 10) % 2 == 0) {
            int ancho = (int) (campo.getAnchoPorteria() * escala);
            int alto = (int) (campo.getAltoPorteria() * escala);
            int x = pantallaX(campo.getAncho());
            if (ladoAnotador == Lado.DERECHA) {
                x = pantallaX(0) - ancho;
            }
            g.setColor(new Color(255, 255, 255, 200));
            g.fillRect(x, (int) suelo - alto, ancho, alto);
        }

        // Franja de color detras del texto
        int w = getWidth();
        int centroY = (int) (suelo - campo.getAlto() * escala * 0.82);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        g.fillRect(0, centroY - 70, w, 120);

        // "GOL!" que crece rapido al principio y luego se queda
        double crecer = Math.min(1, avance * 3);
        int tamano = (int) (30 + 70 * crecer);
        g.setFont(new Font("SansSerif", Font.BOLD, tamano));
        g.setColor(Color.BLACK);
        escribirCentrado(g, "GOL!", w / 2 + 3, centroY + 3);
        g.setColor(Color.WHITE);
        escribirCentrado(g, "GOL!", w / 2, centroY);
        g.setFont(Tema.SUBTITULO);
        escribirCentrado(g, "de " + anoto.getNombre(), w / 2, centroY + 40);
    }

    // La cancha mide segun la resolucion en el juego, pero la ventana puede tener cualquier tamano.
    // Calculamos cuanto agrandar o achicar (escala) para que todo quepa centrado.
    private void calcularEscala(Campo campo) {
        double anchoTotal = campo.getAncho() + 2 * campo.getAnchoPorteria(); // cancha + las dos porterias
        double altoDisponible = getHeight() - ALTO_MARCADOR - ALTO_PASTO;
        escala = Math.min(getWidth() / anchoTotal, altoDisponible / campo.getAlto());
        origenX = (getWidth() - anchoTotal * escala) / 2 + campo.getAnchoPorteria() * escala;
        double arriba = ALTO_MARCADOR + (altoDisponible - campo.getAlto() * escala) / 2;
        suelo = arriba + campo.getAlto() * escala;
    }

    // Convierte una x del juego en una x de la pantalla.
    private int pantallaX(double x) {
        return (int) (origenX + x * escala);
    }

    // Convierte una y del juego (crece hacia arriba) en una y de la pantalla (crece hacia abajo).
    private int pantallaY(double y) {
        return (int) (suelo - y * escala);
    }

    private void dibujarFondo(Graphics2D g) {
        g.setColor(Tema.CIELO);
        g.fillRect(0, 0, getWidth(), (int) suelo);
        g.setColor(Tema.PASTO);
        g.fillRect(0, (int) suelo, getWidth(), getHeight() - (int) suelo);
    }

    // Lineas de la cancha sobre el pasto (vistas de lado, como en perspectiva):
    // franjas del pasto, linea de fondo, mitad de cancha con su circulo y las dos areas.
    private void dibujarLineas(Graphics2D g, Campo campo) {
        int pasto = Math.min(getHeight() - (int) suelo, 60);
        int izquierda = pantallaX(0);
        int derecha = pantallaX(campo.getAncho());
        int mitad = pantallaX(campo.getAncho() / 2);

        // Franjas de pasto mas claras y mas oscuras
        int franjas = 10;
        int anchoFranja = (derecha - izquierda) / franjas;
        for (int i = 0; i < franjas; i++) {
            if (i % 2 == 0) {
                g.setColor(new Color(56, 142, 60));
                g.fillRect(izquierda + i * anchoFranja, (int) suelo, anchoFranja, getHeight() - (int) suelo);
            }
        }

        g.setColor(new Color(255, 255, 255, 220));
        g.setStroke(new BasicStroke(3));
        int abajo = (int) suelo + pasto - 6;

        // Linea de fondo (a lo largo de la cancha) y lineas de los arcos
        g.drawLine(izquierda, (int) suelo, derecha, (int) suelo);
        g.drawLine(izquierda, (int) suelo, izquierda - 12, abajo);
        g.drawLine(derecha, (int) suelo, derecha + 12, abajo);

        // Mitad de cancha y circulo central (aplanado porque se ve de lado)
        g.drawLine(mitad, (int) suelo, mitad, abajo);
        int radio = (int) (campo.getAncho() * 0.09 * escala);
        g.drawOval(mitad - radio, (int) suelo + 4, 2 * radio, pasto - 12);
        g.fillOval(mitad - 4, (int) suelo + pasto / 2 - 4, 8, 8);

        // Areas: un trapecio a cada lado y el punto penal
        int area = (int) (campo.getAncho() * 0.15 * escala);
        int alturaArea = pasto - 14;
        g.drawLine(izquierda + area, (int) suelo, izquierda + area - 6, (int) suelo + alturaArea);
        g.drawLine(izquierda - 6, (int) suelo + alturaArea, izquierda + area - 6, (int) suelo + alturaArea);
        g.drawLine(derecha - area, (int) suelo, derecha - area + 6, (int) suelo + alturaArea);
        g.drawLine(derecha + 6, (int) suelo + alturaArea, derecha - area + 6, (int) suelo + alturaArea);
        int penal = (int) (campo.getAncho() * 0.11 * escala);
        g.fillOval(izquierda + penal - 3, (int) suelo + alturaArea / 2 - 3, 6, 6);
        g.fillOval(derecha - penal - 3, (int) suelo + alturaArea / 2 - 3, 6, 6);
    }

    private void dibujarPorteria(Graphics2D g, Campo campo, boolean izquierda) {
        int ancho = (int) (campo.getAnchoPorteria() * escala);
        int alto = (int) (campo.getAltoPorteria() * escala);
        int x;
        if (izquierda) {
            x = pantallaX(0) - ancho;
        } else {
            x = pantallaX(campo.getAncho());
        }
        int arriba = (int) suelo - alto;
        // Red (rectangulo blanco transparente)
        g.setColor(new Color(255, 255, 255, 90));
        g.fillRect(x, arriba, ancho, alto);
        // Travesano y poste
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(5));
        g.drawLine(x, arriba, x + ancho, arriba);
        int poste = x;
        if (izquierda) {
            poste = x + ancho;
        }
        g.drawLine(poste, arriba, poste, (int) suelo);
    }

    private void dibujarJugador(Graphics2D g, Jugador jugador) {
        int r = (int) (Jugador.RADIO * escala);
        int cx = pantallaX(jugador.getCentroX());
        int cy = pantallaY(jugador.getCentroY());
        Color color = Tema.colorDe(jugador.getPersonaje());
        if (jugador.estaAturdido()) {
            color = Color.GRAY;
        }

        // Aura del poder activo: anillo que palpita con el color del personaje
        if (jugador.tieneAura()) {
            int extra = (int) (r * (0.35 + 0.1 * Math.sin(animacion * 10)));
            Color aura = Tema.colorDe(jugador.getPersonaje());
            g.setColor(new Color(aura.getRed(), aura.getGreen(), aura.getBlue(), 110));
            g.fillOval(cx - r - extra, cy - r - extra, 2 * (r + extra), 2 * (r + extra));
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3));
            g.drawOval(cx - r - extra, cy - r - extra, 2 * (r + extra), 2 * (r + extra));
        }

        // Cabeza
        g.setColor(color);
        g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        g.setColor(color.darker());
        g.setStroke(new BasicStroke(3));
        g.drawOval(cx - r, cy - r, 2 * r, 2 * r);

        // Ojo mirando hacia la porteria rival
        int sentido = 1;
        if (jugador.getLado() == Lado.DERECHA) {
            sentido = -1;
        }
        int ojoX = cx + sentido * r / 3;
        g.setColor(Color.WHITE);
        g.fillOval(ojoX - r / 4, cy - r / 3, r / 2, r / 2);
        g.setColor(Color.BLACK);
        if (jugador.estaAturdido()) {
            g.drawLine(ojoX - r / 6, cy - r / 6, ojoX + r / 6, cy + r / 12); // ojo en X
            g.drawLine(ojoX - r / 6, cy + r / 12, ojoX + r / 6, cy - r / 6);
        } else {
            g.fillOval(ojoX - r / 10 + sentido * r / 10, cy - r / 5, r / 5, r / 5);
        }

        // Pie cuando esta pateando
        if (jugador.estaPateando()) {
            g.setColor(Color.DARK_GRAY);
            int pieX = cx + sentido * (r + r / 2);
            g.fillOval(pieX - r / 3, cy + r / 2, 2 * r / 3, r / 3);
        }

        // Efecto del tiro de poder rival o aturdimiento, encima de la cabeza
        dibujarEstado(g, jugador, cx, cy, r);
    }

    private void dibujarEstado(Graphics2D g, Jugador jugador, int cx, int cy, int r) {
        String texto = null;
        Color color = Color.WHITE;
        Efecto efecto = jugador.getEfecto();
        if (jugador.estaAturdido()) {
            texto = "ATURDIDO zzz";
            color = Color.LIGHT_GRAY;
        } else if (efecto != null) {
            texto = efecto.getNombre().toUpperCase();
            color = Tema.ACENTO;
            if (efecto.getNombre().equals("Electrocutado")) {
                // Chispas amarillas alrededor de la cabeza
                g.setColor(Tema.ACENTO);
                g.setStroke(new BasicStroke(3));
                for (int i = 0; i < 6; i++) {
                    double angulo = animacion * 6 + i * Math.PI / 3;
                    int x1 = cx + (int) (Math.cos(angulo) * r * 1.1);
                    int y1 = cy + (int) (Math.sin(angulo) * r * 1.1);
                    int x2 = cx + (int) (Math.cos(angulo + 0.3) * r * 1.4);
                    int y2 = cy + (int) (Math.sin(angulo + 0.3) * r * 1.4);
                    g.drawLine(x1, y1, x2, y2);
                }
            }
        }
        if (texto != null) {
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.setColor(new Color(0, 0, 0, 150));
            int ancho = g.getFontMetrics().stringWidth(texto);
            g.fillRoundRect(cx - ancho / 2 - 6, cy - r - 30, ancho + 12, 20, 10, 10);
            g.setColor(color);
            escribirCentrado(g, texto, cx, cy - r - 15);
        }
    }

    // El balon real y los falsos se dibujan igual: no se puede saber cual es el real.
    private void dibujarBalones(Graphics2D g, Partido partido) {
        Balon balon = partido.getBalon();
        boolean hayFalsos = !partido.getBalonesFalsos().isEmpty();
        for (Balon falso : partido.getBalonesFalsos()) {
            dibujarBalon(g, falso, null);
        }
        Color brillo = null;
        if (balon.esTiroDePoder() && !hayFalsos) {
            brillo = Tema.colorDe(partido.getJugador(balon.getLanzador()).getPersonaje());
        }
        dibujarBalon(g, balon, brillo);
    }

    private void dibujarBalon(Graphics2D g, Balon balon, Color brillo) {
        int r = (int) (Balon.RADIO * escala);
        int cx = pantallaX(balon.getX());
        int cy = pantallaY(balon.getY());
        if (brillo != null) {
            g.setColor(new Color(brillo.getRed(), brillo.getGreen(), brillo.getBlue(), 140));
            g.fillOval(cx - 2 * r, cy - 2 * r, 4 * r, 4 * r);
        }
        g.setColor(Color.WHITE);
        g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
        g.fillOval(cx - r / 3, cy - r / 3, 2 * r / 3, 2 * r / 3);
    }

    // Franja de arriba: nombres, personajes, vida, barra de poder, goles y tiempo (o meta de goles).
    private void dibujarMarcador(Graphics2D g, Partido partido) {
        int w = getWidth();
        g.setColor(Tema.FONDO);
        g.fillRect(0, 0, w, ALTO_MARCADOR);

        dibujarInfoJugador(g, partido.getJugador(Lado.IZQUIERDA), 20, false);
        dibujarInfoJugador(g, partido.getJugador(Lado.DERECHA), w - 20, true);

        int golesIzquierda = partido.getMarcador().getGoles(Lado.IZQUIERDA);
        int golesDerecha = partido.getMarcador().getGoles(Lado.DERECHA);
        g.setColor(Tema.TEXTO);
        g.setFont(new Font("SansSerif", Font.BOLD, 40));
        escribirCentrado(g, golesIzquierda + "  -  " + golesDerecha, w / 2, 45);

        String info;
        if (partido.getConfiguracion().getTipo() == TipoPartido.POR_TIEMPO) {
            if (partido.enMuerteSubita()) {
                info = "MUERTE SUBITA";
            } else {
                int segundos = (int) Math.ceil(partido.getTiempoRestante());
                info = String.format("%d:%02d", segundos / 60, segundos % 60); // minutos:segundos
            }
        } else {
            info = "Primero a " + partido.getConfiguracion().getLimite() + " goles";
        }
        g.setFont(Tema.NORMAL);
        g.setColor(Tema.ACENTO);
        escribirCentrado(g, info, w / 2, 72);
        g.setFont(Tema.PEQUENA);
        g.setColor(Tema.TEXTO);
        escribirCentrado(g, "ESC/P pausa  |  G guardar  |  T terminar", w / 2, 98);
    }

    // Nombre, personaje, vida, barra de poder y estadisticas de un jugador.
    // x es el borde (izquierdo o derecho) desde donde se escribe.
    private void dibujarInfoJugador(Graphics2D g, Jugador jugador, int x, boolean alaDerecha) {
        Color color = Tema.colorDe(jugador.getPersonaje());
        String nombre = jugador.getNombre();
        String personaje = jugador.getPersonaje().getPais() + " - " + jugador.getPersonaje().getNombrePoder();
        int izquierda = x;
        if (alaDerecha) {
            izquierda = x - ANCHO_BARRA;
        }

        g.setFont(Tema.SUBTITULO);
        g.setColor(color);
        escribirDesde(g, nombre, x, 26, alaDerecha);
        g.setFont(Tema.PEQUENA);
        g.setColor(Tema.TEXTO);
        escribirDesde(g, personaje, x, 44, alaDerecha);

        // Vida
        double vida = jugador.getVida() / jugador.getVidaMax();
        Color colorVida = Tema.VIDA;
        if (vida < 0.3) {
            colorVida = Tema.VIDA_BAJA;
        }
        dibujarBarra(g, izquierda, 52, vida, colorVida, alaDerecha);
        g.setColor(Tema.TEXTO);
        String textoVida = "Vida " + (int) Math.ceil(jugador.getVida()) + " / " + (int) jugador.getVidaMax();
        escribirDesde(g, textoVida, x, 76, alaDerecha);

        // Poder
        dibujarBarra(g, izquierda, 82, jugador.getBarraPoder(), Tema.ACENTO, alaDerecha);
        String textoPoder;
        if (jugador.tieneAura()) {
            textoPoder = "AURA ACTIVA";
        } else if (jugador.barraLlena()) {
            textoPoder = "PODER LISTO";
        } else {
            textoPoder = "Poder " + (int) (jugador.getBarraPoder() * 100) + " %";
        }
        if (jugador.esMaquina()) {
            textoPoder = textoPoder + "  (dificultad " + jugador.getMaquina().getDificultad().toString().toLowerCase() + ")";
        }
        g.setColor(Tema.TEXTO);
        escribirDesde(g, textoPoder, x, 108, alaDerecha);
    }

    private void dibujarBarra(Graphics2D g, int x, int y, double lleno, Color color, boolean alaDerecha) {
        int alto = 10;
        g.setColor(Tema.BARRA_VACIA);
        g.fillRect(x, y, ANCHO_BARRA, alto);
        int ancho = (int) (ANCHO_BARRA * Math.max(0, Math.min(1, lleno)));
        g.setColor(color);
        if (alaDerecha) {
            g.fillRect(x + ANCHO_BARRA - ancho, y, ancho, alto); // la del jugador 2 se llena hacia el centro
        } else {
            g.fillRect(x, y, ancho, alto);
        }
    }

    // Mensaje grande en el centro cuando esta en pausa o el partido termino.
    private void dibujarMensaje(Graphics2D g, Partido partido) {
        String titulo;
        String detalle = null;
        String ayuda;
        if (partido.getEstado() == EstadoPartido.PAUSADO) {
            titulo = "PAUSA";
            ayuda = "ESC o P para continuar   |   G guardar   |   M menu";
        } else if (partido.getEstado() == EstadoPartido.FINALIZADO) {
            Jugador ganador = partido.getGanador();
            if (ganador == null) {
                titulo = "EMPATE";
            } else {
                titulo = "GANA " + ganador.getNombre().toUpperCase();
            }
            Jugador izquierdo = partido.getJugador(Lado.IZQUIERDA);
            Jugador derecho = partido.getJugador(Lado.DERECHA);
            detalle = izquierdo.getNombre() + " +" + partido.getPuntosGanados(Lado.IZQUIERDA) + " puntos   |   "
                    + derecho.getNombre() + " +" + partido.getPuntosGanados(Lado.DERECHA) + " puntos";
            ayuda = "R o Enter revancha   |   M menu";
        } else {
            return; // se esta jugando: no hay mensaje
        }
        int w = getWidth();
        int h = getHeight();
        g.setColor(new Color(0, 0, 0, 150)); // oscurece la cancha
        g.fillRect(0, ALTO_MARCADOR, w, h - ALTO_MARCADOR);
        g.setColor(Tema.ACENTO);
        g.setFont(new Font("SansSerif", Font.BOLD, 56));
        escribirCentrado(g, titulo, w / 2, h / 2);
        g.setColor(Tema.TEXTO);
        g.setFont(Tema.SUBTITULO);
        int y = h / 2 + 50;
        if (detalle != null) {
            escribirCentrado(g, detalle, w / 2, y);
            y = y + 40;
        }
        escribirCentrado(g, ayuda, w / 2, y);
    }

    private void escribirCentrado(Graphics2D g, String texto, int x, int y) {
        int ancho = g.getFontMetrics().stringWidth(texto);
        g.drawString(texto, x - ancho / 2, y);
    }

    // Escribe desde x hacia la derecha, o terminando en x si alaDerecha es true.
    private void escribirDesde(Graphics2D g, String texto, int x, int y, boolean alaDerecha) {
        if (alaDerecha) {
            x = x - g.getFontMetrics().stringWidth(texto);
        }
        g.drawString(texto, x, y);
    }
}
