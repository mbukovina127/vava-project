package org.shippin.infrastructure.csv;

import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.PriceListRow;
import org.shippin.domain.Table;
import org.shippin.util.NumberUtils;
import java.util.ArrayList;
import java.util.List;


public class PriceListCsvParser implements CsvParser<PriceListRow> {

    @Override
    public Table<PriceListRow> parseFromCsv(String text) {
        PriceListFormatted table = new PriceListFormatted();

        //safety check
        if (text == null || text.trim().isEmpty()) {
            return table;
        }

        String[] lines = text.split("\\r?\\n");
        if (lines.length < 3) {
            return table;
        }

        //load region codes
        String[] regionLineParts = lines[1].split(";");
        List<String> regionCodes = new ArrayList<>();
        for (int i = 2; i < regionLineParts.length; i++) {
            String code = regionLineParts[i].trim();
            if (!code.isEmpty()) {
                regionCodes.add(code);
            }
        }

        //load weight + volume and region prices for each line
        for (int i = 2; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] fields = line.split(";");
            if (fields.length < 2 + regionCodes.size()) continue;

            float weight = NumberUtils.parseFloat(fields[0]);
            float volume = NumberUtils.parseFloat(fields[1]);

            PriceListRow row = new PriceListRow(weight, volume);

            for (int j = 0; j < regionCodes.size(); j++) {
                int colIndex = 2 + j;
                if (colIndex < fields.length) {
                    float price = NumberUtils.parseFloat(fields[colIndex]);
                    row.getRegions().put(regionCodes.get(j), price);
                }
            }

            table.addRow(row);
        }

        return table;
    }

    @Override
    public String exportToCsv(Table<PriceListRow> table) {
        //safety check
        if (!(table instanceof PriceListFormatted plf)) {
            return "";
        }

        List<PriceListRow> rows = plf.getRows();
        if (rows.isEmpty()) {
            return "";
        }

        // optain regions (from first line)
        PriceListRow firstRow = rows.getFirst();
        List<String> regionCodes = new ArrayList<>(firstRow.getRegions().keySet());
        int numRegions = regionCodes.size();

        StringBuilder sb = new StringBuilder();

        //build header
        sb.append("Hmotnosť do (v kg);Objem do (v m³);Zóny");
        sb.append(";".repeat(Math.max(0, numRegions - 1)));
        sb.append("\n");

        //add region codes
        sb.append(";;");
        sb.append(String.join(";", regionCodes));
        sb.append("\n");

        //build data for each line
        for (PriceListRow row : rows) {

            String weightStr = NumberUtils.formatFloat(row.getWeight());
            String volumeStr = NumberUtils.formatFloat(row.getVolume());

            List<String> priceStrs = new ArrayList<>();
            for (String reg : regionCodes) {
                Float price = row.getRegions().get(reg);
                priceStrs.add(NumberUtils.formatFloat(price != null ? price : 0f));
            }

            sb.append(weightStr)
                    .append(";")
                    .append(volumeStr)
                    .append(";")
                    .append(String.join(";", priceStrs))
                    .append("\n");
        }

        return sb.toString();
    }

}