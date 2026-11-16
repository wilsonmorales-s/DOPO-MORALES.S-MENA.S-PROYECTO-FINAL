package presentation;

import domain.Balon;
import domain.Campo;
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

    private static final int ALTO_MARCADOR = 80; // franja de arriba con nombres y goles
    private static final int ALTO_PASTO = 40;    // franja de pasto debajo de la cancha

    private HeadSoccerGUI ventana;
    private HeadSoccer juego;
    private Timer reloj;                // llama a paso() cada 16 ms (unas 60 veces por segundo)
    private HashSet<Integer> teclas;    // teclas que estan presionadas en este momento
    private long ultimo;                // momento del ultimo cuadro, para medir cuanto tiempo paso

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

        try {
            juego.moverJugador(Lado.IZQUIERDA, direccion(KeyEvent.VK_A, KeyEvent.VK_D));
            juego.moverJugador(Lado.DERECHA, direccion(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT));
            juego.actualizar(dt);
        } catch (HeadSoccerException e) {
            System.out.println(e.getMessage());
            detener();
        }
        repaint(); // vuelve a dibujar (llama a paintComponent)
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
        try {
            switch (codigo) {
                case KeyEvent.VK_W:
                    juego.saltar(Lado.IZQUIERDA);
                    break;
                case KeyEvent.VK_S:
                    juego.patear(Lado.IZQUIERDA);
                    break;
                case KeyEvent.VK_UP:
                    juego.saltar(Lado.DERECHA);
                    break;
                case KeyEvent.VK_DOWN:
                    juego.patear(Lado.DERECHA);
                    break;
                case KeyEvent.VK_ESCAPE:
                case KeyEvent.VK_P:
                    alternarPausa();
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
            System.out.println(e.getMessage());
        }
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
        dibujarPorteria(g, partido.getCampo(), true);
        dibujarPorteria(g, partido.getCampo(), false);
        dibujarJugador(g, partido.getJugador(Lado.IZQUIERDA), Tema.JUGADOR_IZQUIERDO);
        dibujarJugador(g, partido.getJugador(Lado.DERECHA), Tema.JUGADOR_DERECHO);
        dibujarBalon(g, partido.getBalon());
        dibujarMarcador(g, partido);
        dibujarMensaje(g, partido);
    }

    // La cancha mide 1000 x 500 en el juego, pero la ventana puede tener cualquier tamano.
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

    private void dibujarJugador(Graphics2D g, Jugador jugador, Color color) {
        int r = (int) (Jugador.RADIO * escala);
        int cx = pantallaX(jugador.getCentroX());
        int cy = pantallaY(jugador.getCentroY());

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
        g.fillOval(ojoX - r / 10 + sentido * r / 10, cy - r / 5, r / 5, r / 5);

        // Pie cuando esta pateando
        if (jugador.estaPateando()) {
            g.setColor(Color.DARK_GRAY);
            int pieX = cx + sentido * (r + r / 2);
            g.fillOval(pieX - r / 3, cy + r / 2, 2 * r / 3, r / 3);
        }
    }

    private void dibujarBalon(Graphics2D g, Balon balon) {
        int r = (int) (Balon.RADIO * escala);
        int cx = pantallaX(balon.getX());
        int cy = pantallaY(balon.getY());
        g.setColor(Color.WHITE);
        g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
        g.fillOval(cx - r / 3, cy - r / 3, 2 * r / 3, 2 * r / 3);
    }

    // Franja de arriba: nombres, goles y tiempo (o meta de goles).
    private void dibujarMarcador(Graphics2D g, Partido partido) {
        int w = getWidth();
        g.setColor(Tema.FONDO);
        g.fillRect(0, 0, w, ALTO_MARCADOR);

        g.setFont(Tema.SUBTITULO);
        g.setColor(Tema.JUGADOR_IZQUIERDO);
        g.drawString(partido.getJugador(Lado.IZQUIERDA).getNombre(), 20, 35);
        g.setColor(Tema.JUGADOR_DERECHO);
        String nombreDerecho = partido.getJugador(Lado.DERECHA).getNombre();
        g.drawString(nombreDerecho, w - 20 - g.getFontMetrics().stringWidth(nombreDerecho), 35);

        int golesIzquierda = partido.getMarcador().getGoles(Lado.IZQUIERDA);
        int golesDerecha = partido.getMarcador().getGoles(Lado.DERECHA);
        g.setColor(Tema.TEXTO);
        g.setFont(new Font("SansSerif", Font.BOLD, 40));
        escribirCentrado(g, golesIzquierda + "  -  " + golesDerecha, w / 2, 40);

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
        escribirCentrado(g, info, w / 2, 68);
    }

    // Mensaje grande en el centro cuando esta en pausa o el partido termino.
    private void dibujarMensaje(Graphics2D g, Partido partido) {
        String titulo;
        String ayuda;
        if (partido.getEstado() == EstadoPartido.PAUSADO) {
            titulo = "PAUSA";
            ayuda = "ESC o P para continuar   |   M menu";
        } else if (partido.getEstado() == EstadoPartido.FINALIZADO) {
            Jugador ganador = partido.getGanador();
            if (ganador == null) {
                titulo = "EMPATE";
            } else {
                titulo = "GANA " + ganador.getNombre().toUpperCase();
            }
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
        escribirCentrado(g, ayuda, w / 2, h / 2 + 50);
    }

    private void escribirCentrado(Graphics2D g, String texto, int x, int y) {
        int ancho = g.getFontMetrics().stringWidth(texto);
        g.drawString(texto, x - ancho / 2, y);
    }
}
