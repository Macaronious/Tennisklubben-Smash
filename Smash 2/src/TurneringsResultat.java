import java.time.LocalDate;

class TurneringsResultat {
    public String turnering;
    public int rang;
    public String kampResultat;
    public LocalDate dato;

    public TurneringsResultat(String turnering, int rang, String kampResultat, LocalDate dato) {
        this.turnering = turnering;
        this.rang = rang;
        this.kampResultat = kampResultat;
        this.dato = dato;
    }

    public String toString() {
        return String.format("%s: Rang %d, %s (%s)", turnering, rang, kampResultat, dato);
    }
}
