package ServiceTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.shippin.domain.PriceList;
import org.shippin.domain.PriceListEntry;
import org.shippin.domain.RegionTable;
import org.shippin.domain.RegionTableEntry;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.PriceListRow;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.domain.formatted.RegionTableRow;
import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.SmallPriceListRow;
import org.shippin.services.WarehouseParsingService;
import org.shippin.util.Range;


public class WarehouseParsingServiceTest {

    private final WarehouseParsingService service = new WarehouseParsingService();
    @TempDir
    Path tempDir;

    private String priceListXml() {
        return """
                <?xml version="1.0"?>
                <Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
                xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
                <Worksheet ss:Name="priceList">
                <Table>
                <Row>
                    <Cell><Data ss:Type="String">Hmotnosť do (v kg)</Data></Cell>
                    <Cell><Data ss:Type="String">Objem do (v m³)</Data></Cell>
                    <Cell><Data ss:Type="String">Zóny</Data></Cell>
                </Row>
                <Row>
                    <Cell ss:Index="3"><Data ss:Type="String">BA</Data></Cell>
                </Row>
                <Row>
                    <Cell><Data ss:Type="Number">10</Data></Cell>
                    <Cell><Data ss:Type="Number">0.5</Data></Cell>
                    <Cell><Data ss:Type="Number">5.2</Data></Cell>
                </Row>
                </Table>
                </Worksheet>
                </Workbook>
                """;
    }

    private String regionTableXml() {
        return """
                <?xml version="1.0"?>
                <Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
                xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
                <Worksheet ss:Name="regionTable">
                <Table>
                <Row>
                    <Cell><Data ss:Type="String">Rozdelenie PSČ:</Data></Cell>
                </Row>
                <Row>
                    <Cell><Data ss:Type="String">BA</Data></Cell>
                    <Cell ss:Index="3"><Data ss:Type="String">81000-81999</Data></Cell>
                </Row>
                </Table>
                </Worksheet>
                </Workbook>
                """;
    }

    private String smallPriceListXml() {
        return """
                <?xml version="1.0"?>
                <Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
                xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
                <Worksheet ss:Name="smallPriceList">
                <Table>
                <Row>
                    <Cell><Data ss:Type="String">Hmotnosť</Data></Cell>
                    <Cell><Data ss:Type="String">Cena</Data></Cell>
                </Row>
                <Row>
                    <Cell><Data ss:Type="String">do 5 kg</Data></Cell>
                    <Cell><Data ss:Type="Number">2.5</Data></Cell>
                </Row>
                </Table>
                </Worksheet>
                </Workbook>
                """;
    }

    private File writeTempFile(String fileName, String content) throws IOException {
        Path path = tempDir.resolve(fileName);
        Files.writeString(path, content);
        return path.toFile();
    }

