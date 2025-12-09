import java.util.UUID;

// ---------------------------
// Person / Medlem klasser
// ---------------------------
abstract class Person {
    protected UUID id;
    protected String navn;
    protected int alder;
    protected String adresse;

    public Person(String navn, int alder, String adresse) {
        this.id = UUID.randomUUID();
        this.navn = navn;
        this.alder = alder;
        this.adresse = adresse;
    }

    public UUID getId() {
        return id;
    }

    public String getNavn() {
        return navn;
    }

    public int getAlder() {
        return alder;
    }

    public String getAdresse() {
        return adresse;
    }

    public String toString() {
        return String.format("%s (ID:%s) - %d år - %s", navn, id.toString().substring(0, 8), alder, adresse);
    }
}
