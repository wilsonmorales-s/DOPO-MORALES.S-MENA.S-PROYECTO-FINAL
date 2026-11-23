package domain;

// Maquina experta: decide segun como va el partido. Defiende si el balon esta en su mitad
// o si el rival esta mas cerca del balon, y ataca si va perdiendo o se acaba el tiempo.
public class MaquinaExperta extends PerfilMaquina {

    private static final long serialVersionUID = 1L;

    public static final double DISTANCIA_PODER = 180;
    public static final double TIEMPO_URGENTE = 30;   // segundos: al final del partido se arriesga mas

    @Override
    public String getNombre() {
        return "Experta";
    }

    @Override
    public void decidir(Partido partido, Jugador yo, double dt) {
        if (!puedeDecidir(dt) || distraida(yo)) {
            return;
        }
        Jugador rival = rival(partido, yo);
        Balon balon = partido.getBalon();

        // 1. Mira el estado del partido
        int misGoles = partido.getMarcador().getGoles(yo.getLado());
        int golesRival = partido.getMarcador().getGoles(rival.getLado());
        boolean voyPerdiendo = misGoles < golesRival;
        boolean urgente = partido.getConfiguracion().getTipo() == TipoPartido.POR_TIEMPO
                && partido.getTiempoRestante() < TIEMPO_URGENTE;
        boolean rivalMasCerca = distanciaAlBalon(partido, rival) < distanciaAlBalon(partido, yo);

        // 2. Decide si ataca o defiende
        if ((voyPerdiendo || urgente || rival.estaAturdido()) && !balonEnMiMitad(partido, yo)) {
            atacar(partido, yo);
        } else if (balonEnMiMitad(partido, yo) || rivalMasCerca) {
            defender(partido, yo);
        } else {
            atacar(partido, yo);
        }

        // 3. Poder: solo si el balon esta cerca y entre el y el arco rival
        double delante = (balon.getX() - yo.getX()) * sentidoAtaque(yo);
        if (delante > 0 && distanciaAlBalon(partido, yo) < DISTANCIA_PODER) {
            yo.activarPoder();
        }

        // 4. Si el rival esta al alcance del pie, lo patea para quitarle vida
        double dx = rival.getCentroX() - yo.getCentroX();
        double dy = rival.getCentroY() - yo.getCentroY();
        double distanciaRival = Math.sqrt(dx * dx + dy * dy);
        if (distanciaRival < 2 * Jugador.RADIO + Jugador.ALCANCE_PATADA) {
            yo.patear();
        }
    }

    // Con la misma dificultad, la experta reacciona mas rapido y se equivoca menos que las otras.
    @Override
    protected double getTiempoReaccion() {
        return super.getTiempoReaccion() * 0.8;
    }

    @Override
    protected double getImprecision() {
        return super.getImprecision() * 0.6;
    }

    @Override
    protected double getProbabilidadPatada() {
        return Math.min(1, super.getProbabilidadPatada() + 0.1);
    }
}
