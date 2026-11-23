package presentation;

import domain.Estadistica;
import domain.Estadisticas;
import domain.HeadSoccer;
import domain.HeadSoccerException;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

// Pantalla para mejorar las estadisticas de un jugador con los puntos que ha ganado.
// Cada punto de mejora cuesta el doble que el anterior (500, 1000, 2000...).
// Las mejoras aplican desde el siguiente partido.
public class PanelMejoras extends JPanel {

    private static final String[] DESCRIPCIONES = {
        "Velocidad al caminar",
        "Dano que le hace al rival",
        "Altura del salto",
        "Vida maxima"
    };

    private HeadSoccerGUI ventana;
    private HeadSoccer juego;
    private JComboBox<String> jugador;   // se puede elegir un jugador guardado o escribir un nombre
    private JLabel puntos;
    private JLabel[] niveles;
    private JButton[] mejorar;
    private JLabel mensaje;
    private JButton volver;

    public PanelMejoras(HeadSoccerGUI ventana, HeadSoccer juego) {
        this.ventana = ventana;
        this.juego = juego;
        prepareElementos();
        prepareAcciones();
    }

    private void prepareElementos() {
        setBackground(Tema.FONDO);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 10, 6, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("MEJORAR ESTADISTICAS", SwingConstants.CENTER);
        titulo.setFont(Tema.SUBTITULO);
        titulo.setForeground(Tema.ACENTO);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        add(titulo, c);

        jugador = new JComboBox<String>();
        jugador.setEditable(true);
        puntos = crearEtiqueta("Puntos: 0");
        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = 0;
        add(crearEtiqueta("Jugador:"), c);
        c.gridx = 1;
        c.gridwidth = 2;
        add(jugador, c);
        c.gridx = 3;
        c.gridwidth = 1;
        add(puntos, c);

        Estadistica[] estadisticas = Estadistica.values();
        niveles = new JLabel[estadisticas.length];
        mejorar = new JButton[estadisticas.length];
        for (int i = 0; i < estadisticas.length; i++) {
            niveles[i] = crearEtiqueta("1 / 10");
            mejorar[i] = new JButton("Mejorar");
            c.gridy = 2 + i;
            c.gridx = 0;
            add(crearEtiqueta(estadisticas[i].name()), c);
            c.gridx = 1;
            add(crearEtiqueta(DESCRIPCIONES[i]), c);
            c.gridx = 2;
            add(niveles[i], c);
            c.gridx = 3;
            add(mejorar[i], c);
        }

        mensaje = new JLabel(" ", SwingConstants.CENTER);
        mensaje.setFont(Tema.NORMAL);
        mensaje.setForeground(Tema.ERROR);
        c.gridy = 2 + estadisticas.length;
        c.gridx = 0;
        c.gridwidth = 4;
        add(mensaje, c);

        JLabel nota = crearEtiqueta("Ganador +100 puntos, perdedor +25. Las mejoras aplican desde el siguiente partido.");
        nota.setFont(Tema.PEQUENA);
        c.gridy = c.gridy + 1;
        add(nota, c);

        volver = new JButton("VOLVER");
        volver.setFont(Tema.BOTON);
        c.gridy = c.gridy + 1;
        c.fill = GridBagConstraints.NONE;
        add(volver, c);
    }

    private void prepareAcciones() {
        jugador.addActionListener(e -> refrescar());
        Estadistica[] estadisticas = Estadistica.values();
        for (int i = 0; i < estadisticas.length; i++) {
            Estadistica estadistica = estadisticas[i];
            mejorar[i].addActionListener(e -> mejorar(estadistica));
        }
        volver.addActionListener(e -> ventana.mostrarMenu());
    }

    // Recarga la lista de jugadores con progreso guardado.
    public void actualizar() {
        Object actual = jugador.getSelectedItem();
        jugador.removeAllItems();
        List<String> nombres = juego.getJugadoresRegistrados();
        for (String nombre : nombres) {
            jugador.addItem(nombre);
        }
        if (actual != null) {
            jugador.setSelectedItem(actual);
        }
        mensaje.setText(" ");
        refrescar();
    }

    // Muestra los puntos, los niveles y el precio de la siguiente mejora del jugador elegido.
    private void refrescar() {
        String nombre = nombreElegido();
        Estadistica[] estadisticas = Estadistica.values();
        try {
            puntos.setText("Puntos: " + juego.getPuntos(nombre));
            Estadisticas suyas = juego.getEstadisticas(nombre);
            for (int i = 0; i < estadisticas.length; i++) {
                int nivel = suyas.getNivel(estadisticas[i]);
                niveles[i].setText(nivel + " / " + Estadisticas.NIVEL_MAXIMO);
                if (nivel < Estadisticas.NIVEL_MAXIMO) {
                    mejorar[i].setText("Mejorar (" + juego.getPrecioMejora(nombre, estadisticas[i]) + " pts)");
                    mejorar[i].setEnabled(true);
                } else {
                    mejorar[i].setText("Maximo");
                    mejorar[i].setEnabled(false);
                }
            }
        } catch (HeadSoccerException e) {
            puntos.setText("Puntos: -");
            for (int i = 0; i < estadisticas.length; i++) {
                niveles[i].setText("- / " + Estadisticas.NIVEL_MAXIMO);
                mejorar[i].setText("Mejorar");
            }
        }
    }

    private void mejorar(Estadistica estadistica) {
        try {
            juego.mejorarEstadistica(nombreElegido(), estadistica);
            mensaje.setText(" ");
            refrescar();
        } catch (HeadSoccerException e) {
            mensaje.setText(e.getMessage());
            System.out.println(e.getMessage());
        } catch (RuntimeException e) {
            ventana.mostrarErrorInesperado(e);
        }
    }

    private String nombreElegido() {
        Object elegido = jugador.getSelectedItem();
        String nombre = "";
        if (elegido != null) {
            nombre = elegido.toString();
        }
        return nombre;
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(Tema.TEXTO);
        etiqueta.setFont(Tema.NORMAL);
        return etiqueta;
    }
}
