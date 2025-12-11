import java.io.*;
import java.time.LocalDate;
import java.util.*;

/*
 * Tennisklubben Smash
 * gemmer i CSV-filer:
 *  - medlemmer.csv
 *  - traening.csv
 *  - turneringer.csv
 *
 * Funktionalitet:
 *  - Formand: opret medlem, vis medlemmer
 *  - Kasserer: vis forventede indbetalinger, registrer betaling, vis restance
 *  - Coach: vis konkurrencespillere, registrer træningsresultat, registrer turnering, top-5 lister
 *
 */

// ---------------------------
// UI
// ---------------------------
class TennisklubbenSmash {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Klub klub = new Klub();
    private static final String DATA_MAPPE = "data"; // mappe hvor CSV gemmes

    public static void main(String[] args){
        // Indlæs ved start
        try {
            klub.indlaes(DATA_MAPPE); System.out.println("Data indlæst fra ./" + DATA_MAPPE);
        }
        catch (Exception e) {
            System.out.println("Ingen eller fejl ved indlæsning - starter med tomt system.");
        }

        while (true){
            System.out.println("Tennisklubben Smash");
                    System.out.println("Vælg rolle: 1) Formand  2) Kasserer  3) Coach  0) Afslut");
            String valg = scanner.nextLine().trim();
            try {
                switch (valg){
                    case "1": formandMenu(); break;
                    case "2": kassererMenu(); break;
                    case "3": coachMenu(); break;
                    case "0": afslutOgGem(); return;
                    default: System.out.println("Ugyldigt valg");
                }
            } catch (Exception ex){ System.out.println("Fejl: " + ex.getMessage()); }
        }
    }

    private static void formandMenu(){
        while (true){
            System.out.println("-- Formand -- 1) Opret medlem  2) Vis alle medlemmer  0) Tilbage");
            String c = scanner.nextLine().trim();
            try {
                if (c.equals("0")) return;
                if (c.equals("1")){
                    System.out.print("Navn: "); String navn = scanner.nextLine();
                    System.out.print("Alder: "); int alder = Integer.parseInt(scanner.nextLine());
                    System.out.print("Adresse: "); String adr = scanner.nextLine();
                    System.out.print("Aktivt medlem? (j/n): "); boolean aktiv = scanner.nextLine().trim().equalsIgnoreCase("j");
                    System.out.print("Konkurrencespiller? (j/n): "); boolean konk = scanner.nextLine().trim().equalsIgnoreCase("j");
                    Medlem m = klub.opretMedlem(navn, alder, adr, aktiv, konk);
                    System.out.println("Oprettet: " + m);
                } else if (c.equals("2")){
                    List<Medlem> list = klub.hentMedlemmer();
                    System.out.println("--- Medlemmer (alfabetisk) ---");
                    for (Medlem m : list) System.out.println(m);
                } else System.out.println("Ugyldigt valg");
            } catch (NumberFormatException nfe){ System.out.println("Ugyldigt talformat"); }
            catch (DuplikatMedlemException ex){ System.out.println("Fejl: " + ex.getMessage()); } catch (
                    UgyldigAlderException e) {

            }
        }
    }

    private static void kassererMenu(){
        while (true){
            System.out.println("Kasserer 1) Vis forventede indbetalinger  2) Registrer betaling  3) Vis restance  0) Tilbage");
            String c = scanner.nextLine().trim();
            try {
                if (c.equals("0")) return;
                if (c.equals("1")){
                    List<Medlem> list = klub.hentMedlemmer();
                    double samlet = 0.0;
                    System.out.println("Navn | Kontingent | Betalt");
                    for (Medlem m : list){
                        System.out.printf("%s | %.2f | %s", m.getNavn(), m.beregnKontingent(), m.erBetalt()?"Ja":"Nej");
                                samlet += m.beregnKontingent();
                    }
                    System.out.printf("Forventet samlet: %.2f ", samlet);
                } else if (c.equals("2")){
                    System.out.print("Søg medlem efter navn: "); String navn = scanner.nextLine();
                    Optional<Medlem> om = klub.findMedlemVedNavn(navn);
                    if (om.isEmpty()){ System.out.println("Medlem ikke fundet"); continue; }
                    Medlem m = om.get();
                    System.out.println(m);
                    System.out.print("Registrer som betalt? (j/n): "); String s = scanner.nextLine();
                    if (s.trim().equalsIgnoreCase("j")) { klub.registrerBetaling(m.getId(), true); System.out.println("Betaling registreret"); }
                } else if (c.equals("3")){
                    List<Medlem> rest = klub.restanceListe();
                    System.out.println("--- Medlemmer i restance ---");
                    for (Medlem m : rest) System.out.println(m);
                } else System.out.println("Ugyldigt valg");
            } catch (BetalingsException be){ System.out.println("Fejl: " + be.getMessage()); }
            catch (NumberFormatException nfe){ System.out.println("Ugyldigt talformat"); }
        }
    }

