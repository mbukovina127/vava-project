import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.PriceListRow;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.domain.formatted.RegionTableRow;
import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.SmallPriceListRow;
import org.shippin.exception.ValidationException;
import org.shippin.infrastructure.csv.PriceListCsvParser;
import org.shippin.infrastructure.csv.RegionTableCsvParser;
import org.shippin.infrastructure.csv.SmallPriceListCsvParser;
import org.shippin.infrastructure.xml.PriceListXmlParser;
import org.shippin.infrastructure.xml.RegionTableXmlParser;
import org.shippin.infrastructure.xml.SmallPriceListXmlParser;
import org.shippin.util.Range;

public class ParserTest {

    private static final Random RANDOM = new Random(25);

    @Test
    void priceListParserShouldParseRandomCsv() throws ValidationException {
        String priceCsv = generatePriceCsv(10);

        PriceListCsvParser parser = new PriceListCsvParser();
        PriceListFormatted table = (PriceListFormatted) parser.parseFromCsv(priceCsv);

        assertNotNull(table);
        assertEquals(10, table.getRows().size());

        PriceListRow firstRow = table.getRows().getFirst();

        assertTrue(firstRow.getWeight() > 0);
        assertTrue(firstRow.getVolume() > 0);
        assertEquals(6, firstRow.getRegions().size());

        assertTrue(firstRow.getRegions().containsKey("BA1"));
        assertTrue(firstRow.getRegions().containsKey("BA2"));
        assertTrue(firstRow.getRegions().containsKey("BA3"));
        assertTrue(firstRow.getRegions().containsKey("ZA"));
        assertTrue(firstRow.getRegions().containsKey("ZV"));
        assertTrue(firstRow.getRegions().containsKey("KE"));
    }

    @Test
    void priceListParserShouldExportParsedCsv() throws ValidationException {
        String priceCsv = generatePriceCsv(5);

        PriceListCsvParser parser = new PriceListCsvParser();
        PriceListFormatted table = (PriceListFormatted) parser.parseFromCsv(priceCsv);

        String exportedCsv = parser.exportToCsv(table);

        assertNotNull(exportedCsv);
        assertFalse(exportedCsv.isBlank());
        assertTrue(exportedCsv.contains("Hmotnosť do (v kg)"));
        assertTrue(exportedCsv.contains("BA1"));
        assertTrue(exportedCsv.contains("KE"));
    }

    @Test
    void regionTableParserShouldParseRandomCsv() throws ValidationException {
        String regionCsv = generateRegionCsv();

        RegionTableCsvParser parser = new RegionTableCsvParser();
        RegionTableFormatted table = (RegionTableFormatted) parser.parseFromCsv(regionCsv);

        assertNotNull(table);
        assertEquals(4, table.getRows().size());

        for (RegionTableRow row : table.getRows()) {
            assertNotNull(row.getRegionCode());
            assertFalse(row.getRegionCode().isBlank());
            assertFalse(row.getRanges().isEmpty());

            for (Range range : row.getRanges()) {
                assertTrue(range.getMin() <= range.getMax());
            }
        }
    }

    @Test
    void regionTableParserShouldFindPostalCodeInsideGeneratedRange() throws ValidationException {
        int min = 82100;
        int max = 82199;

        String regionCsv = """
                Rozdelenie PSČ:;;;;;;
                BA1;;82100-82199;;;;
                BA2;;90001-91099;;;;
                """;

        RegionTableCsvParser parser = new RegionTableCsvParser();
        RegionTableFormatted table = (RegionTableFormatted) parser.parseFromCsv(regionCsv);

        boolean found = false;

        for (RegionTableRow row : table.getRows()) {
            for (Range range : row.getRanges()) {
                if (range.contains(82105)) {
                    found = true;
                }
            }
        }

        assertTrue(found);
    }

    @Test
    void smallPriceListParserShouldParseRandomCsv() throws ValidationException {
        String smallPriceCsv = generateSmallPriceCsv(8);

        SmallPriceListCsvParser parser = new SmallPriceListCsvParser();
        SmallPriceListFormatted table = (SmallPriceListFormatted) parser.parseFromCsv(smallPriceCsv);

        assertNotNull(table);
        assertEquals(8, table.getRows().size());

        SmallPriceListRow firstRow = table.getRows().getFirst();

        assertTrue(firstRow.getWeight() > 0);
        assertTrue(firstRow.getCost() > 0);
    }

