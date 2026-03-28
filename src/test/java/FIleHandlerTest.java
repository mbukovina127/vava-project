import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shippin.util.io.TextFileHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileHandlerTest {

    private TextFileHandler handler;
    // WARNING folder structure is not "resources/CsvTestFiles", only name just is "resources.CsvTestFiles"
    private static final String TEST_DIR = "src/test/resources.CsvTestFiles/";
    private static final String PRICE_FILE = TEST_DIR + "priceCsv.csv";
    private static final String REGION_FILE = TEST_DIR + "regionCsv.csv";
    private static final String SMALL_FILE = TEST_DIR + "smallPriceCsv.csv";

    @BeforeEach
    void setUp() {
        handler = new TextFileHandler();
    }

    @Test
    void testReadExistingFiles() throws IOException {
        // priceCsv.csv
        File priceFile = new File(PRICE_FILE);
        assertTrue(priceFile.exists(), "priceCsv.csv should exist");
        String priceContent = handler.readFrom(priceFile);
        assertFalse(priceContent.isEmpty(), "priceCsv content should not be empty");
        assertTrue(priceContent.contains("Hmotnosť do (v kg)"), "should contain price header");

        // regionCsv.csv
        File regionFile = new File(REGION_FILE);
        assertTrue(regionFile.exists());
        String regionContent = handler.readFrom(regionFile);
        assertTrue(regionContent.contains("Rozdelenie PSČ"), "should contain region header");

        // smallPriceCsv.csv
        File smallFile = new File(SMALL_FILE);
        assertTrue(smallFile.exists());
        String smallContent = handler.readFrom(smallFile);
        assertTrue(smallContent.contains("Hmotnosť;Cena"), "should contain small price header");
    }

    @Test
    void testWriteAndReadRoundTrip() throws IOException {
        Path tempFile = Files.createTempFile("test-write-", ".txt");
        File file = tempFile.toFile();

        String testContent = "Ahoj toto je testovací obsah\nDruhá veta.\n";

        boolean success = handler.writeTo(file, testContent);
        assertTrue(success, "write should succeed");

        String readBack = handler.readFrom(file);
        assertEquals(testContent, readBack, "read content should match written content");

        // cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testReadNonExistingFile() {
        File fake = new File("neexistuje-vobec.csv");
        String content = handler.readFrom(fake);
        assertEquals("", content, "non-existing file should return empty string");
    }
}