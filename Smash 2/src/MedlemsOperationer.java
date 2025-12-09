import java.util.List;

interface MedlemsOperationer {
    Medlem opretMedlem(String navn, int alder, String adresse, boolean aktiv, boolean konkurrence) 
            throws UgyldigAlderException, DuplikatMedlemException;

    List<Medlem> hentMedlemmer();
}
