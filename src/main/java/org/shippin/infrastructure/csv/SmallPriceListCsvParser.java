package org.shippin.infrastructure.csv;

import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.SmallPriceListRow;
import org.shippin.domain.Row;
import org.shippin.domain.Table;

import java.util.List;


public class SmallPriceListCsvParser implements CsvParser {

    @Override
    public Table parseFromCsv(String text) {
        SmallPriceListFormatted table = new SmallPriceListFormatted();

        //safety check
        if (text == null || text.trim().isEmpty()) {
            return table;
        }

        String[] lines = text.split("\\r?\\n");
        if (lines.length < 2) {
            return table;
        }

        // skip header row
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] fields = line.split(";");
            if (fields.length < 2) continue;

            String weightDesc = fields[0].trim();
            String costStr   = fields[1].trim();

            // parse the upper weight limit from "do X kg"
            float weightLimit = 0f;
            if (weightDesc.startsWith("do ")) {
                String numPart = weightDesc.substring(3).replace(" kg", "").trim();
                try {
                    weightLimit = Float.parseFloat(numPart.replace(',', '.'));
                } catch (NumberFormatException ignored) {
                    continue;
                }
            }

            float cost = parseFloat(costStr);

            SmallPriceListRow row = new SmallPriceListRow(weightLimit, cost);
            table.addRow(row);
        }

        return table;
    }

    @Override
    public String exportToCsv(Table table) {
        //safety check
        if (!(table instanceof SmallPriceListFormatted splf)) {
            return "";
        }

        List<Row> rows = splf.getRows();
        if (rows.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("Hmotnosť;Cena\n");

        for (Row r : rows) {
            SmallPriceListRow row = (SmallPriceListRow) r;
            float weight = row.getWeight();
            float cost   = row.getCost();

            // Format weight as "do X kg"
            String weightStr = "do " + formatFloat(weight) + " kg";

            sb.append(weightStr)
                    .append(";")
                    .append(formatFloat(cost))
                    .append("\n");
        }

        return sb.toString();
    }

    private float parseFloat(String s) {
        if (s == null || s.trim().isEmpty()) return 0f;
        String normalized = s.trim().replace(',', '.').replaceAll("[^0-9.\\-]", "");
        try {
            return Float.parseFloat(normalized);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }


    private String formatFloat(float f) {
        String s = String.format("%.2f", f);
        return s.replace('.', ',');
    }
}