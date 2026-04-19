import org.shippin.domain.*;
import org.shippin.domain.formatted.*;
import org.shippin.util.Range;
import org.shippin.util.WarehouseConvertor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Tests for WarehouseConvertor (bidirectional conversion between formatted CSV and domain DB models)
 */
public class WarehouseConvertorTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {

        System.out.println("=== WarehouseConvertor Tests ===\n");

        testToWarehouse_BasicFields();
        testToWarehouse_PriceListConversion();
        testToWarehouse_RegionTableConversion();
        testToWarehouseFormatted_BasicFields();
        testToWarehouseFormatted_PriceListGrouping();
        testToWarehouseFormatted_RegionTableConversion();
        testToWarehouse_NullPriceListAndRegionTable();
        testRoundTrip_FormattedToWarehouseAndBack();
        testSmallPriceList_Conversion();
        testSmallPriceListFormatted_Conversion();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
    }

    private static void testToWarehouse_BasicFields() {
        System.out.println("-- testToWarehouse_BasicFields --");

        WarehouseFormatted formatted = new WarehouseFormatted("ZBS-BA", "SK 83104 Bratislava");

        Warehouse warehouse = WarehouseConvertor.toWarehouse(formatted);

        assertEquals("name", "SK 83104 Bratislava", warehouse.getName());
        assertEquals("regionName", "ZBS-BA", warehouse.getRegionName());
    }

    private static void testToWarehouse_PriceListConversion() {
        System.out.println("-- testToWarehouse_PriceListConversion --");

        WarehouseFormatted formatted = new WarehouseFormatted("ZBS-BA", "SK 83104 Bratislava");

        PriceListFormatted plFormatted = new PriceListFormatted();
        LinkedHashMap<String, Float> regions = new LinkedHashMap<>();
        regions.put("BA1", 15.66f);
        regions.put("BA2", 16.80f);
        regions.put("KE", 18.52f);
        plFormatted.addRow(new PriceListRow(50f, 0.2f, regions));

        formatted.setPriceList(plFormatted);

        Warehouse warehouse = WarehouseConvertor.toWarehouse(formatted);

        PriceList priceList = warehouse.getPriceList();
        assertNotNull("priceList", priceList);

        List<PriceListEntry> entries = priceList.getEntries();
        assertEquals("priceList entry count", 3, entries.size());

        PriceListEntry first = entries.getFirst();
        assertEquals("first entry zone", "BA1", first.getZone());
        assertFloatEquals("first entry weight", 50f, first.getWeight());
        assertFloatEquals("first entry volume", 0.2f, first.getVolume());
        assertFloatEquals("first entry cost", 15.66f, first.getCost());

        PriceListEntry last = entries.get(2);
        assertEquals("last entry zone", "KE", last.getZone());
        assertFloatEquals("last entry cost", 18.52f, last.getCost());
    }

    private static void testToWarehouse_RegionTableConversion() {
        System.out.println("-- testToWarehouse_RegionTableConversion --");

        WarehouseFormatted formatted = new WarehouseFormatted("ZBS-BA", "SK 83104 Bratislava");

        RegionTableFormatted rtFormatted = new RegionTableFormatted();
        RegionTableRow row1 = new RegionTableRow("BA1");
        row1.addRange(new Range(81000, 85999));
        rtFormatted.addRow(row1);

        RegionTableRow row2 = new RegionTableRow("BA2");
        row2.addRange(new Range(90001, 91099));
        row2.addRange(new Range(91700, 92099));
        rtFormatted.addRow(row2);

        formatted.setRegionTable(rtFormatted);

        Warehouse warehouse = WarehouseConvertor.toWarehouse(formatted);

        RegionTable regionTable = warehouse.getRegionTable();
        assertNotNull("regionTable", regionTable);

        List<RegionTableEntry> entries = regionTable.getEntries();
        assertEquals("regionTable entry count", 2, entries.size());

        RegionTableEntry firstEntry = entries.getFirst();
        assertEquals("first entry regionCode", "BA1", firstEntry.getRegionCode());
        assertEquals("first entry range count", 1, firstEntry.getRanges().size());
        assertEquals("first entry range min", 81000, firstEntry.getRanges().getFirst().getMin());
        assertEquals("first entry range max", 85999, firstEntry.getRanges().getFirst().getMax());

        RegionTableEntry secondEntry = entries.get(1);
        assertEquals("second entry regionCode", "BA2", secondEntry.getRegionCode());
        assertEquals("second entry range count", 2, secondEntry.getRanges().size());
    }

    private static void testToWarehouseFormatted_BasicFields() {
        System.out.println("-- testToWarehouseFormatted_BasicFields --");

        Warehouse warehouse = new Warehouse();
        warehouse.setName("SK 83104 Bratislava");
        warehouse.setRegionName("ZBS-BA");

        WarehouseFormatted formatted = WarehouseConvertor.toWarehouseFormatted(warehouse);

        assertEquals("name", "SK 83104 Bratislava", formatted.getName());
        assertEquals("title", "ZBS-BA", formatted.getTitle());
    }

    private static void testToWarehouseFormatted_PriceListGrouping() {
        System.out.println("-- testToWarehouseFormatted_PriceListGrouping --");

        Warehouse warehouse = new Warehouse();
        warehouse.setName("SK 83104 Bratislava");
        warehouse.setRegionName("ZBS-BA");

        PriceList priceList = new PriceList();
        List<PriceListEntry> entries = new ArrayList<>();
        entries.add(new PriceListEntry(0, 50f, 0.2f, 15.66f, "BA1"));
        entries.add(new PriceListEntry(0, 50f, 0.2f, 16.80f, "BA2"));
        entries.add(new PriceListEntry(0, 100f, 0.4f, 18.46f, "BA1"));
        priceList.setEntries(entries);
        warehouse.setPriceList(priceList);

        WarehouseFormatted formatted = WarehouseConvertor.toWarehouseFormatted(warehouse);

        PriceListFormatted plFormatted = formatted.getPriceList();
        assertNotNull("priceListFormatted", plFormatted);

        List<PriceListRow> rows = plFormatted.getRows();
        assertEquals("grouped row count", 2, rows.size());

        PriceListRow firstRow = rows.getFirst();
        assertFloatEquals("first row weight", 50f, firstRow.getWeight());
        assertFloatEquals("first row volume", 0.2f, firstRow.getVolume());
        assertEquals("first row region count", 2, firstRow.getRegions().size());
        assertFloatEquals("first row BA1 price", 15.66f, firstRow.getRegions().get("BA1"));
        assertFloatEquals("first row BA2 price", 16.80f, firstRow.getRegions().get("BA2"));

        PriceListRow secondRow = rows.get(1);
        assertFloatEquals("second row weight", 100f, secondRow.getWeight());
        assertEquals("second row region count", 1, secondRow.getRegions().size());
    }

    private static void testToWarehouseFormatted_RegionTableConversion() {
        System.out.println("-- testToWarehouseFormatted_RegionTableConversion --");

        Warehouse warehouse = new Warehouse();
        warehouse.setName("SK 83104 Bratislava");
        warehouse.setRegionName("ZBS-BA");

        RegionTable regionTable = new RegionTable();
        List<RegionTableEntry> entries = new ArrayList<>();
        ArrayList<Range> ranges = new ArrayList<>();
        ranges.add(new Range(81000, 85999));
        entries.add(new RegionTableEntry(1, ranges, "BA1"));
        regionTable.setEntries(entries);
        warehouse.setRegionTable(regionTable);

        WarehouseFormatted formatted = WarehouseConvertor.toWarehouseFormatted(warehouse);

        RegionTableFormatted rtFormatted = formatted.getRegionTable();
        assertNotNull("regionTableFormatted", rtFormatted);

        List<RegionTableRow> rows = rtFormatted.getRows();
        assertEquals("row count", 1, rows.size());
        assertEquals("regionCode", "BA1", rows.getFirst().getRegionCode());
        assertEquals("range count", 1, rows.getFirst().getRanges().size());
        assertEquals("range min", 81000, rows.getFirst().getRanges().getFirst().getMin());
        assertEquals("range max", 85999, rows.getFirst().getRanges().getFirst().getMax());
    }

    private static void testToWarehouse_NullPriceListAndRegionTable() {
        System.out.println("-- testToWarehouse_NullPriceListAndRegionTable --");

        WarehouseFormatted formatted = new WarehouseFormatted("ZBS-BA", "SK 83104 Bratislava");

        Warehouse warehouse = WarehouseConvertor.toWarehouse(formatted);

        assertNull("priceList should be null", warehouse.getPriceList());
        assertNull("regionTable should be null", warehouse.getRegionTable());
    }

    private static void testRoundTrip_FormattedToWarehouseAndBack() {
        System.out.println("-- testRoundTrip_FormattedToWarehouseAndBack --");

        WarehouseFormatted original = new WarehouseFormatted("ZBS-BA", "SK 83104 Bratislava");

        PriceListFormatted plFormatted = new PriceListFormatted();
        LinkedHashMap<String, Float> regions1 = new LinkedHashMap<>();
        regions1.put("BA1", 15.66f);
        regions1.put("BA2", 16.80f);
        plFormatted.addRow(new PriceListRow(50f, 0.2f, regions1));

        LinkedHashMap<String, Float> regions2 = new LinkedHashMap<>();
        regions2.put("BA1", 18.46f);
        regions2.put("BA2", 20.14f);
        plFormatted.addRow(new PriceListRow(100f, 0.4f, regions2));
        original.setPriceList(plFormatted);

        RegionTableFormatted rtFormatted = new RegionTableFormatted();
        RegionTableRow rtRow = new RegionTableRow("BA1");
        rtRow.addRange(new Range(81000, 85999));
        rtFormatted.addRow(rtRow);
        original.setRegionTable(rtFormatted);

        // Formatted -> Warehouse -> Formatted
        Warehouse warehouse = WarehouseConvertor.toWarehouse(original);
        WarehouseFormatted roundTripped = WarehouseConvertor.toWarehouseFormatted(warehouse);

        assertEquals("roundtrip name", original.getName(), roundTripped.getName());
        assertEquals("roundtrip title", original.getTitle(), roundTripped.getTitle());

        assertEquals("roundtrip priceList row count",
                original.getPriceList().getRows().size(),
                roundTripped.getPriceList().getRows().size());

        PriceListRow originalRow = original.getPriceList().getRows().getFirst();
        PriceListRow roundTrippedRow = roundTripped.getPriceList().getRows().getFirst();
        assertFloatEquals("roundtrip first row weight", originalRow.getWeight(), roundTrippedRow.getWeight());
        assertFloatEquals("roundtrip first row BA1 price",
                originalRow.getRegions().get("BA1"),
                roundTrippedRow.getRegions().get("BA1"));

        assertEquals("roundtrip regionTable row count",
                original.getRegionTable().getRows().size(),
                roundTripped.getRegionTable().getRows().size());
    }

    private static void testSmallPriceList_Conversion() {
        System.out.println("-- testSmallPriceList_Conversion --");

        SmallPriceListFormatted formatted = new SmallPriceListFormatted();
        formatted.addRow(new SmallPriceListRow(1f, 3.29f));
        formatted.addRow(new SmallPriceListRow(3f, 3.57f));
        formatted.addRow(new SmallPriceListRow(5f, 3.64f));

        SmallPriceList smallPriceList = WarehouseConvertor.toSmallPriceList(formatted);

        assertNotNull("smallPriceList", smallPriceList);
        List<SmallPriceListEntry> entries = smallPriceList.getEntries();
        assertEquals("smallPriceList entry count", 3, entries.size());

        SmallPriceListEntry first = entries.getFirst();
        assertFloatEquals("first entry weight", 1f, first.getWeight());
        assertFloatEquals("first entry cost", 3.29f, first.getCost());

        SmallPriceListEntry second = entries.get(1);
        assertFloatEquals("second entry weight", 3f, second.getWeight());
        assertFloatEquals("second entry cost", 3.57f, second.getCost());

        SmallPriceListEntry third = entries.get(2);
        assertFloatEquals("third entry weight", 5f, third.getWeight());
        assertFloatEquals("third entry cost", 3.64f, third.getCost());
    }

    private static void testSmallPriceListFormatted_Conversion() {
        System.out.println("-- testSmallPriceListFormatted_Conversion --");

        SmallPriceList smallPriceList = new SmallPriceList();
        List<SmallPriceListEntry> entries = new ArrayList<>();
        entries.add(new SmallPriceListEntry(0, 1f, 3.29f));
        entries.add(new SmallPriceListEntry(0, 3f, 3.57f));
        entries.add(new SmallPriceListEntry(0, 5f, 3.64f));
        smallPriceList.setEntries(entries);

        SmallPriceListFormatted formatted = WarehouseConvertor.toSmallPriceListFormatted(smallPriceList);

        assertNotNull("smallPriceListFormatted", formatted);
        List<SmallPriceListRow> rows = formatted.getRows();
        assertEquals("row count", 3, rows.size());

        SmallPriceListRow first = rows.getFirst();
        assertFloatEquals("first row weight", 1f, first.getWeight());
        assertFloatEquals("first row cost", 3.29f, first.getCost());

        SmallPriceListRow second = rows.get(1);
        assertFloatEquals("second row weight", 3f, second.getWeight());
        assertFloatEquals("second row cost", 3.57f, second.getCost());

        SmallPriceListRow third = rows.get(2);
        assertFloatEquals("third row weight", 5f, third.getWeight());
        assertFloatEquals("third row cost", 3.64f, third.getCost());
    }

    // --- assertion helpers ---

    private static void assertEquals(String label, Object expected, Object actual) {
        if (Objects.equals(expected, actual)) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label + " — expected: " + expected + ", got: " + actual);
            failed++;
        }
    }

    private static void assertFloatEquals(String label, float expected, float actual) {
        if (Math.abs(expected - actual) < 0.001f) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label + " — expected: " + expected + ", got: " + actual);
            failed++;
        }
    }

    private static void assertNotNull(String label, Object obj) {
        if (obj != null) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label + " — expected not null");
            failed++;
        }
    }

    private static void assertNull(String label, Object obj) {
        if (obj == null) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label + " — expected null, got: " + obj);
            failed++;
        }
    }
}