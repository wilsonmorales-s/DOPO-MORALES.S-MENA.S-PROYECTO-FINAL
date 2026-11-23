package presentation;

import domain.ConfiguracionPartido;
import domain.Dificultad;
import domain.HeadSoccerException;
import domain.Modalidad;
import domain.PerfilMaquina;
import domain.Personaje;
import domain.Resolucion;
import domain.TipoPartido;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

// Pantalla para configurar el partido: modalidad (y perfil de la maquina), tipo, limite,
// resolucion de la cancha, y el nombre y el personaje de cada jugador.
public class PanelConfiguracion extends JPanel {

    private HeadSoccerGUI ventana;
    private JRadioButton contraJugador;
    private JRadioButton contraMaquina;
    private JComboBox<PerfilMaquina> perfil;
    private JComboBox<Dificultad> dificultad;
    private JRadioButton porTiempo;
    private JRadioButton porGoles;
    private JLabel etiquetaLimite;
    private JSpinner limite;          // caja de numero con flechitas
    private JComboBox<Resolucion> resolucion;
    private JTextField nombre1;
    private JTextField nombre2;
    private JComboBox<Personaje> personaje1;
    private JComboBox<Personaje> personaje2;
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

        contraJugador = new JRadioButton("Jugador vs Jugador", true);
        contraMaquina = new JRadioButton("Jugador vs Maquina");
        agrupar(contraJugador, contraMaquina);
        perfil = new JComboBox<PerfilMaquina>(PerfilMaquina.disponibles().toArray(new PerfilMaquina[0]));
        perfil.setEnabled(false);
        dificultad = new JComboBox<Dificultad>(Dificultad.values());
        dificultad.setSelectedItem(Dificultad.MEDIA);
        dificultad.setEnabled(false);

        porTiempo = new JRadioButton("Por tiempo", true);
        porGoles = new JRadioButton("Por goles");
        agrupar(porTiempo, porGoles);

        etiquetaLimite = crearEtiqueta("Minutos:");
        limite = new JSpinner(new SpinnerNumberModel(3,
                ConfiguracionPartido.MIN_LIMITE, ConfiguracionPartido.MAX_LIMITE, 1));
        resolucion = new JComboBox<Resolucion>(Resolucion.values());
        resolucion.setSelectedItem(Resolucion.MEDIANA);

        nombre1 = new JTextField("Jugador 1", 14);
        nombre2 = new JTextField("Jugador 2", 14);
        Personaje[] personajes = Personaje.disponibles().toArray(new Personaje[0]);
        personaje1 = new JComboBox<Personaje>(personajes);
        personaje2 = new JComboBox<Personaje>(Personaje.disponibles().toArray(new Personaje[0]));
        personaje2.setSelectedIndex(1);

        error = new JLabel(" ", SwingConstants.CENTER);
        error.setForeground(Tema.ERROR);
        error.setFont(Tema.NORMAL);
        volver = new JButton("VOLVER");
        comenzar = new JButton("COMENZAR");
        volver.setFont(Tema.BOTON);
        comenzar.setFont(Tema.BOTON);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 10, 6, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: titulo
        JLabel titulo = new JLabel("CONFIGURAR PARTIDO", SwingConstants.CENTER);
        titulo.setFont(Tema.SUBTITULO);
        titulo.setForeground(Tema.ACENTO);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        add(titulo, c);

        agregarFila(1, crearEtiqueta("Modalidad:"), enFila(contraJugador, contraMaquina), c);
        agregarFila(2, crearEtiqueta("Perfil de la maquina:"), enFila(perfil, dificultad), c);
        agregarFila(3, crearEtiqueta("Tipo de partido:"), enFila(porTiempo, porGoles), c);
        agregarFila(4, etiquetaLimite, limite, c);
        agregarFila(5, crearEtiqueta("Resolucion de la cancha:"), resolucion, c);
        agregarFila(6, crearEtiqueta("Jugador 1 (izquierda):"), enFila(nombre1, personaje1), c);
        agregarFila(7, crearEtiqueta("Jugador 2 (derecha):"), enFila(nombre2, personaje2), c);

