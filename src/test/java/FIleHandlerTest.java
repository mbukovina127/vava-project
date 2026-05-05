import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.shippin.util.io.TextFileHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileHandlerTest {

    private TextFileHandler handler;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        handler = new TextFileHandler();
    }

    @Test
    void readFromShouldReadExistingPriceCsvFile() throws IOException {
        File file = tempDir.resolve("priceCsv.csv").toFile();

        String content = """
                Hmotnosť do (v kg);Objem do (v m³);Zóny;;;;;
                ;;BA1;BA2;BA3;ZA;ZV;KE
                50;0,2;15,66;16,80;17,35;17,57;17,57;18,52
                """;

        Files.writeString(file.toPath(), content);

        String readContent = handler.readFrom(file);

        assertFalse(readContent.isEmpty());
        assertTrue(readContent.contains("Hmotnosť do (v kg)"));
        assertTrue(readContent.contains("BA1"));
        assertTrue(readContent.contains("18,52"));
    }

    @Test
    void readFromShouldReadExistingRegionCsvFile() throws IOException {
        File file = tempDir.resolve("regionCsv.csv").toFile();

        String content = """
                Rozdelenie PSČ:;;;;;;
                BA1;;81000-85999;;;;
                BA2;;90001-91099;91700-92099;;;
                """;

        Files.writeString(file.toPath(), content);

        String readContent = handler.readFrom(file);

        assertFalse(readContent.isEmpty());
        assertTrue(readContent.contains("Rozdelenie PSČ"));
        assertTrue(readContent.contains("BA1"));
        assertTrue(readContent.contains("81000-85999"));
    }

    @Test
    void readFromShouldReadExistingSmallPriceCsvFile() throws IOException {
        File file = tempDir.resolve("smallPriceCsv.csv").toFile();

        String content = """
                Hmotnosť;Cena
                do 1 kg;3,29
                do 3 kg;3,57
                """;

        Files.writeString(file.toPath(), content);

        String readContent = handler.readFrom(file);

        assertFalse(readContent.isEmpty());
        assertTrue(readContent.contains("Hmotnosť;Cena"));
        assertTrue(readContent.contains("do 1 kg"));
        assertTrue(readContent.contains("3,29"));
    }

    @Test
    void readFromShouldReturnEmptyStringForNullFile() {
        String content = handler.readFrom(null);

        assertEquals("", content);
    }

    @Test
    void readFromShouldReturnEmptyStringForDirectory() {
        File directory = tempDir.toFile();

        String content = handler.readFrom(directory);

        assertEquals("", content);
    }

    @Test
    void readFromShouldReturnEmptyStringForNonExistingFile() {
        File file = tempDir.resolve("non-existing-file.csv").toFile();

        String content = handler.readFrom(file);

        assertEquals("", content);
    }

    @Test
    void readFromShouldReturnEmptyStringForEmptyFile() throws IOException {
        File file = tempDir.resolve("empty.txt").toFile();

        Files.writeString(file.toPath(), "");

        String content = handler.readFrom(file);

        assertEquals("", content);
    }

    @Test
    void writeToShouldReturnFalseForNullFile() {
        boolean success = handler.writeTo(null, "content");

        assertFalse(success);
    }

    @Test
void writeToShouldReturnFalseWhenTargetIsDirectory() {
    File directory = tempDir.toFile();

    boolean success = handler.writeTo(directory, "content");

    assertFalse(success);
}

    @Test
    void writeToShouldReturnFalseForNullContent() {
        File file = tempDir.resolve("null-content.txt").toFile();

        boolean success = handler.writeTo(file, null);

        assertFalse(success);
    }

    @Test
    void writeToShouldCreateParentDirectories() throws IOException {
        File file = tempDir.resolve("nested/folder/output.txt").toFile();

        boolean success = handler.writeTo(file, "hello");

        assertTrue(success);
        assertTrue(file.exists());
        assertEquals("hello", Files.readString(file.toPath()));
    }

    @Test
    void writeToShouldWriteContentToFile() throws IOException {
        File file = tempDir.resolve("output.txt").toFile();

        String content = "Ahoj toto je testovací obsah\nDruhá veta.\n";

        boolean success = handler.writeTo(file, content);

        assertTrue(success);
        assertTrue(file.exists());

        String readBack = Files.readString(file.toPath());

        assertEquals(content, readBack);
    }

    @Test
    void writeToAndReadFromShouldWorkTogether() {
        File file = tempDir.resolve("roundtrip.txt").toFile();

        String content = "Testovací obsah\nDruhý riadok\nTretí riadok\n";

        boolean success = handler.writeTo(file, content);
        String readBack = handler.readFrom(file);

        assertTrue(success);
        assertEquals(content, readBack);
    }
}