    @Test
    void smallPriceListParserShouldExportParsedCsv() throws ValidationException {
        String smallPriceCsv = generateSmallPriceCsv(5);

        SmallPriceListCsvParser parser = new SmallPriceListCsvParser();
        SmallPriceListFormatted table = (SmallPriceListFormatted) parser.parseFromCsv(smallPriceCsv);

        String exportedCsv = parser.exportToCsv(table);

        assertNotNull(exportedCsv);
        assertFalse(exportedCsv.isBlank());
        assertTrue(exportedCsv.contains("Hmotnosť"));
        assertTrue(exportedCsv.contains("Cena"));
    }

    private static String generatePriceCsv(int rows) {
        StringBuilder sb = new StringBuilder();

        sb.append("Hmotnosť do (v kg);Objem do (v m³);Zóny;;;;;\n");
        sb.append(";;BA1;BA2;BA3;ZA;ZV;KE\n");

        int weight = 50;
        double volume = 0.2;

        for (int i = 0; i < rows; i++) {
            sb.append(weight).append(";");
            sb.append(format(volume)).append(";");

            for (int j = 0; j < 6; j++) {
                double price = 10 + RANDOM.nextDouble() * 100;
                sb.append(format(price));

                if (j < 5) {
                    sb.append(";");
                }
            }

            sb.append("\n");

            weight += 50;
            volume += 0.2;
        }

        return sb.toString();
    }

    private static String generateRegionCsv() {
        StringBuilder sb = new StringBuilder();

        sb.append("Rozdelenie PSČ:;;;;;;\n");

        String[] regions = {"BA1", "BA2", "ZA", "KE"};
        int start = 10000;

        for (String region : regions) {
            sb.append(region).append(";;");

            for (int i = 0; i < 3; i++) {
                int min = start + RANDOM.nextInt(200);
                int max = min + RANDOM.nextInt(300) + 1;

                sb.append(min).append("-").append(max);

                if (i < 2) {
                    sb.append(";");
                }

                start += 1000;
            }

            sb.append(";\n");
        }

        return sb.toString();
    }

    private static String generateSmallPriceCsv(int rows) {
        StringBuilder sb = new StringBuilder();

        sb.append("Hmotnosť;Cena\n");

        int weight = 1;

        for (int i = 0; i < rows; i++) {
            double price = 2 + RANDOM.nextDouble() * 10;

            sb.append("do ");
            sb.append(weight);
            sb.append(" kg;");
            sb.append(format(price));
            sb.append("\n");

            weight += RANDOM.nextInt(5) + 1;
        }

        return sb.toString();
    }

    @Test
    void priceListXmlParserShouldParseXml() throws ValidationException {
        String priceXml = generatePriceXml(10);

        PriceListXmlParser parser = new PriceListXmlParser();
        PriceListFormatted table = (PriceListFormatted) parser.parseFromXml(priceXml);

        assertNotNull(table);
        assertEquals(10, table.getRows().size());

        PriceListRow firstRow = table.getRows().getFirst();

        assertTrue(firstRow.getWeight() > 0);
        assertTrue(firstRow.getVolume() > 0);
        assertEquals(6, firstRow.getRegions().size());

        assertTrue(firstRow.getRegions().containsKey("BA1"));
        assertTrue(firstRow.getRegions().containsKey("BA2"));
        assertTrue(firstRow.getRegions().containsKey("BA3"));
        assertTrue(firstRow.getRegions().containsKey("ZA"));
        assertTrue(firstRow.getRegions().containsKey("ZV"));
        assertTrue(firstRow.getRegions().containsKey("KE"));
    }

    @Test
    void priceListXmlParserShouldExportParsedXml() throws ValidationException {
        String priceXml = generatePriceXml(5);

        PriceListXmlParser parser = new PriceListXmlParser();
        PriceListFormatted table = (PriceListFormatted) parser.parseFromXml(priceXml);

        String exportedXml = parser.exportToXml(table);

        assertNotNull(exportedXml);
        assertFalse(exportedXml.isBlank());
        assertTrue(exportedXml.contains("BA1"));
        assertTrue(exportedXml.contains("KE"));
    }

    @Test
    void priceListXmlParserShouldRoundFloatPrecision() throws ValidationException {
        String priceXml = """
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
                    <Cell ss:Index="3"><Data ss:Type="String">BA1</Data></Cell>
                   </Row>
                   <Row>
                    <Cell><Data ss:Type="Number">50</Data></Cell>
                    <Cell><Data ss:Type="Number">0.2</Data></Cell>
                    <Cell><Data ss:Type="Number">17.350000000000001</Data></Cell>
                   </Row>
                  </Table>
                 </Worksheet>
                </Workbook>
                """;

        PriceListXmlParser parser = new PriceListXmlParser();
        PriceListFormatted table = (PriceListFormatted) parser.parseFromXml(priceXml);

        assertEquals(1, table.getRows().size());
        assertEquals(17.35f, table.getRows().getFirst().getRegions().get("BA1"), 0.001f);
    }

