package org.shippin.services;

import org.shippin.domain.BriefWarehouse;
import org.shippin.domain.Table;
import org.shippin.domain.Warehouse;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.PriceListRow;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.domain.formatted.RegionTableRow;
import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.SmallPriceListRow;
import org.shippin.domain.formatted.WarehouseFormatted;
import org.shippin.infrastructure.csv.PriceListCsvParser;
import org.shippin.infrastructure.csv.RegionTableCsvParser;
import org.shippin.infrastructure.csv.SmallPriceListCsvParser;
import org.shippin.infrastructure.xml.PriceListXmlParser;
import org.shippin.infrastructure.xml.RegionTableXmlParser;
import org.shippin.infrastructure.xml.SmallPriceListXmlParser;
import org.shippin.util.WarehouseConvertor;
import org.shippin.util.io.TextFileHandler;

import java.io.File;

public class WarehouseParsingService {
	
	private static WarehouseParsingService instance;

    private final TextFileHandler textFileHandler;

    private final PriceListCsvParser priceListCsvParser;
    private final PriceListXmlParser priceListXmlParser;

    private final RegionTableCsvParser regionTableCsvParser;
    private final RegionTableXmlParser regionTableXmlParser;

    private final SmallPriceListCsvParser smallPriceListCsvParser;
    private final SmallPriceListXmlParser smallPriceListXmlParser;

    public WarehouseParsingService() {
        this.textFileHandler = new TextFileHandler();

        this.priceListCsvParser = new PriceListCsvParser();
        this.priceListXmlParser = new PriceListXmlParser();

        this.regionTableCsvParser = new RegionTableCsvParser();
        this.regionTableXmlParser = new RegionTableXmlParser();

        this.smallPriceListCsvParser = new SmallPriceListCsvParser();
        this.smallPriceListXmlParser = new SmallPriceListXmlParser();
    }
    
    public static WarehouseParsingService getInstance() {
		if (instance == null) {
            instance = new WarehouseParsingService();
        }
        return instance;
    }

    // ---------------------------------------------------------------------
    // Price list
    // ---------------------------------------------------------------------

    private PriceListFormatted parsePriceListCsv(File file) {
        String text = textFileHandler.readFrom(file);
        return (PriceListFormatted)priceListCsvParser.parseFromCsv(text);
    }

    private PriceListFormatted parsePriceListXml(File file) {
        String text = textFileHandler.readFrom(file);
        return (PriceListFormatted)priceListXmlParser.parseFromXml(text);
    }

    public PriceListFormatted parsePriceList(File file) {
        String extension = getExtension(file);

        return switch (extension) {
            case "csv" -> parsePriceListCsv(file);
            case "xml" -> parsePriceListXml(file);
            default -> throw new IllegalArgumentException("Unsupported price list file type: " + extension);
        };
    }

    private String exportPriceListToCsv(Table<PriceListRow> table) {
        return priceListCsvParser.exportToCsv(table);
    }

    private String exportPriceListToXml(Table<PriceListRow> table) {
        return priceListXmlParser.exportToXml(table);
    }

    private boolean writePriceListCsv(File file, Table<PriceListRow> table) {
        return textFileHandler.writeTo(file, exportPriceListToCsv(table));
    }

    private boolean writePriceListXml(File file, Table<PriceListRow> table) {
        return textFileHandler.writeTo(file, exportPriceListToXml(table));
    }

    public boolean writePriceList(File file, Table<PriceListRow> table) {
        String extension = getExtension(file);

        return switch (extension) {
            case "csv" -> writePriceListCsv(file, table);
            case "xml" -> writePriceListXml(file, table);
            default -> throw new IllegalArgumentException("Unsupported price list file type: " + extension);
        };
    }

    // ---------------------------------------------------------------------
    // Region table
    // ---------------------------------------------------------------------

    private RegionTableFormatted parseRegionTableCsv(File file) {
        String text = textFileHandler.readFrom(file);
        return (RegionTableFormatted)regionTableCsvParser.parseFromCsv(text);
    }

