import java.time.LocalDate;

class TraeningsResultat {
    public double score;
    public LocalDate dato;

    public TraeningsResultat(double score, LocalDate dato) {
        this.score = score;
        this.dato = dato;
    }

    public String toString() {
        return String.format("%.2f (%s)", score, dato);
    }
}
