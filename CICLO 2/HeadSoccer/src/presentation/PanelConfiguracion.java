package presentation;

import domain.ConfiguracionPartido;
import domain.HeadSoccerException;
import domain.TipoPartido;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

// Pantalla para configurar el partido: tipo, limite (minutos o goles) y nombres.
public class PanelConfiguracion extends JPanel {

    private HeadSoccerGUI ventana;
    private JRadioButton porTiempo;
    private JRadioButton porGoles;
    private JLabel etiquetaLimite;
    private JSpinner limite;          // caja de numero con flechitas
    private JTextField nombre1;
    private JTextField nombre2;
    private JLabel error;             // aqui se muestra el mensaje si algo esta mal
    private JButton volver;
    private JButton comenzar;

    public PanelConfiguracion(HeadSoccerGUI ventana) {
        this.ventana = ventana;
        prepareElementos();
        prepareAcciones();
    }

    private void prepareElementos() {
        setBackground(Tema.FONDO);
        setLayout(new GridBagLayout()); // cuadricula: etiquetas a la izquierda, campos a la derecha

        porTiempo = new JRadioButton("Por tiempo", true);
        porGoles = new JRadioButton("Por goles");
        ButtonGroup grupo = new ButtonGroup(); // para que solo se pueda elegir uno
        grupo.add(porTiempo);
        grupo.add(porGoles);
        darEstilo(porTiempo);
        darEstilo(porGoles);

        etiquetaLimite = crearEtiqueta("Minutos:");
        limite = new JSpinner(new SpinnerNumberModel(3,
                ConfiguracionPartido.MIN_LIMITE, ConfiguracionPartido.MAX_LIMITE, 1));
        nombre1 = new JTextField("Jugador 1", 14);
        nombre2 = new JTextField("Jugador 2", 14);
        error = new JLabel(" ", SwingConstants.CENTER);
        error.setForeground(new Color(255, 138, 128));
        error.setFont(Tema.NORMAL);
        volver = new JButton("VOLVER");
        comenzar = new JButton("COMENZAR");
        volver.setFont(Tema.BOTON);
        comenzar.setFont(Tema.BOTON);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: titulo
        JLabel titulo = new JLabel("CONFIGURAR PARTIDO", SwingConstants.CENTER);
        titulo.setFont(Tema.SUBTITULO);
        titulo.setForeground(Tema.ACENTO);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        add(titulo, c);

        // Fila 1: tipo de partido
        JPanel tipos = new JPanel();
        tipos.setOpaque(false);
        tipos.add(porTiempo);
        tipos.add(porGoles);
        agregarFila(1, crearEtiqueta("Tipo de partido:"), tipos, c);

        // Filas 2 a 4: limite y nombres
        agregarFila(2, etiquetaLimite, limite, c);
        agregarFila(3, crearEtiqueta("Jugador 1 (izquierda):"), nombre1, c);
        agregarFila(4, crearEtiqueta("Jugador 2 (derecha):"), nombre2, c);

        // Fila 5: mensaje de error
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        add(error, c);

        // Fila 6: botones
        JPanel botones = new JPanel();
        botones.setOpaque(false);
        botones.add(volver);
        botones.add(comenzar);
        c.gridy = 6;
        add(botones, c);
    }

    private void prepareAcciones() {
        // La etiqueta cambia segun el tipo: el limite son minutos o goles
        porTiempo.addActionListener(e -> etiquetaLimite.setText("Minutos:"));
        porGoles.addActionListener(e -> etiquetaLimite.setText("Goles:"));
        volver.addActionListener(e -> ventana.mostrarMenu());
        comenzar.addActionListener(e -> comenzar());
    }

    // Arma la configuracion con lo que escribio el usuario y empieza el partido.
    // Si algo esta mal, el dominio lanza la excepcion y aqui se muestra el mensaje.
    private void comenzar() {
        try {
            TipoPartido tipo = TipoPartido.POR_GOLES;
            if (porTiempo.isSelected()) {
                tipo = TipoPartido.POR_TIEMPO;
            }
            int valorLimite = (Integer) limite.getValue();
            ConfiguracionPartido configuracion =
                    new ConfiguracionPartido(tipo, valorLimite, nombre1.getText(), nombre2.getText());
            error.setText(" ");
            ventana.comenzar(configuracion);
        } catch (HeadSoccerException e) {
            error.setText(e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    private void agregarFila(int fila, JLabel etiqueta, java.awt.Component campo, GridBagConstraints c) {
        c.gridwidth = 1;
        c.gridy = fila;
        c.gridx = 0;
        add(etiqueta, c);
        c.gridx = 1;
        add(campo, c);
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(Tema.TEXTO);
        etiqueta.setFont(Tema.NORMAL);
        return etiqueta;
    }

    private void darEstilo(JRadioButton opcion) {
        opcion.setOpaque(false);
        opcion.setForeground(Tema.TEXTO);
        opcion.setFont(Tema.NORMAL);
    }
}