    @Test
    void regionTableXmlParserShouldParseXml() throws ValidationException {
        String regionXml = generateRegionXml();

        RegionTableXmlParser parser = new RegionTableXmlParser();
        RegionTableFormatted table = (RegionTableFormatted) parser.parseFromXml(regionXml);

        assertNotNull(table);
        assertEquals(4, table.getRows().size());

        for (RegionTableRow row : table.getRows()) {
            assertNotNull(row.getRegionCode());
            assertFalse(row.getRegionCode().isBlank());
            assertFalse(row.getRanges().isEmpty());

            for (Range range : row.getRanges()) {
                assertTrue(range.getMin() <= range.getMax());
            }
        }
    }

    @Test
    void regionTableXmlParserShouldHandleContinuationRows() throws ValidationException {
        String regionXml = """
                <?xml version="1.0"?>
                <Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
                 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
                 <Worksheet ss:Name="regionTable">
                  <Table>
                   <Row>
                    <Cell><Data ss:Type="String">Rozdelenie PSČ:</Data></Cell>
                   </Row>
                   <Row>
                    <Cell><Data ss:Type="String">KE</Data></Cell>
                    <Cell ss:Index="3"><Data ss:Type="String">04000-04900</Data></Cell>
                    <Cell><Data ss:Type="String">04902-04912</Data></Cell>
                   </Row>
                   <Row>
                    <Cell ss:Index="3"><Data ss:Type="String">04965-05000</Data></Cell>
                    <Cell><Data ss:Type="String">05002-06599</Data></Cell>
                   </Row>
                  </Table>
                 </Worksheet>
                </Workbook>
                """;

        RegionTableXmlParser parser = new RegionTableXmlParser();
        RegionTableFormatted table = (RegionTableFormatted) parser.parseFromXml(regionXml);

        assertEquals(1, table.getRows().size());

        RegionTableRow keRow = table.getRows().getFirst();
        assertEquals("KE", keRow.getRegionCode());
        assertEquals(4, keRow.getRanges().size());
    }

    @Test
    void regionTableXmlParserShouldFindPostalCode() throws ValidationException {
        String regionXml = """
                <?xml version="1.0"?>
                <Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
                 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
                 <Worksheet ss:Name="regionTable">
                  <Table>
                   <Row>
                    <Cell><Data ss:Type="String">Rozdelenie PSČ:</Data></Cell>
                   </Row>
                   <Row>
                    <Cell><Data ss:Type="String">BA1</Data></Cell>
                    <Cell ss:Index="3"><Data ss:Type="String">81000-85999</Data></Cell>
                   </Row>
                   <Row>
                    <Cell><Data ss:Type="String">BA2</Data></Cell>
                    <Cell ss:Index="3"><Data ss:Type="String">90001-91099</Data></Cell>
                   </Row>
                  </Table>
                 </Worksheet>
                </Workbook>
                """;

        RegionTableXmlParser parser = new RegionTableXmlParser();
        RegionTableFormatted table = (RegionTableFormatted) parser.parseFromXml(regionXml);

        boolean found = false;
        for (RegionTableRow row : table.getRows()) {
            for (Range range : row.getRanges()) {
                if (range.contains(82105)) {
                    found = true;
                    assertEquals("BA1", row.getRegionCode());
                }
            }
        }

        assertTrue(found);
    }

    @Test
    void smallPriceListXmlParserShouldParseXml() throws ValidationException {
        String smallPriceXml = generateSmallPriceXml(8);

        SmallPriceListXmlParser parser = new SmallPriceListXmlParser();
        SmallPriceListFormatted table = (SmallPriceListFormatted) parser.parseFromXml(smallPriceXml);

        assertNotNull(table);
        assertEquals(8, table.getRows().size());

        SmallPriceListRow firstRow = table.getRows().getFirst();

        assertTrue(firstRow.getWeight() > 0);
        assertTrue(firstRow.getCost() > 0);
    }