    private RegionTableFormatted parseRegionTableXml(File file) {
        String text = textFileHandler.readFrom(file);
        return (RegionTableFormatted)regionTableXmlParser.parseFromXml(text);
    }

    public RegionTableFormatted parseRegionTable(File file) {
        String extension = getExtension(file);

        return switch (extension) {
            case "csv" -> parseRegionTableCsv(file);
            case "xml" -> parseRegionTableXml(file);
            default -> throw new IllegalArgumentException("Unsupported region table file type: " + extension);
        };
    }

    private String exportRegionTableToCsv(Table<RegionTableRow> table) {
        return regionTableCsvParser.exportToCsv(table);
    }

    private String exportRegionTableToXml(Table<RegionTableRow> table) {
        return regionTableXmlParser.exportToXml(table);
    }

    private boolean writeRegionTableCsv(File file, Table<RegionTableRow> table) {
        return textFileHandler.writeTo(file, exportRegionTableToCsv(table));
    }

    private boolean writeRegionTableXml(File file, Table<RegionTableRow> table) {
        return textFileHandler.writeTo(file, exportRegionTableToXml(table));
    }

    public boolean writeRegionTable(File file, Table<RegionTableRow> table) {
        String extension = getExtension(file);

        return switch (extension) {
            case "csv" -> writeRegionTableCsv(file, table);
            case "xml" -> writeRegionTableXml(file, table);
            default -> throw new IllegalArgumentException("Unsupported region table file type: " + extension);
        };
    }

    // ---------------------------------------------------------------------
    // Small price list
    // ---------------------------------------------------------------------

    private SmallPriceListFormatted parseSmallPriceListCsv(File file) {
        String text = textFileHandler.readFrom(file);
        return (SmallPriceListFormatted)smallPriceListCsvParser.parseFromCsv(text);
    }

    private SmallPriceListFormatted parseSmallPriceListXml(File file) {
        String text = textFileHandler.readFrom(file);
        return (SmallPriceListFormatted)smallPriceListXmlParser.parseFromXml(text);
    }

    public SmallPriceListFormatted parseSmallPriceList(File file) {
        String extension = getExtension(file);

        return switch (extension) {
            case "csv" -> parseSmallPriceListCsv(file);
            case "xml" -> parseSmallPriceListXml(file);
            default -> throw new IllegalArgumentException("Unsupported small price list file type: " + extension);
        };
    }

    private String exportSmallPriceListToCsv(Table<SmallPriceListRow> table) {
        return smallPriceListCsvParser.exportToCsv(table);
    }

    private String exportSmallPriceListToXml(Table<SmallPriceListRow> table) {
        return smallPriceListXmlParser.exportToXml(table);
    }

    private boolean writeSmallPriceListCsv(File file, Table<SmallPriceListRow> table) {
        return textFileHandler.writeTo(file, exportSmallPriceListToCsv(table));
    }

    private boolean writeSmallPriceListXml(File file, Table<SmallPriceListRow> table) {
        return textFileHandler.writeTo(file, exportSmallPriceListToXml(table));
    }

    public boolean writeSmallPriceList(File file, Table<SmallPriceListRow> table) {
        String extension = getExtension(file);

        return switch (extension) {
            case "csv" -> writeSmallPriceListCsv(file, table);
            case "xml" -> writeSmallPriceListXml(file, table);
            default -> throw new IllegalArgumentException("Unsupported small price list file type: " + extension);
        };
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private String getExtension(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null.");
        }

        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return "";
        }

        return name.substring(dotIndex + 1).toLowerCase();
    }
    
    public void parseTable(BriefWarehouse briefWarehouse, boolean isPriceList, File file) {
    	Warehouse warehouse = WarehouseService.getInstance().getWarehouse(briefWarehouse);
    	WarehouseFormatted warehouseFormatted = WarehouseConvertor.toWarehouseFormatted(warehouse);

    	if(isPriceList) {
    		this.writePriceList(file, warehouseFormatted.getPriceList());
    	}
    	else {
    		this.writeRegionTable(file, warehouseFormatted.getRegionTable());
    	}
    }
}