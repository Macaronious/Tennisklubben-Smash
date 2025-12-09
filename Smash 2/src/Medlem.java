class Medlem extends Person implements Comparable<Medlem> {
    protected Enums.MedlemsType medlemsType;
    protected boolean konkurrenceSpiller;
    protected boolean betalt;

    public Medlem(String navn, int alder, String adresse, Enums.MedlemsType type, boolean konkurrence) {
        super(navn, alder, adresse);
        this.medlemsType = type;
        this.konkurrenceSpiller = konkurrence;
        this.betalt = false;
    }

    public Enums.MedlemsType getMedlemsType() {
        return medlemsType;
    }

    public boolean erKonkurrence() {
        return konkurrenceSpiller;
    }

    public boolean erBetalt() {
        return betalt;
    }

    public void setBetalt(boolean b) {
        betalt = b;
    }

    public double beregnKontingent() {
        if (medlemsType == Enums.MedlemsType.PASSIVE) return 250.0;
        if (alder < 18) return 800.0;
        double senior = 1500.0;
        if (alder >= 60) return senior * 0.75;
        return senior;
    }

    @Override
    public int compareTo(Medlem o) {
        return this.navn.compareToIgnoreCase(o.navn);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | Betalt:%s | Kontingent:%.2f", super.toString(), medlemsType, konkurrenceSpiller ? "Konkurrence" : "Alm.", betalt ? "Ja" : "Nej", beregnKontingent());
    }
}
