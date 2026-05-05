package org.shippin.infrastructure.csv;

import org.shippin.infrastructure.validation.SmallPriceListValidator;
import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.SmallPriceListRow;
import org.shippin.exception.ValidationException;
import org.shippin.domain.Table;
import org.shippin.util.NumberUtils;

import java.util.ArrayList;
import java.util.List;


public class SmallPriceListCsvParser implements CsvParser<SmallPriceListRow> {

    @Override
    public Table<SmallPriceListRow> parseFromCsv(String text) throws ValidationException {
        SmallPriceListFormatted table = new SmallPriceListFormatted();

        //safety check
        if (text == null || text.trim().isEmpty()) {
            return table;
        }

        String[] lines = text.split("\r?\n");
        if (lines.length < 2) {
            return table;
        }

        String[] headerFields = lines[0].split(";");
        String headerCol0 = headerFields.length > 0 ? headerFields[0].trim() : "";
        String headerCol1 = headerFields.length > 1 ? headerFields[1].trim() : "";

        List<String> weightDescs = new ArrayList<>();
        List<String> priceStrs   = new ArrayList<>();

        // skip header row
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] fields = line.split(";");
            if (fields.length < 2) continue;

            weightDescs.add(fields[0].trim());
            priceStrs.add(fields[1].trim());
        }

        SmallPriceListValidator.validate(headerCol0, headerCol1, weightDescs, priceStrs);

        for (int i = 0; i < weightDescs.size(); i++) {
            String weightDesc = weightDescs.get(i);
            String costStr    = priceStrs.get(i);

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