        // Fila 8: mensaje de error
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 2;
        add(error, c);

        // Fila 9: botones
        c.gridy = 9;
        add(enFila(volver, comenzar), c);
    }

    private void prepareAcciones() {
        // La etiqueta cambia segun el tipo: el limite son minutos o goles
        porTiempo.addActionListener(e -> etiquetaLimite.setText("Minutos:"));
        porGoles.addActionListener(e -> etiquetaLimite.setText("Goles:"));
        // Contra la maquina se elige el perfil y el jugador 2 se llama como la maquina
        contraJugador.addActionListener(e -> actualizarModalidad());
        contraMaquina.addActionListener(e -> actualizarModalidad());
        perfil.addActionListener(e -> actualizarModalidad());
        volver.addActionListener(e -> ventana.mostrarMenu());
        comenzar.addActionListener(e -> comenzar());
    }

    private void actualizarModalidad() {
        boolean maquina = contraMaquina.isSelected();
        perfil.setEnabled(maquina);
        dificultad.setEnabled(maquina);
        nombre2.setEnabled(!maquina);
        if (maquina) {
            nombre2.setText(ConfiguracionPartido.PREFIJO_MAQUINA + perfil.getSelectedItem());
        } else if (nombre2.getText().startsWith(ConfiguracionPartido.PREFIJO_MAQUINA)) {
            nombre2.setText("Jugador 2");
        }
    }

    // Arma la configuracion con lo que eligio el usuario y empieza el partido.
    // Si algo esta mal, el dominio lanza la excepcion y aqui se muestra el mensaje.
    private void comenzar() {
        try {
            Modalidad modalidad = Modalidad.JUGADOR_VS_JUGADOR;
            if (contraMaquina.isSelected()) {
                modalidad = Modalidad.JUGADOR_VS_MAQUINA;
            }
            TipoPartido tipo = TipoPartido.POR_GOLES;
            if (porTiempo.isSelected()) {
                tipo = TipoPartido.POR_TIEMPO;
            }
            int valorLimite = (Integer) limite.getValue();
            if (contraMaquina.isSelected()) {
                ((PerfilMaquina) perfil.getSelectedItem()).setDificultad((Dificultad) dificultad.getSelectedItem());
            }
            ConfiguracionPartido configuracion = new ConfiguracionPartido(modalidad,
                    (PerfilMaquina) perfil.getSelectedItem(), tipo, valorLimite,
                    nombre1.getText(), nombre2.getText(),
                    (Personaje) personaje1.getSelectedItem(), (Personaje) personaje2.getSelectedItem(),
                    (Resolucion) resolucion.getSelectedItem());
            error.setText(" ");
            ventana.comenzar(configuracion);
        } catch (HeadSoccerException e) {
            error.setText(e.getMessage());
            System.out.println(e.getMessage());
        } catch (RuntimeException e) {
            ventana.mostrarErrorInesperado(e);
        }
    }

    private void agregarFila(int fila, JLabel etiqueta, Component campo, GridBagConstraints c) {
        c.gridwidth = 1;
        c.gridy = fila;
        c.gridx = 0;
        add(etiqueta, c);
        c.gridx = 1;
        add(campo, c);
    }

    private JPanel enFila(Component primero, Component segundo) {
        JPanel fila = new JPanel();
        fila.setOpaque(false);
        fila.add(primero);
        fila.add(segundo);
        return fila;
    }

    private void agrupar(JRadioButton primero, JRadioButton segundo) {
        ButtonGroup grupo = new ButtonGroup(); // para que solo se pueda elegir uno
        grupo.add(primero);
        grupo.add(segundo);
        darEstilo(primero);
        darEstilo(segundo);
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
