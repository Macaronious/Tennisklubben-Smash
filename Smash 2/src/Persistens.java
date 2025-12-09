import java.io.IOException;

// ---------------------------
// Interfaces
// ---------------------------
interface Persistens {
    void gem(String mappe) throws IOException;

    void indlaes(String mappe) throws IOException;
}
