import org.junit.jupiter.api.Test;
import org.shippin.domain.PriceList;
import org.shippin.domain.PriceListEntry;
import org.shippin.domain.RegionTable;
import org.shippin.domain.RegionTableEntry;
import org.shippin.services.ShipmentService;
import org.shippin.util.Range;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ShipmentServiceTest {

    private final ShipmentService shipmentService = new ShipmentService();

    @Test
    void findRegionForPostalCodeReturnsMatchingRegion() throws Exception {
        RegionTable regionTable = createRegionTable();

        String region = invokeFindRegionForPostalCode(regionTable, 82101);

        assertEquals("BA", region);
    }

    @Test
    void findRegionForPostalCodeReturnsSecondMatchingRegion() throws Exception {
        RegionTable regionTable = createRegionTable();

        String region = invokeFindRegionForPostalCode(regionTable, 94901);

        assertEquals("NR", region);
    }

    @Test
    void findRegionForPostalCodeThrowsWhenNoRegionMatches() {
        RegionTable regionTable = createRegionTable();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invokeFindRegionForPostalCode(regionTable, 11111)
        );

        assertTrue(exception.getMessage().contains("No region found"));
    }

    @Test
    void findCostInPriceListByWeightReturnsSmallestMatchingThreshold() throws Exception {
        PriceList priceList = createPriceList();

        float cost = invokeFindCostInPriceList(priceList, "BA", 12f, true);

        assertEquals(7f, cost, 0.001f);
    }

    @Test
    void findCostInPriceListByVolumeReturnsSmallestMatchingThreshold() throws Exception {
        PriceList priceList = createPriceList();

        float cost = invokeFindCostInPriceList(priceList, "BA", 150f, false);

        assertEquals(7f, cost, 0.001f);
    }

    @Test
    void findCostInPriceListIgnoresDifferentRegion() throws Exception {
        PriceList priceList = createPriceList();

        float cost = invokeFindCostInPriceList(priceList, "NR", 12f, true);

        assertEquals(9f, cost, 0.001f);
    }

    @Test
    void findCostInPriceListThrowsWhenNoPriceMatches() {
        PriceList priceList = createPriceList();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invokeFindCostInPriceList(priceList, "BA", 1000f, true)
        );

        assertTrue(exception.getMessage().contains("No price found"));
    }

    private RegionTable createRegionTable() {
        RegionTable regionTable = new RegionTable();

        ArrayList<Range> baRanges = new ArrayList<>();
        baRanges.add(new Range(80000, 89999));

        ArrayList<Range> nrRanges = new ArrayList<>();
        nrRanges.add(new Range(90000, 99999));

        regionTable.setEntries(List.of(
                new RegionTableEntry(1, baRanges, "BA"),
                new RegionTableEntry(2, nrRanges, "NR")
        ));

        return regionTable;
    }

    private PriceList createPriceList() {
        PriceList priceList = new PriceList();

        priceList.setEntries(List.of(
                new PriceListEntry(1, 10f, 100f, 5f, "BA"),
                new PriceListEntry(2, 20f, 200f, 7f, "BA"),
                new PriceListEntry(3, 50f, 500f, 12f, "BA"),
                new PriceListEntry(4, 20f, 200f, 9f, "NR")
        ));

        return priceList;
    }

    private String invokeFindRegionForPostalCode(RegionTable regionTable, int postalCode) throws Exception {
        Method method = ShipmentService.class.getDeclaredMethod(
                "findRegionForPostalCode",
                RegionTable.class,
                int.class
        );

        method.setAccessible(true);

        try {
            return (String) method.invoke(shipmentService, regionTable, postalCode);
        } catch (InvocationTargetException e) {
            throwOriginalException(e);
            return null;
        }
    }

    private float invokeFindCostInPriceList(
            PriceList priceList,
            String regionCode,
            float value,
            boolean byWeight
    ) throws Exception {
        Method method = ShipmentService.class.getDeclaredMethod(
                "findCostInPriceList",
                PriceList.class,
                String.class,
                float.class,
                boolean.class
        );

        method.setAccessible(true);

        try {
            return (Float) method.invoke(shipmentService, priceList, regionCode, value, byWeight);
        } catch (InvocationTargetException e) {
            throwOriginalException(e);
            return -1;
        }
    }

    private void throwOriginalException(InvocationTargetException e) throws Exception {
        Throwable cause = e.getCause();

        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }

        if (cause instanceof Exception) {
            throw (Exception) cause;
        }

        throw e;
    }
}