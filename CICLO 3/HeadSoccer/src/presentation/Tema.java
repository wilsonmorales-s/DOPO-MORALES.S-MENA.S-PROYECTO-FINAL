package presentation;

import domain.Personaje;
import java.awt.Color;
import java.awt.Font;

// Colores y letras que usan todas las pantallas, para que se vean iguales.
public class Tema {

    public static final Color FONDO = new Color(18, 32, 58);
    public static final Color CIELO = new Color(120, 180, 235);
    public static final Color PASTO = new Color(46, 125, 50);
    public static final Color JUGADOR_IZQUIERDO = new Color(33, 150, 243); // azul
    public static final Color JUGADOR_DERECHO = new Color(229, 57, 53);    // rojo
    public static final Color TEXTO = Color.WHITE;
    public static final Color ACENTO = new Color(255, 193, 7);             // amarillo
    public static final Color ERROR = new Color(255, 138, 128);
    public static final Color VIDA = new Color(76, 175, 80);
    public static final Color VIDA_BAJA = new Color(244, 67, 54);
    public static final Color BARRA_VACIA = new Color(55, 71, 99);

    public static final Font TITULO = new Font("SansSerif", Font.BOLD, 48);
    public static final Font SUBTITULO = new Font("SansSerif", Font.BOLD, 22);
    public static final Font NORMAL = new Font("SansSerif", Font.PLAIN, 16);
    public static final Font PEQUENA = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font BOTON = new Font("SansSerif", Font.BOLD, 20);

    // Color con el que se dibuja un personaje (lo trae el dominio en formato RGB).
    public static Color colorDe(Personaje personaje) {
        return new Color(personaje.getColor());
    }
}
