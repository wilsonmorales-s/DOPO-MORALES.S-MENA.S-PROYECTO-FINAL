package domain;

// Maquina defensiva: su prioridad es defender el arco. Se queda cerca de su porteria,
// sale solo cuando el balon entra a su mitad y guarda el poder para despejar.
public class MaquinaDefensiva extends PerfilMaquina {

    private static final long serialVersionUID = 1L;

    public static final double DISTANCIA_PODER = 200;

    @Override
    public String getNombre() {
        return "Defensiva";
    }

    @Override
    public void decidir(Partido partido, Jugador yo, double dt) {
        if (puedeDecidir(dt) && !distraida(yo)) {
            defender(partido, yo);
            if (balonEnMiMitad(partido, yo) && distanciaAlBalon(partido, yo) < DISTANCIA_PODER) {
                yo.activarPoder();
            }
        }
    }

    // Se ubica mejor que las otras porque casi siempre esta quieta frente a su arco.
    @Override
    protected double getImprecision() {
        return super.getImprecision() * 0.6;
    }
}
