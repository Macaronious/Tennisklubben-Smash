import java.util.*;

class KonkurrenceSpiller extends Medlem {
    private String coachNavn;
    private Map<Enums, TraeningsResultat> traening = new EnumMap<>(Enums.class);
    private List<TurneringsResultat> turneringer = new ArrayList<>();

    public KonkurrenceSpiller(String navn, int alder, String adresse, Enums.MedlemsType type, String coachNavn) {
        super(navn, alder, adresse, type, true);
        this.coachNavn = coachNavn == null ? "Uden coach" : coachNavn;
    }

    public String getCoachNavn() {
        return coachNavn;
    }

    public void setCoachNavn(String c) {
        this.coachNavn = c;
    }

    public void setTraeningsResultat(Enums d, TraeningsResultat r) {
        TraeningsResultat eks = traening.get(d);
        if (eks == null || r.score > eks.score) traening.put(d, r);
    }

    public Optional<TraeningsResultat> getTraeningsResultat(Enums d) {
        return Optional.ofNullable(traening.get(d));
    }

    public void addTurneringsResultat(TurneringsResultat tr) {
        turneringer.add(tr);
    }

    public List<TurneringsResultat> getTurneringer() {
        return turneringer;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(" | Coach:" + coachNavn);
        for (Enums d : Enums.values())
            sb.append(" | " + d + ": " + (traening.containsKey(d) ? traening.get(d) : "-"));
        return sb.toString();
    }
}
