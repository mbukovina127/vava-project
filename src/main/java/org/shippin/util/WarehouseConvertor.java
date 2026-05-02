package org.shippin.util;

import org.shippin.domain.*;
import org.shippin.domain.formatted.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Bidirectional converter between formatted (CSV) and domain (DB) warehouse models.
 */
public class WarehouseConvertor {

    public static Warehouse toWarehouse(WarehouseFormatted formatted) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(formatted.getName());
        warehouse.setRegionName(formatted.getTitle());

        if (formatted.getPriceList() != null) {
            warehouse.setPriceList(convertPriceList(formatted.getPriceList()));
        }

        if (formatted.getRegionTable() != null) {
            warehouse.setRegionTable(convertRegionTable(formatted.getRegionTable()));
        }

        return warehouse;
    }

    public static WarehouseFormatted toWarehouseFormatted(Warehouse warehouse) {
        WarehouseFormatted formatted = new WarehouseFormatted();
        formatted.setName(warehouse.getName());
        formatted.setTitle(warehouse.getRegionName());

        if (warehouse.getPriceList() != null) {
            formatted.setPriceList(convertPriceListFormatted(warehouse.getPriceList()));
        }

        if (warehouse.getRegionTable() != null) {
            formatted.setRegionTable(convertRegionTableFormatted(warehouse.getRegionTable()));
        }

        return formatted;
    }

    public static PriceList convertPriceList(PriceListFormatted formatted) {
        PriceList priceList = new PriceList();
        List<PriceListEntry> entries = new ArrayList<>();

        for (PriceListRow row : formatted.getRows()) {
            for (var regionEntry : row.getRegions().entrySet()) {
                PriceListEntry entry = new PriceListEntry();
                entry.setWeight(row.getWeight());
                entry.setVolume(row.getVolume());
                entry.setZone(regionEntry.getKey());
                entry.setCost(regionEntry.getValue());
                entries.add(entry);
            }
        }

        priceList.setEntries(entries);
        return priceList;
    }

    public static RegionTable convertRegionTable(RegionTableFormatted formatted) {
        RegionTable regionTable = new RegionTable();
        List<RegionTableEntry> entries = new ArrayList<>();

        for (RegionTableRow row : formatted.getRows()) {
            RegionTableEntry entry = new RegionTableEntry(
                    0,
                    new ArrayList<>(row.getRanges()),
                    row.getRegionCode()
            );
            entries.add(entry);
        }

        regionTable.setEntries(entries);
        return regionTable;
    }

    public static PriceListFormatted convertPriceListFormatted(PriceList priceList) {
        PriceListFormatted formatted = new PriceListFormatted();

        LinkedHashMap<String, PriceListRow> rowMap = new LinkedHashMap<>();

        for (PriceListEntry entry : priceList.getEntries()) {
            String key = entry.getWeight() + "|" + entry.getVolume();

            PriceListRow row = rowMap.get(key);
            if (row == null) {
                row = new PriceListRow(entry.getWeight(), entry.getVolume(), new LinkedHashMap<>());
                rowMap.put(key, row);
            }
            row.getRegions().put(entry.getZone(), entry.getCost());
        }

        for (PriceListRow row : rowMap.values()) {
            formatted.addRow(row);
        }

        return formatted;
    }

    public static RegionTableFormatted convertRegionTableFormatted(RegionTable regionTable) {
        RegionTableFormatted formatted = new RegionTableFormatted();

        for (RegionTableEntry entry : regionTable.getEntries()) {
            RegionTableRow row = new RegionTableRow(
                    entry.getRegionCode(),
                    new ArrayList<>(entry.getRanges())
            );
            formatted.addRow(row);
        }

        return formatted;
    }

    public static SmallPriceList toSmallPriceList(SmallPriceListFormatted formatted) {
        SmallPriceList smallPriceList = new SmallPriceList();
        List<SmallPriceListEntry> entries = new ArrayList<>();

        for (SmallPriceListRow row : formatted.getRows()) {
            SmallPriceListEntry entry = new SmallPriceListEntry();
            entry.setWeight(row.getWeight());
            entry.setCost(row.getCost());
            entries.add(entry);
        }

        smallPriceList.setEntries(entries);
        return smallPriceList;
    }

    public static SmallPriceListFormatted toSmallPriceListFormatted(SmallPriceList smallPriceList) {
        SmallPriceListFormatted formatted = new SmallPriceListFormatted();

        for (SmallPriceListEntry entry : smallPriceList.getEntries()) {
            SmallPriceListRow row = new SmallPriceListRow(entry.getWeight(), entry.getCost());
            formatted.addRow(row);
        }

        return formatted;
    }
}