    private String readFile(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            fail("Could not read temporary file: " + file.getAbsolutePath());
            return "";
        }
    }

    private PriceListFormatted priceListTable() {
        PriceListRow row = new PriceListRow(10f, 0.5f);

        LinkedHashMap<String, Float> regions = new LinkedHashMap<>();
        regions.put("BA", 5.2f);
        regions.put("NR", 6.3f);

        row.setRegions(regions);

        PriceListFormatted table = new PriceListFormatted();
        table.setRows(List.of(row));

        return table;
    }

    private RegionTableFormatted regionTable() {
        RegionTableRow row = new RegionTableRow("BA");
        row.setRanges(List.of(new Range(81000, 81999)));

        RegionTableFormatted table = new RegionTableFormatted();
        table.setRows(List.of(row));

        return table;
    }

    private SmallPriceListFormatted smallPriceListTable() {
        SmallPriceListFormatted table = new SmallPriceListFormatted();
        table.setRows(List.of(new SmallPriceListRow(5f, 2.5f)));

        return table;
    }

    @Test
    void getInstanceReturnsSameInstance() {
        WarehouseParsingService first = WarehouseParsingService.getInstance();
        WarehouseParsingService second = WarehouseParsingService.getInstance();

        assertNotNull(first);
        assertSame(first, second);
    }

    @Test
    void checkTableCompatibilityReturnsTrueForSameRegions() {
        PriceList priceList = new PriceList();
        priceList.setEntries(List.of(
                new PriceListEntry(1, 10f, 0.1f, 5f, "BA"),
                new PriceListEntry(2, 20f, 0.2f, 6f, "NR")
        ));

        ArrayList<Range> ranges = new ArrayList<>();
        ranges.add(new Range(80000, 89999));

        RegionTable regionTable = new RegionTable();
        regionTable.setEntries(List.of(
                new RegionTableEntry(1, ranges, "NR"),
                new RegionTableEntry(2, ranges, "BA")
        ));

        assertTrue(service.checkTableCompatibility(priceList, regionTable));
    }

    @Test
    void checkTableCompatibilityReturnsFalseForDifferentRegions() {
        PriceList priceList = new PriceList();
        priceList.setEntries(List.of(
                new PriceListEntry(1, 10f, 0.1f, 5f, "BA")
        ));

        ArrayList<Range> ranges = new ArrayList<>();
        ranges.add(new Range(90000, 99999));

        RegionTable regionTable = new RegionTable();
        regionTable.setEntries(List.of(
                new RegionTableEntry(1, ranges, "NR")
        ));

        assertFalse(service.checkTableCompatibility(priceList, regionTable));
    }

    @Test
    void parsePriceListRejectsNullFile() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.parsePriceList(null)
        );

        assertEquals("File cannot be null.", exception.getMessage());
    }

    @Test
    void parsePriceListCsvReadsFileContent() throws IOException {
        File file = writeTempFile("price.csv", """
                Hmotnosť do (v kg);Objem do (v m³);Zóny;
                ;;BA;NR
                10;0,50;5,20;6,30
                """);

        PriceListFormatted result = service.parsePriceList(file);

        assertEquals(1, result.getRows().size());
        PriceListRow row = result.getRows().getFirst();

        assertEquals(10f, row.getWeight(), 0.001f);
        assertEquals(0.5f, row.getVolume(), 0.001f);
        assertEquals(5.2f, row.getRegions().get("BA"), 0.001f);
        assertEquals(6.3f, row.getRegions().get("NR"), 0.001f);
    }

    @Test
    void parsePriceListXmlReadsFileContent() throws IOException {
        File file = writeTempFile("price.xml", priceListXml());

        PriceListFormatted result = service.parsePriceList(file);

        assertEquals(1, result.getRows().size());
        PriceListRow row = result.getRows().getFirst();

        assertEquals(10f, row.getWeight(), 0.001f);
        assertEquals(0.5f, row.getVolume(), 0.001f);
        assertEquals(5.2f, row.getRegions().get("BA"), 0.001f);
    }

    @Test
    void parsePriceListRejectsUnsupportedExtension() {
        File file = tempDir.resolve("price.txt").toFile();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.parsePriceList(file)
        );

        assertEquals("Unsupported price list file type: txt", exception.getMessage());
    }

    @Test
    void writePriceListCsvWritesFileContent() {
        File file = tempDir.resolve("price.csv").toFile();

        assertTrue(service.writePriceList(file, priceListTable()));

        String content = readFile(file);
        assertTrue(content.contains("Hmotnosť do (v kg)"));
        assertTrue(content.contains("BA"));
        assertTrue(content.contains("5,20"));
    }

    @Test
    void writePriceListXmlWritesFileContent() {
        File file = tempDir.resolve("price.xml").toFile();

        assertTrue(service.writePriceList(file, priceListTable()));

        String content = readFile(file);
        assertTrue(content.contains("Workbook"));
        assertTrue(content.contains("BA"));
        assertTrue(content.contains("5.2"));
    }

    @Test
    void writePriceListRejectsUnsupportedExtension() {
        File file = tempDir.resolve("price.txt").toFile();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.writePriceList(file, priceListTable())
        );

        assertEquals("Unsupported price list file type: txt", exception.getMessage());
    }

    @Test
    void parseRegionTableCsvReadsFileContent() throws IOException {
        File file = writeTempFile("regions.csv", """
                Rozdelenie PSČ:;;;;;;
                BA;;81000-81999;82100
                """);

        RegionTableFormatted result = service.parseRegionTable(file);

        assertEquals(1, result.getRows().size());
        RegionTableRow row = result.getRows().getFirst();

        assertEquals("BA", row.getRegionCode());
        assertEquals(2, row.getRanges().size());
        assertTrue(row.getRanges().getFirst().contains(81500));
        assertTrue(row.getRanges().get(1).contains(82100));
    }

    @Test
    void parseRegionTableXmlReadsFileContent() throws IOException {
        File file = writeTempFile("regions.xml", regionTableXml());

        RegionTableFormatted result = service.parseRegionTable(file);

        assertEquals(1, result.getRows().size());
        RegionTableRow row = result.getRows().getFirst();

        assertEquals("BA", row.getRegionCode());
        assertEquals(1, row.getRanges().size());
        assertTrue(row.getRanges().getFirst().contains(81500));
    }

    @Test
    void parseRegionTableRejectsUnsupportedExtension() {
        File file = tempDir.resolve("regions.txt").toFile();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.parseRegionTable(file)
        );

        assertEquals("Unsupported region table file type: txt", exception.getMessage());
    }

    @Test
    void writeRegionTableCsvWritesFileContent() {
        File file = tempDir.resolve("regions.csv").toFile();

        assertTrue(service.writeRegionTable(file, regionTable()));

        String content = readFile(file);
        assertTrue(content.contains("Rozdelenie PSČ:"));
        assertTrue(content.contains("BA"));
        assertTrue(content.contains("81000-81999"));
    }

    @Test
    void writeRegionTableXmlWritesFileContent() {
        File file = tempDir.resolve("regions.xml").toFile();

        assertTrue(service.writeRegionTable(file, regionTable()));

        String content = readFile(file);
        assertTrue(content.contains("Workbook"));
        assertTrue(content.contains("BA"));
        assertTrue(content.contains("81000-81999"));
    }

    @Test
    void writeRegionTableRejectsUnsupportedExtension() {
        File file = tempDir.resolve("regions.txt").toFile();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.writeRegionTable(file, regionTable())
        );

        assertEquals("Unsupported region table file type: txt", exception.getMessage());
    }

    @Test
    void parseSmallPriceListCsvReadsFileContent() throws IOException {
        File file = writeTempFile("small.csv", """
                Hmotnosť;Cena
                do 5 kg;2,50
                """);

        SmallPriceListFormatted result = service.parseSmallPriceList(file);

        assertEquals(1, result.getRows().size());
        assertEquals(5f, result.getRows().getFirst().getWeight(), 0.001f);
        assertEquals(2.5f, result.getRows().getFirst().getCost(), 0.001f);
    }

    @Test
    void parseSmallPriceListXmlReadsFileContent() throws IOException {
        File file = writeTempFile("small.xml", smallPriceListXml());

        SmallPriceListFormatted result = service.parseSmallPriceList(file);

        assertEquals(1, result.getRows().size());
        assertEquals(5f, result.getRows().getFirst().getWeight(), 0.001f);
        assertEquals(2.5f, result.getRows().getFirst().getCost(), 0.001f);
    }

    @Test
    void parseSmallPriceListRejectsUnsupportedExtension() {
        File file = tempDir.resolve("small.txt").toFile();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.parseSmallPriceList(file)
        );

        assertEquals("Unsupported small price list file type: txt", exception.getMessage());
    }

    @Test
    void writeSmallPriceListCsvWritesFileContent() {
        File file = tempDir.resolve("small.csv").toFile();

        assertTrue(service.writeSmallPriceList(file, smallPriceListTable()));

        String content = readFile(file);
        assertTrue(content.contains("Hmotnosť"));
        assertTrue(content.contains("Cena"));
        assertTrue(content.contains("do 5,00 kg"));
        assertTrue(content.contains("2,50"));
    }

    @Test
    void writeSmallPriceListXmlWritesFileContent() {
        File file = tempDir.resolve("small.xml").toFile();

        assertTrue(service.writeSmallPriceList(file, smallPriceListTable()));

        String content = readFile(file);
        assertTrue(content.contains("Workbook"));
        assertTrue(content.contains("do 5,00 kg"));
        assertTrue(content.contains("2.5"));
    }

    @Test
    void writeSmallPriceListRejectsUnsupportedExtension() {
        File file = tempDir.resolve("small.txt").toFile();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.writeSmallPriceList(file, smallPriceListTable())
        );

        assertEquals("Unsupported small price list file type: txt", exception.getMessage());
    }

    @Test
    void parsePriceListRejectsFileWithoutExtension() {
        File file = tempDir.resolve("price").toFile();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.parsePriceList(file)
        );

        assertEquals("Unsupported price list file type: ", exception.getMessage());
    }

    @Test
    void parsePriceListRejectsFileWithTrailingDot() {
        File file = tempDir.resolve("price.").toFile();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.parsePriceList(file)
        );

        assertEquals("Unsupported price list file type: ", exception.getMessage());
    }

    @Test
    void checkTableCompatibilityReturnsFalseWhenRegionTableIsMissingPriceListRegion() {
        PriceList priceList = new PriceList();
        priceList.setEntries(List.of(
                new PriceListEntry(1, 10f, 0.1f, 5f, "BA"),
                new PriceListEntry(2, 20f, 0.2f, 6f, "NR")
        ));

        ArrayList<Range> ranges = new ArrayList<>();
        ranges.add(new Range(80000, 89999));

        RegionTable regionTable = new RegionTable();
        regionTable.setEntries(List.of(
                new RegionTableEntry(1, ranges, "BA")
        ));

        assertFalse(service.checkTableCompatibility(priceList, regionTable));
    }
}