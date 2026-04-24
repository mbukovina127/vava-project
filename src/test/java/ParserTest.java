import org.junit.jupiter.api.Test;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.PriceListRow;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.domain.formatted.RegionTableRow;
import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.SmallPriceListRow;
import org.shippin.infrastructure.csv.PriceListCsvParser;
import org.shippin.infrastructure.csv.RegionTableCsvParser;
import org.shippin.infrastructure.csv.SmallPriceListCsvParser;
import org.shippin.util.Range;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private static final Random RANDOM = new Random(25);

    @Test
    void priceListParserShouldParseRandomCsv() {
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
    void priceListParserShouldExportParsedCsv() {
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
    void regionTableParserShouldParseRandomCsv() {
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
    void regionTableParserShouldFindPostalCodeInsideGeneratedRange() {
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
    void smallPriceListParserShouldParseRandomCsv() {
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
    void smallPriceListParserShouldExportParsedCsv() {
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

    private static String format(double value) {
        return String.format(java.util.Locale.US, "%.2f", value).replace(".", ",");
    }
}