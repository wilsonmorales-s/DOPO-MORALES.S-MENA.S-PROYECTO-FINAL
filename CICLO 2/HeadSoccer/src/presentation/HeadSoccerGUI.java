package presentation;

import domain.ConfiguracionPartido;
import domain.HeadSoccer;
import domain.HeadSoccerException;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

// Ventana principal. Tiene tres pantallas (menu, configuracion y juego)
// y muestra una a la vez, como un mazo de cartas (CardLayout).
public class HeadSoccerGUI extends JFrame {

    private static final String MENU = "menu";
    private static final String CONFIGURACION = "configuracion";
    private static final String JUEGO = "juego";

    private HeadSoccer juego;          // la fachada del dominio
    private CardLayout cartas;
    private JPanel contenedor;
    private PanelMenu panelMenu;
    private PanelConfiguracion panelConfiguracion;
    private PanelJuego panelJuego;

    public HeadSoccerGUI() {
        super("Head Soccer");
        juego = new HeadSoccer();
        prepareElementos();
        prepareAcciones();
    }

    private void prepareElementos() {
        cartas = new CardLayout();
        contenedor = new JPanel(cartas);
        panelMenu = new PanelMenu(this);
        panelConfiguracion = new PanelConfiguracion(this);
        panelJuego = new PanelJuego(this, juego);
        contenedor.add(panelMenu, MENU);
        contenedor.add(panelConfiguracion, CONFIGURACION);
        contenedor.add(panelJuego, JUEGO);
        add(contenedor);
        setMinimumSize(new Dimension(800, 520));
        setSize(1100, 700);
        setLocationRelativeTo(null); // centrada en la pantalla
    }

    private void prepareAcciones() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void mostrarMenu() {
        panelJuego.detener();
        cartas.show(contenedor, MENU);
    }

    public void mostrarConfiguracion() {
        cartas.show(contenedor, CONFIGURACION);
    }

    // Crea el partido en el dominio y pasa a la pantalla de juego.
    public void comenzar(ConfiguracionPartido configuracion) throws HeadSoccerException {
        juego.iniciarPartido(configuracion);
        mostrarJuego();
    }

    // Vuelve a jugar con la misma configuracion.
    public void revancha() throws HeadSoccerException {
        juego.revancha();
        mostrarJuego();
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
