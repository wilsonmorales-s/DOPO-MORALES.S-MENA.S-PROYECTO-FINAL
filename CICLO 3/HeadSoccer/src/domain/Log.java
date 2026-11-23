package domain;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

// Log de errores para los programadores: guarda en HeadSoccer.log la traza de cada error.
public class Log {

    public static final String ARCHIVO = "HeadSoccer.log";

    private static Logger logger;

    // Registra el error. Si no se puede escribir el log, lo muestra en la consola para no perderlo.
    public static void registre(Exception e) {
        try {
            if (logger == null) {
                logger = Logger.getLogger(Log.class.getName());
                logger.setUseParentHandlers(false);
                FileHandler archivo = new FileHandler(ARCHIVO, true); // true: agrega al final
                archivo.setFormatter(new SimpleFormatter());
                logger.addHandler(archivo);
            }
            logger.log(Level.SEVERE, e.toString(), e);
        } catch (IOException | SecurityException otro) {
            System.out.println("No se pudo escribir el log: " + otro.getMessage());
            e.printStackTrace();
        }
    }
}