    private static void coachMenu(){
        while (true){
            System.out.println(" -- Coach -- 1) Vis konkurrencespillere  2) Registrer træningsresultat  3) Registrer turneringsresultat  4) Top-5 lister  0) Tilbage");
            String c = scanner.nextLine().trim();
            try {
                if (c.equals("0")) return;
                if (c.equals("1")){
                    System.out.println("Juniorhold (under 18):");
                    for (KonkurrenceSpiller k : klub.hentKonkurrenceSpillere(true)) System.out.println(k);
                    System.out.println("Seniorhold (18+):");
                    for (KonkurrenceSpiller k : klub.hentKonkurrenceSpillere(false)) System.out.println(k);
                } else if (c.equals("2")){
                    System.out.print("Hvilken spiller (navn): "); String navn = scanner.nextLine();
                    Optional<Medlem> om = klub.findMedlemVedNavn(navn);
                    if (om.isEmpty() || !(om.get() instanceof KonkurrenceSpiller)){ System.out.println("Konkurrencespiller ikke fundet"); continue; }
                    KonkurrenceSpiller ks = (KonkurrenceSpiller)om.get();
                    System.out.print("Disciplin (SINGLE/DOUBLE/MIXED_DOUBLE): "); Enums d = Enums.valueOf(scanner.nextLine().trim().toUpperCase());
                    System.out.print("Score (tal, højere er bedre): "); double score = Double.parseDouble(scanner.nextLine());
                    ks.setTraeningsResultat(d, new TraeningsResultat(score, LocalDate.now()));
                    System.out.println("Træningsresultat registreret: " + ks.getTraeningsResultat(d).get());
                } else if (c.equals("3")){
                    System.out.print("Hvilken spiller (navn): "); String navn = scanner.nextLine();
                    Optional<Medlem> om = klub.findMedlemVedNavn(navn);
                    if (om.isEmpty() || !(om.get() instanceof KonkurrenceSpiller)){ System.out.println("Konkurrencespiller ikke fundet"); continue; }
                    KonkurrenceSpiller ks = (KonkurrenceSpiller)om.get();
                    System.out.print("Turneringens navn: "); String tnavn = scanner.nextLine();
                    System.out.print("Rangering (1=bedst): "); int rang = Integer.parseInt(scanner.nextLine());
                    System.out.print("Kampresultat (fx 6-3 6-4): "); String kRes = scanner.nextLine();
                    ks.addTurneringsResultat(new TurneringsResultat(tnavn, rang, kRes, LocalDate.now()));
                    System.out.println("Turneringsresultat registreret");
                } else if (c.equals("4")){
                    for (boolean junior : new boolean[]{true,false}){
                        System.out.println(junior?"--- Top-5 Junior ---":"--- Top-5 Senior ---");
                        for (Enums d : Enums.values()){
                            System.out.println("Disciplin: " + d);
                            List<KonkurrenceSpiller> top = klub.top5(d, junior);
                            if (top.isEmpty()) System.out.println("  Ingen data");
                            else {
                                int i=1;
                                for (KonkurrenceSpiller k : top) System.out.printf("  %d) %s - %s - Score: %s ", i++, k.getNavn(), k.getCoachNavn(), k.getTraeningsResultat(d).get());
                            }
                        }
                    }
                } else System.out.println("Ugyldigt valg");
            } catch (NumberFormatException nfe){ System.out.println("Ugyldigt talformat"); }
            catch (IllegalArgumentException iae){ System.out.println("Ugyldigt input: " + iae.getMessage()); }
        }
    }

    private static void afslutOgGem(){
        try { klub.gem(DATA_MAPPE); System.out.println("Data gemt i ./" + DATA_MAPPE); } catch (IOException e){ System.out.println("Kunne ikke gemme: " + e.getMessage()); }
        System.out.println("Lukker ned");
    }
}
