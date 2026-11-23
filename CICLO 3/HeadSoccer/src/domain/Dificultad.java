package domain;

// Dificultad de la maquina. Sirve para cualquier perfil:
// tiempoReaccion   -> cada cuanto piensa (segundos)
// imprecision      -> cuanto se equivoca al ubicarse
// probabilidadPatada -> que tanto patea cuando esta cerca del arco rival
// distraccion      -> probabilidad de quedarse quieta en una decision
public enum Dificultad {
    BAJA("Baja", 0.45, 75, 0.3, 0.4),
    MEDIA("Media", 0.25, 40, 0.5, 0.15),
    ALTA("Alta", 0.12, 12, 0.85, 0);

    private final String nombre;
    private final double tiempoReaccion;
    private final double imprecision;
    private final double probabilidadPatada;
    private final double distraccion;

    Dificultad(String nombre, double tiempoReaccion, double imprecision, double probabilidadPatada,
            double distraccion) {
        this.nombre = nombre;
        this.tiempoReaccion = tiempoReaccion;
        this.imprecision = imprecision;
        this.probabilidadPatada = probabilidadPatada;
        this.distraccion = distraccion;
    }

    public double getDistraccion() {
        return distraccion;
    }

    public double getTiempoReaccion() {
        return tiempoReaccion;
    }

    public double getImprecision() {
        return imprecision;
    }

    public double getProbabilidadPatada() {
        return probabilidadPatada;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
