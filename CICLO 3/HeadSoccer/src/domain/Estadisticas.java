package domain;

import java.io.Serializable;

// Speed, kick, jump y survival. Empiezan en 1 y llegan hasta 10; cada punto suma un 10 %.
public class Estadisticas implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int NIVEL_MINIMO = 1;
    public static final int NIVEL_MAXIMO = 10;
    public static final double INCREMENTO = 0.10;   // 10 % por punto
    public static final double VIDA_BASE = 100;

    private int speed;
    private int kick;
    private int jump;
    private int survival;

    public Estadisticas() {
        speed = NIVEL_MINIMO;
        kick = NIVEL_MINIMO;
        jump = NIVEL_MINIMO;
        survival = NIVEL_MINIMO;
    }

    // Copia independiente (cada partido usa su propia copia).
    public Estadisticas copia() {
        Estadisticas otra = new Estadisticas();
        otra.speed = speed;
        otra.kick = kick;
        otra.jump = jump;
        otra.survival = survival;
        return otra;
    }

    public int getNivel(Estadistica estadistica) {
        int nivel = NIVEL_MINIMO;
        if (estadistica == Estadistica.SPEED) {
            nivel = speed;
        } else if (estadistica == Estadistica.KICK) {
            nivel = kick;
        } else if (estadistica == Estadistica.JUMP) {
            nivel = jump;
        } else if (estadistica == Estadistica.SURVIVAL) {
            nivel = survival;
        }
        return nivel;
    }

    // Sube un punto la estadistica. No puede pasar de 10.
    public void subir(Estadistica estadistica) throws ProgresoException {
        if (getNivel(estadistica) < NIVEL_MAXIMO) {
            if (estadistica == Estadistica.SPEED) {
                speed = speed + 1;
            } else if (estadistica == Estadistica.KICK) {
                kick = kick + 1;
            } else if (estadistica == Estadistica.JUMP) {
                jump = jump + 1;
            } else if (estadistica == Estadistica.SURVIVAL) {
                survival = survival + 1;
            }
        } else {
            throw new ProgresoException(ProgresoException.NIVEL_MAXIMO);
        }
    }

    // 1 + 0,10 * (nivel - 1): cuanto se multiplica el valor base de la estadistica.
    public double getFactor(Estadistica estadistica) {
        return 1 + INCREMENTO * (getNivel(estadistica) - 1);
    }

    // multiplicadorKick = 1 + 0,10 * (Kick - 1)
    public double getMultiplicadorKick() {
        return getFactor(Estadistica.KICK);
    }

    // vidaMax = 100 * (1 + 0,10 * (Survival - 1))
    public double getVidaMax() {
        return VIDA_BASE * getFactor(Estadistica.SURVIVAL);
    }
}
