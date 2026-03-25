package org.shippin.infrastructure.csv;

import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.SmallPriceListRow;
import org.shippin.domain.Table;
import org.shippin.util.NumberUtils;

import java.util.List;


public class SmallPriceListCsvParser implements CsvParser<SmallPriceListRow> {

    @Override
    public Table<SmallPriceListRow> parseFromCsv(String text) {
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

            float cost = NumberUtils.parseFloat(costStr);

            SmallPriceListRow row = new SmallPriceListRow(weightLimit, cost);
            table.addRow(row);
        }

        return table;
    }

    @Override
    public String exportToCsv(Table<SmallPriceListRow> table) {
        //safety check
        if (!(table instanceof SmallPriceListFormatted splf)) {
            return "";
        }

        List<SmallPriceListRow> rows = splf.getRows();
        if (rows.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("Hmotnosť;Cena\n");

        for (SmallPriceListRow row : rows) {
            float weight = row.getWeight();
            float cost   = row.getCost();

            // Format weight as "do X kg"
            String weightStr = "do " + NumberUtils.formatFloat(weight) + " kg";

            sb.append(weightStr)
                    .append(";")
                    .append(NumberUtils.formatFloat(cost))
                    .append("\n");
        }

        return sb.toString();
    }
}