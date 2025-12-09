import java.io.*;
import java.time.LocalDate;
import java.util.*;

// ---------------------------
// Klub manager med CSV-persistens
// ---------------------------
class Klub implements MedlemsOperationer, Persistens {
    private Map<UUID, Medlem> medlemmer = new HashMap<>();

    @Override
    public Medlem opretMedlem(String navn, int alder, String adresse, boolean aktiv, boolean konkurrence) throws UgyldigAlderException, DuplikatMedlemException{
        if (alder < 0 || alder > 120) throw new UgyldigAlderException("Ugyldig alder: " + alder);
        for (Medlem m : medlemmer.values()){
            if (m.getNavn().equalsIgnoreCase(navn) && m.getAdresse().equalsIgnoreCase(adresse)){
                throw new DuplikatMedlemException("Medlem findes allerede: " + navn);
            }
        }
        Enums.MedlemsType t = aktiv ? Enums.MedlemsType.ACTIVE : Enums.MedlemsType.PASSIVE;
        Medlem m;
        if (konkurrence) m = new KonkurrenceSpiller(navn, alder, adresse, t, "Uden coach");
        else m = new Medlem(navn, alder, adresse, t, false);
        medlemmer.put(m.getId(), m);
        return m;
    }

    @Override
    public List<Medlem> hentMedlemmer() {
        List<Medlem> list = new ArrayList<>(medlemmer.values());
        Collections.sort(list);
        return list;
    }

    public Optional<Medlem> findMedlemVedNavn(String navn) {
        for (Medlem m : medlemmer.values()) if (m.getNavn().equalsIgnoreCase(navn)) return Optional.of(m);
        return Optional.empty();
    }

    public void registrerBetaling(UUID id, boolean betalt) throws BetalingsException {
        Medlem m = medlemmer.get(id);
        if (m == null) throw new BetalingsException("Medlem ikke fundet");
        m.setBetalt(betalt);
    }

    public List<Medlem> restanceListe() {
        List<Medlem> list = new ArrayList<>();
        for (Medlem m : medlemmer.values()) if (!m.erBetalt()) list.add(m);
        Collections.sort(list);
        return list;
    }

    public List<KonkurrenceSpiller> hentKonkurrenceSpillere(boolean junior) {
        List<KonkurrenceSpiller> list = new ArrayList<>();
        for (Medlem m : medlemmer.values()) {
            if (m instanceof KonkurrenceSpiller) {
                KonkurrenceSpiller ks = (KonkurrenceSpiller) m;
                if (junior && ks.getAlder() < 18) list.add(ks);
                if (!junior && ks.getAlder() >= 18) list.add(ks);
            }
        }
        Collections.sort(list, Comparator.comparing(Medlem::getNavn));
        return list;
    }

    public List<KonkurrenceSpiller> top5(Enums d, boolean junior) {
        List<KonkurrenceSpiller> spillere = hentKonkurrenceSpillere(junior);
        List<KonkurrenceSpiller> medScore = new ArrayList<>();
        for (KonkurrenceSpiller k : spillere) if (k.getTraeningsResultat(d).isPresent()) medScore.add(k);
        medScore.sort((a, b) -> Double.compare(b.getTraeningsResultat(d).get().score, a.getTraeningsResultat(d).get().score));
        return medScore.size() > 5 ? medScore.subList(0, 5) : medScore;
    }

