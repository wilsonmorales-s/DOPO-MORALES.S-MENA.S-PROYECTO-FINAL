package presentation;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

// Menu principal: JUGAR, CONTROLES y SALIR.
public class PanelMenu extends JPanel {

    private static final String AYUDA =
            "Jugador 1 (izquierda)\n"
            + "   A / D mover     W saltar     S patear\n\n"
            + "Jugador 2 (derecha)\n"
            + "   Flechas izquierda / derecha mover\n"
            + "   Flecha arriba saltar     Flecha abajo patear\n\n"
            + "Durante el partido\n"
            + "   ESC o P pausa     T terminar     M volver al menu\n"
            + "   R o Enter revancha (cuando termina)";

    private HeadSoccerGUI ventana;
    private JButton jugar;
    private JButton controles;
    private JButton salir;

    public PanelMenu(HeadSoccerGUI ventana) {
        this.ventana = ventana;
        prepareElementos();
        prepareAcciones();
    }

    private void prepareElementos() {
        setBackground(Tema.FONDO);
        setLayout(new GridBagLayout()); // acomoda todo en una columna centrada
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("HEAD SOCCER", SwingConstants.CENTER);
        titulo.setFont(Tema.TITULO);
        titulo.setForeground(Tema.ACENTO);
        c.insets = new Insets(0, 0, 40, 0);
        add(titulo, c);

        jugar = crearBoton("JUGAR");
        controles = crearBoton("CONTROLES");
        salir = crearBoton("SALIR");
        c.insets = new Insets(10, 0, 10, 0);
        add(jugar, c);
        add(controles, c);
        add(salir, c);
    }

    // Lo que pasa al hacer clic en cada boton.
    private void prepareAcciones() {
        jugar.addActionListener(e -> ventana.mostrarConfiguracion());
        controles.addActionListener(e ->
                JOptionPane.showMessageDialog(this, AYUDA, "Controles", JOptionPane.PLAIN_MESSAGE));
        salir.addActionListener(e -> System.exit(0));
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(Tema.BOTON);
        boton.setFocusPainted(false);
        boton.setPreferredSize(new Dimension(260, 50));
        return boton;
    }
}
