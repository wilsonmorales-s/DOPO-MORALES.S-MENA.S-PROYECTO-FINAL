package presentation;

import domain.ConfiguracionPartido;
import domain.EstadoPartido;
import domain.HeadSoccer;
import domain.HeadSoccerException;
import domain.Log;
import domain.Partido;
import domain.Resolucion;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

// Ventana principal. Tiene cuatro pantallas (menu, configuracion, mejoras y juego)
// y muestra una a la vez, como un mazo de cartas (CardLayout).
// El menu Archivo permite empezar un partido nuevo, abrir y guardar partidas.
public class HeadSoccerGUI extends JFrame {

    private static final String MENU = "menu";
    private static final String CONFIGURACION = "configuracion";
    private static final String MEJORAS = "mejoras";
    private static final String JUEGO = "juego";
    private static final String EXTENSION = "hsp";   // partidas guardadas: *.hsp

    private HeadSoccer juego;          // la fachada del dominio
    private CardLayout cartas;
    private JPanel contenedor;
    private PanelMenu panelMenu;
    private PanelConfiguracion panelConfiguracion;
    private PanelMejoras panelMejoras;
    private PanelJuego panelJuego;
    private JMenuItem nuevo;
    private JMenuItem abrir;
    private JMenuItem guardar;
    private JMenuItem salir;
    private JFileChooser selector;

    public HeadSoccerGUI() {
        super("Head Soccer");
        juego = new HeadSoccer(new File(HeadSoccer.ARCHIVO_PROGRESO));
        prepareElementos();
        prepareAcciones();
    }

    private void prepareElementos() {
        cartas = new CardLayout();
        contenedor = new JPanel(cartas);
        panelMenu = new PanelMenu(this);
        panelConfiguracion = new PanelConfiguracion(this);
        panelMejoras = new PanelMejoras(this, juego);
        panelJuego = new PanelJuego(this, juego);
        contenedor.add(panelMenu, MENU);
        contenedor.add(panelConfiguracion, CONFIGURACION);
        contenedor.add(panelMejoras, MEJORAS);
        contenedor.add(panelJuego, JUEGO);
        add(contenedor);
        prepareElementosMenu();
        selector = new JFileChooser(new File("."));
        selector.setFileFilter(new FileNameExtensionFilter("Partidas de Head Soccer (*.hsp)", EXTENSION));
        setMinimumSize(new Dimension(800, 560));
        setSize(1100, 720);
        setLocationRelativeTo(null); // centrada en la pantalla
    }

    private void prepareElementosMenu() {
        JMenuBar barra = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");
        nuevo = new JMenuItem("Nuevo partido");
        abrir = new JMenuItem("Abrir partida...");
        guardar = new JMenuItem("Guardar partida...");
        salir = new JMenuItem("Salir");
        archivo.add(nuevo);
        archivo.add(abrir);
        archivo.add(guardar);
        archivo.addSeparator();
        archivo.add(salir);
        barra.add(archivo);
        setJMenuBar(barra);
    }

    private void prepareAcciones() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        nuevo.addActionListener(e -> mostrarConfiguracion());
        abrir.addActionListener(e -> abrir());
        guardar.addActionListener(e -> guardar());
        salir.addActionListener(e -> System.exit(0));
    }

    public void mostrarMenu() {
        panelJuego.detener();
        cartas.show(contenedor, MENU);
    }

    public void mostrarConfiguracion() {
        panelJuego.detener();
        cartas.show(contenedor, CONFIGURACION);
    }

    public void mostrarMejoras() {
        panelJuego.detener();
        panelMejoras.actualizar();
        cartas.show(contenedor, MEJORAS);
    }

    // Crea el partido en el dominio y pasa a la pantalla de juego.
    public void comenzar(ConfiguracionPartido configuracion) throws HeadSoccerException {
        juego.iniciarPartido(configuracion);
        ajustarTamano(configuracion.getResolucion());
        mostrarJuego();
    }

    // Vuelve a jugar con la misma configuracion.
    public void revancha() throws HeadSoccerException {
        juego.revancha();
        mostrarJuego();
    }

    // Abre una partida guardada. Queda en pausa: ESC o P para seguir.
    public void abrir() {
        panelJuego.detener();
        int opcion = selector.showOpenDialog(this);
        if (opcion == JFileChooser.APPROVE_OPTION) {
            try {
                juego.abrir(selector.getSelectedFile());
                ajustarTamano(juego.getPartido().getCampo().getResolucion());
                mostrarJuego();
            } catch (HeadSoccerException e) {
                mostrarError(e);
            } catch (RuntimeException e) {
                mostrarErrorInesperado(e);
            }
        } else if (juego.hayPartido() && panelJuego.isShowing()) {
            panelJuego.iniciar();
        }
    }

    // Guarda la partida actual. Si se estaba jugando, la pausa primero.
    public void guardar() {
        if (juego.hayPartido()) {
            boolean enJuego = panelJuego.isShowing();
            try {
                Partido partido = juego.getPartido();
                if (partido.getEstado() == EstadoPartido.EN_JUEGO) {
                    juego.pausar();
                }
                int opcion = selector.showSaveDialog(this);
                if (opcion == JFileChooser.APPROVE_OPTION) {
                    File archivo = selector.getSelectedFile();
                    if (!archivo.getName().toLowerCase().endsWith("." + EXTENSION)) {
                        archivo = new File(archivo.getParentFile(), archivo.getName() + "." + EXTENSION);
                    }
                    juego.guardar(archivo);
                    JOptionPane.showMessageDialog(this, "Partida guardada en " + archivo.getName(),
                            "Guardar partida", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (HeadSoccerException e) {
                mostrarError(e);
            } catch (RuntimeException e) {
                mostrarErrorInesperado(e);
            }
            if (enJuego) {
                panelJuego.iniciar();
            }
        } else {
            mostrarError(new HeadSoccerException(HeadSoccerException.SIN_PARTIDO));
        }
    }

    // Error esperado: se le muestra al usuario el mensaje de la excepcion.
    public void mostrarError(HeadSoccerException e) {
        System.out.println(e.getMessage());
        JOptionPane.showMessageDialog(this, e.getMessage(), "Head Soccer", JOptionPane.ERROR_MESSAGE);
    }

    // Error que no deberia pasar: va al log para los programadores y el usuario ve un aviso.
    public void mostrarErrorInesperado(Exception e) {
        Log.registre(e);
        JOptionPane.showMessageDialog(this, "Ocurrio un error inesperado. Se guardo el detalle en "
                + Log.ARCHIVO, "Head Soccer", JOptionPane.ERROR_MESSAGE);
    }

    // La ventana se agranda o se achica segun la resolucion de la cancha.
    private void ajustarTamano(Resolucion resolucion) {
        if (resolucion == Resolucion.PEQUENA) {
            setSize(900, 580);
        } else if (resolucion == Resolucion.GRANDE) {
            setSize(1340, 860);
        } else {
            setSize(1100, 720);
        }
        setLocationRelativeTo(null);
    }

    private void mostrarJuego() {
        cartas.show(contenedor, JUEGO);
        panelJuego.iniciar();
    }

    public static void main(String[] args) {
        HeadSoccerGUI ventana = new HeadSoccerGUI();
        ventana.setVisible(true);
    }
}