    // ---------------------------
    // CSV Persistens
    // ---------------------------
    @Override
    public void gem(String mappe) throws IOException {
        File dir = new File(mappe);
        if (!dir.exists()) dir.mkdirs();

        // medlemmer.csv
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(dir, "medlemmer.csv")))) {
            pw.println("id;navn;alder;adresse;type;konkurrence;betalt;coach");
            for (Medlem m : medlemmer.values()) {
                String coach = "-";
                if (m instanceof KonkurrenceSpiller) coach = ((KonkurrenceSpiller) m).getCoachNavn();
                pw.printf("%s;%s;%d;%s;%s;%b;%b;%s%n",
                        m.getId().toString(), m.getNavn(), m.getAlder(), m.getAdresse(), m.getMedlemsType().name(), m.erKonkurrence(), m.erBetalt(), coach);
            }
        }

        // traening.csv
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(dir, "traening.csv")))) {
            pw.println("id;disciplin;score;dato");
            for (Medlem m : medlemmer.values()) {
                if (m instanceof KonkurrenceSpiller) {
                    KonkurrenceSpiller ks = (KonkurrenceSpiller) m;
                    for (Enums d : Enums.values()) {
                        ks.getTraeningsResultat(d).ifPresent(r -> pw.printf("%s;%s;%.2f;%s%n", ks.getId().toString(), d.name(), r.score, r.dato));
                    }
                }
            }
        }

        // turneringer.csv
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(dir, "turneringer.csv")))) {
            pw.println("id;turnering;rang;resultat;dato");
            for (Medlem m : medlemmer.values()) {
                if (m instanceof KonkurrenceSpiller) {
                    KonkurrenceSpiller ks = (KonkurrenceSpiller) m;
                    for (TurneringsResultat tr : ks.getTurneringer()) {
                        pw.printf("%s;%s;%d;%s;%s%n", ks.getId().toString(), tr.turnering, tr.rang, tr.kampResultat, tr.dato);
                    }
                }
            }
        }
    }

    @Override
    public void indlaes(String mappe) throws IOException {
        medlemmer.clear();
        File dir = new File(mappe);
        if (!dir.exists()) return;

        File fMed = new File(dir, "medlemmer.csv");
        if (fMed.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fMed))) {
                String header = br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(";", -1);
                    if (p.length < 8) continue;
                    UUID id = UUID.fromString(p[0]);
                    String navn = p[1];
                    int alder = Integer.parseInt(p[2]);
                    String adresse = p[3];
                    Enums.MedlemsType type = Enums.MedlemsType.valueOf(p[4]);
                    boolean konk = Boolean.parseBoolean(p[5]);
                    boolean bet = Boolean.parseBoolean(p[6]);
                    String coach = p[7].isEmpty() ? "Uden coach" : p[7];

                    Medlem m;
                    if (konk) m = new KonkurrenceSpiller(navn, alder, adresse, type, coach);
                    else m = new Medlem(navn, alder, adresse, type, false);
                    m.setBetalt(bet);
                    // sæt id via reflection
                    try {
                        java.lang.reflect.Field f = Person.class.getDeclaredField("id");
                        f.setAccessible(true);
                        f.set(m, id);
                    } catch (Exception ignored) {
                    }
                    medlemmer.put(m.getId(), m);
                }
            }
        }

        File fT = new File(dir, "traening.csv");
        if (fT.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fT))) {
                br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(";", -1);
                    if (p.length < 4) continue;
                    UUID id = UUID.fromString(p[0]);
                    Enums d = Enums.valueOf(p[1]);
                    double score = Double.parseDouble(p[2]);
                    LocalDate dato = LocalDate.parse(p[3]);
                    Medlem m = medlemmer.get(id);
                    if (m instanceof KonkurrenceSpiller) {
                        ((KonkurrenceSpiller) m).setTraeningsResultat(d, new TraeningsResultat(score, dato));
                    }
                }
            }
        }

        File fU = new File(dir, "turneringer.csv");
        if (fU.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fU))) {
                br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(";", -1);
                    if (p.length < 5) continue;
                    UUID id = UUID.fromString(p[0]);
                    String tnavn = p[1];
                    int rang = Integer.parseInt(p[2]);
                    String res = p[3];
                    LocalDate dato = LocalDate.parse(p[4]);
                    Medlem m = medlemmer.get(id);
                    if (m instanceof KonkurrenceSpiller) {
                        ((KonkurrenceSpiller) m).addTurneringsResultat(new TurneringsResultat(tnavn, rang, res, dato));
                    }
                }
            }
        }
    }
}
