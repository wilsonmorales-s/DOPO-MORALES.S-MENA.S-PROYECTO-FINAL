package domain;

// Maquina ofensiva: su prioridad es anotar. Siempre va por el balon y lo empuja
// hacia el arco rival, y usa el poder en cuanto lo tiene.
public class MaquinaOfensiva extends PerfilMaquina {

    private static final long serialVersionUID = 1L;

    @Override
    public String getNombre() {
        return "Ofensiva";
    }

    @Override
    public void decidir(Partido partido, Jugador yo, double dt) {
        if (puedeDecidir(dt) && !distraida(yo)) {
            atacar(partido, yo);
            yo.activarPoder();
        }
    }
}
