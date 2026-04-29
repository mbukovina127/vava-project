import org.junit.jupiter.api.Test;
import org.shippin.domain.*;
import org.shippin.domain.formatted.*;
import org.shippin.util.Range;
import org.shippin.util.WarehouseConvertor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WarehouseConvertorTest {

    @Test
    void toWarehouseShouldConvertBasicFields() {
        WarehouseFormatted formatted = new WarehouseFormatted("ZBS-BA", "SK 83104 Bratislava");

        Warehouse warehouse = WarehouseConvertor.toWarehouse(formatted);

        assertEquals("SK 83104 Bratislava", warehouse.getName());
        assertEquals("ZBS-BA", warehouse.getRegionName());
    }

    @Test
    void toWarehouseShouldConvertPriceList() {
        WarehouseFormatted formatted = new WarehouseFormatted("ZBS-BA", "SK 83104 Bratislava");

        PriceListFormatted priceListFormatted = new PriceListFormatted();

        LinkedHashMap<String, Float> regions = new LinkedHashMap<>();
        regions.put("BA1", 15.66f);
        regions.put("BA2", 16.80f);
        regions.put("KE", 18.52f);

        priceListFormatted.addRow(new PriceListRow(50f, 0.2f, regions));
        formatted.setPriceList(priceListFormatted);

        Warehouse warehouse = WarehouseConvertor.toWarehouse(formatted);

        PriceList priceList = warehouse.getPriceList();

        assertNotNull(priceList);
        assertEquals(3, priceList.getEntries().size());

        PriceListEntry first = priceList.getEntries().getFirst();

        assertEquals("BA1", first.getZone());
        assertEquals(50f, first.getWeight(), 0.001f);
        assertEquals(0.2f, first.getVolume(), 0.001f);
        assertEquals(15.66f, first.getCost(), 0.001f);

        PriceListEntry last = priceList.getEntries().get(2);

        assertEquals("KE", last.getZone());
        assertEquals(18.52f, last.getCost(), 0.001f);
    }

    @Test
    void toWarehouseShouldConvertRegionTable() {
        WarehouseFormatted formatted = new WarehouseFormatted("ZBS-BA", "SK 83104 Bratislava");

        RegionTableFormatted regionTableFormatted = new RegionTableFormatted();

        RegionTableRow row1 = new RegionTableRow("BA1");
        row1.addRange(new Range(81000, 85999));
        regionTableFormatted.addRow(row1);

        RegionTableRow row2 = new RegionTableRow("BA2");
        row2.addRange(new Range(90001, 91099));
        row2.addRange(new Range(91700, 92099));
        regionTableFormatted.addRow(row2);

        formatted.setRegionTable(regionTableFormatted);

        Warehouse warehouse = WarehouseConvertor.toWarehouse(formatted);

        RegionTable regionTable = warehouse.getRegionTable();

        assertNotNull(regionTable);
        assertEquals(2, regionTable.getEntries().size());

        RegionTableEntry firstEntry = regionTable.getEntries().getFirst();

        assertEquals("BA1", firstEntry.getRegionCode());
        assertEquals(1, firstEntry.getRanges().size());
        assertEquals(81000, firstEntry.getRanges().getFirst().getMin());
        assertEquals(85999, firstEntry.getRanges().getFirst().getMax());

        RegionTableEntry secondEntry = regionTable.getEntries().get(1);

        assertEquals("BA2", secondEntry.getRegionCode());
        assertEquals(2, secondEntry.getRanges().size());
    }

    @Test
    void toWarehouseFormattedShouldConvertBasicFields() {
        Warehouse warehouse = new Warehouse();
        warehouse.setName("SK 83104 Bratislava");
        warehouse.setRegionName("ZBS-BA");

        WarehouseFormatted formatted = WarehouseConvertor.toWarehouseFormatted(warehouse);

        assertEquals("SK 83104 Bratislava", formatted.getName());
        assertEquals("ZBS-BA", formatted.getTitle());
    }

    @Test
    void toWarehouseFormattedShouldGroupPriceListEntries() {
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

        PriceListFormatted priceListFormatted = formatted.getPriceList();

        assertNotNull(priceListFormatted);
        assertEquals(2, priceListFormatted.getRows().size());

        PriceListRow firstRow = priceListFormatted.getRows().getFirst();

        assertEquals(50f, firstRow.getWeight(), 0.001f);
        assertEquals(0.2f, firstRow.getVolume(), 0.001f);
        assertEquals(2, firstRow.getRegions().size());
        assertEquals(15.66f, firstRow.getRegions().get("BA1"), 0.001f);
        assertEquals(16.80f, firstRow.getRegions().get("BA2"), 0.001f);

        PriceListRow secondRow = priceListFormatted.getRows().get(1);

        assertEquals(100f, secondRow.getWeight(), 0.001f);
        assertEquals(1, secondRow.getRegions().size());
        assertEquals(18.46f, secondRow.getRegions().get("BA1"), 0.001f);
    }

    @Test
    void toWarehouseFormattedShouldConvertRegionTable() {
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

        RegionTableFormatted regionTableFormatted = formatted.getRegionTable();

        assertNotNull(regionTableFormatted);
        assertEquals(1, regionTableFormatted.getRows().size());

        RegionTableRow firstRow = regionTableFormatted.getRows().getFirst();

        assertEquals("BA1", firstRow.getRegionCode());
        assertEquals(1, firstRow.getRanges().size());
        assertEquals(81000, firstRow.getRanges().getFirst().getMin());
        assertEquals(85999, firstRow.getRanges().getFirst().getMax());
    }

    @Test
    void toWarehouseShouldAllowNullPriceListAndRegionTable() {
        WarehouseFormatted formatted = new WarehouseFormatted("ZBS-BA", "SK 83104 Bratislava");

        Warehouse warehouse = WarehouseConvertor.toWarehouse(formatted);

        assertNull(warehouse.getPriceList());
        assertNull(warehouse.getRegionTable());
    }

    @Test
    void formattedWarehouseShouldSurviveRoundTripConversion() {
        WarehouseFormatted original = new WarehouseFormatted("ZBS-BA", "SK 83104 Bratislava");

        PriceListFormatted priceListFormatted = new PriceListFormatted();

        LinkedHashMap<String, Float> regions1 = new LinkedHashMap<>();
        regions1.put("BA1", 15.66f);
        regions1.put("BA2", 16.80f);
        priceListFormatted.addRow(new PriceListRow(50f, 0.2f, regions1));

        LinkedHashMap<String, Float> regions2 = new LinkedHashMap<>();
        regions2.put("BA1", 18.46f);
        regions2.put("BA2", 20.14f);
        priceListFormatted.addRow(new PriceListRow(100f, 0.4f, regions2));

        original.setPriceList(priceListFormatted);

        RegionTableFormatted regionTableFormatted = new RegionTableFormatted();

        RegionTableRow regionRow = new RegionTableRow("BA1");
        regionRow.addRange(new Range(81000, 85999));

        regionTableFormatted.addRow(regionRow);
        original.setRegionTable(regionTableFormatted);

        Warehouse warehouse = WarehouseConvertor.toWarehouse(original);
        WarehouseFormatted roundTripped = WarehouseConvertor.toWarehouseFormatted(warehouse);

        assertEquals(original.getName(), roundTripped.getName());
        assertEquals(original.getTitle(), roundTripped.getTitle());

        assertNotNull(roundTripped.getPriceList());
        assertEquals(
                original.getPriceList().getRows().size(),
                roundTripped.getPriceList().getRows().size()
        );

        PriceListRow originalFirstRow = original.getPriceList().getRows().getFirst();
        PriceListRow roundTrippedFirstRow = roundTripped.getPriceList().getRows().getFirst();

        assertEquals(originalFirstRow.getWeight(), roundTrippedFirstRow.getWeight(), 0.001f);
        assertEquals(
                originalFirstRow.getRegions().get("BA1"),
                roundTrippedFirstRow.getRegions().get("BA1"),
                0.001f
        );

        assertNotNull(roundTripped.getRegionTable());
        assertEquals(
                original.getRegionTable().getRows().size(),
                roundTripped.getRegionTable().getRows().size()
        );
    }

    @Test
    void smallPriceListShouldConvertToDomainModel() {
        SmallPriceListFormatted formatted = new SmallPriceListFormatted();

        formatted.addRow(new SmallPriceListRow(1f, 3.29f));
        formatted.addRow(new SmallPriceListRow(3f, 3.57f));
        formatted.addRow(new SmallPriceListRow(5f, 3.64f));

        SmallPriceList smallPriceList = WarehouseConvertor.toSmallPriceList(formatted);

        assertNotNull(smallPriceList);
        assertEquals(3, smallPriceList.getEntries().size());

        SmallPriceListEntry first = smallPriceList.getEntries().getFirst();

        assertEquals(1f, first.getWeight(), 0.001f);
        assertEquals(3.29f, first.getCost(), 0.001f);

        SmallPriceListEntry second = smallPriceList.getEntries().get(1);

        assertEquals(3f, second.getWeight(), 0.001f);
        assertEquals(3.57f, second.getCost(), 0.001f);

        SmallPriceListEntry third = smallPriceList.getEntries().get(2);

        assertEquals(5f, third.getWeight(), 0.001f);
        assertEquals(3.64f, third.getCost(), 0.001f);
    }

    @Test
    void smallPriceListShouldConvertToFormattedModel() {
        SmallPriceList smallPriceList = new SmallPriceList();

        List<SmallPriceListEntry> entries = new ArrayList<>();
        entries.add(new SmallPriceListEntry(0, 1f, 3.29f));
        entries.add(new SmallPriceListEntry(0, 3f, 3.57f));
        entries.add(new SmallPriceListEntry(0, 5f, 3.64f));

        smallPriceList.setEntries(entries);

        SmallPriceListFormatted formatted = WarehouseConvertor.toSmallPriceListFormatted(smallPriceList);

        assertNotNull(formatted);
        assertEquals(3, formatted.getRows().size());

        SmallPriceListRow first = formatted.getRows().getFirst();

        assertEquals(1f, first.getWeight(), 0.001f);
        assertEquals(3.29f, first.getCost(), 0.001f);

        SmallPriceListRow second = formatted.getRows().get(1);

        assertEquals(3f, second.getWeight(), 0.001f);
        assertEquals(3.57f, second.getCost(), 0.001f);

        SmallPriceListRow third = formatted.getRows().get(2);

        assertEquals(5f, third.getWeight(), 0.001f);
        assertEquals(3.64f, third.getCost(), 0.001f);
    }
}