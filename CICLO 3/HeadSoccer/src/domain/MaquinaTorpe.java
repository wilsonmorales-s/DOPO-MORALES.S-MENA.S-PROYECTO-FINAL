package domain;

import java.util.Random;

// Maquina torpe: escoge sus movimientos al azar.
public class MaquinaTorpe extends PerfilMaquina {

    private static final long serialVersionUID = 1L;

    public static final double TIEMPO_DECISION = 0.4;   // segundos entre una decision y otra

    private Random azar;
    private double espera;

    public MaquinaTorpe() {
        this(new Random());
    }

    // Para las pruebas: con la misma semilla siempre hace lo mismo.
    public MaquinaTorpe(Random azar) {
        this.azar = azar;
        this.espera = 0;
    }

    @Override
    public String getNombre() {
        return "Torpe";
    }

    @Override
    public void decidir(Partido partido, Jugador yo, double dt) {
        espera = espera - dt;
        if (espera <= 0) {
            espera = TIEMPO_DECISION * getTiempoReaccion() / Dificultad.MEDIA.getTiempoReaccion();
            yo.mover(azar.nextInt(3) - 1);   // -1, 0 o 1
            if (azar.nextInt(5) == 0) {
                yo.saltar();
            }
            if (azar.nextInt(3) == 0) {
                yo.patear();
            }
            if (azar.nextBoolean()) {
                yo.activarPoder();
            }
        }
    }
}