    @Test
    void smallPriceListXmlParserShouldExportParsedXml() throws ValidationException {
        String smallPriceXml = generateSmallPriceXml(5);

        SmallPriceListXmlParser parser = new SmallPriceListXmlParser();
        SmallPriceListFormatted table = (SmallPriceListFormatted) parser.parseFromXml(smallPriceXml);

        String exportedXml = parser.exportToXml(table);

        assertNotNull(exportedXml);
        assertFalse(exportedXml.isBlank());
        assertTrue(exportedXml.contains("do "));
        assertTrue(exportedXml.contains("kg"));
    }

    // =====================================================================
    // XML Generators
    // =====================================================================

    private static String generatePriceXml(int rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n");
        sb.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n");
        sb.append(" <Worksheet ss:Name=\"priceList\">\n");
        sb.append("  <Table>\n");

        sb.append("   <Row>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">Hmotnosť do (v kg)</Data></Cell>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">Objem do (v m³)</Data></Cell>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">Zóny</Data></Cell>\n");
        sb.append("   </Row>\n");

        sb.append("   <Row>\n");
        sb.append("    <Cell ss:Index=\"3\"><Data ss:Type=\"String\">BA1</Data></Cell>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">BA2</Data></Cell>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">BA3</Data></Cell>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">ZA</Data></Cell>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">ZV</Data></Cell>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">KE</Data></Cell>\n");
        sb.append("   </Row>\n");

        int weight = 50;
        double volume = 0.2;

        for (int i = 0; i < rows; i++) {
            sb.append("   <Row>\n");
            sb.append("    <Cell><Data ss:Type=\"Number\">").append(weight).append("</Data></Cell>\n");
            sb.append("    <Cell><Data ss:Type=\"Number\">").append(volume).append("</Data></Cell>\n");

            for (int j = 0; j < 6; j++) {
                double price = 10 + RANDOM.nextDouble() * 100;
                sb.append("    <Cell><Data ss:Type=\"Number\">").append(price).append("</Data></Cell>\n");
            }

            sb.append("   </Row>\n");

            weight += 50;
            volume += 0.2;
        }

        sb.append("  </Table>\n");
        sb.append(" </Worksheet>\n");
        sb.append("</Workbook>\n");

        return sb.toString();
    }

    private static String generateRegionXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n");
        sb.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n");
        sb.append(" <Worksheet ss:Name=\"regionTable\">\n");
        sb.append("  <Table>\n");

        sb.append("   <Row>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">Rozdelenie PSČ:</Data></Cell>\n");
        sb.append("   </Row>\n");

        String[] regions = {"BA1", "BA2", "ZA", "KE"};
        int start = 10000;

        for (String region : regions) {
            sb.append("   <Row>\n");
            sb.append("    <Cell><Data ss:Type=\"String\">").append(region).append("</Data></Cell>\n");

            for (int i = 0; i < 3; i++) {
                int min = start + RANDOM.nextInt(200);
                int max = min + RANDOM.nextInt(300) + 1;

                if (i == 0) {
                    sb.append("    <Cell ss:Index=\"3\"><Data ss:Type=\"String\">");
                } else {
                    sb.append("    <Cell><Data ss:Type=\"String\">");
                }
                sb.append(min).append("-").append(max).append("</Data></Cell>\n");

                start += 1000;
            }

            sb.append("   </Row>\n");
        }

        sb.append("  </Table>\n");
        sb.append(" </Worksheet>\n");
        sb.append("</Workbook>\n");

        return sb.toString();
    }

    private static String generateSmallPriceXml(int rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n");
        sb.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n");
        sb.append(" <Worksheet ss:Name=\"smallPriceList\">\n");
        sb.append("  <Table>\n");

        sb.append("   <Row>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">Hmotnosť</Data></Cell>\n");
        sb.append("    <Cell><Data ss:Type=\"String\">Cena</Data></Cell>\n");
        sb.append("   </Row>\n");

        int weight = 1;

        for (int i = 0; i < rows; i++) {
            double price = 2 + RANDOM.nextDouble() * 10;

            sb.append("   <Row>\n");
            sb.append("    <Cell><Data ss:Type=\"String\">do ").append(weight).append(" kg</Data></Cell>\n");
            sb.append("    <Cell><Data ss:Type=\"Number\">").append(price).append("</Data></Cell>\n");
            sb.append("   </Row>\n");

            weight += RANDOM.nextInt(5) + 1;
        }

        sb.append("  </Table>\n");
        sb.append(" </Worksheet>\n");
        sb.append("</Workbook>\n");

        return sb.toString();
    }

    private static String format(double value) {
        return String.format(java.util.Locale.US, "%.2f", value).replace(".", ",");
    }
}